package in.ashwanthkumar.gocd.slack.jsonapi;

import com.google.gson.annotations.SerializedName;

public class MaterialRevision {
    @SerializedName("changed")
    public boolean changed;

    @SerializedName("material")
    public Material material;

    @SerializedName("modifications")
    public Modification[] modifications;

    /**
     * Is this revision a pipeline, or something else (generally a commit
     * to version control system)?
     */
    public boolean isPipeline() {
        return material.isPipeline();
    }

}
