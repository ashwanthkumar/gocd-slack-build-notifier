package in.ashwanthkumar.gocd.slack.base.config;

import in.ashwanthkumar.gocd.slack.base.PluginConfig;
import in.ashwanthkumar.gocd.slack.base.serializer.GsonFactory;
import org.apache.commons.io.IOUtils;
import org.hamcrest.CoreMatchers;
import org.hibernate.validator.constraints.Length;
import org.junit.Test;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

class TestSettings1 {
    @PluginConfig(displayName = "Server Name")
    String server_url;
    @PluginConfig(displayName = "Username")
    String username;
    @PluginConfig(displayName = "password")
    String password;
    @PluginConfig(displayName = "port")
    Integer port;
}

class TestSettings2 {
    @PluginConfig(displayName = "Server Name", id = "go-server-url")
    String server_url;
    @PluginConfig(displayName = "Username")
    String username;
    @Length(max = 8, min = 1)
    @NotNull
    @PluginConfig(displayName = "password", id = "pass")
    String password;
    @PluginConfig(displayName = "port")
    Integer port;
}

public class ValidateConfigTest {

    @Test
    public void shouldDeserializeValidatePluginConfigRequest() throws IOException, InstantiationException, IllegalAccessException {
        String requestBody = IOUtils.toString(getClass().getResourceAsStream("/json/validate-settings.json"));
        ValidateConfig validateConfig = GsonFactory.GSON.fromJson(requestBody, ValidateConfig.class);
        assertNotNull(validateConfig);
        Map<String, Object> rawSettings = validateConfig.getRawSettings();
        assertThat(rawSettings.size(), is(4));
        assertThat(rawSettings.get("server_url"), CoreMatchers.<Object>is("http://localhost.com"));
        assertThat(rawSettings.get("username"), CoreMatchers.<Object>is("user"));
        assertThat(rawSettings.get("password"), CoreMatchers.<Object>is("password"));
        assertThat(rawSettings.get("port"), CoreMatchers.<Object>is("8080"));

        TestSettings1 settings = validateConfig.toSettings(TestSettings1.class, ConfigurationsParser.parseConfigurations(TestSettings1.class));
        assertThat(settings.server_url, is("http://localhost.com"));
        assertThat(settings.username, is("user"));
        assertThat(settings.password, is("password"));
        assertThat(settings.port, is(8080));
    }

    @Test
    public void shouldDeserializeValidatePluginConfigRequestWithFieldNamesNotMatchingTheId() throws IOException, InstantiationException, IllegalAccessException {
        String requestBody = IOUtils.toString(getClass().getResourceAsStream("/json/validate-settings-2.json"));
        ValidateConfig validateConfig = GsonFactory.GSON.fromJson(requestBody, ValidateConfig.class);
        assertNotNull(validateConfig);
        Map<String, Object> rawSettings = validateConfig.getRawSettings();
        assertThat(rawSettings.size(), is(4));
        assertThat(rawSettings.get("go-server-url"), CoreMatchers.<Object>is("http://localhost.com"));
        assertThat(rawSettings.get("username"), CoreMatchers.<Object>is("user"));
        assertThat(rawSettings.get("pass"), CoreMatchers.<Object>is("password"));
        assertThat(rawSettings.get("port"), CoreMatchers.<Object>is("8080"));

        TestSettings2 settings = validateConfig.toSettings(TestSettings2.class, ConfigurationsParser.parseConfigurations(TestSettings2.class));
        assertThat(settings.server_url, is("http://localhost.com"));
        assertThat(settings.username, is("user"));
        assertThat(settings.password, is("password"));
        assertThat(settings.port, is(8080));
    }

    @Test
    public void shouldValidateTheSettingsFromIncomingRequest() throws Exception {
        String requestBody = IOUtils.toString(getClass().getResourceAsStream("/json/validate-invalid-settings-2.json"));
        ValidateConfig validateConfig = GsonFactory.GSON.fromJson(requestBody, ValidateConfig.class);
        assertNotNull(validateConfig);

        List<ValidationError> validationErrors = validateConfig.validate(TestSettings2.class);
        assertThat(validationErrors.size(), is(1));
        assertThat(validationErrors, hasItem(new ValidationError("pass", "length must be between 1 and 8")));
    }


}