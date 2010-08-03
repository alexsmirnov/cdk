/*
 * $Id$
 *
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

package org.richfaces.cdk.templatecompiler;

import static org.richfaces.cdk.templatecompiler.QNameComparator.*;
import static org.richfaces.cdk.util.JavaUtils.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;
import javax.xml.namespace.QName;

import org.richfaces.cdk.CdkException;
import org.richfaces.cdk.Logger;
import org.richfaces.cdk.attributes.Attribute;
import org.richfaces.cdk.attributes.Element;
import org.richfaces.cdk.attributes.Schema;
import org.richfaces.cdk.attributes.SchemaSet;
import org.richfaces.cdk.model.EventName;
import org.richfaces.cdk.model.PropertyBase;
import org.richfaces.cdk.templatecompiler.builder.model.Argument;
import org.richfaces.cdk.templatecompiler.builder.model.JavaClass;
import org.richfaces.cdk.templatecompiler.builder.model.JavaField;
import org.richfaces.cdk.templatecompiler.builder.model.JavaMethod;
import org.richfaces.cdk.templatecompiler.builder.model.JavaModifier;
import org.richfaces.cdk.templatecompiler.builder.model.MethodBody;
import org.richfaces.cdk.templatecompiler.builder.model.MethodBodyStatementsContainer;
import org.richfaces.cdk.templatecompiler.el.ELParserUtils;
import org.richfaces.cdk.templatecompiler.el.ELVisitor;
import org.richfaces.cdk.templatecompiler.el.HelperMethod;
import org.richfaces.cdk.templatecompiler.el.ParsingException;
import org.richfaces.cdk.templatecompiler.el.Type;
import org.richfaces.cdk.templatecompiler.el.types.TypesFactory;
import org.richfaces.cdk.templatecompiler.model.AnyElement;
import org.richfaces.cdk.templatecompiler.model.CdkBodyElement;
import org.richfaces.cdk.templatecompiler.model.CdkCallElement;
import org.richfaces.cdk.templatecompiler.model.CdkChooseElement;
import org.richfaces.cdk.templatecompiler.model.CdkForEachElement;
import org.richfaces.cdk.templatecompiler.model.CdkIfElement;
import org.richfaces.cdk.templatecompiler.model.CdkObjectElement;
import org.richfaces.cdk.templatecompiler.model.CdkOtherwiseElement;
import org.richfaces.cdk.templatecompiler.model.CdkWhenElement;
import org.richfaces.cdk.templatecompiler.model.CompositeImplementation;
import org.richfaces.cdk.templatecompiler.model.CompositeInterface;
import org.richfaces.cdk.templatecompiler.model.Template;
import org.richfaces.cdk.templatecompiler.model.TemplateVisitor;
import org.richfaces.cdk.util.Strings;
import org.richfaces.cdk.xmlconfig.JAXB;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * <p class="changed_added_4_0">
 * </p>
 *
 * @author asmirnov@exadel.com
 */
public class RendererClassVisitor implements TemplateVisitor {

    /**
     *
     */
    // TODO externalize
    static final String RENDER_KIT_UTILS_CLASS_NAME = "org.richfaces.renderkit.RenderKitUtils";
    /**
     *
     */
    static final String RESPONSE_WRITER_VARIABLE = "responseWriter";
    /**
     *
     */
    static final String COMPONENT_VARIABLE = "component";
    /**
     *
     */
    static final String THIS_VARIABLE = "this";
    /**
     *
     */
    static final String SUPER_VARIABLE = "super";
    /**
     *
     */
    static final String FACES_CONTEXT_VARIABLE = "facesContext";
    /**
     *
     */
    static final String CLIENT_ID_VARIABLE = "clientId";

    /**
     *
     */
    private static final String PASS_THROUGH_ATTRIBUTES_FIELD_NAME = "PASS_THROUGH_ATTRIBUTES";

    private static final Set<String> DEFAULT_NAMESPACES = ImmutableSet.of(Template.XHTML_EL_NAMESPACE,
        Template.XHTML_NAMESPACE);

    private static final EnumMap<HelperMethod, HelperMethodBodyStatement> HELPER_METHOD_BODIES =
        new EnumMap<HelperMethod, HelperMethodBodyStatement>(HelperMethod.class);

