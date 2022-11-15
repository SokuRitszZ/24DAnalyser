package com.soku.a24danalysiser.activity;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.soku.a24danalysiser.R;
import com.soku.a24danalysiser.utils.Constant;

import java.io.IOException;

import cn.hutool.json.JSONObject;
import kotlin.Pair;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http2.Header;

public class LoginActivity extends AppCompatActivity {
    EditText etUsername;
    EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loadView();
    }

    private void loadView() {
        etUsername = findViewById(R.id.et_account);
        etPassword = findViewById(R.id.et_password);
    }

    public void handleClickToRegisterBtn(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    public void handleClickLoginBtn(View view) {

        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        login(username, password, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Looper.prepare();
                Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                JSONObject json = new JSONObject(response.body().string());

                String result = json.getStr("result");
                switch (result) {
                    case "fail":
                        String message = json.getStr("message");
                        Looper.prepare();
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        Looper.loop();
                        break;
                    case "success":
                        int id = json.getInt("id");
                        Constant.setId(id);
                        startActivity(new Intent(LoginActivity.this, ModelListActivity.class));
                        Looper.prepare();
                        Toast.makeText(LoginActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                        break;
                }
            }
        });
    }

    private void login(String username, String password, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody
                .Builder()
                .add("username", username)
                .add("password", password)
                .build();
        Request request = new Request.Builder()
                .url(Constant.URL("/user/login"))
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }
}