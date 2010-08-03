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

package org.richfaces.cdk.templatecompiler.el.node;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import org.jboss.el.parser.Node;
import org.richfaces.cdk.templatecompiler.el.ELNodeConstants;
import org.richfaces.cdk.templatecompiler.el.ELParserUtils;
import org.richfaces.cdk.templatecompiler.el.ELVisitor;
import org.richfaces.cdk.templatecompiler.el.ParsingException;
import org.richfaces.cdk.templatecompiler.el.Type;
import org.richfaces.cdk.templatecompiler.el.types.TypesFactory;

/**
 * This class extend AbstractTreeNode and wrap AstPropertySuffix node.
 *
 * @author amarkhel
 */
public class AstPropertySuffixTreeNode extends AbstractTreeNode {

    public AstPropertySuffixTreeNode(Node node) {
        super(node);
    }

    private String capitalize(String propertyName) {
        char[] chars = propertyName.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    private Method getReadMethod(Class<?> clazz, String propertyName) {
        if (clazz == null) {
            return null;
        }

        PropertyDescriptor propertyDescriptor = null;
        try {
            propertyDescriptor = ELParserUtils.getPropertyDescriptor(clazz, propertyName);
        } catch (ParsingException e) {
            // TODO: handle exception
        }
        
        if (propertyDescriptor == null) {
            return null;
        }

        return propertyDescriptor.getReadMethod();
    }

    @Override
    public void visit(StringBuilder sb, ELVisitor visitor) throws ParsingException {
        String propertyName = getNode().getImage();

        Type variableType = visitor.getExpressionType();
        Class<?> clazz = variableType.getRawType();

        String readMethodName;
        Type readMethodReturnType;

        Method readMethod = getReadMethod(clazz, propertyName);
        if (readMethod != null) {
            readMethodName = readMethod.getName();
            readMethodReturnType = TypesFactory.getType(readMethod.getGenericReturnType());
        } else {
            readMethodName = ELNodeConstants.GETTER_PREFIX + capitalize(propertyName);
            readMethodReturnType = TypesFactory.getType(Object.class);
        }

        visitor.setExpressionType(readMethodReturnType);

        sb.append(ELNodeConstants.DOT);
        sb.append(readMethodName);
        sb.append(ELNodeConstants.LEFT_BRACKET);
        sb.append(ELNodeConstants.RIGHT_BRACKET);
    }
}
