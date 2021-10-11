package in.ashwanthkumar.gocd.slack;

import com.thoughtworks.go.plugin.api.logging.Logger;
import in.ashwanthkumar.gocd.slack.ruleset.PipelineRule;
import in.ashwanthkumar.gocd.slack.ruleset.PipelineStatus;
import in.ashwanthkumar.gocd.slack.ruleset.Rules;
import in.ashwanthkumar.utils.lang.option.Option;

import java.util.List;

abstract public class PipelineListener {
    private static final Logger LOG = Logger.getLoggerFor(PipelineListener.class);
    protected Rules rules;

    public PipelineListener(Rules rules) {
        this.rules = rules;
    }

    public void notify(GoNotificationMessage message) throws Exception {
        message.tryToFixStageResult(rules);
        LOG.debug(String.format("Finding rules with state %s", message.getStageResult()));
        List<PipelineRule> foundRules = rules.find(message.getPipelineName(), message.getStageName(), message.getPipelineGroup(), message.getStageResult());
        if (foundRules.size() > 0) {
            for (PipelineRule pipelineRule : foundRules) {
                LOG.debug(String.format("Matching rule is %s", pipelineRule));
                handlePipelineStatus(pipelineRule, PipelineStatus.valueOf(message.getStageResult().toUpperCase()), message);
                if (! rules.getProcessAllRules()) {
                    break;
                }
            }
        } else {
            LOG.warn(String.format("Couldn't find any matching rule for %s/%s with status=%s", message.getPipelineName(), message.getStageName(), message.getStageResult()));
        }
    }

    protected void handlePipelineStatus(PipelineRule rule, PipelineStatus status, GoNotificationMessage message) throws Exception {
        status.handle(this, rule, message);
    }

    /**
     * Invoked when pipeline is BUILDING
     *
     * @param rule
     * @param message
     * @throws Exception
     */
    public abstract void onBuilding(PipelineRule rule, GoNotificationMessage message) throws Exception;

    /**
     * Invoked when pipeline PASSED
     *
     * @param message
     * @throws Exception
     */
    public abstract void onPassed(PipelineRule rule, GoNotificationMessage message) throws Exception;

    /**
     * Invoked when pipeline FAILED
     *
     * @param message
     * @throws Exception
     */
    public abstract void onFailed(PipelineRule rule, GoNotificationMessage message) throws Exception;

    /**
     * Invoked when pipeline is BROKEN
     *
     * Note - This currently is not implemented
     *
     * @param message
     * @throws Exception
     */
    public abstract void onBroken(PipelineRule rule, GoNotificationMessage message) throws Exception;

    /**
     * Invoked when pipeline is FIXED
     *
     * Note - This currently is not implemented
     *
     * @param message
     * @throws Exception
     */
    public abstract void onFixed(PipelineRule rule, GoNotificationMessage message) throws Exception;

    /**
     * Invoked when pipeline is CANCELLED
     *
     * @param message
     * @throws Exception
     */
    public abstract void onCancelled(PipelineRule rule, GoNotificationMessage message) throws Exception;
}
