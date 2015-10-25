package in.ashwanthkumar.gocd.slack.ruleset;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import in.ashwanthkumar.slack.webhook.util.Lists;
import in.ashwanthkumar.utils.collections.Sets;
import org.junit.Test;

import static in.ashwanthkumar.gocd.slack.ruleset.PipelineStatus.FAILED;
import static in.ashwanthkumar.gocd.slack.ruleset.PipelineStatus.PASSED;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

public class PipelineRuleTest {
    @Test
    public void shouldGenerateRuleFromConfig() {
        Config config = ConfigFactory.parseResources("configs/pipeline-rule-1.conf").getConfig("pipeline");
        PipelineRule build = PipelineRule.fromConfig(config);
        assertThat(build.getNameRegex(), is(".*"));
        assertThat(build.getStageRegex(), is(".*"));
        assertThat(build.getStatus(), hasItem(FAILED));
        assertThat(build.getChannel(), is("#gocd"));
    }

    @Test
    public void shouldSetValuesFromDefaultsWhenPropertiesAreNotDefined() {
        Config defaultConf = ConfigFactory.parseResources("configs/default-pipeline-rule.conf").getConfig("pipeline");
        PipelineRule defaultRule = PipelineRule.fromConfig(defaultConf);

        Config config = ConfigFactory.parseResources("configs/pipeline-rule-2.conf").getConfig("pipeline");
        PipelineRule build = PipelineRule.fromConfig(config);

        PipelineRule mergedRule = PipelineRule.merge(build, defaultRule);
        assertThat(mergedRule.getNameRegex(), is("gocd-slack-build-notifier"));
        assertThat(mergedRule.getStageRegex(), is("build"));
        assertThat(mergedRule.getStatus(), hasItem(FAILED));
        assertThat(mergedRule.getChannel(), is("#gocd"));
    }

    @Test
    public void shouldMatchThePipelineAndStageAgainstRegex() {
        PipelineRule pipelineRule = new PipelineRule("gocd-.*", ".*").setStatus(Sets.of(FAILED, PASSED));
        assertTrue(pipelineRule.matches("gocd-slack-build-notifier", "build", "failed"));
        assertTrue(pipelineRule.matches("gocd-slack-build-notifier", "package", "passed"));
        assertTrue(pipelineRule.matches("gocd-slack-build-notifier", "publish", "passed"));

        assertFalse(pipelineRule.matches("gocd", "publish", "failed"));
    }


}