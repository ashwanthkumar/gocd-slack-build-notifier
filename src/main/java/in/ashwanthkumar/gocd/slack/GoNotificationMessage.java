package in.ashwanthkumar.gocd.slack;

import in.ashwanthkumar.gocd.slack.ruleset.Rules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.bind.DatatypeConverter;

public class GoNotificationMessage {
    /**
     * Raised when we can't find information about our build in the array
     * returned by the server.
     */
    static public class BuildDetailsNotFoundException extends Exception {
        public BuildDetailsNotFoundException(String pipelineName,
                                             String pipelineCounter)
        {
            super(String.format("could not find details for %s/%s",
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
     * get specify a specific version, unfortunately.  But we may want
     * modify this to only fetch the most recent 10 revisions or so.
     */
    public JsonArray fetchRecentPipelineHistory(Rules rules)
        throws URISyntaxException, IOException
    {
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
        return json.get("pipelines").getAsJsonArray();
    }

    public JsonObject fetchDetails(Rules rules)
        throws URISyntaxException, IOException,
               BuildDetailsNotFoundException
    {
        JsonArray history = fetchRecentPipelineHistory(rules);
        int wanted = Integer.parseInt(pipelineCounter);
        // Search through the builds in our recent history, and hope that
        // we can find our build.
        for (int i = 0, size = history.size(); i < size; i++) {
            JsonObject build = history.get(i).getAsJsonObject();
            if (build.get("counter").getAsInt() == wanted)
                return build;
        }
        throw new BuildDetailsNotFoundException(pipelineName, pipelineCounter);
    }
}
