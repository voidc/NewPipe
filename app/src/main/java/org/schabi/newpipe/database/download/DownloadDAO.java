package org.schabi.newpipe.database.download;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import io.reactivex.Flowable;
import org.schabi.newpipe.database.BasicDAO;

import java.util.List;

import static org.schabi.newpipe.database.download.DownloadEntity.*;
import static org.schabi.newpipe.database.stream.model.StreamEntity.STREAM_ID;
import static org.schabi.newpipe.database.stream.model.StreamEntity.STREAM_TABLE;

@Dao
public abstract class DownloadDAO implements BasicDAO<DownloadEntity> {
    @Override
    @Query("SELECT * FROM " + DOWNLOAD_TABLE)
    public abstract Flowable<List<DownloadEntity>> getAll();

    @Override
    @Query("DELETE FROM " + DOWNLOAD_TABLE)
    public abstract int deleteAll();

    @Override
    public Flowable<List<DownloadEntity>> listByService(int serviceId) {
        throw new UnsupportedOperationException();
    }

    @Query("SELECT * FROM " + STREAM_TABLE +
            " INNER JOIN " + DOWNLOAD_TABLE +
            " ON " + STREAM_ID + " = " + JOIN_STREAM_ID +
            " ORDER BY " + DOWNLOAD_DATE + " DESC")
    public abstract Flowable<List<DownloadEntry>> getDownloads();
}
