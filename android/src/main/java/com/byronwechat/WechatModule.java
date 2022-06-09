// WechatModule.java

package com.byronwechat;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.tencent.mm.opensdk.modelbiz.SubscribeMessage;
import com.tencent.mm.opensdk.modelbiz.WXOpenCustomerServiceChat;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXAppExtendObject;
import com.tencent.mm.opensdk.modelmsg.WXEmojiObject;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXMusicObject;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXVideoObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.ByteArrayOutputStream;

public class WechatModule extends ReactContextBaseJavaModule {

    private IWXAPI api;
    private final ReactApplicationContext reactContext;
    // 缩略图大小 kb
    private final static int THUMB_SIZE = 32;

    public static DeviceEventManagerModule.RCTDeviceEventEmitter eventEmitter;
    public static String appId = "";

    public WechatModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @NonNull
    @Override
    public String getName() {
        return "Wechat";
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    public int parseInt(final String string, final int def) {
        try {
            return (string == null || string.length() <= 0) ? def : Integer.parseInt(string);

        } catch (Exception e) {
            return def;
        }
    }

    private byte[] bitmapTopBytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        bitmap.recycle();
        return baos.toByteArray();
    }

    private byte[] bitmapResizeGetBytes(Bitmap image, int size) {
        // little-snow-fox 2019.10.20
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 质量压缩方法，这里100表示第一次不压缩，把压缩后的数据缓存到 baos
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        // 循环判断压缩后依然大于 32kb 则继续压缩
        while (baos.toByteArray().length / 1024 > size) {
            // 重置baos即清空baos
            baos.reset();
            if (options > 10) {
                options -= 8;
            } else {
                return bitmapResizeGetBytes(Bitmap.createScaledBitmap(image, 280, image.getHeight() / image.getWidth() * 280, true), size);
            }
            // 这里压缩options%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
        }
        return baos.toByteArray();
    }

    private void getImage(Uri uri, final ImageCallback imageCallback) {
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
            protected void onFailureImpl(@NonNull DataSource<CloseableReference<CloseableImage>> dataSource) {
                imageCallback.invoke(null);
            }
        };

