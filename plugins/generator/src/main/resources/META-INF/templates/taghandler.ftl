<#include "_copyright.ftl">

package ${tag.targetClass.package};

import javax.faces.view.facelets.*;
import org.richfaces.MethodMetadata;
import ${model.targetClass};
<#list model.generatedAttributes as prop><#if (!prop.signature.empty)><#list prop.signature as class>import ${class.name};
</#list></#if></#list>

public class ${tag.targetClass.simpleName} extends ${tag.baseClass.simpleName} {

    private static final ${tag.targetClass.simpleName}MetaRule META_RULE = new ${tag.targetClass.simpleName}MetaRule();


    public ${tag.targetClass.simpleName}(${model}Config config) {
        super(config);
        <#list model.requiredAttributes as prop>getRequiredAttribute("${prop.name}");
        </#list>

    }

    protected MetaRuleset createMetaRuleset(Class type) {
        MetaRuleset m = super.createMetaRuleset(type);
        m.addRule(META_RULE);
        return m;
    }

    static class ${tag.targetClass.simpleName}MetaRule extends MetaRule{

        public Metadata applyRule(String name, TagAttribute attribute, MetadataTarget meta) {
            if (meta.isTargetInstanceOf(${model.targetClass.simpleName}.class)) {
            <#list model.tagAttributes as prop><#if (prop.binding || prop.bindingAttribute)>
                if ("${prop.name}".equals(name)) {
                    return new MethodMetadata(attribute<#if (!prop.signature.empty)><#list prop.signature as class>, ${class.simpleName}.class</#list></#if>) {
                        public void applyMetadata(FaceletContext ctx, Object instance) {
                            ((${model.targetClass.simpleName}) instance).${prop.setterName}(<#if (prop.isBinging)>getMethodBinding(ctx));<#else>getMethodExpression(ctx));</#if>
                        }
                    };
                }
            </#if></#list>
            }
            return null;
        }
    }
}
