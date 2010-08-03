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

import static org.richfaces.cdk.templatecompiler.el.HelperMethod.*;
import static org.richfaces.cdk.util.JavaUtils.*;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.jboss.el.parser.AstCompositeExpression;
import org.jboss.el.parser.ELParser;
import org.jboss.el.parser.Node;
import org.richfaces.cdk.templatecompiler.el.node.ITreeNode;
import org.richfaces.cdk.templatecompiler.el.types.TypesFactory;
/**
 * Entry point for parsing EL expressions. @see parse() method.
 *
 * @author amarkhel
 */
public final class ELVisitor {

    private String parsedExpression = null;

    private Type expressionType = null;

    private Map<String, Type> variables = null;
    
    private Set<HelperMethod> usedHelperMethods = EnumSet.noneOf(HelperMethod.class); 

    private Set<HelperMethod> usedConversionMethods = EnumSet.noneOf(HelperMethod.class); 

    public Type getExpressionType() {
        return expressionType;
    }

    public void setExpressionType(Type variableType) {
        this.expressionType = variableType;
    }
    
    /**
     * @return the variables
     */
    public Map<String, Type> getVariables() {
        return variables;
    }
    
    public Type getVariable(String name) throws ParsingException {
        Type variableType = variables.get(name);
        if (variableType == null) {
            throw new ParsingException(MessageFormat.format(
                "No type found in context for identifier ''{0}'', handling as generic Object", name));
        }

        return variableType;
    }
    
    /**
     * @return the uses
     */
    public Set<HelperMethod> getUsedHelperMethods() {
        return usedHelperMethods;
    }

    /**
     * @return the parsedExpression
     */
    public String getParsedExpression() {
        return parsedExpression;
    }

    /**
     * Parse specified EL expression and return Java code, that represent this expression
     *
     * @param expression - expression to resolve
     * @param contextMap - Map<String, Class<?>> - context for search classes.
     * @return generated Java code.
     * @throws ParsingException - if error occurred during parsing.
     */
    public void parse(String expression, Map<String, Type> contextMap, Type expectedType) throws ParsingException {
        reset();

        Node ret = ELParser.parse(expression);
        variables = contextMap;

        if (ret instanceof AstCompositeExpression && ret.jjtGetNumChildren() >= 2) {
            //AstCompositeExpression with 2+ children is a mixed expression
            getUsedHelperMethods().add(TO_STRING_CONVERSION);
        }

        if (ret != null && ret.jjtGetNumChildren() > 0) {
            parsedExpression = this.visit(ret);
        } else {
            parsedExpression = getEscapedString("");
            expressionType = TypesFactory.getType(String.class);
        }
        
        parsedExpression = ELParserUtils.coerceToType(parsedExpression, this, expectedType);
    }

    private String visit(Node node) throws ParsingException {
        int numChildren = node.jjtGetNumChildren();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < numChildren; i++) {
            Node child = node.jjtGetChild(i);

            ITreeNode treeNode = ELParserUtils.determineNodeType(child);

            treeNode.visit(sb, this);

            if (i != numChildren - 1) {
                sb.append(" + ");
            }
        }

        return sb.toString();
    }

    private void reset() {
        parsedExpression = null;
        usedHelperMethods.clear();
        variables = null;
        expressionType = null;
    }

}
