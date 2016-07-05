package in.ashwanthkumar.gocd.slack.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PluginConfig {
    /**
     * Defaults to name of the field. In order to keep the existing settings backward compatible, please
     * use this value to set the id of the field so even up refactoring the Class, the settings doesn't break.
     */
    String id() default "";

    /**
     * Name of the property when displaying it on the template
     */
    String displayName();

    /**
     * Default value of the configuration, if none is given
     */
    String defaultValue() default "";

    /**
     * Display order of the configuration on the settings page.
     */
    int displayOrder() default 1;

    /**
     * Is this config property mandatory. Defaults to false, if not providing this, we recommend using <i>defaultValue</i>
     */
    boolean required() default false;

    /**
     * Enable this to store the value of the property in a secured manner. Generally used with passwords like fields.
     */
    boolean secure() default false;
}
