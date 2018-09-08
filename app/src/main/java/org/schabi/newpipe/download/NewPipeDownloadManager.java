package org.schabi.newpipe.download;

import android.app.DownloadManager;
import android.content.Context;
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
}
