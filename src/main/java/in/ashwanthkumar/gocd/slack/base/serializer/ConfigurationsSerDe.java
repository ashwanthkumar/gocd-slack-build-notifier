package in.ashwanthkumar.gocd.slack.base.serializer;

import com.google.gson.*;
import in.ashwanthkumar.gocd.slack.base.config.Configuration;
import in.ashwanthkumar.gocd.slack.base.config.Configurations;
import in.ashwanthkumar.utils.collections.Lists;
import in.ashwanthkumar.utils.func.Function;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ConfigurationsSerDe implements JsonSerializer<Configurations>, JsonDeserializer<Configurations> {
    @Override
    public JsonElement serialize(Configurations configurations, Type type, JsonSerializationContext jsonSerializationContext) {
        Map<String, Configuration> configMap = new TreeMap<>();
        for (Configuration configuration : configurations) {
            configMap.put(configuration.getId(), configuration);
        }
        return jsonSerializationContext.serialize(configMap);
    }

    @Override
    public Configurations deserialize(JsonElement jsonElement, Type type, final JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();
        List<Configuration> configurations = new ArrayList<>();
        for (Map.Entry<String, JsonElement> element : json.entrySet()) {
            String id = element.getKey();
            Configuration config = jsonDeserializationContext.deserialize(json.get(id), Configuration.class);
            config.setId(id);
            configurations.add(config);
        }
        return new Configurations(configurations);
    }
}
