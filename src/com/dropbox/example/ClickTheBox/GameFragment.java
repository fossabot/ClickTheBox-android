package com.dropbox.example.ClickTheBox;

import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import com.dropbox.sync.android.*;

public class GameFragment extends Fragment {
    View box;
    int currentLevel = 0;
    DbxDatastore mDatastore;
    DbxAccountManager mAccountManager;

    private DbxDatastore.SyncStatusListener mDatastoreListener = new DbxDatastore.SyncStatusListener() {
        @Override
        public void onDatastoreStatusChange(DbxDatastore dbxDatastore) {
            if (dbxDatastore.getSyncStatus().hasIncoming) {
                try {
                    dbxDatastore.sync();
                } catch (DbxException e) {
                    e.printStackTrace();
                }
                updateLevel();
            }
        }
    };

    public GameFragment(DbxAccountManager accountManager) {
        mAccountManager = accountManager;
    }

    @Override
    public void onResume() {
        super.onResume();
        // When the app resumes, open the datastore and start listening for changes.
        if (mDatastore == null && mAccountManager.hasLinkedAccount()) {
            try {
                mDatastore = DbxDatastore.openDefault(mAccountManager.getLinkedAccount());
                DbxTable table = mDatastore.getTable("state");
                table.setResolutionRule("level", DbxTable.ResolutionRule.MAX);
                mDatastore.addSyncStatusListener(mDatastoreListener);
                updateLevel();
            } catch (DbxException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // When the app is paused, stop listening for changes and close the datastore.
        if (mDatastore != null) {
            mDatastore.removeSyncStatusListener(mDatastoreListener);
            mDatastore.close();
            mDatastore = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.game_fragment, container, false);
        box = v.findViewById(R.id.box);
        box.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Move to the next level.
                    DbxRecord record = getRecord();
                    record.set("level", record.getDouble("level") + 1);
                    try {
                        mDatastore.sync();
                    } catch (DbxException e) {
                        e.printStackTrace();
                    }
                    updateLevel();
                    return true;
                } else {
                    return false;
                }
            }
        });

        Button reset = (Button)v.findViewById(R.id.reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Move back to level 0.
                getRecord().set("level", 0.0);
                try {
                    mDatastore.sync();
                } catch (DbxException e) {
                    e.printStackTrace();
                }
                updateLevel();
            }
        });

        Button logout = (Button)v.findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop listening for changes and close the datastore.
                if (mDatastore != null) {
                    mDatastore.removeSyncStatusListener(mDatastoreListener);
                    mDatastore.close();
                    mDatastore = null;
                }
                // "Unlink" the account, which will cause the login screen to be displayed again.
                mAccountManager.unlink();
            }
        });

        return v;
    }

    DbxRecord getRecord() {
        DbxRecord record = null;
        try {
            // We use getOrInsert to initialize the record to level 0.
            record = mDatastore.getTable("state").getOrInsert("current_level", new DbxFields().set("level", 0.0));
        } catch (DbxException e) {
            e.printStackTrace();
        }
        return record;
    }

    void updateLevel() {
        // The box should start at 256x256 pixels and shrink by half (in each dimension) at each subsequent level.
        DbxRecord record = getRecord();
        int level = (int)record.getDouble("level");
        float prevD = (float)Math.pow(2, currentLevel);
        float nextD = (float)Math.pow(2, level);
        for (String prop : new String[] { "scaleX", "scaleY" }) {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(box, prop, 1.0f/prevD, 1.0f/nextD);
            objectAnimator.setDuration(100);
            objectAnimator.setInterpolator(new DecelerateInterpolator());
            objectAnimator.start();
        }
        currentLevel = level;
        ((HasHeader)getActivity()).setHeaderText("CTB: LEVEL " + (currentLevel + 1));
    }
}
