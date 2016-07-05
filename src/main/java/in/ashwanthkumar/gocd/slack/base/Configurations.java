package in.ashwanthkumar.gocd.slack.base;

import java.util.Iterator;
import java.util.List;

public class Configurations implements Iterable<Configuration> {
    private List<Configuration> configurations;

    public Configurations(List<Configuration> configurations) {
        this.configurations = configurations;
    }

    @Override
    public Iterator<Configuration> iterator() {
        return configurations.iterator();
    }
}
