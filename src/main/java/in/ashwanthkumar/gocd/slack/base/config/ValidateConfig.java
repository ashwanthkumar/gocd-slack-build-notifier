package in.ashwanthkumar.gocd.slack.base.config;

import com.thoughtworks.go.plugin.api.logging.Logger;
import in.ashwanthkumar.utils.collections.Lists;
import in.ashwanthkumar.utils.func.Function;
import in.ashwanthkumar.utils.lang.option.Option;

import javax.validation.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

import static in.ashwanthkumar.gocd.slack.base.serializer.GsonFactory.GSON;

public class ValidateConfig {
    private static final Logger LOG = Logger.getLoggerFor(ValidateConfig.class);
    private Map<String, Object> rawSettings;

    public ValidateConfig() {
        this.rawSettings = new HashMap<>();
    }

    public Map<String, Object> getRawSettings() {
        return rawSettings;
    }

    public ValidateConfig addProp(String key, Object value) {
        this.rawSettings.put(key, value);
        return this;
    }

    public <T> T toSettings(Class<T> settingsClazz, Configurations configurations) throws IllegalAccessException, InstantiationException, IOException {
        Field[] fields = settingsClazz.getDeclaredFields();
        T settings = settingsClazz.newInstance();
        for (Field field : fields) {
            Option<Configuration> byName = configurations.findByName(field.getName());
            if (byName.isDefined()) {
                field.set(settings, toType(rawSettings.get(byName.get().getId()), field.getType()));
            } else {
                LOG.warn(field.getName() + " is not present in the " + settingsClazz.getCanonicalName() + " class. Probably we removed the field?");
            }
        }
        return settings;
    }

    public <T> List<ValidationError> validate(Class<T> settingzClazz) throws Exception {
        Configurations configurations = ConfigurationsParser.parseConfigurations(settingzClazz);
        T settings = toSettings(settingzClazz, configurations);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(settings);
        List<ValidationError> errors = new ArrayList<>();
        for (ConstraintViolation<T> violation : constraintViolations) {
            String propertyPath = Lists.mkString(Lists.map(violation.getPropertyPath(), new Function<Path.Node, String>() {
                @Override
                public String apply(Path.Node node) {
                    return node.getName();
                }
            }));
            errors.add(new ValidationError(configurations.findByName(propertyPath).get().getId(), violation.getMessage()));
        }
        return errors;
    }

    public <T> T toType(Object obj, Type type) {
        String serialized = GSON.toJson(obj);
        return GSON.fromJson(serialized, type);
    }


}
