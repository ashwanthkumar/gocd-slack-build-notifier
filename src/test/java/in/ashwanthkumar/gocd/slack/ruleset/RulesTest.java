package in.ashwanthkumar.gocd.slack.ruleset;


import in.ashwanthkumar.gocd.slack.Status;
import in.ashwanthkumar.utils.lang.option.Option;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class RulesTest {

    @Test
    public void shouldFindMatch() {
        Rules rules = new Rules();
        PipelineRule pRules1 = new PipelineRule("pipeline", "stage");
        pRules1.setStatus(new HashSet<PipelineStatus>(Arrays.asList(PipelineStatus.PASSED, PipelineStatus.FAILED)));

        rules.setPipelineRules(Arrays.asList(
                pipelineRule("pipeline1", "stage1", "ch1", statuses(PipelineStatus.BUILDING, PipelineStatus.FAILED)),
                pipelineRule("pipeline1", "stage2", "ch2", statuses(PipelineStatus.FIXED, PipelineStatus.PASSED)),
                pipelineRule("pipeline2", "stage2", "ch3", statuses(PipelineStatus.CANCELLED, PipelineStatus.BROKEN))
        ));

        Option<PipelineRule> rule1 = rules.find("pipeline1", "stage1", Status.Building.getStatus());
        assertThat(rule1.isDefined(), is(true));
        assertThat(rule1.get().getNameRegex(), is("pipeline1"));
        assertThat(rule1.get().getStageRegex(), is("stage1"));

        Option<PipelineRule> rule2 = rules.find("pipeline2", "stage2", Status.Cancelled.getStatus());
        assertThat(rule2.isDefined(), is(true));
        assertThat(rule2.get().getNameRegex(), is("pipeline2"));
        assertThat(rule2.get().getStageRegex(), is("stage2"));

        Option<PipelineRule> rule3 = rules.find("pipeline2", "stage2", Status.Passed.getStatus());
        assertThat(rule3.isDefined(), is(false));
    }

    @Test
    public void shouldFindMatchWithRegexp() {
        Rules rules = new Rules();
        PipelineRule pRules1 = new PipelineRule("pipeline", "stage");
        pRules1.setStatus(new HashSet<PipelineStatus>(Arrays.asList(PipelineStatus.PASSED, PipelineStatus.FAILED)));

        rules.setPipelineRules(Arrays.asList(
                pipelineRule("[a-z]*", "[a-z]*", "ch1", statuses(PipelineStatus.BUILDING)),
                pipelineRule("\\d*", "\\d*", "ch2", statuses(PipelineStatus.BUILDING)),
                pipelineRule("\\d*", "\\d*", "ch3", statuses(PipelineStatus.PASSED)),
                pipelineRule("\\d*", "[a-z]*", "ch4", statuses(PipelineStatus.BUILDING))
        ));

        Option<PipelineRule> rule1 = rules.find("abc", "efg", Status.Building.getStatus());
        assertThat(rule1.isDefined(), is(true));
        assertThat(rule1.get().getNameRegex(), is("[a-z]*"));
        assertThat(rule1.get().getStageRegex(), is("[a-z]*"));
        assertThat(rule1.get().getChannel(), is("ch1"));

        Option<PipelineRule> rule2 = rules.find("123", "456", Status.Building.getStatus());
        assertThat(rule2.isDefined(), is(true));
        assertThat(rule2.get().getNameRegex(), is("\\d*"));
        assertThat(rule2.get().getStageRegex(), is("\\d*"));
        assertThat(rule2.get().getChannel(), is("ch2"));

        Option<PipelineRule> rule3 = rules.find("123", "456", Status.Passed.getStatus());
        assertThat(rule3.isDefined(), is(true));
        assertThat(rule3.get().getNameRegex(), is("\\d*"));
        assertThat(rule3.get().getStageRegex(), is("\\d*"));
        assertThat(rule3.get().getChannel(), is("ch3"));

        Option<PipelineRule> rule4 = rules.find("pipeline1", "stage1", Status.Passed.getStatus());
        assertThat(rule4.isDefined(), is(false));
    }

    @Test
    public void shouldFindMatchAll() {
        Rules rules = new Rules();
        PipelineRule pRules1 = new PipelineRule("pipeline", "stage");
        pRules1.setStatus(new HashSet<PipelineStatus>(Arrays.asList(PipelineStatus.PASSED, PipelineStatus.FAILED)));

        rules.setPipelineRules(Arrays.asList(
                pipelineRule("p1", "s1", "ch1", statuses(PipelineStatus.ALL))
        ));

        assertThat(rules.find("p1", "s1", Status.Building.getStatus()).isDefined(), is(true));
        assertThat(rules.find("p1", "s1", Status.Broken.getStatus()).isDefined(), is(true));
        assertThat(rules.find("p1", "s1", Status.Cancelled.getStatus()).isDefined(), is(true));
        assertThat(rules.find("p1", "s1", Status.Failed.getStatus()).isDefined(), is(true));
        assertThat(rules.find("p1", "s1", Status.Failing.getStatus()).isDefined(), is(true));
        assertThat(rules.find("p1", "s1", Status.Fixed.getStatus()).isDefined(), is(true));
        assertThat(rules.find("p1", "s1", Status.Passed.getStatus()).isDefined(), is(true));
        assertThat(rules.find("p1", "s1", Status.Unknown.getStatus()).isDefined(), is(true));
    }

    @Test
    public void shouldGetAPIServerHost() {
        Rules rules = new Rules();

        rules.setGoServerHost("https://gocd.com");
        assertThat(rules.getGoAPIServerHost(), is("https://gocd.com"));

        rules.setGoAPIServerHost("http://localhost");
        assertThat(rules.getGoAPIServerHost(), is("http://localhost"));
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