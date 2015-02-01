package in.ashwanthkumar.gocd.slack.ruleset;

import com.typesafe.config.Config;
import in.ashwanthkumar.gocd.slack.PipelineListener;
import in.ashwanthkumar.utils.collections.Lists;
import in.ashwanthkumar.utils.func.Function;
import in.ashwanthkumar.utils.func.Predicate;
import in.ashwanthkumar.utils.lang.option.Option;

import java.util.ArrayList;
import java.util.List;

import static in.ashwanthkumar.gocd.slack.ruleset.PipelineRule.merge;

public class Rules {
    private boolean enabled;
    private String slackChannel;
    private String webHookUrl;
    private String goServerHost;
    private List<PipelineRule> pipelineRules = new ArrayList<PipelineRule>();
    private PipelineListener pipelineListener;

    public boolean isEnabled() {
        return enabled;
    }

    public Rules setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getSlackChannel() {
        return slackChannel;
    }

    public Rules setSlackChannel(String slackChannel) {
        this.slackChannel = slackChannel;
        return this;
    }

    public String getWebHookUrl() {
        return webHookUrl;
    }

    public Rules setWebHookUrl(String webHookUrl) {
        this.webHookUrl = webHookUrl;
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

    public PipelineListener getPipelineListener() {
        return pipelineListener;
    }

    public Option<PipelineRule> find(final String pipeline, final String stage, final String pipelineStatus) {
        return Lists.find(pipelineRules, new Predicate<PipelineRule>() {
            @Override
            public Boolean apply(PipelineRule input) {
                return input.matches(pipeline, stage, pipelineStatus);
            }
        });
    }

    public static Rules fromConfig(Config config) {
        boolean isEnabled = config.getBoolean("enabled");
        String channel = null;
        if (config.hasPath("channel")) {
            channel = config.getString("channel");
        }
        String webhookUrl = config.getString("webhookUrl");
        String serverHost = config.getString("server-host");
        final PipelineRule defaultRule = PipelineRule.fromConfig(config.getConfig("default"), channel);

        List<PipelineRule> pipelineRules = Lists.map((List<Config>) config.getConfigList("pipelines"), new Function<Config, PipelineRule>() {
            @Override
            public PipelineRule apply(Config input) {
                return merge(PipelineRule.fromConfig(input), defaultRule);
            }
        });

        Rules rules = new Rules()
                .setEnabled(isEnabled)
                .setSlackChannel(channel)
                .setWebHookUrl(webhookUrl)
                .setPipelineRules(pipelineRules)
                .setGoServerHost(serverHost);
        try {
            rules.pipelineListener = Class.forName(config.getString("listener")).asSubclass(PipelineListener.class).getConstructor(Rules.class).newInstance(rules);
        } catch (Exception ignore) {
        }

        return rules;
    }
}
