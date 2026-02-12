package com.github.mkram17.bazaarutils.misc.autoregistration;

import com.github.mkram17.bazaarutils.events.util.EventPriorities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a static method to be called automatically during mod initialization.
 * The method must be public, static, and take no arguments.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RunOnInit {
    /**
     * The priority of the initialization method. Lower values run first.
     * @return the priority, defaults to 2.
     */
    EventPriorities priority() default EventPriorities.NORMAL;
}