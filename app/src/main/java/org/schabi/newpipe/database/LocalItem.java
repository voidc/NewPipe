package org.schabi.newpipe.database;

public interface LocalItem {
    enum LocalItemType {
        PLAYLIST_LOCAL_ITEM,
        PLAYLIST_REMOTE_ITEM,

        PLAYLIST_STREAM_ITEM,
        STATISTIC_STREAM_ITEM,
        DOWNLOAD_ITEM,
    }

    LocalItemType getLocalItemType();
}
