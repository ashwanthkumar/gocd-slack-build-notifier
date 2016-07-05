package in.ashwanthkumar.gocd.slack;

import in.ashwanthkumar.gocd.slack.base.PluginConfig;

public class SlackPluginSettings {

    @PluginConfig(id = "server-url-external", displayName = "External GoCD Server", displayOrder = 1)
    private String externalServerUrl;

    @PluginConfig(id = "pluginConfig", displayName = "Pipeline Notification Rules", displayOrder = 2)
    private String pipelineConfig;

}
