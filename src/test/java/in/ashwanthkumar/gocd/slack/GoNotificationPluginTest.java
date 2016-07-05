package in.ashwanthkumar.gocd.slack;


import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import in.ashwanthkumar.gocd.slack.util.TestUtils;
import org.junit.Test;

import java.io.File;

import static in.ashwanthkumar.gocd.slack.GoNotificationPlugin.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GoNotificationPluginTest {

    public static final String USER_HOME = "user.home";

    public static final String NOTIFICATION_INTEREST_RESPONSE = "{\"notifications\":[\"stage-status\"]}";
    public static final String GET_CONFIGURATION_RESPONSE = "{\"pipelineConfig\":{\"display-name\":\"Pipeline Notification Rules\",\"display-order\":\"2\",\"display-value\":\"\",\"required\":true,\"secure\":false},\"server-url-external\":{\"display-name\":\"External GoCD Server URL\",\"display-order\":\"1\",\"display-value\":\"\",\"required\":true,\"secure\":false}}";
    private static final String GET_CONFIG_VALIDATION_RESPONSE = "[]";

    @Test
    public void canHandleConfigValidationRequest() {
        GoNotificationPlugin plugin = createGoNotificationPluginFromConfigAtHomeDir();

        GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestName()).thenReturn(REQUEST_VALIDATE_CONFIGURATION);
        when(request.requestBody()).thenReturn("{\"plugin-settings\":" +
                "{\"external_server_url\":{\"value\":\"bob\"}}}");

        GoPluginApiResponse rv = plugin.handle(request);

        assertThat(rv, is(notNullValue()));
        assertThat(rv.responseBody(), equalTo(GET_CONFIG_VALIDATION_RESPONSE));
    }

    @Test
    public void canHandleConfigurationRequest() {
        GoNotificationPlugin plugin = createGoNotificationPluginFromConfigAtHomeDir();

        GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestName()).thenReturn(REQUEST_GET_CONFIGURATION);

        GoPluginApiResponse rv = plugin.handle(request);

        assertThat(rv, is(notNullValue()));
        assertThat(rv.responseBody(), equalTo(GET_CONFIGURATION_RESPONSE));
    }

    @Test
    public void canHandleGetViewRequest() {
        GoNotificationPlugin plugin = createGoNotificationPluginFromConfigAtHomeDir();

        GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestName()).thenReturn(REQUEST_GET_VIEW);

        GoPluginApiResponse rv = plugin.handle(request);

        assertThat(rv, is(notNullValue()));
        assertThat(rv.responseBody(), containsString("<div class=\\\""));
    }

    @Test
    public void canHandleNotificationInterestedInRequest() {
        GoNotificationPlugin plugin = createGoNotificationPluginFromConfigAtHomeDir();

        GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestName()).thenReturn(REQUEST_NOTIFICATIONS_INTERESTED_IN);

        GoPluginApiResponse rv = plugin.handle(request);

        assertThat(rv, is(notNullValue()));
        assertThat(rv.responseBody(), equalTo(NOTIFICATION_INTEREST_RESPONSE));
    }

    @Test
    public void canHandleNotificationInterestedInRequestForConfigFromEnvVariable() {
        GoNotificationPlugin plugin = createGoNotificationPluginFromConfigAtEnvironmentVariableLocation(GO_NOTIFY_CONF);

        GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestName()).thenReturn(REQUEST_NOTIFICATIONS_INTERESTED_IN);

        GoPluginApiResponse rv = plugin.handle(request);

        assertThat(rv, is(notNullValue()));
        assertThat(rv.responseBody(), equalTo(NOTIFICATION_INTEREST_RESPONSE));
    }

    @Test
    public void canHandleNotificationInterestedInRequestForConfigFromGoServerPath() {
        GoNotificationPlugin plugin = createGoNotificationPluginFromConfigAtEnvironmentVariableLocation(CRUISE_SERVER_DIR);

        GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestName()).thenReturn(REQUEST_NOTIFICATIONS_INTERESTED_IN);

        GoPluginApiResponse rv = plugin.handle(request);

        assertThat(rv, is(notNullValue()));
        assertThat(rv.responseBody(), equalTo(NOTIFICATION_INTEREST_RESPONSE));
    }

    public GoNotificationPlugin createGoNotificationPluginFromConfigAtHomeDir() {
        String folder = TestUtils.getResourceDirectory("configs/go_notify.conf");

        String oldUserHome = System.getProperty(USER_HOME);
        System.setProperty(USER_HOME, folder);

        GoNotificationPlugin plugin = new GoNotificationPlugin();

        System.setProperty(USER_HOME, oldUserHome);
        return plugin;
    }

    public GoNotificationPlugin createGoNotificationPluginFromConfigAtEnvironmentVariableLocation(String envVariable) {
        String folder = TestUtils.getResourceDirectory("configs/go_notify.conf");
        GoEnvironment goEnvironment = new GoEnvironment().setEnv(envVariable, folder + File.separator + CONFIG_FILE_NAME);
        return new GoNotificationPlugin(goEnvironment);
    }

}
