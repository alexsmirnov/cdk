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

package org.richfaces.cdk.templatecompiler.parser.el.test;

import static org.junit.Assert.*;
import static org.richfaces.cdk.templatecompiler.el.HelperMethod.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.richfaces.cdk.templatecompiler.el.ELVisitor;
import org.richfaces.cdk.templatecompiler.el.ParsingException;
import org.richfaces.cdk.templatecompiler.el.Type;
import org.richfaces.cdk.templatecompiler.el.types.TypesFactory;

public class ELParserTest {

    private ELVisitor visitor;

    private void parseExpression(String expression) throws ParsingException {
        parseExpression(expression, Object.class);
    }

    private void parseExpression(String expression, Class<?> returnType) throws ParsingException {
        Map<String, Type> contextMap = new HashMap<String, Type>();

        contextMap.put("action", TypesFactory.getType(org.richfaces.cdk.templatecompiler.parser.el.test.Bean.class));
        contextMap.put("clientId", TypesFactory.getType(String.class));
        contextMap.put("test", TypesFactory.getType(boolean.class));
        contextMap.put("otherTest", TypesFactory.getType(boolean.class));
        contextMap.put("this", TypesFactory.getType(Object.class));
        contextMap.put("super", TypesFactory.getType(Object.class));
        contextMap.put("objectVar", TypesFactory.getType(Object.class));

        visitor.parse(expression, contextMap, TypesFactory.getType(returnType));
    }

    @Before
    public void setUp() {
        visitor = new ELVisitor();
    }

    @After
    public void tearDown() {
        visitor = null;
    }

