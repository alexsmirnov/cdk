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

import java.io.File;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.richfaces.cdk.CdkException;
import org.richfaces.cdk.FileManager;
import org.richfaces.cdk.Logger;
import org.richfaces.cdk.ModelBuilder;
import org.richfaces.cdk.NamingConventions;
import org.richfaces.cdk.RichFacesConventions;
import org.richfaces.cdk.Source;
import org.richfaces.cdk.Sources;
import org.richfaces.cdk.model.ClassName;
import org.richfaces.cdk.model.ComponentLibrary;
import org.richfaces.cdk.model.EventName;
import org.richfaces.cdk.model.PropertyBase;
import org.richfaces.cdk.model.PropertyModel;
import org.richfaces.cdk.model.RenderKitModel;
import org.richfaces.cdk.model.RendererModel;
import org.richfaces.cdk.model.SimpleVisitor;
import org.richfaces.cdk.templatecompiler.model.Attribute;
import org.richfaces.cdk.templatecompiler.model.ClientBehavior;
import org.richfaces.cdk.templatecompiler.model.CompositeInterface;
import org.richfaces.cdk.templatecompiler.model.ImportAttributes;
import org.richfaces.cdk.templatecompiler.model.Template;
import org.richfaces.cdk.util.Strings;
import org.richfaces.cdk.xmlconfig.FragmentParser;
import org.richfaces.cdk.xmlconfig.JAXB;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

/**
 * <p class="changed_added_4_0">
 * </p>
 * 
 * @author asmirnov@exadel.com
 */
public class RendererTemplateParser implements ModelBuilder {

    private static final Pattern PARAMETERS_STRING_PATTERN = Pattern.compile("\\( ( [^\\)]* ) \\) \\s*$",
        Pattern.COMMENTS);

    private static final Pattern COMMA_SEPARATED_PATTERN = Pattern.compile("\\s*,\\s*", Pattern.COMMENTS);

    // todo cahche
    private static final Map<String, Template> PROCESSED_TEMPLATES = new HashMap<String, Template>();

    private ComponentLibrary library;
    
    private JAXB jaxbBinding;
    
    private Logger log;
    
    private FileManager sources;
    
    private FragmentParser fragmentParser;

//    @Inject
    private NamingConventions namingConventions;

    /**
     * <p class="changed_added_4_0"></p>
     * @param library
     * @param jaxbBinding
     * @param log
     * @param sources
     */
    @Inject
    public RendererTemplateParser(ComponentLibrary library, JAXB jaxbBinding, Logger log,
                                  @Source(Sources.RENDERER_TEMPLATES) FileManager sources,
                                  FragmentParser fragmentParser) {
        this.library = library;
        this.jaxbBinding = jaxbBinding;
        this.log = log;
        this.sources = sources;
        this.fragmentParser = fragmentParser;
    }

    private Set<EventName> convert(Collection<ClientBehavior> clientBehaviors) {
        if (clientBehaviors == null || clientBehaviors.isEmpty()) {
            return null;
        }

        Set<EventName> result = Sets.newLinkedHashSet();
        for (ClientBehavior clientBehavior : clientBehaviors) {
            EventName eventName = new EventName();
            eventName.setName(clientBehavior.getEvent());
            eventName.setDefaultEvent(clientBehavior.isDefaultEvent());
            result.add(eventName);
        }

        return result;
    }

