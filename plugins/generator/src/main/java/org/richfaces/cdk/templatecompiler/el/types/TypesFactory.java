/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.richfaces.cdk.templatecompiler.el.types;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.behavior.Behavior;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.event.FacesEvent;
import javax.faces.model.DataModel;
import javax.faces.render.Renderer;
import javax.faces.validator.Validator;

import org.richfaces.cdk.JavaLogger;
import org.richfaces.cdk.Logger;
import org.richfaces.cdk.templatecompiler.el.Type;
import org.richfaces.cdk.util.ArrayUtils;

/**
 * @author Nick Belaevski
 * 
 */
public final class TypesFactory {

    private static Logger log = new JavaLogger();

    private static final Map<java.lang.reflect.Type, Type> REFLECTION_TYPES_CACHE = Collections
        .synchronizedMap(new HashMap<java.lang.reflect.Type, Type>());

    private static final Map<String, Type> REFERENCED_TYPES_CACHE = Collections
        .synchronizedMap(new HashMap<String, Type>());

    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER_CLASSES_MAP;
    private static final Map<String, Class<?>> PRIMITIVE_CLASSES_MAP;

    static {
        Map<Class<?>, Class<?>> primitiveToWrapperClassesMap = new HashMap<Class<?>, Class<?>>();
        primitiveToWrapperClassesMap.put(Boolean.TYPE, Boolean.class);
        primitiveToWrapperClassesMap.put(Float.TYPE, Float.class);
        primitiveToWrapperClassesMap.put(Long.TYPE, Long.class);
        primitiveToWrapperClassesMap.put(Integer.TYPE, Integer.class);
        primitiveToWrapperClassesMap.put(Short.TYPE, Short.class);
        primitiveToWrapperClassesMap.put(Byte.TYPE, Byte.class);
        primitiveToWrapperClassesMap.put(Double.TYPE, Double.class);
        primitiveToWrapperClassesMap.put(Character.TYPE, Character.class);

        PRIMITIVE_TO_WRAPPER_CLASSES_MAP = Collections.unmodifiableMap(primitiveToWrapperClassesMap);

        Map<String, Class<?>> primitiveClassesMap = new HashMap<String, Class<?>>();
        for (Class<?> primitiveClass : PRIMITIVE_TO_WRAPPER_CLASSES_MAP.keySet()) {
            primitiveClassesMap.put(primitiveClass.getName(), primitiveClass);
        }

        PRIMITIVE_CLASSES_MAP = Collections.unmodifiableMap(primitiveClassesMap);
    }

    private static final Pattern CLASS_SIGNATURE_PATTERN = Pattern.compile("^" + "\\s*([^\\[<]+)\\s*" + // class name
        "(?:<\\s*(.*)\\s*>)?\\s*" + // generic signature
        "((?:\\[\\s*\\]\\s*)+)?\\s*" + // array signature
        "$");

    private static final int CLASS_NAME_GROUP_IDX = 1;

    private static final int TYPE_ARGUMENTS_GROUP_IDX = 2;

    private static final int ARRAY_SIGNATURE_GROUP_IDX = 3;

    private static final int ARRAY_SIGNATURE_LENGTH = "[]".length();

    private static final String[] GUESS_PACKAGES;

    static {
        Class<?>[] guessPackagesClasses = {
            UIComponent.class, Behavior.class, Converter.class, Validator.class, 
            FacesContext.class, Application.class, FacesEvent.class, DataModel.class, Renderer.class, 
            Collection.class, Object.class };

        GUESS_PACKAGES = new String[guessPackagesClasses.length];
        int i = 0;
        for (Class<?> guessPackageClass : guessPackagesClasses) {
            GUESS_PACKAGES[i++] = guessPackageClass.getPackage().getName();
        }
    }

    private TypesFactory() {
    }

    private static Type getPlainClassType(Class<?> plainClass) {
        Type plainClassType = REFLECTION_TYPES_CACHE.get(plainClass);
        if (plainClassType == null) {
            plainClassType = new PlainClassType(plainClass);
            REFLECTION_TYPES_CACHE.put(plainClass, plainClassType);
        }

        return plainClassType;
    }

    private static Type getReferencedType(String classCodeString) {
        Type type = REFERENCED_TYPES_CACHE.get(classCodeString);
        if (type == null) {
            type = new ReferencedType(classCodeString);
            REFERENCED_TYPES_CACHE.put(classCodeString, type);
        }

        return type;
    }

    private static Class<?> tryLoadClas(String className, ClassLoader classLoader) throws ClassNotFoundException {
        int dotIndex = className.indexOf('.');
        if (dotIndex < 0) {
            // guess type
            for (String guessPackage : GUESS_PACKAGES) {
                String guessTypeName = guessPackage + "." + className;
                try {
                    //while by default initialize = true for Class.forName(String) method
                    //initialize = false used here prevents loading of dependencies that
                    //are accessible only in runtime, e.g. static log initializer from API 
                    //depends on the concrete logger implementation provided in runtime only
                    return Class.forName(guessTypeName, false, classLoader);
                } catch (ClassNotFoundException e) {
                    // ignore
                } catch (LinkageError e) {
                    if (log.isInfoEnabled()) {
                        log.info(MessageFormat.format("Class {0} couldn''t be loaded because of: {1}", guessTypeName, 
                            e.getMessage()));
                    }
                }
            }
        }

        Class<?> result = PRIMITIVE_CLASSES_MAP.get(className);
        if (result == null) {
            try {
                //initialize = false here for the same reason as already mentioned for the previous load block
                result = Class.forName(className, false, classLoader);
            } catch (LinkageError e) {
                String errorMessage = MessageFormat.format("Class {0} couldn''t be loaded because of: {1}", className, 
                    e.getMessage());
                if (log.isInfoEnabled()) {
                    log.info(errorMessage);
                }
                throw new ClassNotFoundException(errorMessage, e);
            }
        }

        return result;
    }
    
