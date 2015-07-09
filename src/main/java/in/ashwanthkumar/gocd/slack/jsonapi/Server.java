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
import com.thoughtworks.go.plugin.api.logging.Logger;

import in.ashwanthkumar.gocd.slack.ruleset.Rules;

/**
 * Actual methods for contacting the remote server.
 */
public class Server {
    // Contains authentication credentials, etc.
    private Rules mRules;

    /**
     * Construct a new server object, using credentials from Rules.
     */
    public Server(Rules rules) {
        mRules = rules;
    }

    private JsonElement getUrl(URL url)
        throws IOException
    {
        // Based on
        // https://github.com/matt-richardson/gocd-websocket-notifier/blob/master/src/main/java/com/matt_richardson/gocd/websocket_notifier/PipelineDetailsPopulator.java
        // http://stackoverflow.com/questions/496651/connecting-to-remote-url-which-requires-authentication-using-java

        HttpURLConnection request = (HttpURLConnection) url.openConnection();

        // Add in our HTTP authorization credentials if we have them.
        String username = mRules.getGoLogin();
        String password = mRules.getGoPassword();
        if (username != null && password != null) {
            String userpass = username + ":" + password;
            String basicAuth = "Basic "
                + DatatypeConverter.printBase64Binary(userpass.getBytes());
            request.setRequestProperty("Authorization", basicAuth);
        }

        request.connect();

        JsonParser parser = new JsonParser();
        return parser.parse(new InputStreamReader((InputStream) request.getContent()));
    }

    /**
     * Get the recent history of a pipeline.
     */
    public History getPipelineHistory(String pipelineName)
        throws MalformedURLException, IOException
    {
        URL url = new URL(String.format("http://localhost:8153/go/api/pipelines/%s/history", pipelineName));
        JsonElement json = getUrl(url);
        return new GsonBuilder().create().fromJson(json, History.class);
    }

    /**
     * Get a specific instance of a pipeline.
     */
    public Pipeline getPipelineInstance(String pipelineName, int pipelineCounter)
        throws MalformedURLException, IOException
    {
        URL url = new URL(String.format("http://localhost:8153/go/api/pipelines/%s/instance/%d",
                                        pipelineName, pipelineCounter));
        JsonElement json = getUrl(url);
        return new GsonBuilder().create().fromJson(json, Pipeline.class);
    }
}
