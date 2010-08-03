/**
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

package org.richfaces.cdk.templatecompiler.builder.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Maksim Kaszynski
 */
public class JavaLanguageElement {
    private Set<JavaModifier> modifiers = new TreeSet<JavaModifier>();
    private List<JavaComment> comments = new ArrayList<JavaComment>();
    private List<JavaAnnotation> annotations = new ArrayList<JavaAnnotation>();
    private String name;

    public JavaLanguageElement() {
        super();
    }

    public JavaLanguageElement(String name) {
        super();
        this.name = name;
    }

    public Set<JavaModifier> getModifiers() {
        return modifiers;
    }

    public List<JavaAnnotation> getAnnotations() {
        return annotations;
    }

    public List<JavaComment> getComments() {
        return comments;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addModifier(JavaModifier modifier) {
        modifiers.add(modifier);
    }

    public void addAnnotation(Class<?> annotation) {
        addAnnotation(new JavaClass(annotation));
    }
    
    public void addAnnotation(JavaClass annotation) {
        addAnnotation(new JavaAnnotation(annotation));
    }

    public void addAnnotation(JavaAnnotation annotation) {
        annotations.add(annotation);
    }

    public void addAnnotation(Class<?> annotation, String... arguments) {
        addAnnotation(new JavaClass(annotation), arguments);
    }

    public void addAnnotation(JavaClass annotation, String... arguments) {
        annotations.add(new JavaAnnotation(annotation, arguments));
    }

    public void addComment(JavaComment comment) {
        comments.add(comment);
    }
}
