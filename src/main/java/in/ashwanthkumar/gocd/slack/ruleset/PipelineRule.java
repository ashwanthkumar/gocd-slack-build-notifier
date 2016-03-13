package in.ashwanthkumar.gocd.slack.ruleset;

import com.typesafe.config.Config;
import in.ashwanthkumar.utils.collections.Iterables;
import in.ashwanthkumar.utils.func.Predicate;
import in.ashwanthkumar.utils.lang.StringUtils;

import java.util.HashSet;
import java.util.Set;

import static in.ashwanthkumar.utils.lang.StringUtils.isEmpty;

public class PipelineRule {
    private String nameRegex;
    private String stageRegex;
    private String channel;
    private Set<PipelineStatus> status = new HashSet<PipelineStatus>();

    public PipelineRule() {
    }

    public PipelineRule(PipelineRule copy) {
        this.nameRegex = copy.nameRegex;
        this.stageRegex = copy.stageRegex;
        this.channel = copy.channel;
        this.status = copy.status;
    }

    public PipelineRule(String nameRegex, String stageRegex) {
        this.nameRegex = nameRegex;
        this.stageRegex = stageRegex;
    }

    public String getNameRegex() {
        return nameRegex;
    }

    public PipelineRule setNameRegex(String nameRegex) {
        this.nameRegex = nameRegex;
        return this;
    }

    public String getStageRegex() {
        return stageRegex;
    }

    public PipelineRule setStageRegex(String stageRegex) {
        this.stageRegex = stageRegex;
        return this;
    }

    public String getChannel() {
        return channel;
    }

    public PipelineRule setChannel(String channel) {
        this.channel = channel;
        return this;
    }

    public Set<PipelineStatus> getStatus() {
        return status;
    }

    public PipelineRule setStatus(Set<PipelineStatus> status) {
        this.status = status;
        return this;
    }

    public boolean matches(String pipeline, String stage, final String pipelineState) {
        return pipeline.matches(nameRegex) && stage.matches(stageRegex) && Iterables.exists(status, hasStateMatching(pipelineState));
    }

    private Predicate<PipelineStatus> hasStateMatching(final String pipelineState) {
        return new Predicate<PipelineStatus>() {
            @Override
            public Boolean apply(PipelineStatus input) {
                return input.matches(pipelineState);
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PipelineRule that = (PipelineRule) o;

        if (channel != null ? !channel.equals(that.channel) : that.channel != null) return false;
        if (nameRegex != null ? !nameRegex.equals(that.nameRegex) : that.nameRegex != null) return false;
        if (stageRegex != null ? !stageRegex.equals(that.stageRegex) : that.stageRegex != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = nameRegex != null ? nameRegex.hashCode() : 0;
        result = 31 * result + (stageRegex != null ? stageRegex.hashCode() : 0);
        result = 31 * result + (channel != null ? channel.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PipelineRule{" +
                "nameRegex='" + nameRegex + '\'' +
                ", stageRegex='" + stageRegex + '\'' +
                ", channel='" + channel + '\'' +
                ", status=" + status +
                '}';
    }

    public static PipelineRule fromConfig(Config config) {
        PipelineRule pipelineRule = new PipelineRule();
        pipelineRule.setNameRegex(config.getString("name"));
        if (config.hasPath("stage")) {
            pipelineRule.setStageRegex(config.getString("stage"));
        }
        if (config.hasPath("state")) {
            String stateT = config.getString("state");
            String[] states = stateT.split("\\|");
            Set<PipelineStatus> status = new HashSet<PipelineStatus>();
            for (String state : states) {
                status.add(PipelineStatus.valueOf(state.toUpperCase()));
            }
            pipelineRule.setStatus(status);
        }
        if (config.hasPath("channel")) {
            pipelineRule.setChannel(config.getString("channel"));
        }

        return pipelineRule;
    }

    public static PipelineRule fromConfig(Config config, String channel) {
        PipelineRule pipelineRule = fromConfig(config);
        if (StringUtils.isEmpty(pipelineRule.getChannel())) {
            pipelineRule.setChannel(channel);
        }
        return pipelineRule;
    }

    public static PipelineRule merge(PipelineRule pipelineRule, PipelineRule defaultRule) {
        PipelineRule ruleToReturn = new PipelineRule(pipelineRule);
        if (isEmpty(pipelineRule.getNameRegex())) {
            ruleToReturn.setNameRegex(defaultRule.getNameRegex());
        }
        if (isEmpty(pipelineRule.getStageRegex())) {
            ruleToReturn.setStageRegex(defaultRule.getStageRegex());
        }

        if (isEmpty(pipelineRule.getChannel())) {
            ruleToReturn.setChannel(defaultRule.getChannel());
        }

        if (pipelineRule.getStatus().isEmpty()) {
            ruleToReturn.setStatus(defaultRule.getStatus());
        } else {
            ruleToReturn.getStatus().addAll(pipelineRule.getStatus());
        }

        return ruleToReturn;
    }

}
