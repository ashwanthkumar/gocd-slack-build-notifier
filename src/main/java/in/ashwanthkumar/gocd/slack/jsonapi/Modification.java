package in.ashwanthkumar.gocd.slack.jsonapi;

import com.google.gson.annotations.SerializedName;

public class Modification {
    // Format: "cucumber/102/BuildAndPublish/1" for pipelines, and
    // "2d110a724f3e716f801b6e87d420d7f0c32a208f" for git commits.
    @SerializedName("revision")
    public String revision;

    @SerializedName("comment")
    public String comment;

    @SerializedName("user_name")
    public String userName;

    //"modified_time": 1436365681065,
    //"id": 12184,
    //"email_address": null
}
