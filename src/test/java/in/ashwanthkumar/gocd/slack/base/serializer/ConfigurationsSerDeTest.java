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

public class ConfigurationsSerDeTest {
    @Test
    public void shouldSerializeConfigurationsProperly() {
        Configurations configurations = new Configurations(Lists.of(
                new Configuration().setId("prop-1").setDisplayName("Prop 1"),
                new Configuration().setId("prop-2").setDisplayName("Prop 2").setRequired(true)
        ));
        Gson gson = GsonFactory.getGson();
        String configAsJson = gson.toJson(configurations);
        Map<String, Configuration> expectedMap = new TreeMap<>();
        for (Configuration config : configurations) {
            expectedMap.put(config.getId(), config);
        }
        String expectedJson = gson.toJson(expectedMap);

        assertThat(configAsJson, is(expectedJson));
    }

    @Test
    public void shouldDeserializeConfigurations() {
        String input = "{\"pluginConfig\":{\"display-name\":\"Pipeline Notification Rules\",\"default-value\":\"\",\"display-order\":1,\"required\":false,\"secure\":false}," +
                "\"server-url-external\":{\"display-name\":\"External GoCD Server\",\"default-value\":\"\",\"display-order\":1,\"required\":false,\"secure\":false}}";
        Configurations configurations = GsonFactory.getGson().fromJson(input, Configurations.class);
        assertThat(configurations.size(), is(2));

        Configuration c1 = configurations.get(0);
        Configuration e1 = new Configuration().setId("pluginConfig").setDisplayName("Pipeline Notification Rules").setDisplayOrder(1);
        assertThat(c1, is(e1));

        Configuration c2 = configurations.get(1);
        Configuration e2 = new Configuration().setId("server-url-external").setDisplayName("External GoCD Server").setDisplayOrder(1);
        assertThat(c2, is(e2));
    }


}
