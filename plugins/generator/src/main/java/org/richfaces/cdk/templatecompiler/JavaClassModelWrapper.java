package org.richfaces.cdk.templatecompiler;

import org.richfaces.cdk.templatecompiler.builder.model.MethodBodyStatement;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class JavaClassModelWrapper extends BeansWrapper implements ObjectWrapper {

    private final JavaClassConfiguration configuration;

    public JavaClassModelWrapper(JavaClassConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public TemplateModel wrap(Object obj) throws TemplateModelException {

        // TODO wrap specified model classes.
        TemplateModel templateModel;

        if (obj instanceof MethodBodyStatement) {
            templateModel = new MethodBodyTemplateModel((MethodBodyStatement) obj, this);
        } else {
            templateModel = super.wrap(obj);
        }

        return templateModel;
    }

    /**
     * <p class="changed_added_4_0"></p>
     *
     * @return the configuration
     */
    public JavaClassConfiguration getConfiguration() {
        return configuration;
    }

}
