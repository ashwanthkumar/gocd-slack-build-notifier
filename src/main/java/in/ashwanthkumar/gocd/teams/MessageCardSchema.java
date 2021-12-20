package in.ashwanthkumar.gocd.teams;

import com.google.gson.annotations.SerializedName;
import in.ashwanthkumar.gocd.slack.ruleset.PipelineStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * These objects create the MessageCard JSON sent to Teams using {@link com.google.gson.Gson}.
 * More details:
 * https://docs.microsoft.com/en-us/outlook/actionable-messages/message-card-reference
 */
public class MessageCardSchema {
    @SerializedName("@type")
    String type = "MessageCard";
    String themeColor = Color.NONE.getHexCode();
    String title = "";
    /**
     * Not sure what this does, but a summary or text field is required.
     */
    String summary = "GoCD build update";
    List<FactSection> sections = new ArrayList<>();
    List<Object> potentialAction = new ArrayList<>();

    public enum Color {
        NONE(""),
        RED("990000"),
        GREEN("009900");

        private final String hexCode;

        Color(String hexCode) {
            this.hexCode = hexCode;
        }

        public static Color findColor(PipelineStatus status) {
            switch (status) {
                case PASSED:
                case FIXED:
                    return Color.GREEN;
                case FAILED:
                case BROKEN:
                    return Color.RED;
                default:
                    return Color.NONE;
            }
        }

        public String getHexCode() {
            return this.hexCode;
        }
    }

    public static class Fact {
        String name = "";
        String value = "";

        public Fact(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    public static class FactSection {
        List<Fact> facts = new ArrayList<>();
    }

    public static class OpenUriAction {
        @SerializedName("@type")
        String type = "OpenUri";
        String name = "";
        List<Target> targets = new ArrayList<>();

        public OpenUriAction(String name, String uri) {
            this.name = name;
            this.targets.add(new MessageCardSchema.Target(uri));
        }
    }

    public static class Target {
        String os = "default";
        String uri = "";

        public Target(String uri) {
            this.uri = uri;
        }
    }
}
