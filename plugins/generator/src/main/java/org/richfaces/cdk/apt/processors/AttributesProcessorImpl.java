package org.richfaces.cdk.apt.processors;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;

import org.richfaces.cdk.CdkException;
import org.richfaces.cdk.Logger;
import org.richfaces.cdk.annotations.Attribute;
import org.richfaces.cdk.annotations.EventName;
import org.richfaces.cdk.annotations.Signature;
import org.richfaces.cdk.apt.SourceUtils;
import org.richfaces.cdk.apt.SourceUtils.BeanProperty;
import org.richfaces.cdk.apt.SourceUtils.SuperTypeVisitor;
import org.richfaces.cdk.model.BeanModelBase;
import org.richfaces.cdk.model.ClassName;
import org.richfaces.cdk.model.PropertyBase;
import org.richfaces.cdk.util.Strings;
import org.richfaces.cdk.xmlconfig.CdkEntityResolver;
import org.richfaces.cdk.xmlconfig.FragmentParser;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class AttributesProcessorImpl implements AttributesProcessor {

    private static final String SIGNATURE_NONE_CLASS_NAME = Signature.NONE.class.getName().replace('$', '.');

    private static final String STRING_NAME = String.class.getName();
    
    @Inject
    private Logger log;

    private final DescriptionProcessor descriptionProcessor;

    private final Provider<SourceUtils> utilsProvider;

    private final FragmentParser parser;

    /**
     * <p class="changed_added_4_0"></p>
     * @param descriptionProcessor
     * @param utilsProvider
     * @param parser
     */
    @Inject
    public AttributesProcessorImpl(DescriptionProcessor descriptionProcessor, Provider<SourceUtils> utilsProvider, FragmentParser parser) {
        this.descriptionProcessor = descriptionProcessor;
        this.utilsProvider = utilsProvider;
        this.parser = parser;
    }

    protected void processAttribute(SourceUtils.BeanProperty beanProperty, PropertyBase attribute) {

        attribute.setType(beanProperty.getType());

        Attribute attributeAnnotarion = beanProperty.getAnnotation(Attribute.class);
        if (attributeAnnotarion == null) {
            attribute.setGenerate(!beanProperty.isExists());
            attribute.setDescription(beanProperty.getDocComment());
            attribute.setHidden(true);

            if (attribute.getType().isPrimitive()) {
                String value = getPimitiveDefaultValue(attribute.getType().getName());
                if (value != null) {
                    attribute.setDefaultValue(value);
                }
            }
        } else {
            attribute.setHidden(attributeAnnotarion.hidden());
            attribute.setLiteral(attributeAnnotarion.literal());
            attribute.setPassThrough(attributeAnnotarion.passThrough());
            attribute.setRequired(attributeAnnotarion.required());
            attribute.setReadOnly(attributeAnnotarion.readOnly());
            attribute.setGenerate(attributeAnnotarion.generate() || !beanProperty.isExists());

            descriptionProcessor.processDescription(attribute, attributeAnnotarion.description(), beanProperty.getDocComment());

            setDefaultValue(attribute, attributeAnnotarion);

            String suggestedValue = attributeAnnotarion.suggestedValue();
            if (!Strings.isEmpty(suggestedValue)) {
                attribute.setSuggestedValue(suggestedValue);
            }

            // MethodExpression call signature.
            AnnotationValue signatureMirror = null;
            List<? extends AnnotationMirror> mirrors = beanProperty.getAnnotationMirrors();
            for (AnnotationMirror mirror : mirrors) {
                if (Attribute.class.getName().equals(mirror.getAnnotationType().toString())) {
                    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
                        Name simpleName = entry.getKey().getSimpleName();
                        if ("signature".equals(simpleName.toString())) {
                            signatureMirror = entry.getValue();
                        }
                    }

                }
            }
            attribute.setSignature(getSignature(attributeAnnotarion.signature(), signatureMirror));

            for (EventName event : attributeAnnotarion.events()) {
                setBehaviorEvent(attribute, event);
            }
        }
    }

    private void setDefaultValue(PropertyBase attribute, Attribute attributeAnnotarion) {
        String defaultValue = attributeAnnotarion.defaultValue();
        if (Strings.isEmpty(defaultValue)) {
            if (attribute.getType().isPrimitive()) {
                String pimitiveDefaultValue = getPimitiveDefaultValue(attribute.getType().getName());
                if (pimitiveDefaultValue != null) {
                    attribute.setDefaultValue(pimitiveDefaultValue);
                }
            }
        } else {
            if (STRING_NAME.equals(attribute.getType().toString())) {
                defaultValue = "\"" + defaultValue + "\"";
            }
            attribute.setDefaultValue(defaultValue);
        }
    }

    private String getPimitiveDefaultValue(String typeName) {
        if (isInstace(boolean.class, typeName)) {
            return "false";
        } else if (isInstace(int.class, typeName)) {
            return "Integer.MIN_VALUE";
        } else if (isInstace(long.class, typeName)) {
            return "Long.MIN_VALUE";
        } else if (isInstace(byte.class, typeName)) {
            return "Byte.MIN_VALUE";
        } else if (isInstace(short.class, typeName)) {
            return "Short.MIN_VALUE";
        } else if (isInstace(float.class, typeName)) {
            return "Float.MIN_VALUE";
        } else if (isInstace(double.class, typeName)) {
            return "Double.MIN_VALUE";
        } else if (isInstace(char.class, typeName)) {
            return "Character.MIN_VALUE";
        }

        return null;
    }

    private boolean isInstace(Class<?> byteClass, String typeName) {
        return byteClass.getSimpleName().equals(typeName);
    }

    private List<ClassName> getSignature(Signature signature, AnnotationValue signatureMirror) {
        if (signature == null) {
            return null;
        }

        String returnType;
        try {
            returnType = signature.returnType().getName();
        } catch (MirroredTypeException e) {
            TypeMirror returnTypeMirror = e.getTypeMirror();
            returnType = returnTypeMirror.toString();
        }

        if (signature != null && SIGNATURE_NONE_CLASS_NAME.equals(returnType)) {
            return getSignatureParams(signature, signatureMirror);
            // signature parameters always should be replaced.
            // TODO - set method return type.

        }

        return null;
    }

    private List<ClassName> getSignatureParams(Signature signature, AnnotationValue signatureMirror) {
        List<ClassName> parameters = Lists.newArrayList();

        try {
            for (Class<?> parameterType : signature.parameters()) {
                parameters.add(new ClassName(parameterType.getName()));
            }
        } catch (MirroredTypeException e) {
            AnnotationValue params = signatureMirror.accept(new EmptyAnnotationValueVisitor<AnnotationValue>() {
                @Override
                public AnnotationValue visitAnnotation(AnnotationMirror a, Object o) {
                    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : a.getElementValues().entrySet()) {
                        if ("parameters".equals(entry.getKey().getSimpleName().toString())) {
                            return entry.getValue();
                        }
                    }
                    return null;
                }
            }, null);

            List<? extends AnnotationValue> o = params.accept(new EmptyAnnotationValueVisitor<List<? extends AnnotationValue>>(){
                @Override
                public List<? extends AnnotationValue> visitArray(List<? extends AnnotationValue> vals, Object o) {
                    return vals;
                }
            }, null);

            for (AnnotationValue annotationValue : o) {
                parameters.add(annotationValue.accept(new EmptyAnnotationValueVisitor<ClassName>() {
                    @Override
                    public ClassName visitType(TypeMirror t, Object o) {
                        return new ClassName(t.toString());
                    }
                }, null));
            }
//            TypeMirror parameterType = e.getTypeMirror();
//            parameters.add(new ClassName(parameterType.toString()));
        } catch (MirroredTypesException e) {
            for (TypeMirror parameterType : e.getTypeMirrors()) {
                parameters.add(new ClassName(parameterType.toString()));
            }
        }

        return parameters;
        // signature parameters always should be replaced.
        // TODO - set method return type.
    }
    
    private void setBehaviorEvent(PropertyBase attribute, EventName eventName) {
        if (null != eventName) {
            org.richfaces.cdk.model.EventName event = new org.richfaces.cdk.model.EventName();

            event.setName(eventName.value());
            event.setDefaultEvent(eventName.defaultEvent());
            attribute.getEventNames().add(event);
        }
    }
    
    @Override
    public void processType(final BeanModelBase component, TypeElement element) throws CdkException {
        log.debug("AttributesProcessorImpl.processType");
        log.debug("  -> component = " + component);
        log.debug("  -> typeElement = " + element);

        log.debug("  -- Process XML files with standard attributes definitions.");
        log.debug("     -> sourceUtils.visitSupertypes...");
        SourceUtils sourceUtils = getSourceUtils();
        sourceUtils.visitSupertypes(element, new SuperTypeVisitor() {
            
            @Override
            public void visit(TypeMirror type) {
                try {
                    log.debug("        -> visit - " + type.toString());
                    component.getAttributes().addAll(
                            parseProperties(type.toString() + ".xml"));
                } catch (CdkException e) {
                    // TODO - log errors ?
                }
            }

        });


        log.debug("  -- Process Java files.");
        Set<BeanProperty> properties = Sets.newHashSet();
        properties.addAll(sourceUtils.getBeanPropertiesAnnotatedWith(Attribute.class, element));
        properties.addAll(sourceUtils.getAbstractBeanProperties(element));
        // TODO - encapsulate attribute builder into utility class.
        for (BeanProperty beanProperty : properties) {
            processAttribute(beanProperty, component.getOrCreateAttribute(beanProperty.getName()));
        }
    }

    private Collection<? extends PropertyBase> parseProperties(String uri) {
        return parser.parseProperties(CdkEntityResolver.URN_ATTRIBUTES + uri);
    }

    private SourceUtils getSourceUtils() {
        return utilsProvider.get();
    }


    @Override
    public void processXmlFragment(BeanModelBase component, String ...attributesConfig) {
        // Process all files from @Jsf.. attributes property.
        for (String attributes : attributesConfig) {
            try {
                component.getAttributes().addAll(parseProperties(attributes));
            } catch (CdkException e) {
                // TODO - log errors ?
            }
        }
    }

}
