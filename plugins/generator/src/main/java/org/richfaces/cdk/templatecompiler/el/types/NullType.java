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

import java.util.Collection;
import java.util.Collections;

import org.richfaces.cdk.templatecompiler.el.Type;

/**
 * @author Nick Belaevski
 * 
 */
public final class NullType implements Type {

    /**
     * Singleton instance of {@link NullType}
     */
    public static final Type INSTANCE = new NullType();
    
    private NullType() {
        //this class is a singleton, thus has private ctor
    }
    
    /* (non-Javadoc)
     * @see org.richfaces.cdk.templatecompiler.el.Type#getCode()
     */
    @Override
    public String getCode() {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.richfaces.cdk.templatecompiler.el.Type#getImportsIterator()
     */
    @Override
    public Collection<Class<?>> getImportsList() {
        return Collections.emptyList();
    }

    /* (non-Javadoc)
     * @see org.richfaces.cdk.templatecompiler.el.Type#isNullType()
     */
    @Override
    public boolean isNullType() {
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.richfaces.cdk.templatecompiler.el.Type#getRawType()
     */
    @Override
    public Class<?> getRawType() {
        return Object.class;
    }

    /* (non-Javadoc)
     * @see org.richfaces.cdk.templatecompiler.el.Type#getTypeArguments()
     */
    @Override
    public Type[] getTypeArguments() {
        return null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getName();
    }

    /* (non-Javadoc)
     * @see org.richfaces.cdk.templatecompiler.el.Type#getContainerType()
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
        return true;
    }
}