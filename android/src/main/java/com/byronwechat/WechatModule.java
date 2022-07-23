// WechatModule.java

package com.byronwechat;

import android.content.Intent;
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
import com.tencent.mm.opensdk.modelbiz.SubscribeMessage;
import com.tencent.mm.opensdk.modelbiz.WXOpenCustomerServiceChat;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXMusicObject;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXVideoObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class WechatModule extends ReactContextBaseJavaModule implements IWXAPIEventHandler {

    private IWXAPI api;
    private final ReactApplicationContext reactContext;
    // 缩略图大小 kb
    private final static int THUMB_SIZE = 32;

    public DeviceEventManagerModule.RCTDeviceEventEmitter eventEmitter;

    public WechatModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @NonNull
    @Override
    public String getName() {
        return "Wechat";
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

    private static final ArrayList<WechatModule> modules = new ArrayList<>();

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
        for (WechatModule mod : modules) {
            mod.api.handleIntent(intent, mod);
        }
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
        api = WXAPIFactory.createWXAPI(reactContext, appid, false);
        eventEmitter = reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
        promise.resolve(api.registerApp(appid));
    }

    @ReactMethod
    public void isWXAppInstalled(Promise promise) {
        if (api == null) {
            promise.resolve(false);
        }
        promise.resolve(api.isWXAppInstalled());
    }

    @ReactMethod
    public void isWXAppSupportApi(Promise promise) {
        if (api == null) {
            promise.resolve(false);
        }
        promise.resolve(api.getWXAppSupportAPI());
    }

    @ReactMethod
    public void getWXAppInstallUrl(Promise promise) {
        promise.resolve("");
    }

    @ReactMethod
    public void getApiVersion(Promise promise) {
        if (api == null) {
            promise.resolve(false);
        }
        promise.resolve(api.getWXAppSupportAPI());
    }

    @ReactMethod
    public void openWXApp(Promise promise) {
        if (api == null) {
            promise.resolve(false);
        }
        promise.resolve(api.openWXApp());
    }

    @ReactMethod
    public void sendAuthReq(String scope, String state, String openID, Promise promise) {
        if (api == null) {
            promise.resolve(false);
        }
        final SendAuth.Req req = new SendAuth.Req();
        req.scope = scope;
        req.state = state;
        req.openId = openID;
        promise.resolve(api.sendReq(req));
    }

    @ReactMethod
    public void pay(ReadableMap data, Promise promise) {
        if (api == null) {
            promise.resolve(false);
        }
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
        if (data.hasKey("appId")) {
            payReq.appId = data.getString("appId");
        }
        promise.resolve(api.sendReq(payReq));
    }

    @ReactMethod
    public void sendText(String text, int scene, Promise promise) {
        if (api == null) {
            promise.resolve(false);
        }
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
        promise.resolve(api.sendReq(req));
    }

    @ReactMethod
    public void sendImage(String filePath, String tagName, String messageExt, String action, String thumbPath, int scene, Promise promise) {
        if (api == null) {
            promise.resolve(false);
        }
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
                promise.resolve(api.sendReq(req));
            }
        });
    }

    @ReactMethod
    public void sendLinkURL(String urlString, String tagName, String title, String description, String thumbPath, int scene, Promise promise) {
        if (api == null) {
            promise.resolve(false);
        }
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
                promise.resolve(api.sendReq(req));
            }
        });
    }

    @ReactMethod
    public void sendMusicURL(String musicURL, String dataURL, String title, String description, String thumbPath, int scene, Promise promise) {
        if (api == null) {
            promise.resolve(false);
        }
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
                promise.resolve(api.sendReq(req));
            }
        });
    }

    @ReactMethod
    public void sendVideoURL(String videoURL, String title, String description, String thumbPath, int scene, Promise promise) {
        if (api == null) {
            promise.resolve(false);
        }
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
                promise.resolve(api.sendReq(req));
            }
        });
    }

    @ReactMethod
    public void subscription(String text, String templateId, String reserved, Promise promise) {
        if (api == null) {
            promise.resolve(false);
        }
        SubscribeMessage.Req req = new SubscribeMessage.Req();
        req.scene = parseInt(text, 0);
        req.templateID = templateId;
        req.reserved = reserved;
        promise.resolve(api.sendReq(req));
    }

    @ReactMethod
    public void openCustomerService(String corpId, String url, Promise promise) {
        if (api == null) {
            promise.resolve(false);
        }
        WXOpenCustomerServiceChat.Req req = new WXOpenCustomerServiceChat.Req();
        req.corpId = corpId;
        req.url = url;
        promise.resolve(api.sendReq(req));
    }

    @Override
    public void onReq(BaseReq req) {

    }

    @Override
    public void onResp(BaseResp resp) {
        if (resp.getType() == ConstantsAPI.COMMAND_SUBSCRIBE_MESSAGE) {
            SubscribeMessage.Resp subscribeMsgResp = (SubscribeMessage.Resp) resp;
            WritableMap data = Arguments.createMap();
            data.putInt("errCode", subscribeMsgResp.errCode);
            data.putInt("type", subscribeMsgResp.getType());
            data.putString("errStr", subscribeMsgResp.errStr);
            data.putString("templateId", subscribeMsgResp.templateID);
            data.putInt("scene", subscribeMsgResp.scene);
            data.putString("action", subscribeMsgResp.action);
            data.putString("reserved", subscribeMsgResp.reserved);
            data.putString("openId", subscribeMsgResp.openId);
            eventEmitter.emit("WXSubscribeMsgResp", data);
        } else if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
            SendAuth.Resp authResp = (SendAuth.Resp)resp;
            WritableMap data = Arguments.createMap();
            data.putString("code", authResp.code);
            data.putString("state", authResp.state);
            data.putInt("errCode", authResp.errCode);
            data.putInt("type", authResp.getType());
            data.putString("errStr", authResp.errStr);
            data.putString("lang", authResp.lang);
            data.putString("country", authResp.country);
            eventEmitter.emit("SendAuthResp", data);
        } else if (resp.getType() == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {
            SendMessageToWX.Resp item = (SendMessageToWX.Resp)resp;
            WritableMap data = Arguments.createMap();
            data.putInt("errCode", item.errCode);
            data.putInt("type", item.getType());
            data.putString("errStr", item.errStr);
            eventEmitter.emit("SendMessageToWXResp", data);
        } else if (resp.getType() == ConstantsAPI.COMMAND_OPEN_CUSTOMER_SERVICE_CHAT) {
            WXOpenCustomerServiceChat.Resp  item = (WXOpenCustomerServiceChat.Resp)resp;
            WritableMap data = Arguments.createMap();
            data.putInt("errCode", item.errCode);
            data.putInt("type", item.getType());
            data.putString("errStr", item.errStr);
            data.putString("extMsg", "");
            eventEmitter.emit("WXOpenCustomerServiceResp", data);
        }
    }

    private interface ImageCallback {
        void invoke(@Nullable Bitmap bitmap);
    }
}
