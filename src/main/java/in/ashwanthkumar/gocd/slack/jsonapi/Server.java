package in.ashwanthkumar.gocd.slack.jsonapi;

import com.google.gson.JsonElement;
import com.thoughtworks.go.plugin.api.logging.Logger;
import in.ashwanthkumar.gocd.slack.ruleset.Rules;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import static in.ashwanthkumar.utils.lang.StringUtils.isNotEmpty;

/**
 * Actual methods for contacting the remote server.
 */
public class Server {
    private Logger LOG = Logger.getLoggerFor(Server.class);

    // Contains authentication credentials, etc.
    private Rules mRules;
    private HttpConnectionUtil httpConnectionUtil;

    /**
     * Construct a new server object, using credentials from Rules.
     */
    public Server(Rules rules) {
        mRules = rules;
        httpConnectionUtil = new HttpConnectionUtil();
    }

    Server(Rules mRules, HttpConnectionUtil httpConnectionUtil) {
        this.mRules = mRules;
        this.httpConnectionUtil = httpConnectionUtil;
    }

    JsonElement getUrl(URL url)
            throws IOException {
        URL normalizedUrl;
        try {
            normalizedUrl = url.toURI().normalize().toURL();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        LOG.info("Fetching " + normalizedUrl.toString());

        HttpURLConnection request = httpConnectionUtil.getConnection(normalizedUrl);

        // Add in our HTTP authorization credentials if we have them.
        // Favor the API Token over username/password
        String authHeader = null;
        if (isNotEmpty(mRules.getGoAPIToken())) {
            String bearerToken = "Bearer "
                    + DatatypeConverter.printBase64Binary(mRules.getGoAPIToken().getBytes());
            request.setRequestProperty("Authorization", bearerToken);
        } else if (isNotEmpty(mRules.getGoLogin()) && isNotEmpty(mRules.getGoPassword())) {
            String userpass = mRules.getGoLogin() + ":" + mRules.getGoPassword();
            authHeader = "Basic "
                    + DatatypeConverter.printBase64Binary(userpass.getBytes());
        }
        if (authHeader != null) {
            request.setRequestProperty("Authorization", authHeader);
            request.setRequestProperty("Accept", "application/vnd.go.cd.v1+json");
        }

        request.connect();

        return httpConnectionUtil.responseToJson(request.getContent());
    }

    /**
     * Get the recent history of a pipeline.
     */
    public History getPipelineHistory(String pipelineName)
            throws MalformedURLException, IOException {
        URL url = new URL(String.format("%s/go/api/pipelines/%s/history",
                mRules.getGoAPIServerHost(), pipelineName));
        JsonElement json = getUrl(url);
        return httpConnectionUtil.convertResponse(json, History.class);
    }

    /**
     * Get a specific instance of a pipeline.
     */
    public Pipeline getPipelineInstance(String pipelineName, int pipelineCounter)
            throws MalformedURLException, IOException {
        URL url = new URL(String.format("%s/go/api/pipelines/%s/instance/%d",
                mRules.getGoAPIServerHost(), pipelineName, pipelineCounter));
        JsonElement json = getUrl(url);
        return httpConnectionUtil.convertResponse(json, Pipeline.class);
    }
}
