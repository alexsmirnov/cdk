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

import java.util.ArrayList;
import java.util.List;

import org.richfaces.cdk.templatecompiler.FreeMarkerRenderer;

/**
 * @author Nick Belaevski
 * @since 4.0
 */
public class MethodBodyStatementsContainer implements MethodBodyStatement {

    private List<MethodBodyStatement> statements = new ArrayList<MethodBodyStatement>();

    public List<MethodBodyStatement> getStatements() {
        return statements;
    }

    public void addStatement(MethodBodyStatement statement) {
        statements.add(statement);
    }

    public void addStatement(int index, MethodBodyStatement statement) {
        statements.add(index, statement);
    }

    public void addStatement(String statementCode) {
        addStatement(new MethodBodyStatementImpl(statementCode));
    }

    public void addStatement(int index, String statementCode) {
        addStatement(index, new MethodBodyStatementImpl(statementCode));
    }

    public boolean isEmpty() {
        return statements.isEmpty();
    }

    /* (non-Javadoc)
      * @see org.richfaces.cdk.templatecompiler.builder.model.MethodBodyStatement#getCode()
      */
    @Override
    public String getCode(FreeMarkerRenderer renderer) {
        StringBuilder sb = new StringBuilder();
        for (MethodBodyStatement statement : statements) {
            sb.append(statement.getCode(renderer));
            sb.append('\n');
        }

        return sb.toString();
    }
}
