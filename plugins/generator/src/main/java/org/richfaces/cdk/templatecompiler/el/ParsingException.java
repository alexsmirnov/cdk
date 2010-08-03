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

package org.richfaces.cdk.templatecompiler.el;

/**
 * Parsing Exception
 *
 * @author amarkhel
 */
public class ParsingException extends Exception {
    private static final long serialVersionUID = 6045782920008419804L;

    public ParsingException() {
    }

    /**
     * @param message
     */
    public ParsingException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public ParsingException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
