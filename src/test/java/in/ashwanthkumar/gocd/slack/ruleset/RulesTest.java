package in.ashwanthkumar.gocd.slack.ruleset;

import in.ashwanthkumar.gocd.slack.Status;
import in.ashwanthkumar.utils.lang.option.Option;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class RulesTest {

    @Test
    public void shouldFindMatch() {
        Rules rules = new Rules();

        rules.setPipelineRules(Arrays.asList(
                pipelineRule("pipeline1", "stage1", "ch1", statuses(PipelineStatus.BUILDING, PipelineStatus.FAILED)),
                pipelineRule("pipeline1", "stage2", "ch2", statuses(PipelineStatus.FIXED, PipelineStatus.PASSED)),
                pipelineRule("pipeline2", "stage2", "ch3", statuses(PipelineStatus.CANCELLED, PipelineStatus.BROKEN))
        ));

        List<PipelineRule> foundRules1 = rules.find("pipeline1", "stage1", "ci", Status.Building.getStatus());
        assertThat(foundRules1.size(), is(1));
        assertThat(foundRules1.get(0).getNameRegex(), is("pipeline1"));
        assertThat(foundRules1.get(0).getStageRegex(), is("stage1"));

        List<PipelineRule> foundRules2 = rules.find("pipeline2", "stage2", "ci", Status.Cancelled.getStatus());
        assertThat(foundRules2.size(), is(1));
        assertThat(foundRules2.get(0).getNameRegex(), is("pipeline2"));
        assertThat(foundRules2.get(0).getStageRegex(), is("stage2"));

        List<PipelineRule> foundRules3 = rules.find("pipeline2", "stage2", "ci", Status.Passed.getStatus());
        assertThat(foundRules3.size(), is(0));
    }

    @Test
    public void shouldFindMatchWithRegexp() {
        Rules rules = new Rules();

        rules.setPipelineRules(Arrays.asList(
                pipelineRule("[a-z]*", "[a-z]*", "ch1", statuses(PipelineStatus.BUILDING)),
                pipelineRule("\\d*", "\\d*", "ch2", statuses(PipelineStatus.BUILDING)),
                pipelineRule("\\d*", "\\d*", "ch3", statuses(PipelineStatus.PASSED)),
                pipelineRule("\\d*", "[a-z]*", "ch4", statuses(PipelineStatus.BUILDING))
        ));

        List<PipelineRule> foundRules1 = rules.find("abc", "efg", "ci", Status.Building.getStatus());
        assertThat(foundRules1.size(), is(1));
        assertThat(foundRules1.get(0).getNameRegex(), is("[a-z]*"));
        assertThat(foundRules1.get(0).getStageRegex(), is("[a-z]*"));
        assertThat(foundRules1.get(0).getChannel(), is("ch1"));

        List<PipelineRule> foundRules2 = rules.find("123", "456", "ci", Status.Building.getStatus());
        assertThat(foundRules2.size(), is(1));
        assertThat(foundRules2.get(0).getNameRegex(), is("\\d*"));
        assertThat(foundRules2.get(0).getStageRegex(), is("\\d*"));
        assertThat(foundRules2.get(0).getChannel(), is("ch2"));

        List<PipelineRule> foundRules3 = rules.find("123", "456", "ci", Status.Passed.getStatus());
        assertThat(foundRules3.size(), is(1));
        assertThat(foundRules3.get(0).getNameRegex(), is("\\d*"));
        assertThat(foundRules3.get(0).getStageRegex(), is("\\d*"));
        assertThat(foundRules3.get(0).getChannel(), is("ch3"));

        List<PipelineRule> foundRules4 = rules.find("pipeline1", "stage1", "ci", Status.Passed.getStatus());
        assertThat(foundRules4.size(), is(0));
    }

    @Test
    public void shouldFindAllMatchesIfProcessAllRules() {
        Rules rules = new Rules();
        rules.setProcessAllRules(true);

        rules.setPipelineRules(Arrays.asList(
                pipelineRule("[a-z]*", "stage\\d+", "ch1", statuses(PipelineStatus.BUILDING)),
                pipelineRule("[a-z]*", "stage2", "ch2", statuses(PipelineStatus.BUILDING))
        ));

        List<PipelineRule> foundRules1 = rules.find("abc", "stage1", "ci", Status.Building.getStatus());
        assertThat(foundRules1.size(), is(1));
        assertThat(foundRules1.get(0).getChannel(), is("ch1"));

        List<PipelineRule> foundRules2 = rules.find("abc", "stage2", "ci", Status.Building.getStatus());
        assertThat(foundRules2.size(), is(2));
        assertThat(foundRules2.get(0).getChannel(), is("ch1"));
        assertThat(foundRules2.get(1).getChannel(), is("ch2"));

        List<PipelineRule> foundRules3 = rules.find("abc1", "stage2", "ci", Status.Building.getStatus());
        assertThat(foundRules3.size(), is(0));
    }

    @Test
    public void shouldFindMatchAll() {
        Rules rules = new Rules();

        rules.setPipelineRules(Arrays.asList(
                pipelineRule("p1", "s1", "ch1", statuses(PipelineStatus.ALL))
        ));

        assertThat(rules.find("p1", "s1", "ci", Status.Building.getStatus()).size(), is(1));
        assertThat(rules.find("p1", "s1", "ci", Status.Broken.getStatus()).size(), is(1));
        assertThat(rules.find("p1", "s1", "ci", Status.Cancelled.getStatus()).size(), is(1));
        assertThat(rules.find("p1", "s1", "ci", Status.Failed.getStatus()).size(), is(1));
        assertThat(rules.find("p1", "s1", "ci", Status.Failing.getStatus()).size(), is(1));
        assertThat(rules.find("p1", "s1", "ci", Status.Fixed.getStatus()).size(), is(1));
        assertThat(rules.find("p1", "s1", "ci", Status.Passed.getStatus()).size(), is(1));
        assertThat(rules.find("p1", "s1", "ci", Status.Unknown.getStatus()).size(), is(1));
    }

    @Test
    public void shouldGetAPIServerHost() {
        Rules rules = new Rules();

        rules.setGoServerHost("https://gocd.com");
        assertThat(rules.getGoAPIServerHost(), is("https://gocd.com"));

        rules.setGoAPIServerHost("http://localhost");
        assertThat(rules.getGoAPIServerHost(), is("http://localhost"));
    }

    @Test
    public void shouldGetAPIToken() {
        Rules rules = new Rules();

        rules.setGoAPIToken("a-valid-token-from-gocd-server");
        assertThat(rules.getGoAPIToken(), is("a-valid-token-from-gocd-server"));
    }

    private static PipelineRule pipelineRule(String pipeline, String stage, String channel, Set<PipelineStatus> statuses) {
        PipelineRule pipelineRule = new PipelineRule(pipeline, stage);
        pipelineRule.setStatus(statuses);
        pipelineRule.setChannel(channel);
        return pipelineRule;
    }

    private static Set<PipelineStatus> statuses(PipelineStatus... statuses) {
        return new HashSet<PipelineStatus>(Arrays.asList(statuses));
    }

}