    static {
        HELPER_METHOD_BODIES.put(HelperMethod.EMPTINESS_CHECK, new EmptinessCheckingMethodBodyStatement());
        HELPER_METHOD_BODIES.put(HelperMethod.EQUALS_CHECK, new EqualsCheckingMethodBodyStatement());
        HELPER_METHOD_BODIES.put(HelperMethod.TO_BOOLEAN_CONVERSION, new ConversionToBooleanMethodBodyStatement());
        HELPER_METHOD_BODIES.put(HelperMethod.TO_STRING_CONVERSION, new ConversionToStringMethodBodyStatement());
    }

    private final Logger log;

    private MethodBodyStatementsContainer currentStatement;

    private Schema attributesSchema = null;
    private JavaClass generatedClass;
    private CompositeInterface compositeInterface;

    private final LinkedList<MethodBodyStatementsContainer> statements = Lists.newLinkedList();

    private Map<String, Type> localsTypesMap;
    private ClassLoader classLoader;

    private Set<HelperMethod> addedHelperMethods = EnumSet.noneOf(HelperMethod.class);

    private Type lastCompiledExpressionType;
    private int passThroughCounter;
    private Collection<PropertyBase> attributes;

    public RendererClassVisitor(CompositeInterface compositeInterface, Collection<PropertyBase> attributes,
                                ClassLoader classLoader, JAXB jaxbBinding, Logger log) {
        this.compositeInterface = compositeInterface;
        this.attributes = attributes;
        this.classLoader = classLoader;
        this.log = log;
        // TODO - cache unmarshalled data (as CDKWorker?)
        SchemaSet schemaSet = jaxbBinding.unmarshal("urn:attributes:xhtml-el.xml", null, SchemaSet.class);
        this.attributesSchema = schemaSet.getSchemas().get(Template.XHTML_EL_NAMESPACE);
    }

    private void initializeJavaClass() {
        this.generatedClass = new JavaClass(compositeInterface.getJavaClass());
        this.generatedClass.addModifier(JavaModifier.PUBLIC);
        this.generatedClass.setSuperClass(new JavaClass(compositeInterface.getBaseClass()));

        this.generatedClass.addImport(FacesContext.class);
        this.generatedClass.addImport(ResponseWriter.class);
        this.generatedClass.addImport(UIComponent.class);

        // TODO - make this JavaDoc - Generated annotation is present since JDK6
        // this.generatedClass.addAnnotation(Generated.class, "\"RichFaces CDK\"");
        // TODO remove this after improving Java model
        // this.generatedClass.addImport(Generated.class);

        this.createMethodContext();
    }

    private void addHelperMethod(HelperMethod helperMethod) {
        if (addedHelperMethods.add(helperMethod)) {
            HelperMethodBodyStatement methodBodyStatement = HELPER_METHOD_BODIES.get(helperMethod);

            String[] argumentNames = methodBodyStatement.getArgumentNames();
            Class<?>[] argumentTypes = helperMethod.getArgumentTypes();

            assert argumentNames.length == argumentTypes.length;

            Argument[] arguments = new Argument[argumentTypes.length];
            for (int i = 0; i < arguments.length; i++) {
                arguments[i] = new Argument(argumentNames[i], argumentTypes[i]);
            }

            JavaMethod helperJavaMethod = new JavaMethod(helperMethod.getName(),
                helperMethod.getReturnType(), arguments);

            helperJavaMethod.addModifier(JavaModifier.PRIVATE);
            helperJavaMethod.addModifier(JavaModifier.STATIC);
            helperJavaMethod.addModifier(JavaModifier.FINAL);

            MethodBody helperJavaMethodBody = new MethodBody(helperJavaMethod);
            helperJavaMethod.setMethodBody(helperJavaMethodBody);
            helperJavaMethodBody.addStatement(methodBodyStatement);

            generatedClass.addMethod(helperJavaMethod);
        }
    }

