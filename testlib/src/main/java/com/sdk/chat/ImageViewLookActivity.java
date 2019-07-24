package com.sdk.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.sdk.R;
import com.sdk.view.TouchImageView;

/**
 * Created by long on 2015/7/3.
 */
public class ImageViewLookActivity extends Activity {

    private TouchImageView touchImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.kf_activity_image_look);
        touchImageView = (TouchImageView) findViewById(R.id.matrixImageView);

        Intent intent = getIntent();
        final String imgPath = intent.getStringExtra("imagePath");

        if(imgPath != null && !"".equals(imgPath)) {

        }else {
            finish();
        }

        touchImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


}
