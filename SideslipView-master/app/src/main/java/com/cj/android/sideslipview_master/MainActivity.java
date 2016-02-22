package com.cj.android.sideslipview_master;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by maesinfo-024 on 2016/2/19.
 */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void left(View view){
        startActivity(new Intent(this,LeftActivity.class));
    }
    public void right(View view){
        startActivity(new Intent(this,RightActivity.class));
    }
}
