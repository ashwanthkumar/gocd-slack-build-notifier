package in.ashwanthkumar.gocd.slack;

import in.ashwanthkumar.gocd.slack.jsonapi.MaterialRevision;
import in.ashwanthkumar.gocd.slack.jsonapi.Modification;
import in.ashwanthkumar.gocd.slack.jsonapi.Pipeline;
import in.ashwanthkumar.gocd.slack.ruleset.PipelineRule;
import in.ashwanthkumar.gocd.slack.ruleset.PipelineStatus;
import in.ashwanthkumar.gocd.slack.ruleset.Rules;
import in.ashwanthkumar.slack.webhook.Slack;
import in.ashwanthkumar.slack.webhook.SlackAttachment;

import java.net.URISyntaxException;
import java.util.List;

import com.thoughtworks.go.plugin.api.logging.Logger;

import static in.ashwanthkumar.slack.webhook.util.StringUtils.startsWith;

public class SlackPipelineListener extends PipelineListener {
    private Logger LOG = Logger.getLoggerFor(SlackPipelineListener.class);

    private final Slack slack;

    public SlackPipelineListener(Rules rules) {
        super(rules);
        slack = new Slack(rules.getWebHookUrl());
        updateSlackChannel(rules.getSlackChannel());

        slack.displayName(rules.getSlackDisplayName())
            .icon(rules.getSlackUserIcon());
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
        StringBuilder sb = new StringBuilder();

        // Describe the build.
        try {
            Pipeline details = message.fetchDetails(rules);
            String triggerMessage = details.buildCause.triggerMessage;
            triggerMessage =
                // Capitalize first letter. Really the shortest way:
                // http://stackoverflow.com/questions/3904579
                triggerMessage.substring(0,1).toUpperCase()
                + triggerMessage.substring(1);
            sb.append(triggerMessage);
            sb.append(". ");
        } catch (Exception e) {
            sb.append("(Couldn't fetch build details; see server log.) ");
            LOG.warn("Couldn't fetch build details", e);
        }
        sb.append("See details - ");
        sb.append(message.goServerUrl(rules.getGoServerHost()));
        sb.append("\n");

        // Describe the root changes that made up this build.
        try {
            List<MaterialRevision> changes = message.fetchChanges(rules);
            for (MaterialRevision change : changes) {
                sb.append(change.material.description);
                sb.append("\n");
                for (Modification mod : change.modifications) {
                    String url = change.modificationUrl(mod);
                    if (url != null) {
                        // This would be nicer if our Slack library allowed
                        // us to use formatted attachements.
                        sb.append(url);
                        sb.append(" ");
                    } else if (mod.revision != null) {
                        sb.append(mod.revision);
                        sb.append(": ");
                    }
                    String comment = mod.summarizeComment();
                    if (comment != null) {
                        sb.append(comment);
                    }
                    if (mod.userName != null) {
                        sb.append(" - ");
                        sb.append(mod.userName);
                    }
                    sb.append("\n");
                }
            }
        } catch (Exception e) {
            sb.append("(Couldn't fetch changes; see server log.) ");
            LOG.warn("Couldn't fetch changes", e);
        }

        return new SlackAttachment(sb.toString())
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
