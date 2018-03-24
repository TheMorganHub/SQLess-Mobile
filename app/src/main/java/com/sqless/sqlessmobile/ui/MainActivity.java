package com.sqless.sqlessmobile.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.sqless.sqlessmobile.R;

public class MainActivity extends AppCompatActivity {

    public static final int ACC_SIGN_IN = 6969;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivityForResult(intent, ACC_SIGN_IN);
        } else {
            //TODO user is already logged in
//            updateUI(account);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                GoogleSignInAccount account = data.getParcelableExtra("ACCOUNT");
                Toast.makeText(this, "Bienvenido " + account.getEmail(), Toast.LENGTH_SHORT).show();
            } else {
                finish();
            }
        }
    }
}