    private List<ClassName> parseSignature(String signatureString) {
        if (signatureString == null || signatureString.trim().length() == 0) {
            return null;
        }

        List<ClassName> result = Lists.newArrayList();
        Matcher parametersStringMatcher = PARAMETERS_STRING_PATTERN.matcher(signatureString);
        if (!parametersStringMatcher.find()) {
            // TODO - handle exception
            throw new IllegalArgumentException(MessageFormat.format("Signature string {0} cannot be parsed!",
                signatureString));
        }

        String parametersString = parametersStringMatcher.group(1).trim();
        if (parametersString.length() != 0) {
            String[] parameters = COMMA_SEPARATED_PATTERN.split(parametersString);
            for (String parameter : parameters) {
                String trimmedParameter = parameter.trim();
                result.add(new ClassName(trimmedParameter));
            }
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.richfaces.cdk.ModelBuilder#build()
     */
    @Override
    public void build() throws CdkException {
        Iterable<File> sourceFiles = this.sources.getFiles();
        if (null != sourceFiles) {
            for (File file : sourceFiles) {
                build(file, null);
            }
        }
    }

    public void build(File file, RendererModel rendererModel) {
        log.debug("RendererTemplateParser.build");
        final String absolutePath = file.getAbsolutePath();
        log.debug("  - file = " + absolutePath);
        log.debug("  - renderer = " + rendererModel);

        RendererModel existedModel = library.accept(new SimpleVisitor<RendererModel,String>() {
            @Override
            public RendererModel visitRender(RendererModel model,String absolutePath) {
                Template template = model.getTemplate();
                if(null != template && absolutePath.equals(template.getTemplatePath())){
                    return model;
                } else {
                    return null;
                }
            }
        },absolutePath);
        if (null != existedModel) {
            log.debug("  - Template was already processed.");
            return;
        }

        Template template = parseTemplate(file);

        template.setTemplatePath(absolutePath);

        mergeTemplateIntoModel(template, rendererModel);
    }

    protected void mergeTemplateIntoModel(Template template, RendererModel renderer) throws CdkException {
        CompositeInterface compositeInterface = template.getInterface();

        if (renderer == null) {
            renderer = new RendererModel();
            RenderKitModel renderKit = library.addRenderKit(compositeInterface.getRenderKitId());
            renderKit.getRenderers().add(renderer);
        }
        renderer.setTemplate(template);
        setRendererType(template, compositeInterface, renderer);
        setFamily(compositeInterface, renderer); //TODO set default values according to template name
        setRendererClass(compositeInterface, renderer);
        setRendererBaseClass(compositeInterface, renderer);


        Boolean rendersChildren = compositeInterface.getRendersChildren();
        if (rendersChildren != null) {
            renderer.setRendersChildren(rendersChildren);
        }
        
        List<ImportAttributes> attributesImports = compositeInterface.getAttributesImports();
        if (attributesImports != null) {
            for (ImportAttributes attributesImport : attributesImports) {
                String importURI = attributesImport.getSource();
                Collection<PropertyBase> properties = fragmentParser.parseProperties(importURI);
                if (properties != null) {
                    renderer.getAttributes().addAll(properties);
                }
            }
        }
        
        List<Attribute> templateAttributes = compositeInterface.getAttributes();
        if (templateAttributes != null) {
            for (Attribute templateAttribute : templateAttributes) {
                renderer.getAttributes().add(buildProperty(templateAttribute));
            }
        }
    }

    private PropertyModel buildProperty(Attribute templateAttribute) {
        PropertyModel rendererProperty = new PropertyModel();
        rendererProperty.setName(templateAttribute.getName());
        rendererProperty.setDefaultValue(templateAttribute.getDefaultValue());

        // TODO is it the right one?
        rendererProperty.setDescription(templateAttribute.getShortDescription());
        rendererProperty.setDisplayname(templateAttribute.getDisplayName());

        Set<EventName> eventNamesSet = convert(templateAttribute.getClientBehaviors());
        if (eventNamesSet != null) {
            rendererProperty.getEventNames().addAll(eventNamesSet);
        }

        // rendererProperty.setAliases(aliases)
        // rendererProperty.setExtension(extension)
        // rendererProperty.setGenerate(exists)
        // rendererProperty.setHidden(hidden)
        // rendererProperty.setIcon(icon)
        // rendererProperty.setLiteral(literal)
        // rendererProperty.setPassThrough(passThrough)
        // rendererProperty.setReadOnly(readOnly)
        // rendererProperty.setSuggestedValue(suggestedValue)

        rendererProperty.setRequired(templateAttribute.isRequired());

        List<ClassName> parsedSignature = parseSignature(templateAttribute.getMethodSignature());
        if (parsedSignature != null) {
            rendererProperty.getSignature().addAll(parsedSignature);
        }

        String templateAttributeType = templateAttribute.getType();
        if (templateAttributeType != null) {
            rendererProperty.setType(new ClassName(templateAttributeType));
        }
        return rendererProperty;
    }

    private void setRendererClass(CompositeInterface compositeInterface, RendererModel renderer) {
        String javaClass = compositeInterface.getJavaClass();
        if (javaClass == null) {
            javaClass = getNamingConventions().inferRendererName(renderer.getType());
        }
        renderer.setRendererClass(new ClassName(javaClass));
    }

    private void setRendererBaseClass(CompositeInterface compositeInterface, RendererModel renderer) {
        String baseClass = compositeInterface.getBaseClass();
        if (baseClass == null) {
            baseClass = getNamingConventions().inferRendererBaseName(renderer.getType());
        }
        renderer.setBaseClass(new ClassName(baseClass));
    }

    private void setFamily(CompositeInterface compositeInterface, RendererModel renderer) {
        String family = compositeInterface.getComponentFamily();
        if (family == null) {
            family = getNamingConventions().inferComponentFamily(renderer.getType());
        }
        renderer.setFamily(family);
    }

    private void setRendererType(Template template, CompositeInterface compositeInterface, RendererModel renderer) {
        String type = compositeInterface.getRendererType();
        if (Strings.isEmpty(type)) {
            type = getNamingConventions().inferRendererTypeByTemplatePath(template.getTemplatePath());
        }
        renderer.setType(new RendererModel.Type(type));
    }

    protected Template parseTemplate(File file) throws CdkException {
        return jaxbBinding.unmarshal(file, "http://richfaces.org/cdk/cdk-template.xsd", Template.class);
    }

    public NamingConventions getNamingConventions() {
        if (namingConventions == null) {
            namingConventions = new RichFacesConventions();
        }
        return namingConventions;
    }

    public void setNamingConventions(NamingConventions namingConventions) {
        this.namingConventions = namingConventions;
    }
}
