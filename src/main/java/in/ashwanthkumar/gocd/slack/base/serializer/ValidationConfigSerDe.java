package in.ashwanthkumar.gocd.slack.base.serializer;

import com.google.gson.*;
import in.ashwanthkumar.gocd.slack.base.config.ValidateConfig;

import java.lang.reflect.Type;
import java.util.Map;

public class ValidationConfigSerDe implements JsonDeserializer<ValidateConfig> {
    @Override
    public ValidateConfig deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject pluginSettings = jsonElement.getAsJsonObject().getAsJsonObject("plugin-settings");
        ValidateConfig validateConfig = new ValidateConfig();
        for (Map.Entry<String, JsonElement> entry : pluginSettings.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().getAsJsonObject().get("value").getAsString();
            validateConfig.addProp(key, value);
        }
        return validateConfig;
    }
}
