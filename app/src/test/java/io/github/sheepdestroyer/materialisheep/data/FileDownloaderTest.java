package io.github.sheepdestroyer.materialisheep.data;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLooper;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.MediaType;

@RunWith(RobolectricTestRunner.class)
public class FileDownloaderTest {

    private Context context;

    @Mock
    private Call.Factory callFactory;

    @Mock
    private Call call;

    @Mock
    private FileDownloader.FileDownloaderCallback callback;

    private FileDownloader fileDownloader;
    private File cacheDir;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = ApplicationProvider.getApplicationContext();
        cacheDir = context.getCacheDir();

        when(callFactory.newCall(any(Request.class))).thenReturn(call);

        fileDownloader = new FileDownloader(context, callFactory);
    }

    @Test
    public void downloadFile_fileExists_callsOnSuccess() throws IOException {
        String url = "http://example.com/testfile.txt";
        File testFile = new File(cacheDir, "testfile.txt");
        testFile.createNewFile();

        fileDownloader.downloadFile(url, "text/plain", callback);
        ShadowLooper.idleMainLooper();

        verify(callback).onSuccess(testFile.getPath());
        testFile.delete();
    }

    @Test
    public void downloadFile_networkSuccess_callsOnSuccess() throws IOException {
        String url = "http://example.com/testfile2.txt";
        File testFile = new File(cacheDir, "testfile2.txt");
        if(testFile.exists()) testFile.delete();

        doAnswer(invocation -> {
            Callback okHttpCallback = invocation.getArgument(0);
            Response response = new Response.Builder()
                .request(new Request.Builder().url(url).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(MediaType.parse("text/plain"), "test content"))
                .build();
            okHttpCallback.onResponse(call, response);
            return null;
        }).when(call).enqueue(any(Callback.class));

        fileDownloader.downloadFile(url, "text/plain", callback);
        ShadowLooper.idleMainLooper();

        verify(callback).onSuccess(testFile.getPath());
        testFile.delete();
    }

    @Test
    public void downloadFile_networkFailure_callsOnFailure() throws IOException {
        String url = "http://example.com/testfile3.txt";
        File testFile = new File(cacheDir, "testfile3.txt");
        if(testFile.exists()) testFile.delete();

        IOException exception = new IOException("Network error");
        doAnswer(invocation -> {
            Callback okHttpCallback = invocation.getArgument(0);
            okHttpCallback.onFailure(call, exception);
            return null;
        }).when(call).enqueue(any(Callback.class));

        fileDownloader.downloadFile(url, "text/plain", callback);
        ShadowLooper.idleMainLooper();

        verify(callback).onFailure(call, exception);
        testFile.delete();
    }

    @Test
    public void downloadFile_networkSuccessButIoException_callsOnFailure() throws IOException {
        String url = "http://example.com/testfile4.txt";
        File testFile = new File(cacheDir, "testfile4.txt");
        if(testFile.exists()) testFile.delete();

        doAnswer(invocation -> {
            Callback okHttpCallback = invocation.getArgument(0);

            // Return an invalid response to throw IOException in Okio sink writing
            ResponseBody mockBody = mock(ResponseBody.class);
            when(mockBody.source()).thenAnswer(inv -> {
                throw new IOException("File write error");
            });

            Response response = new Response.Builder()
                .request(new Request.Builder().url(url).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(mockBody)
                .build();

            okHttpCallback.onResponse(call, response);
            return null;
        }).when(call).enqueue(any(Callback.class));

        fileDownloader.downloadFile(url, "text/plain", callback);
        ShadowLooper.idleMainLooper();

        verify(callback).onFailure(any(), any());
        testFile.delete();
    }
}
