package io.github.sheepdestroyer.materialisheep.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLooper;

import java.io.File;
import java.io.IOException;

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
    private Call.Factory mCallFactory;
    private FileDownloader mFileDownloader;

    @Before
    public void setUp() {
        mContext = ApplicationProvider.getApplicationContext();
        mCallFactory = mock(Call.Factory.class);
        mFileDownloader = new FileDownloader(mContext, mCallFactory);
    }

    @Test
    public void testSanitizeFileName() {
        assertEquals("test.pdf", FileDownloader.sanitizeFileName("https://example.com/test.pdf"));
        assertEquals("sample.png", FileDownloader.sanitizeFileName("https://example.com/a/b/sample.png?v=123"));
        assertEquals("passwd", FileDownloader.sanitizeFileName("../../etc/passwd"));
        assertNull(FileDownloader.sanitizeFileName("https://example.com/.."));
        assertNull(FileDownloader.sanitizeFileName(".."));
        assertNull(FileDownloader.sanitizeFileName("."));
        assertNull(FileDownloader.sanitizeFileName("   "));
        assertNull(FileDownloader.sanitizeFileName(null));
    }

    @Test
    public void testDownloadFile_PathTraversalRejected() {
        FileDownloader.FileDownloaderCallback callback = mock(FileDownloader.FileDownloaderCallback.class);

        mFileDownloader.downloadFile("https://example.com/..", "application/pdf", callback);
        ShadowLooper.idleMainLooper();

        ArgumentCaptor<IOException> exceptionCaptor = ArgumentCaptor.forClass(IOException.class);
        verify(callback).onFailure(any(), exceptionCaptor.capture());
        assertTrue(exceptionCaptor.getValue().getMessage().contains("Invalid file name")
                || exceptionCaptor.getValue().getMessage().contains("Path traversal"));
    }

    @Test
    public void testDownloadFile_Success() throws IOException {
        Call call = mock(Call.class);
        when(mCallFactory.newCall(any(Request.class))).thenReturn(call);

        FileDownloader.FileDownloaderCallback callback = mock(FileDownloader.FileDownloaderCallback.class);

        mFileDownloader.downloadFile("https://example.com/file.pdf", "application/pdf", callback);

        ArgumentCaptor<Callback> okHttpCallbackCaptor = ArgumentCaptor.forClass(Callback.class);
        verify(call).enqueue(okHttpCallbackCaptor.capture());

        Response response = new Response.Builder()
                .request(new Request.Builder().url("https://example.com/file.pdf").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("content", MediaType.parse("application/pdf")))
                .build();

        okHttpCallbackCaptor.getValue().onResponse(call, response);
        ShadowLooper.idleMainLooper();

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(callback).onSuccess(pathCaptor.capture());

        File downloadedFile = new File(pathCaptor.getValue());
        assertTrue(downloadedFile.exists());
        assertTrue(downloadedFile.getCanonicalPath().startsWith(mContext.getCacheDir().getCanonicalPath()));
    }

    @Test
    public void testDownloadFile_CachedFileExists() throws IOException {
        File cachedFile = new File(mContext.getCacheDir(), "already_cached.pdf");
        assertTrue(cachedFile.createNewFile());

        FileDownloader.FileDownloaderCallback callback = mock(FileDownloader.FileDownloaderCallback.class);

        mFileDownloader.downloadFile("https://example.com/already_cached.pdf", "application/pdf", callback);
        ShadowLooper.idleMainLooper();

        verify(callback).onSuccess(cachedFile.getCanonicalPath());
    }
}
