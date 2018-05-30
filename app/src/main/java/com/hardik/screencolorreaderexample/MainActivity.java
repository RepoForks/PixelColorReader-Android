package com.hardik.screencolorreaderexample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Button btn1, btn2, btn3;
    ScreenColorPicker screenColorPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn1 = findViewById(R.id.startButton);
        btn2 = findViewById(R.id.stopButton);
        btn3 = findViewById(R.id.bt);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screenColorPicker.start();
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screenColorPicker.stop();
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap b = screenColorPicker.getLatestBitmap();
                        Log.e("sixe", b.getHeight()+","+b.getWidth());
                        FileOutputStream out = null;
                        try {
                            out = new FileOutputStream(Environment.getExternalStorageDirectory()+"/erererere.png");
                            b.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                            // PNG is a lossless format, the compression factor (100) is ignored
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (out != null) {
                                    out.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, 2000);
            }
        });

        screenColorPicker = new ScreenColorPicker(this, 1080, 1920);
        screenColorPicker.initialise();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        screenColorPicker.checkPermission(new ScreenColorPicker.PermissionCallbacks() {
            @Override
            public void onPermissionGranted() {
                Log.d("Permission","Granted");
            }

            @Override
            public void onPermissionDenied() {
                finish();
            }
        },requestCode, resultCode, data);
    }
