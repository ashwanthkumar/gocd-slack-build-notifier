package in.ashwanthkumar.gocd.slack.jsonapi;

import com.google.gson.annotations.SerializedName;

public class Pipeline {
    @SerializedName("id")
    public int id;

    @SerializedName("name")
    public String name;

    @SerializedName("counter")
    public int counter;

    @SerializedName("preparing_to_schedule")
    public boolean preparingToSchedule;

    @SerializedName("can_run")
    public boolean canRun;

    @SerializedName("build_cause")
    public BuildCause buildCause;

    @SerializedName("stages")
    public Stage[] stages;
    
    // "comment"
    // "label"
    // "natural_order"
    // "stages"
}
