package in.ashwanthkumar.gocd.slack;

import in.ashwanthkumar.gocd.slack.ruleset.PipelineRule;
import in.ashwanthkumar.gocd.slack.ruleset.PipelineStatus;
import in.ashwanthkumar.gocd.slack.ruleset.Rules;
import in.ashwanthkumar.slack.webhook.Slack;
import in.ashwanthkumar.slack.webhook.SlackAttachment;

import java.net.URISyntaxException;

import static in.ashwanthkumar.slack.webhook.util.StringUtils.startsWith;

public class SlackPipelineListener extends PipelineListener {

    private final Slack slack;

    public SlackPipelineListener(Rules rules) {
        super(rules);
        slack = new Slack(rules.getWebHookUrl());
        updateSlackChannel(rules.getSlackChannel());

        // TODO - Make these configurable
        slack.displayName("gocd-slack-bot")
                .icon("https://raw.githubusercontent.com/ashwanthkumar/assets/c597777ee749c89fec7ce21304d727724a65be7d/images/gocd-logo.png");
    }

    @Override
    public void onBuilding(PipelineRule rule, GoNotificationMessage message) throws Exception {
        updateSlackChannel(rule.getChannel());
        slack.push(slackAttachment(message, PipelineStatus.BUILDING));
    }

    @Override
    public void onPassed(PipelineRule rule, GoNotificationMessage message) throws Exception {
        updateSlackChannel(rule.getChannel());
        slack.push(slackAttachment(message, PipelineStatus.PASSED));
    }

    @Override
    public void onFailed(PipelineRule rule, GoNotificationMessage message) throws Exception {
        updateSlackChannel(rule.getChannel());
        slack.push(slackAttachment(message, PipelineStatus.FAILED));
    }

    @Override
    public void onBroken(PipelineRule rule, GoNotificationMessage message) throws Exception {
        updateSlackChannel(rule.getChannel());
        slack.push(slackAttachment(message, PipelineStatus.BROKEN));
    }

    @Override
    public void onFixed(PipelineRule rule, GoNotificationMessage message) throws Exception {
        updateSlackChannel(rule.getChannel());
        slack.push(slackAttachment(message, PipelineStatus.FIXED));
    }

    @Override
    public void onCancelled(PipelineRule rule, GoNotificationMessage message) throws Exception {
        updateSlackChannel(rule.getChannel());
        slack.push(slackAttachment(message, PipelineStatus.CANCELLED));
    }

    private SlackAttachment slackAttachment(GoNotificationMessage message, PipelineStatus pipelineStatus) throws URISyntaxException {
        String messageText = "See details - " + message.goServerUrl(rules.getGoServerHost());
        return new SlackAttachment(messageText)
                .fallback(String.format("%s %s %s", message.fullyQualifiedJobName(), verbFor(pipelineStatus), pipelineStatus).replaceAll("\\s+", " "))
                .title(String.format("Stage [%s] %s %s", message.fullyQualifiedJobName(), verbFor(pipelineStatus), pipelineStatus).replaceAll("\\s+", " "));
    }

    private String verbFor(PipelineStatus pipelineStatus) {
        switch (pipelineStatus) {
            case BROKEN:
            case FIXED:
                return "is";
            case FAILED:
            case PASSED:
                return "has";
            case CANCELLED:
                return "was";
            default:
                return "";
        }
    }

    private void updateSlackChannel(String slackChannel) {
        // by default post it to where ever the hook is configured to do so
        if (startsWith(slackChannel, "#")) {
            slack.sendToChannel(slackChannel.substring(1));
        } else if (startsWith(slackChannel, "@")) {
            slack.sendToUser(slackChannel.substring(1));
        }
    }
}
