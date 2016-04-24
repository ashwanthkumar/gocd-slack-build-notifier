package in.ashwanthkumar.gocd.slack.jsonapi;

import in.ashwanthkumar.gocd.slack.ruleset.Rules;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class ServerTest {

    @Test
    public void testGetPipelineHistory() throws Exception {
        HttpConnectionUtil httpConnectionUtil = mockConnection();

        Rules rules = new Rules();
        rules.setGoServerHost("https://example.org");
        Server server = new Server(rules, httpConnectionUtil);

        server.getPipelineHistory("pipeline-test");

        ArgumentCaptor<URL> url = ArgumentCaptor.forClass(URL.class);
        verify(httpConnectionUtil).getConnection(
                url.capture()
        );
        assertThat(url.getValue().toString(), is("https://example.org/go/api/pipelines/pipeline-test/history"));
    }

    @Test
    public void testGetPipelineHistoryEvenWhenGoServerHostHasTrailingSlash() throws Exception {
        HttpConnectionUtil httpConnectionUtil = mockConnection();

        Rules rules = new Rules();
        rules.setGoServerHost("https://example.org/");
        Server server = new Server(rules, httpConnectionUtil);

        server.getPipelineHistory("pipeline-test");

        ArgumentCaptor<URL> url = ArgumentCaptor.forClass(URL.class);
        verify(httpConnectionUtil).getConnection(
                url.capture()
        );
        assertThat(url.getValue().toString(), is("https://example.org/go/api/pipelines/pipeline-test/history"));
    }

    @Test
    public void testGetPipelineInstance() throws Exception {
        HttpConnectionUtil httpConnectionUtil = mockConnection();

        Rules rules = new Rules();
        rules.setGoServerHost("https://example.org");
        Server server = new Server(rules, httpConnectionUtil);

        server.getPipelineInstance("pipeline-test", 42);

        ArgumentCaptor<URL> url = ArgumentCaptor.forClass(URL.class);
        verify(httpConnectionUtil).getConnection(
                url.capture()
        );
        assertThat(url.getValue().toString(), is("https://example.org/go/api/pipelines/pipeline-test/instance/42"));
    }

    @Test
    public void shouldConnectWithCredentials() throws IOException {
        HttpConnectionUtil httpConnectionUtil = mockConnection();
        Rules rules = new Rules();
        Server server = new Server(rules, httpConnectionUtil);
        rules.setGoLogin("login");
        rules.setGoPassword("pass");

        HttpURLConnection conn = mock(HttpURLConnection.class);
        when(httpConnectionUtil.getConnection(any(URL.class))).thenReturn(conn);
        when(conn.getContent()).thenReturn(new Object());

        server.getUrl(new URL("http://exmaple.org/"));

        verify(conn).setRequestProperty("Authorization", "Basic bG9naW46cGFzcw==");
    }

    @Test
    public void shouldNotConnectWithoutCredentials() throws IOException {
        HttpConnectionUtil httpConnectionUtil = mockConnection();
        Rules rules = new Rules();
        Server server = new Server(rules, httpConnectionUtil);

        HttpURLConnection conn = mock(HttpURLConnection.class);
        when(httpConnectionUtil.getConnection(any(URL.class))).thenReturn(conn);
        when(conn.getContent()).thenReturn(new Object());

        server.getUrl(new URL("http://exmaple.org/"));

        verify(conn, never()).setRequestProperty(anyString(), anyString());
    }

    @Test
    public void shouldNotConnectWithEmptyPassword() throws IOException {
        HttpConnectionUtil httpConnectionUtil = mockConnection();
        Rules rules = new Rules();
        rules.setGoLogin("login");
        rules.setGoPassword(null);
        Server server = new Server(rules, httpConnectionUtil);

        HttpURLConnection conn = mock(HttpURLConnection.class);
        when(httpConnectionUtil.getConnection(any(URL.class))).thenReturn(conn);
        when(conn.getContent()).thenReturn(new Object());

        server.getUrl(new URL("http://exmaple.org/"));

        verify(conn, never()).setRequestProperty(anyString(), anyString());
    }

    @Test
    public void shouldNotConnectWithEmptyLoginName() throws IOException {
        HttpConnectionUtil httpConnectionUtil = mockConnection();
        Rules rules = new Rules();
        rules.setGoLogin(null);
        rules.setGoPassword("pass");
        Server server = new Server(rules, httpConnectionUtil);

        HttpURLConnection conn = mock(HttpURLConnection.class);
        when(httpConnectionUtil.getConnection(any(URL.class))).thenReturn(conn);
        when(conn.getContent()).thenReturn(new Object());

        server.getUrl(new URL("http://exmaple.org/"));

        verify(conn, never()).setRequestProperty(anyString(), anyString());
    }

    @Test
    public void shouldNotConnectWithEmptyPasswordCredentials() throws IOException {
        HttpConnectionUtil httpConnectionUtil = mockConnection();
        Rules rules = new Rules();
        rules.setGoLogin("");
        rules.setGoPassword("");
        Server server = new Server(rules, httpConnectionUtil);

        HttpURLConnection conn = mock(HttpURLConnection.class);
        when(httpConnectionUtil.getConnection(any(URL.class))).thenReturn(conn);
        when(conn.getContent()).thenReturn(new Object());

        server.getUrl(new URL("http://exmaple.org/"));

        verify(conn, never()).setRequestProperty(anyString(), anyString());
    }

    private HttpConnectionUtil mockConnection() throws IOException {
        HttpConnectionUtil httpConnectionUtil = mock(HttpConnectionUtil.class);

        HttpURLConnection conn = mock(HttpURLConnection.class);
        when(httpConnectionUtil.getConnection(any(URL.class))).thenReturn(conn);
        when(conn.getContent()).thenReturn(new Object());

        return httpConnectionUtil;
    }
}