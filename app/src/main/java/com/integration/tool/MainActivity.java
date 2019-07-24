package com.integration.tool;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast.makeText(this, "toast", Toast.LENGTH_SHORT);


        Log.d("tag", "msg");

        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).run();

        Map<Integer, Integer> map = new HashMap();

    }

}
