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
package org.richfaces.cdk.templatecompiler.parser.el.test;

import static org.junit.Assert.*;

import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.richfaces.cdk.templatecompiler.el.Type;
import org.richfaces.cdk.templatecompiler.el.types.TypesFactory;

/**
 * @author Nick Belaevski
 * 
 */
public class TypesFactoryTest {

    private static final class ParameterizedTypesHolder {

        @SuppressWarnings("unused")
        public List<String>[] getArray() {
            return null;
        }

        @SuppressWarnings("unused")
        public Map<String, Object> getMap() {
            return null;
        }

        @SuppressWarnings("unused")
        public <Abc> Abc getTypeVariableList() {
            return null;
        }

        @SuppressWarnings("unused")
        public List<? extends String> getWildcardList() {
            return null;
        }
    }

    private java.lang.reflect.Type getParameterizedArrayType() throws Exception {
        return ParameterizedTypesHolder.class.getMethod("getArray").getGenericReturnType();
    }

    private java.lang.reflect.Type getParameterizedMapType() throws Exception {
        return ParameterizedTypesHolder.class.getMethod("getMap").getGenericReturnType();
    }

    private java.lang.reflect.Type getTypeVariableType() throws Exception {
        return ParameterizedTypesHolder.class.getMethod("getTypeVariableList").getGenericReturnType();
    }

    private java.lang.reflect.Type getWildcardType() throws Exception {
        return ParameterizedTypesHolder.class.getMethod("getWildcardList").getGenericReturnType();
    }

    @Test
    public void testCaching() throws Exception {
        Type objectType = TypesFactory.getType(Object.class);
        Type objectType2 = TypesFactory.getType(Object.class);

        assertNotNull(objectType);
        assertNotNull(objectType2);

        assertSame(objectType, objectType2);

        TypesFactory.clearCaches();

        Type objectType3 = TypesFactory.getType(Object.class);
        assertNotNull(objectType3);

        assertNotSame(objectType, objectType3);
    }

    @Test
    public void testGetNullType() throws Exception {
        Type nullType = TypesFactory.getNullType();
        assertNotNull(nullType);
        assertTrue(nullType.isNullType());
    }

    @Test
    public void testGetTypeFromReflectionType() throws Exception {
        Type integerType = TypesFactory.getType(Integer.TYPE);
        assertNotNull(integerType);
        assertEquals(Integer.TYPE, integerType.getRawType());
        assertNull(integerType.getTypeArguments());
        assertFalse(integerType.isArray());
        assertFalse(integerType.isNullType());

        Type stringType = TypesFactory.getType(String.class);
        assertNotNull(stringType);
        assertEquals(String.class, stringType.getRawType());
        assertNull(stringType.getTypeArguments());
        assertFalse(stringType.isArray());
        assertFalse(stringType.isNullType());

        Type arrayType = TypesFactory.getType(String[].class);
        assertNotNull(arrayType);
        assertNull(arrayType.getTypeArguments());
        assertTrue(arrayType.isArray());
        assertEquals(String.class, arrayType.getContainerType().getRawType());
        assertFalse(arrayType.isNullType());

        Type multiDimArrayType = TypesFactory.getType(String[][][].class);
        assertNotNull(multiDimArrayType);
        assertNull(multiDimArrayType.getTypeArguments());
        assertTrue(multiDimArrayType.isArray());
        assertEquals(String[][].class, multiDimArrayType.getContainerType().getRawType());
        assertFalse(multiDimArrayType.isNullType());

        Type parameterizedMapType = TypesFactory.getType(getParameterizedMapType());
        assertNotNull(parameterizedMapType);
        assertFalse(parameterizedMapType.isArray());
        assertEquals(Map.class, parameterizedMapType.getRawType());

        Type[] parameterizedMapTypeArguments = parameterizedMapType.getTypeArguments();
        assertNotNull(parameterizedMapTypeArguments);
        assertEquals(2, parameterizedMapTypeArguments.length);
        assertEquals(String.class, parameterizedMapTypeArguments[0].getRawType());
        assertEquals(Object.class, parameterizedMapTypeArguments[1].getRawType());

        Type parameterizedArrayType = TypesFactory.getType(getParameterizedArrayType());
        assertNotNull(parameterizedArrayType);
        assertTrue(parameterizedArrayType.isArray());
        assertEquals(List[].class, parameterizedArrayType.getRawType());

        Type[] parameterizedArrayTypeArguments = parameterizedArrayType.getTypeArguments();
        assertNotNull(parameterizedArrayTypeArguments);
        assertEquals(1, parameterizedArrayTypeArguments.length);
        Type parameterizedArrayTypeArgument = parameterizedArrayTypeArguments[0];
        assertEquals(String.class, parameterizedArrayTypeArgument.getRawType());
        assertFalse(parameterizedArrayTypeArgument.isArray());

        Type typeVariableType = TypesFactory.getType(getTypeVariableType());

        assertNotNull(typeVariableType);
        assertEquals("Abc", typeVariableType.getCode());

        Type wildcardTypeHolder = TypesFactory.getType(getWildcardType());
        assertNotNull(wildcardTypeHolder);
        assertEquals(List.class, wildcardTypeHolder.getRawType());

        Type[] wildcardTypeHolderArguments = wildcardTypeHolder.getTypeArguments();
        assertNotNull(wildcardTypeHolderArguments);
        assertEquals(1, wildcardTypeHolderArguments.length);
        Type wildcardType = wildcardTypeHolderArguments[0];
        assertEquals("? extends java.lang.String", wildcardType.getCode());
    }

