package in.ashwanthkumar.gocd.slack;

import in.ashwanthkumar.gocd.slack.jsonapi.MaterialRevision;
import in.ashwanthkumar.gocd.slack.jsonapi.Modification;
import in.ashwanthkumar.gocd.slack.jsonapi.Pipeline;
import in.ashwanthkumar.gocd.slack.jsonapi.Stage;
import in.ashwanthkumar.gocd.slack.ruleset.PipelineRule;
import in.ashwanthkumar.gocd.slack.ruleset.PipelineStatus;
import in.ashwanthkumar.gocd.slack.ruleset.Rules;
import in.ashwanthkumar.slack.webhook.Slack;
import in.ashwanthkumar.slack.webhook.SlackAttachment;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.go.plugin.api.logging.Logger;
import in.ashwanthkumar.utils.collections.Lists;
import in.ashwanthkumar.utils.func.Function;
import in.ashwanthkumar.utils.lang.StringUtils;

import static in.ashwanthkumar.utils.lang.StringUtils.startsWith;

public class SlackPipelineListener extends PipelineListener {
    public static final int MAX_CHANGES_PER_MATERIAL_IN_SLACK = 5;
    private Logger LOG = Logger.getLoggerFor(SlackPipelineListener.class);

    private final Slack slack;

    public SlackPipelineListener(Rules rules) {
        super(rules);
        slack = new Slack(rules.getWebHookUrl(), rules.getProxy());
        updateSlackChannel(rules.getSlackChannel());

        slack.displayName(rules.getSlackDisplayName())
                .icon(rules.getSlackUserIcon());
    }

    @Override
    public void onBuilding(PipelineRule rule, GoNotificationMessage message) throws Exception {
        updateSlackChannel(rule.getChannel());
        updateWebhookUrl(rule.getWebhookUrl());
        slack.push(slackAttachment(rule, message, PipelineStatus.BUILDING));
    }

    @Override
    public void onPassed(PipelineRule rule, GoNotificationMessage message) throws Exception {
        updateSlackChannel(rule.getChannel());
        updateWebhookUrl(rule.getWebhookUrl());
        slack.push(slackAttachment(rule, message, PipelineStatus.PASSED).color("good"));
    }

    @Override
    public void onFailed(PipelineRule rule, GoNotificationMessage message) throws Exception {
        updateSlackChannel(rule.getChannel());
        updateWebhookUrl(rule.getWebhookUrl());
        slack.push(slackAttachment(rule, message, PipelineStatus.FAILED).color("danger"));
    }

    @Override
    public void onBroken(PipelineRule rule, GoNotificationMessage message) throws Exception {
        updateSlackChannel(rule.getChannel());
        updateWebhookUrl(rule.getWebhookUrl());
        slack.push(slackAttachment(rule, message, PipelineStatus.BROKEN).color("danger"));
    }

    @Override
    public void onFixed(PipelineRule rule, GoNotificationMessage message) throws Exception {
        updateSlackChannel(rule.getChannel());
        updateWebhookUrl(rule.getWebhookUrl());
        slack.push(slackAttachment(rule, message, PipelineStatus.FIXED).color("good"));
    }

    @Override
    public void onCancelled(PipelineRule rule, GoNotificationMessage message) throws Exception {
        updateSlackChannel(rule.getChannel());
        updateWebhookUrl(rule.getWebhookUrl());
        slack.push(slackAttachment(rule, message, PipelineStatus.CANCELLED).color("warning"));
    }

