package in.ashwanthkumar.gocd.slack.ruleset;

public enum PipelineStatus {
    /**
     * Pipeline has failed for the first time
     */
    FAILED,
    /**
     * Current and previous run of the pipeline failed hences broken
     */
    BROKEN,
    /**
     * Previous run has failed but now it succeeded
     */
    FIXED,
    /**
     * The pipeline has passed earlier and also now.
     */
    PASSED,
    /**
     * Pretty obvious ah?
     */
    ALL,
    /**
     * Status of the pipeline while being built.
     */
    UNKNOWN;

    public boolean matches(String state) {
        return this == ALL || this == PipelineStatus.valueOf(state.toUpperCase());
    }
}
