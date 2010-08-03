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

import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.richfaces.cdk.CdkTestRunner;
import org.richfaces.cdk.Mock;
import org.richfaces.cdk.MockController;
import org.richfaces.cdk.annotations.Description;
import org.richfaces.cdk.model.DescriptionGroup;

import com.google.inject.Inject;

/**
 * <p class="changed_added_4_0">
 * </p>
 * 
 * @author asmirnov@exadel.com
 * 
 */
@RunWith(CdkTestRunner.class)
public class DescriptionProcessorTest extends AnnotationProcessorTest {

    private static final String FOO_BAR_ELEMENT = "Foo bar element";

    private static final String FOO_FACET = "fooFacet";

    @Mock
    private DescriptionGroup bean;

    @Mock
    private Description description;

    @Inject
    private DescriptionProcessorImpl descriptionProcessor;

    @Inject
    private MockController mockController;

    /**
     * Test method for
     * {@link org.richfaces.cdk.apt.processors.DescriptionProcessorImpl#processDescription(org.richfaces.cdk.model.DescriptionGroup, org.richfaces.cdk.annotations.Description, java.lang.String)}
     * .
     */
    @Test
    public void testProcessDescription() {
        bean.setDescription(FOO_BAR_ELEMENT);
        expectLastCall();
        bean.setDisplayname(FOO_FACET);
        expectLastCall();
        expect(this.description.smallIcon()).andReturn("");
        expect(this.description.largeIcon()).andReturn("");
        expect(this.description.displayName()).andReturn(FOO_FACET).times(2);
        expect(this.description.value()).andReturn("");
        mockController.replay();
        descriptionProcessor.processDescription(bean, description, FOO_BAR_ELEMENT);
        mockController.verify();
    }

    @Override
    protected Iterable<String> sources() {
        return Collections.emptySet();
    }

}
