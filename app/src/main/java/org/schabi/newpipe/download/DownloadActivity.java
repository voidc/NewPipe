package org.schabi.newpipe.download;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewTreeObserver;

import org.schabi.newpipe.R;
import org.schabi.newpipe.settings.SettingsActivity;
import org.schabi.newpipe.util.ThemeHelper;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
//import us.shandian.giga.service.DownloadManagerService;
//import us.shandian.giga.ui.fragment.AllMissionsFragment;
//import us.shandian.giga.ui.fragment.MissionsFragment;

public class DownloadActivity extends AppCompatActivity {

    private static final String MISSIONS_FRAGMENT_TAG = "fragment_tag";
    //private DeleteDownloadManager mDeleteDownloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*
        // Service
        Intent i = new Intent();
        i.setClass(this, DownloadManagerService.class);
        startService(i);
        */

        ThemeHelper.setTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloader);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.downloads_title);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        //mDeleteDownloadManager = new DeleteDownloadManager(this);
        //mDeleteDownloadManager.restoreState(savedInstanceState);

        /*
        MissionsFragment fragment = (MissionsFragment) getFragmentManager().findFragmentByTag(MISSIONS_FRAGMENT_TAG);
        if (fragment != null) {
            fragment.setDeleteManager(mDeleteDownloadManager);
        } else {
            getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    updateFragments();
                    getWindow().getDecorView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });
        }
        */
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //mDeleteDownloadManager.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    private void updateFragments() {
        /*
        MissionsFragment fragment = new AllMissionsFragment();
        fragment.setDeleteManager(mDeleteDownloadManager);

        getFragmentManager().beginTransaction()
                .replace(R.id.frame, fragment, MISSIONS_FRAGMENT_TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
        */
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.download_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            case R.id.action_settings: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                deletePending();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        deletePending();
    }

    private void deletePending() {
        /*
        Completable.fromAction(mDeleteDownloadManager::deletePending)
                .subscribeOn(Schedulers.io())
                .subscribe();
        */
    }
}
