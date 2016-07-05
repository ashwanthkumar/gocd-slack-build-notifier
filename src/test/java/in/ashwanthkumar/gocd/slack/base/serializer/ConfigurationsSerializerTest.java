package in.ashwanthkumar.gocd.slack.base.serializer;

import com.google.gson.Gson;
import in.ashwanthkumar.gocd.slack.base.config.Configuration;
import in.ashwanthkumar.gocd.slack.base.config.Configurations;
import in.ashwanthkumar.utils.collections.Lists;
import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ConfigurationsSerializerTest {
    @Test
    public void shouldSerializeConfigurationsProperly() {
        Configurations configurations = new Configurations(Lists.of(
                new Configuration().setId("prop-1").setDisplayName("Prop 1"),
                new Configuration().setId("prop-2").setDisplayName("Prop 2").setRequired(true)
        ));
        Gson gson = GsonFactory.getGson();
        String configAsJson = gson.toJson(configurations);
        Map<String, Map<String, Object>> expectedMap = new TreeMap<>();
        for (Configuration config : configurations) {
            expectedMap.put(config.getId(), config.asMap());
        }
        String expectedJson = gson.toJson(expectedMap);

        assertThat(configAsJson, is(expectedJson));
    }

}
