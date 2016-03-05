package in.ashwanthkumar.gocd.slack;

import in.ashwanthkumar.gocd.slack.jsonapi.*;
import in.ashwanthkumar.gocd.slack.ruleset.Rules;
import in.ashwanthkumar.gocd.slack.util.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class GoNotificationMessage_FixStageTest {

    public static final String PIPELINE_NAME = "PL";
    public static final String STAGE_NAME = "STG";

    private History pipelineHistory;
    private GoNotificationMessage.PipelineInfo pipeline;
    private String expectedStatus;

    public GoNotificationMessage_FixStageTest(History pipelineHistory, GoNotificationMessage.PipelineInfo pipeline, String expectedStatus) {
        this.pipelineHistory = pipelineHistory;
        this.pipeline = pipeline;
        this.expectedStatus = expectedStatus;
    }

    @Parameterized.Parameters(name = "{index}: Pipeline <{0}> to <{1}> should return status {2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
        // One history pipeline, same pipeline run
        {
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Failed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(2), Status.Building))),
                thenExpectStatus(Status.Building)
        }, {
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Failed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(2), Status.Passed))),
                thenExpectStatus(Status.Fixed)
        }, {
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Failed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(2), Status.Passed))),
                thenExpectStatus(Status.Fixed)
        }, {
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(2), Status.Failed))),
                thenExpectStatus(Status.Broken)
        }, {
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(2), Status.Passed))),
                thenExpectStatus(Status.Passed)
        }, {
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Failed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(2), Status.Failed))),
                thenExpectStatus(Status.Failed)
        }, {
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Cancelled))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(2), Status.Passed))),
                thenExpectStatus(Status.Fixed)
        }, {
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Cancelled))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(2), Status.Failed))),
                thenExpectStatus(Status.Failed)
        }, {
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Cancelled))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(2), Status.Cancelled))),
                thenExpectStatus(Status.Cancelled)
        },

        // Multiple stages
        {
                givenHistory(pipeline(PIPELINE_NAME, counter(1),
                        stage("other-stage-name-1", counter(1), Status.Failed),
                        stage(STAGE_NAME,           counter(1), Status.Failed),
                        stage("other-stage-name-2", counter(1), Status.Failed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(4), Status.Passed))),
                thenExpectStatus(Status.Fixed)
        }, {
                givenHistory(pipeline(PIPELINE_NAME, counter(1),
                        stage("other-stage-name-1", counter(1), Status.Passed),
                        stage(STAGE_NAME,           counter(1), Status.Failed),
                        stage("other-stage-name-2", counter(1), Status.Passed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(4), Status.Passed))),
                thenExpectStatus(Status.Fixed)
        },

        // One history pipeline, next pipeline run
        {
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Failed))),
                thenExpectStatus(Status.Broken)
        }, {
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed))),
                thenExpectStatus(Status.Passed)
        }, {
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Failed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed))),
                thenExpectStatus(Status.Fixed)
        }, {
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Failed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Failed))),
                thenExpectStatus(Status.Failed)
        }, {
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Failed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Cancelled))),
                thenExpectStatus(Status.Cancelled)
        }, {
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Cancelled))),
                thenExpectStatus(Status.Cancelled)
        },
        // No history
        {
                givenHistory(noPipelines()),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed))),
                thenExpectStatus(Status.Passed)
        }, {
                givenHistory(noPipelines()),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Failed))),
                thenExpectStatus(Status.Failed)
        }, {
                givenHistory(noPipelines()),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Cancelled))),
                thenExpectStatus(Status.Cancelled)
        }, {
                givenHistory(noPipelines()),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Building))),
                thenExpectStatus(Status.Building)
        },
        // Longer history, next pipeline run
        {
                givenHistory(pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Failed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Failed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Failed))),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(4), stage(STAGE_NAME, counter(1), Status.Passed))),
                thenExpectStatus(Status.Fixed)
        }, {
                givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Failed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(4), stage(STAGE_NAME, counter(1), Status.Passed))),
                thenExpectStatus(Status.Fixed)
        }, {
                givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Failed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Failed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(4), stage(STAGE_NAME, counter(1), Status.Passed))),
                thenExpectStatus(Status.Fixed)
        }, {
                givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(4), stage(STAGE_NAME, counter(1), Status.Failed))),
                thenExpectStatus(Status.Broken)
        }, {
                givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Failed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Passed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(4), stage(STAGE_NAME, counter(1), Status.Failed))),
                thenExpectStatus(Status.Broken)
        }, {
                givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Failed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(4), stage(STAGE_NAME, counter(1), Status.Failed))),
                thenExpectStatus(Status.Failed)
        }, {
                givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Passed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(4), stage(STAGE_NAME, counter(1), Status.Failed))),
                thenExpectStatus(Status.Broken)
        }, {
                givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Passed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(4), stage(STAGE_NAME, counter(1), Status.Cancelled))),
                thenExpectStatus(Status.Cancelled)
        }, {
                givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Failed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(4), stage(STAGE_NAME, counter(1), Status.Cancelled))),
                thenExpectStatus(Status.Cancelled)
        }, {
                givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Failed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Passed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(4), stage(STAGE_NAME, counter(1), Status.Passed))),
                thenExpectStatus(Status.Passed)
        }, {
                givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Failed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Failed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Failed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(4), stage(STAGE_NAME, counter(1), Status.Failed))),
                thenExpectStatus(Status.Failed)
        },
        // Longer history, same pipeline as the last in history
        {
                givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Passed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(3), Status.Failed))),
                thenExpectStatus(Status.Broken)
        }, {
                givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Failed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(3), Status.Passed))),
                thenExpectStatus(Status.Fixed)
        }, {
                givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Failed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(3), Status.Failed))),
                thenExpectStatus(Status.Failed)
        }, {
                givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Passed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(3), Status.Passed))),
                thenExpectStatus(Status.Passed)
        }, {
                givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Failed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(3), Status.Cancelled))),
                thenExpectStatus(Status.Cancelled)
        }, {
                givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Cancelled))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(3), Status.Failed))),
                thenExpectStatus(Status.Failed)
        }, {
                givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Cancelled))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(3), Status.Passed))),
                thenExpectStatus(Status.Fixed)
        }, {
                givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Failed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Passed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(3), Status.Passed))),
                thenExpectStatus(Status.Passed)
        }, {
                givenHistory(
                        pipeline(PIPELINE_NAME, counter(1), stage(STAGE_NAME, counter(1), Status.Failed)),
                        pipeline(PIPELINE_NAME, counter(2), stage(STAGE_NAME, counter(1), Status.Failed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(1), Status.Passed)),
                        pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(2), Status.Failed))
                ),
                whenPipelineFinished(pipeline(PIPELINE_NAME, counter(3), stage(STAGE_NAME, counter(3), Status.Failed))),
                thenExpectStatus(Status.Failed)
        }

        });
    }

    @Test
    public void shouldResolveCorrectStageStatus() throws IOException {
        Server server = mock(Server.class);
        when(server.getPipelineHistory(PIPELINE_NAME)).thenReturn(pipelineHistory);

        GoNotificationMessage message = new GoNotificationMessage(
                TestUtils.createMockServerFactory(server),
                pipeline
        );

        message.tryToFixStageResult(new Rules());

        assertThat(message.getStageResult(), is(expectedStatus));
    }

    /**
     * @param pipelines Pipelines in chronological order, oldest one first.
     * @return History object
     */
    private static History givenHistory(Pipeline... pipelines) {
        History history = new History();
        List<Pipeline> helperList = Arrays.asList(pipelines);
        Collections.reverse(helperList);
        history.pipelines = helperList.toArray(new Pipeline[pipelines.length]);
        return history;
    }

    private static Pipeline pipeline(String name, int counter, Stage... stages) {
        Pipeline pipeline = new Pipeline();
        pipeline.name = name;
        pipeline.counter = counter;
        pipeline.stages = stages;

        return pipeline;
    }

    private static Pipeline[] noPipelines() {
        return new Pipeline[0];
    }

    private static Stage stage(String name, int counter, Status status) {
        Stage stage = new Stage();
        stage.name = name;
        stage.counter = counter;
        stage.result = status.getStatus();
        return stage;
    }

    private static GoNotificationMessage.PipelineInfo whenPipelineFinished(Pipeline pipeline) {
        GoNotificationMessage.PipelineInfo info = new GoNotificationMessage.PipelineInfo();
        info.name = pipeline.name;
        info.counter = Integer.toString(pipeline.counter);
        info.stage = new GoNotificationMessage.StageInfo();

        Stage stage = pipeline.stages[0];
        info.stage.counter = Integer.toString(stage.counter);
        info.stage.name = stage.name;
        info.stage.state = Status.valueOf(stage.result).getStatus();
        info.stage.result = Status.valueOf(stage.result).getResult();
        return info;
    }

    private static String thenExpectStatus(Status status) {
        return status.getStatus();
    }

    private static int counter(int value) {
        return value;
    }

}