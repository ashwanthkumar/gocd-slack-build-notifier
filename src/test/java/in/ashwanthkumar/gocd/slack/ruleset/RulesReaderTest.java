package in.ashwanthkumar.gocd.slack.ruleset;

import in.ashwanthkumar.utils.collections.Sets;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.nullValue;
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
                .setStatus(Sets.of(PipelineStatus.FAILED));
        assertThat(rules.getPipelineRules(), hasItem(pipelineRule1));

        PipelineRule pipelineRule2 = new PipelineRule()
                .setNameRegex("my-java-utils")
                .setStageRegex("build")
                .setChannel("#gocd-build")
                .setStatus(Sets.of(PipelineStatus.FAILED));
        assertThat(rules.getPipelineRules(), hasItem(pipelineRule2));
    }

    @Test
    public void shouldReadMinimalConfig() {
        Rules rules = RulesReader.read("test-config-minimal.conf");
        assertThat(rules.isEnabled(), is(true));
        assertThat(rules.getSlackChannel(), nullValue());
        assertThat(rules.getGoServerHost(), is("https://go-instance:8153/"));
        assertThat(rules.getWebHookUrl(), is("http://slack-instance.net/"));
        assertThat(rules.getPipelineRules().size(), is(1));

        PipelineRule pipelineRule = new PipelineRule()
                .setNameRegex(".*")
                .setStageRegex(".*")
                .setChannel("#foo")
                .setStatus(Sets.of(PipelineStatus.FAILED));
        assertThat(rules.getPipelineRules(), hasItem(pipelineRule));
    }

}