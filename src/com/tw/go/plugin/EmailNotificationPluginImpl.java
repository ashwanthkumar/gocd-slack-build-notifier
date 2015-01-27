package com.tw.go.plugin;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.*;

import static java.util.Arrays.asList;

@Extension
public class EmailNotificationPluginImpl implements GoPlugin {
    private static Logger LOGGER = Logger.getLoggerFor(EmailNotificationPluginImpl.class);

    public static final String EXTENSION_NAME = "notification";
    private static final List<String> goSupportedVersions = asList("1.0");

    public static final String REQUEST_NOTIFICATIONS_INTERESTED_IN = "notifications-interested-in";
    public static final String REQUEST_STAGE_STATUS = "stage-status";

    public static final int SUCCESS_RESPONSE_CODE = 200;
    public static final int INTERNAL_ERROR_RESPONSE_CODE = 500;

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
        // ignore
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest goPluginApiRequest) {
        if (goPluginApiRequest.requestName().equals(REQUEST_NOTIFICATIONS_INTERESTED_IN)) {
            return handleNotificationsInterestedIn();
        } else if (goPluginApiRequest.requestName().equals(REQUEST_STAGE_STATUS)) {
            return handleStageNotification(goPluginApiRequest);
        }
        return null;
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier(EXTENSION_NAME, goSupportedVersions);
    }

    private GoPluginApiResponse handleNotificationsInterestedIn() {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("notifications", Arrays.asList(REQUEST_STAGE_STATUS));
        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private GoPluginApiResponse handleStageNotification(GoPluginApiRequest goPluginApiRequest) {
        Map<String, Object> dataMap = getMapFor(goPluginApiRequest);

        int responseCode = SUCCESS_RESPONSE_CODE;
        Map<String, Object> response = new HashMap<String, Object>();
        List<String> messages = new ArrayList<String>();
        try {
            String subject = "Stage: " + dataMap.get("pipeline-name") + "/" + ((Double) dataMap.get("pipeline-counter")).intValue() + "/" + dataMap.get("stage-name") + "/" + dataMap.get("stage-counter");
            String body = "State: " + dataMap.get("stage-state") + "\nResult: " + dataMap.get("stage-result") + "\nCreate Time: " + dataMap.get("create-time");

            LOGGER.warn("Sending Email for " + subject);

            SMTPSettings settings = new SMTPSettings("smtp.gmail.com", 587, true, "", "");
            new SMTPMailSender(settings).send(subject, body, "");

            LOGGER.warn("Done");

            response.put("status", "success");
            messages.add("Could connect to URL successfully");
        } catch (Exception e) {
            responseCode = INTERNAL_ERROR_RESPONSE_CODE;
            response.put("status", "failure");
            messages.add(e.getMessage());
        }

        response.put("messages", messages);
        return renderJSON(responseCode, response);
    }

    private Map<String, Object> getMapFor(GoPluginApiRequest goPluginApiRequest) {
        return (Map<String, Object>) new GsonBuilder().create().fromJson(goPluginApiRequest.requestBody(), Object.class);
    }

    private GoPluginApiResponse renderJSON(final int responseCode, Object response) {
        final String json = response == null ? null : new GsonBuilder().create().toJson(response);
        return new GoPluginApiResponse() {
            @Override
            public int responseCode() {
                return responseCode;
            }

            @Override
            public Map<String, String> responseHeaders() {
                return null;
            }

            @Override
            public String responseBody() {
                return json;
            }
        };
    }
}
