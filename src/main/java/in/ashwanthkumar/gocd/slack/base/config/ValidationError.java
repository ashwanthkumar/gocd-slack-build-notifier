package in.ashwanthkumar.gocd.slack.base.config;

/**
 * ValidationError Object as per
 * <a href="https://plugin-api.go.cd/16.3.0/notifications/#the-validation-error-object">https://plugin-api.go.cd/16.3.0/notifications/#the-validation-error-object</a>
 */
public class ValidationError {
    private String key;
    private String message;

    public ValidationError(String key, String message) {
        this.key = key;
        this.message = message;
    }

    public String getKey() {
        return key;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ValidationError{" +
                "key='" + key + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValidationError that = (ValidationError) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        return !(message != null ? !message.equals(that.message) : that.message != null);

    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }
}
