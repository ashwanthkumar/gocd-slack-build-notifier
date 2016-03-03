package in.ashwanthkumar.gocd.slack.jsonapi;

import com.google.gson.annotations.SerializedName;

public class Stage {
    @SerializedName("id")
    public int id;

    @SerializedName("name")
    public String name;

    @SerializedName("counter")
    public int counter;

    @SerializedName("result")
    public String result;

    // "approval_type"
    // "approved_by"
    // "can_run"
    // "jobs"
    // "operate_permission"
    // "rerun_of_counter"
    // "scheduled"
}
