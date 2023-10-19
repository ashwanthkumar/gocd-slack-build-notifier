package in.ashwanthkumar.gocd.slack.ruleset;

import com.typesafe.config.Config;
import in.ashwanthkumar.utils.collections.Iterables;
import in.ashwanthkumar.utils.collections.Lists;
import in.ashwanthkumar.utils.func.Predicate;
import in.ashwanthkumar.utils.lang.StringUtils;

import java.util.*;

import static in.ashwanthkumar.utils.lang.StringUtils.isEmpty;

public class PipelineRule {
    private String nameRegex;
    private String stageRegex;
    private String groupRegex;
    private String labelRegex;
    private String channel;
    private String webhookUrl;
    private Set<String> owners = new HashSet<>();
    private Set<PipelineStatus> status = new HashSet<>();

    public PipelineRule() {
    }

    public PipelineRule(PipelineRule copy) {
        this.nameRegex = copy.nameRegex;
        this.stageRegex = copy.stageRegex;
        this.groupRegex = copy.groupRegex;
        this.labelRegex = copy.labelRegex;
        this.channel = copy.channel;
        this.status = copy.status;
        this.owners = copy.owners;
        this.webhookUrl = copy.webhookUrl;
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

    public String getGroupRegex() {
        return groupRegex;
    }

    public PipelineRule setGroupRegex(String groupRegex) {
        this.groupRegex = groupRegex;
        return this;
    }
    
    public String getStageRegex() {
        return stageRegex;
    }

    public PipelineRule setStageRegex(String stageRegex) {
        this.stageRegex = stageRegex;
        return this;
    }
    
    public String getLabelRegex() {
        return labelRegex;
    }

    public PipelineRule setLabelRegex(String labelRegex) {
        this.labelRegex = labelRegex;
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

    public Set<String> getOwners() {
        return owners;
    }

    public PipelineRule setOwners(Set<String> owners) {
        this.owners = owners;
        return this;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public PipelineRule setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        return this;
    }

    public boolean matches(String pipeline, String stage, String group, String label, final String pipelineState) {
        return pipeline.matches(nameRegex)
                && stage.matches(stageRegex)
                && matchesGroup(group)
                && Iterables.exists(status, hasStateMatching(pipelineState))
                && (StringUtils.isNotEmpty(labelRegex) ? label.matches(labelRegex) : true);
    }

    private boolean matchesGroup(String group) {
        return StringUtils.isEmpty(groupRegex) || group.matches(groupRegex);
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
        if (groupRegex != null ? !groupRegex.equals(that.groupRegex) : that.groupRegex != null) return false;
        if (labelRegex != null ? !labelRegex.equals(that.labelRegex) : that.labelRegex != null) return false;
        if (stageRegex != null ? !stageRegex.equals(that.stageRegex) : that.stageRegex != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (owners != null ? !owners.equals(that.owners) : that.owners != null) return false;
        if (webhookUrl != null ? !webhookUrl.equals(that.webhookUrl) : that.webhookUrl != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = nameRegex != null ? nameRegex.hashCode() : 0;
        result = 31 * result + (groupRegex != null ? groupRegex.hashCode() : 0);
        result = 31 * result + (stageRegex != null ? stageRegex.hashCode() : 0);
        result = 31 * result + (labelRegex != null ? labelRegex.hashCode() : 0);
        result = 31 * result + (channel != null ? channel.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (owners != null ? owners.hashCode() : 0);
        result = 31 * result + (webhookUrl != null ? webhookUrl.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PipelineRule{" +
                "nameRegex='" + nameRegex + '\'' +
                ", groupRegex='" + groupRegex + '\'' +
                ", stageRegex='" + stageRegex + '\'' +
                ", labelRegex='" + labelRegex + '\'' +
                ", channel='" + channel + '\'' +
                ", status=" + status +
                ", owners=" + owners +
                ", webhookUrl=" + webhookUrl +
                '}';
    }

    public static PipelineRule fromConfig(Config config) {
        PipelineRule pipelineRule = new PipelineRule();
        pipelineRule.setNameRegex(config.getString("name"));
        if (config.hasPath("group")) {
            pipelineRule.setGroupRegex(config.getString("group"));
        }
        if (config.hasPath("stage")) {
            pipelineRule.setStageRegex(config.getString("stage"));
        }
        if (config.hasPath("label")) {
            pipelineRule.setLabelRegex(config.getString("label"));
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
        if (config.hasPath("webhookUrl")) {
            pipelineRule.setWebhookUrl(config.getString("webhookUrl"));
        }
        if (config.hasPath("owners")) {
            List<String> nonEmptyOwners = Lists.filter(config.getStringList("owners"), new Predicate<String>() {
                @Override
                public Boolean apply(String input) {
                    return StringUtils.isNotEmpty(input);
                }
            });
            pipelineRule.getOwners().addAll(nonEmptyOwners);
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

        if (isEmpty(pipelineRule.getGroupRegex())) {
            ruleToReturn.setGroupRegex(defaultRule.getGroupRegex());
        }

        if (isEmpty(pipelineRule.getStageRegex())) {
            ruleToReturn.setStageRegex(defaultRule.getStageRegex());
        }
        
        if (isEmpty(pipelineRule.getLabelRegex())) {
            ruleToReturn.setLabelRegex(defaultRule.getLabelRegex());
        }

        if (isEmpty(pipelineRule.getChannel())) {
            ruleToReturn.setChannel(defaultRule.getChannel());
        }

        if (isEmpty(pipelineRule.getWebhookUrl())) {
            ruleToReturn.setWebhookUrl(defaultRule.getWebhookUrl());
        }

        if (pipelineRule.getStatus().isEmpty()) {
            ruleToReturn.setStatus(defaultRule.getStatus());
        } else {
            ruleToReturn.getStatus().addAll(pipelineRule.getStatus());
        }

        if (pipelineRule.getOwners().isEmpty()) {
            ruleToReturn.setOwners(defaultRule.getOwners());
        } else {
            ruleToReturn.getOwners().addAll(pipelineRule.getOwners());
        }

        return ruleToReturn;
    }

}
