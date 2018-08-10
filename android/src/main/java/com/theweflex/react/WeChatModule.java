package com.theweflex.react;

import com.comori.tools.databus.DataBus;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelpay.PayResp;
import com.theweflex.react.exception.InvalidArgumentException;
import com.theweflex.react.exception.InvokeException;
import com.theweflex.react.exception.NotRegisterException;

/**
 * Created by tdzl2_000 on 2015-10-10.
 */
public class WeChatModule extends ReactContextBaseJavaModule {

    public static final String KEY_WX_RESULT = "WX_RESULT";

    private WXPresenter mWxPresenter;

    public WeChatModule(ReactApplicationContext context) {
        super(context);
        mWxPresenter = new WXPresenter(context);
    }

    @Override
    public String getName() {
        return "RCTWeChat";
    }

    /**
     * fix Native module WeChatModule tried to override WeChatModule for module name RCTWeChat.
     * If this was your intention, return true from WeChatModule#canOverrideExistingModule() bug
     *
     * @return
     */
    public boolean canOverrideExistingModule() {
        return true;
    }

    @Override
    public void initialize() {
        super.initialize();
        DataBus.get().with(KEY_WX_RESULT, BaseResp.class)
                .observe(onWxResultOB);
    }

    @Override
    public void onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy();
        if (mWxPresenter != null) {
            mWxPresenter.destroy();
        }
        DataBus.get().remove(KEY_WX_RESULT);
    }

    @ReactMethod
    public void registerApp(String appid, Callback callback) {
        mWxPresenter.register(appid);
    }

    @ReactMethod
    public void isWXAppInstalled(Callback callback) {
        try {
            callback.invoke(null, mWxPresenter.isWXAppInstalled());
        } catch (NotRegisterException e) {
            callback.invoke(e.getMessage());
        }
    }

    @ReactMethod
    public void isWXAppSupportApi(Callback callback) {
        try {
            callback.invoke(null, mWxPresenter.isWXAppSupportApi());
        } catch (NotRegisterException e) {
            callback.invoke(e.getMessage());
        }
    }

    @ReactMethod
    public void getApiVersion(Callback callback) {
        try {
            callback.invoke(null, mWxPresenter.getApiVersion());
        } catch (NotRegisterException e) {
            callback.invoke(e.getMessage());
        }
    }

    @ReactMethod
    public void openWXApp(Callback callback) {
        try {
            callback.invoke(null, mWxPresenter.openWXApp());
        } catch (NotRegisterException e) {
            callback.invoke(e.getMessage());
        }
    }

    @ReactMethod
    public void sendAuthRequest(String scope, String state, Callback callback) {
        try {
            callback.invoke(null, mWxPresenter.sendAuthRequest(scope, state));
        } catch (NotRegisterException e) {
            callback.invoke(e.getMessage());
        }
    }

    public void share(int scene, ReadableMap data, final Callback callback) {
        try {
            mWxPresenter.rnShare(scene, data, new WXPresenter.ShareCallback() {
                @Override
                public void onShareCompleted(boolean success) {
                    callback.invoke(null, success);
                }
            });
        } catch (InvalidArgumentException | NotRegisterException e) {
            callback.invoke(e.getMessage());
        }
    }

    @ReactMethod
    public void shareToTimeline(ReadableMap data, final Callback callback) {
        share(SendMessageToWX.Req.WXSceneTimeline, data, callback);
    }

    @ReactMethod
    public void shareToSession(ReadableMap data, Callback callback) {
        share(SendMessageToWX.Req.WXSceneSession, data, callback);
    }

    @ReactMethod
    public void shareToFavorite(ReadableMap data, Callback callback) {
        share(SendMessageToWX.Req.WXSceneFavorite, data, callback);
    }

    @ReactMethod
    public void pay(ReadableMap data, Callback callback) {
        try {
            callback.invoke(null, mWxPresenter.rnPay(data));
        } catch (NotRegisterException | InvokeException e) {
            callback.invoke(e.getMessage());
        }
    }

    private DataBus.OnBusResult<BaseResp> onWxResultOB = new DataBus.OnBusResult<BaseResp>() {
        @Override
        public void onResult(BaseResp baseResp) {
            WritableMap map = Arguments.createMap();
            map.putInt("errCode", baseResp.errCode);
            map.putString("errStr", baseResp.errStr);
            map.putString("openId", baseResp.openId);
            map.putString("transaction", baseResp.transaction);

            if (baseResp instanceof SendAuth.Resp) {
                SendAuth.Resp resp = (SendAuth.Resp) (baseResp);

                map.putString("type", "SendAuth.Resp");
                map.putString("code", resp.code);
                map.putString("state", resp.state);
                map.putString("url", resp.url);
                map.putString("lang", resp.lang);
                map.putString("country", resp.country);
            } else if (baseResp instanceof SendMessageToWX.Resp) {
                SendMessageToWX.Resp resp = (SendMessageToWX.Resp) (baseResp);
                map.putString("type", "SendMessageToWX.Resp");
            } else if (baseResp instanceof PayResp) {
                PayResp resp = (PayResp) (baseResp);
                map.putString("type", "PayReq.Resp");
                map.putString("returnKey", resp.returnKey);
            }

            getReactApplicationContext()
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("WeChat_Resp", map);
        }
    };

}
