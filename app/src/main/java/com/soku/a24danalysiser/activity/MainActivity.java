package com.soku.a24danalysiser.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.soku.a24danalysiser.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void handleClickStartBtn(View view) {
        start();
    }

    private void start() {
        finish();
        startActivity(new Intent(this, LoginActivity.class));
    }
}