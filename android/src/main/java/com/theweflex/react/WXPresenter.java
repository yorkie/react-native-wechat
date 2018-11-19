/*
 * Copyright (c) 2018. chenqiang Inc. All rights reserved.
 */

package com.theweflex.react;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.internal.Closeables;
import com.facebook.common.internal.Preconditions;
import com.facebook.common.memory.PooledByteBuffer;
import com.facebook.common.memory.PooledByteBufferInputStream;
import com.facebook.common.references.CloseableReference;
import com.facebook.common.util.UriUtil;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.modules.network.OkHttpClientProvider;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXEmojiObject;
import com.tencent.mm.sdk.modelmsg.WXFileObject;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXMusicObject;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXVideoObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.theweflex.react.exception.InvalidArgumentException;
import com.theweflex.react.exception.InvokeException;
import com.theweflex.react.exception.NotRegisterException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

/**
 * 处理微信具体事务的代理类
 *
 * @author chenqiang
 * @date 2018/8/9
 */
public class WXPresenter {

    private static final int IMAGE_SIZE = 32768;
    private static final boolean DEBUG = false;
    ReactApplicationContext mReactApplicationContext;
    IWXAPI mWxapi;
    boolean mHasRegister;
    private HashMap<String, String> mExtensionMap;
    private static String mAppId;

    public WXPresenter(ReactApplicationContext reactApplicationContext) {
        this.mReactApplicationContext = reactApplicationContext;
        mExtensionMap = new HashMap<>();
    }

    public static String getAppId() throws NotRegisterException {
        if (TextUtils.isEmpty(mAppId)) throw new NotRegisterException();
        return mAppId;
    }

    public void checkRegister() throws NotRegisterException {
        if (!mHasRegister || mWxapi == null) throw new NotRegisterException();
    }

    public boolean register(String appId) {
        WXPresenter.mAppId = appId;
        mWxapi = WXAPIFactory.createWXAPI(mReactApplicationContext.getApplicationContext()
                , appId);
        mHasRegister = true;
        return mWxapi.registerApp(appId);
    }

    public boolean isWXAppInstalled() throws NotRegisterException {
        checkRegister();
        return mWxapi.isWXAppInstalled();
    }

    public boolean isWXAppSupportApi() throws NotRegisterException {
        checkRegister();
        return mWxapi.isWXAppSupportAPI();
    }

    public int getApiVersion() throws NotRegisterException {
        checkRegister();
        return mWxapi.getWXAppSupportAPI();
    }

    public boolean openWXApp() throws NotRegisterException {
        checkRegister();
        return mWxapi.openWXApp();
    }

    public boolean sendAuthRequest(String scope, String state) throws NotRegisterException {
        checkRegister();
        SendAuth.Req req = new SendAuth.Req();
        req.scope = scope;
        req.state = state;
        return mWxapi.sendReq(req);
    }

    public boolean share(SendMessageToWX.Req req) throws NotRegisterException {
        checkRegister();
        return mWxapi.sendReq(req);
    }

    //----------RN start
    public String rnPay(ReadableMap data) throws NotRegisterException, InvokeException {
        checkRegister();
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
        payReq.appId = mAppId;

        if (mWxapi.sendReq(payReq)) {
            return null;
        } else {
            throw new InvokeException();
        }
    }

