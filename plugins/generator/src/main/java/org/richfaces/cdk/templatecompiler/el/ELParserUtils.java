/**
 * License Agreement.
 *
 * Rich Faces - Natural Ajax for Java Server Faces (JSF)
 *
 * Copyright (C) 2007 Exadel, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package org.richfaces.cdk.templatecompiler.el;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.jboss.el.parser.AstAnd;
import org.jboss.el.parser.AstBracketSuffix;
import org.jboss.el.parser.AstChoice;
import org.jboss.el.parser.AstDeferredExpression;
import org.jboss.el.parser.AstDiv;
import org.jboss.el.parser.AstDynamicExpression;
import org.jboss.el.parser.AstEmpty;
import org.jboss.el.parser.AstEqual;
import org.jboss.el.parser.AstFalse;
import org.jboss.el.parser.AstFloatingPoint;
import org.jboss.el.parser.AstFunction;
import org.jboss.el.parser.AstGreaterThan;
import org.jboss.el.parser.AstGreaterThanEqual;
import org.jboss.el.parser.AstIdentifier;
import org.jboss.el.parser.AstInteger;
import org.jboss.el.parser.AstLessThan;
import org.jboss.el.parser.AstLessThanEqual;
import org.jboss.el.parser.AstLiteralExpression;
import org.jboss.el.parser.AstMethodSuffix;
import org.jboss.el.parser.AstMinus;
import org.jboss.el.parser.AstMod;
import org.jboss.el.parser.AstMult;
import org.jboss.el.parser.AstNegative;
import org.jboss.el.parser.AstNot;
import org.jboss.el.parser.AstNotEqual;
import org.jboss.el.parser.AstNull;
import org.jboss.el.parser.AstOr;
import org.jboss.el.parser.AstPlus;
import org.jboss.el.parser.AstPropertySuffix;
import org.jboss.el.parser.AstString;
import org.jboss.el.parser.AstTrue;
import org.jboss.el.parser.AstValue;
import org.jboss.el.parser.Node;
import org.richfaces.cdk.templatecompiler.el.node.AstBracketSuffixTreeNode;
import org.richfaces.cdk.templatecompiler.el.node.AstChoiceTreeNode;
import org.richfaces.cdk.templatecompiler.el.node.AstDeferredOrDynamicExpressionTreeNode;
import org.richfaces.cdk.templatecompiler.el.node.AstEmptyTreeNode;
import org.richfaces.cdk.templatecompiler.el.node.AstFloatingPointTreeNode;
import org.richfaces.cdk.templatecompiler.el.node.AstFunctionTreeNode;
import org.richfaces.cdk.templatecompiler.el.node.AstIdentifierTreeNode;
import org.richfaces.cdk.templatecompiler.el.node.AstIntegerTreeNode;
import org.richfaces.cdk.templatecompiler.el.node.AstLiteralTreeNode;
import org.richfaces.cdk.templatecompiler.el.node.AstMethodSuffixTreeNode;
import org.richfaces.cdk.templatecompiler.el.node.AstNegativeTreeNode;
import org.richfaces.cdk.templatecompiler.el.node.AstNotTreeNode;
import org.richfaces.cdk.templatecompiler.el.node.AstPropertySuffixTreeNode;
import org.richfaces.cdk.templatecompiler.el.node.AstStringTreeNode;
import org.richfaces.cdk.templatecompiler.el.node.AstValueTreeNode;
import org.richfaces.cdk.templatecompiler.el.node.BinaryArithmeticIntegerOperationTreeNode;
import org.richfaces.cdk.templatecompiler.el.node.BinaryArithmeticOperationTreeNode;
import org.richfaces.cdk.templatecompiler.el.node.BinaryBooleanOperationTreeNode;
import org.richfaces.cdk.templatecompiler.el.node.BinaryBooleanResultOperationTreeNode;
import org.richfaces.cdk.templatecompiler.el.node.ConstantValueTreeNode;
import org.richfaces.cdk.templatecompiler.el.node.EqualityTestTreeNode;
import org.richfaces.cdk.templatecompiler.el.node.ITreeNode;
import org.richfaces.cdk.templatecompiler.el.types.TypesFactory;

/**
 * Class, that encapsulate all functionality, related to Reflection calls, such as loading classes, get property
 * descriptors etc...
 * 
 * @author amarkhel
 * 
 */
public final class ELParserUtils {

    private static final class ClassDataHolder implements ClassVisitor {

