package com.soku.a24danalysiser.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.soku.a24danalysiser.R;
import com.soku.a24danalysiser.utils.Constant;

import java.io.IOException;

import cn.hutool.json.JSONObject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {
    EditText etUsername;
    EditText etPassword;
    EditText etConfirmedPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        loadView();
    }

    private void loadView() {
        etUsername = findViewById(R.id.et_account);
        etPassword = findViewById(R.id.et_password);
        etConfirmedPassword = findViewById(R.id.et_confirmed_password);
    }

    public void handleClickRegisterBtn(View view) {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        String confirmedPassword = etConfirmedPassword.getText().toString();

        register(username, password, confirmedPassword, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("ERR", e.getMessage());
                Looper.prepare();
                Toast.makeText(RegisterActivity.this, "发生了错误", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                JSONObject json = new JSONObject(response.body().string());
                String result = json.getStr("result");
                switch (result) {
                    case "success":
                        Looper.prepare();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        Toast.makeText(RegisterActivity.this, "注册成功，请尝试登录", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                        break;
                    case "fail":
                        Looper.prepare();
                        String message = json.getStr("message");
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                        Looper.loop();
                        break;
                }
            }
        });
    }

    public void handleClickToLoginBtn(View view) {
        startActivity(new Intent(this, LoginActivity.class));
    }

    private void register(String username, String password, String confirmedPassword, Callback callback) {
        if (!password.equals(confirmedPassword)) {
            Toast.makeText(this, "密码不一致", Toast.LENGTH_SHORT).show();
            return ;
        }
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody
                .Builder()
                .add("username", username)
                .add("password", password)
                .build();
        Request request = new Request
                .Builder()
                .post(body)
                .url(Constant.URL("/user/register"))
                .build();
        client.newCall(request).enqueue(callback);
    }
}