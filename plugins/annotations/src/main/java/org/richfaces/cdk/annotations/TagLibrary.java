/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.richfaces.cdk.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation defines taglib for which all tags for JSF components from that packages belong to
 * 
 * @author asmirnov
 * @version $Id$
 * 
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PACKAGE)
public @interface TagLibrary {
    
    /**
     * <p class="changed_added_4_0">Library URI, the same used for JSP and Facelets.</p>
     * @return
     */
    public String uri();

    /**
     * <p class="changed_added_4_0">Library short name ( default prefix )</p>
     * @return
     */
    public String shortName();
    
    
    /**
     * <p class="changed_added_4_0">Default preffix for package names and JSF ids in the library</p>
     * @return
     */
    public String preffix() default "";

    /**
     * <p class="changed_added_4_0">Implementation version of the generated taglib.</p>
     * @return
     */
    public String tlibVersion() default "";

    /**
     * <p class="changed_added_4_0">JSP taglib validator.  TODO - ? extends Validator  ?</p>
     * @return
     */
    public Class<?> validatorClass() default NONE.class;

    /**
     * <p class="changed_added_4_0">Servlet ... listener used by JSP library. TODO - ? extends {@link EventListener} ?</p>
     * @return
     */
    public Class<?> listenerClass()  default NONE.class;

    /**
     * <p class="changed_added_4_0">Library description to use by IDE</p>
     * @return
     */
    public String displayName() default "";

    /**
     * <p class="changed_added_4_0">Java Server Pages version for generated tld.</p>
     * @return
     */
    public String jspVersion() default "2.0";


    public static final class NONE {}

}
