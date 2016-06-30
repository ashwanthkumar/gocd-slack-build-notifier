package in.ashwanthkumar.gocd.slack;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import in.ashwanthkumar.gocd.slack.ruleset.Rules;
import in.ashwanthkumar.gocd.slack.ruleset.RulesReader;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.Arrays.asList;

@Extension
public class GoNotificationPlugin implements GoPlugin {
    private static Logger LOGGER = Logger.getLoggerFor(GoNotificationPlugin.class);
    private static final long CONFIG_REFRESH_INTERVAL = 60 * 1000; // 1 minute

    public static final String EXTENSION_TYPE = "notification";
    private static final List<String> goSupportedVersions = asList("1.0");

    public static final String REQUEST_NOTIFICATIONS_INTERESTED_IN = "notifications-interested-in";
    public static final String REQUEST_STAGE_STATUS = "stage-status";
    public static final String REQUEST_GET_CONFIGURATION = "go.plugin-settings.get-configuration";
    public static final String REQUEST_GET_VIEW = "go.plugin-settings.get-view";
    public static final String REQUEST_VALIDATE_CONFIGURATION = "go.plugin-settings.validate-configuration";

    public static final int SUCCESS_RESPONSE_CODE = 200;
    public static final int INTERNAL_ERROR_RESPONSE_CODE = 500;

    public static final String GO_NOTIFY_CONFIGURATION = "go_notify.conf";
    public static final String PLUGIN_CONFIG_PATH = System.getProperty("user.home") + File.separator + GO_NOTIFY_CONFIGURATION;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private Rules rules;

    private final Timer timer = new Timer();
    private long configLastModified = 0l;

    public GoNotificationPlugin() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                File pluginConfig = new File(PLUGIN_CONFIG_PATH);
                if (!pluginConfig.exists()) {
                    throw new RuntimeException(String.format("%s file is not found in %s", GO_NOTIFY_CONFIGURATION, System.getProperty("user.home")));
                }
                if (pluginConfig.lastModified() != configLastModified) {
                    lock.writeLock().lock();
                    rules = RulesReader.read(PLUGIN_CONFIG_PATH);
                    lock.writeLock().unlock();
                    configLastModified = pluginConfig.lastModified();
                }
            }
        }, 0, CONFIG_REFRESH_INTERVAL);
    }

    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
        // ignore
    }

    public GoPluginApiResponse handle(GoPluginApiRequest goPluginApiRequest) {
        String requestName = goPluginApiRequest.requestName();
        if (requestName.equals(REQUEST_NOTIFICATIONS_INTERESTED_IN)) {
            return handleNotificationsInterestedIn();
        } else if (requestName.equals(REQUEST_STAGE_STATUS)) {
            return handleStageNotification(goPluginApiRequest);
        } else if (requestName.equals(REQUEST_GET_VIEW)) {
            return handleRequestGetView();
        } else if (requestName.equals(REQUEST_VALIDATE_CONFIGURATION)) {
            return handleValidateConfig(goPluginApiRequest.requestBody());
        } else if (requestName.equals(REQUEST_GET_CONFIGURATION)) {
            return handleRequestGetConfiguration();
        }
        return null;
    }

    private GoPluginApiResponse handleValidateConfig(String requestBody) {
        List<Object> response = Arrays.asList();
        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }


    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier(EXTENSION_TYPE, goSupportedVersions);
    }


    private GoPluginApiResponse handleRequestGetView() {
        Map<String, Object> response = new HashMap<String, Object>();

        try {
            String template = IOUtils.toString(getClass().getResourceAsStream("/views/config.template.html"), "UTF-8");
            response.put("template", template);
        } catch (IOException e) {
            response.put("error", "Can't load view template");
            return renderJSON(INTERNAL_ERROR_RESPONSE_CODE, response);
        }


        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private GoPluginApiResponse handleRequestGetConfiguration() {
        Map<String, Object> response = new HashMap<String, Object>();
        Map<String, Object> serverUrlParams = new HashMap<String, Object>();
        serverUrlParams.put("display-name", "External GoCD Server URL");

        response.put("server-url-external", serverUrlParams);

        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private GoPluginApiResponse handleNotificationsInterestedIn() {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("notifications", Arrays.asList(REQUEST_STAGE_STATUS));
        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private GoPluginApiResponse handleStageNotification(GoPluginApiRequest goPluginApiRequest) {
        GoNotificationMessage message = parseNotificationMessage(goPluginApiRequest);
        int responseCode = SUCCESS_RESPONSE_CODE;

        Map<String, Object> response = new HashMap<String, Object>();
        List<String> messages = new ArrayList<String>();
        try {
            response.put("status", "success");
            LOGGER.info(message.fullyQualifiedJobName() + " has " + message.getStageState() + "/" + message.getStageResult());
            lock.readLock().lock();
            rules.getPipelineListener().notify(message);
        } catch (Exception e) {
            LOGGER.info(message.fullyQualifiedJobName() + " failed with error", e);
            responseCode = INTERNAL_ERROR_RESPONSE_CODE;
            response.put("status", "failure");
            if (!isEmpty(e.getMessage())) {
                messages.add(e.getMessage());
            }
        } finally {
            lock.readLock().unlock();
        }

        if (!messages.isEmpty()) {
            response.put("messages", messages);
        }
        return renderJSON(responseCode, response);
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private GoNotificationMessage parseNotificationMessage(GoPluginApiRequest goPluginApiRequest) {
        return new GsonBuilder().create().fromJson(goPluginApiRequest.requestBody(), GoNotificationMessage.class);
    }

    private GoPluginApiResponse renderJSON(final int responseCode, Object response) {
        final String json = response == null ? null : new GsonBuilder().disableHtmlEscaping().create().toJson(response);
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
