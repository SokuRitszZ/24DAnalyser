package com.soku.a24danalysiser.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.state.State;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.soku.a24danalysiser.R;
import com.soku.a24danalysiser.pojo.PreviewItem;

import java.util.ArrayList;
import java.util.List;

public class AddModelActivity extends AppCompatActivity {
    GridLayout glList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_model);

        loadViews();
        showItems();
    }

    private void loadViews() {
        glList = findViewById(R.id.gl_list);
    }

    private void showItems() {
        List<PreviewItem> list = new ArrayList<>();
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        for (PreviewItem item : list) {
            LinearLayout.LayoutParams params;
            params = new LinearLayout.LayoutParams((int) (screenWidth * 0.8), LinearLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin = 20;

            View view = LayoutInflater.from(this).inflate(R.layout.preview_item, null);
            ImageView ivPhoto = view.findViewById(R.id.iv_photo);
            EditText etPreview = view.findViewById(R.id.et_preview);

            ivPhoto.setImageResource(R.color.purple_700);
            etPreview.setText("123");

            glList.addView(view, params);
        }
    }
}