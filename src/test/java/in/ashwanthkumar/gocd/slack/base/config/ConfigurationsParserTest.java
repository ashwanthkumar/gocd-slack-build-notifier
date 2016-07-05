package in.ashwanthkumar.gocd.slack.base.config;

import in.ashwanthkumar.gocd.slack.base.Configuration;
import in.ashwanthkumar.gocd.slack.base.Configurations;
import in.ashwanthkumar.gocd.slack.base.PluginConfig;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ConfigurationsParserTest {

    class TestSettings {
        @PluginConfig(id = "server-url-external", displayName = "WebHook URL")
        private String externalServerUrl;

        @PluginConfig(id = "pluginConfig", displayName = "Plugin configurations")
        private String pipelineConfig;

        private String ignoredField;
    }

    @Test
    public void shouldParseSettingsFromAClass() {
        Configurations configurations = ConfigurationsParser.parseConfigurations(TestSettings.class);
        assertThat(2, is(configurations.size()));

        Configuration config1 = configurations.get(0);
        Configuration expectedConfig1 = new Configuration().setId("server-url-external").setDisplayName("WebHook URL")
                .setDisplayOrder(1).setRequired(false).setSecure(false);
        assertThat(config1, is(expectedConfig1));

        Configuration config2 = configurations.get(1);
        Configuration expectedConfig2 = new Configuration().setId("pluginConfig").setDisplayName("Plugin configurations")
                .setDisplayOrder(1).setRequired(false).setSecure(false);
        assertThat(config2, is(expectedConfig2));
    }

}
