package com.sqless.sqlessmobile.network;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.ui.activities.SignInActivity;
import com.sqless.sqlessmobile.utils.Callback;

public class GoogleTokenManager {
    public static final int ACC_SIGN_IN = 6969;
    private static GoogleTokenManager ourInstance = new GoogleTokenManager();

    public static GoogleTokenManager getInstance() {
        return ourInstance;
    }

    private GoogleTokenManager() {
    }

    public void silentSignIn(Activity context, Callback<GoogleSignInAccount> accountCallback) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(context.getString(R.string.google_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
        mGoogleSignInClient.silentSignIn().addOnCompleteListener(context, task -> {
            if (task.isSuccessful()) {
                accountCallback.exec(task.getResult());
            } else {
                startSignInActivity(context);
            }
        });
    }

    public void logOut(Activity context) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(context.getString(R.string.google_client_id))
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
        mGoogleSignInClient.revokeAccess().addOnSuccessListener(aVoid -> startSignInActivity(context));
    }

    public void startSignInActivity(Activity context) {
        Intent intent = new Intent(context, SignInActivity.class);
        context.startActivityForResult(intent, ACC_SIGN_IN);
    }
}
