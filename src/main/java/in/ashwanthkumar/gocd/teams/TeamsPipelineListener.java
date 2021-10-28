package in.ashwanthkumar.gocd.teams;

import com.thoughtworks.go.plugin.api.logging.Logger;
import in.ashwanthkumar.gocd.slack.GoNotificationMessage;
import in.ashwanthkumar.gocd.slack.PipelineListener;
import in.ashwanthkumar.gocd.slack.jsonapi.Pipeline;
import in.ashwanthkumar.gocd.slack.jsonapi.Stage;
import in.ashwanthkumar.gocd.slack.ruleset.PipelineRule;
import in.ashwanthkumar.gocd.slack.ruleset.PipelineStatus;
import in.ashwanthkumar.gocd.slack.ruleset.Rules;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * To enable this for Teams support add the following line to your config:
 * listener = "in.ashwanthkumar.gocd.teams.TeamsPipelineListener"
 *
 * @see in.ashwanthkumar.gocd.slack.SlackPipelineListener
 */
public class TeamsPipelineListener extends PipelineListener {
    private static final Logger LOG = Logger.getLoggerFor(TeamsPipelineListener.class);
    private final TeamsWebhook teams;

    public TeamsPipelineListener(Rules rules) {
        super(rules);
        teams = new TeamsWebhook(rules.getProxy());
    }

    private String getWebhook(PipelineRule rule) {
        final String ruleWebhook = rule.getWebhookUrl();
        if (ruleWebhook != null && !ruleWebhook.isEmpty()) {
            return ruleWebhook;
        } else {
            return this.rules.getWebHookUrl();
        }
    }

    private void sendMessage(PipelineRule rule, GoNotificationMessage message, PipelineStatus status)
            throws GoNotificationMessage.BuildDetailsNotFoundException, URISyntaxException, IOException {
        final TeamsCard card = new TeamsCard();
        card.setColor(MessageCardSchema.Color.findColor(status));
        card.addLinkAction("Details", message.goServerUrl(rules.getGoServerHost()));
        card.setTitle(String.format("Stage [%s] %s %s",
                        message.fullyQualifiedJobName(),
                        status.verb(),
                        status)
                .replaceAll("\\s+", " "));

        Pipeline details = message.fetchDetails(rules);
        Stage stage = message.pickCurrentStage(details.stages);

        card.addFact("Triggered by", stage.approvedBy);
        card.addFact("Reason", details.buildCause.triggerMessage);
        card.addFact("Label", details.label);

        teams.send(getWebhook(rule), card);
    }


    @Override
    public void onBuilding(PipelineRule rule, GoNotificationMessage message) throws Exception {
        sendMessage(rule, message, PipelineStatus.BUILDING);
    }

    @Override
    public void onPassed(PipelineRule rule, GoNotificationMessage message) throws Exception {
        sendMessage(rule, message, PipelineStatus.PASSED);
    }

    @Override
    public void onFailed(PipelineRule rule, GoNotificationMessage message) throws Exception {
        sendMessage(rule, message, PipelineStatus.FAILED);
    }

    @Override
    public void onBroken(PipelineRule rule, GoNotificationMessage message) throws Exception {
        sendMessage(rule, message, PipelineStatus.BROKEN);
    }

    @Override
    public void onFixed(PipelineRule rule, GoNotificationMessage message) throws Exception {
        sendMessage(rule, message, PipelineStatus.FIXED);
    }

    @Override
    public void onCancelled(PipelineRule rule, GoNotificationMessage message) throws Exception {
        sendMessage(rule, message, PipelineStatus.CANCELLED);
    }
}
