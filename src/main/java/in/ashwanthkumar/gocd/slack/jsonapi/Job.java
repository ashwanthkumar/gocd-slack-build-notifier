package in.ashwanthkumar.gocd.slack.jsonapi;

import com.google.gson.annotations.SerializedName;

public class Job {
    @SerializedName("name")
    public String name;

    @SerializedName("result")
    public String result;

    @SerializedName("state")
    public String state;

    @SerializedName("id")
    private int id;

    @SerializedName("scheduled_date")
    private long scheduledDate;
}
