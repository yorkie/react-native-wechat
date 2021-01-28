package com.theweflex.react;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.common.util.UriUtil;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.modelbiz.WXOpenBusinessWebview;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.ShowMessageFromWX;
import com.tencent.mm.opensdk.modelmsg.WXAppExtendObject;
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
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import javax.annotation.Nullable;

public class WechatModule extends ReactContextBaseJavaModule implements IWXAPIEventHandler {
    private String appId;
    private IWXAPI api = null;
    private final static String NOT_REGISTERED = "registerApp required.";
    private final static String INVOKE_FAILED = "Wechat API invoke returns false.";
    private final static String INVALID_ARGUMENT = "invalid argument.";
    private final static int THUMB_SIZE = 32; // The size of thumb image in KB.
    private static Intent wechatIntent;

    private static byte[] bitmapToBytesArray(Bitmap bitmap, final boolean needRecycle) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        if (needRecycle) {
            bitmap.recycle();
        }
        try {
            baos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    /**
     * Compress the bitmap image
     */
    private static byte[] bitmapResizeGetBytes(Bitmap image, int size, final boolean needRecycle) {
        // FIXME(little-snow-fox): 该算法存在效率问题，希望有"义士"可以进行优化
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
        if (needRecycle)
            image.recycle();

        return baos.toByteArray();
    }

    public WechatModule(ReactApplicationContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "Wechat";
    }

    /**
     * fix Native module WechatModule tried to override WechatModule for module name RCTWechat.
     * If this was your intention, return true from WechatModule#canOverrideExistingModule() bug
     *
     * @return
     */
    public boolean canOverrideExistingModule() {
        return true;
    }

    private static ArrayList<WechatModule> modules = new ArrayList<>();

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

    /**
     * @param intent 冷启动需要将intent信息缓存，等到微信注册好再处理intent数据
     */
    public static void handleIntent(Intent intent) {
        wechatIntent = intent;
        for (WechatModule mod : modules) {
            if (mod.api != null)
                mod.api.handleIntent(wechatIntent, mod);
        }
    }

    @ReactMethod
    public void registerApp(String appid, String universalLink, Promise promise) {
        this.appId = appid;
        api = WXAPIFactory.createWXAPI(this.getReactApplicationContext().getBaseContext(), appid, true);
        boolean result = api.registerApp(appid);
        if (result) {
            promise.resolve(null);
            for (WechatModule mod : modules) {
                if (mod.api != null && wechatIntent != null)
                    mod.api.handleIntent(wechatIntent, mod);
            }
        } else {
            promise.reject(BaseResp.ErrCode.ERR_COMM + "", "register failed");
        }
    }

    @ReactMethod
    public void isWXAppInstalled(Promise promise) {
        if (api == null) {
            promise.reject(BaseResp.ErrCode.ERR_COMM + "", NOT_REGISTERED);
            return;
        }
        promise.resolve(api.isWXAppInstalled());
    }

    @ReactMethod
    public void isWXAppSupportApi(Promise promise) {
        if (api == null) {
            promise.reject(BaseResp.ErrCode.ERR_COMM + "", NOT_REGISTERED);
            return;
        }
        promise.resolve(api.getWXAppSupportAPI());
    }

    @ReactMethod
    public void openWXApp(Promise promise) {
        if (api == null) {
            promise.reject(BaseResp.ErrCode.ERR_COMM + "", NOT_REGISTERED);
            return;
        }
        boolean result = api.openWXApp();
        if (result) {
            promise.resolve(null);
        } else {
            promise.reject(BaseResp.ErrCode.ERR_COMM + "", "register failed");
        }

    }

    @ReactMethod
    public void openMiniprogram(ReadableMap data, Promise promise) {
        if (api == null) {
            promise.reject(BaseResp.ErrCode.ERR_COMM + "", NOT_REGISTERED);
            return;
        }
        WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
        // 填小程序原始ID
        req.userName = data.getString("userName");
        // 拉起小程序页面的可带参路径，不填默认拉起小程序首页
        req.path = data.getString("path");
        // 可选打开开发版，体验版和正式版
        req.miniprogramType = data.getInt("miniprogramType");
        boolean success = api.sendReq(req);
        if (!success) {
            promise.reject(BaseResp.ErrCode.ERR_COMM + "", INVALID_ARGUMENT);
        }
    }

    @ReactMethod
    public void sendAuthRequest(String scope, String state, Promise promise) {
        if (api == null) {
            promise.reject(BaseResp.ErrCode.ERR_COMM + "", NOT_REGISTERED);
            return;
        }
        SendAuth.Req req = new SendAuth.Req();
        req.scope = scope;
        req.state = state;
        boolean result = api.sendReq(req);
        if (result) {
            promise.resolve(null);
        } else {
            promise.reject(BaseResp.ErrCode.ERR_COMM + "", "send Auth Request failed");
        }
    }

    private void sendShareRequest(final WXMediaMessage.IMediaObject media, final ReadableMap data, final Promise promise) {
        if (data.hasKey("thumbImageUrl")) {
            createImageRequest(Uri.parse(data.getString("thumbImageUrl")), new ImageCallback() {
                @Override
                public void invoke(@Nullable Bitmap thumb) {
                    WechatModule.this.sendShareRequest(media, thumb, data, promise);
                }
            });
        } else {
            this.sendShareRequest(media, null, data, promise);
        }
    }

    private void sendShareRequest(WXMediaMessage.IMediaObject media, Bitmap thumb, ReadableMap data, Promise promise) {
        WXMediaMessage message = new WXMediaMessage();
        message.mediaObject = media;
        if (data.hasKey("title")) {
            message.title = data.getString("title");
        }
        if (data.hasKey("description")) {
            message.description = data.getString("description");
        }
        if (data.hasKey("mediaTagName")) {
            message.mediaTagName = data.getString("mediaTagName");
        }
        if (data.hasKey("messageAction")) {
            message.messageAction = data.getString("messageAction");
        }
        if (data.hasKey("messageExt")) {
            message.messageExt = data.getString("messageExt");
        }
        if (thumb != null) {
            byte[] thumbData = bitmapToBytesArray(thumb, false);
            if (thumbData.length / 1024 > THUMB_SIZE) {
                message.thumbData = bitmapResizeGetBytes(thumb, THUMB_SIZE, true);
            } else {
                message.thumbData = thumbData;
                thumb.recycle();
            }
        }

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.scene = data.hasKey("scene") ? data.getInt("scene") : SendMessageToWX.Req.WXSceneSession;
        req.transaction = UUID.randomUUID().toString();
        req.message = message;
        boolean result = api.sendReq(req);
        if (result) {
            promise.resolve(null);
        } else {
            promise.reject(BaseResp.ErrCode.ERR_COMM + "", "sendReq failed");
        }
    }

    /**
     * Share the plain text
     *
     * @param data
     * @param promise
     */
    @ReactMethod
    public void shareText(ReadableMap data, Promise promise) {
        WXTextObject media = new WXTextObject();
        media.text = data.getString("text");
        this.sendShareRequest(media, null, data, promise);
    }

    /**
     * 分享图片
     *
     * @param data
     * @param promise
     */
    @ReactMethod
    public void shareImage(final ReadableMap data, final Promise promise) {
        Uri imgUrl;
        try {
            imgUrl = Uri.parse(data.getString("imageUrl"));
            if (imgUrl.getScheme() == null) {
                // handle static resource if no schema is provided.
                // todo Drawable TYPE
                imgUrl = getResourceDrawableURI(getReactApplicationContext(), "");
            }
        } catch (Exception ex) {
            imgUrl = null;
        }

        if (imgUrl == null) {
            promise.reject(BaseResp.ErrCode.ERR_COMM + "", "imgUrl is null");
            return;
        }
        createImageRequest(imgUrl, new ImageCallback() {
            @Override
            public void invoke(@Nullable Bitmap image) {
                WXImageObject media = new WXImageObject(image);
                WechatModule.this.sendShareRequest(media, image/* as thumb */, data, promise);
            }
        });
    }

    /**
     * 分享音乐
     *
     * @param data
     * @param promise
     */
    @ReactMethod
    public void shareMusic(final ReadableMap data, final Promise promise) {
        WXMusicObject media = new WXMusicObject();
        media.musicUrl = data.hasKey("musicUrl") ? data.getString("musicUrl") : null;
        media.musicLowBandUrl = data.hasKey("musicLowBandUrl") ? data.getString("musicLowBandUrl") : null;
        media.musicDataUrl = data.hasKey("musicDataUrl") ? data.getString("musicDataUrl") : null;
        media.musicUrl = data.hasKey("musicUrl") ? data.getString("musicUrl") : null;
        media.musicLowBandDataUrl = data.hasKey("musicLowBandDataUrl") ? data.getString("musicLowBandDataUrl") : null;
        this.sendShareRequest(media, data, promise);
    }

    /**
     * 分享视频
     *
     * @param data
     * @param promise
     */
    @ReactMethod
    public void shareVideo(final ReadableMap data, final Promise promise) {
        WXVideoObject media = new WXVideoObject();
        media.videoUrl = data.hasKey("videoUrl") ? data.getString("videoUrl") : null;
        media.videoLowBandUrl = data.hasKey("videoLowBandUrl") ? data.getString("videoLowBandUrl") : null;
        this.sendShareRequest(media, data, promise);
    }

    /**
     * 分享网页
     *
     * @param data
     * @param promise
     */
    @ReactMethod
    public void shareWebpage(final ReadableMap data, final Promise promise) {
        WXWebpageObject media = new WXWebpageObject();
        media.webpageUrl = data.hasKey("webpageUrl") ? data.getString("webpageUrl") : null;
        this.sendShareRequest(media, data, promise);
    }

    /**
     * 分享网页
     *
     * @param data
     * @param promise
     */
    @ReactMethod
    public void shareFile(final ReadableMap data, final Promise promise) {
        WXFileObject media = new WXFileObject(data.getString("filePath"));
        this.sendShareRequest(media, data, promise);
    }

    /**
     * 分享小程序
     *
     * @param data
     * @param promise
     */
    @ReactMethod
    public void shareMiniprogram(final ReadableMap data, final Promise promise) {
        WXMiniProgramObject media = new WXMiniProgramObject();
        ReadableMap miniprogram = data.getMap("miniprogram");
        media.webpageUrl = data.hasKey("webpageUrl") ? data.getString("webpageUrl") : null;
        media.miniprogramType = miniprogram.hasKey("miniprogramType") ? miniprogram.getInt("miniprogramType") : WXMiniProgramObject.MINIPTOGRAM_TYPE_RELEASE;
        media.userName = miniprogram.hasKey("userName") ? miniprogram.getString("userName") : null;
        media.path = miniprogram.hasKey("path") ? miniprogram.getString("path") : null;
        this.sendShareRequest(media, data, promise);

    }

    @ReactMethod
    public void pay(ReadableMap data, Promise promise) {
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
        boolean result = api.sendReq(payReq);
        if (result) {
            promise.resolve(null);
        } else {
            promise.reject(BaseResp.ErrCode.ERR_COMM + "", INVOKE_FAILED);
        }
    }

    @ReactMethod
    public void entrust(final ReadableMap data, final Promise promise) {
        if (!data.hasKey("preEntrustWebId")) {
            promise.reject(BaseResp.ErrCode.ERR_COMM + "", INVALID_ARGUMENT);
            return;
        }

        WXOpenBusinessWebview.Req req = new WXOpenBusinessWebview.Req();
        req.businessType = 12;//固定值
        HashMap queryInfo = new HashMap<>();
        queryInfo.put("pre_entrustweb_id", data.getString("preEntrustWebId"));
        req.queryInfo = queryInfo;
        boolean result = api.sendReq(req);
        if (result) {
            promise.resolve(null);
        } else {
            promise.reject(BaseResp.ErrCode.ERR_COMM + "", INVOKE_FAILED);
        }
    }

    private static void createImageRequest(Uri uri, final ImageCallback imageCallback) {
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
        // if (resizeOptions != null) {
        //     builder = builder.setResizeOptions(resizeOptions);
        // }
        ImageRequest imageRequest = builder.build();
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchDecodedImage(imageRequest, null);
        dataSource.subscribe(dataSubscriber, UiThreadImmediateExecutorService.getInstance());
    }

    private static Uri getResourceDrawableURI(Context ctx, String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        // FIXME(Yorkie): what's this for?
        name = name.toLowerCase().replace("-", "_");
        int id = ctx.getResources().getIdentifier(name, "drawable", ctx.getPackageName());
        if (id == 0) {
            return null;
        } else {
            return new Uri.Builder()
                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                .path(String.valueOf(id))
                .build();
        }
    }

    @Override
    public void onReq(BaseReq req) {
        switch (req.getType()) {
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
                goToShowMsg((ShowMessageFromWX.Req) req);
                break;
            default:
                break;
        }
    }

    private void goToShowMsg(ShowMessageFromWX.Req showReq) {
        WXMediaMessage wxMsg = showReq.message;
        WXAppExtendObject obj = (WXAppExtendObject) wxMsg.mediaObject;
        WritableMap map = Arguments.createMap();
        map.putString("country", showReq.country);
        map.putString("lang", showReq.lang);
        map.putString("extInfo", obj.extInfo);
        this.getReactApplicationContext()
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit("LaunchFromWX.Req", map);
    }

    @Override
    public void onResp(BaseResp baseResp) {
        WritableMap map = Arguments.createMap();
        String emitType = null;
        map.putInt("errCode", baseResp.errCode);
        map.putString("errStr", baseResp.errStr);
        map.putString("openId", baseResp.openId);
        map.putString("transaction", baseResp.transaction);

        if (baseResp instanceof SendAuth.Resp) {
            SendAuth.Resp resp = (SendAuth.Resp) (baseResp);
            emitType = "SendAuth.Resp";
            map.putString("code", resp.code);
            map.putString("state", resp.state);
            map.putString("url", resp.url);
            map.putString("lang", resp.lang);
            map.putString("country", resp.country);
        } else if (baseResp instanceof SendMessageToWX.Resp) {
            SendMessageToWX.Resp resp = (SendMessageToWX.Resp) (baseResp);
            emitType = "SendMessageToWX.Resp";
        } else if (baseResp instanceof WXOpenBusinessWebview.Resp) {
            emitType = "WXOpenBusinessWebview.Resp";
            map.putInt("businessType", ((WXOpenBusinessWebview.Resp) baseResp).businessType);
            map.putString("resultInfo", ((WXOpenBusinessWebview.Resp) baseResp).resultInfo);
        } else if (baseResp.getType() == ConstantsAPI.COMMAND_LAUNCH_WX_MINIPROGRAM) {
            WXLaunchMiniProgram.Resp resp = (WXLaunchMiniProgram.Resp) (baseResp);
            emitType = "LaunchMiniprogram.Resp";
            map.putString("data", resp.extMsg);
        } else if (baseResp instanceof PayResp) {
            PayResp resp = (PayResp) (baseResp);
            emitType = "PayReq.Resp";
            map.putString("returnKey", resp.returnKey);
        }
        if (emitType != null) {
            this.getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(emitType, map);
        }
    }

    private interface ImageCallback {
        void invoke(@Nullable Bitmap bitmap);
    }

    private interface MediaObjectCallback {
        void invoke(@Nullable WXMediaMessage.IMediaObject mediaObject);
    }

}
