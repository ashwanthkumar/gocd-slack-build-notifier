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
import in.ashwanthkumar.utils.collections.Lists;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.*;

import static in.ashwanthkumar.gocd.slack.GoNotificationPlugin.INTERNAL_ERROR_RESPONSE_CODE;
import static in.ashwanthkumar.gocd.slack.GoNotificationPlugin.SUCCESS_RESPONSE_CODE;
import static in.ashwanthkumar.utils.lang.StringUtils.isNotEmpty;

abstract public class AbstractNotificationPlugin implements GoPlugin {
    public static final String EXTENSION_TYPE = "notification";
    public static final String REQUEST_STAGE_STATUS = "stage-status";

    protected Logger LOGGER = Logger.getLoggerFor(this.getClass());
    protected GoApplicationAccessor goApplicationAccessor;

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
    abstract public String template() throws Exception;

    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
        this.goApplicationAccessor = goApplicationAccessor;
    }

    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier(EXTENSION_TYPE, getGoSupportedVersions());
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

    // TODO - Make me private
    protected GoPluginApiResponse handleStageNotification(GoPluginApiRequest goPluginApiRequest) {
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

    // TODO - Make me private
    protected GoPluginApiResponse handleNotificationsInterestedIn() {
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", subscriptions());
        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    // TODO - Make me private
    protected GoPluginApiResponse handleRequestGetView() {
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


    /**
     * Create a configuration field for the plugin.
     *
     * @param displayName  Name of the configuration
     * @param defaultValue Default value if none provided
     * @param displayOrder Order in which it should be displayed
     * @param required     If the field is mandatory.
     * @param secure       If the data in the field should be stored encrypted.
     * @return
     */
    protected Map<String, Object> configField(String displayName, String defaultValue, String displayOrder, boolean required, boolean secure) {
        Map<String, Object> serverUrlParams = new TreeMap<>();
        serverUrlParams.put("display-name", displayName);
        serverUrlParams.put("display-value", defaultValue);
        serverUrlParams.put("display-order", displayOrder);
        serverUrlParams.put("required", required);
        serverUrlParams.put("secure", secure);
        return serverUrlParams;
    }


    protected GoPluginApiResponse renderJSON(final int responseCode, final Object response) {
        final String json = response == null ? null : new GsonBuilder().disableHtmlEscaping().create().toJson(response);
        DefaultGoPluginApiResponse pluginApiResponse = new DefaultGoPluginApiResponse(responseCode);
        pluginApiResponse.setResponseBody(json);
        return pluginApiResponse;
    }

    private GoNotificationMessage parseNotificationMessage(GoPluginApiRequest goPluginApiRequest) {
        return new GsonBuilder().create().fromJson(goPluginApiRequest.requestBody(), GoNotificationMessage.class);
    }
}
