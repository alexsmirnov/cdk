/*
 * JBoss, Home of Professional Open Source
 * Copyright , Red Hat, Inc. and individual contributors
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

package org.richfaces.cdk.generate.java.taghandler;

import org.richfaces.cdk.CdkWriter;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * @author akolonitsky
 * @since Feb 22, 2010
 */
public class TagHandlerModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<CdkWriter> multibinder = Multibinder.newSetBinder(binder(), CdkWriter.class);
        multibinder
                .addBinding().to(TagHandlerWriter.class);
        multibinder
                .addBinding().to(ListenerTagHandlerGenerator.class);
    }
}
