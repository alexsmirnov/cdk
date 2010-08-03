package org.richfaces.cdk.apt;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.richfaces.cdk.Logger;
import org.richfaces.cdk.model.ClassName;
import org.richfaces.cdk.model.InvalidNameException;
import org.richfaces.cdk.util.PropertyUtils;

import com.google.inject.Inject;

public class AptSourceUtils implements SourceUtils {
    private static final Set<String> PROPERTIES =
        new HashSet<String>(Arrays.asList("getEventNames", "getDefaultEventName", "getClientBehaviors", "getFamily"));

    private final ProcessingEnvironment processingEnv;

    @Inject
    private Logger log;

    /**
     * <p class="changed_added_4_0">
     * </p>
     * 
     * @param processingEnv
     */
    public AptSourceUtils(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    /**
     * <p class="changed_added_4_0">
     * Get all fields and bean properties that are annotated with given annotation.
     * </p>
     * 
     * @param annotation
     * @param type
     * @return
     */
    public Set<BeanProperty> getBeanPropertiesAnnotatedWith(Class<? extends Annotation> annotation, TypeElement type) {
        Set<BeanProperty> properties = new HashSet<BeanProperty>();
        List<? extends Element> members = this.processingEnv.getElementUtils().getAllMembers(type);

        // Get all methods and fields annotated by annotation.
        for (Element childElement : members) {
            boolean annotated = null != childElement.getAnnotation(annotation);
            if (!annotated) {
                continue;
            }

            // Have an annotation, infer property name.
            if (ElementKind.METHOD.equals(childElement.getKind())) {
                processMethod(properties, childElement, annotated);
            } else if (ElementKind.FIELD.equals(childElement.getKind())) {
                processFiled(properties, childElement);
            }

            // TODO - merge properties with same name ?
        }

        return properties;
    }

    public Set<BeanProperty> getAbstractBeanProperties(TypeElement type) {
        log.debug("AptSourceUtils.getAbstractBeanProperties");
        log.debug("  - type = " + type);

        Set<BeanProperty> properties = new HashSet<BeanProperty>();
        List<? extends Element> members = this.processingEnv.getElementUtils().getAllMembers(type);

        Map<String, List<ExecutableElement>> props = groupMethodsBySignature(members);
        removeNotAbstractGroups(props);

        for (List<ExecutableElement> methods : props.values()) {
            ExecutableElement method = methods.get(0);

            if (ElementKind.METHOD.equals(method.getKind()) && !PROPERTIES.contains(method.getSimpleName().toString())) {
                processMethod(properties, method, false);
            }

            // TODO - merge properties with same name ?
        }

        return properties;
    }

    private void removeNotAbstractGroups(Map<String, List<ExecutableElement>> props) {
        List<String> removeKeys = new ArrayList<String>();
        for (Map.Entry<String, List<ExecutableElement>> entry : props.entrySet()) {
            List<ExecutableElement> value = entry.getValue();
            for (ExecutableElement element : value) {
                if (!isAbstract(element)) {
                    removeKeys.add(entry.getKey());
                }
            }
        }

        for (String removeKey : removeKeys) {
            props.remove(removeKey);
        }
    }

    private Map<String, List<ExecutableElement>> groupMethodsBySignature(List<? extends Element> members) {
        Map<String, List<ExecutableElement>> props = new HashMap<String, List<ExecutableElement>>();
        for (Element element : members) {
            if (ElementKind.METHOD.equals(element.getKind())
                && !PROPERTIES.contains(element.getSimpleName().toString())) {

                ExecutableElement method = (ExecutableElement) element;

                String signature = getSignature(method);

                List<ExecutableElement> methods = props.get(signature);
                if (methods == null) {
                    methods = new ArrayList<ExecutableElement>(5);
                    props.put(signature, methods);
                }

                methods.add(method);
            }
        }
        return props;
    }

    private String getSignature(ExecutableElement method) {
        String name = method.getSimpleName().toString();
        List<? extends VariableElement> methodParams = method.getParameters();
        StringBuilder builder = new StringBuilder(name);
        for (VariableElement methodParam : methodParams) {
            builder.append(":").append(methodParam.getKind().name());
        }
        return builder.toString();
    }

    private void processFiled(Set<BeanProperty> properties, Element childElement) {
        AptBeanProperty property = new AptBeanProperty(childElement.getSimpleName().toString());

        property.type = asClassDescription(childElement.asType());
        property.element = childElement;

        // TODO - find getter/setter, check them for abstract.
        property.exists = true;

        properties.add(property);
    }

    private void processMethod(Set<BeanProperty> properties, Element childElement, boolean annotated) {
        ExecutableElement method = (ExecutableElement) childElement;
        boolean exists = !isAbstract(method);
        if (!annotated && exists) {
            log.debug("      - " + childElement.getSimpleName() + " : didn't annotated and didn't abstract.");
            return;
        }

        TypeMirror propertyType = method.getReturnType();
        List<? extends VariableElement> parameters = method.getParameters();
        if (TypeKind.VOID.equals(propertyType.getKind()) && 1 == parameters.size()) {

            // That is setter method, get type from parameter.
            propertyType = parameters.get(0).asType();
        } else if (!parameters.isEmpty()) {
            // TODO Invalid method signature for a bean property,
            // throw exception ?
            log.debug("      - " + childElement.getSimpleName() + " : Invalid method signature for a bean property.");
            return;
        }

        try {
            String name = PropertyUtils.methodToName(childElement.getSimpleName().toString());
            AptBeanProperty property = new AptBeanProperty(name);

            property.type = asClassDescription(propertyType);
            property.element = childElement;
            property.exists = exists;

            properties.add(property);
            log.debug("      - " + childElement.getSimpleName() + " : was added.");

        } catch (InvalidNameException e) {
            log.debug("      - " + childElement.getSimpleName() + " : Invalid method name for a bean property, throw.");

            // TODO Invalid method name for a bean property, throw
            // exception ?
        }
    }

    private boolean isAbstract(ExecutableElement method) {
        return method.getModifiers().contains(Modifier.ABSTRACT);
    }

    private ClassName asClassDescription(TypeMirror type) {
        return new ClassName(type.toString());
    }

    public String getDocComment(Element componentElement) {
        return this.processingEnv.getElementUtils().getDocComment(componentElement);
    }

    public Object getConstant(TypeElement componentElement, String name) {
        List<VariableElement> fieldsIn =
            ElementFilter.fieldsIn(this.processingEnv.getElementUtils().getAllMembers(componentElement));
        Object value = null;

        for (VariableElement field : fieldsIn) {
            Set<Modifier> modifiers = field.getModifiers();

            if (modifiers.contains(Modifier.FINAL) && modifiers.contains(Modifier.STATIC)
                && field.getSimpleName().toString().equals(name)) {
                value = field.getConstantValue();
            }
        }
        return value;
    }

    public void visitSupertypes(TypeElement type, SuperTypeVisitor visitor) {
        visitSupertypes(type.asType(), visitor);
    }

    private void visitSupertypes(TypeMirror type, SuperTypeVisitor visitor) {
        List<? extends TypeMirror> supertypes = this.processingEnv.getTypeUtils().directSupertypes(type);
        for (TypeMirror typeMirror : supertypes) {
            visitSupertypes(typeMirror, visitor);
        }
        visitor.visit(type);
    }

    @Override
    public TypeElement asTypeElement(ClassName type) {
        return processingEnv.getElementUtils().getTypeElement(type.toString());
    }

    @Override
    public TypeElement asTypeElement(TypeMirror mirror) {
        if (TypeKind.DECLARED.equals(mirror.getKind())) {
            return (TypeElement) processingEnv.getTypeUtils().asElement(mirror);
        } else {
            return null;
        }
    }

    /**
     * <p class="changed_added_4_0">
     * </p>
     * 
     * @author asmirnov@exadel.com
     * 
     */
    protected final class AptBeanProperty implements BeanProperty {
        private Element element;
        private boolean exists;
        private final String name;
        private ClassName type;

        /**
         * <p class="changed_added_4_0">
         * </p>
         * 
         * @param name
         */
        public AptBeanProperty(String name) {
            this.name = name;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;

            result = prime * result + ((name == null) ? 0 : name.hashCode());

            return result;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            AptBeanProperty other = (AptBeanProperty) obj;

            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }

            return true;
        }

        /**
         * <p class="changed_added_4_0">
         * </p>
         * 
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * <p class="changed_added_4_0">
         * Get JavaDoc comment of appropriate bean property element.
         * </p>
         * 
         * @return
         */
        public String getDocComment() {
            return processingEnv.getElementUtils().getDocComment(element);
        }

        public ClassName getType() {
            return type;
        }

        /**
         * <p class="changed_added_4_0">
         * </p>
         * 
         * @return the exists
         */
        public boolean isExists() {
            return exists;
        }

        public List<? extends AnnotationMirror> getAnnotationMirrors() {
            return element.getAnnotationMirrors();
        }

        public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
            return element.getAnnotation(annotationType);
        }
    }

}
