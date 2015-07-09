package in.ashwanthkumar.gocd.slack.jsonapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.DatatypeConverter;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.thoughtworks.go.plugin.api.logging.Logger;

import in.ashwanthkumar.gocd.slack.ruleset.Rules;

public class History {
    private Logger LOG = Logger.getLoggerFor(History.class);

    public static URL url(String pipelineName) throws MalformedURLException {
        return new URL(String.format("http://localhost:8153/go/api/pipelines/%s/history", pipelineName));
    }

    public static History get(Rules rules, String pipelineName)
        throws MalformedURLException, IOException
    {
        // Based on
        // https://github.com/matt-richardson/gocd-websocket-notifier/blob/master/src/main/java/com/matt_richardson/gocd/websocket_notifier/PipelineDetailsPopulator.java
        // http://stackoverflow.com/questions/496651/connecting-to-remote-url-which-requires-authentication-using-java

        URL url = url(pipelineName);
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
        JsonElement json = parser.parse(new InputStreamReader((InputStream) request.getContent()));
        return new GsonBuilder().create().fromJson(json, History.class);
    }

    @SerializedName("pipelines")
    public Pipeline[] pipelines;

    /**
     * Find the most recent run of the specified stage _before_ this one.
     */
    public Stage previousRun(int pipelineCounter, String stageName, int stageCounter) {
        LOG.info(String.format("Looking for stage before %d/%s/%d",
                               pipelineCounter, stageName, stageCounter));
        for (int i = pipelines.length - 1; i >= 0; i--) {
            Pipeline pipeline = pipelines[i];
            for (int j = pipeline.stages.length - 1; j >= 0; j--) {
                Stage stage = pipeline.stages[j];
                LOG.info(String.format("Checking %d/%s/%d",
                                       pipeline.counter, stage.name, stage.counter));

                if (stage.name == stageName) {

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

