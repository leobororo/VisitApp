package com.leandrobororo.visitapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.leandrobororo.visitapp.criptografia.EnCryptor;

public class MainActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private ProfileTracker profileTracker;
    private LoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Profile.getCurrentProfile() == null) {
            esperaUsuarioLogar();
        } else {
            redirecionaUsuarioParaBaseActitvity(Profile.getCurrentProfile());
        }
    }

    private void redirecionaUsuarioParaBaseActitvity(Profile profile) {
        makeToast("Bem-vindo ao VisitApp " + profile.getFirstName());

        Intent it = new Intent(this, BaseActivity.class);
        it.putExtra("profile", profile);
        startActivity(it);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void esperaUsuarioLogar() {
        loginButton = (LoginButton) findViewById(R.id.login_button);
        callbackManager = CallbackManager.Factory.create();

        loginButton.setReadPermissions("public_profile","user_friends");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                storeAccessToken(loginResult);

                if (Profile.getCurrentProfile() == null) {
                    profileTracker = new ProfileTracker() {

                        @Override
                        protected void onCurrentProfileChanged(Profile profile, Profile newProfile) {
                            profileTracker.stopTracking();
                            redirecionaUsuarioParaBaseActitvity(newProfile);
                        }
                    };
                } else {
                    redirecionaUsuarioParaBaseActitvity(Profile.getCurrentProfile());
                }
            }

            @Override
            public void onCancel() {
                makeToast("O login foi cancelado");
            }

            @Override
            public void onError(FacebookException error) {
                makeToast("Ocorreu um erro durante a tentativa de efetuar login");
            }
        });
    }

    private void storeAccessToken(LoginResult loginResult) {
        try {
            EnCryptor.getInstance().encryptText("access_token", loginResult.getAccessToken().getToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void makeToast(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }
}
