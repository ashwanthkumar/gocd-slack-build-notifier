package in.ashwanthkumar.gocd.slack.base.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import in.ashwanthkumar.gocd.slack.base.Configurations;

public final class GsonFactory {
    private final GsonBuilder gsonBuilder;
    private static final GsonFactory INSTANCE = new GsonFactory();

    /**
     * Get a correct configured gson
     *
     * @return Gson implementation
     */
    public static Gson getGson() {
        return GsonFactory.INSTANCE.gsonBuilder.create();
    }

    private GsonFactory() {
        gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Configurations.class, new ConfigurationsSerializer());
        gsonBuilder.disableHtmlEscaping();
    }
}
