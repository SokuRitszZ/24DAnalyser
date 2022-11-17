package com.soku.a24danalysiser.activity;

import static com.soku.a24danalysiser.utils.FileUtil.createImageFile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.soku.a24danalysiser.R;
import com.soku.a24danalysiser.pojo.PreviewItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.hutool.core.lang.Pair;

public class ModelDetailActivity extends AppCompatActivity {
    Integer id;
    Bitmap photoBitmap;
    Bitmap modelBitmap;
    Uri uri;
    List<Pair<Double, Double>> pairs;
    Double a, b;
    Double x, y;
    Double maxX = Double.MIN_VALUE;
    Double maxY = Double.MIN_VALUE;
    String title;

    ImageView ivModel;
    ImageView ivSelectedPhoto;
    TextView tvTitle;
    TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_detail);

        loadViews();
        loadBundle();

        if (!ModelListActivity.pre.containsKey(id)) {
            ModelListActivity.requestForPreviewItems(id, new Thread(() -> {
                ModelDetailActivity.this.runOnUiThread(() -> {
                    continueInit();
                });
            }));
        } else {
            continueInit();
        }
    }

    private void continueInit() {
        getPhotos();
        loadPairs();
        calculateRegression();
        paint();
    }

    public void handleClickShareBtn(View view) {
        share();
    }

    private void share() {
        Bitmap sharedBitmap = getSharedBitmap();
        File sharedImageFile = null;
        try {
            sharedImageFile = createImageFile(getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            FileOutputStream fos = new FileOutputStream(sharedImageFile);
            sharedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            Uri sharedImageUri = FileProvider.getUriForFile(this, "com.soku.a24danalysiser.filepvd", sharedImageFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, sharedImageUri);
            shareIntent.setType("image/*");

            startActivity(Intent.createChooser(shareIntent, "分享"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadViews() {
        ivModel = findViewById(R.id.iv_model);
        ivSelectedPhoto = findViewById(R.id.iv_selected_image);
        tvTitle = findViewById(R.id.tv_title);
        tvResult = findViewById(R.id.tv_result);
    }

    private void loadBundle() {
        // 要获取：需要预测的图片，所选择的模型id
        Bundle bundle = getIntent().getExtras();
        id = bundle.getInt("id");
        title = bundle.getString("title");
        String path = bundle.getString("path");
        uri = Uri.parse(path);
        photoBitmap = BitmapFactory.decodeFile(path);
    }

    private void getPhotos() {
        if (!ModelListActivity.pre.containsKey(id)) {
            ModelListActivity.requestForPreviewItems(id, new Thread(() -> {
                loadPairs();
            }));
        } else {
            loadPairs();
        }
    }

    private void loadPairs() {
        List<PreviewItem> list = ModelListActivity.pre.get(id);
        pairs = new ArrayList<>();

        for (PreviewItem item : list) {
            byte[] photo = item.getPhoto();
            Double preview = item.getPreview();
            double grey = calculateGrey(BitmapFactory.decodeByteArray(photo, 0, photo.length));
            pairs.add(new Pair<>(grey, preview));
        }
    }

    private void calculateRegression() {
        List<Double> keys = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        for (Pair<Double, Double> pair : pairs) {
            maxX = Math.max(maxX, pair.getKey());
            maxY = Math.max(maxY, pair.getValue());
            keys.add(pair.getKey());
            values.add(pair.getValue());
        }

        Double averageX = average(keys);
        Double averageY = average(values);

        Double sumbu = 0d;
        Double sumbd = 0d;

        int n = pairs.size();
        for (int i = 0; i < n; ++i) {
            Double x = keys.get(i);
            Double y = values.get(i);
            sumbu += (x - averageX) * (y - averageY);
            sumbd += (x - averageX) * (x - averageX);
        }
        b = sumbd.equals(0d) ? 0 : sumbu / sumbd;
        a = averageY - b * averageX;

        x = calculateGrey(photoBitmap);
        y = b * x + a;
        maxX = Math.max(maxX, x);
        maxY = Math.max(maxY, y);

        tvResult.setText(String.format("预测得到浓度为 %.2f mg/L", y));
    }

    private Double average(List<Double> list) {
        if (list.size() == 0) return 0d;
        Double sum = 0d;
        for (Double d : list) { sum += d; }
        sum /= list.size();
        return sum;
    }

    private Double calculateGrey(Bitmap bitmap) {
        double grey = 0d;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < height; ++i) for (int j = 0; j < width; ++j) {
            int pixel = pixels[width * i + j];
            int R = pixel >> 16 & 0xff;
            int G = pixel >> 8 & 0xff;
            int B = pixel & 0xff;
            grey += 0.299 * R + 0.587 * G + 0.114 * B;
        }
        grey /= width * height;
        return grey;
    }

    private void paint() {
        paintModel();
        paintSelectedPhoto();
    }

    private void paintModel() {
        Bitmap bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        // background
        canvas.drawColor(Color.WHITE);
        paint.setColor(Color.BLACK);

        canvas.drawLine(50, 350, 370, 350, paint);
        canvas.drawLine(50, 30, 50, 350, paint);

        // text
        paint.setTextSize(20f);
        canvas.drawText("灰度", 350, 330, paint);
        canvas.drawText("浓度-mg/L", 10, 20, paint);

        // 0
        paint.setTextSize(15f);
        canvas.drawText("0", 30, 370, paint);

        // scale
        Double scaleX = maxX / 6;
        Double scale = 0d;
        for (int x = 50; x <= 350; x += 50, scale += scaleX) {
            canvas.drawLine(x, 350, x, 340, paint);
            if (scale != 0) {
                canvas.drawText(String.format("%.2f", scale), x - 20, 370, paint);
            }
        }

        Double scaleY = maxY / 6;
        scale = 0d;
        for (int y = 350; y >= 50; y -= 50, scale += scaleY) {
            canvas.drawLine(50, y, 60, y, paint);
            if (scale != 0) {
                canvas.drawText(String.format("%.2f", scale), 10, y + 5, paint);
            }
        }

        // points
        scaleX /= 50;
        scaleY /= 50;

        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(7f);
        for (Pair<Double, Double> pair : pairs) {
            Double x = pair.getKey();
            Double y = pair.getValue();
            Float posx = 50f + Float.parseFloat("" + x / scaleX);
            Float posy = 350f - Float.parseFloat("" + y / scaleY);
            canvas.drawPoint(posx, posy, paint);
        }

        // result
        paint.setColor(Color.RED);
        Float posx = 50f + Float.parseFloat("" + x / scaleX);
        Float posy = 350f - Float.parseFloat("" + y / scaleY);
        canvas.drawPoint(posx, posy, paint);

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(1f);

        Double x0 = -a / b;
        canvas.drawLine(50f, 350f - Float.parseFloat("" + a / scaleY), 50f + Float.parseFloat("" + x0 / scaleX), 350f, paint);

        modelBitmap = bitmap;
        ivModel.setImageBitmap(bitmap);
    }

    private void paintSelectedPhoto() {
        tvTitle.setText(title);
        ivSelectedPhoto.setImageURI(uri);
    }

    private Bitmap getSharedBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(400, 1000, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        canvas.drawColor(Color.WHITE);

        canvas.drawBitmap(modelBitmap, 0, 50, paint);

        canvas.drawBitmap(smallerBitmap(this.photoBitmap, 400, 400), 0, 500, paint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        canvas.drawText(String.format("%s模型下预测的结果", title), 20, 30, paint);

        canvas.drawText(String.format("预测得到浓度为 %.2f mg/L", y), 20, 950, paint);
        return bitmap;
    }

    private Bitmap smallerBitmap(Bitmap bitmap, float newWidth, float newHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = newWidth / width;
        float scaleHeight = newHeight / height;

        Matrix mat = new Matrix();
        mat.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, mat, false);
    }
}