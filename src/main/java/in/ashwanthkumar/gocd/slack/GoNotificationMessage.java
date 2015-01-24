package in.ashwanthkumar.gocd.slack;

import com.google.gson.annotations.SerializedName;

public class GoNotificationMessage {
    @SerializedName("pipeline-name")
    private String pipelineName;

    @SerializedName("pipeline-counter")
    private String pipelineCounter;

    @SerializedName("stage-name")
    private String stageName;

    @SerializedName("stage-counter")
    private String stageCounter;

    @SerializedName("stage-state")
    private String stageState;

    @SerializedName("stage-result")
    private String stageResult;

    @SerializedName("create-time")
    private String createTime;

    @SerializedName("last-transition-time")
    private String lastTransitionTime;

    public String getPipelineName() {
        return pipelineName;
    }

    public String getPipelineCounter() {
        return pipelineCounter;
    }

    public String getStageName() {
        return stageName;
    }

    public String getStageCounter() {
        return stageCounter;
    }

    public String getStageState() {
        return stageState;
    }

    public String getStageResult() {
        return stageResult;
    }

    public String getCreateTime() {
        return createTime;
    }

    public String getLastTransitionTime() {
        return lastTransitionTime;
    }
}
