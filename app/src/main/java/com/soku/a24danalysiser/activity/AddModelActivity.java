package com.soku.a24danalysiser.activity;

import static com.soku.a24danalysiser.utils.FileUtil.createImageFile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

public class AddModelActivity extends AppCompatActivity {

    private static final int REQUEST_PHOTOLIB = 1;
    private static final int REQUEST_CAMERA = 2;

    private GridLayout glList;
    private List<Uri> uris = new ArrayList<>();
    private List<View> views = new ArrayList<>();
    private Uri imageUri;
    private EditText etTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_model);

        imageUri = Uri.fromFile(new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "test.jpg"));
        loadViews();
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
                addNewView(data);
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
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // 设置内容类型为图片类型
        intent.setType("image/*");
        // 打开系统相册选择图片
        startActivityForResult(intent, REQUEST_PHOTOLIB);
    }

    public void handleClickTakePhotoBtn(View view) {
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

    /**
     * 先创建，再把所有图片发送上去
     * @param view
     */
    public void handleClickOkBtn(View view) {
        String title = etTitle.getText().toString();
        createModel(title, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Looper.prepare();
                Toast.makeText(AddModelActivity.this, "出现预期外错误，添加失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                JSONObject json = new JSONObject(response.body().string());
                String result = json.getStr("result");
                if (result.equals("fail")) {
                    Looper.prepare();
                    Toast.makeText(AddModelActivity.this, String.format("添加失败：%s", json.getStr("message")), Toast.LENGTH_SHORT).show();
                    Looper.loop();
                } else {
                    Integer id = json.getInt("id");
                    uploadImages(id);
                    // 跳转回去
                    Looper.prepare();
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", id);
                    bundle.putString("title", title);
                    intent.putExtras(bundle);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                    Looper.loop();
                }
            }
        });
    }

    private void createModel(String title, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody
                .Builder()
                .add("title", title)
                .add("userId", "" + Constant.getId())
                .build();
        Request request = new Request.Builder()
                .url(Constant.URL("/model/add"))
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    private void addNewView(Uri data) {
        View view = newItem();
        ImageView ivPhoto = view.findViewById(R.id.iv_photo);
        ivPhoto.setImageURI(data);

        Button btnRemove = view.findViewById(R.id.btn_remove);
        btnRemove.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(AddModelActivity.this);
            builder.setTitle("提示：");
            builder.setMessage(String.format("确认删除此图片？"));

            builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    AddModelActivity.this.runOnUiThread(() -> {
                        glList.removeView(view);
                    });
                }
            });
            builder.setPositiveButton("取消", null);
            builder.show();
        });


        glList.addView(view, getLayoutParams());
        uris.add(data);
        views.add(view);
    }

    private void uploadImages(Integer id) {
        List<PreviewItem> list = new ArrayList<>();
        ModelListActivity.pre.put(id, list);
        int n = uris.size();
        for (int i = 0; i < n; ++i) {
            Uri uri = uris.get(i);
            View view = views.get(i);
            EditText etPreview = view.findViewById(R.id.et_preview);
            double preview = Double.parseDouble(etPreview.getText().toString());
            String path = uri.getPath();
            File file = new File(path);
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
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Looper.prepare();
                    Toast.makeText(AddModelActivity.this, "出现预期外的错误，上传失败", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    JSONObject json = new JSONObject(response.body().string());
                    String result = json.getStr("result");
                    Looper.prepare();
                    if (result.equals("fail"))
                        Toast.makeText( AddModelActivity.this, String.format("上传失败：%s", json.getStr("message") ), Toast.LENGTH_SHORT)
                                .show();
                    else {
                        Integer newId = json.getInt("id");
                        list.add(new PreviewItem(newId, File2Bytes(file), preview));
                    }
                    Looper.loop();
                }
            });
        }
    }

    private byte[] File2Bytes(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] bytes = new byte[(int) file.length()];
        fis.read(bytes);
        fis.close();
        return bytes;
    }

    private void loadViews() {
        glList = findViewById(R.id.gl_list);
        etTitle = findViewById(R.id.et_title);
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

    public void compressPhoto(Uri uri) throws IOException {
        String path = uri.getPath();
        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);
        byte[] fileBytes = new byte[(int) file.length()];
        fis.read(fileBytes);
        fis.close();
        Bitmap bitmap = Compresser.compress(BitmapFactory.decodeByteArray(fileBytes, 0, fileBytes.length), 50);
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        bos.flush();
        bos.close();
    }
}