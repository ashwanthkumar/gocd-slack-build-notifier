package in.ashwanthkumar.gocd.slack;

import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import in.ashwanthkumar.gocd.slack.base.AbstractNotificationPlugin;
import in.ashwanthkumar.gocd.slack.ruleset.Rules;
import in.ashwanthkumar.gocd.slack.ruleset.RulesReader;
import in.ashwanthkumar.utils.lang.StringUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Extension
public class GoNotificationPlugin extends AbstractNotificationPlugin<SlackPluginSettings> implements GoPlugin {
    public static final String CRUISE_SERVER_DIR = "CRUISE_SERVER_DIR";
    private static final long CONFIG_REFRESH_INTERVAL = 10 * 1000; // 10 seconds

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
        super();
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
        this.settings = new SlackPluginSettings();
    }

    // used for tests
    GoNotificationPlugin(GoEnvironment environment) {
        super();
        this.environment = environment;
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

    @Override
    public void handleStageNotification(GoNotificationMessage notification) throws Exception {
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
}
