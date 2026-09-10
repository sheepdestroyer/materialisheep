package io.github.sheepdestroyer.materialisheep.accounts;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.lang.reflect.Method;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class UserServicesClientTest {

    @Mock
    private Call.Factory callFactory;

    @Mock
    private Call call;

    private UserServicesClient client;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        client = new UserServicesClient(callFactory, Schedulers.trampoline());
    }

    @Test
    public void execute_throwsIOException_emitsError() throws Exception {
        Request request = new Request.Builder().url("http://example.com").build();
        IOException ioException = new IOException("Mock error");

        when(callFactory.newCall(any(Request.class))).thenReturn(call);
        when(call.execute()).thenThrow(ioException);

        Method executeMethod = UserServicesClient.class.getDeclaredMethod("execute", Request.class);
        executeMethod.setAccessible(true);

        @SuppressWarnings("unchecked")
        Observable<Response> observable = (Observable<Response>) executeMethod.invoke(client, request);

        observable.test()
                .assertError(ioException)
                .assertNotComplete();
    }

    @Test
    public void testParseLoginError_ioExceptionReturnsNull() throws Exception {
        Response response = mock(Response.class);
        ResponseBody responseBody = mock(ResponseBody.class);

        when(response.body()).thenReturn(responseBody);
        when(responseBody.string()).thenThrow(new IOException("Mock IO Exception"));

        // Access the private method using reflection since we want to test it specifically
        Method method = UserServicesClient.class.getDeclaredMethod("parseLoginError", Response.class);
        method.setAccessible(true);

        String result = (String) method.invoke(client, response);
        assertNull(result);
    }
}
