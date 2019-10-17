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

public class WeChatModule extends ReactContextBaseJavaModule implements IWXAPIEventHandler {
    private String appId;
    private IWXAPI api = null;
    private final static String NOT_REGISTERED = "registerApp required.";
    private final static String INVOKE_FAILED = "WeChat API invoke returns false.";
    private final static String INVALID_ARGUMENT = "invalid argument.";
    private final static int THUMB_SIZE = 32; // The size of thumb image in KB.

    private static byte[] bitmapTopBytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        bitmap.recycle();
        return baos.toByteArray();
    }

    /**
     * Compress the bitmap image
     */
    private static byte[] bitmapResizeGetBytes(Bitmap image, int size) {
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
    public void openMiniProgram(ReadableMap data, Callback callback) {
        if (api == null) {
            callback.invoke(NOT_REGISTERED);
            return;
        }
        WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
        // 填小程序原始ID
        req.userName = data.getString("userName");
        //拉起小程序页面的可带参路径，不填默认拉起小程序首页
        req.path = data.getString("path");
        // 可选打开开发版，体验版和正式版
        req.miniprogramType = data.getInt("miniProgramType");
        boolean success = api.sendReq(req);
        if (!success) {
            callback.invoke(INVALID_ARGUMENT);
        }
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

    private void sendShareRequest(WXMediaMessage.IMediaObject media, ReadableMap data, Callback callback) {
        if (data.hasKey("thumbImageUrl")) {
            createImageRequest(Uri.parse(data.getString("thumbImageUrl")), new ImageCallback() {
                @Override
                public void invoke(@Nullable Bitmap thumb) {
                    this.sendShareRequest(media, thumb, data, callback);
                }
            });
        } else {
            this.sendShareRequest(media, null, data, callback);
        }
    }

    private void sendShareRequest(WXMediaMessage.IMediaObject media, Bitmap thumb, ReadableMap data, Callback callback) {
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
            if (thumb.length() / 1024 > THUMB_SIZE) {
                message.thumbData = bitmapResizeGetBytes(thumb, THUMB_SIZE);
            } else {
                message.thumbData = thumb;
            }
        }

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.scene = data.hasKey("scene") ? data.getInt("scene") : SendMessageToWX.Req.WXSceneSession;
        req.transaction = UUID.randomUUID().toString();
        req.message = message;
        callback.invoke(null, api.sendReq(req));
    }

    /**
     * Share the plain text
     * @param data
     * @param callback
     */
    @ReactMethod
    public void shareText(ReadableMap data, Callback callback) {
        WXTextObject media = new WXTextObject();
        media.text = data.getString("text");
        this.sendShareRequest(media, null, data, callback);
    }

    /**
     * 分享图片
     * @param data
     * @param callback
     */
    @ReactMethod
    public void shareImage(final ReadableMap data, final Callback callback) {
        Uri imgUrl;
        try {
            imgUrl = Uri.parse(data.getString("imageUrl"));
            if (imgUrl.getScheme() == null) {
                // handle static resource if no schema is provided.
                imgUrl = getResourceDrawableURI(getReactApplicationContext(), imgUrl);
            }
        } catch (Exception ex) {
            imgUrl = null;
        }

        if (imgUrl == null) {
            callback.invoke(null);
            return;
        }
        createImageRequest(imgUrl, new ImageCallback() {
            @Override
            public void invoke(@Nullable Bitmap image) {
                WXImageObject media = new WXImageObject(image);
                this.sendShareRequest(media, image/* as thumb */, data, callback);
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
        WXMusicObject media = new WXMusicObject();
        media.musicUrl = data.hasKey("musicUrl") ? data.getString("musicUrl") : null;
        media.musicLowBandUrl = data.hasKey("musicLowBandUrl") ? data.getString("musicLowBandUrl") : null;
        media.musicDataUrl = data.hasKey("musicDataUrl") ? data.getString("musicDataUrl") : null;
        media.musicUrl = data.hasKey("musicUrl") ? data.getString("musicUrl") : null;
        media.musicLowBandDataUrl = data.hasKey("musicLowBandDataUrl") ? data.getString("musicLowBandDataUrl") : null;
        this.sendShareRequest(media, data, callback);
    }

    /**
     * 分享视频
     * @param data
     * @param callback
     */
    @ReactMethod
    public void shareVideo(final ReadableMap data, final Callback callback) {
        WXVideoObject media = new WXVideoObject();
        media.videoUrl = data.hasKey("videoUrl") ? data.getString("videoUrl") : null;
        media.videoLowBandUrl = data.hasKey("videoLowBandUrl") ? data.getString("videoLowBandUrl") : null;
        this.sendShareRequest(media, data, callback);
    }

    /**
     * 分享网页
     * @param data
     * @param callback
     */
    @ReactMethod
    public void shareWebpage(final ReadableMap data, final Callback callback) {
        WXWebpageObject media = new WXWebpageObject();
        media.webpageUrl = data.hasKey("webpageUrl") ? data.getString("webpageUrl") : null;
        this.sendShareRequest(media, data, callback);
    }

    /**
     * 分享网页
     * @param data
     * @param callback
     */
    @ReactMethod
    public void shareFile(final ReadableMap data, final Callback callback) {
        WXFileObject media = new WXFileObject(data.getString("filePath"));
        this.sendShareRequest(media, data, callback);
    }

    /**
     * 分享小程序
     * @param data
     * @param callback
     */
    @ReactMethod
    public void shareMiniProgram(final ReadableMap data, final Callback callback) {
        WXMiniProgramObject media = new WXMiniProgramObject();
        media.webpageUrl = data.hasKey("webpageUrl") ? data.getString("webpageUrl") : null;
        media.miniprogramType = data.hasKey("miniProgramType") ? data.getInt("miniProgramType") : WXMiniProgramObject.MINIPTOGRAM_TYPE_RELEASE;
        media.userName = data.hasKey("miniProgramId") ? data.getString("miniProgramId") : null;
        media.path = data.hasKey("miniProgramPath") ? data.getString("miniProgramPath") : null;
        this.sendShareRequest(media, data, callback);

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

    // TODO: implement sendRequest、sendSuccessResponse、sendErrorCommonResponse、sendErrorUserCancelResponse.

    @Override
    public void onReq(BaseReq baseReq) {
        // TODO
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
        } else if (baseResp.getType() == ConstantsAPI.COMMAND_LAUNCH_WX_MINIPROGRAM) {
            WXLaunchMiniProgram.Resp resp = (WXLaunchMiniProgram.Resp) (baseResp);
            map.putString("type", "LaunchMiniProgram.Resp");
            map.putString("data", resp.extMsg);
        } else if (baseResp instanceof PayResp) {
            PayResp resp = (PayResp) (baseResp);
            map.putString("type", "PayReq.Resp");
            map.putString("returnKey", resp.returnKey);
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
