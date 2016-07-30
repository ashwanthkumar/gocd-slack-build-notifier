package in.ashwanthkumar.gocd.slack.base.config;

import in.ashwanthkumar.gocd.slack.base.PluginConfig;
import in.ashwanthkumar.utils.func.Function;
import in.ashwanthkumar.utils.func.Predicates;
import in.ashwanthkumar.utils.lang.StringUtils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static in.ashwanthkumar.utils.collections.Lists.filter;
import static in.ashwanthkumar.utils.collections.Lists.map;

public class ConfigurationsParser {

    public static Configurations parseConfigurations(Class<?> settingsClazz) {
        Field[] declaredFields = settingsClazz.getDeclaredFields();
        List<Configuration> configurations = filter(
                map(declaredFields, new Function<Field, Configuration>() {
                    @Override
                    public Configuration apply(Field field) {
                        PluginConfig annotation = field.getAnnotation(PluginConfig.class);
                        if (annotation != null) {
                            String id = (StringUtils.isNotEmpty(annotation.id())) ? annotation.id() : field.getName();
                            return new Configuration()
                                    .setId(id)
                                    .setFieldName(field.getName())
                                    .setDefaultValue(annotation.defaultValue())
                                    .setDisplayName(annotation.displayName())
                                    .setDisplayOrder(annotation.displayOrder())
                                    .setRequired(annotation.required())
                                    .setSecure(annotation.secure());
                        }
                        return null;
                    }
                }), Predicates.<Configuration>notNull());

        Collections.sort(configurations);
        return new Configurations(configurations);
    }
}
