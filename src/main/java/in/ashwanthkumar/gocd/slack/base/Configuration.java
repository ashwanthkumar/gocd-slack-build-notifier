package in.ashwanthkumar.gocd.slack.base;

import com.google.gson.annotations.SerializedName;
import in.ashwanthkumar.utils.lang.StringUtils;

import java.util.Map;
import java.util.TreeMap;

/**
 * Represents a plugin configuration
 */
public class Configuration {
    private transient String id;

    @SerializedName("display-name")
    private String displayName;

    @SerializedName("default-value")
    private String defaultValue;

    @SerializedName("display-order")
    private Integer displayOrder;

    @SerializedName("required")
    private boolean required;

    @SerializedName("secure")
    private boolean secure;

    public Map<String, Object> asMap() {
        Map<String, Object> map = new TreeMap<>();
        map.put("display-name", displayName);
        map.put("default-value", defaultValue);
        map.put("display-order", displayOrder);
        map.put("required", required);
        map.put("secure", secure);
        return map;
    }

    public String getId() {
        return id;
    }

    public Configuration setId(String id) {
        this.id = id;
        return this;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Configuration setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public Configuration setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public Configuration setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
        return this;
    }

    public boolean isRequired() {
        return required;
    }

    public Configuration setRequired(boolean required) {
        this.required = required;
        return this;
    }

    public boolean isSecure() {
        return secure;
    }

    public Configuration setSecure(boolean secure) {
        this.secure = secure;
        return this;
    }
}
