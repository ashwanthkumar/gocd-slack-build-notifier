package in.ashwanthkumar.gocd.slack;

public enum Status {
    Building("Unknown"),
    Failing("Failed"),
    Passed("Passed"),
    Failed("Failed"),
    Unknown("Unknown"),
    Cancelled("Cancelled"),

    // Non standard
    Broken("Failed"),
    Fixed("Passed")
    ;

    private String status;
    private String result;

    Status(String result) {
        this.status = this.toString();
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public String getResult() {
        return result;
    }
}
