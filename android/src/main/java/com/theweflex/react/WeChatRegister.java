package com.theweflex.react;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class WeChatRegister extends BroadcastReceiver {

	private static final String TAG = "WeChatRegister";
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e(TAG, "onReceive: ");
		// final IWXAPI api = WXAPIFactory.createWXAPI(context, WeChatModule.appId);
		// api.registerApp(WeChatModule.appId);
	}
}
