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
package org.richfaces.cdk.templatecompiler.el.node;

import static org.richfaces.cdk.templatecompiler.el.HelperMethod.*;

import org.jboss.el.parser.Node;
import org.richfaces.cdk.templatecompiler.el.ELNodeConstants;
import org.richfaces.cdk.templatecompiler.el.ELVisitor;
import org.richfaces.cdk.templatecompiler.el.ParsingException;
import org.richfaces.cdk.templatecompiler.el.Type;
import org.richfaces.cdk.templatecompiler.el.types.TypesFactory;

/**
 * @author Nick Belaevski
 * 
 */
public class EqualityTestTreeNode extends AbstractTreeNode {

    private boolean negateValue = false;
    
    /**
     * @param node
     */
    public EqualityTestTreeNode(Node node) {
        super(node);
    }

    public EqualityTestTreeNode(Node node, boolean negateValue) {
        super(node);
        
        this.negateValue = negateValue;
    }
    
    private boolean isPrimitive(Type type) {
        return type.getRawType().isPrimitive();
    }
    
    private boolean useIsEqualsMethod(Type firstType, Type secondType) {
        if (firstType.isNullType() && !isPrimitive(secondType)) {
            return false;
        }

        if (secondType.isNullType() && !isPrimitive(firstType)) {
            return false;
        }
        
        if (isPrimitive(firstType) && isPrimitive(secondType)) {
            return false;
        }
        
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.richfaces.cdk.templatecompiler.el.node.AbstractTreeNode#visit(java.lang.StringBuilder, java.util.Map, org.richfaces.cdk.templatecompiler.el.ELVisitor)
     */
    @Override
    public void visit(StringBuilder sb, ELVisitor visitor) throws ParsingException {
        String firstChildOutput = getChildOutput(0, visitor);
        Type firstChildType = visitor.getExpressionType();
        String secondChildOutput = getChildOutput(1, visitor);
        Type secondChildType = visitor.getExpressionType();

        if (useIsEqualsMethod(firstChildType, secondChildType)) {
            
            if (negateValue) {
                sb.append(ELNodeConstants.EXCLAMATION_MARK);
            } else {
                //do nothing
            }
            
            sb.append(ELNodeConstants.IS_EQUAL_FUNCTION);
            
            sb.append(ELNodeConstants.LEFT_BRACKET);
            sb.append(firstChildOutput);
            sb.append(ELNodeConstants.COMMA);
            sb.append(secondChildOutput);
            sb.append(ELNodeConstants.RIGHT_BRACKET);

            visitor.getUsedHelperMethods().add(EQUALS_CHECK);
        } else {
            sb.append(ELNodeConstants.LEFT_BRACKET);

            sb.append(firstChildOutput);
            
            if (negateValue) {
                sb.append(ELNodeConstants.INEQUALITY_OPERATOR);
            } else {
                sb.append(ELNodeConstants.EQUALITY_OPERATOR);
            }
            
            sb.append(secondChildOutput);

            sb.append(ELNodeConstants.RIGHT_BRACKET);
        }
        
        visitor.setExpressionType(TypesFactory.getType(Boolean.TYPE));
    }

}
