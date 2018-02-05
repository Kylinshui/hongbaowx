package com.bshui.hongbaowx;

import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //直接打开辅助功能
        try{
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
