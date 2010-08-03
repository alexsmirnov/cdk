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

package org.richfaces.cdk.templatecompiler.builder.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Maksim Kaszynski
 */
public final class JavaPrimitive {
    private static final Map<String, Class<?>> TYPES = new HashMap<String, Class<?>>() {
        {
            put("boolean", boolean.class);
            put("byte", byte.class);
            put("short", short.class);
            put("char", char.class);
            put("int", int.class);
            put("long", long.class);
            put("float", float.class);
            put("double", double.class);
        }
    };
    private static final Map<String, Class<?>> WRAPPERS = new HashMap<String, Class<?>>() {
        {
            put(boolean.class.getName(), Boolean.class);
            put(byte.class.getName(), Byte.class);
            put(short.class.getName(), Short.class);
            put(char.class.getName(), Character.class);
            put(int.class.getName(), Integer.class);
            put(float.class.getName(), Float.class);
            put(long.class.getName(), Long.class);
            put(double.class.getName(), Double.class);
        }
    };

    private JavaPrimitive() {
    }

    public static Class<?> forName(String name) throws ClassNotFoundException {
        Class<?> class1 = TYPES.get(name);

        if (class1 == null) {
            throw new ClassNotFoundException(name);
        }

        return class1;
    }

    public static Class<?> wrapperType(Class<?> primitive) {
        if (!primitive.isPrimitive()) {
            throw new IllegalArgumentException("Class " + primitive + " is not primitive.");
        }

        return WRAPPERS.get(primitive.getName());
    }

    public static boolean isPrimitive(String typeName) {
        return TYPES.containsKey(typeName);
    }
}
