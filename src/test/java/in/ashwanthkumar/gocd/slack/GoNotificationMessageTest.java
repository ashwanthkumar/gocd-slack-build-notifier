package in.ashwanthkumar.gocd.slack;

import in.ashwanthkumar.gocd.slack.jsonapi.*;
import in.ashwanthkumar.gocd.slack.ruleset.Rules;
import in.ashwanthkumar.gocd.slack.util.TestUtils;
import in.ashwanthkumar.utils.collections.Lists;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GoNotificationMessageTest {

    private static final String PIPELINE_NAME = "pipeline";

    @Test
    public void shouldFetchPipelineDetails() throws Exception {
        Server server = mock(Server.class);

        History pipelineHistory = new History();
        pipelineHistory.pipelines = new Pipeline[]{
                pipeline(PIPELINE_NAME, 8),
                pipeline(PIPELINE_NAME, 9),
                pipeline(PIPELINE_NAME, 10),
                pipeline(PIPELINE_NAME, 11),
                pipeline(PIPELINE_NAME, 12)
        };
        when(server.getPipelineHistory(PIPELINE_NAME)).thenReturn(pipelineHistory);

        GoNotificationMessage message = new GoNotificationMessage(
                TestUtils.createMockServerFactory(server),
                info(PIPELINE_NAME, 10)
        );

        Pipeline result = message.fetchDetails(new Rules());

        assertThat(result.name, is(PIPELINE_NAME));
        assertThat(result.counter, is(10));
    }

    @Test(expected = GoNotificationMessage.BuildDetailsNotFoundException.class)
    public void shouldFetchPipelineDetailsNotFound() throws Exception {
        Server server = mock(Server.class);

        History pipelineHistory = new History();
        pipelineHistory.pipelines = new Pipeline[]{
                pipeline(PIPELINE_NAME, 8),
                pipeline(PIPELINE_NAME, 9)
        };
        when(server.getPipelineHistory(PIPELINE_NAME)).thenReturn(pipelineHistory);

        GoNotificationMessage message = new GoNotificationMessage(
                TestUtils.createMockServerFactory(server),
                info(PIPELINE_NAME, 10)
        );

        message.fetchDetails(new Rules());
    }

    @Test(expected = GoNotificationMessage.BuildDetailsNotFoundException.class)
    public void shouldFetchPipelineDetailsNothingFound() throws Exception {
        Server server = mock(Server.class);

        History pipelineHistory = new History();
        pipelineHistory.pipelines = new Pipeline[]{
                pipeline("something-different", 10)
        };
        when(server.getPipelineHistory("something-different")).thenReturn(pipelineHistory);

        GoNotificationMessage message = new GoNotificationMessage(
                TestUtils.createMockServerFactory(server),
                info(PIPELINE_NAME, 10)
        );

        message.fetchDetails(new Rules());
    }

    @Test
    public void shouldFetchChanges() throws Exception {
        Server server = mock(Server.class);

        Pipeline pipeline1 = new Pipeline();
        {
            pipeline1.buildCause = new BuildCause();

            MaterialRevision leafRevision = new MaterialRevision();
            leafRevision.material = new Material();
            leafRevision.material.type = "Something";
            leafRevision.material.id = 1338;
            leafRevision.changed = true;

            MaterialRevision pipelineRevision = new MaterialRevision();
            pipelineRevision.material = new Material();
            pipelineRevision.material.type = "Pipeline";
            pipelineRevision.changed = true;

            Modification modification = new Modification();
            modification.revision = "pipeline2/11/foo";

            pipelineRevision.modifications = Lists.of(modification);
            pipeline1.buildCause.materialRevisions = new MaterialRevision[]{
                    leafRevision, pipelineRevision
            };
        }
        Pipeline pipeline2 = new Pipeline();
        {
            pipeline2.buildCause = new BuildCause();

            MaterialRevision leafRevision = new MaterialRevision();
            leafRevision.material = new Material();
            leafRevision.material.type = "Something other";
            leafRevision.material.id = 1337;
            leafRevision.changed = true;

            pipeline2.buildCause.materialRevisions = new MaterialRevision[]{
                    leafRevision
            };
        }
        when(server.getPipelineInstance("pipeline1", 10)).thenReturn(pipeline1);
        when(server.getPipelineInstance("pipeline2", 11)).thenReturn(pipeline2);

        GoNotificationMessage message = new GoNotificationMessage(
                TestUtils.createMockServerFactory(server),
                info("pipeline1", 10)
        );

        List<MaterialRevision> revisions = message.fetchChanges(new Rules());

        assertThat(revisions.size(), is(2));
    }

    private static Pipeline pipeline(String name, int counter) {
        Pipeline pipeline = new Pipeline();
        pipeline.name = name;
        pipeline.counter = counter;
        return pipeline;
    }

    private static GoNotificationMessage.PipelineInfo info(String name, int counter) {
        GoNotificationMessage.PipelineInfo pipeline = new GoNotificationMessage.PipelineInfo();
        pipeline.counter = Integer.toString(counter);
        pipeline.name = name;
        return pipeline;
    }

}
