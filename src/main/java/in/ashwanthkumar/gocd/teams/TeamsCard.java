package in.ashwanthkumar.gocd.teams;

import com.google.gson.Gson;

/**
 * Populate the values of a Message Card for Teams.
 */
public class TeamsCard {
    private final MessageCardSchema.FactSection factSection = new MessageCardSchema.FactSection();
    private final MessageCardSchema schema = new MessageCardSchema();

    public TeamsCard() {
        this.schema.sections.add(this.factSection);
    }

    public void setTitle(String title) {
        this.schema.title = title;
    }

    public void addFact(String name, String value) {
        this.factSection.facts.add(new MessageCardSchema.Fact(name, value));
    }

    @Override
    public String toString() {
        return new Gson().toJson(schema);
    }

    public void setColor(MessageCardSchema.Color color) {
        this.schema.themeColor = color.getHexCode();
    }

    public void addLinkAction(String name, String uri) {
        this.schema.potentialAction.add(new MessageCardSchema.OpenUriAction(name, uri));
    }
}
