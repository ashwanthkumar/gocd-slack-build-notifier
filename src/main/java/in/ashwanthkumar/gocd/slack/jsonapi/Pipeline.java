package in.ashwanthkumar.gocd.slack.jsonapi;

import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Collect all changed MaterialRevision objects, walking changed
     * "Pipeline" objects recursively instead of including them directly.
     */
    public List<MaterialRevision> rootChanges(Server server)
        throws MalformedURLException, IOException
    {
        List result = new ArrayList();
        addChangesRecursively(server, result);
        return result;
    }

    void addChangesRecursively(Server server, List<MaterialRevision> outChanges)
        throws MalformedURLException, IOException
    {
        for (MaterialRevision mr : buildCause.materialRevisions) {
            mr.addChangesRecursively(server, outChanges);
        }
    }

    @Override
    public String toString()  {
        if (stages != null && stages.length > 0) {
            return name + "/" + counter + "/" + stages[0].name + "/" + stages[0].result;
        } else {
            return name + "/" + counter;
        }
    }
}
