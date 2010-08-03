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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.richfaces.cdk.templatecompiler.el.Type;
import org.richfaces.cdk.util.ArrayUtils;

/**
 * @author Nick Belaevski
 * 
 */
public class ComplexType implements Type {

    private Type clearComponentType;
    
    private Type[] typeArguments;
    
    private Class<?> cachedRawType;
    
    private int arrayDepth;
    
    public ComplexType(Type clearComponentType, Type[] typeArguments, int arrayDepth) {
        super();
        this.clearComponentType = clearComponentType;
        this.typeArguments = typeArguments;
        this.arrayDepth = arrayDepth;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.richfaces.cdk.templatecompiler.el.Type#getImportsList()
     */
    @Override
    public Collection<Class<?>> getImportsList() {
        Set<Class<?>> result = new LinkedHashSet<Class<?>>();

        result.addAll(clearComponentType.getImportsList());
        for (Type typeArgument : typeArguments) {
            result.addAll(typeArgument.getImportsList());
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.richfaces.cdk.templatecompiler.el.Type#isNullType()
     */
    @Override
    public boolean isNullType() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.richfaces.cdk.templatecompiler.el.Type#getTypeArguments()
     */
    @Override
    public Type[] getTypeArguments() {
        return typeArguments;
    }

    @Override
    public Type getContainerType() {
        if (arrayDepth != 0) {
            return new ComplexType(clearComponentType, typeArguments, arrayDepth - 1);
        } else {
            if (!ArrayUtils.isEmpty(typeArguments)) {
                return typeArguments[typeArguments.length - 1];
            } else {
                return TypesFactory.getType(Object.class);
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.richfaces.cdk.templatecompiler.el.Type#getCode()
     */
    @Override
    public String getCode() {
        StringBuilder sb = new StringBuilder();
        sb.append(clearComponentType.getCode());

        if (!ArrayUtils.isEmpty(typeArguments)) {
            sb.append("<");
            for (int i = 0; i < typeArguments.length; i++) {
                Type typeArgument = typeArguments[i];

                if (i != 0) {
                    sb.append(", ");
                }

                sb.append(typeArgument.getCode());
            }

            sb.append(">");
        }
        
        for (int i = 0; i < arrayDepth; i++) {
            sb.append("[]");
        }

        return sb.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + arrayDepth;
        result = prime * result + ((clearComponentType == null) ? 0 : clearComponentType.hashCode());
        result = prime * result + Arrays.hashCode(typeArguments);
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ComplexType other = (ComplexType) obj;
        if (arrayDepth != other.arrayDepth) {
            return false;
        }
        if (clearComponentType == null) {
            if (other.clearComponentType != null) {
                return false;
            }
        } else if (!clearComponentType.equals(other.clearComponentType)) {
            return false;
        }
        if (!Arrays.equals(typeArguments, other.typeArguments)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.richfaces.cdk.templatecompiler.el.Type#getRawType()
     */
    @Override
    public Class<?> getRawType() {
        if (cachedRawType == null) {
            Class<?> rawType = clearComponentType.getRawType();
            if (rawType != null) {
                if (arrayDepth != 0) {
                    int[] dimensions = new int[arrayDepth];
                    //TODO: more efficient way to create array class
                    cachedRawType = Array.newInstance(rawType, dimensions).getClass();
                } else {
                    cachedRawType = rawType;
                }
            } 
        }

        return cachedRawType;
    }

    /* (non-Javadoc)
     * @see org.richfaces.cdk.templatecompiler.el.Type#isArray()
     */
    @Override
    public boolean isArray() {
        return arrayDepth != 0;
    }
    
    /* (non-Javadoc)
     * @see org.richfaces.cdk.templatecompiler.el.Type#isAssignableFrom(org.richfaces.cdk.templatecompiler.el.Type)
     */
    @Override
    public boolean isAssignableFrom(Type anotherType) {
        Class<?> thisWrapperClass = TypesFactory.getWrapperClass(getRawType());
        Class<?> anotherWrapperClass = TypesFactory.getWrapperClass(anotherType.getRawType());
        
        if (thisWrapperClass.isAssignableFrom(anotherWrapperClass)) {
            Type[] thisTypeArguments = getTypeArguments();
            if (ArrayUtils.isEmpty(thisTypeArguments)) {
                return true;
            }
            
            Type[] anotherTypeArguments = anotherType.getTypeArguments();
            if (ArrayUtils.isEmpty(anotherTypeArguments)) {
                return true;
            }
            
            return Arrays.equals(thisTypeArguments, anotherTypeArguments);
        } else {
            return false;
        }
    }
}