    @Test
    public void testGetTypeFromString() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();

        Type primitiveIntType = TypesFactory.getType("int", classLoader);
        assertNotNull(primitiveIntType);
        assertEquals(Integer.TYPE, primitiveIntType.getRawType());

        Type guessedMapType = TypesFactory.getType("Map", classLoader);
        assertNotNull(guessedMapType);
        assertEquals(Map.class, guessedMapType.getRawType());
        assertNull(guessedMapType.getTypeArguments());

        Type writerType = TypesFactory.getType(java.io.Writer.class.getName(), classLoader);
        assertNotNull(writerType);
        assertEquals(Writer.class, writerType.getRawType());
        assertNull(writerType.getTypeArguments());

        Type genericMapType = TypesFactory.getType("Map<String, Object>", classLoader);
        assertNotNull(genericMapType);

        assertEquals(Map.class, genericMapType.getRawType());
        Type[] genericMapTypeArguments = genericMapType.getTypeArguments();
        assertNotNull(genericMapTypeArguments);
        assertFalse(genericMapType.isArray());
        assertEquals(2, genericMapTypeArguments.length);

        Type genericMapTypeKeyArgument = genericMapTypeArguments[0];
        assertEquals(String.class, genericMapTypeKeyArgument.getRawType());

        Type genericMapTypeValueArgument = genericMapTypeArguments[1];
        assertEquals(Object.class, genericMapTypeValueArgument.getRawType());

        Type arrayType = TypesFactory.getType("String[]", classLoader);
        assertNotNull(arrayType);
        assertTrue(arrayType.isArray());
        assertEquals(String[].class, arrayType.getRawType());

        Type genericArrayType = TypesFactory.getType("List<String>[]", classLoader);
        assertNotNull(genericArrayType);
        assertTrue(genericArrayType.isArray());
        assertEquals(List[].class, genericArrayType.getRawType());

        Type[] genericArrayTypeArguments = genericArrayType.getTypeArguments();
        assertNotNull(genericArrayTypeArguments);
        assertEquals(1, genericArrayTypeArguments.length);

        Type genericArrayTypeArgument = genericArrayTypeArguments[0];
        assertEquals(String.class, genericArrayTypeArgument.getRawType());
    }

    @Test
    public void testReferencedType() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        String className = "some.not.available.Class";
        Type plainReferencedType = TypesFactory.getType(className, classLoader);

        assertNotNull(plainReferencedType);
        assertEquals(className, plainReferencedType.getCode());

        String arraySignature = className + "[]";
        Type arrayReferencedType = TypesFactory.getType(arraySignature, classLoader);
        assertNotNull(arrayReferencedType);
        assertTrue(arrayReferencedType.isArray());
        assertEquals(arraySignature, arrayReferencedType.getCode());

        String genericSignature = className + "<String>";
        Type genericReferenceType = TypesFactory.getType(genericSignature, classLoader);
        assertNotNull(genericReferenceType);
        assertEquals(genericSignature, genericReferenceType.getCode());

        Type[] genericTypeArguments = genericReferenceType.getTypeArguments();
        assertNotNull(genericTypeArguments);
        assertEquals(1, genericTypeArguments.length);
        Type genericTypeArgument = genericTypeArguments[0];
        assertEquals(String.class, genericTypeArgument.getRawType());
    }
}
