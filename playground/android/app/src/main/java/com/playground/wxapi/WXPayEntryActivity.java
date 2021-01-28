package com.playground.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.playground.MainActivity;
import com.theweflex.react.WechatModule;

public class WXPayEntryActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intentTo = new Intent(this, MainActivity.class);
        intentTo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentTo.setAction(Intent.ACTION_VIEW);
        this.startActivity(intentTo);
        WechatModule.handleIntent(getIntent());
        finish();
    }
}
