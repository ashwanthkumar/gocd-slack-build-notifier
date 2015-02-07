package in.ashwanthkumar.gocd.slack;

import com.thoughtworks.go.plugin.api.logging.Logger;
import in.ashwanthkumar.gocd.slack.ruleset.PipelineRule;
import in.ashwanthkumar.gocd.slack.ruleset.PipelineStatus;
import in.ashwanthkumar.gocd.slack.ruleset.Rules;
import in.ashwanthkumar.utils.lang.option.Option;

abstract public class PipelineListener {
    private Logger LOG = Logger.getLoggerFor(PipelineListener.class);
    protected Rules rules;

    public PipelineListener(Rules rules) {
        this.rules = rules;
    }

    public void notify(GoNotificationMessage message) throws Exception {
        Option<PipelineRule> ruleOption = rules.find(message.getPipelineName(), message.getStageName(), message.getStageResult());
        if (ruleOption.isDefined()) {
            PipelineRule pipelineRule = ruleOption.get();
            handlePipelineStatus(pipelineRule, PipelineStatus.valueOf(message.getStageState().toUpperCase()), message);
        } else {
            LOG.warn(String.format("Couldn't find any matching rule for %s/%s with status=%s", message.getPipelineName(), message.getStageName(), message.getStageResult()));
        }
    }

    protected void handlePipelineStatus(PipelineRule rule, PipelineStatus status, GoNotificationMessage message) throws Exception {
        switch (status) {
            case PASSED:
                onSuccess(rule, message);
                break;
            case FAILED:
                onFailed(rule, message);
                break;
            case FIXED:
                onFixed(rule, message);
                break;
            case BROKEN:
                onBroken(rule, message);
                break;
            case BUILDING:
                onBuilding(rule, message);
                break;
            default:
                throw new RuntimeException("I just got pipeline status=" + status + ". I don't know how to handle it.");
        }
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
    public abstract void onSuccess(PipelineRule rule, GoNotificationMessage message) throws Exception;

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
}
