package org.richfaces.cdk.templatecompiler;

import java.io.IOException;
import java.io.Writer;

import freemarker.template.TemplateException;

public interface FreeMarkerRenderer {

    public void writeSnippet(String templateName, Object object, Writer writer)
        throws IOException, TemplateException;

    public String renderSnippet(String templateName, Object object);

}