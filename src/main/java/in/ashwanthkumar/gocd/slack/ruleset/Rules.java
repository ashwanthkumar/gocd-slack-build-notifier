package in.ashwanthkumar.gocd.slack.ruleset;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.typesafe.config.Config;
import in.ashwanthkumar.gocd.slack.PipelineListener;
import in.ashwanthkumar.utils.collections.Lists;
import in.ashwanthkumar.utils.func.Function;
import in.ashwanthkumar.utils.func.Predicate;
import in.ashwanthkumar.utils.lang.StringUtils;
import in.ashwanthkumar.utils.lang.option.Option;

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
    private boolean displayMaterialChanges;

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

    public boolean getDisplayMaterialChanges() {
        return displayMaterialChanges;
    }

    public Rules setDisplayMaterialChanges(boolean displayMaterialChanges) {
        this.displayMaterialChanges = displayMaterialChanges;
        return this;
    }

    
    public PipelineListener getPipelineListener() {
        return pipelineListener;
    }

    public Option<PipelineRule> find(final String pipeline, final String stage, final String pipelineStatus) {
        return Lists.find(pipelineRules, new Predicate<PipelineRule>() {
            public Boolean apply(PipelineRule input) {
                return input.matches(pipeline, stage, pipelineStatus);
            }
        });
    }

    public static Rules fromConfig(Config config) {
        boolean isEnabled = config.getBoolean("enabled");

        String webhookUrl = config.getString("webhookUrl");
        String channel = null;
        if (config.hasPath("channel")) {
            channel = config.getString("channel");
        }

        String displayName = "gocd-slack-bot";
        if(config.hasPath("slackDisplayName")) {
            displayName = config.getString("slackDisplayName");
        }

        String iconURL = "https://raw.githubusercontent.com/ashwanthkumar/assets/c597777ee749c89fec7ce21304d727724a65be7d/images/gocd-logo.png";
        if(config.hasPath("slackUserIconURL")) {
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
	boolean displayMaterialChanges = true;
	if (config.hasPath("displayMaterialChanges")) {
	    displayMaterialChanges = config.getBoolean("displayMaterialChanges");
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
                .setDisplayMaterialChanges(displayMaterialChanges);
        try {
            rules.pipelineListener = Class.forName(config.getString("listener")).asSubclass(PipelineListener.class).getConstructor(Rules.class).newInstance(rules);
        } catch (Exception e) {
            LOGGER.error("Exception while initializing pipeline listener", e);
            throw new RuntimeException(e);
        }

        return rules;
    }
}
