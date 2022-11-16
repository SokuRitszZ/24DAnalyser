package com.soku.a24danalysiser.activity;

import static com.soku.a24danalysiser.utils.Compresser.compressPhoto;
import static com.soku.a24danalysiser.utils.FileUtil.createImageFile;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.soku.a24danalysiser.R;
import com.soku.a24danalysiser.pojo.ModelItem;
import com.soku.a24danalysiser.pojo.PreviewItem;
import com.soku.a24danalysiser.utils.Constant;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private static final int REQUEST_PHOTOLIB = 1;
    private static final int REQUEST_CAMERA = 2;

    public static Map<Integer, List<PreviewItem>> pre = new HashMap<>();

    GridLayout glList;
    List<ModelItem> modelItemList = new ArrayList<>();
    List<View> views = new ArrayList<>();
    ActivityResultLauncher<Intent> launcher;
    Uri imageUri;
    Integer selectedId;
    String selectedTitle;

    public static void requestForPreviewItems(Integer id, Thread thread) {
        List<PreviewItem> list = new ArrayList<>();
        ModelListActivity.pre.put(id, list);
        RequestBody body = new FormBody
                .Builder()
                .add("id", "" + id)
                .build();
        Request request = new Request.Builder()
                .url(Constant.URL("/model/getPhotos"))
                .post(body)
                .build();
        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Looper.prepare();
                Looper.loop();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                JSONObject json = new JSONObject(response.body().string());
                JSONArray jsonArray = json.getJSONArray("list");
                int n = jsonArray.size();
                for (int i = 0; i < n; ++i) {
                    JSONObject item = jsonArray.getJSONObject(i);
                    Integer id = item.getInt("id");
                    byte[] photo = item.getBytes("photo");
                    Double preview = item.getDouble("preview");
                    list.add(new PreviewItem(id, photo, preview));
                }
                thread.start();
            }
        });
    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode != RESULT_OK) return ;

        switch (requestCode) {
            case UCrop.REQUEST_CROP: {
                if (intent == null) break;
                Uri data = UCrop.getOutput(intent);
                try {
                    compressPhoto(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Intent detailIntent = new Intent(this, ModelDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("path", data.getPath());
                bundle.putInt("id", selectedId);
                bundle.putString("title", selectedTitle);
                detailIntent.putExtras(bundle);
                startActivity(detailIntent);
            }
            break;
            case REQUEST_CAMERA: {
                Uri destinationURI = Uri.parse(String.format("file://%s/%s.jpg", getExternalFilesDir(null).getPath(), UUID.randomUUID()));
                UCrop.of(imageUri, destinationURI).start(this);
            }
            break;
            case REQUEST_PHOTOLIB: {
                if (intent == null) break;
                Uri data = intent.getData();
                if (data == null) break;
                Uri destinationURI = Uri.parse(String.format("file://%s/%s.jpg", getExternalFilesDir(null).getPath(), UUID.randomUUID()));
                UCrop.of(data, destinationURI).start(this);
            }
            break;
            default: break;
        }
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
        params.height = 450;
        params.topMargin = 100;

        View view = LayoutInflater.from(this).inflate(R.layout.model_item, null);

        TextView tvId = view.findViewById(R.id.tv_id);
        TextView tvTitle = view.findViewById(R.id.tv_title);

        tvId.setText("" + id);
        tvTitle.setText(title);

        Button btnEdit = view.findViewById(R.id.btn_edit);
        Button btnTakePhoto = view.findViewById(R.id.btn_take_photo);
        Button btnAddPhoto = view.findViewById(R.id.btn_add_photo);
        Button btnRemove = view.findViewById(R.id.btn_remove);

        btnEdit.setOnClickListener(v -> {
            Integer _id = Integer.parseInt(tvId.getText().toString());
            String _title = tvTitle.getText().toString();
            toEditItem(_id, _title);
        });

        btnTakePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                File photo = null;
                try {
                    photo = createImageFile(getExternalFilesDir(Environment.DIRECTORY_PICTURES));
                    imageUri = FileProvider.getUriForFile(this, "com.soku.a24danalysiser.filepvd", photo);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    selectedId = id;
                    selectedTitle = tvTitle.getText().toString();
                    startActivityForResult(intent, REQUEST_CAMERA);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        btnAddPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            selectedId = id;
            selectedTitle = tvTitle.getText().toString();
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_PHOTOLIB);
        });

        btnRemove.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ModelListActivity.this);
            builder.setTitle("提示");
            builder.setMessage(String.format("确认删除\"%s#%d\"？", title, id));

            builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    removeModel(id, view);
                }
            });
            builder.setPositiveButton("取消", null);
            builder.show();
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

    private void removeModel(Integer id, View view) {
        views.remove(views.indexOf(view));
        glList.removeView(view);
        requestRemoveModel(id);
    }

    private void requestRemoveModel(Integer id) {
        RequestBody body = new FormBody
                .Builder()
                .add("id", "" + id)
                .build();
        Request request = new Request.Builder()
                .url(Constant.URL("/model/remove"))
                .post(body)
                .build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Looper.prepare();
                Toast.makeText(ModelListActivity.this, "发生预期外错误，删除失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Looper.prepare();
                Toast.makeText(ModelListActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        });
    }
}