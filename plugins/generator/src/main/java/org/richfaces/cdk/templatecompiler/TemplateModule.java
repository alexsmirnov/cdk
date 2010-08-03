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

package org.richfaces.cdk.templatecompiler;

import org.richfaces.cdk.CdkWriter;
import org.richfaces.cdk.ModelBuilder;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;

/**
 * <p class="changed_added_4_0"></p>
 * @author asmirnov@exadel.com
 *
 */
public class TemplateModule extends AbstractModule {

    /* (non-Javadoc)
     * @see com.google.inject.AbstractModule#configure()
     */
    @Override
    protected void configure() {
        Multibinder<ModelBuilder> modelBinder = Multibinder.newSetBinder(binder(), ModelBuilder.class);
        modelBinder.addBinding().to(RendererTemplateParser.class);
        Multibinder.newSetBinder(binder(), CdkWriter.class).addBinding().to(RendererClassGenerator.class);
        bind(new TypeLiteral<TemplateVisitorFactory<RendererClassVisitor>>(){}).to(VisitorFactoryImpl.class);
        bind(FreeMarkerRenderer.class).to(JavaClassConfiguration.class);
    }

}
