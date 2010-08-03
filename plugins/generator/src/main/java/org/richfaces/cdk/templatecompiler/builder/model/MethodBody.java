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

import java.util.HashSet;
import java.util.Set;

/**
 * @author Maksim Kaszynski
 * @author Nick Belaevski
 */
public class MethodBody extends MethodBodyStatementsContainer {
    private Set<Class<?>> usedClasses = new HashSet<Class<?>>();
    private JavaMethod method;

    public MethodBody() {
    }

    public MethodBody(JavaMethod method) {
        super();
        this.method = method;
    }

    protected JavaMethod getMethod() {
        return method;
    }

    protected void setMethod(JavaMethod method) {
        this.method = method;
    }

    public void addType(Class<?> clazz) {
        usedClasses.add(clazz);
    }

    public Set<Class<?>> getUsedClasses() {
        return usedClasses;
    }

}
