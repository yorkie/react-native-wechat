package com.theweflex.react;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;

import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.internal.Preconditions;
import com.facebook.common.references.CloseableReference;
import com.facebook.common.util.UriUtil;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.OrientedDrawable;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableAnimatedImage;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.CloseableStaticBitmap;
import com.facebook.imagepipeline.image.EncodedImage;
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
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXFileObject;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXMusicObject;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXVideoObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

/**
 * Created by tdzl2_000 on 2015-10-10.
 */
public class WeChatModule extends ReactContextBaseJavaModule implements IWXAPIEventHandler {
    private String appId;

    private IWXAPI api = null;
    private final static String NOT_REGISTERED = "registerApp required.";
    private final static String INVOKE_FAILED = "WeChat API invoke returns false.";

    public WeChatModule(ReactApplicationContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "RCTWeChat";
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
        if (api != null){
            api = null;
        }
        modules.remove(this);
    }

    public static void handleIntent(Intent intent) {
        for (WeChatModule mod : modules){
            mod.api.handleIntent(intent, mod);
        }
    }

    @ReactMethod
    public void registerApp(String appid, Callback callback) {
        api = WXAPIFactory.createWXAPI(this.getReactApplicationContext().getBaseContext(), appid, true);
        callback.invoke(api.registerApp(appid) ? null : INVOKE_FAILED);
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
        callback.invoke(null, api.isWXAppSupportAPI());
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
        callback.invoke(api.openWXApp() ? null : INVOKE_FAILED);
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
        callback.invoke(api.sendReq(req) ? null : INVOKE_FAILED);
    }

    @ReactMethod
    public void shareToTimeline(ReadableMap data, Callback callback){
        if (api == null) {
            callback.invoke(NOT_REGISTERED);
            return;
        }
        _share(SendMessageToWX.Req.WXSceneTimeline, data, callback);
    }

    @ReactMethod
    public void shareToSession(ReadableMap data, Callback callback){
        if (api == null) {
            callback.invoke(NOT_REGISTERED);
            return;
        }
        _share(SendMessageToWX.Req.WXSceneSession, data, callback);
    }

