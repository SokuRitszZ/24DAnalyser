package com.soku.a24danalysiser.pojo;

import android.content.Intent;
import android.media.Image;

import lombok.Data;

@Data
public class PreviewItem {
    private Image image;
    private Intent preview;
}
