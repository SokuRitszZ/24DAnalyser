package com.soku.a24danalysiser.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.soku.a24danalysiser.R;
import com.soku.a24danalysiser.pojo.ModelItem;
import com.soku.a24danalysiser.pojo.PreviewItem;
import com.soku.a24danalysiser.utils.Constant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ModelListActivity extends AppCompatActivity {
    public static Map<Integer, List<PreviewItem>> pre = new HashMap<>();

    GridLayout glList;
    List<ModelItem> modelItemList = new ArrayList<>();
    List<View> views = new ArrayList<>();
    ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_list);

        loadViews();
        getItems();

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result == null) return;
                Intent intent = result.getData();
                if (intent == null || result.getResultCode() != Activity.RESULT_OK) return ;
                Bundle bundle = intent.getExtras();
                Integer id = bundle.getInt("id");
                String title = bundle.getString("title");
                ModelListActivity.this.runOnUiThread(() -> {
                    addNewModel(modelItemList.size(), id, title);
                    modelItemList.add(new ModelItem(id, title));
                });
            }
        });
    }

    public void handleClickAddModelBtn(View view) {
        launcher.launch(new Intent(this, AddModelActivity.class));
    }

    private void loadViews() {
        glList = findViewById(R.id.gl_list);
    }

    private void getItems() {
        RequestBody body = new FormBody
                .Builder()
                .add("id", "" + Constant.getId())
                .build();
        Request request = new Request.Builder()
                .url(Constant.URL("/model/get"))
                .post(body)
                .build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Looper.prepare();
                Toast.makeText(ModelListActivity.this, "发生预期外错误，获取模型失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                JSONObject json = new JSONObject(response.body().string());
                JSONArray list = json.getJSONArray("list");
                int n = list.size();
                for (int i = 0; i < n; ++i) {
                    JSONObject obj = list.getJSONObject(i);
                    String title = obj.getStr("title");
                    Integer id = obj.getInt("id");
                    modelItemList.add(new ModelItem(id, title));
                }
                ModelListActivity.this.runOnUiThread(() -> {
                    showItems();
                });
            }
        });
    }

    private void showItems() {
        int i = 0;
        for (ModelItem item : modelItemList) {
            addNewModel(i++, item.getId(), item.getTitle());
        }
    }

    private void addNewModel(Integer i, Integer id, String title) {
        GridLayout.Spec rowSpec = GridLayout.spec(i);
        GridLayout.Spec colSpec = GridLayout.spec(0);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, colSpec);
        params.height = 300;
        params.topMargin = 100;

        View view = LayoutInflater.from(this).inflate(R.layout.model_item, null);

        TextView tvId = view.findViewById(R.id.tv_id);
        TextView tvTitle = view.findViewById(R.id.tv_title);

        tvId.setText("" + id);
        tvTitle.setText(title);

        Button btnEdit = view.findViewById(R.id.btn_edit);
        btnEdit.setOnClickListener(v -> {
            Integer _id = Integer.parseInt(tvId.getText().toString());
            String _title = tvTitle.getText().toString();
            toEditItem(_id, _title);
        });

        glList.addView(view, params);
        views.add(view);
    }

    private void toEditItem(Integer id, String title) {
        Intent intent = new Intent(this, EditModelActivity.class);
        Bundle bundle = new Bundle();

        bundle.putInt("id", id);
        bundle.putString("title", title);
        intent.putExtras(bundle);

        startActivity(intent);
    }
}