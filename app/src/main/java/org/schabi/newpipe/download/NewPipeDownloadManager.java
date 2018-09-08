package org.schabi.newpipe.download;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.schedulers.Schedulers;
import org.schabi.newpipe.NewPipeDatabase;
import org.schabi.newpipe.database.AppDatabase;
import org.schabi.newpipe.database.download.DownloadDAO;
import org.schabi.newpipe.database.download.DownloadEntity;
import org.schabi.newpipe.database.download.DownloadEntry;
import org.schabi.newpipe.database.stream.dao.StreamDAO;
import org.schabi.newpipe.database.stream.model.StreamEntity;
import org.schabi.newpipe.extractor.stream.StreamInfo;

import java.util.Date;
import java.util.List;

public class NewPipeDownloadManager {
    private DownloadManager downloadManager;

    private AppDatabase database;
    private StreamDAO streamTable;
    private DownloadDAO downloadTable;

    public NewPipeDownloadManager(final Context context) {
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        database = NewPipeDatabase.getInstance(context);
        streamTable = database.streamDAO();
        downloadTable = database.downloadDAO();
    }

    public Maybe<Long> startDownload(Uri url, Uri destination, StreamInfo info) {
        DownloadManager.Request request = new DownloadManager.Request(url);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle(info.getName());
        request.setDescription("Download with NewPipe");
        request.setDestinationUri(destination);
        request.allowScanningByMediaScanner();
        long downloadId = downloadManager.enqueue(request);

        final Date currentTime = new Date();
        return Maybe.fromCallable(() -> database.runInTransaction(() -> {
            final long streamId = streamTable.upsert(new StreamEntity(info));
            return downloadTable.insert(new DownloadEntity(downloadId, streamId, currentTime));
        })).subscribeOn(Schedulers.io());
    }

    public Flowable<List<DownloadEntry>> getDownloads() {
        return downloadTable.getDownloads().subscribeOn(Schedulers.io());
    }

    public Maybe<DownloadInfo> query(long downloadId) {
        final DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        return Maybe.fromCallable(() -> {
            Cursor result = downloadManager.query(query);
            if(result.moveToFirst()) {
                Uri localUri = Uri.parse(result.getString(
                        result.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI)));

                String mediaType = result.getString(
                        result.getColumnIndexOrThrow(DownloadManager.COLUMN_MEDIA_TYPE));

                DownloadStatus status = DownloadStatus.fromCode(result.getInt(
                        result.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)));

                int downloadedBytes = result.getInt(
                        result.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));

                int totalBytes = result.getInt(
                        result.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                result.close();
                return new DownloadInfo(localUri, mediaType, status, downloadedBytes, totalBytes);
            } else {
                return null;
            }
        }).subscribeOn(Schedulers.io());
    }

    public static class DownloadInfo {
        final public Uri localUri;
        final public String mediaType;
        final public DownloadStatus status;
        final public int downloadedBytes;
        final public int totalBytes;

        public DownloadInfo(Uri localUri, String mediaType, DownloadStatus status, int downloadedBytes, int totalBytes) {
            this.localUri = localUri;
            this.mediaType = mediaType;
            this.status = status;
            this.downloadedBytes = downloadedBytes;
            this.totalBytes = totalBytes;
        }

        @Override
        public String toString() {
            return "DownloadInfo{" +
                    "localUri=" + localUri +
                    ", mediaType='" + mediaType + '\'' +
                    ", status=" + status +
                    ", downloadedBytes=" + downloadedBytes +
                    ", totalBytes=" + totalBytes +
                    '}';
        }
    }

    public enum DownloadStatus {
        FAILED(DownloadManager.STATUS_FAILED),
        PAUSED(DownloadManager.STATUS_PAUSED),
        PENDING(DownloadManager.STATUS_PENDING),
        RUNNING(DownloadManager.STATUS_RUNNING),
        SUCCESSFUL(DownloadManager.STATUS_SUCCESSFUL);

        final private int code;
        DownloadStatus(int code) {
            this.code = code;
        }

        static DownloadStatus fromCode(int code) {
            for(DownloadStatus status : DownloadStatus.values()) {
                if(code == status.code)
                    return status;
            }
            throw new IllegalArgumentException("Illegal status code: " + code);
        }
    }
}
