package in.ashwanthkumar.gocd.slack.base.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import in.ashwanthkumar.gocd.slack.base.config.Configurations;
import in.ashwanthkumar.gocd.slack.base.config.ValidateConfig;

public final class GsonFactory {
    private final GsonBuilder gsonBuilder;
    public static final Gson GSON = new GsonFactory().gsonBuilder.create();

    /**
     * Get a correct configured gson
     *
     * @deprecated
     * @return Gson implementation
     */
    public static Gson getGson() {
        return GSON;
    }

    private GsonFactory() {
        gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Configurations.class, new ConfigurationsSerDe());
        gsonBuilder.registerTypeAdapter(ValidateConfig.class, new ValidationConfigSerDe());
        gsonBuilder.disableHtmlEscaping();
    }
}
