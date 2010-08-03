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
 * Representation of method argument
 *
 * @author Maksim Kaszynski
 */
public class Argument {

    private String name;
    
    private JavaClass type;

    public Argument(String name, Class<?> type) {
        super();
        this.name = name;
        this.type = new JavaClass(type);
    }

    public Argument(String name, String type) {
        super();
        this.name = name;
        this.type = new JavaClass(type);
    }
    
    public static Argument arg(String name, Class<?> type) {
        return new Argument(name, type);
    }

    public JavaClass getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
