package com.cj.android.sideslipview_master;

import android.app.Activity;
import android.os.Bundle;

import com.cj.android.sideslip.SideSlipView;

/**
 * Created by maesinfo-024 on 2016/2/19.
 */
public class RightActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maincontent);
        SideSlipView sideSlipView = new SideSlipView(this);
        sideSlipView.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        sideSlipView.setMenuWidget(R.layout.menu_right);
        sideSlipView.setEnableDirection(SideSlipView.RIGHT);
    }
}
