package com.soku.a24danalysiser.activity;

import static com.soku.a24danalysiser.utils.FileUtil.File2Bytes;
import static com.soku.a24danalysiser.utils.FileUtil.createImageFile;
import static com.soku.a24danalysiser.utils.FileUtil.dumpToPhotoUri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.soku.a24danalysiser.R;
import com.soku.a24danalysiser.pojo.PreviewItem;
import com.soku.a24danalysiser.utils.Compresser;
import com.soku.a24danalysiser.utils.Constant;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.hutool.json.JSONObject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditModelActivity extends AppCompatActivity {
    private static final int REQUEST_PHOTOLIB = 1;
    private static final int REQUEST_CAMERA = 2;

    private EditText etTitle;
    private GridLayout glList;
    private Integer id;
    private String title;
    private Uri imageUri;

    private List<View> views = new ArrayList<>();
    private List<Integer> ids = new ArrayList<>();
    private List<Double> oldDoubles = new ArrayList<>();
    private List<Uri> uris = new ArrayList<>();
    private List<PreviewItem> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_model);

        loadViews();
        loadBundle();
        loadPhotos();
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
                    Compresser.compressPhoto(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                View view = newItem();
                ImageView ivPhoto = view.findViewById(R.id.iv_photo);
                ivPhoto.setImageURI(data);
                glList.addView(view, getLayoutParams());
                views.add(view);
                uris.add(data);
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

    public void handleClickAddPhotoBtn(View view) {
       openPhotoLib();
    }

    public void handleClickTakePhotoBtn(View view) {
        openCamera();
    }

    public void handleClickOkBtn(View view) {
        finishEdit();
        // 比较差异

    }

    private void requestModifyPhoto(Integer id, Double preview) {
        RequestBody body = new FormBody
                .Builder()
                .add("id", "" + id)
                .add("preview", "" + preview)
                .build();
        Request request = new Request.Builder()
                .url(Constant.URL("/model/modifyPhoto"))
                .post(body)
                .build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Looper.prepare();
                Toast.makeText(EditModelActivity.this, "发生预期外错误，修改失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            }
        });
    }

    private void requestRemovePhoto(Integer id) {
        RequestBody body = new FormBody
                .Builder()
                .add("id", "" + id)
                .build();
        Request request = new Request.Builder()
                .url(Constant.URL("/model/removePhoto"))
                .post(body)
                .build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Looper.prepare();
                Toast.makeText(EditModelActivity.this, "发生预期外错误，删除失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Looper.prepare();
                Toast.makeText(EditModelActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        });
    }

    private void requestAddPhoto(Integer id, Double preview, File file, List<PreviewItem> list) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        "file",
                        file.getName(),
                        RequestBody.create(MediaType.parse("image/jpg"), file)
                )
                .addFormDataPart("preview", "" + preview)
                .addFormDataPart("id", "" + id);
        RequestBody requestBody = builder.build();
        Request request = new Request.Builder()
                .url(Constant.URL("/model/addPhoto"))
                .post(requestBody)
                .build();
        OkHttpClient client = new OkHttpClient();
        double finalPreview = preview;
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("ERR", e.getMessage());
                Looper.prepare();
                Toast.makeText(EditModelActivity.this, "出现预期外的错误，上传失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                JSONObject json = new JSONObject(response.body().string());
                String result = json.getStr("result");
                Looper.prepare();
                if (result.equals("fail"))
                    Toast.makeText( EditModelActivity.this, String.format("上传失败：%s", json.getStr("message") ), Toast.LENGTH_SHORT)
                            .show();
                else {
                    Integer newId = json.getInt("id");
                    list.add(new PreviewItem(newId, File2Bytes(file), finalPreview));
                }
                Looper.loop();
            }
        });
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photo = null;
            try {
                photo = createImageFile(getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photo != null) {
                imageUri = FileProvider.getUriForFile(this, "com.soku.a24danalysiser.filepvd", photo);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, REQUEST_CAMERA);
            }
        }
    }

    private void openPhotoLib() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PHOTOLIB);
    }

    private void finishEdit() {
        List<PreviewItem> list = ModelListActivity.pre.get(id);
        List<Double> newDoubles = new ArrayList<>();
        for (View _view : views) {
            EditText etPreview = _view.findViewById(R.id.et_preview);
            Editable text = etPreview.getText();
            if (text == null || text.toString().length() == 0) {
                newDoubles.add(0d);
            } else {
                newDoubles.add(Double.parseDouble(text.toString()));
            }
        }
        int n = oldDoubles.size();
        for (int i = 0; i < n; ++i) {
            Double oldDouble = oldDoubles.get(i);
            Double newDouble = newDoubles.get(i);
            if (oldDouble.equals(newDouble)) continue;

            // 修改
            Integer id = ids.get(i);
            list.get(i).setPreview(newDouble);
            requestModifyPhoto(id, newDouble);
        }

        // 添加
        int nn = newDoubles.size();
        for (int i = n; i < nn; ++i) {
            Uri uri = uris.get(i - n);
            View _view = views.get(i);
            EditText etPreview = _view.findViewById(R.id.et_preview);
            double preview = 0d;
            Editable text = etPreview.getText();
            if (text != null && text.toString().length() != 0) {
                preview = Double.parseDouble(text.toString());
            }
            String path = uri.getPath();
            File file = new File(path);
            requestAddPhoto(id, preview, file, list);
        }
        finish();
    }

    private void loadViews() {
        etTitle = findViewById(R.id.et_title);
        glList = findViewById(R.id.gl_list);
    }

    private void loadBundle() {
        Bundle bundle = getIntent().getExtras();

        id = bundle.getInt("id");
        title = bundle.getString("title");

        etTitle.setText(title);

        Log.d("ID", "" + id);
    }

    private void loadPhotos() {
        if (!ModelListActivity.pre.containsKey(id)) {
            ModelListActivity.requestForPreviewItems(id, new Thread(() -> {
                EditModelActivity.this.runOnUiThread(() -> {
                    showItems();
                });
            }));
        } else {
            showItems();
        }
    }

    private void showItems() {
        list = ModelListActivity.pre.get(id);
        for (PreviewItem item : list) {
            byte[] photo = item.getPhoto();
            Double preview = item.getPreview();

            Uri uri = null;
            try {
                uri = dumpToPhotoUri(photo, getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            } catch (IOException e) {
                e.printStackTrace();
            }

            View view = newItem();
            ImageView ivPhoto = view.findViewById(R.id.iv_photo);
            EditText etPreview = view.findViewById(R.id.et_preview);

            ivPhoto.setImageURI(uri);
            etPreview.setText("" + preview);

            Button btnRemove = view.findViewById(R.id.btn_remove);
            btnRemove.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditModelActivity.this);
                builder.setTitle("提示：");
                builder.setMessage(String.format("确认删除此图片？"));

                builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        removePreview(view, item.getId());
                    }
                });
                builder.setPositiveButton("取消", null);
                builder.show();
            });

            glList.addView(view, getLayoutParams());
            oldDoubles.add(preview);
            views.add(view);
            ids.add(id);
        }
    }

    private View newItem() {
        return LayoutInflater.from(this).inflate(R.layout.preview_item, null);
    }

    private LinearLayout.LayoutParams getLayoutParams() {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;

        LinearLayout.LayoutParams params;
        params = new LinearLayout.LayoutParams((int) (screenWidth * 0.8), LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = 20;

        return params;
    }

    private void removePreview(View view, Integer id) {
        glList.removeView(view);
        int i = views.indexOf(view);

        oldDoubles.remove(i);
        views.remove(i);
        ids.remove(i);
        list.remove(i);
        requestRemovePhoto(id);
    }
}