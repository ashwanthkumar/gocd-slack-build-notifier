package in.ashwanthkumar.gocd.teams;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TeamsTest {

    private TeamsCard buildCard() {
        TeamsCard card = new TeamsCard();
        card.setTitle("title");
        card.setColor(MessageCardSchema.Color.GREEN);
        card.addFact("k", "v");
        card.addLinkAction("name", "uri");
        return card;
    }

    @Test
    public void testCardHttpContent() throws IOException {
        TeamsCard card = buildCard();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CardHttpContent(card).writeTo(baos);
        final String result = baos.toString();

        Assert.assertEquals(card.toString(), result);
    }

    @Test
    public void testCardToString() {
        TeamsCard card = buildCard();
        String result = card.toString();

        String expected = ("{'@type':'MessageCard','themeColor':'009900','title':'title'," +
                "'summary':'GoCD build update','sections':[{'facts':[{'name':'k'," +
                "'value':'v'}]}],'potentialAction':[{'@type':'OpenUri','name':'name'," +
                "'targets':[{'os':'default','uri':'uri'}]}]}")
                .replace('\'', '"');

        Assert.assertEquals(expected, result);
    }
}
