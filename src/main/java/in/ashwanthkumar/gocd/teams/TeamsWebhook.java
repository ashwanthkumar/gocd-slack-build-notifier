package in.ashwanthkumar.gocd.teams;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.thoughtworks.go.plugin.api.logging.Logger;

import java.io.IOException;
import java.net.Proxy;
import java.util.Objects;

/**
 * Sends post requests to a Teams channels incoming webhook.
 * Uses Google HTTP Client.
 */
public class TeamsWebhook {
    private static final Logger LOG = Logger.getLoggerFor(TeamsWebhook.class);
    private final HttpRequestFactory requestFactory;

    public TeamsWebhook(Proxy proxy) {
        requestFactory = new NetHttpTransport.Builder()
                .setProxy(proxy)
                .build()
                .createRequestFactory();
    }

    public void send(String webhookUrl, TeamsCard card) throws IOException {
        Objects.requireNonNull(webhookUrl);
        Objects.requireNonNull(card);
        LOG.debug("Using webhook: " + webhookUrl);
        LOG.debug("Sending Card: " + card);
        requestFactory.buildPostRequest(
                        new GenericUrl(webhookUrl),
                        new CardHttpContent(card)
                )
                .execute();
    }
}
