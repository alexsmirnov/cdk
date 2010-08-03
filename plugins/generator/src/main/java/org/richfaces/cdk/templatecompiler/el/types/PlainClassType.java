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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;

import org.richfaces.cdk.templatecompiler.el.Type;

/**
 * @author Nick Belaevski
 * 
 */
public class PlainClassType implements Type {

    private Class<?> clazz;

    public PlainClassType(Class<?> clazz) {
        super();
        
        if (clazz.isArray()) {
            throw new IllegalArgumentException("Array classes are not supported");
        }
        
        this.clazz = clazz;
    }

    /* (non-Javadoc)
     * @see org.richfaces.cdk.templatecompiler.el.Type#getCode()
     */
    @Override
    public String getCode() {
        return clazz.getSimpleName();
    }

    /* (non-Javadoc)
     * @see org.richfaces.cdk.templatecompiler.el.Type#getImportsIterator()
     */
    @Override
    public Collection<Class<?>> getImportsList() {
        return Arrays.<Class<?>>asList(clazz);
    }

    /* (non-Javadoc)
     * @see org.richfaces.cdk.templatecompiler.el.Type#isNullType()
     */
    @Override
    public boolean isNullType() {
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.richfaces.cdk.templatecompiler.el.Type#getRawType()
     */
    @Override
    public Class<?> getRawType() {
        return clazz;
    }

    /* (non-Javadoc)
     * @see org.richfaces.cdk.templatecompiler.el.Type#getTypeArguments()
     */
    @Override
    public Type[] getTypeArguments() {
        return null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
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
        PlainClassType other = (PlainClassType) obj;
        if (clazz == null) {
            if (other.clazz != null) {
                return false;
            }
        } else if (!clazz.equals(other.clazz)) {
            return false;
        }
        return true;
    }
 
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return MessageFormat.format("{0}: {1}", getClass().getName(), clazz.toString());
    }

    /* (non-Javadoc)
     * @see org.richfaces.cdk.templatecompiler.el.Type#getCompositeType()
     */
    @Override
    public Type getContainerType() {
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.richfaces.cdk.templatecompiler.el.Type#isArray()
     */
    @Override
    public boolean isArray() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.richfaces.cdk.templatecompiler.el.Type#isAssignableFrom(org.richfaces.cdk.templatecompiler.el.Type)
     */
    @Override
    public boolean isAssignableFrom(Type anotherType) {
        if (anotherType.isNullType()) {
            return !clazz.isPrimitive();
        } else {
            Class<?> thisWrapperClass = TypesFactory.getWrapperClass(clazz);
            Class<?> anotherWrapperClass = TypesFactory.getWrapperClass(anotherType.getRawType());
            
            return thisWrapperClass.isAssignableFrom(anotherWrapperClass);
        }
    }
}
