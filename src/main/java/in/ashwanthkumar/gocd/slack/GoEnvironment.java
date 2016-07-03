package in.ashwanthkumar.gocd.slack;

import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper around System.getenv, where in we can plug in custom values for unit testing
 */
public class GoEnvironment {
    private Map<String, String> env = new HashMap<String, String>();

    public String getenv(String name) {
        if(env.containsKey(name)) return env.get(name);
        else return System.getenv(name);
    }

    /* default */ GoEnvironment setEnv(String name, String value) {
        env.put(name, value);
        return this;
    }
}
