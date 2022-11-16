package com.soku.a24danalysiser.pojo;

import android.media.Image;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreviewItem {
    private Integer id;
    private byte[] photo;
    private Double preview;
}
