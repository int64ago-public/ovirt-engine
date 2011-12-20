package org.ovirt.engine.ui.webadmin.idhandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.UIObject;

/**
 * Indicates that the given field will have its DOM element ID set by an {@link ElementIdHandler} implementation.
 * <p>
 * Semantics of this annotation for different types are shown in the following table:
 * <p>
 * 
 * <blockquote>
 * <table border="1" cellpadding="5" cellspacing="0">
 * <thead>
 * <tr>
 * <th>Field Type</th>
 * <th>Semantics</th>
 * </tr>
 * </thead> <tbody>
 * <tr>
 * <td>{@link HasElementId}</td>
 * <td>call {@link HasElementId#setElementId setElementId} (used with custom UI object types)</td>
 * </tr>
 * <tr>
 * <td>{@link UIObject}</td>
 * <td>access element through {@linkplain UIObject#getElement getElement} and set its ID using {@link Element#setId
 * setId}</td>
 * </tr>
 * <tr>
 * <td>{@link Element}</td>
 * <td>set element ID using {@link Element#setId setId}</td>
 * </tr>
 * </tbody>
 * </table>
 * </blockquote>
 * 
 * <p>
 * Since {@link ElementIdHandler} implementations access field values directly through field declarations, annotated
 * fields should not be {@code private}.
 * 
 * @see ElementIdHandler
 * @see HasElementId
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface WithElementId {

    /**
     * Overrides the default field ID that is part of the resulting DOM element ID.
     * <p>
     * When not specified, the name of the annotated field will be taken as the field ID value.
     * 
     * @return Custom field ID or an empty string to use the default value.
     */
    String value() default "";

}