    public void rnShare(final int scene, final ReadableMap data,
                        final ShareCallback callback) throws
            InvalidArgumentException, NotRegisterException {
        checkRegister();
        Uri thumbUri = null;
        if (data.hasKey("thumbImage")) {
            String thumbImage = data.getString("thumbImage");
            try {
                thumbUri = Uri.parse(thumbImage);
                if (thumbUri.getScheme() == null) {
                    thumbUri = getResourceDrawableUri(mReactApplicationContext, thumbImage);
                }
            } catch (Exception e) {
                // ignore malformed uri, then attempt to extract resource ID.
            }
        }

        String imageUrl = data.hasKey("imageUrl") ? data.getString("imageUrl") : null;
        String type = data.hasKey("type") ? data.getString("type") : null;
        if (type == null) {
            throw new InvalidArgumentException();
        }

        if (thumbUri == null) { // 无缩略图
            this.shareWithThumb(scene, data, null, callback);
        } else { // 有缩略图
            if ("imageUrl".equals(type) || "imageResource".equals(type) || "imageFile".equals(type)) { // 分享图片
                if (imageUrl == null) {
                    throw new InvalidArgumentException();
                } else {
                    final Uri finalThumbUri = thumbUri;
                    getExtension(imageUrl, new ExtensionCallback() {
                        @Override
                        public void onExtension(@Nullable String extension) {
                            getImage(finalThumbUri, new ResizeOptions(100, 100), new ImageCallback() {
                                @Override
                                public void onBitmap(@Nullable Bitmap bitmap) throws InvalidArgumentException {
                                    shareWithThumb(scene, data, bitmap, callback);
                                }
                            });
                        }
                    });
                }
            } else { // 分享非图片
//                shareWithThumb(scene, data, null, callback);
                getImage(thumbUri, new ResizeOptions(100, 100), new ImageCallback() {
                    @Override
                    public void onBitmap(@Nullable Bitmap bitmap) throws InvalidArgumentException {

                        shareWithThumb(scene, data, bitmap, callback);
                    }
                });
            }

        }
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

        if (resId == 0) {
            return null;
        } else {
            return new Uri.Builder()
                    .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                    .path(String.valueOf(resId))
                    .build();
        }
    }

