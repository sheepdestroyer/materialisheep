package io.github.sheepdestroyer.materialisheep.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLooper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@RunWith(RobolectricTestRunner.class)
public class FileDownloaderTest {

    private Context mContext;
    private Call.Factory mMockCallFactory;
    private Call mMockCall;
    private FileDownloader.FileDownloaderCallback mMockCallback;
    private FileDownloader mFileDownloader;

    @Before
    public void setUp() {
        mContext = ApplicationProvider.getApplicationContext();
        mMockCallFactory = mock(Call.Factory.class);
        mMockCall = mock(Call.class);
        mMockCallback = mock(FileDownloader.FileDownloaderCallback.class);

        mFileDownloader = new FileDownloader(mContext, mMockCallFactory);
    }

    @After
    public void tearDown() {
        File cacheDir = mContext.getCacheDir();
        if (cacheDir.exists()) {
            File[] files = cacheDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    // Delete files created during tests
                    file.delete();
                }
            }
        }
    }

    @Test
    public void downloadFile_whenFileExistsInCache_callsSuccessWithoutNetworkCall() throws IOException {
        String filename = "existing_file.txt";
        String url = "https://example.com/" + filename;
        File cachedFile = new File(mContext.getCacheDir(), filename);
        try (FileWriter writer = new FileWriter(cachedFile)) {
            writer.write("cached content");
        }

        mFileDownloader.downloadFile(url, "text/plain", mMockCallback);
        ShadowLooper.idleMainLooper();

        verifyNoInteractions(mMockCallFactory);
        verify(mMockCallback).onSuccess(cachedFile.getPath());
    }

    @Test
    public void downloadFile_whenFileNotCached_dispatchesRequestAndSavesFile() throws IOException {
        String filename = "download_file.txt";
        String url = "https://example.com/" + filename;
        String mimeType = "text/plain";
        String fileContent = "hello world downloader";

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(Callback.class);

        when(mMockCallFactory.newCall(requestCaptor.capture())).thenReturn(mMockCall);

        mFileDownloader.downloadFile(url, mimeType, mMockCallback);

        verify(mMockCallFactory).newCall(any(Request.class));
        verify(mMockCall).enqueue(callbackCaptor.capture());

        Request capturedRequest = requestCaptor.getValue();
        assertEquals(url, capturedRequest.url().toString());
        assertEquals(mimeType, capturedRequest.header("Content-Type"));

        ResponseBody body = ResponseBody.create(fileContent, MediaType.parse(mimeType));
        Response response = new Response.Builder()
                .request(capturedRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(body)
                .build();

        callbackCaptor.getValue().onResponse(mMockCall, response);
        ShadowLooper.idleMainLooper();

        File outputFile = new File(mContext.getCacheDir(), filename);
        assertTrue(outputFile.exists());
        String savedContent = new String(Files.readAllBytes(outputFile.toPath()));
        assertEquals(fileContent, savedContent);

        verify(mMockCallback).onSuccess(outputFile.getPath());
    }

    @Test
    public void downloadFile_whenNetworkFails_invokesFailureCallback() {
        String filename = "failed_file.txt";
        String url = "https://example.com/" + filename;
        String mimeType = "text/plain";
        IOException ioException = new IOException("Network error");

        ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(Callback.class);
        when(mMockCallFactory.newCall(any(Request.class))).thenReturn(mMockCall);

        mFileDownloader.downloadFile(url, mimeType, mMockCallback);

        verify(mMockCall).enqueue(callbackCaptor.capture());

        callbackCaptor.getValue().onFailure(mMockCall, ioException);
        ShadowLooper.idleMainLooper();

        verify(mMockCallback).onFailure(mMockCall, ioException);
    }

    @Test
    public void downloadFile_whenResponseBodyStreamFails_invokesFailureCallback() throws IOException {
        String filename = "stream_failed_file.txt";
        String url = "https://example.com/" + filename;
        String mimeType = "text/plain";

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(Callback.class);

        when(mMockCallFactory.newCall(requestCaptor.capture())).thenReturn(mMockCall);

        mFileDownloader.downloadFile(url, mimeType, mMockCallback);

        verify(mMockCall).enqueue(callbackCaptor.capture());

        ResponseBody failingBody = mock(ResponseBody.class);
        when(failingBody.source()).thenAnswer(invocation -> {
            throw new IOException("Stream read error");
        });

        Request capturedRequest = requestCaptor.getValue();
        Response response = new Response.Builder()
                .request(capturedRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(failingBody)
                .build();

        callbackCaptor.getValue().onResponse(mMockCall, response);
        ShadowLooper.idleMainLooper();

        verify(mMockCallback).onFailure(any(Call.class), any(IOException.class));
    }
}
