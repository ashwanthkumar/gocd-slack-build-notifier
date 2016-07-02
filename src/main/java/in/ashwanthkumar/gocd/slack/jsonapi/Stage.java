package in.ashwanthkumar.gocd.slack.jsonapi;

import com.google.gson.annotations.SerializedName;
import in.ashwanthkumar.utils.collections.Lists;
import in.ashwanthkumar.utils.func.Function;

import java.util.List;

public class Stage {
    @SerializedName("id")
    public int id;

    @SerializedName("name")
    public String name;

    @SerializedName("counter")
    public int counter;

    @SerializedName("result")
    public String result;

    @SerializedName("approved_by")
    public String approvedBy;

    @SerializedName("jobs")
    public Job[] jobs;

    // "approval_type"
    // "can_run"
    // "operate_permission"
    // "rerun_of_counter"
    // "scheduled"

    public List<String> jobNames() {
        return Lists.map(Lists.of(jobs), new Function<Job, String>() {
            @Override
            public String apply(Job input) {
                return input.name;
            }
        });
    }
}