        private Map<String, PropertyDescriptor> resolvedProperties;

        private List<Method> resolvedMethods;

        public ClassDataHolder() {
            super();

            this.resolvedProperties = new HashMap<String, PropertyDescriptor>();
            this.resolvedMethods = new ArrayList<Method>();
        }

        public Map<String, PropertyDescriptor> getResolvedProperties() {
            return resolvedProperties;
        }

        public List<Method> getResolvedMethods() {
            return resolvedMethods;
        }

        @Override
        public void visit(Class<?> clazz) throws ParsingException {
            PropertyDescriptor[] pds;
            Method[] declaredMethods;

            try {
                pds = getPropertyDescriptors(clazz);
                declaredMethods = clazz.getDeclaredMethods();
            } catch (LinkageError e) {
                throw new ParsingException(e.getMessage(), e);
            }

            for (PropertyDescriptor descriptor : pds) {
                String descriptorName = descriptor.getName();
                if (resolvedProperties.get(descriptorName) == null) {
                    resolvedProperties.put(descriptorName, descriptor);
                }
            }

            for (Method declaredMethod : declaredMethods) {
                resolvedMethods.add(declaredMethod);
            }
        }
    }

    private static Map<Class<?>, ClassDataHolder> classDataCache = Collections
        .synchronizedMap(new HashMap<Class<?>, ClassDataHolder>());

    private ELParserUtils() {
    }

    private static ClassDataHolder resolveClassPropertiesAndMethods(Class<?> initialClass) throws ParsingException {
        ClassDataHolder classDataHolder = classDataCache.get(initialClass);
        if (classDataHolder == null) {
            classDataHolder = new ClassDataHolder();
            new ClassWalkingLogic(initialClass).walk(classDataHolder);
            classDataCache.put(initialClass, classDataHolder);
        }

        return classDataHolder;
    }

    /**
     * This method determine type of parsed node and create wrapper for them, that extends AbstractTreeNode. If node
     * type is not recognized - throws ParsingException.
     * 
     * @param child
     *            - parsed node
     * @throws ParsingException
     *             - if node type is not recognized.
     * @return wrapper for parsed node(if node type is recognized), that implement ITreeNode interface.
     */
    public static ITreeNode determineNodeType(Node child) throws ParsingException {
        ITreeNode treeNode = null;

        if (child instanceof AstIdentifier) {
            treeNode = new AstIdentifierTreeNode(child);
        } else if (child instanceof AstValue) {
            treeNode = new AstValueTreeNode(child);
        } else if (child instanceof AstInteger) {
            treeNode = new AstIntegerTreeNode(child);
        } else if (child instanceof AstString) {
            treeNode = new AstStringTreeNode(child);
        } else if (child instanceof AstFunction) {
            treeNode = new AstFunctionTreeNode(child);
        } else if (child instanceof AstDeferredExpression || child instanceof AstDynamicExpression) {
            treeNode = new AstDeferredOrDynamicExpressionTreeNode(child);
        } else if (child instanceof AstNot) {
            treeNode = new AstNotTreeNode(child);
        } else if (child instanceof AstChoice) {
            treeNode = new AstChoiceTreeNode(child);
        } else if (child instanceof AstEmpty) {
            treeNode = new AstEmptyTreeNode(child);
        } else if (child instanceof AstLiteralExpression) {
            treeNode = new AstLiteralTreeNode(child);
        } else if (child instanceof AstFalse) {
            treeNode = ConstantValueTreeNode.FALSE_NODE;
        } else if (child instanceof AstTrue) {
            treeNode = ConstantValueTreeNode.TRUE_NODE;
        } else if (child instanceof AstNull) {
            treeNode = ConstantValueTreeNode.NULL_NODE;
        } else if (child instanceof AstAnd) {
            treeNode = new BinaryBooleanOperationTreeNode(child, ELNodeConstants.AND_OPERATOR);
        } else if (child instanceof AstEqual) {
            treeNode = new EqualityTestTreeNode(child);
        } else if (child instanceof AstGreaterThan) {
            treeNode = new BinaryBooleanResultOperationTreeNode(child, ELNodeConstants.GREATER_THEN_OPERATOR);
        } else if (child instanceof AstGreaterThanEqual) {
            treeNode = new BinaryBooleanResultOperationTreeNode(child, ELNodeConstants.GREATER_THEN_OR_EQUALITY_OPERATOR);
        } else if (child instanceof AstLessThan) {
            treeNode = new BinaryBooleanResultOperationTreeNode(child, ELNodeConstants.LESS_THEN_OPERATOR);
        } else if (child instanceof AstLessThanEqual) {
            treeNode = new BinaryBooleanResultOperationTreeNode(child, ELNodeConstants.LESS_THEN_OR_EQUALITY_OPERATOR);
        } else if (child instanceof AstNotEqual) {
            treeNode = new EqualityTestTreeNode(child, true);
        } else if (child instanceof AstOr) {
            treeNode = new BinaryBooleanOperationTreeNode(child, ELNodeConstants.OR_OPERATOR);
        } else if (child instanceof AstDiv) {
            treeNode = new BinaryArithmeticOperationTreeNode(child, ELNodeConstants.DIV_OPERATOR);
        } else if (child instanceof AstMult) {
            treeNode = new BinaryArithmeticOperationTreeNode(child, ELNodeConstants.MULT_OPERATOR);
        } else if (child instanceof AstMod) {
            treeNode = new BinaryArithmeticIntegerOperationTreeNode(child, ELNodeConstants.MOD_OPERATOR);
        } else if (child instanceof AstPlus) {
            treeNode = new BinaryArithmeticOperationTreeNode(child, ELNodeConstants.PLUS_OPERATOR);
        } else if (child instanceof AstMinus) {
            treeNode = new BinaryArithmeticOperationTreeNode(child, ELNodeConstants.MINUS_OPERATOR);
        } else if (child instanceof AstBracketSuffix) {
            treeNode = new AstBracketSuffixTreeNode(child);
        } else if (child instanceof AstNegative) {
            treeNode = new AstNegativeTreeNode(child);
        } else if (child instanceof AstFloatingPoint) {
            treeNode = new AstFloatingPointTreeNode(child);
        } else if (child instanceof AstMethodSuffix) {
            treeNode = new AstMethodSuffixTreeNode(child);
        } else if (child instanceof AstPropertySuffix) {
            treeNode = new AstPropertySuffixTreeNode(child);
        } else {
            throw new ParsingException("Node " + child.getClass().getSimpleName() + "[" + child.getImage()
                + "] is not recognized;");
        }

        return treeNode;
    }

