package in.ashwanthkumar.gocd.slack;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.google.gson.annotations.SerializedName;
import com.thoughtworks.go.plugin.api.logging.Logger;

import in.ashwanthkumar.gocd.slack.jsonapi.History;
import in.ashwanthkumar.gocd.slack.jsonapi.Pipeline;
import in.ashwanthkumar.gocd.slack.jsonapi.Server;
import in.ashwanthkumar.gocd.slack.jsonapi.Stage;
import in.ashwanthkumar.gocd.slack.ruleset.Rules;

public class GoNotificationMessage {
    private Logger LOG = Logger.getLoggerFor(GoNotificationMessage.class);

    /**
     * Raised when we can't find information about our build in the array
     * returned by the server.
     */
    static public class BuildDetailsNotFoundException extends Exception {
        public BuildDetailsNotFoundException(String pipelineName,
                                             int pipelineCounter)
        {
            super(String.format("could not find details for %s/%d",
                                pipelineName, pipelineCounter));
        }
    }

    static class StageInfo {
        @SerializedName("name")
        private String name;

        @SerializedName("counter")
        private String counter;

        @SerializedName("state")
        private String state;

        @SerializedName("result")
        private String result;

        @SerializedName("create-time")
        private String createTime;

        @SerializedName("last-transition-time")
        private String lastTransitionTime;
    }

    static class PipelineInfo {
        @SerializedName("name")
        private String name;

        @SerializedName("counter")
        private String counter;

        @SerializedName("stage")
        private StageInfo stage;
    }

    @SerializedName("pipeline")
    private PipelineInfo pipeline;

    // Internal cache of pipeline history data from GoCD's JSON API.
    private History mRecentPipelineHistory;

    public String goServerUrl(String host) throws URISyntaxException {
        return new URI(String.format("%s/go/pipelines/%s/%s/%s/%s", host, pipeline.name, pipeline.counter, pipeline.stage.name, pipeline.stage.counter)).normalize().toASCIIString();
    }

    public String fullyQualifiedJobName() {
        return pipeline.name + "/" + pipeline.counter + "/" + pipeline.stage.name + "/" + pipeline.stage.counter;
    }

    public String getPipelineName() {
        return pipeline.name;
    }

    public String getPipelineCounter() {
        return pipeline.counter;
    }

    public String getStageName() {
        return pipeline.stage.name;
    }

    public String getStageCounter() {
        return pipeline.stage.counter;
    }

    public String getStageState() {
        return pipeline.stage.state;
    }

    public String getStageResult() {
        return pipeline.stage.result;
    }

    public String getCreateTime() {
        return pipeline.stage.createTime;
    }

    public String getLastTransitionTime() {
        return pipeline.stage.lastTransitionTime;
    }

    /**
     * Fetch the full history of this pipeline from the server.  We can't
     * get specify a specific version, unfortunately.
     */
    public History fetchRecentPipelineHistory(Rules rules)
        throws URISyntaxException, IOException
    {
        if (mRecentPipelineHistory == null) {
            Server server = new Server(rules);
            mRecentPipelineHistory = server.getPipelineHistory(pipeline.name);
        }
        return mRecentPipelineHistory;
    }

    public Pipeline fetchDetailsForBuild(Rules rules, int counter)
        throws URISyntaxException, IOException, BuildDetailsNotFoundException
    {
        Pipeline[] pipelines = fetchRecentPipelineHistory(rules).pipelines;
        // Search through the builds in our recent history, and hope that
        // we can find the build we want.
        for (int i = 0, size = pipelines.length; i < size; i++) {
            Pipeline build = pipelines[i];
            if (build.counter == counter)
                return build;
        }
        throw new BuildDetailsNotFoundException(getPipelineName(), counter);
    }

    public void tryToFixStageResult(Rules rules)
    {
        // We only need to double-check certain messages; the rest are
        // trusty-worthy.
        String currentResult = pipeline.stage.result.toUpperCase();
        if (!currentResult.equals("PASSED") && !currentResult.equals("FAILED"))
            return;

        // Fetch our history.  If we can't get it, just give up; this is a
        // low-priority tweak.
        History history = null;
        try {
            history = fetchRecentPipelineHistory(rules);
        } catch(Exception e) {
            LOG.warn(String.format("Error getting pipeline history: " +
                                   e.getMessage()));
            return;
        }

        // Figure out whether the previous run of this stage passed or failed.
        Stage previous = history.previousRun(Integer.parseInt(pipeline.counter),
                                             pipeline.stage.name,
                                             Integer.parseInt(pipeline.stage.counter));
        if (previous == null) {
            LOG.info("Couldn't find any previous run of " +
                     pipeline.name + "/" + pipeline.counter + "/" +
                     pipeline.stage.name + "/" + pipeline.stage.counter);
            return;
        }
        String previousResult = previous.result.toUpperCase();

        // Fix up our build status.  This is slightly asymmetrical, because
        // we want to be quicker to praise than to blame.  Also, I _think_
        // that the typical representation of stageResult is initial caps
        // only, but our callers should all be using toUpperCase on it in
        // any event.
        //LOG.info("current: "+currentResult + ", previous: "+previousResult);
        if (currentResult.equals("PASSED") && !previousResult.equals("PASSED"))
            pipeline.stage.result = "Fixed";
        else if (currentResult.equals("FAILED") &&
                 previousResult.equals("PASSED"))
            pipeline.stage.result = "Broken";
    }

    public Pipeline fetchDetails(Rules rules)
        throws URISyntaxException, IOException, BuildDetailsNotFoundException
    {
        return fetchDetailsForBuild(rules, Integer.parseInt(getPipelineCounter()));
    }
}