    private SlackAttachment slackAttachment(PipelineRule rule, GoNotificationMessage message, PipelineStatus pipelineStatus) throws URISyntaxException {
        String title = String.format("Stage [%s] %s %s", message.fullyQualifiedJobName(), verbFor(pipelineStatus), pipelineStatus).replaceAll("\\s+", " ");
        SlackAttachment buildAttachment = new SlackAttachment("")
                .fallback(title)
                .title(title, message.goServerUrl(rules.getGoServerHost()));

        List<String> consoleLogLinks = new ArrayList<String>();
        // Describe the build.
        try {
            Pipeline details = message.fetchDetails(rules);
            Stage stage = pickCurrentStage(details.stages, message);
            buildAttachment.addField(new SlackAttachment.Field("Triggered by", stage.approvedBy, true));
            if (details.buildCause.triggerForced) {
                buildAttachment.addField(new SlackAttachment.Field("Reason", "Manual Trigger", true));
            } else {
                buildAttachment.addField(new SlackAttachment.Field("Reason", details.buildCause.triggerMessage, true));
            }
            buildAttachment.addField(new SlackAttachment.Field("Label", details.label, true));
            if (rules.getDisplayConsoleLogs()) {
                consoleLogLinks = createConsoleLogLinks(rules.getGoServerHost(), details, stage, pipelineStatus);
            }
        } catch (Exception e) {
            buildAttachment.text("(Couldn't fetch build details; see server log.) ");
            LOG.warn("Couldn't fetch build details", e);
        }
        buildAttachment.addField(new SlackAttachment.Field("Status", pipelineStatus.name(), true));

        // Describe the root changes that made up this build.
        if (rules.getDisplayMaterialChanges()) {
            try {
                List<MaterialRevision> changes = message.fetchChanges(rules);
                StringBuilder sb = new StringBuilder();
                for (MaterialRevision change : changes) {
                    boolean isTruncated = false;
                    if (change.modifications.size() > MAX_CHANGES_PER_MATERIAL_IN_SLACK) {
                        change.modifications = Lists.take(change.modifications, MAX_CHANGES_PER_MATERIAL_IN_SLACK);
                        isTruncated = true;
                    }
                    for (Modification mod : change.modifications) {
                        String url = change.modificationUrl(mod);
                        if (url != null) {
                            sb.append("<").append(url).append("|").append(mod.revision).append(">");
                            sb.append(": ");
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
                    String fieldNamePrefix = (isTruncated) ? String.format("Latest %d", MAX_CHANGES_PER_MATERIAL_IN_SLACK) : "All";
                    String fieldName = String.format("%s changes for %s", fieldNamePrefix, change.material.description);
                    buildAttachment.addField(new SlackAttachment.Field(fieldName, sb.toString(), false));
                }
            } catch (Exception e) {
                buildAttachment.addField(new SlackAttachment.Field("Changes", "(Couldn't fetch changes; see server log.)", true));
                LOG.warn("Couldn't fetch changes", e);
            }
        }

        if (!consoleLogLinks.isEmpty()) {
            String logLinks = Lists.mkString(consoleLogLinks, "", "", "\n");
            buildAttachment.addField(new SlackAttachment.Field("Console Logs", logLinks, true));
        }

        if (!rule.getOwners().isEmpty()) {
            List<String> slackOwners = Lists.map(rule.getOwners(), new Function<String, String>() {
                @Override
                public String apply(String input) {
                    return String.format("<@%s>", input);
                }
            });
            buildAttachment.addField(new SlackAttachment.Field("Owners", Lists.mkString(slackOwners, ","), true));
        }
        return buildAttachment;
    }

    private List<String> createConsoleLogLinks(String host, Pipeline pipeline, Stage stage, PipelineStatus pipelineStatus) throws URISyntaxException {
        List<String> consoleLinks = new ArrayList<String>();
        for (String job : stage.jobNames()) {
            URI link;
            // We should be linking to Console Tab when the status is building,
            // while all others will be the console.log artifact.
            if (pipelineStatus == PipelineStatus.BUILDING) {
                link = new URI(String.format("%s/go/tab/build/detail/%s/%d/%s/%d/%s#tab-console", host, pipeline.name, pipeline.counter, stage.name, stage.counter, job));
            } else {
                link = new URI(String.format("%s/go/files/%s/%d/%s/%d/%s/cruise-output/console.log", host, pipeline.name, pipeline.counter, stage.name, stage.counter, job));
            }
            // TODO - May be it's only useful to show the failed job logs instead of all jobs?
            consoleLinks.add("<" + link.normalize().toASCIIString() + "| View " + job + " logs>");
        }
        return consoleLinks;
    }

    private Stage pickCurrentStage(Stage[] stages, GoNotificationMessage message) {
        for (Stage stage : stages) {
            if (message.getStageName().equals(stage.name)) {
                return stage;
            }
        }

        throw new IllegalArgumentException("The list of stages from the pipeline (" + message.getPipelineName() + ") doesn't have the active stage (" + message.getStageName() + ") for which we got the notification.");
    }

    private String verbFor(PipelineStatus pipelineStatus) {
        switch (pipelineStatus) {
            case BROKEN:
            case FIXED:
            case BUILDING:
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
        LOG.debug(String.format("Updating target slack channel to %s", slackChannel));
        // by default post it to where ever the hook is configured to do so
        if (startsWith(slackChannel, "#")) {
            slack.sendToChannel(slackChannel.substring(1));
        } else if (startsWith(slackChannel, "@")) {
            slack.sendToUser(slackChannel.substring(1));
        }
    }

    private void updateWebhookUrl(String webbookUrl) {
        LOG.debug(String.format("Updating target webhookUrl to %s", webbookUrl));
        // by default pick the global webhookUrl
        if (StringUtils.isNotEmpty(webbookUrl)) {
            slack.setWebhookUrl(webbookUrl);
        }
    }
}