    private void _share(final int scene, final ReadableMap data, final Callback callback){
        if (data.hasKey("thumbImage")) {
            String imageUrl = data.getString("thumbImage");
            Uri uri = null;
            try {
                uri = Uri.parse(imageUrl);
                // Verify scheme is set, so that relative uri (used by static resources) are not handled.
                if (uri.getScheme() == null) {
                    uri = null;
                }
            } catch (Exception e) {
                // ignore malformed uri, then attempt to extract resource ID.
            }
            if (uri == null) {
                uri = getResourceDrawableUri(getReactApplicationContext(), imageUrl);

                if (uri == null) {
                    this._share(scene, data, null, callback);
                    return;
                }
            } else {
            }

            DataSubscriber<CloseableReference<CloseableImage>> dataSubscriber =
                    new BaseDataSubscriber<CloseableReference<CloseableImage>>() {
                        @Override
                        public void onNewResultImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                            // isFinished must be obtained before image, otherwise we might set intermediate result
                            // as final image.
                            boolean isFinished = dataSource.isFinished();
//                        float progress = dataSource.getProgress();
                            CloseableReference<CloseableImage> image = dataSource.getResult();
                            if (image != null) {
                                Drawable drawable = WeChatModule.this._createDrawable(image);
                                Bitmap bitmap = WeChatModule.this._drawable2Bitmap(drawable);
                                WeChatModule.this._share(scene, data, bitmap, callback);
                            } else if (isFinished) {
                                WeChatModule.this._share(scene, data, null, callback);
                            }
                            dataSource.close();
                        }
                        @Override
                        public void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                            dataSource.close();
                            WeChatModule.this._share(scene, data, null, callback);
                        }

                        @Override
                        public void onProgressUpdate(DataSource<CloseableReference<CloseableImage>> dataSource) {
                        }
                    };
            this._getImage(uri, dataSubscriber);
        }
        else {
            this._share(scene, data, null, callback);
        }
    }

    private void  _getImage(Uri uri, DataSubscriber<CloseableReference<CloseableImage>> dataSubscriber) {
        ResizeOptions resizeOptions = new ResizeOptions(100, 100);

        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri)
                .setResizeOptions(resizeOptions)
                .build();

        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchDecodedImage(imageRequest, null);
        dataSource.subscribe(dataSubscriber, UiThreadImmediateExecutorService.getInstance());
    }

    private static Uri getResourceDrawableUri(Context context, String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        name = name.toLowerCase().replace("-", "_");
        int resId = context.getResources().getIdentifier(
                name,
                "drawable",
                context.getPackageName());
        return new Uri.Builder()
                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                .path(String.valueOf(resId))
                .build();
    }

    private Drawable _createDrawable(CloseableReference<CloseableImage> image) {
        Preconditions.checkState(CloseableReference.isValid(image));
        CloseableImage closeableImage = image.get();
        if (closeableImage instanceof CloseableStaticBitmap) {
            CloseableStaticBitmap closeableStaticBitmap = (CloseableStaticBitmap) closeableImage;
            BitmapDrawable bitmapDrawable = new BitmapDrawable(
                    getReactApplicationContext().getResources(),
                    closeableStaticBitmap.getUnderlyingBitmap());
            if (closeableStaticBitmap.getRotationAngle() == 0 ||
                    closeableStaticBitmap.getRotationAngle() == EncodedImage.UNKNOWN_ROTATION_ANGLE) {
                return bitmapDrawable;
            } else {
                return new OrientedDrawable(bitmapDrawable, closeableStaticBitmap.getRotationAngle());
            }
        } else if (closeableImage instanceof CloseableAnimatedImage) {
            return Fresco.getImagePipelineFactory().getAnimatedDrawableFactory().create(
                    ((CloseableAnimatedImage) closeableImage).getImageResult());
        } else {
            throw new UnsupportedOperationException("Unrecognized image class: " + closeableImage);
        }
    }

    private Bitmap _drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable) {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        } else {
            return null;
        }
    }

    private void _share(int scene, ReadableMap data, Bitmap thumbImage, Callback callback) {
        WXMediaMessage message = new WXMediaMessage();
        if (data.hasKey("title")){
            message.title = data.getString("title");
        }
        if (data.hasKey("description")) {
            message.description = data.getString("description");
        }

        String type = "news";
        if (data.hasKey("type")) {
            type = data.getString("type");
        }
        if (type.equals("news")) {
            message.mediaObject = _jsonToWebpageMedia(data);
        }
        else if (type.equals("text")) {
            message.mediaObject = _jsonToTextMedia(data);
        }
        else if (type.equals("image")) {
            message.mediaObject = __jsonToImageMedia(data);
        }
        else if (type.equals("video")) {
            message.mediaObject = __jsonToVideoMedia(data);
        }
        else if (type.equals("audio")) {
            message.mediaObject = __jsonToMusicMedia(data);
        }
        else if (type.equals("file")) {
            message.mediaObject = __jsonToFileMedia(data);
        }

        if (thumbImage != null) {
            message.setThumbImage(thumbImage);
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

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.message = message;
        req.scene = scene;

        callback.invoke(api.sendReq(req) ? null : INVOKE_FAILED);
    }

    private WXTextObject _jsonToTextMedia(ReadableMap data)
    {
        WXTextObject ret = new WXTextObject();
        if (data.hasKey("description")) {
            ret.text = data.getString("description");
        }
        return ret;
    }

    private WXWebpageObject _jsonToWebpageMedia(ReadableMap data){
        WXWebpageObject ret = new WXWebpageObject();
        if (data.hasKey("webpageUrl")){
            ret.webpageUrl = data.getString("webpageUrl");
        }
        if (data.hasKey("extInfo")){
            ret.extInfo = data.getString("extInfo");
        }
        return ret;
    }

    private WXImageObject __jsonToImageMedia(ReadableMap data){
        WXImageObject ret = new WXImageObject();
        if (data.hasKey("imageUrl")) {
            ret.imageUrl = data.getString("imageUrl");
        }
        return ret;
    }

    private WXMusicObject __jsonToMusicMedia(ReadableMap data){
        WXMusicObject ret = new WXMusicObject();
        if (data.hasKey("musicUrl")) {
            ret.musicUrl = data.getString("musicUrl");
        }
        return ret;
    }

    private WXVideoObject __jsonToVideoMedia(ReadableMap data){
        WXVideoObject ret = new WXVideoObject();
        if (data.hasKey("videoUrl")) {
            ret.videoUrl = data.getString("videoUrl");
        }
        return ret;
    }

    private WXFileObject __jsonToFileMedia(ReadableMap data){

        WXFileObject ret = new WXFileObject();
        if (data.hasKey("filePath")) {
            String path = new File(URI.create(data.getString("filePath"))).getPath();
            ret.filePath = path;
        }
        return ret;
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
            SendAuth.Resp resp = (SendAuth.Resp)(baseResp);

            map.putString("type", "SendAuth.Resp");
            map.putString("code", resp.code);
            map.putString("state", resp.state);
            map.putString("url", resp.url);
            map.putString("lang", resp.lang);
            map.putString("country", resp.country);
        } else if (baseResp instanceof SendMessageToWX.Resp){
            SendMessageToWX.Resp resp = (SendMessageToWX.Resp)(baseResp);
            map.putString("type", "SendMessageToWX.Resp");
        }

        this.getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("WeChat_Resp", map);
    }


}
