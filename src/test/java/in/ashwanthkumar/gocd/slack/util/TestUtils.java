package in.ashwanthkumar.gocd.slack.util;

import in.ashwanthkumar.gocd.slack.jsonapi.Server;
import in.ashwanthkumar.gocd.slack.jsonapi.ServerFactory;
import in.ashwanthkumar.gocd.slack.ruleset.Rules;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtils {

    public static ServerFactory createMockServerFactory(Server server) {
        ServerFactory factory = mock(ServerFactory.class);
        when(factory.getServer(any(Rules.class))).thenReturn(server);
        return factory;
    }

}
