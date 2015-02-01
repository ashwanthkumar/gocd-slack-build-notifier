package in.ashwanthkumar.gocd.slack.ruleset;

import in.ashwanthkumar.slack.webhook.util.Lists;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RulesReaderTest {

    @Test
    public void shouldReadTestConfig() {
        Rules rules = RulesReader.read("test-config-1.conf");
        assertThat(rules.isEnabled(), is(true));
        assertThat(rules.getSlackChannel(), is("#gocd"));
        assertThat(rules.getGoServerHost(), is("http://localhost:8080/"));
        assertThat(rules.getPipelineRules().size(), is(2));
        assertThat(rules.getPipelineRules().size(), is(2));

        PipelineRule pipelineRule1 = new PipelineRule()
                .setNameRegex("gocd-slack-build-notifier")
                .setStageRegex(".*")
                .setChannel("#gocd")
                .setStatus(Lists.of(PipelineStatus.FAILED));
        assertThat(rules.getPipelineRules(), hasItem(pipelineRule1));
    }

}