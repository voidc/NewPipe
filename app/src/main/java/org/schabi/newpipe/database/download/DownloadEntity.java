package org.schabi.newpipe.database.download;

import android.arch.persistence.room.*;
import android.support.annotation.NonNull;
import org.schabi.newpipe.database.stream.model.StreamEntity;

import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;
import static org.schabi.newpipe.database.download.DownloadEntity.DOWNLOAD_TABLE;
import static org.schabi.newpipe.database.download.DownloadEntity.JOIN_STREAM_ID;

@Entity(tableName = DOWNLOAD_TABLE,
        indices = {@Index(value = {JOIN_STREAM_ID})},
        foreignKeys = {
                @ForeignKey(entity = StreamEntity.class,
                        parentColumns = StreamEntity.STREAM_ID,
                        childColumns = JOIN_STREAM_ID,
                        onDelete = CASCADE, onUpdate = CASCADE)
        })
public class DownloadEntity {
    final static String DOWNLOAD_TABLE = "downloads";
    final static String DOWNLOAD_ID = "download_id";
    final public static String JOIN_STREAM_ID = "stream_id";
    final static String DOWNLOAD_DATE = "download_date";

    @PrimaryKey
    @ColumnInfo(name = DOWNLOAD_ID)
    private long downloadId;

    @ColumnInfo(name = JOIN_STREAM_ID)
    private long streamUid;

    @NonNull
    @ColumnInfo(name = DOWNLOAD_DATE)
    private Date downloadDate;

    public DownloadEntity(long downloadId, long streamUid, @NonNull Date downloadDate) {
        this.downloadId = downloadId;
        this.streamUid = streamUid;
        this.downloadDate = downloadDate;
    }

    public long getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(long downloadId) {
        this.downloadId = downloadId;
    }

    public long getStreamUid() {
        return streamUid;
    }

    public void setStreamUid(long streamUid) {
        this.streamUid = streamUid;
    }

    @NonNull
    public Date getDownloadDate() {
        return downloadDate;
    }

    public void setDownloadDate(@NonNull Date downloadDate) {
        this.downloadDate = downloadDate;
    }
}
