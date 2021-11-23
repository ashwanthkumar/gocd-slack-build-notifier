package in.ashwanthkumar.gocd.teams;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import in.ashwanthkumar.gocd.slack.ruleset.Rules;
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

    @Test
    public void testTeamsListener() {
        Config config = ConfigFactory.parseResources("configs/test-config-teams.conf")
                .withFallback(ConfigFactory.load(getClass().getClassLoader()))
                .getConfig("gocd.slack");
        Rules rules = Rules.fromConfig(config);

        Assert.assertEquals(TeamsPipelineListener.class, rules.getPipelineListener().getClass());
        Assert.assertEquals("https://example.com/default", rules.getWebHookUrl());
        Assert.assertEquals("https://example.com/pipeline-override",
                rules.getPipelineRules().get(0).getWebhookUrl());
    }
}
