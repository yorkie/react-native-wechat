package com.theweflex.react;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by tdzl2_000 on 2015-10-10.
 */
public class WeChatModule extends ReactContextBaseJavaModule implements IWXAPIEventHandler {
    private String appId;

    public WeChatModule(ReactApplicationContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "RCTWeChat";
    }

    private IWXAPI api = null;

    private static ArrayList<WeChatModule> modules = new ArrayList<>();

    @Override
    public void initialize() {
        super.initialize();
        modules.add(this);
    }

    @Override
    public void onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy();
        if (api != null){
            api = null;
        }
        modules.remove(this);
    }

    public static void handleIntent(Intent intent){
        for (WeChatModule mod : modules){
            mod.api.handleIntent(intent, mod);
        }
    }

    @ReactMethod
    public void registerApp(String appid, Callback resolve){
        api = WXAPIFactory.createWXAPI(this.getReactApplicationContext().getBaseContext(), appid, true);
        resolve.invoke(api.registerApp(appid));
    }

    @ReactMethod
    public void isWXAppInstalled(Callback resolve){
        resolve.invoke(api.isWXAppInstalled());
    }

    @ReactMethod
    public void isWXAppSupportApi(Callback resolve){
        resolve.invoke(api.isWXAppSupportAPI());
    }

    @ReactMethod
    public void getApiVersion(Callback resolve){
        resolve.invoke(api.getWXAppSupportAPI());
    }

    @ReactMethod
    public void openWXApp(Callback resolve){
        resolve.invoke(api.openWXApp());
    }

    @ReactMethod
    public void sendAuthRequest(String state, Callback resolve){
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = state;
        resolve.invoke(api.sendReq(req));
    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {
        WritableMap map = Arguments.createMap();
        map.putInt("errCode", baseResp.errCode);
        map.putString("errStr", baseResp.errStr);
        map.putString("openId", baseResp.openId);
        map.putString("transaction", baseResp.transaction);

        if (baseResp instanceof SendAuth.Resp){
            SendAuth.Resp resp = (SendAuth.Resp)(baseResp);

            map.putString("type", "SendAuth.Resp");
            map.putString("code", resp.code);
            map.putString("state", resp.state);
            map.putString("url", resp.url);
            map.putString("lang", resp.lang);
            map.putString("country", resp.country);
        }

        this.getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("WeChat_Resp", map);
    }


}
