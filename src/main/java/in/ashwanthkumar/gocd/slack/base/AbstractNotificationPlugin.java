package in.ashwanthkumar.gocd.slack.base;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import in.ashwanthkumar.gocd.slack.GoNotificationMessage;
import in.ashwanthkumar.gocd.slack.base.config.Configurations;
import in.ashwanthkumar.gocd.slack.base.config.ConfigurationsParser;
import in.ashwanthkumar.gocd.slack.base.serializer.GsonFactory;
import in.ashwanthkumar.utils.collections.Lists;

import java.util.*;

import static in.ashwanthkumar.utils.lang.StringUtils.isNotEmpty;

abstract public class AbstractNotificationPlugin<T> implements GoPlugin {
    public static final String EXTENSION_TYPE = "notification";

    public static final int SUCCESS_RESPONSE_CODE = 200;
    public static final int INTERNAL_ERROR_RESPONSE_CODE = 500;

    public static final String REQUEST_STAGE_STATUS = "stage-status";
    public static final String REQUEST_NOTIFICATIONS_INTERESTED_IN = "notifications-interested-in";
    public static final String REQUEST_GET_CONFIGURATION = "go.plugin-settings.get-configuration";
    public static final String REQUEST_GET_VIEW = "go.plugin-settings.get-view";
    public static final String REQUEST_VALIDATE_CONFIGURATION = "go.plugin-settings.validate-configuration";

    protected Logger LOGGER = Logger.getLoggerFor(this.getClass());
    protected GoApplicationAccessor goApplicationAccessor;

    private Configurations configurations;
    protected T settings;

    /**
     * StageNotification handler of the plugin. For every notification from the server, we'll call this method.
     *
     * @param notification Actual Notification from the server.
     * @throws Exception If something went wrong while trying to handle the notification
     */
    abstract public void handleStageNotification(GoNotificationMessage notification) throws Exception;

    /**
     * Return the template of the settings page of the plugin - if present.
     *
     * @return Entire HTML template as string
     * @throws Exception In case of any issues while trying to get the template from a local file / URL.
     */
    public String template() throws Exception {
        return null;
    }

    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
        this.goApplicationAccessor = goApplicationAccessor;
    }

    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier(EXTENSION_TYPE, getGoSupportedVersions());
    }

    public GoPluginApiResponse handle(GoPluginApiRequest goPluginApiRequest) {
        String requestName = goPluginApiRequest.requestName();
        switch (requestName) {
            case REQUEST_NOTIFICATIONS_INTERESTED_IN:
                return handleNotificationsInterestedIn();
            case REQUEST_STAGE_STATUS:
                return handleStageNotification(goPluginApiRequest);
            case REQUEST_GET_VIEW:
                return handleRequestGetView();
            case REQUEST_VALIDATE_CONFIGURATION:
                return handleValidateConfig(goPluginApiRequest.requestBody());
            case REQUEST_GET_CONFIGURATION:
                return handleRequestGetConfiguration();
        }
        return null;
    }

    /**
     * List of Go Notification API do we support.
     */
    protected List<String> getGoSupportedVersions() {
        return Lists.of("1.0");
    }

    /**
     * List of notifications that we should subscribe to.
     */
    protected List<String> subscriptions() {
        return Lists.of(REQUEST_STAGE_STATUS);
    }

    private GoPluginApiResponse handleStageNotification(GoPluginApiRequest goPluginApiRequest) {
        GoNotificationMessage message = parseNotificationMessage(goPluginApiRequest);
        int responseCode = SUCCESS_RESPONSE_CODE;

        Map<String, Object> response = new HashMap<String, Object>();
        List<String> messages = new ArrayList<String>();
        try {
            response.put("status", "success");
            handleStageNotification(message);
        } catch (Exception e) {
            LOGGER.error(message.fullyQualifiedJobName() + " failed with error", e);
            responseCode = INTERNAL_ERROR_RESPONSE_CODE;
            response.put("status", "failure");
            if (isNotEmpty(e.getMessage())) {
                messages.add(e.getMessage());
            }
        }

        if (!messages.isEmpty()) {
            response.put("messages", messages);
        }
        return renderJSON(responseCode, response);
    }

    private GoPluginApiResponse handleNotificationsInterestedIn() {
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", subscriptions());
        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private GoPluginApiResponse handleRequestGetView() {
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("template", template());
        } catch (Exception e) {
            LOGGER.error("Unable to fetch the template because - " + e.getMessage(), e);
            response.put("error", e.getMessage());
            return renderJSON(INTERNAL_ERROR_RESPONSE_CODE, response);
        }

        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private GoPluginApiResponse handleRequestGetConfiguration() {
        if(isSettingsConfigured()) {
            lazyUpdateConfigurations();
            return renderJSON(SUCCESS_RESPONSE_CODE, this.configurations);
        }
        return renderJSON(SUCCESS_RESPONSE_CODE, new HashMap<String, Object>());
    }

    private GoPluginApiResponse handleValidateConfig(String requestBody) {
        List<Object> response = Arrays.asList();
        // TODO - Implement parsing Configurations back to settings
        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private GoPluginApiResponse renderJSON(final int responseCode, final Object response) {
        final String json = response == null ? null : GsonFactory.getGson().toJson(response);
        DefaultGoPluginApiResponse pluginApiResponse = new DefaultGoPluginApiResponse(responseCode);
        pluginApiResponse.setResponseBody(json);
        return pluginApiResponse;
    }

    private GoNotificationMessage parseNotificationMessage(GoPluginApiRequest goPluginApiRequest) {
        return GsonFactory.getGson().fromJson(goPluginApiRequest.requestBody(), GoNotificationMessage.class);
    }

    private void lazyUpdateConfigurations() {
        if (this.configurations == null) {
            this.configurations = ConfigurationsParser.parseConfigurations(settings.getClass());
        }
    }

    private boolean isSettingsConfigured() {
        return settings != null;
    }
}
