package in.ashwanthkumar.gocd.slack.jsonapi;

import com.google.gson.annotations.SerializedName;

public class BuildCause {
    @SerializedName("approver")
    public String approver;

    @SerializedName("trigger_forced")
    public String triggerForced;

    @SerializedName("trigger_message")
    public String triggerMessage;

    @SerializedName("material_revisions")
    public MaterialRevision[] materialRevisions;
}
