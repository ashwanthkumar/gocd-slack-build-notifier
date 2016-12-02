package in.ashwanthkumar.gocd.slack.ruleset;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.typesafe.config.Config;
import in.ashwanthkumar.gocd.slack.PipelineListener;
import in.ashwanthkumar.utils.collections.Lists;
import in.ashwanthkumar.utils.func.Function;
import in.ashwanthkumar.utils.func.Predicate;
import in.ashwanthkumar.utils.lang.StringUtils;
import in.ashwanthkumar.utils.lang.option.Option;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

import static in.ashwanthkumar.gocd.slack.ruleset.PipelineRule.merge;

public class Rules {

    private static Logger LOGGER = Logger.getLoggerFor(Rules.class);

    private boolean enabled;
    private String webHookUrl;
    private String slackChannel;
    private String slackDisplayName;
    private String slackUserIconURL;
    private String goServerHost;
    private String goAPIServerHost;
    private String goLogin;
    private String goPassword;
    private boolean displayConsoleLogLinks;
    private boolean displayMaterialChanges;
    private boolean processAllRules;
    private boolean truncateChanges;

    private Proxy proxy;

    private List<PipelineRule> pipelineRules = new ArrayList<PipelineRule>();
    private PipelineListener pipelineListener;

    public boolean isEnabled() {
        return enabled;
    }

    public Rules setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getWebHookUrl() {
        return webHookUrl;
    }

    public Rules setWebHookUrl(String webHookUrl) {
        this.webHookUrl = webHookUrl;
        return this;
    }

    public String getSlackChannel() {
        return slackChannel;
    }

    public Rules setSlackChannel(String slackChannel) {
        this.slackChannel = slackChannel;
        return this;
    }

    public String getSlackDisplayName() {
        return slackDisplayName;
    }

    private Rules setSlackDisplayName(String displayName) {
        this.slackDisplayName = displayName;
        return this;
    }

    public String getSlackUserIcon() {
        return slackUserIconURL;
    }

    private Rules setSlackUserIcon(String iconURL) {
        this.slackUserIconURL = iconURL;
        return this;
    }

    public List<PipelineRule> getPipelineRules() {
        return pipelineRules;
    }

    public Rules setPipelineRules(List<PipelineRule> pipelineRules) {
        this.pipelineRules = pipelineRules;
        return this;
    }

    public String getGoServerHost() {
        return goServerHost;
    }

    public Rules setGoServerHost(String goServerHost) {
        this.goServerHost = goServerHost;
        return this;
    }


    public String getGoAPIServerHost() {
        if (StringUtils.isNotEmpty(goAPIServerHost)) {
            return goAPIServerHost;
        }
        return getGoServerHost();
    }

    public Rules setGoAPIServerHost(String goAPIServerHost) {
        this.goAPIServerHost = goAPIServerHost;
        return this;
    }

    public String getGoLogin() {
        return goLogin;
    }

    public Rules setGoLogin(String goLogin) {
        this.goLogin = goLogin;
        return this;
    }

    public String getGoPassword() {
        return goPassword;
    }

    public Rules setGoPassword(String goPassword) {
        this.goPassword = goPassword;
        return this;
    }

    public boolean getDisplayConsoleLogLinks() {
        return displayConsoleLogLinks;
    }

    public Rules setDisplayConsoleLogLinks(boolean displayConsoleLogLinks) {
        this.displayConsoleLogLinks = displayConsoleLogLinks;
        return this;
    }

    public boolean getDisplayMaterialChanges() {
        return displayMaterialChanges;
    }

    public Rules setDisplayMaterialChanges(boolean displayMaterialChanges) {
        this.displayMaterialChanges = displayMaterialChanges;
        return this;
    }

    public boolean getProcessAllRules() {
        return processAllRules;
    }

    public Rules setProcessAllRules(boolean processAllRules) {
        this.processAllRules = processAllRules;
        return this;
    }

    public boolean isTruncateChanges() {
        return truncateChanges;
    }

