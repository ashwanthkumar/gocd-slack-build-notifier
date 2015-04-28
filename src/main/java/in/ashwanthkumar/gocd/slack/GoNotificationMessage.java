package in.ashwanthkumar.gocd.slack;


import java.net.URI;
import java.net.URISyntaxException;

public class GoNotificationMessage {
    private Pipeline pipeline;

    public String goServerUrl(String host) throws URISyntaxException {
        return new URI(String.format("%s/go/pipelines/%s/%s/%s/%s", host, getPipelineName(), getPipelineCounter(), getStageName(), getStageCounter())).normalize().toASCIIString();
    }

    public String fullyQualifiedJobName() {
        return getPipelineName() + "/" + getPipelineCounter() + "/" + getStageName() + "/" + getStageCounter();
    }

    public String getPipelineName() {
        return pipeline.getName();
    }

    public String getPipelineCounter() {
        return pipeline.getCounter();
    }

    public String getStageName() {
        return pipeline.getStageName();
    }

    public String getStageCounter() {
        return pipeline.getStageCounter();
    }

    public String getStageState() {
        return pipeline.getStageState();
    }

    public String getStageResult() {
        return pipeline.getStageResult();
    }

    public String getCreateTime() {
        return pipeline.getCreateTime();
    }

    public String getLastTransitionTime() {
        return pipeline.getLastTransitionTime();
    }
}

class Pipeline {
    private String name;
    private String counter;
    private Stage stage;

    public String getName() {
        return name;
    }

    public String getCounter() {
        return counter;
    }

    public String getStageName() {
        return stage.getName();
    }

    public String getStageCounter() {
        return stage.getCounter();
    }

    public String getStageState() {
        return stage.getState();
    }

    public String getStageResult() {
        return stage.getResult();
    }

    public String getCreateTime() {
        return stage.getCreateTime();
    }

    public String getLastTransitionTime() {
        return stage.getLastTransitionTime();
    }
}

class Stage {
    private String name;
    private String counter;
    private String state;
    private String result;
    private String createTime;
    private String lastTransitionTime;

    public String getName() {
        return name;
    }

    public String getCounter() {
        return counter;
    }

    public String getState() {
        return state;
    }

    public String getResult() {
        return result;
    }

    public String getCreateTime() {
        return createTime;
    }

    public String getLastTransitionTime() {
        return lastTransitionTime;
    }
}


