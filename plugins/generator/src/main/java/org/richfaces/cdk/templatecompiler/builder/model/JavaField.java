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


/**
 * Class field abstraction
 *
 * @author Maksim Kaszynski
 */
public class JavaField extends JavaLanguageElement {
    private JavaClass type;

    private Object value;

    private JavaClass[] genericArguments;
    
    public JavaField(Class<?> type, String name) {
        this(new JavaClass(type), name, null);
    }

    public JavaField(JavaClass type, String name) {
        this(type, name, null);
    }

    public JavaField(Class<?> type, String name, Object value) {
        this(new JavaClass(type), name, value);
    }
    
    public JavaField(JavaClass type, String name, Object value) {
        super(name);
        this.type = type;
        this.value = value;
    }

    public JavaClass getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
    
    /**
     * @return the genericArguments
     */
    public JavaClass[] getGenericArguments() {
        return genericArguments;
    }
    
    /**
     * @param genericArguments the genericArguments to set
     */
    public void setGenericArguments(JavaClass[] genericArguments) {
        this.genericArguments = genericArguments;
    }
    
    public String getGenericSignature() {
        StringBuilder result = new StringBuilder();
        
        if (genericArguments != null) {
            for (JavaClass genericArgument : genericArguments) {
                if (result.length() == 0) {
                    result.append('<');
                } else {
                    result.append(", ");
                }

                result.append(genericArgument.getName());
            }
        }
        
        if (result.length() != 0) {
            result.append('>');
        }
        
        return result.toString();
    }
}
