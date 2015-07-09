package in.ashwanthkumar.gocd.slack.jsonapi;

import com.google.gson.annotations.SerializedName;

public class History {
    @SerializedName("pipelines")
    public Pipeline[] pipelines;
}

