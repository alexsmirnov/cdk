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

package org.richfaces.cdk.apt;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import org.richfaces.cdk.model.ClassName;

import com.google.inject.ProvidedBy;

/**
 * <p class="changed_added_4_0">This class provides utility methods to analayze java classes. This implementation uses APT API to get
 * information about Java code.</p>
 * @author asmirnov@exadel.com
 *
 */
@ProvidedBy(SourceUtilsProvider.class)
public interface SourceUtils  {
    
    

    /**
     * <p class="changed_added_4_0"></p>
     * @author asmirnov@exadel.com
     *
     */
    public interface SuperTypeVisitor {
        public void visit(TypeMirror type);
    }


    /**
     * <p class="changed_added_4_0">
     * </p>
     *
     * @author asmirnov@exadel.com
     *
     */
    public interface BeanProperty {
    
        /**
         * <p class="changed_added_4_0">
         * </p>
         *
         * @return the name
         */
        public String getName();
    
        /**
         * <p class="changed_added_4_0">Get JavaDoc comment of appropriate bean property element.</p>
         * @return
         */
        public String getDocComment();
    
        public ClassName getType();
    
        /**
         * <p class="changed_added_4_0"></p>
         * @return the exists
         */
        public boolean isExists();

        public List<? extends AnnotationMirror> getAnnotationMirrors();

        public <T extends Annotation> T getAnnotation(Class<T> annotationType);
    }


    /**
     * <p class="changed_added_4_0">
     * Get all fields and bean properties that are annotated with given
     * annotation.
     * </p>
     *
     * @param annotation
     * @param type
     * @return
     */
    public Set<BeanProperty> getBeanPropertiesAnnotatedWith(Class<? extends Annotation> annotation,
        TypeElement type);

    /**
     * <p class="changed_added_4_0"></p>
     * @param type
     * @return
     */
    public Set<BeanProperty> getAbstractBeanProperties(TypeElement type);

    /**
     * <p class="changed_added_4_0">Get JavaDoc comments associated with given element.</p>
     * @param componentElement
     * @return
     */
    public String getDocComment(Element element);

    /**
     * <p class="changed_added_4_0"></p>
     * @param componentElement
     * @param name
     * @return
     */
    public Object getConstant(TypeElement element, String name);
    
    
    /**
     * <p class="changed_added_4_0"></p>
     * @param type
     * @param visitor
     */
    public void visitSupertypes(TypeElement type, SuperTypeVisitor visitor);

    /**
     * <p class="changed_added_4_0">Converts TypeMirror into corresponding TypeElement</p>
     * @param mirror
     * @return
     */
    public TypeElement asTypeElement(TypeMirror mirror);

    /**
     * <p class="changed_added_4_0"></p>
     * @param type
     * @return
     */
    public TypeElement asTypeElement(ClassName type);
    
}
