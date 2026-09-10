package io.github.sheepdestroyer.materialisheep.accounts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.net.HttpURLConnection;

import io.github.sheepdestroyer.materialisheep.BuildConfig;
import io.github.sheepdestroyer.materialisheep.Preferences;
import io.github.sheepdestroyer.materialisheep.R;
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@RunWith(RobolectricTestRunner.class)
public class UserServicesClientTest {

    private Call.Factory mCallFactory;
    private Call mCall;
    private UserServicesClient mClient;
    private Context mContext;

    @Before
    public void setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(scheduler -> Schedulers.trampoline());
        RxAndroidPlugins.setMainThreadSchedulerHandler(scheduler -> Schedulers.trampoline());

        mCallFactory = mock(Call.Factory.class);
        mCall = mock(Call.class);
        when(mCallFactory.newCall(any(Request.class))).thenReturn(mCall);
        mClient = new UserServicesClient(mCallFactory, Schedulers.trampoline());
        mContext = ApplicationProvider.getApplicationContext();
    }

    @After
    public void tearDown() {
        RxAndroidPlugins.reset();
    }

    private void setupAccount(String username, String password) {
        Preferences.setUsername(mContext, username);
        Account account = new Account(username, BuildConfig.APPLICATION_ID);
        AccountManager accountManager = AccountManager.get(mContext);
        accountManager.addAccountExplicitly(account, password, null);
    }

    @Test
    public void testLoginNetworkError() throws IOException {
        IOException networkException = new IOException("Network error");
        when(mCall.execute()).thenThrow(networkException);

        UserServices.Callback callback = mock(UserServices.Callback.class);
        mClient.login("user", "pass", false, callback);

        verify(callback).onError(networkException);
    }

    @Test
    public void testLoginParseLoginErrorIOException() throws IOException {
        ResponseBody mockResponseBody = mock(ResponseBody.class);
        when(mockResponseBody.string()).thenThrow(new IOException("Read error"));

        Request dummyRequest = new Request.Builder().url("https://news.ycombinator.com/login").build();
        Response response = new Response.Builder()
                .request(dummyRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_OK)
                .message("OK")
                .body(mockResponseBody)
                .build();

        when(mCall.execute()).thenReturn(response);

        UserServices.Callback callback = mock(UserServices.Callback.class);
        mClient.login("user", "pass", false, callback);

        ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
        verify(callback).onError(captor.capture());
        assertTrue(captor.getValue() instanceof UserServices.Exception);
        UserServices.Exception exception = (UserServices.Exception) captor.getValue();
        assertNull(exception.getMessage());
    }

    @Test
    public void testLoginParseLoginErrorSuccess() throws IOException {
        String errorHtml = "<html><body>Bad login</body></html>";
        ResponseBody body = ResponseBody.create(errorHtml, MediaType.parse("text/html"));

        Request dummyRequest = new Request.Builder().url("https://news.ycombinator.com/login").build();
        Response response = new Response.Builder()
                .request(dummyRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_OK)
                .message("OK")
                .body(body)
                .build();

        when(mCall.execute()).thenReturn(response);

        UserServices.Callback callback = mock(UserServices.Callback.class);
        mClient.login("user", "pass", false, callback);

        ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
        verify(callback).onError(captor.capture());
        assertTrue(captor.getValue() instanceof UserServices.Exception);
        UserServices.Exception exception = (UserServices.Exception) captor.getValue();
        assertEquals("Bad login", exception.getMessage());
    }

    @Test
    public void testLoginSuccessRedirect() throws IOException {
        ResponseBody body = ResponseBody.create("", MediaType.parse("text/html"));
        Request dummyRequest = new Request.Builder().url("https://news.ycombinator.com/login").build();
        Response response = new Response.Builder()
                .request(dummyRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_MOVED_TEMP)
                .message("Moved Temporarily")
                .body(body)
                .build();

        when(mCall.execute()).thenReturn(response);

        UserServices.Callback callback = mock(UserServices.Callback.class);
        mClient.login("user", "pass", false, callback);

        verify(callback).onDone(true);
    }

    @Test
    public void testVoteUpNoCredentials() {
        UserServices.Callback callback = mock(UserServices.Callback.class);
        boolean result = mClient.voteUp(mContext, "123", callback);

        assertFalse(result);
    }

    @Test
    public void testVoteUpNetworkError() throws IOException {
        setupAccount("user", "pass");
        IOException networkException = new IOException("Network error");
        when(mCall.execute()).thenThrow(networkException);

        UserServices.Callback callback = mock(UserServices.Callback.class);
        boolean result = mClient.voteUp(mContext, "123", callback);

        assertTrue(result);
        verify(callback).onError(networkException);
    }

    @Test
    public void testVoteUpSuccess() throws IOException {
        setupAccount("user", "pass");
        ResponseBody body = ResponseBody.create("", MediaType.parse("text/html"));
        Request dummyRequest = new Request.Builder().url("https://news.ycombinator.com/vote").build();
        Response response = new Response.Builder()
                .request(dummyRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_MOVED_TEMP)
                .message("Moved Temporarily")
                .body(body)
                .build();

        when(mCall.execute()).thenReturn(response);

        UserServices.Callback callback = mock(UserServices.Callback.class);
        boolean result = mClient.voteUp(mContext, "123", callback);

        assertTrue(result);
        verify(callback).onDone(true);
    }

    @Test
    public void testReplyNoCredentials() {
        UserServices.Callback callback = mock(UserServices.Callback.class);
        mClient.reply(mContext, "123", "text", callback);

        verify(callback).onDone(false);
    }

    @Test
    public void testReplyNetworkError() throws IOException {
        setupAccount("user", "pass");
        IOException networkException = new IOException("Network error");
        when(mCall.execute()).thenThrow(networkException);

        UserServices.Callback callback = mock(UserServices.Callback.class);
        mClient.reply(mContext, "123", "text", callback);

        verify(callback).onError(networkException);
    }

    @Test
    public void testReplySuccess() throws IOException {
        setupAccount("user", "pass");
        ResponseBody body = ResponseBody.create("", MediaType.parse("text/html"));
        Request dummyRequest = new Request.Builder().url("https://news.ycombinator.com/comment").build();
        Response response = new Response.Builder()
                .request(dummyRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_MOVED_TEMP)
                .message("Moved Temporarily")
                .body(body)
                .build();

        when(mCall.execute()).thenReturn(response);

        UserServices.Callback callback = mock(UserServices.Callback.class);
        mClient.reply(mContext, "123", "text", callback);

        verify(callback).onDone(true);
    }

    @Test
    public void testSubmitNoCredentials() {
        UserServices.Callback callback = mock(UserServices.Callback.class);
        mClient.submit(mContext, "title", "content", false, callback);

        verify(callback).onDone(false);
    }

    @Test
    public void testSubmitNetworkError() throws IOException {
        setupAccount("user", "pass");
        IOException networkException = new IOException("Network error");
        when(mCall.execute()).thenThrow(networkException);

        UserServices.Callback callback = mock(UserServices.Callback.class);
        mClient.submit(mContext, "title", "content", false, callback);

        verify(callback).onError(networkException);
    }

    @Test
    public void testSubmitFormRedirectFailure() throws IOException {
        setupAccount("user", "pass");
        ResponseBody body = ResponseBody.create("", MediaType.parse("text/html"));
        Request dummyRequest = new Request.Builder().url("https://news.ycombinator.com/submit").build();
        Response response = new Response.Builder()
                .request(dummyRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_MOVED_TEMP)
                .message("Redirect to login")
                .body(body)
                .build();

        when(mCall.execute()).thenReturn(response);

        UserServices.Callback callback = mock(UserServices.Callback.class);
        mClient.submit(mContext, "title", "content", false, callback);

        ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
        verify(callback).onError(captor.capture());
        assertTrue(captor.getValue() instanceof IOException);
        assertEquals("Login failed, received redirect", captor.getValue().getMessage());
    }

    @Test
    public void testSubmitMissingFnidFailure() throws IOException {
        setupAccount("user", "pass");
        String formHtml = "<html><body><form></form></body></html>";
        ResponseBody body = ResponseBody.create(formHtml, MediaType.parse("text/html"));
        Request dummyRequest = new Request.Builder().url("https://news.ycombinator.com/submit").build();
        Response response = new Response.Builder()
                .request(dummyRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_OK)
                .message("OK")
                .body(body)
                .build();

        when(mCall.execute()).thenReturn(response);

        UserServices.Callback callback = mock(UserServices.Callback.class);
        mClient.submit(mContext, "title", "content", false, callback);

        ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
        verify(callback).onError(captor.capture());
        assertTrue(captor.getValue() instanceof IOException);
        assertEquals("Failed to get fnid for submission", captor.getValue().getMessage());
    }

    @Test
    public void testSubmitSuccess() throws IOException {
        setupAccount("user", "pass");
        String formHtml = "<html><body><input name=\"fnid\" value=\"test_fnid_123\" /></body></html>";
        ResponseBody formBody = ResponseBody.create(formHtml, MediaType.parse("text/html"));
        Request formRequest = new Request.Builder().url("https://news.ycombinator.com/submit").build();
        Response formResponse = new Response.Builder()
                .request(formRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_OK)
                .message("OK")
                .header("set-cookie", "session=123")
                .body(formBody)
                .build();

        ResponseBody submitBody = ResponseBody.create("", MediaType.parse("text/html"));
        Request submitRequest = new Request.Builder().url("https://news.ycombinator.com/r").build();
        Response submitResponse = new Response.Builder()
                .request(submitRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_MOVED_TEMP)
                .message("Moved Temporarily")
                .header("location", "newest")
                .body(submitBody)
                .build();

        when(mCall.execute()).thenReturn(formResponse).thenReturn(submitResponse);

        UserServices.Callback callback = mock(UserServices.Callback.class);
        mClient.submit(mContext, "title", "content", false, callback);

        verify(callback).onDone(true);
    }

    @Test
    public void testSubmitItemExistError() throws IOException {
        setupAccount("user", "pass");
        String formHtml = "<html><body><input name=\"fnid\" value=\"test_fnid_123\" /></body></html>";
        ResponseBody formBody = ResponseBody.create(formHtml, MediaType.parse("text/html"));
        Request formRequest = new Request.Builder().url("https://news.ycombinator.com/submit").build();
        Response formResponse = new Response.Builder()
                .request(formRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_OK)
                .message("OK")
                .header("set-cookie", "session=123")
                .body(formBody)
                .build();

        ResponseBody submitBody = ResponseBody.create("", MediaType.parse("text/html"));
        Request submitRequest = new Request.Builder().url("https://news.ycombinator.com/r").build();
        Response submitResponse = new Response.Builder()
                .request(submitRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_MOVED_TEMP)
                .message("Moved Temporarily")
                .header("location", "item?id=999")
                .body(submitBody)
                .build();

        when(mCall.execute()).thenReturn(formResponse).thenReturn(submitResponse);

        UserServices.Callback callback = mock(UserServices.Callback.class);
        mClient.submit(mContext, "title", "content", false, callback);

        ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
        verify(callback).onError(captor.capture());
        assertTrue(captor.getValue() instanceof UserServices.Exception);
        UserServices.Exception exception = (UserServices.Exception) captor.getValue();
        assertEquals(R.string.item_exist, exception.message);
        assertNotNull(exception.data);
        assertEquals("999", exception.data.getLastPathSegment());
    }
}