    /**
     * This method return PropertyDescriptor by specified propertyName and clazz.
     * 
     * @param clazz
     *            - class to search
     * @param propertyName
     *            - propertyName to search
     * @return property descriptor if found.
     * @throws ParsingException
     *             if error occured.
     */
    public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String propertyName) throws ParsingException {

        if (clazz == null) {
            return null;
        }

        ClassDataHolder classDataHolder = resolveClassPropertiesAndMethods(clazz);
        return classDataHolder.getResolvedProperties().get(propertyName);
    }

    /**
     * <p>
     * Retrieve the property descriptors for the specified class, introspecting and caching them the first time a
     * particular bean class is encountered.
     * </p>
     * 
     * <p>
     * <strong>FIXME</strong> - Does not work with DynaBeans.
     * </p>
     * 
     * @param beanClass
     *            Bean class for which property descriptors are requested
     * @return the property descriptors
     * @throws ParsingException
     *             if error occured.
     * 
     * @exception IllegalArgumentException
     *                if <code>beanClass</code> is null
     */
    private static PropertyDescriptor[] getPropertyDescriptors(Class<?> beanClass) throws ParsingException {
        if (beanClass == null) {
            throw new IllegalArgumentException("No bean class specified");
        }

        // Look up any cached descriptors for this bean class
        PropertyDescriptor[] descriptors = null;

        // Introspect the bean and cache the generated descriptors
        BeanInfo beanInfo = null;

        try {
            beanInfo = Introspector.getBeanInfo(beanClass);
            descriptors = beanInfo.getPropertyDescriptors();
        } catch (IntrospectionException e) {
            return new PropertyDescriptor[0];
        }

        if (descriptors == null) {
            descriptors = new PropertyDescriptor[0];
        }

        return descriptors;
    }

    private static boolean isMethodVisible(Method method) {
        return !Modifier.isPrivate(method.getModifiers());
    }

    /**
     * <p>
     * Find an accessible method that matches the given name and has compatible parameters. Compatible parameters mean
     * that every method parameter is assignable from the given parameters. In other words, it finds a method with the
     * given name that will take the parameters given.
     * <p>
     * 
     * <p>
     * This method is slightly undeterminstic since it loops through methods names and return the first matching method.
     * </p>
     * 
     * <p>
     * This method is used by {@link #invokeMethod(Object object,String methodName,Object [] args,Class[] parameterTypes)}.
     * 
     * <p>
     * This method can match primitive parameter by passing in wrapper classes. For example, a <code>Boolean</code> will
     * match a primitive <code>boolean</code> parameter.
     * 
     * @param clazz
     *            find method in this class
     * @param methodName
     *            find method with this name
     * @param parameterTypes
     *            find method with compatible parameters
     * @return The accessible method
     * @throws ParsingException
     *             if error occured.
     */
    public static Type getMatchingVisibleMethodReturnType(Class<?> clazz, final String methodName, Type[] parameterTypes)
        throws ParsingException {

        if (clazz == null) {
            return TypesFactory.getType(Object.class);
        }

        ClassDataHolder classDataHolder = resolveClassPropertiesAndMethods(clazz);
        List<Method> resolvedMethods = classDataHolder.getResolvedMethods();

        // search through all methods
        int paramSize = parameterTypes.length;
        Method bestMatch = null;

        for (Method resolvedMethod : resolvedMethods) {
            if (!isMethodVisible(resolvedMethod)) {
                continue;
            }

            if (!resolvedMethod.getName().equals(methodName)) {
                continue;
            }

            // compare parameters
            Type[] methodsParams = TypesFactory.getTypesArray(resolvedMethod.getParameterTypes());
            int methodParamSize = methodsParams.length;

            if (methodParamSize == paramSize) {
                boolean match = true;

                for (int n = 0; n < methodParamSize; n++) {
                    if (!methodsParams[n].isAssignableFrom(parameterTypes[n])) {
                        match = false;

                        break;
                    }
                }

                if (match) {
                    if (bestMatch == null) {
                        bestMatch = resolvedMethod;
                    } else {
                        throw new ParsingException(
                            "Detected two methods with the alike signature, not able to select the appropriate one: "
                                + resolvedMethod.toString() + " " + bestMatch.toString());
                    }
                }
            }
        }

        if (bestMatch != null) {
            return TypesFactory.getType(bestMatch.getGenericReturnType());
        } else {
            return TypesFactory.getType(Object.class);
        }
    }

    interface ClassVisitor {
        public void visit(Class<?> clazz) throws ParsingException;
    }

    static class ClassWalkingLogic {

        private Queue<Class<?>> classesList = new LinkedList<Class<?>>();

        private Set<Class<?>> visitedClasses = new HashSet<Class<?>>();

        public ClassWalkingLogic(Class<?> clazz) {
            super();

            this.classesList.add(clazz);
        }

        public void walk(ClassVisitor visitor) throws ParsingException {
            // BFS algorithm
            while (!classesList.isEmpty()) {
                Class<?> clazz = classesList.remove();

                if (visitedClasses.add(clazz)) {
                    visitor.visit(clazz);

                    Class<?> superclass = clazz.getSuperclass();
                    if (superclass != null) {
                        if (!visitedClasses.contains(superclass)) {
                            classesList.add(superclass);
                        }
                    }

                    Class<?>[] interfaces = clazz.getInterfaces();
                    if (interfaces != null) {
                        for (Class<?> iface : interfaces) {
                            if (!visitedClasses.contains(iface)) {
                                classesList.add(iface);
                            }
                        }
                    }
                }
            }

            // While interfaces do not have Object.class in their hierarchy directly,
            // implementations of interface are always inherited from Object.
            // As methods in this class are primarily designed to work with implementations (beans),
            // we are adding Object.class explicitly if it hasn't been visited yet.
            if (visitedClasses.add(Object.class)) {
                visitor.visit(Object.class);
            }

            visitedClasses.clear();
        }
    }

    public static void clearCaches() {
        classDataCache.clear();
    }
    
    public static String coerceToType(String valueString, ELVisitor visitor, Type expectedType) {
        if (!expectedType.isAssignableFrom(visitor.getExpressionType())) {
            for (HelperMethod conversionMethod : HelperMethod.getConversionMethods()) {
                Type returnType = TypesFactory.getType(conversionMethod.getReturnType());
                if (expectedType.isAssignableFrom(returnType)) {
                    visitor.getUsedHelperMethods().add(conversionMethod);
                    visitor.setExpressionType(returnType);
                    return conversionMethod.getName() + "(" + valueString + ")";
                }
            }
        }
        
        return valueString;
    }
}
