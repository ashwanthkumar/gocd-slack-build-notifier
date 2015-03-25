package in.ashwanthkumar.gocd.slack;

import in.ashwanthkumar.gocd.slack.ruleset.Rules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import com.thoughtworks.go.plugin.api.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.bind.DatatypeConverter;

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

    // Internal cache of pipeline history data from GoCD's JSON API.
    private JsonArray mRecentPipelineHistory;

    public String goServerUrl(String host) throws URISyntaxException {
        return new URI(String.format("%s/go/pipelines/%s/%s/%s/%s", host, pipelineName, pipelineCounter, stageName, stageCounter)).normalize().toASCIIString();
    }

    public String goHistoryUrl() throws URISyntaxException {
        return new URI(String.format("http://localhost:8153/go/api/pipelines/%s/history", pipelineName)).normalize().toASCIIString();
    }

    public String fullyQualifiedJobName() {
        return pipelineName + "/" + pipelineCounter + "/" + stageName + "/" + stageCounter;
    }

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

    /**
     * Fetch the full history of this pipeline from the server.  We can't
     * get specify a specific version, unfortunately.
     */
    public JsonArray fetchRecentPipelineHistory(Rules rules)
        throws URISyntaxException, IOException
    {
        if (mRecentPipelineHistory == null) {
            // Based on
            // https://github.com/matt-richardson/gocd-websocket-notifier/blob/master/src/main/java/com/matt_richardson/gocd/websocket_notifier/PipelineDetailsPopulator.java
            // http://stackoverflow.com/questions/496651/connecting-to-remote-url-which-requires-authentication-using-java

            URL url = new URL(goHistoryUrl());
            HttpURLConnection request = (HttpURLConnection) url.openConnection();

            // Add in our HTTP authorization credentials if we have them.
            String username = rules.getGoLogin();
            String password = rules.getGoPassword();
            if (username != null && password != null) {
                String userpass = username + ":" + password;
                String basicAuth = "Basic "
                    + DatatypeConverter.printBase64Binary(userpass.getBytes());
                request.setRequestProperty("Authorization", basicAuth);
            }

            request.connect();

            JsonParser parser = new JsonParser();
            JsonElement rootElement = parser.parse(new InputStreamReader((InputStream) request.getContent()));
            JsonObject json = rootElement.getAsJsonObject();
            mRecentPipelineHistory = json.get("pipelines").getAsJsonArray();
        }
        return mRecentPipelineHistory;
    }

    public JsonObject fetchDetailsForBuild(Rules rules, int counter)
        throws URISyntaxException, IOException, BuildDetailsNotFoundException
    {
        JsonArray history = fetchRecentPipelineHistory(rules);
        // Search through the builds in our recent history, and hope that
        // we can find the build we want.
        for (int i = 0, size = history.size(); i < size; i++) {
            JsonObject build = history.get(i).getAsJsonObject();
            if (build.get("counter").getAsInt() == counter)
                return build;
        }
        throw new BuildDetailsNotFoundException(pipelineName, counter);
    }

    public void tryToFixStageResult(Rules rules)
    {
        // We only need to double-check certain messages; the rest are
        // trusty-worthy.
        String currentResult = stageResult.toUpperCase();
        if (!currentResult.equals("PASSED") && !currentResult.equals("FAILED"))
            return;

        // Fetch our previous build.  If we can't get it, just give up;
        // this is a low-priority tweak.
        JsonObject previous = null;
        int wanted = Integer.parseInt(pipelineCounter) - 1;
        try {
            previous = fetchDetailsForBuild(rules, wanted);
        } catch(Exception e) {
            LOG.warn(String.format("Error getting previous build: " +
                                   e.getMessage()));
            return;
        }

        // Figure out whether the previous stage passed or failed.
        JsonArray stages = previous.get("stages").getAsJsonArray();
        JsonObject lastStage = stages.get(stages.size() - 1).getAsJsonObject();
        String previousResult = lastStage.get("result").getAsString()
            .toUpperCase();

        // Fix up our build status.  This is slightly asymmetrical, because
        // we want to be quicker to praise than to blame.  Also, I _think_
        // that the typical representation of stageResult is initial caps
        // only, but our callers should all be using toUpperCase on it in
        // any event.
        //LOG.info("current: "+currentResult + ", previous: "+previousResult);
        if (currentResult.equals("PASSED") && !previousResult.equals("PASSED"))
            stageResult = "Fixed";
        else if (currentResult.equals("FAILED") &&
                 previousResult.equals("PASSED"))
            stageResult = "Broken";
    }

    public JsonObject fetchDetails(Rules rules)
        throws URISyntaxException, IOException, BuildDetailsNotFoundException
    {
        return fetchDetailsForBuild(rules, Integer.parseInt(pipelineCounter));
    }
}
