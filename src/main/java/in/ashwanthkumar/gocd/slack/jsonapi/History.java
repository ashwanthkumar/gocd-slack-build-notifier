package in.ashwanthkumar.gocd.slack.jsonapi;

import com.google.gson.annotations.SerializedName;
import com.thoughtworks.go.plugin.api.logging.Logger;

public class History {
    private Logger LOG = Logger.getLoggerFor(History.class);

    @SerializedName("pipelines")
    public Pipeline[] pipelines;

    /**
     * Find the most recent run of the specified stage _before_ this one.
     */
    public Stage previousRun(int pipelineCounter, String stageName, int stageCounter) {
        LOG.debug(String.format("Looking for stage before %d/%s/%d",
                                pipelineCounter, stageName, stageCounter));

        // Note that pipelines and stages are stored in reverse
        // chronological order.
        for (int i = 0; i < pipelines.length; i++) {
            Pipeline pipeline = pipelines[i];
            for (int j = 0; j < pipeline.stages.length; j++) {
                Stage stage = pipeline.stages[j];
                LOG.debug(String.format("Checking %d/%s/%d",
                                        pipeline.counter, stage.name, stage.counter));

                if (stage.name.equals(stageName)) {

                    // Same pipeline run, earlier instance of stage.
                    if (pipeline.counter == pipelineCounter &&
                        stage.counter < stageCounter)
                        return stage;

                    // Previous pipeline run.
                    if (pipeline.counter < pipelineCounter)
                        return stage;
                }
            }
        }
        // Not found.
        return null;
    }
}

