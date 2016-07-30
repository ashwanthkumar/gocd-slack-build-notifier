package in.ashwanthkumar.gocd.slack.base.config;

import in.ashwanthkumar.utils.collections.Lists;
import in.ashwanthkumar.utils.func.Predicate;
import in.ashwanthkumar.utils.lang.option.Option;

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

    public int size() {
        return configurations.size();
    }

    public Configuration get(int index) {
        return configurations.get(index);
    }

    public Option<Configuration> findByName(final String name) {
        return Lists.find(configurations, new Predicate<Configuration>() {
            @Override
            public Boolean apply(Configuration configuration) {
                return configuration.getFieldName().equals(name);
            }
        });
    }
}
