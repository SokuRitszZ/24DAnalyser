package com.soku.a24danalysiser;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void handleClickToRegisterBtn(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    public void handleClickLoginBtn(View view) {
        startActivity(new Intent(this, ModelListActivity.class));
    }
}