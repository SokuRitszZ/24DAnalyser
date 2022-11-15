package com.soku.a24danalysiser.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;

import com.soku.a24danalysiser.R;
import com.soku.a24danalysiser.pojo.ModelItem;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL;

public class ModelListActivity extends AppCompatActivity {
    GridLayout glList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_list);

        loadViews();
        showItems();
    }

    public void handleClickAddModelBtn(View view) {
        startActivity(new Intent(this, AddModelActivity.class));
    }

    private void loadViews() {
        glList = findViewById(R.id.gl_list);
    }

    private void showItems() {
        List<ModelItem> list = new ArrayList<>();
        list.add(new ModelItem());
        list.add(new ModelItem());
        list.add(new ModelItem());
        list.add(new ModelItem());
        int i = 0;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        for (ModelItem item : list) {
            GridLayout.Spec rowSpec = GridLayout.spec(i);
            GridLayout.Spec colSpec = GridLayout.spec(0);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, colSpec);
            params.height = 300;
            params.topMargin = 100;
            View view = LayoutInflater.from(this).inflate(R.layout.model_item, null);

            glList.addView(view, params);
            ++i;
        }
    }
}