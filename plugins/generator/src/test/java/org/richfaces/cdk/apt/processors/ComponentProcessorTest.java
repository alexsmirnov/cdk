/*
 * $Id$
 *
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

package org.richfaces.cdk.apt.processors;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Collections;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.richfaces.cdk.CdkTestRunner;
import org.richfaces.cdk.FileManager;
import org.richfaces.cdk.Mock;
import org.richfaces.cdk.NamingConventions;
import org.richfaces.cdk.Source;
import org.richfaces.cdk.Sources;
import org.richfaces.cdk.Stub;
import org.richfaces.cdk.annotations.Description;
import org.richfaces.cdk.annotations.Facet;
import org.richfaces.cdk.annotations.JsfComponent;
import org.richfaces.cdk.apt.SourceUtils;
import org.richfaces.cdk.apt.SourceUtils.BeanProperty;
import org.richfaces.cdk.model.ComponentLibrary;
import org.richfaces.cdk.model.ComponentModel;
import org.richfaces.cdk.model.FacetModel;
import org.richfaces.cdk.xmlconfig.JAXB;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;

/**
 * <p class="changed_added_4_0">
 * </p>
 * 
 * @author asmirnov@exadel.com
 * 
 */
@RunWith(CdkTestRunner.class)
public class ComponentProcessorTest extends AnnotationProcessorTest {
    public static final String LARGE_GIF = "/icons/Large.gif";
    public static final String SMALL_JPG = "/icons/Small.jpg";
    private static final String COMPONENT_CLASS_JAVA = "org/richfaces/cdk/test/component/AbstractTestComponent.java";
    private static final String FACES_COMPONENT_CLASS_JAVA = "org/richfaces/cdk/test/component/UITestCommand.java";
    private static final String FOO_HTML_BAR = "foo.HtmlBar";

    @Mock
    private JsfComponent annotation;

    @Mock
    private AttributesProcessor attributesProcessor;

    @Mock
    private TypeElement componentElement;

    @Mock
    private NamingConventions conventions;

    @Mock
    private Description description;

    @Stub
    private DescriptionProcessor descriptionProcessor;

    @Mock
    private JAXB jaxb;

    @Inject
    private ComponentLibrary library;

    @Inject
    private ComponentModel model;

    @Inject
    private ComponentProcessor processor;

    @Mock
    private BeanProperty property;

    @Stub
    @Source(Sources.RENDERER_TEMPLATES)
    private FileManager sources;

    @Mock
    private SourceUtils utils;

    @Test
    public void testProcessFacetsFromAnnotation() throws Exception {
        Facet facet = createMock(Facet.class);
        expect(utils.getBeanPropertiesAnnotatedWith(eq(Facet.class), same(componentElement))).andReturn(
            Collections.<BeanProperty> emptySet());
        expect(annotation.facets()).andReturn(new Facet[] { facet });
        expect(facet.name()).andReturn("foo");
        expect(facet.description()).andReturn(this.description);
        expect(facet.generate()).andReturn(true);
        // expect(this.description.smallIcon()).andReturn("");
        // expect(this.description.largeIcon()).andReturn("");
        // expect(this.description.displayName()).andReturn("fooFacet").times(2);
        // expect(this.description.value()).andReturn("");
        replay(utils, componentElement, jaxb, annotation, property, facet, description);

        processor.processFacets(componentElement, model, annotation);

        verify(utils, componentElement, jaxb, annotation, property, facet, description);
        assertEquals(1, model.getFacets().size());
        FacetModel facetModel = Iterables.getOnlyElement(model.getFacets());
        assertTrue(facetModel.isGenerate());
        assertEquals("foo", facetModel.getName());
        // assertEquals("my comment", facetModel.getDescription());
        // assertEquals("fooFacet", facetModel.getDisplayname());
        // assertNull(facetModel.getIcon());
    }

