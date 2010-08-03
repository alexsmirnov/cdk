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

package org.richfaces.cdk.generate.freemarker;

import org.richfaces.cdk.Logger;

import com.google.inject.Inject;

import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;

/**
 * <p class="changed_added_4_0"></p>
 *
 * @author asmirnov@exadel.com
 */
public class CdkConfiguration extends Configuration {
    
    private static final String TEMPLATES = "/META-INF/templates";
    
    private Logger log;

    @Inject
    public CdkConfiguration(ObjectWrapper wrapper, Logger log, FreeMakerUtils utils) {
        super();
        this.log = log;

        // load templates from plugin classloader.
        setClassForTemplateLoading(this.getClass(), TEMPLATES);
        setTemplateUpdateDelay(10000);// Forever...
        setSharedVariable("utils", utils);
        setObjectWrapper(wrapper);

        // Add context variables
//        this.setSharedVariable("context", new BeanModel(context, new BeansWrapper()));
    }
}
