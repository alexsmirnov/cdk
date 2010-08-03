/**
 * 
 */
package org.richfaces.cdk.templatecompiler;

import org.richfaces.cdk.attributes.Schema;
import org.richfaces.cdk.attributes.SchemaSet;
import org.richfaces.cdk.templatecompiler.model.Template;
import org.richfaces.cdk.xmlconfig.JAXB;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author asmirnov
 *
 */
public class XhtmlElSchemaProvider implements Provider<Schema> {
    
    private final JAXB jaxbBinding;

    @Inject
    public XhtmlElSchemaProvider(JAXB jaxbBinding) {
        this.jaxbBinding = jaxbBinding;
    }

    @Override
    public Schema get() {
        SchemaSet schemaSet = jaxbBinding.unmarshal("urn:attributes:xhtml-el.xml", null, SchemaSet.class);
        return  schemaSet.getSchemas().get(Template.XHTML_EL_NAMESPACE);
    }

}
