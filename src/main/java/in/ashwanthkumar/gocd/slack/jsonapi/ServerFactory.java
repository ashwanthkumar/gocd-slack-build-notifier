package in.ashwanthkumar.gocd.slack.jsonapi;

import in.ashwanthkumar.gocd.slack.ruleset.Rules;

public class ServerFactory {

    public Server getServer(Rules rules) {
        return new Server(rules);
    }
}
