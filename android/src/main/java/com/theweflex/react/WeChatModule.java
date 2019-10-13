package com.theweflex.react;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import androidx.annotation.Nullable;

import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.internal.Files;
import com.facebook.common.references.CloseableReference;
import com.facebook.common.util.UriUtil;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXFileObject;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXMiniProgramObject;
import com.tencent.mm.opensdk.modelmsg.WXMusicObject;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXVideoObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.modelpay.PayResp;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.mm.opensdk.constants.ConstantsAPI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.UUID;

/**
* @Author: little-snow-fox
* @Date: 2019-10-10
*/
public class WeChatModule extends ReactContextBaseJavaModule implements IWXAPIEventHandler {
    private String appId;
    private IWXAPI api = null;
    private final static String NOT_REGISTERED = "registerApp required.";
    private final static String INVOKE_FAILED = "WeChat API invoke returns false.";
    private final static String INVALID_ARGUMENT = "invalid argument.";
    // 缩略图大小 kb
    private final static int THUMB_SIZE = 32;

    private static byte[] bitmapTopBytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        bitmap.recycle();
        return baos.toByteArray();
    }

    // 图片压缩算法
    // little-snow-fox: 该算法存在效率问题，希望有义士可以进行优化
    private static byte[] bitmapResizeGetBytes(Bitmap image, int size) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 质量压缩方法，这里100表示第一次不压缩，把压缩后的数据缓存到 baos
        image.compress(Bitmap.CompressFormat.JPEG, 10, baos);
        int options = 10;
        // 循环判断压缩后依然大于 32kb 则继续压缩
        while (baos.toByteArray().length / 1024 > size) {
            // 重置baos即清空baos
            baos.reset();
            // 每次都减少1
            options += 1;
            // 这里压缩options%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, 10 / options * 10, baos);
        }
        return baos.toByteArray();
    }

    public WeChatModule(ReactApplicationContext context) {
        super(context);
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

    private static ArrayList<WeChatModule> modules = new ArrayList<>();

    @Override
    public void initialize() {
        super.initialize();
        modules.add(this);
    }

    @Override
    public void onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy();
        if (api != null) {
            api = null;
        }
        modules.remove(this);
    }

    public static void handleIntent(Intent intent) {
        for (WeChatModule mod : modules) {
            mod.api.handleIntent(intent, mod);
        }
    }

    @ReactMethod
    public void registerApp(String appid, String universalLink, Callback callback) {
        this.appId = appid;
        api = WXAPIFactory.createWXAPI(this.getReactApplicationContext().getBaseContext(), appid, true);
        callback.invoke(null, api.registerApp(appid));
    }

    @ReactMethod
    public void isWXAppInstalled(Callback callback) {
        if (api == null) {
            callback.invoke(NOT_REGISTERED);
            return;
        }
        callback.invoke(null, api.isWXAppInstalled());
    }

    @ReactMethod
    public void isWXAppSupportApi(Callback callback) {
        if (api == null) {
            callback.invoke(NOT_REGISTERED);
            return;
        }
        callback.invoke(null, api.getWXAppSupportAPI());
    }

    @ReactMethod
    public void getApiVersion(Callback callback) {
        if (api == null) {
            callback.invoke(NOT_REGISTERED);
            return;
        }
        callback.invoke(null, api.getWXAppSupportAPI());
    }

    @ReactMethod
    public void openWXApp(Callback callback) {
        if (api == null) {
            callback.invoke(NOT_REGISTERED);
            return;
        }
        callback.invoke(null, api.openWXApp());
    }

    @ReactMethod
    public void sendAuthRequest(String scope, String state, Callback callback) {
        if (api == null) {
            callback.invoke(NOT_REGISTERED);
            return;
        }
        SendAuth.Req req = new SendAuth.Req();
        req.scope = scope;
        req.state = state;
        callback.invoke(null, api.sendReq(req));
    }

    /**
     * 分享文本
     * @param data
     * @param callback
     */
    @ReactMethod
    public void shareText(ReadableMap data, Callback callback) {
        //初始化一个 WXTextObject 对象，填写分享的文本内容
        WXTextObject textObj = new WXTextObject();
        textObj.text = data.getString("text");

        //用 WXTextObject 对象初始化一个 WXMediaMessage 对象
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
         msg.description = data.getString("text");

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = "text";
        req.message = msg;
        req.scene = data.hasKey("scene") ? data.getInt("scene") : SendMessageToWX.Req.WXSceneSession;
        callback.invoke(null, api.sendReq(req));
    }

    /**
     * 分享图片
     * @param data
     * @param callback
     */
    @ReactMethod
    public void shareImage(final ReadableMap data, final Callback callback) {
        this._getImage(Uri.parse(data.getString("imageUrl")), null, new ImageCallback() {
            @Override
            public void invoke(@Nullable Bitmap bmp) {
                // 初始化 WXImageObject 和 WXMediaMessage 对象
                WXImageObject imgObj = new WXImageObject(bmp);
                WXMediaMessage msg = new WXMediaMessage();
                msg.mediaObject = imgObj;

                // 设置缩略图
                msg.thumbData = bitmapResizeGetBytes(bmp, THUMB_SIZE);

                // 构造一个Req
                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = "img";
                req.message = msg;
                // req.userOpenId = getOpenId();
                req.scene = data.hasKey("scene") ? data.getInt("scene") : SendMessageToWX.Req.WXSceneSession;
                callback.invoke(null, api.sendReq(req));
            }
        });

    }

    /**
     * 分享音乐
     * @param data
     * @param callback
     */
    @ReactMethod
    public void shareMusic(final ReadableMap data, final Callback callback) {
        // 初始化一个WXMusicObject，填写url
        WXMusicObject music = new WXMusicObject();
        music.musicUrl = data.hasKey("musicUrl") ? data.getString("musicUrl") : null;
        music.musicLowBandUrl = data.hasKey("musicLowBandUrl") ? data.getString("musicLowBandUrl") : null;
        music.musicDataUrl = data.hasKey("musicDataUrl") ? data.getString("musicDataUrl") : null;
        music.musicUrl = data.hasKey("musicUrl") ? data.getString("musicUrl") : null;
        music.musicLowBandDataUrl = data.hasKey("musicLowBandDataUrl") ? data.getString("musicLowBandDataUrl") : null;
        // 用 WXMusicObject 对象初始化一个 WXMediaMessage 对象
        final WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = music;
        msg.title = data.hasKey("title") ? data.getString("title") : null;
        msg.description = data.hasKey("description") ? data.getString("description") : null;

        if (data.hasKey("thumbImageUrl")) {
            this._getImage(Uri.parse(data.getString("thumbImageUrl")), null, new ImageCallback() {
                @Override
                public void invoke(@Nullable Bitmap bmp) {
                    // 设置缩略图
                    msg.thumbData = bitmapResizeGetBytes(bmp, THUMB_SIZE);
                    // 构造一个Req
                    SendMessageToWX.Req req = new SendMessageToWX.Req();
                    req.transaction = "music";
                    req.message = msg;
                    req.scene = data.hasKey("scene") ? data.getInt("scene") : SendMessageToWX.Req.WXSceneSession;
                    callback.invoke(null, api.sendReq(req));
                }
            });
        } else {
            // 构造一个Req
            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = "music";
            req.message = msg;
            req.scene = data.hasKey("scene") ? data.getInt("scene") : SendMessageToWX.Req.WXSceneSession;
            callback.invoke(null, api.sendReq(req));
        }

    }

    /**
     * 分享视频
     * @param data
     * @param callback
     */
    @ReactMethod
    public void shareVideo(final ReadableMap data, final Callback callback) {
        // 初始化一个WXVideoObject，填写url
        WXVideoObject video = new WXVideoObject();
        video.videoUrl = data.hasKey("videoUrl") ? data.getString("videoUrl") : null;
        video.videoLowBandUrl = data.hasKey("videoLowBandUrl") ? data.getString("videoLowBandUrl") : null;
        //用 WXVideoObject 对象初始化一个 WXMediaMessage 对象
        final WXMediaMessage msg = new WXMediaMessage(video);
        msg.title = data.hasKey("title") ? data.getString("title") : null;
        msg.description = data.hasKey("description") ? data.getString("description") : null;

        if (data.hasKey("thumbImageUrl")) {
            this._getImage(Uri.parse(data.getString("thumbImageUrl")), null, new ImageCallback() {
                @Override
                public void invoke(@Nullable Bitmap bmp) {
                    // 设置缩略图
                    msg.thumbData = bitmapResizeGetBytes(bmp, THUMB_SIZE);
                    // 构造一个Req
                    SendMessageToWX.Req req = new SendMessageToWX.Req();
                    req.transaction = "video";
                    req.message = msg;
                    req.scene = data.hasKey("scene") ? data.getInt("scene") : SendMessageToWX.Req.WXSceneSession;
                    callback.invoke(null, api.sendReq(req));
                }
            });
        } else {
            // 构造一个Req
            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = "video";
            req.message = msg;
            req.scene = data.hasKey("scene") ? data.getInt("scene") : SendMessageToWX.Req.WXSceneSession;
            callback.invoke(null, api.sendReq(req));
        }
    }

    /**
     * 分享网页
     * @param data
     * @param callback
     */
    @ReactMethod
    public void shareWebpage(final ReadableMap data, final Callback callback) {
        // 初始化一个WXWebpageObject，填写url
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = data.hasKey("webpageUrl") ? data.getString("webpageUrl") : null;

        //用 WXWebpageObject 对象初始化一个 WXMediaMessage 对象
        final WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = data.hasKey("title") ? data.getString("title") : null;
        msg.description = data.hasKey("description") ? data.getString("description") : null;

        if (data.hasKey("thumbImageUrl")) {
            this._getImage(Uri.parse(data.getString("thumbImageUrl")), null, new ImageCallback() {
                @Override
                public void invoke(@Nullable Bitmap bmp) {
                    // 设置缩略图
                    msg.thumbData = bitmapResizeGetBytes(bmp, THUMB_SIZE);
                    // 构造一个Req
                    SendMessageToWX.Req req = new SendMessageToWX.Req();
                    req.transaction = "webpage";
                    req.message = msg;
                    req.scene = data.hasKey("scene") ? data.getInt("scene") : SendMessageToWX.Req.WXSceneSession;
                    callback.invoke(null, api.sendReq(req));
                }
            });
        } else {
            // 构造一个Req
            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = "webpage";
            req.message = msg;
            req.scene = data.hasKey("scene") ? data.getInt("scene") : SendMessageToWX.Req.WXSceneSession;
            callback.invoke(null, api.sendReq(req));
        }
    }

    /**
     * 分享小程序
     * @param data
     * @param callback
     */
    @ReactMethod
    public void shareMiniProgram(final ReadableMap data, final Callback callback) {
        WXMiniProgramObject miniProgramObj = new WXMiniProgramObject();
        // 兼容低版本的网页链接
        miniProgramObj.webpageUrl = data.hasKey("webpageUrl") ? data.getString("webpageUrl") : null;
        // 正式版:0，测试版:1，体验版:2
        miniProgramObj.miniprogramType = data.hasKey("miniprogramType") ? data.getInt("miniprogramType") : WXMiniProgramObject.MINIPTOGRAM_TYPE_RELEASE;
        // 小程序原始id
        miniProgramObj.userName = data.hasKey("userName") ? data.getString("userName") : null;
        // 小程序页面路径；对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"
        miniProgramObj.path = data.hasKey("path") ? data.getString("path") : null;
        final WXMediaMessage msg = new WXMediaMessage(miniProgramObj);
        // 小程序消息 title
        msg.title = data.hasKey("title") ? data.getString("title") : null;
        // 小程序消息 desc
        msg.description = data.hasKey("description") ? data.getString("description") : null;

        String thumbImageUrl = data.hasKey("hdImageUrl") ? data.getString("hdImageUrl") : data.hasKey("thumbImageUrl") ? data.getString("thumbImageUrl") : null;

        if (thumbImageUrl != null && !thumbImageUrl.equals("")) {
            this._getImage(Uri.parse(thumbImageUrl), null, new ImageCallback() {
                @Override
                public void invoke(@Nullable Bitmap bmp) {
                    // 小程序消息封面图片，小于128k
                    msg.thumbData = bitmapResizeGetBytes(bmp, 128);
                    // 构造一个Req
                    SendMessageToWX.Req req = new SendMessageToWX.Req();
                    req.transaction = "miniProgram";
                    req.message = msg;
                    req.scene = data.hasKey("scene") ? data.getInt("scene") : SendMessageToWX.Req.WXSceneSession;
                    callback.invoke(null, api.sendReq(req));
                }
            });
        } else {
            // 构造一个Req
            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = "miniProgram";
            req.message = msg;
            req.scene = data.hasKey("scene") ? data.getInt("scene") : SendMessageToWX.Req.WXSceneSession;
            callback.invoke(null, api.sendReq(req));
        }
    }

    @ReactMethod
    public void launchMiniProgram(ReadableMap data, Callback callback) {
        if (api == null) {
            callback.invoke(NOT_REGISTERED);
            return;
        }
        WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
        // 填小程序原始id
        req.userName = data.getString("userName");
        //拉起小程序页面的可带参路径，不填默认拉起小程序首页
        req.path = data.getString("path");
        // 可选打开 开发版，体验版和正式版
        req.miniprogramType = data.getInt("miniProgramType");
        boolean success = api.sendReq(req);
        if (!success) callback.invoke(INVALID_ARGUMENT);
    }

    @ReactMethod
    public void pay(ReadableMap data, Callback callback) {
        PayReq payReq = new PayReq();
        if (data.hasKey("partnerId")) {
            payReq.partnerId = data.getString("partnerId");
        }
        if (data.hasKey("prepayId")) {
            payReq.prepayId = data.getString("prepayId");
        }
        if (data.hasKey("nonceStr")) {
            payReq.nonceStr = data.getString("nonceStr");
        }
        if (data.hasKey("timeStamp")) {
            payReq.timeStamp = data.getString("timeStamp");
        }
        if (data.hasKey("sign")) {
            payReq.sign = data.getString("sign");
        }
        if (data.hasKey("package")) {
            payReq.packageValue = data.getString("package");
        }
        if (data.hasKey("extData")) {
            payReq.extData = data.getString("extData");
        }
        payReq.appId = appId;
        callback.invoke(api.sendReq(payReq) ? null : INVOKE_FAILED);
    }

    private void _getImage(Uri uri, ResizeOptions resizeOptions, final ImageCallback imageCallback) {
        BaseBitmapDataSubscriber dataSubscriber = new BaseBitmapDataSubscriber() {
            @Override
            protected void onNewResultImpl(Bitmap bitmap) {
                if (bitmap != null) {
                    if (bitmap.getConfig() != null) {
                        bitmap = bitmap.copy(bitmap.getConfig(), true);
                        imageCallback.invoke(bitmap);
                    } else {
                        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                        imageCallback.invoke(bitmap);
                    }
                } else {
                    imageCallback.invoke(null);
                }
            }

            @Override
            protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                imageCallback.invoke(null);
            }
        };

        ImageRequestBuilder builder = ImageRequestBuilder.newBuilderWithSource(uri);
        if (resizeOptions != null) {
            builder = builder.setResizeOptions(resizeOptions);
        }
        ImageRequest imageRequest = builder.build();

        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchDecodedImage(imageRequest, null);
        dataSource.subscribe(dataSubscriber, UiThreadImmediateExecutorService.getInstance());
    }

    // TODO: 实现sendRequest、sendSuccessResponse、sendErrorCommonResponse、sendErrorUserCancelResponse

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
        } else if (baseResp.getType() == ConstantsAPI.COMMAND_LAUNCH_WX_MINIPROGRAM) {
            WXLaunchMiniProgram.Resp resp = (WXLaunchMiniProgram.Resp) baseResp;
            // 对应JsApi navigateBackApplication中的extraData字段数据
            String extraData = resp.extMsg;
            map.putString("extraData", extraData);
        }

        this.getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("WeChat_Resp", map);
    }

    private interface ImageCallback {
        void invoke(@Nullable Bitmap bitmap);
    }

    private interface MediaObjectCallback {
        void invoke(@Nullable WXMediaMessage.IMediaObject mediaObject);
    }

}
