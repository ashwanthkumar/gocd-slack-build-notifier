package in.ashwanthkumar.gocd.slack;

public interface PipelineListener {
    public void onSuccess(GoNotificationMessage message) throws Exception;

    public void onFailed(GoNotificationMessage message) throws Exception;

    public void onBroken(GoNotificationMessage message) throws Exception;

    public void onFixed(GoNotificationMessage message) throws Exception;
}
