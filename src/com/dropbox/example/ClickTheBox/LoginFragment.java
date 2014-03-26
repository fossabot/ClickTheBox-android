package com.dropbox.example.ClickTheBox;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.dropbox.sync.android.DbxAccountManager;

public class LoginFragment extends Fragment {
    private DbxAccountManager mAccountManager;
    private int REQUEST_LINK_TO_DBX = 0;

    public LoginFragment(DbxAccountManager accountManager) {
        mAccountManager = accountManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.login_fragment, container, false);

        ((Button) v.findViewById(R.id.login)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAccountManager.startLink(getActivity(), REQUEST_LINK_TO_DBX);
            }
        });
        return v;
    }
}