    @Test
    public void testAnd() throws Exception {
        parseExpression("#{test and otherTest}");
        assertEquals("(test && otherTest)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());

        parseExpression("#{otherTest && test}");
        assertEquals("(otherTest && test)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());

        parseExpression("#{action and otherTest}");
        assertTrue(visitor.getUsedHelperMethods().contains(TO_BOOLEAN_CONVERSION));
        assertEquals("(convertToBoolean(action) && otherTest)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());

        parseExpression("#{test && action}");
        assertTrue(visitor.getUsedHelperMethods().contains(TO_BOOLEAN_CONVERSION));
        assertEquals("(test && convertToBoolean(action))", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testBooleanReturnType() throws Exception {
        parseExpression("#{clientId}", Boolean.TYPE);

        assertEquals("convertToBoolean(clientId)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
        assertTrue(visitor.getUsedHelperMethods().contains(TO_BOOLEAN_CONVERSION));

        parseExpression("#{test}", Boolean.TYPE);

        assertEquals("test", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
        assertFalse(visitor.getUsedHelperMethods().contains(TO_BOOLEAN_CONVERSION));
    }

    @Test
    public void testChoice() throws Exception {
        parseExpression("#{test ? 2 : 3}");
        assertEquals("(test ? 2 : 3)", visitor.getParsedExpression());
        assertEquals(Integer.TYPE, visitor.getExpressionType().getRawType());

        parseExpression("#{test ? null : 'string'}");
        assertEquals("(test ? null : \"string\")", visitor.getParsedExpression());
        assertEquals(String.class, visitor.getExpressionType().getRawType());

        parseExpression("#{action ? null : 'string'}");
        assertEquals("(convertToBoolean(action) ? null : \"string\")", visitor.getParsedExpression());
        assertEquals(String.class, visitor.getExpressionType().getRawType());
        assertTrue(visitor.getUsedHelperMethods().contains(TO_BOOLEAN_CONVERSION));
    }

    @Test
    public void testDiv() throws Exception {
        parseExpression("#{1/2}");
        assertEquals("(1 / 2)", visitor.getParsedExpression());
        assertEquals(Integer.TYPE, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testEmpty() throws Exception {
        parseExpression("#{empty action.array}");
        assertEquals("isEmpty(action.getArray())", visitor.getParsedExpression());
        assertTrue(visitor.getUsedHelperMethods().contains(EMPTINESS_CHECK));
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testEmptyString() throws Exception {
        parseExpression("");
        assertEquals("\"\"", visitor.getParsedExpression());
        assertEquals(String.class, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testEquals() throws Exception {
        parseExpression("#{1 eq 2}");
        assertEquals("(1 == 2)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
        assertFalse(visitor.getUsedHelperMethods().contains(EQUALS_CHECK));

        parseExpression("#{3 == 2}");
        assertEquals("(3 == 2)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
        assertFalse(visitor.getUsedHelperMethods().contains(EQUALS_CHECK));

        parseExpression("#{action == 2}");
        assertEquals("isEqual(action,2)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
        assertTrue(visitor.getUsedHelperMethods().contains(EQUALS_CHECK));

        parseExpression("#{2 eq action}");
        assertEquals("isEqual(2,action)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
        assertTrue(visitor.getUsedHelperMethods().contains(EQUALS_CHECK));

        parseExpression("#{action == clientId}");
        assertEquals("isEqual(action,clientId)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
        assertTrue(visitor.getUsedHelperMethods().contains(EQUALS_CHECK));

        parseExpression("#{action eq clientId}");
        assertEquals("isEqual(action,clientId)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
        assertTrue(visitor.getUsedHelperMethods().contains(EQUALS_CHECK));

        parseExpression("#{action eq null}");
        assertEquals("(action == null)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
        assertFalse(visitor.getUsedHelperMethods().contains(EQUALS_CHECK));

        parseExpression("#{2 == null}");
        assertEquals("isEqual(2,null)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
        assertTrue(visitor.getUsedHelperMethods().contains(EQUALS_CHECK));
    }

    @Test
    public void testFalse() throws Exception {
        parseExpression("#{false}");
        assertEquals("false", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testFloat() throws Exception {
        parseExpression("#{5.0}");
        assertEquals("Double.valueOf(5.0)", visitor.getParsedExpression());
        assertEquals(Double.TYPE, visitor.getExpressionType().getRawType());

        parseExpression("#{5.012e+34}");
        assertEquals("Double.valueOf(5.012e+34)", visitor.getParsedExpression());
        assertEquals(Double.TYPE, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testFunction() throws Exception {
        parseExpression("#{super:getType()}");
        assertEquals("super.getType()", visitor.getParsedExpression());
        Type variableType = visitor.getExpressionType();
    }

    @Test
    public void testGreatThen() throws Exception {
        parseExpression("#{1 gt 2}");
        assertEquals("(1 > 2)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());

        parseExpression("#{3 > 2}");
        assertEquals("(3 > 2)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testGreatThenEquals() throws Exception {
        parseExpression("#{1 ge 2}");
        assertEquals("(1 >= 2)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());

        parseExpression("#{3 >= 2}");
        assertEquals("(3 >= 2)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testIdentifier() throws Exception {
        parseExpression("#{clientId}");
        assertEquals("clientId", visitor.getParsedExpression());
        assertEquals(String.class, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testInteger() throws Exception {
        parseExpression("#{152}");
        assertEquals("152", visitor.getParsedExpression());
        assertEquals(Integer.TYPE, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testLessThen() throws Exception {
        parseExpression("#{1 lt 2}");
        assertEquals("(1 < 2)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());

        parseExpression("#{3 < 2}");
        assertEquals("(3 < 2)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testLessThenEquals() throws Exception {
        parseExpression("#{1 le 2}");
        assertEquals("(1 <= 2)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());

        parseExpression("#{3 <= 2}");
        assertEquals("(3 <= 2)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testLiteral() throws Exception {
        parseExpression("clientId");
        assertEquals("\"clientId\"", visitor.getParsedExpression());
        assertEquals(String.class, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testLiteralWithDeferred() throws Exception {
        parseExpression("#{1}#{2}");
        assertEquals("convertToString(1) + convertToString(2)", visitor.getParsedExpression());
        assertEquals(String.class, visitor.getExpressionType().getRawType());

        parseExpression("abs #{getType()}");
        assertEquals("\"abs \" + convertToString(this.getType())", visitor.getParsedExpression());
        assertEquals(String.class, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testLiteralWithDeferred2() throws Exception {
        parseExpression("#{getType()} abs ");
        assertEquals("convertToString(this.getType()) + \" abs \"", visitor.getParsedExpression());
        assertEquals(String.class, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testMethod() throws Exception {
        parseExpression("#{action.readOnly}");
        assertEquals("action.isReadOnly()", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testMethodReturnArray() throws Exception {
        parseExpression("#{action.array}");
        assertEquals("action.getArray()", visitor.getParsedExpression());
        assertEquals(UIComponent[].class, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testMethodReturnArrayElement() throws Exception {
        parseExpression("#{action.array[0]}");
        assertEquals("action.getArray()[0]", visitor.getParsedExpression());
        assertEquals(UIComponent.class, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testMethodReturnList() throws Exception {
        parseExpression("#{action.components}");
        assertEquals("action.getComponents()", visitor.getParsedExpression());
        Type variableType = visitor.getExpressionType();
        assertEquals(List.class, variableType.getRawType());
        assertEquals(UIComponent.class, variableType.getContainerType().getRawType());
    }

    @Test
    public void testMethodReturnListElement() throws Exception {
        parseExpression("#{action.components[0]}");
        assertEquals("action.getComponents().get(0)", visitor.getParsedExpression());
        assertEquals(UIComponent.class, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testMethodReturnListElement2() throws Exception {
        parseExpression("#{action.components[0].rendered}");
        assertEquals("action.getComponents().get(0).isRendered()", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
    }

    // @Test
    // public void testMethodReturnMapElement1() throws Exception {
    // assertEquals("action.getFacets().get(\"header\")", resolveExpression("#{action.facets.header}"));
    // }

    @Test
    public void testMethodReturnMap() throws Exception {
        parseExpression("#{action.facets}");
        assertEquals("action.getFacets()", visitor.getParsedExpression());
        Type variableType = visitor.getExpressionType();
        assertEquals(Map.class, variableType.getRawType());
        assertEquals(UIComponent.class, variableType.getContainerType().getRawType());

        parseExpression("#{action.rawMap}");
        assertEquals("action.getRawMap()", visitor.getParsedExpression());
    }

    @Test
    public void testMethodReturnMapElement() throws Exception {
        parseExpression("#{action.getFacet('header')}");
        assertEquals("action.getFacet(\"header\")", visitor.getParsedExpression());
        assertEquals(UIComponent.class, visitor.getExpressionType().getRawType());

        parseExpression("#{action.facets['header']}");
        assertEquals("action.getFacets().get(\"header\")", visitor.getParsedExpression());
        assertEquals(UIComponent.class, visitor.getExpressionType().getRawType());

        parseExpression("#{action.rawMap['something']}");
        assertEquals("action.getRawMap().get(\"something\")", visitor.getParsedExpression());
        assertEquals(Object.class, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testMethodReturnMapElement2() throws Exception {
        parseExpression("#{action.facets.toString()}");
        assertEquals("action.getFacets().toString()", visitor.getParsedExpression());
        assertEquals(String.class, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testMethodReturnMapElement3() throws Exception {
        // assertEquals("action.getFacet(\"header\").isRendered()",
        // resolveExpression("#{action.getFacet('header').rendered}"));
        parseExpression("#{action.facets['header'].rendered}");
        assertEquals("action.getFacets().get(\"header\").isRendered()", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testMethodWithParam() throws Exception {
        parseExpression("#{getType(action.array[0].rendered, action.readOnly, true)}");
        assertEquals("this.getType(action.getArray()[0].isRendered(),action.isReadOnly(),true)", visitor
            .getParsedExpression());

        Type variableType = visitor.getExpressionType();

        parseExpression("#{action.count(123)}");
        assertEquals("action.count(123)", visitor.getParsedExpression());
        assertEquals(Integer.class, visitor.getExpressionType().getRawType());

        parseExpression("#{action.count(clientId)}");
        assertEquals("action.count(clientId)", visitor.getParsedExpression());
        assertEquals(Object.class, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testMinus() throws Exception {
        parseExpression("#{1-2}");
        assertEquals("(1 - 2)", visitor.getParsedExpression());
        assertEquals(Integer.TYPE, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testMod() throws Exception {
        parseExpression("#{1%2}");
        assertEquals("(1 % 2)", visitor.getParsedExpression());
        assertEquals(Integer.TYPE, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testMult() throws Exception {
        parseExpression("#{1*2}");
        assertEquals("(1 * 2)", visitor.getParsedExpression());
        assertEquals(Integer.TYPE, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testNegative() throws Exception {
        parseExpression("#{-5}");
        assertEquals("-5", visitor.getParsedExpression());
        assertEquals(Integer.TYPE, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testNegativeFloat() throws Exception {
        parseExpression("#{-5.0}");
        assertEquals("-Double.valueOf(5.0)", visitor.getParsedExpression());
        assertEquals(Double.TYPE, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testNestedMethod() throws Exception {
        parseExpression("#{action.testBean2.string}");
        assertEquals("action.getTestBean2().getString()", visitor.getParsedExpression());
        assertEquals(String.class, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testNonExistingMethod() throws Exception {
        parseExpression("#{action.doSomething(clientId, 123)}");
        assertEquals("action.doSomething(clientId,123)", visitor.getParsedExpression());
        assertEquals(Object.class, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testNot() throws Exception {
        parseExpression("#{not test}");
        assertEquals("(!test)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());

        parseExpression("#{!otherTest}");
        assertEquals("(!otherTest)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());

        parseExpression("#{!action}");
        assertEquals("(!convertToBoolean(action))", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
        assertTrue(visitor.getUsedHelperMethods().contains(TO_BOOLEAN_CONVERSION));
    }

    @Test
    public void testNotEqual() throws Exception {
        parseExpression("#{1 ne 3}");
        assertEquals("(1 != 3)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
        assertFalse(visitor.getUsedHelperMethods().contains(EQUALS_CHECK));

        parseExpression("#{2 != 3}");
        assertEquals("(2 != 3)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
        assertFalse(visitor.getUsedHelperMethods().contains(EQUALS_CHECK));

        parseExpression("#{action != 2}");
        assertEquals("!isEqual(action,2)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
        assertTrue(visitor.getUsedHelperMethods().contains(EQUALS_CHECK));

        parseExpression("#{2 ne action}");
        assertEquals("!isEqual(2,action)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
        assertTrue(visitor.getUsedHelperMethods().contains(EQUALS_CHECK));

        parseExpression("#{action != clientId}");
        assertEquals("!isEqual(action,clientId)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
        assertTrue(visitor.getUsedHelperMethods().contains(EQUALS_CHECK));

        parseExpression("#{action ne clientId}");
        assertEquals("!isEqual(action,clientId)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
        assertTrue(visitor.getUsedHelperMethods().contains(EQUALS_CHECK));

        parseExpression("#{action ne null}");
        assertEquals("(action != null)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
        assertFalse(visitor.getUsedHelperMethods().contains(EQUALS_CHECK));

        parseExpression("#{2 != null}");
        assertEquals("!isEqual(2,null)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
        assertTrue(visitor.getUsedHelperMethods().contains(EQUALS_CHECK));
    }

    @Test
    public void testNull() throws Exception {
        parseExpression("#{null}");
        assertEquals("null", visitor.getParsedExpression());
        assertTrue(visitor.getExpressionType().isNullType());
    }

    @Test
    public void testOr() throws Exception {
        parseExpression("#{test or otherTest}");
        assertEquals("(test || otherTest)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());

        parseExpression("#{otherTest || test}");
        assertEquals("(otherTest || test)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());

        parseExpression("#{action or otherTest}");
        assertTrue(visitor.getUsedHelperMethods().contains(TO_BOOLEAN_CONVERSION));
        assertEquals("(convertToBoolean(action) || otherTest)", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());

        parseExpression("#{test || action}");
        assertTrue(visitor.getUsedHelperMethods().contains(TO_BOOLEAN_CONVERSION));
        assertEquals("(test || convertToBoolean(action))", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testPlus() throws Exception {
        // TODO: tests involving double values
        parseExpression("#{1+2}");
        assertEquals("(1 + 2)", visitor.getParsedExpression());
        assertEquals(Integer.TYPE, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testString() throws Exception {
        parseExpression("#{\"nabc\"}");
        assertEquals("\"nabc\"", visitor.getParsedExpression());
        assertEquals(String.class, visitor.getExpressionType().getRawType());

        parseExpression("#{'nabc'}");
        assertEquals("\"nabc\"", visitor.getParsedExpression());
        assertEquals(String.class, visitor.getExpressionType().getRawType());

        parseExpression("#{'\tabc'}");
        assertEquals("\"\\tabc\"", visitor.getParsedExpression());
        assertEquals(String.class, visitor.getExpressionType().getRawType());

        parseExpression("#{'/nabc'}");
        assertEquals("\"/nabc\"", visitor.getParsedExpression());
        assertEquals(String.class, visitor.getExpressionType().getRawType());

        parseExpression("#{'na\"bc'}");
        assertEquals("\"na\\\"bc\"", visitor.getParsedExpression());
        assertEquals(String.class, visitor.getExpressionType().getRawType());

        parseExpression("#{'na\\\\bc'}");
        assertEquals("\"na\\\\bc\"", visitor.getParsedExpression());
        assertEquals(String.class, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testStringReturnType() throws Exception {
        parseExpression("#{clientId}", String.class);

        assertEquals("clientId", visitor.getParsedExpression());
        assertEquals(String.class, visitor.getExpressionType().getRawType());
        assertFalse(visitor.getUsedHelperMethods().contains(TO_STRING_CONVERSION));

        parseExpression("#{test}", String.class);

        assertEquals("convertToString(test)", visitor.getParsedExpression());
        assertEquals(String.class, visitor.getExpressionType().getRawType());
        assertTrue(visitor.getUsedHelperMethods().contains(TO_STRING_CONVERSION));
    }

    @Test
    public void testThisFunction() throws Exception {
        parseExpression("#{getType()}");
        assertEquals("this.getType()", visitor.getParsedExpression());
        Type variableType = visitor.getExpressionType();

        parseExpression("#{this.getType()}");
        assertEquals("this.getType()", visitor.getParsedExpression());
    }

    @Test
    public void testTrue() throws Exception {
        parseExpression("#{true}");
        assertEquals("true", visitor.getParsedExpression());
        assertEquals(Boolean.TYPE, visitor.getExpressionType().getRawType());
    }

    @Test
    public void testVariableFunction() throws Exception {
        parseExpression("#{objectVar.getType()}");
        assertEquals("objectVar.getType()", visitor.getParsedExpression());
        Type variableType = visitor.getExpressionType();
    }

    @Test
    public void testWrongExpression() throws Exception {
        try {
            parseExpression("#{bean.property}");
            fail("Parsing Exception is not thrown");
        } catch (ParsingException pe) {
            // TODO - check message
            // ignore exception
        }
    }

    @Test
    public void testWrongExpression2() throws Exception {
        parseExpression("#{action.property}");
        assertEquals("action.getProperty()", visitor.getParsedExpression());
        assertEquals(Object.class, visitor.getExpressionType().getRawType());
    }
}
