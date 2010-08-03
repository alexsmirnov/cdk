/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and individual contributors
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
package org.richfaces.cdk.templatecompiler.el;

import static org.richfaces.cdk.templatecompiler.el.ELNodeConstants.*;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author Nick Belaevski
 * 
 */
public enum HelperMethod {

    TO_STRING_CONVERSION(CONVERT_TO_STRING_FUNCTION, String.class, Object.class),
    TO_BOOLEAN_CONVERSION(CONVERT_TO_BOOLEAN_FUNCTION, Boolean.TYPE, Object.class),
    EMPTINESS_CHECK(IS_EMPTY_FUNCTION, Boolean.TYPE, Object.class),
    EQUALS_CHECK(IS_EQUAL_FUNCTION, Boolean.TYPE, Object.class, Object.class);

    private static final Set<HelperMethod> CONVERSION_METHODS = EnumSet.of(TO_STRING_CONVERSION, TO_BOOLEAN_CONVERSION);
    
    private String name;

    private Class<?> returnType;
    
    private Class<?>[] argumentTypes;
    
    private HelperMethod(String name, Class<?> returnType, Class<?>... argumentTypes) {
        this.name = name;
        this.returnType = returnType;
        this.argumentTypes = argumentTypes;
    }

    public String getName() {
        return name;
    }
    
    public Class<?> getReturnType() {
        return returnType;
    }
    
    public Class<?>[] getArgumentTypes() {
        return argumentTypes;
    }
    
    public static Set<HelperMethod> getConversionMethods() {
        return CONVERSION_METHODS;
    }
}
