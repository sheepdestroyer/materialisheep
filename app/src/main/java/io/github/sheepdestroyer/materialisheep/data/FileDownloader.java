package io.github.sheepdestroyer.materialisheep.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.WorkerThread;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import io.github.sheepdestroyer.materialisheep.annotation.Synthetic;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

/**
 * A utility class for downloading files.
 */
public class FileDownloader {
    private Call.Factory mCallFactory;
    private final String mCacheDir;
    @Synthetic final Handler mMainHandler;

    /**
     * Constructs a new `FileDownloader`.
     *
     * @param context     the application context
     * @param callFactory the {@link Call.Factory} to use for creating network calls
     */
    @Inject
    public FileDownloader(Context context, Call.Factory callFactory) {
        mCacheDir = context.getCacheDir().getPath(); // don't need to keep a reference to context after this
        mCallFactory = callFactory;
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Downloads a file from the given URL.
     *
     * @param url      the URL of the file to download
     * @param mimeType the MIME type of the file
     * @param callback the callback to be invoked when the download is complete
     */
    @WorkerThread
    public void downloadFile(String url, String mimeType, FileDownloaderCallback callback) {
        File outputFile = new File(mCacheDir, new File(url).getName());
        if (outputFile.exists()) {
            mMainHandler.post(() -> callback.onSuccess(outputFile.getPath()));
            return;
        }

        final Request request = new Request.Builder().url(url)
                .addHeader("Content-Type", mimeType)
                .build();

        mCallFactory.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mMainHandler.post(() -> callback.onFailure(call, e));
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    BufferedSink sink = Okio.buffer(Okio.sink(outputFile));
                    sink.writeAll(response.body().source());
                    sink.close();
                    mMainHandler.post(() -> callback.onSuccess(outputFile.getPath()));
                } catch (IOException e) {
                    this.onFailure(call, e);
                }
            }
        });
    }

    /**
     * A callback interface for receiving file download results.
     */
    public interface FileDownloaderCallback {
        /**
         * Called when the file download fails.
         *
         * @param call the {@link Call} that failed
         * @param e    the {@link IOException} that occurred
         */
        void onFailure(Call call, IOException e);

        /**
         * Called when the file is downloaded successfully.
         *
         * @param filePath the path to the downloaded file
         */
        void onSuccess(String filePath);
    }
}
