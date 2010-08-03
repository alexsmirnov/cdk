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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * <p class="changed_added_4_0"></p>
 * TODO: injection.
 * @author asmirnov@exadel.com
 */
public class JavaClassConfiguration extends Configuration implements FreeMarkerRenderer {
    private static final String TEMPLATES = "/META-INF/templates/java";

    public JavaClassConfiguration() {

        // TODO set proper template loader.
        setClassForTemplateLoading(this.getClass(), TEMPLATES);

        // TODO create an object wrapper for library model.
        setObjectWrapper(new JavaClassModelWrapper(this));

        // Add context variables
//        this.setSharedVariable("context", new BeanModel(context, new BeansWrapper()));
    }

    /* (non-Javadoc)
     * @see org.richfaces.cdk.templatecompiler.FreeMarkerRenderer#writeSnippet(java.lang.String, java.lang.Object, java.io.Writer)
     */
    public void writeSnippet(String templateName, Object object, Writer writer) throws IOException, TemplateException {
        Template t = getTemplate(templateName + ".ftl");
        Map<String, Object> rootMap = new HashMap<String, Object>();
        rootMap.put("modelItem", object);

        rootMap.put("facesContextVariable", RendererClassVisitor.FACES_CONTEXT_VARIABLE);
        rootMap.put("componentVariable", RendererClassVisitor.COMPONENT_VARIABLE);
        rootMap.put("responseWriterVariable", RendererClassVisitor.RESPONSE_WRITER_VARIABLE);
        rootMap.put("clientIdVariable", RendererClassVisitor.CLIENT_ID_VARIABLE);

        t.process(rootMap, writer);
    }

    /* (non-Javadoc)
    * @see org.richfaces.cdk.templatecompiler.FreeMarkerRenderer#renderSnippet(java.lang.String, java.lang.Object)
    */
    public String renderSnippet(String templateName, Object object) {
        StringWriter writer = new StringWriter();
        try {
            writeSnippet(templateName, object, writer);

            return writer.toString();
        } catch (IOException e) {
            // TODO: handle exception
            return e.getMessage();
        } catch (TemplateException e) {
            // TODO: handle exception
            return e.getMessage();
        }
    }

}