    private String compileEl(String expression, Class<?> type) {
        try {
            ELVisitor elVisitor = new ELVisitor();
            elVisitor.parse(expression, localsTypesMap, TypesFactory.getType(type));

            lastCompiledExpressionType = elVisitor.getExpressionType();
            String parsedExpression = elVisitor.getParsedExpression();

            for (HelperMethod helperMethod : elVisitor.getUsedHelperMethods()) {
                addHelperMethod(helperMethod);
            }

            return parsedExpression + "/* " + expression.trim() + " */";
        } catch (ParsingException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private boolean isDefaultNamespace(String namespaceURI) {
        // TODO - another namespaces
        if (Strings.isEmpty(namespaceURI)) {
            return true;
        }

        if (DEFAULT_NAMESPACES.contains(namespaceURI)) {
            return true;
        }

        return false;
    }

    private Type createTypeOfKnownClass(JavaClass initialClass, Class<?> knownSuperClass) {
        assert !knownSuperClass.isInterface();

        Type result = null;

        JavaClass javaClass = initialClass;
        while (javaClass != null) {
            Type type = TypesFactory.getType(javaClass.getName(), classLoader);
            if (knownSuperClass.isAssignableFrom(type.getRawType())) {
                result = type;
                break;
            }

            javaClass = javaClass.getSuperClass();
        }

        if (result == null) {
            result = TypesFactory.getType(knownSuperClass);
        }

        return result;
    }

    private String createPassThroughAttributeCode(String htmlAttributeName, String componentAttributeName) {

        generatedClass.addImport("org.richfaces.renderkit.ComponentAttribute");

        StringBuilder sb = new StringBuilder();
        sb.append("new ComponentAttribute(");

        sb.append(getEscapedString(htmlAttributeName));

        sb.append(")");

        String attributeName = htmlAttributeName;
        if (!attributeName.equals(componentAttributeName)) {
            attributeName = componentAttributeName;

            sb.append(".setComponentAttributeName(");
            sb.append(getEscapedString(componentAttributeName));
            sb.append(")");
        }

        // OPTIMIZATION - use ModelSet with lookup method
        for (PropertyBase property : attributes) {
            if (attributeName.equals(property.getName())) {
                Set<EventName> eventNames = property.getEventNames();
                if (eventNames != null && !eventNames.isEmpty()) {
                    sb.append(".setEventNames(");
                    sb.append("new String[] {");
                    Collection<String> eventNamesStrings = Collections2.transform(eventNames,
                        new Function<EventName, String>() {
                            @Override
                            public String apply(EventName from) {
                                return from.getName();
                            }
                        });
                    sb.append(getEscapedStringsArray(eventNamesStrings));
                    sb.append("})");
                }
                break;
            }
        }

        return sb.toString();
    }

    private String createPassThroughField(Map<String, Attribute> htmlAttributesMap) {
        String fieldName = PASS_THROUGH_ATTRIBUTES_FIELD_NAME;
        if (passThroughCounter >= 0) {
            fieldName += "_" + passThroughCounter;
        }
        passThroughCounter++;

        // TODO generic arguments
        JavaField passThroughField = new JavaField(Map.class, fieldName);
        passThroughField.addModifier(JavaModifier.PRIVATE);
        passThroughField.addModifier(JavaModifier.STATIC);
        passThroughField.addModifier(JavaModifier.FINAL);

        generatedClass.addImport("org.richfaces.renderkit.ComponentAttribute");
        generatedClass.addImport(RENDER_KIT_UTILS_CLASS_NAME);
        generatedClass.addImport(Collections.class);

        // TODO - get rid of FQNs for classes via imports
        passThroughField.setGenericArguments(new JavaClass[]{new JavaClass(String.class),
            new JavaClass("org.richfaces.renderkit.ComponentAttribute")});

        StringBuilder fieldValue = new StringBuilder("Collections.unmodifiableMap(ComponentAttribute.createMap(");
        boolean isFirstArgument = true;
        for (Map.Entry<String, Attribute> entry : htmlAttributesMap.entrySet()) {
            if (isFirstArgument) {
                isFirstArgument = false;
            } else {
                fieldValue.append(", ");
            }

            String htmlAttributeName = entry.getKey();
            String componentAttributeName = entry.getValue().getComponentAttributeName();
            fieldValue.append(createPassThroughAttributeCode(htmlAttributeName, componentAttributeName));
        }

        fieldValue.append("))");

        passThroughField.setValue(fieldValue.toString());
        generatedClass.addField(passThroughField);

        return fieldName;
    }

    private void createMethodContext() {
        this.currentStatement = new MethodBody();
        this.localsTypesMap = new HashMap<String, Type>();
        localsTypesMap.put(FACES_CONTEXT_VARIABLE, TypesFactory.getType(FacesContext.class));
        localsTypesMap.put(RESPONSE_WRITER_VARIABLE, TypesFactory.getType(ResponseWriter.class));
        localsTypesMap.put(CLIENT_ID_VARIABLE, TypesFactory.getType(String.class));

        // TODO: try load component class
        localsTypesMap.put(COMPONENT_VARIABLE, TypesFactory.getType(UIComponent.class));

        Type generatedClassType = createTypeOfKnownClass(generatedClass, Renderer.class);
        localsTypesMap.put(THIS_VARIABLE, generatedClassType);

        Type generatedClassSuperType = createTypeOfKnownClass(generatedClass.getSuperClass(), Renderer.class);
        localsTypesMap.put(SUPER_VARIABLE, generatedClassSuperType);
    }

    private void flushToEncodeMethod(String encodeMethodName) {
        if (!this.currentStatement.isEmpty()) {
            Argument facesContextArgument = new Argument(FACES_CONTEXT_VARIABLE, FacesContext.class);
            Argument componentArgument = new Argument(COMPONENT_VARIABLE, UIComponent.class);

            JavaMethod javaMethod = new JavaMethod(encodeMethodName, facesContextArgument, componentArgument);
            javaMethod.addModifier(JavaModifier.PUBLIC);
            javaMethod.addAnnotation(Override.class);
            javaMethod.getExceptions().add(IOException.class);

            MethodBody methodBody = (MethodBody) currentStatement;
            javaMethod.setMethodBody(methodBody);

            methodBody.addStatement(0, new EncodeMethodPrefaceStatement());
            generatedClass.addMethod(javaMethod);

            Collection<Type> variableTypes = localsTypesMap.values();
            for (Type variableType : variableTypes) {

                Collection<Class<?>> importsList = variableType.getImportsList();
                if (importsList != null) {
                    for (Class<?> importedClass : importsList) {
                        generatedClass.addImport(importedClass);
                    }
                }
            }
        }

        createMethodContext();
    }

    private void defineObject(Type type, String name, String initializationExpression) {
        currentStatement.addStatement(new DefineObjectStatement(type, name, initializationExpression));
        localsTypesMap.put(name, type);
    }

    private void createRendersChildrenMethod() {
        Boolean rendersChildren = compositeInterface.getRendersChildren();
        if (rendersChildren != null) {
            JavaMethod rendersChildrenMethod = new JavaMethod("getRendersChildren", Boolean.TYPE);
            rendersChildrenMethod.addModifier(JavaModifier.PUBLIC);
            rendersChildrenMethod.addAnnotation(Override.class);
            
            MethodBody methodBody = new MethodBody();
            rendersChildrenMethod.setMethodBody(methodBody);

            generatedClass.addMethod(rendersChildrenMethod);
            
            methodBody.addStatement(
                new ConstantReturnMethodBodyStatement(Boolean.toString(compositeInterface.getRendersChildren())));
        }
    }
    
    protected void pushStatement(MethodBodyStatementsContainer container) {
        currentStatement.addStatement(container);
        statements.push(currentStatement);
        currentStatement = container;
    }

    protected void popStatement() {
        currentStatement = statements.pop();
    }

    /**
     * <p class="changed_added_4_0">
     * </p>
     *
     * @return the rendererClass
     */
    public JavaClass getGeneratedClass() {
        return this.generatedClass;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.richfaces.cdk.templatecompiler.model.TemplateVisitor
     * #startElement(org.richfaces.cdk.templatecompiler.model.CdkBodyElement)
     */

    @Override
    public void startElement(CdkBodyElement cdkBodyElement) throws CdkException {
        flushToEncodeMethod("encodeBegin");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.richfaces.cdk.templatecompiler.model.TemplateVisitor
     * #endElement(org.richfaces.cdk.templatecompiler.model.CdkBodyElement)
     */

    @Override
    public void endElement(CdkBodyElement cdkBodyElement) throws CdkException {
        flushToEncodeMethod("encodeChildren");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.richfaces.cdk.templatecompiler.model.TemplateVisitor
     * #startElement(org.richfaces.cdk.templatecompiler.model.AnyElement)
     */

    @Override
    public void startElement(AnyElement anyElement) throws CdkException {
        QName elementName = anyElement.getName();
        Map<QName, Object> elementAttributes = anyElement.getAttributes();

        if (!isDefaultNamespace(elementName.getNamespaceURI())) {
            // TODO: add support
            return;
        }

        currentStatement.addStatement(new StartElementStatement(elementName.getLocalPart()));

        if (elementAttributes != null) {
            Set<String> writtenAttributes = new HashSet<String>();
            boolean shouldEncodePassThrough = false;
            String[] passThroughExclusions = null;

            Map<QName, Object> sortedElementAttributes = new TreeMap<QName, Object>(QNAME_COMPARATOR);
            sortedElementAttributes.putAll(elementAttributes);

            for (Map.Entry<QName, Object> attribute : sortedElementAttributes.entrySet()) {
                QName attributeName = attribute.getKey();
                Object attributeValue = attribute.getValue();

                if (!isDefaultNamespace(attributeName.getNamespaceURI())) {
                    // TODO: add support
                    if (Template.CDK_NAMESPACE.equals(attributeName.getNamespaceURI())
                        && "passThroughWithExclusions".equals(attributeName.getLocalPart())) {

                        shouldEncodePassThrough = true;
                        if (attributeValue != null) {
                            passThroughExclusions = attributeValue.toString().split("\\s+");
                        }
                    }
                } else {
                    String attributeLocalName = attributeName.getLocalPart();
                    if (writtenAttributes.add(attributeLocalName)) {
                        generatedClass.addImport(RENDER_KIT_UTILS_CLASS_NAME);
                        currentStatement.addStatement(new WriteAttributeStatement(attributeLocalName, compileEl(
                            attributeValue.toString(), Object.class)));
                    }
                }
            }

            if (shouldEncodePassThrough) {
                Element attributesElement = attributesSchema.getElements().get(elementName.getLocalPart());
                if (attributesElement != null) {
                    // make a copy of original set
                    TreeMap<String, Attribute> actualAttributesMap = new TreeMap<String, Attribute>(attributesElement
                        .getAttributes());

                    if (passThroughExclusions != null) {
                        for (String passThroughExclusion : passThroughExclusions) {
                            actualAttributesMap.remove(passThroughExclusion);
                        }
                    }

                    for (String writtenAttribute : writtenAttributes) {
                        actualAttributesMap.remove(writtenAttribute);
                    }

                    if (!actualAttributesMap.isEmpty()) {
                        String passThroughFieldName = createPassThroughField(actualAttributesMap);
                        generatedClass.addImport(RENDER_KIT_UTILS_CLASS_NAME);
                        currentStatement.addStatement(new WriteAttributesSetStatement(passThroughFieldName));
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.richfaces.cdk.templatecompiler.model.TemplateVisitor
     * #endElement(org.richfaces.cdk.templatecompiler.model.AnyElement)
     */

    @Override
    public void endElement(AnyElement anyElement) throws CdkException {
        QName elementName = anyElement.getName();
        currentStatement.addStatement(new EndElementStatement(elementName.getLocalPart()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.richfaces.cdk.templatecompiler.model.TemplateVisitor#visitElement(java.lang.String)
     */

    @Override
    public void visitElement(String text) throws CdkException {
        if (text != null) {
            String trimmedText = text.trim();
            if (!Strings.isEmpty(trimmedText)) {
                currentStatement.addStatement(new WriteTextStatement(compileEl(trimmedText, String.class)));
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.richfaces.cdk.templatecompiler.model.TemplateVisitor
     * #visitElement(org.richfaces.cdk.templatecompiler.model.CdkCallElement)
     */

    @Override
    public void visitElement(CdkCallElement cdkCallElement) throws CdkException {
        String expression = cdkCallElement.getExpression();
        if (Strings.isEmpty(expression)) {
            expression = cdkCallElement.getBodyValue();
        }

        currentStatement.addStatement(expression + ";");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.richfaces.cdk.templatecompiler.model.TemplateVisitor
     * #startElement(org.richfaces.cdk.templatecompiler.model.CdkIfElement)
     */

    @Override
    public void startElement(CdkIfElement cdkIfElement) {
        String compiledTestExpression = compileEl(cdkIfElement.getTest(), Boolean.TYPE);

        pushStatement(new IfElseStatement());
        pushStatement(new IfStatement(compiledTestExpression));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.richfaces.cdk.templatecompiler.model.TemplateVisitor
     * #endElement(org.richfaces.cdk.templatecompiler.model.CdkIfElement)
     */

    @Override
    public void endElement(CdkIfElement cdkIfElement) {
        popStatement();
        popStatement();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.richfaces.cdk.templatecompiler.model.TemplateVisitor
     * #startElement(org.richfaces.cdk.templatecompiler.model.CdkChooseElement)
     */

    @Override
    public void startElement(CdkChooseElement cdkChooseElement) {
        pushStatement(new IfElseStatement());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.richfaces.cdk.templatecompiler.model.TemplateVisitor
     * #endElement(org.richfaces.cdk.templatecompiler.model.CdkChooseElement)
     */

    @Override
    public void endElement(CdkChooseElement cdkChooseElement) {
        popStatement();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.richfaces.cdk.templatecompiler.model.TemplateVisitor
     * #startElement(org.richfaces.cdk.templatecompiler.model.CdkWhenElement)
     */

    @Override
    public void startElement(CdkWhenElement cdkWhenElement) {
        String compiledTestExpression = compileEl(cdkWhenElement.getTest(), Boolean.TYPE);

        pushStatement(new IfStatement(compiledTestExpression));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.richfaces.cdk.templatecompiler.model.TemplateVisitor
     * #endElement(org.richfaces.cdk.templatecompiler.model.CdkWhenElement)
     */

    @Override
    public void endElement(CdkWhenElement cdkWhenElement) {
        popStatement();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.richfaces.cdk.templatecompiler.model.TemplateVisitor
     * #startElement(org.richfaces.cdk.templatecompiler.model.CdkOtherwiseElement)
     */

    @Override
    public void startElement(CdkOtherwiseElement cdkOtherwiseElement) {
        pushStatement(new IfStatement(""));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.richfaces.cdk.templatecompiler.model.TemplateVisitor
     * #endElement(org.richfaces.cdk.templatecompiler.model.CdkOtherwiseElement)
     */

    @Override
    public void endElement(CdkOtherwiseElement cdkOtherwiseElement) {
        popStatement();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.richfaces.cdk.templatecompiler.model.TemplateVisitor
     * #visitElement(org.richfaces.cdk.templatecompiler.model.CdkObjectElement)
     */

    @Override
    public void visitElement(CdkObjectElement cdkObjectElement) {
        String name = cdkObjectElement.getName();

        String value = cdkObjectElement.getValue();
        if (Strings.isEmpty(value)) {
            value = cdkObjectElement.getBodyValue();
        }

        String typeString = cdkObjectElement.getType();
        String typeArgumentsString = cdkObjectElement.getTypeArguments();
        if (!Strings.isEmpty(typeArgumentsString)) {
            // TODO: generic arrays
            typeString += "<" + typeArgumentsString + ">";
        }

        Type type = null;
        if (!Strings.isEmpty(typeString)) {
            type = TypesFactory.getType(typeString, classLoader);
        }

        if (!Strings.isEmpty(value)) {
            Class<?> valueType;
            if (type != null) {
                valueType = type.getRawType();
            } else {
                valueType = Object.class;
            }

            value = compileEl(value, valueType);
            if (type == null) {
                type = lastCompiledExpressionType;
            }
        }

        if (type == null) {
            type = TypesFactory.getType(Object.class);
        }

        defineObject(type, name, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.richfaces.cdk.templatecompiler.model.TemplateVisitor
     * #startElement(org.richfaces.cdk.templatecompiler.model.CdkForEachElement)
     */

    @Override
    public void startElement(CdkForEachElement cdkForEachElement) {
        String items = cdkForEachElement.getItems();
        String itemsExpression = compileEl(items, Iterable.class);

        // TODO - review
        Class<?> collectionElementClass = lastCompiledExpressionType.getContainerType().getRawType();
        if (collectionElementClass == null) {
            collectionElementClass = Object.class;
        }

        pushStatement(new ForEachStatement(itemsExpression, cdkForEachElement.getVar(), collectionElementClass
            .getName()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.richfaces.cdk.templatecompiler.model.TemplateVisitor
     * #endElement(org.richfaces.cdk.templatecompiler.model.CdkForEachElement)
     */

    @Override
    public void endElement(CdkForEachElement cdkForEachElement) {
        popStatement();
    }

    /**
     *
     */
    public void preProcess(CompositeImplementation impl) {
        initializeJavaClass();
        passThroughCounter = -1;
    }

    /**
     *
     */
    public void postProcess(CompositeImplementation impl) {
        flushToEncodeMethod("encodeEnd");
        createRendersChildrenMethod();
    }

    public static void clearCaches() {
        TypesFactory.clearCaches();
        ELParserUtils.clearCaches();
    }
}
