package in.ashwanthkumar.gocd.slack;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import in.ashwanthkumar.gocd.slack.base.AbstractNotificationPlugin;
import in.ashwanthkumar.gocd.slack.ruleset.Rules;
import in.ashwanthkumar.gocd.slack.ruleset.RulesReader;
import in.ashwanthkumar.utils.lang.StringUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.Arrays.asList;

@Extension
public class GoNotificationPlugin extends AbstractNotificationPlugin implements GoPlugin {
    public static final String CRUISE_SERVER_DIR = "CRUISE_SERVER_DIR";
    private static final long CONFIG_REFRESH_INTERVAL = 10 * 1000; // 10 seconds


    public static final String REQUEST_NOTIFICATIONS_INTERESTED_IN = "notifications-interested-in";
    public static final String REQUEST_GET_CONFIGURATION = "go.plugin-settings.get-configuration";
    public static final String REQUEST_GET_VIEW = "go.plugin-settings.get-view";
    public static final String REQUEST_VALIDATE_CONFIGURATION = "go.plugin-settings.validate-configuration";

    public static final int SUCCESS_RESPONSE_CODE = 200;
    public static final int INTERNAL_ERROR_RESPONSE_CODE = 500;

    public static final String GO_NOTIFY_CONF = "GO_NOTIFY_CONF";
    public static final String CONFIG_FILE_NAME = "go_notify.conf";
    public static final String HOME_PLUGIN_CONFIG_PATH = System.getProperty("user.home") + File.separator + CONFIG_FILE_NAME;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private GoEnvironment environment = new GoEnvironment();
    private Rules rules;

    private final Timer timer = new Timer();
    private long configLastModified = 0l;
    private File pluginConfig;

    public GoNotificationPlugin() {
        pluginConfig = findGoNotifyConfigPath();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (pluginConfig.lastModified() != configLastModified) {
                    if (configLastModified == 0l) {
                        LOGGER.info("Loading configuration file");
                    } else {
                        LOGGER.info("Reloading configuration file since some modifications were found");
                    }
                    try {
                        lock.writeLock().lock();
                        rules = RulesReader.read(pluginConfig);
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    } finally {
                        lock.writeLock().unlock();
                    }
                    configLastModified = pluginConfig.lastModified();
                }
            }
        }, 0, CONFIG_REFRESH_INTERVAL);
    }

    // used for tests
    GoNotificationPlugin(GoEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public void handleStageNotification(GoNotificationMessage notification) throws Exception{
        try {
            LOGGER.info(notification.fullyQualifiedJobName() + " has " + notification.getStageState() + "/" + notification.getStageResult());
            lock.readLock().lock();
            rules.getPipelineListener().notify(notification);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public String template() throws Exception {
        return IOUtils.toString(getClass().getResourceAsStream("/views/config.template.html"), "UTF-8");
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

    protected GoPluginApiResponse handleValidateConfig(String requestBody) {
        List<Object> response = Arrays.asList();
        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private GoPluginApiResponse handleRequestGetConfiguration() {
        Map<String, Object> response = new TreeMap<>();
        response.put("server-url-external", configField("External GoCD Server URL", "", "1", true, false));
        response.put("pipelineConfig", configField("Pipeline Notification Rules", "", "2", true, false));
        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private File findGoNotifyConfigPath() {
        // case 1: Look for an environment variable by GO_NOTIFY_CONF and if a file identified by the value exist
        String goNotifyConfPath = environment.getenv(GO_NOTIFY_CONF);
        if (StringUtils.isNotEmpty(goNotifyConfPath)) {
            File pluginConfig = new File(goNotifyConfPath);
            if (pluginConfig.exists()) {
                LOGGER.info(String.format("Configuration file found using GO_NOTIFY_CONF at %s", pluginConfig.getAbsolutePath()));
                return pluginConfig;
            }
        }
        // case 2: Look for a file called go_notify.conf in the home folder
        File pluginConfig = new File(HOME_PLUGIN_CONFIG_PATH);
        if (pluginConfig.exists()) {
            LOGGER.info(String.format("Configuration file found at Home Dir as %s", pluginConfig.getAbsolutePath()));
            return pluginConfig;
        }
        // case 3: Look for a file - go_notify.conf in the current working directory of the server
        String goServerDir = environment.getenv(CRUISE_SERVER_DIR);
        pluginConfig = new File(goServerDir + File.separator + CONFIG_FILE_NAME);
        if (pluginConfig.exists()) {
            LOGGER.info(String.format("Configuration file found using CRUISE_SERVER_DIR at %s", pluginConfig.getAbsolutePath()));
            return pluginConfig;
        }

        throw new RuntimeException("Unable to find go_notify.conf. Please make sure you've set it up right.");
    }
}