        ImageRequestBuilder builder = ImageRequestBuilder.newBuilderWithSource(uri);
        ImageRequest imageRequest = builder.build();

        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchDecodedImage(imageRequest, null);
        dataSource.subscribe(dataSubscriber, UiThreadImmediateExecutorService.getInstance());
    }

    @ReactMethod
    public void registerApp(String appid, String universalLink, Promise promise) {
        appId = appid;
        api = WXAPIFactory.createWXAPI(reactContext, appid, false);
        eventEmitter = reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
        promise.resolve(api.registerApp(appid));
    }

    @ReactMethod
    public void isWXAppInstalled(Promise promise) {
        promise.resolve(api.isWXAppInstalled());
    }

    @ReactMethod
    public void isWXAppSupportApi(Promise promise) {
        promise.resolve(api.getWXAppSupportAPI());
    }

    @ReactMethod
    public void getWXAppInstallUrl(Promise promise) {
        promise.resolve("");
    }

    @ReactMethod
    public void getApiVersion(Promise promise) {
        promise.resolve(api.getWXAppSupportAPI());
    }

    @ReactMethod
    public void openWXApp(Promise promise) {
        promise.resolve(api.openWXApp());
    }

    @ReactMethod
    public void sendAuthReq(String scope, String state, String openID) {
        final SendAuth.Req req = new SendAuth.Req();
        req.scope = scope;
        req.state = state;
        req.openId = openID;
        api.sendReq(req);
    }

    @ReactMethod
    public void sendText(String text, int scene) {
        WXTextObject textObj = new WXTextObject();
        textObj.text = text;

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        msg.description = text;
        msg.mediaTagName = "text";
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("text");
        req.message = msg;
        req.scene = scene;
        api.sendReq(req);
    }

    @ReactMethod
    public void sendImage(String filePath, String tagName, String messageExt, String action, String thumbPath, int scene) {
        getImage(Uri.parse(filePath), new ImageCallback() {
            @Override
            public void invoke(@Nullable Bitmap bitmap) {
                WXImageObject imgObj = new WXImageObject(bitmap);

                WXMediaMessage msg = new WXMediaMessage();
                msg.mediaObject = imgObj;
                if (bitmap != null) {
                    msg.thumbData = bitmapResizeGetBytes(bitmap, THUMB_SIZE);
                }
                msg.mediaTagName = tagName;
                msg.messageExt = messageExt;
                msg.messageAction = action;
                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = buildTransaction("img");
                req.message = msg;
                req.scene = scene;
                api.sendReq(req);
            }
        });
    }

    @ReactMethod
    public void sendLinkURL(String urlString, String tagName, String title, String description, String thumbPath, int scene) {
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = urlString;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        msg.description = description;
        msg.mediaTagName = tagName;
        getImage(Uri.parse(thumbPath), new ImageCallback() {
            @Override
            public void invoke(@Nullable Bitmap bitmap) {
                if (bitmap != null) {
                    msg.thumbData = bitmapResizeGetBytes(bitmap, THUMB_SIZE);
                }
                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = buildTransaction("webpage");
                req.message = msg;
                req.scene = scene;
                api.sendReq(req);
            }
        });
    }

    @ReactMethod
    public void sendMusicURL(String musicURL, String dataURL, String title, String description, String thumbPath, int scene) {
        WXMusicObject music = new WXMusicObject();
        music.musicUrl = musicURL;
        music.musicDataUrl = dataURL;
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = music;
        msg.title = title;
        msg.description = description;
        getImage(Uri.parse(thumbPath), new ImageCallback() {
            @Override
            public void invoke(@Nullable Bitmap bitmap) {
                if (bitmap != null) {
                    msg.thumbData = bitmapResizeGetBytes(bitmap, THUMB_SIZE);
                }
                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = buildTransaction("music");
                req.message = msg;
                req.scene = scene;
                api.sendReq(req);
            }
        });
    }

    @ReactMethod
    public void sendVideoURL(String videoURL, String title, String description, String thumbPath, int scene) {
        WXVideoObject video = new WXVideoObject();
        video.videoUrl = videoURL;

        WXMediaMessage msg = new WXMediaMessage(video);

        msg.title = title;
        msg.description = description;
        getImage(Uri.parse(thumbPath), new ImageCallback() {
            @Override
            public void invoke(@Nullable Bitmap bitmap) {
                if (bitmap != null) {
                    msg.thumbData = bitmapResizeGetBytes(bitmap, THUMB_SIZE);
                }
                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = buildTransaction("video");
                req.message = msg;
                req.scene = scene;
                api.sendReq(req);
            }
        });
    }

    @ReactMethod
    public void sendAppData(String info, String url, String title, String description, String messageExt, String action, String thumbPath, int scene) {
        final WXAppExtendObject appdata = new WXAppExtendObject();
        appdata.filePath = url;
        appdata.extInfo = info;

        final WXMediaMessage msg = new WXMediaMessage();

        msg.title = title;
        msg.description = description;
        msg.mediaObject = appdata;
        msg.messageExt = messageExt;
        msg.messageAction = action;
        getImage(Uri.parse(thumbPath), new ImageCallback() {
            @Override
            public void invoke(@Nullable Bitmap bitmap) {
                if (bitmap != null) {
                    msg.thumbData = bitmapResizeGetBytes(bitmap, THUMB_SIZE);
                }
                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = buildTransaction("appdata");
                req.message = msg;
                req.scene = scene;
                api.sendReq(req);
            }
        });
    }

    @ReactMethod
    public void sendEmotionData(String filePath, String thumbPath, int scene) {
        getImage(Uri.parse(filePath), new ImageCallback() {
            @Override
            public void invoke(@Nullable Bitmap bitmap) {
                if (bitmap != null) {
                    WXEmojiObject emoji = new WXEmojiObject();
                    emoji.emojiData = bitmapTopBytes(bitmap);
                    final WXMediaMessage msg = new WXMediaMessage();
                    msg.thumbData = bitmapResizeGetBytes(bitmap, THUMB_SIZE);
                    msg.mediaObject = emoji;
                    SendMessageToWX.Req req = new SendMessageToWX.Req();
                    req.transaction = buildTransaction("emoji");
                    req.message = msg;
                    req.scene = scene;
                    api.sendReq(req);
                }
            }
        });
    }

    @ReactMethod
    public void subscription(String text, String templateId, String reserved) {
        SubscribeMessage.Req req = new SubscribeMessage.Req();
        req.scene = parseInt(text, 0);
        req.templateID = templateId;
        req.reserved = reserved;
        api.sendReq(req);
    }

    @ReactMethod
    public void openCustomerService(String corpId, String url) {
        WXOpenCustomerServiceChat.Req req = new WXOpenCustomerServiceChat.Req();
        req.corpId = corpId;
        req.url = url;
        api.sendReq(req);
    }

    private interface ImageCallback {
        void invoke(@Nullable Bitmap bitmap);
    }

}