    public Rules setTruncateChanges(boolean truncateChanges) {
        this.truncateChanges = truncateChanges;
        return this;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public Rules setProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    public PipelineListener getPipelineListener() {
        return pipelineListener;
    }

    public List<PipelineRule> find(final String pipeline, final String stage, final String pipelineStatus) {
        Predicate<PipelineRule> predicate = new Predicate<PipelineRule>() {
            public Boolean apply(PipelineRule input) {
                return input.matches(pipeline, stage, pipelineStatus);
            }
        };

        if(processAllRules) {
            return Lists.filter(pipelineRules, predicate);
        } else {
            List<PipelineRule> found = new ArrayList<PipelineRule>();
            Option<PipelineRule> match = Lists.find(pipelineRules, predicate);
            if(match.isDefined()) {
                found.add(match.get());
            }
            return found;
        }
    }

    public static Rules fromConfig(Config config) {
        boolean isEnabled = config.getBoolean("enabled");

        String webhookUrl = config.getString("webhookUrl");
        String channel = null;
        if (config.hasPath("channel")) {
            channel = config.getString("channel");
        }

        String displayName = "gocd-slack-bot";
        if (config.hasPath("slackDisplayName")) {
            displayName = config.getString("slackDisplayName");
        }

        String iconURL = "https://raw.githubusercontent.com/ashwanthkumar/assets/c597777ee749c89fec7ce21304d727724a65be7d/images/gocd-logo.png";
        if (config.hasPath("slackUserIconURL")) {
            iconURL = config.getString("slackUserIconURL");
        }

        String serverHost = config.getString("server-host");
        String apiServerHost = null;
        if (config.hasPath("api-server-host")) {
            apiServerHost = config.getString("api-server-host");
        }
        String login = null;
        if (config.hasPath("login")) {
            login = config.getString("login");
        }
        String password = null;
        if (config.hasPath("password")) {
            password = config.getString("password");
        }

        boolean displayConsoleLogLinks = true;
        if (config.hasPath("display-console-log-links")) {
            displayConsoleLogLinks = config.getBoolean("display-console-log-links");
        }

        // TODO - Next major release - change this to - separated config
        boolean displayMaterialChanges = true;
        if (config.hasPath("displayMaterialChanges")) {
            displayMaterialChanges = config.getBoolean("displayMaterialChanges");
        }

        boolean processAllRules = false;
        if (config.hasPath("process-all-rules")) {
            processAllRules = config.getBoolean("process-all-rules");
        }

        boolean truncateChanges = true;
        if(config.hasPath("truncate-changes")) {
            truncateChanges = config.getBoolean("truncate-changes");
        }

        Proxy proxy = null;
        if (config.hasPath("proxy")) {
            Config proxyConfig = config.getConfig("proxy");
            if (proxyConfig.hasPath("hostname") && proxyConfig.hasPath("port") && proxyConfig.hasPath("type")) {
                String hostname = proxyConfig.getString("hostname");
                int port = proxyConfig.getInt("port");
                String type = proxyConfig.getString("type").toUpperCase();
                Proxy.Type proxyType = Proxy.Type.valueOf(type);
                proxy = new Proxy(proxyType, new InetSocketAddress(hostname, port));
            }
        }

        final PipelineRule defaultRule = PipelineRule.fromConfig(config.getConfig("default"), channel);

        List<PipelineRule> pipelineRules = Lists.map((List<Config>) config.getConfigList("pipelines"), new Function<Config, PipelineRule>() {
            public PipelineRule apply(Config input) {
                return merge(PipelineRule.fromConfig(input), defaultRule);
            }
        });

        Rules rules = new Rules()
                .setEnabled(isEnabled)
                .setWebHookUrl(webhookUrl)
                .setSlackChannel(channel)
                .setSlackDisplayName(displayName)
                .setSlackUserIcon(iconURL)
                .setPipelineRules(pipelineRules)
                .setGoServerHost(serverHost)
                .setGoAPIServerHost(apiServerHost)
                .setGoLogin(login)
                .setGoPassword(password)
                .setDisplayConsoleLogLinks(displayConsoleLogLinks)
                .setDisplayMaterialChanges(displayMaterialChanges)
                .setProcessAllRules(processAllRules)
                .setTruncateChanges(truncateChanges)
                .setProxy(proxy);
        try {
            rules.pipelineListener = Class.forName(config.getString("listener")).asSubclass(PipelineListener.class).getConstructor(Rules.class).newInstance(rules);
        } catch (Exception e) {
            LOGGER.error("Exception while initializing pipeline listener", e);
            throw new RuntimeException(e);
        }

        return rules;
    }
}
