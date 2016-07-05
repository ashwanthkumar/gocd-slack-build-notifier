package in.ashwanthkumar.gocd.slack.base.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import in.ashwanthkumar.gocd.slack.base.Configuration;
import in.ashwanthkumar.gocd.slack.base.Configurations;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.TreeMap;

public class ConfigurationsSerializer implements JsonSerializer<Configurations> {
    @Override
    public JsonElement serialize(Configurations configurations, Type type, JsonSerializationContext jsonSerializationContext) {
        Map<String, Configuration> configMap = new TreeMap<>();
        for (Configuration configuration : configurations) {
            configMap.put(configuration.getId(), configuration);
        }
        return jsonSerializationContext.serialize(configMap);
    }
}