    @Test
    public void testProcessFacetsFromProperty() throws Exception {
        Facet facet = createMock(Facet.class);
        expect(utils.getBeanPropertiesAnnotatedWith(eq(Facet.class), same(componentElement))).andReturn(
            Collections.singleton(property));
        expect(annotation.facets()).andReturn(new Facet[0]);
        expect(property.getAnnotation(Facet.class)).andReturn(facet);
        expect(property.getName()).andReturn("foo");
        expect(property.getDocComment()).andReturn("my comment").times(2);
        expect(property.isExists()).andReturn(true);
        expect(facet.description()).andReturn(description);
        expect(facet.generate()).andReturn(true);
        // expect(this.description.smallIcon()).andReturn("");
        // expect(this.description.largeIcon()).andReturn("");
        // expect(this.description.displayName()).andReturn("fooFacet").times(2);
        // expect(this.description.value()).andReturn("");
        replay(utils, componentElement, jaxb, annotation, property, facet, description);

        processor.processFacets(componentElement, model, annotation);

        verify(utils, componentElement, jaxb, annotation, property, facet, description);
        assertEquals(1, model.getFacets().size());
        FacetModel facetModel = Iterables.getOnlyElement(model.getFacets());
        assertTrue(facetModel.isGenerate());
        assertEquals("foo", facetModel.getName());
        // assertEquals("my comment", facetModel.getDescription());
        // assertEquals("fooFacet", facetModel.getDisplayname());
        // assertNull(facetModel.getIcon());
    }

    /**
     * Test method for
     * {@link org.richfaces.cdk.apt.CdkProcessor#process(java.util.Set, javax.annotation.processing.RoundEnvironment)} .
     * 
     * @throws Exception
     */
    @Test
    public void testSetClassNames() throws Exception {
        expect(componentElement.getModifiers()).andReturn(Collections.<Modifier> emptySet());
        expect(componentElement.getQualifiedName()).andReturn(new TestName(FOO_BAR));
        replay(utils, componentElement, jaxb, annotation);
        processor.setClassNames(componentElement, model, "");
        verify(utils, componentElement, jaxb, annotation);
        assertFalse(model.isGenerate());
        assertEquals(FOO_BAR, model.getBaseClass().toString());
        assertNull(model.getTargetClass());
    }

    /**
     * Test method for
     * {@link org.richfaces.cdk.apt.CdkProcessor#process(java.util.Set, javax.annotation.processing.RoundEnvironment)} .
     * 
     * @throws Exception
     */
    @Test
    public void testSetClassNames1() throws Exception {
        expect(componentElement.getModifiers()).andReturn(Collections.<Modifier> singleton(Modifier.ABSTRACT));
        expect(componentElement.getQualifiedName()).andReturn(new TestName(FOO_BAR));
        replay(utils, componentElement, jaxb, annotation);

        processor.setClassNames(componentElement, model, "");

        verify(utils, componentElement, jaxb, annotation);
        assertTrue(model.isGenerate());
        assertEquals(FOO_BAR, model.getBaseClass().toString());
        assertNull(model.getTargetClass());
    }

    /**
     * Test method for
     * {@link org.richfaces.cdk.apt.CdkProcessor#process(java.util.Set, javax.annotation.processing.RoundEnvironment)} .
     * 
     * @throws Exception
     */
    @Test
    public void testSetClassNames2() throws Exception {
        expect(componentElement.getModifiers()).andReturn(Collections.<Modifier> emptySet());
        expect(componentElement.getQualifiedName()).andReturn(new TestName(FOO_BAR));
        replay(utils, componentElement, jaxb, annotation);

        processor.setClassNames(componentElement, model, FOO_HTML_BAR);

        verify(utils, componentElement, jaxb, annotation);
        assertTrue(model.isGenerate());
        assertEquals(FOO_BAR, model.getBaseClass().toString());
        assertEquals(FOO_HTML_BAR, model.getTargetClass().toString());
    }

    @Test
    public void testSetFamily() throws Exception {
        replay(utils, componentElement, jaxb, annotation);

        processor.setComponeneFamily(componentElement, model, FOO_HTML_BAR);

        verify(utils, componentElement, jaxb, annotation);
        assertEquals(FOO_HTML_BAR, model.getFamily());
    }

    @Test
    public void testSetFamily1() throws Exception {
        expect(utils.getConstant(same(componentElement), eq(ComponentProcessor.COMPONENT_FAMILY))).andReturn(
            FOO_HTML_BAR);
        replay(utils, componentElement, jaxb, annotation);

        processor.setComponeneFamily(componentElement, model, "");

        verify(utils, componentElement, jaxb, annotation);
        assertEquals(FOO_HTML_BAR, model.getFamily());
    }

    @Test
    public void testSetFamily2() throws Exception {
        expect(utils.getConstant(same(componentElement), eq(ComponentProcessor.COMPONENT_FAMILY))).andReturn(null);
        replay(utils, componentElement, jaxb, annotation);

        processor.setComponeneFamily(componentElement, model, "");

        verify(utils, componentElement, jaxb, annotation);
        assertNull(model.getFamily());
    }

    @Override
    protected Iterable<String> sources() {
        return Collections.singleton(COMPONENT_CLASS_JAVA);
    }

}
