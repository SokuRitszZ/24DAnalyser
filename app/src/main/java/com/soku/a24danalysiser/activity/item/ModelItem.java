package com.soku.a24danalysiser.activity.item;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.soku.a24danalysiser.R;
import com.soku.a24danalysiser.activity.EditModelActivity;

import org.w3c.dom.Text;

public class ModelItem extends AppCompatActivity {
    TextView tvId;
    TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.model_item);
    }
}