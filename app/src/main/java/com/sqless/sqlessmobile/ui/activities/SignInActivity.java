package com.sqless.sqlessmobile.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.network.PostRequest;
import com.sqless.sqlessmobile.network.RestRequest;
import com.sqless.sqlessmobile.utils.UIUtils;

import us.monoid.json.JSONObject;
import us.monoid.web.Resty;

public class SignInActivity extends Activity implements View.OnClickListener {

    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setOnClickListener(this);
        getWindow().getDecorView().setBackgroundColor(Color.rgb(67, 90, 100));
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.google_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    public void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (task.isSuccessful()) {
                handleSignInResult(task);
            }
        }
    }

    public void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            ProgressBar signInProgressBar = findViewById(R.id.sign_in_progressbar);
            signInProgressBar.setVisibility(View.VISIBLE);
            AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                    .setTitle("Error")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

            final GoogleSignInAccount account = task.getResult(ApiException.class);
            RestRequest request = new PostRequest(getString(R.string.auth_url), Resty.data("id_token", account.getIdToken())) {
                @Override
                public void onSuccess(JSONObject json) throws Exception {
                    setResult(RESULT_OK, getIntent().putExtra("ACCOUNT", account));
                    finish();
                }

                @Override
                public void onFailure(String message) {
                    mGoogleSignInClient.signOut();
                    UIUtils.invokeOnUIThreadIfNotDestroyed(SignInActivity.this, () -> {
                        signInProgressBar.setVisibility(View.INVISIBLE);
                        String errMessage = "Hubo un error al procesar la autenticación con Google.";
                        if (message.equals("connect timed out")) {
                            errMessage = "No se pudo crear la conexión con el servidor de SQLess. Disculpa las molestias.";
                        }
                        builder.setMessage(errMessage);
                        builder.show();
                        Log.e("SignInActivity", message);
                    });
                }
            };
            request.exec();
        } catch (ApiException e) {
            Log.w("ERR", "status code: " + e.getStatusCode());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }
}
