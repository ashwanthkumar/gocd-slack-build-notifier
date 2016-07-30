package in.ashwanthkumar.gocd.slack.base.config;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.TreeMap;

/**
 * Represents a plugin configuration
 */
public class Configuration implements Comparable<Configuration> {
    private transient String id;
    private transient String fieldName;

    @SerializedName("display-name")
    private String displayName;

    @SerializedName("default-value")
    private String defaultValue = "";

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

    public String getFieldName() {
        return fieldName;
    }

    public Configuration setFieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "id='" + id + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", displayName='" + displayName + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                ", displayOrder=" + displayOrder +
                ", required=" + required +
                ", secure=" + secure +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Configuration that = (Configuration) o;

        if (required != that.required) return false;
        if (secure != that.secure) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (fieldName != null ? !fieldName.equals(that.fieldName) : that.fieldName != null) return false;
        if (displayName != null ? !displayName.equals(that.displayName) : that.displayName != null) return false;
        if (defaultValue != null ? !defaultValue.equals(that.defaultValue) : that.defaultValue != null) return false;
        return !(displayOrder != null ? !displayOrder.equals(that.displayOrder) : that.displayOrder != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (fieldName != null ? fieldName.hashCode() : 0);
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        result = 31 * result + (displayOrder != null ? displayOrder.hashCode() : 0);
        result = 31 * result + (required ? 1 : 0);
        result = 31 * result + (secure ? 1 : 0);
        return result;
    }

    @Override
    public int compareTo(Configuration o) {
        return displayOrder.compareTo(o.displayOrder);
    }
}
