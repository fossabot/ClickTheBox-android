package com.dropbox.example.ClickTheBox;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;
import com.dropbox.sync.android.*;

public class MainActivity extends Activity implements HasHeader {
    TextView header;
    DbxAccountManager mAccountManager;
    int REQUEST_LINK_TO_DBX = 0;
    private DbxAccountManager.AccountListener mAccountListener = new DbxAccountManager.AccountListener() {
        @Override public void onLinkedAccountChange(DbxAccountManager mgr, DbxAccount acct) {
            if (mgr.hasLinkedAccount()) {
                // When the user is logged in, show the game.
                getFragmentManager().beginTransaction().replace(R.id.frame, new GameFragment(mAccountManager)).commitAllowingStateLoss();
            } else {
                // When the user is not logged in, show the login screen.
                setHeaderText("CLICK THE BOX");
                getFragmentManager().beginTransaction().replace(R.id.frame, new LoginFragment(mAccountManager)).commitAllowingStateLoss();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        header = (TextView)findViewById(R.id.header);

        mAccountManager = DbxAccountManager.getInstance(getApplicationContext(), "8i3v3z06x98zza5", "7tbby4wvjfdwnwu");
    }

    @Override
    public void onPause() {
        super.onPause();
        mAccountManager.addListener(mAccountListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAccountManager.addListener(mAccountListener);
        mAccountListener.onLinkedAccountChange(mAccountManager, mAccountManager.getLinkedAccount());
    }

    public void setHeaderText(String text) {
        header.setText(text);
    }
}