    static Type[] parseTypeArgumentsString(String typeArguments, ClassLoader classLoader) {
        if (typeArguments == null) {
            return null;
        }

        String[] typeArgumentsSplit = typeArguments.trim().split(",");

        Type[] types = new Type[typeArgumentsSplit.length];
        for (int i = 0; i < typeArgumentsSplit.length; i++) {
            types[i] = getType(typeArgumentsSplit[i], classLoader);
        }

        return types;
    }

    public static Type getType(String typeString, ClassLoader classLoader) {
        Matcher matcher = CLASS_SIGNATURE_PATTERN.matcher(typeString);
        boolean matchResult = matcher.matches();
        if (matchResult) {
            String className = matcher.group(CLASS_NAME_GROUP_IDX).trim();

            String typeArgumentsString = matcher.group(TYPE_ARGUMENTS_GROUP_IDX);
            Type[] typeArguments = parseTypeArgumentsString(typeArgumentsString, classLoader);

            String arraySignature = matcher.group(ARRAY_SIGNATURE_GROUP_IDX);
            int arrayDepth = 0;
            if (arraySignature != null) {
                arrayDepth = arraySignature.replaceAll("\\s+", "").length() / ARRAY_SIGNATURE_LENGTH;
            }

            Type baseType;
            try {
                // NB: loadedClass can have name that differs from className!
                Class<?> loadedClas = tryLoadClas(className, classLoader);

                baseType = getType(loadedClas);
            } catch (ClassNotFoundException e) {
                baseType = getReferencedType(className);
            }

            if (arrayDepth != 0 || !ArrayUtils.isEmpty(typeArguments)) {
                return new ComplexType(baseType, typeArguments, arrayDepth);
            } else {
                return baseType;
            }
        } else {
            if (log.isWarnEnabled()) {
                log.warn(MessageFormat.format("Cannot parse type signature: ''{0}''", typeString));
            }
            return getReferencedType(typeString);
        }
    }

    static Type createType(java.lang.reflect.Type reflectionType) {
        java.lang.reflect.Type[] actualTypeArguments = null;
        Class<?> rawType = null;
        int arrayDepth = 0;

        java.lang.reflect.Type localReflectionType = reflectionType;

        while (localReflectionType instanceof GenericArrayType) {
            localReflectionType = ((GenericArrayType) localReflectionType).getGenericComponentType();
            arrayDepth++;
        }

        if (localReflectionType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) localReflectionType;

            actualTypeArguments = parameterizedType.getActualTypeArguments();
            rawType = (Class<?>) parameterizedType.getRawType();
        } else if (localReflectionType instanceof Class<?>) {
            rawType = (Class<?>) localReflectionType;
        }

        if (rawType != null) {
            while (rawType.isArray()) {
                arrayDepth++;
                rawType = rawType.getComponentType();
            }

            Type[] typeArguments = null;
            if (!ArrayUtils.isEmpty(actualTypeArguments)) {
                typeArguments = getTypesArray(actualTypeArguments);
            }

            Type clearComponentType = getPlainClassType(rawType);
            if (!ArrayUtils.isEmpty(typeArguments) || arrayDepth != 0) {
                return new ComplexType(clearComponentType, typeArguments, arrayDepth);
            } else {
                return clearComponentType;
            }
        } else {
            //TODO better way of handling unknown types
            return getReferencedType(reflectionType.toString());
        }
    }

    public static Type getType(java.lang.reflect.Type reflectionType) {
        Type result = REFLECTION_TYPES_CACHE.get(reflectionType);
        if (result == null) {
            result = createType(reflectionType);
            REFLECTION_TYPES_CACHE.put(reflectionType, result);
        }

        return result;
    }

    public static Type[] getTypesArray(java.lang.reflect.Type[] reflectionTypes) {
        Type[] types = new Type[reflectionTypes.length];
        for (int i = 0; i < reflectionTypes.length; i++) {
            types[i] = getType(reflectionTypes[i]);
        }

        return types;
    }

    public static Type getNullType() {
        return NullType.INSTANCE;
    }

    public static void clearCaches() {
        REFLECTION_TYPES_CACHE.clear();
        REFERENCED_TYPES_CACHE.clear();
    }

    /**
     * Returns wrapper classes for passed-in class. If type is primitive, then corresponding wrapper class is returned
     * (e.g. boolean -> Boolean), otherwise does nothing and returns passed-in class.
     * 
     * @return wrapper for primitive types, or passed-in class
     */
    static Class<?> getWrapperClass(Class<?> inClazz) {
        if (inClazz.isPrimitive()) {
            return PRIMITIVE_TO_WRAPPER_CLASSES_MAP.get(inClazz);
        } else {
            return inClazz;
        }
    }

}
