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
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Java Class model.
 * Intended for building java classes.
 *
 * @author Maksim Kaszynski
 */
public class JavaClass extends JavaLanguageElement {
    private static final JavaClass DEFAULT_SUPERCLASS = new JavaClass(Object.class);
    private List<JavaField> fields = new ArrayList<JavaField>();
    private List<JavaMethod> methods = new ArrayList<JavaMethod>();
    private Set<JavaImport> imports = new TreeSet<JavaImport>(new Comparator<JavaImport>() {
        public int compare(JavaImport o1, JavaImport o2) {
            return o1.getName().compareTo(o2.getName());
        }
    });
    private JavaClass superClass = DEFAULT_SUPERCLASS;
    private JavaPackage pakg;

    private String simpleName;
    
    public JavaClass() {
        super();
    }

    public JavaClass(Class<?> clazz) {
        this(clazz.getSimpleName(), new JavaPackage(clazz.getPackage()));
    }

    public JavaClass(String name) {
        this(getSimpleName(name), createPackage(name));
    }
    
    public JavaClass(String simpleName, JavaPackage pakg) {
        super(getFullName(pakg, simpleName));
        this.pakg = pakg;
        this.simpleName = simpleName;
    }

    public JavaClass(String simpleName, JavaPackage pakg, Class<?> superClass) {
        this(simpleName, pakg);
        setSuperClass(new JavaClass(superClass));
    }

    private static String getFullName(JavaPackage javaPackage, String className) {
        StringBuilder fullName = new StringBuilder();

        fullName.append(javaPackage.getName());

        if (fullName.length() != 0) {
            fullName.append('.');
        }
        fullName.append(className);

        return fullName.toString();
    }
    
    private static JavaPackage createPackage(String name) {
        int lastDotIdx = name.lastIndexOf('.');

        if (lastDotIdx != -1) {
            return new JavaPackage(name.substring(0, lastDotIdx));
        } else {
            return new JavaPackage("");
        }
    }
    
    private static String getSimpleName(String name) {
        int lastDotIdx = name.lastIndexOf('.');

        if (lastDotIdx != -1) {
            return name.substring(lastDotIdx + 1);
        } else {
            return name;
        }
    }
    
    public void addImport(String name) {
        if (shouldAddToImports(name)) {
            imports.add(new RuntimeImport(name));
        }
    }

    public void addImport(JavaClass javaClass) {
        addImport(javaClass.getName());
    }

    public void addImport(Class<?> claz) {
        if (shouldAddToImports(claz.getName())) {
            imports.add(new ClassImport(claz));
        }
    }

    @Override
    public void addAnnotation(JavaAnnotation annotation) {
        super.addAnnotation(annotation);
        addImport(annotation.getType());
    }

    public void addField(JavaField field) {
        fields.add(field);
        addImport(field.getType());

        List<JavaAnnotation> annotations2 = field.getAnnotations();

        if (annotations2 != null) {
            for (JavaAnnotation javaAnnotation : annotations2) {
                addImport(javaAnnotation.getType());
            }
        }
    }

    public void addMethod(JavaMethod method) {
        methods.add(method);
        addImport(method.getReturnType());

        List<Class<? extends Throwable>> exceptions = method.getExceptions();

        for (Class<? extends Throwable> exception : exceptions) {
            addImport(exception);
        }

        List<Argument> arguments = method.getArguments();

        if (arguments != null) {
            for (Argument argument : arguments) {
                addImport(argument.getType());
            }
        }

        List<JavaAnnotation> annotations2 = method.getAnnotations();

        if (annotations2 != null) {
            for (JavaAnnotation javaAnnotation : annotations2) {
                addImport(javaAnnotation.getType());
            }
        }

        MethodBody methodBody = method.getMethodBody();

        if (methodBody != null) {
            Set<Class<?>> usedClasses = methodBody.getUsedClasses();

            for (Class<?> class1 : usedClasses) {
                addImport(class1);
            }
        }
    }

    public JavaPackage getPakg() {
        return pakg;
    }

    public JavaClass getSuperClass() {
        return superClass;
    }

    public void setSuperClass(JavaClass superClass) {
        this.superClass = superClass;
        addImport(superClass.getName());
    }

    public void setPackage(JavaPackage s) {
        pakg = s;
    }

    public JavaPackage getPackage() {
        return pakg;
    }

    public List<JavaField> getFields() {
        return fields;
    }

    public List<JavaMethod> getMethods() {
        return methods;
    }

    public Set<JavaImport> getImports() {
        return imports;
    }

    public String getSimpleName() {
        return simpleName;
    }
    
    private boolean shouldAddToImports(String className) {
        if (className == null || className.length() == 0) {
            return false;
        }
        
        //default package & primitive types
        if (className.indexOf('.') == -1) {
            return false;
        }
        
        if (className.matches("^java\\.lang\\.[^\\.]+$")) {
            return false;
        }
        
        return true;
    }
}
