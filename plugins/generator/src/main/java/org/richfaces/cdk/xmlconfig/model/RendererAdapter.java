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

package org.richfaces.cdk.xmlconfig.model;

import org.richfaces.cdk.model.ClassName;
import org.richfaces.cdk.model.RendererModel;
import org.richfaces.cdk.model.RendererModel.Type;
import org.richfaces.cdk.util.Strings;

/**
 * <p class="changed_added_4_0">
 * </p>
 * 
 * @author asmirnov@exadel.com
 */
public class RendererAdapter extends AdapterBase<RendererBean, RendererModel> {

    @Override
    protected Class<? extends RendererBean> getBeanClass(RendererModel model) {
        return RendererBean.class;
    }

    @Override
    protected Class<? extends RendererModel> getModelClass(RendererBean bean) {
        return RendererModel.class;
    }

    @Override
    protected void postMarshal(RendererModel model, RendererBean bean) {
        ClassName baseClass = model.getBaseClass();
        ClassName rendererClass = model.getRendererClass();
        if (!model.isGenerate() && !isEmpty(baseClass) && isEmpty(rendererClass)) {
            bean.setRendererClass(baseClass);
        }
    }

    private static boolean isEmpty(ClassName className) {
        return className == null || Strings.isEmpty(className.getName());
    }

    @Override
    protected void postUnmarshal(RendererBean bean, RendererModel model) {
        // Copy type.
        String type = bean.getType();
        if (null != type) {
            model.setType(new Type(type.trim()));
        }
    }
}