    /**
     * 获取链接指向文件后缀
     *
     * @param url
     * @return
     */
    private void getExtension(final String url, final ExtensionCallback callback) {
        if (url == null) {
            callback.onExtension(null);
            return;
        }
        String temp = mExtensionMap.get(url);
        if (temp != null) {
            callback.onExtension(temp);
            return;
        }
        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme();
        if (scheme == null || scheme.equals("file")) {
            String path = uri.getPath();
            int index = path.lastIndexOf(".");
            String extension;
            if (index > 0) {
                extension = path.substring(index + 1);
            } else {
                extension = null;
            }
            if (extension != null) {
                mExtensionMap.put(url, extension);
            }
            callback.onExtension(extension);
        } else {
            Request request = new Request.Builder().url(url).head().build();
            OkHttpClient client = OkHttpClientProvider.getOkHttpClient();
            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onExtension(null);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String contentType = response.body().contentType().toString();
                    String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentType);
                    if (extension != null) {
                        mExtensionMap.put(url, extension);
                    }
                    callback.onExtension(extension);
                }
            });
        }
    }


    private void shareWithThumb(final int scene, final ReadableMap data, final Bitmap
            thumbImage, final
                                ShareCallback callback) throws InvalidArgumentException {
        if (!data.hasKey("type")) {
            throw new InvalidArgumentException();
        }
        String type = data.getString("type");

        WXMediaMessage.IMediaObject mediaObject = null;
        if (type.equals("news")) {
            mediaObject = _jsonToWebpageMedia(data);
        } else if (type.equals("text")) {
            mediaObject = _jsonToTextMedia(data);
        } else if (type.equals("imageUrl") || type.equals("imageResource")) {
            __jsonToImageUrlMedia(data, new MediaObjectCallback() {
                @Override
                public void onMedia(@Nullable WXMediaMessage.IMediaObject mediaObject) throws InvalidArgumentException {
                    if (mediaObject == null) {
                        throw new InvalidArgumentException();
                    } else {
                        shareWithMedia(scene, data, thumbImage, mediaObject, callback);
                    }
                }
            });
            return;
        } else if (type.equals("imageFile")) {
            __jsonToImageFileMedia(data, new MediaObjectCallback() {
                @Override
                public void onMedia(@Nullable WXMediaMessage.IMediaObject mediaObject) throws InvalidArgumentException {
                    if (mediaObject == null) {
                        throw new InvalidArgumentException();
                    } else {
                        shareWithMedia(scene, data, thumbImage, mediaObject, callback);
                    }
                }
            });
            return;
        } else if (type.equals("video")) {
            mediaObject = __jsonToVideoMedia(data);
        } else if (type.equals("audio")) {
            mediaObject = __jsonToMusicMedia(data);
        } else if (type.equals("file")) {
            mediaObject = __jsonToFileMedia(data);
        }

        if (mediaObject == null) {
            throw new InvalidArgumentException();
        } else {
            shareWithMedia(scene, data, thumbImage, mediaObject, callback);
        }
    }

    private void shareWithMedia(int scene, ReadableMap data, Bitmap thumbImage, WXMediaMessage
            .IMediaObject
            mediaObject, ShareCallback shareCallback) {

        WXMediaMessage message = new WXMediaMessage();
        message.mediaObject = mediaObject;
        if (thumbImage != null) {
            try {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                thumbImage.compress(Bitmap.CompressFormat.JPEG, 85, output);
                int options = 85;
                while (output.toByteArray().length > IMAGE_SIZE && options != 10) {
                    output.reset(); //清空baos
                    thumbImage.compress(Bitmap.CompressFormat.JPEG, options, output);//这里压缩options%，把压缩后的数据存放到baos中
                    options -= 10;
                }
                thumbImage.recycle();
                message.thumbData = output.toByteArray();
            }catch (Throwable t){
                if(DEBUG){
                    Log.e("wechat", "shareWithMedia: ", t);
                }
            }
        }

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

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.message = message;
        req.scene = scene;
        req.transaction = UUID.randomUUID().toString();
        shareCallback.onShareCompleted(mWxapi.sendReq(req));
    }

    private WXTextObject _jsonToTextMedia(ReadableMap data) {
        if (!data.hasKey("description")) {
            return null;
        }

        WXTextObject ret = new WXTextObject();
        ret.text = data.getString("description");
        return ret;
    }

    private WXWebpageObject _jsonToWebpageMedia(ReadableMap data) {
        if (!data.hasKey("webpageUrl")) {
            return null;
        }

        WXWebpageObject ret = new WXWebpageObject();
        ret.webpageUrl = data.getString("webpageUrl");
        if (data.hasKey("extInfo")) {
            ret.extInfo = data.getString("extInfo");
        }
        return ret;
    }

    private void __jsonToImageMedia(String imageUrl, final MediaObjectCallback callback) throws InvalidArgumentException {
        Uri imageUri;
        try {
            imageUri = Uri.parse(imageUrl);
            // Verify scheme is set, so that relative uri (used by static resources) are not handled.
            if (imageUri.getScheme() == null) {
                imageUri = getResourceDrawableUri(mReactApplicationContext, imageUrl);
            }
        } catch (Exception e) {
            imageUri = null;
        }

        if (imageUri == null) {
            callback.onMedia(null);
            return;
        }

        final Uri finalImageUri = imageUri;
        getExtension(imageUrl, new ExtensionCallback() {
            @Override
            public void onExtension(String extension) {
                if (extension != null && extension.toLowerCase().equals("gif")) {
                    getImageData(finalImageUri, null, new ImageDataCallback() {
                        @Override
                        public void onData(@Nullable byte[] bytes) throws InvalidArgumentException {
                            callback.onMedia(bytes == null ? null : new WXEmojiObject(bytes));
                        }
                    });
                } else {
                    getImage(finalImageUri, new ResizeOptions(7000, 7000), new ImageCallback() {
                        @Override
                        public void onBitmap(@Nullable Bitmap bitmap) throws InvalidArgumentException {
                            callback.onMedia(bitmap == null ? null : new WXImageObject(bitmap));
                        }
                    });
                }
            }
        });

    }

    private void __jsonToImageUrlMedia(ReadableMap data, MediaObjectCallback callback) throws InvalidArgumentException {
        if (!data.hasKey("imageUrl")) {
            callback.onMedia(null);
            return;
        }
        String imageUrl = data.getString("imageUrl");
        __jsonToImageMedia(imageUrl, callback);
    }

    private void __jsonToImageFileMedia(ReadableMap data, MediaObjectCallback callback) throws InvalidArgumentException {
        if (!data.hasKey("imageUrl")) {
            callback.onMedia(null);
            return;
        }
        String imageUrl = data.getString("imageUrl");
        if (!imageUrl.toLowerCase().startsWith("file://")) {
            imageUrl = "file://" + imageUrl;
        }
        __jsonToImageMedia(imageUrl, callback);
    }

    private WXMusicObject __jsonToMusicMedia(ReadableMap data) {
        if (!data.hasKey("musicUrl")) {
            return null;
        }
        WXMusicObject ret = new WXMusicObject();
        ret.musicUrl = data.getString("musicUrl");
        return ret;
    }

    private WXVideoObject __jsonToVideoMedia(ReadableMap data) {
        if (!data.hasKey("videoUrl")) {
            return null;
        }
        WXVideoObject ret = new WXVideoObject();
        ret.videoUrl = data.getString("videoUrl");
        return ret;
    }

    private WXFileObject __jsonToFileMedia(ReadableMap data) {
        if (!data.hasKey("filePath")) {
            return null;
        }
        return new WXFileObject(data.getString("filePath"));
    }

    private void getImage(Uri uri, ResizeOptions resizeOptions, final ImageCallback imageCallback) {
        BaseBitmapDataSubscriber dataSubscriber = new BaseBitmapDataSubscriber() {
            @Override
            protected void onNewResultImpl(Bitmap bitmap) {
                if (bitmap != null) {
                    bitmap = bitmap.copy(bitmap.getConfig(), true);
                }
                try {
                    imageCallback.onBitmap(bitmap);
                } catch (InvalidArgumentException ignore) {

                }
            }

            @Override
            protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
                try {
                    imageCallback.onBitmap(null);
                } catch (InvalidArgumentException ignore) {

                }
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

    private void getImageData(Uri uri, ResizeOptions resizeOptions, final ImageDataCallback imageCallback) {
        DataSubscriber<CloseableReference<PooledByteBuffer>> dataSubscriber =
                new BaseDataSubscriber<CloseableReference<PooledByteBuffer>>() {

                    @Override
                    protected void onNewResultImpl(DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
                        // isFinished must be obtained before image, otherwise we might set intermediate result
                        // as final image.
                        boolean isFinished = dataSource.isFinished();
                        CloseableReference<PooledByteBuffer> image = dataSource.getResult();
                        if (image != null) {
                            Preconditions.checkState(CloseableReference.isValid(image));
                            PooledByteBuffer result = image.get();
                            InputStream inputStream = new PooledByteBufferInputStream(result);
                            byte[] bytes = null;
                            try {
                                bytes = getBytes(inputStream);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    imageCallback.onData(bytes);
                                } catch (InvalidArgumentException ignore) {

                                }
                                Closeables.closeQuietly(inputStream);
                            }
                        } else if (isFinished) {
                            try {
                                imageCallback.onData(null);
                            } catch (InvalidArgumentException ignore) {

                            }
                        }
                        dataSource.close();
                    }

                    @Override
                    protected void onFailureImpl(DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
                        try {
                            imageCallback.onData(null);
                        } catch (InvalidArgumentException ignore) {

                        }
                    }
                };

        ImageRequestBuilder builder = ImageRequestBuilder.newBuilderWithSource(uri);
        if (resizeOptions != null) {
            builder = builder.setResizeOptions(resizeOptions);
        }
        ImageRequest imageRequest = builder.build();

        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<PooledByteBuffer>> dataSource = imagePipeline
                .fetchEncodedImage(imageRequest, mReactApplicationContext);
        dataSource.subscribe(dataSubscriber, UiThreadImmediateExecutorService.getInstance());
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }


    public void destroy() {
        mAppId = null;
        mWxapi = null;
        mExtensionMap.clear();
        mExtensionMap = null;
    }

    private interface ImageCallback {
        void onBitmap(@Nullable Bitmap bitmap) throws InvalidArgumentException;
    }

    private interface ImageDataCallback {
        void onData(@Nullable byte[] bytes) throws InvalidArgumentException;
    }

    private interface MediaObjectCallback {
        void onMedia(@Nullable WXMediaMessage.IMediaObject mediaObject) throws InvalidArgumentException;
    }

    private interface ExtensionCallback {
        void onExtension(@Nullable String extension);
    }

    public interface ShareCallback {
        void onShareCompleted(boolean success);
    }

}
