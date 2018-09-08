package org.schabi.newpipe.download;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import icepick.State;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.schabi.newpipe.R;
import org.schabi.newpipe.database.LocalItem;
import org.schabi.newpipe.database.download.DownloadEntry;
import org.schabi.newpipe.local.BaseLocalListFragment;
import org.schabi.newpipe.report.UserAction;
import org.schabi.newpipe.util.OnClickGesture;

import java.util.List;

public class DownloadFragment
        extends BaseLocalListFragment<List<DownloadEntry>, Void> {

    @State
    protected Parcelable itemsListState;

    private Subscription databaseSubscription;
    private NewPipeDownloadManager downloadManager;
    private CompositeDisposable disposables = new CompositeDisposable();

    ///////////////////////////////////////////////////////////////////////////
    // Fragment LifeCycle - Creation
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (activity == null) return;
        downloadManager = new NewPipeDownloadManager(activity);
        disposables = new CompositeDisposable();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState) {

        if(!useAsFrontPage) {
            setTitle(activity.getString(R.string.downloads_title));
        }
        return inflater.inflate(R.layout.fragment_bookmarks, container, false);
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (activity != null && isVisibleToUser) {
            setTitle(activity.getString(R.string.downloads_title));
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Fragment LifeCycle - Views
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void initViews(View rootView, Bundle savedInstanceState) {
        super.initViews(rootView, savedInstanceState);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        itemListAdapter.setSelectedListener(new OnClickGesture<LocalItem>() {
            @Override
            public void selected(LocalItem selectedItem) {
                final FragmentManager fragmentManager = getFM();

                if(selectedItem instanceof DownloadEntry) {
                    Log.d("DownloadFragment", "Selected download: " + ((DownloadEntry) selectedItem).title);
                }
            }

            @Override
            public void held(LocalItem selectedItem) {
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    // Fragment LifeCycle - Loading
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void startLoading(boolean forceLoad) {
        super.startLoading(forceLoad);

        downloadManager.getDownloads()
                .onBackpressureLatest()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getDownloadSubscriber());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Fragment LifeCycle - Destruction
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onPause() {
        super.onPause();
        itemsListState = itemsList.getLayoutManager().onSaveInstanceState();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (disposables != null) disposables.clear();
        if (databaseSubscription != null) databaseSubscription.cancel();

        databaseSubscription = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposables != null) disposables.dispose();

        disposables = null;
        downloadManager = null;
        itemsListState = null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Subscriptions Loader
    ///////////////////////////////////////////////////////////////////////////

    private Subscriber<List<DownloadEntry>> getDownloadSubscriber() {
        return new Subscriber<List<DownloadEntry>>() {
            @Override
            public void onSubscribe(Subscription s) {
                showLoading();
                if (databaseSubscription != null) databaseSubscription.cancel();
                databaseSubscription = s;
                databaseSubscription.request(1);
            }

            @Override
            public void onNext(List<DownloadEntry> subscriptions) {
                handleResult(subscriptions);
                if (databaseSubscription != null) databaseSubscription.request(1);
            }

            @Override
            public void onError(Throwable exception) {
                DownloadFragment.this.onError(exception);
            }

            @Override
            public void onComplete() {
            }
        };
    }

    @Override
    public void handleResult(@NonNull List<DownloadEntry> result) {
        super.handleResult(result);

        itemListAdapter.clearStreamItemList();

        if (result.isEmpty()) {
            showEmptyState();
            return;
        }

        itemListAdapter.addItems(result);
        if (itemsListState != null) {
            itemsList.getLayoutManager().onRestoreInstanceState(itemsListState);
            itemsListState = null;
        }
        hideLoading();
    }
    ///////////////////////////////////////////////////////////////////////////
    // Fragment Error Handling
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected boolean onError(Throwable exception) {
        if (super.onError(exception)) return true;

        onUnrecoverableError(exception, UserAction.SOMETHING_ELSE,
                "none", "Bookmark", R.string.general_error);
        return true;
    }

    @Override
    protected void resetFragment() {
        super.resetFragment();
        if (disposables != null) disposables.clear();
    }

}
