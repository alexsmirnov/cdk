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
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import org.richfaces.cdk.model.ClassName;


/**
 * <p class="changed_added_4_0"></p>
 * @author asmirnov@exadel.com
 *
 */
public class ReflectionUtils implements SourceUtils {

    @Override
    public TypeElement asTypeElement(TypeMirror mirror) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TypeElement asTypeElement(ClassName type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<BeanProperty> getAbstractBeanProperties(TypeElement type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<BeanProperty> getBeanPropertiesAnnotatedWith(Class<? extends Annotation> annotation, TypeElement type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getConstant(TypeElement element, String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDocComment(Element element) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void visitSupertypes(TypeElement type, SuperTypeVisitor visitor) {
        // TODO Auto-generated method stub
        
    }


}
