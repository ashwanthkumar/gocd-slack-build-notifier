package in.ashwanthkumar.gocd.teams;

import com.google.api.client.http.AbstractHttpContent;
import com.google.api.client.json.Json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Serialize a {@link TeamsCard} for the Google HTTP Client.
 */
public class CardHttpContent extends AbstractHttpContent {
    private final TeamsCard card;

    protected CardHttpContent(TeamsCard card) {
        super(Json.MEDIA_TYPE);
        this.card = card;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        try (var osw = new OutputStreamWriter(out)) {
            osw.write(card.toString());
        }
    }
}
