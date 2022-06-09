package com.byronwechat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelbiz.SubscribeMessage;
import com.tencent.mm.opensdk.modelbiz.WXOpenCustomerServiceChat;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.ShowMessageFromWX;
import com.tencent.mm.opensdk.modelmsg.WXAppExtendObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class ByronWXEntryActivity extends Activity implements IWXAPIEventHandler {
    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Intent intent = getIntent();
            api = WXAPIFactory.createWXAPI(this, WechatModule.appId, false);
            api.handleIntent(intent, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
        if (req.getType() == ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX) {
            goToShowMsg((ShowMessageFromWX.Req) req);
        }
        finish();
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
            WechatModule.eventEmitter.emit("WXSubscribeMsgResp", data);
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
            WechatModule.eventEmitter.emit("SendAuthResp", data);
        } else if (resp.getType() == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {
            SendMessageToWX.Resp item = (SendMessageToWX.Resp)resp;
            WritableMap data = Arguments.createMap();
            data.putInt("errCode", item.errCode);
            data.putInt("type", item.getType());
            data.putString("errStr", item.errStr);
            WechatModule.eventEmitter.emit("SendMessageToWXResp", data);
        } else if (resp.getType() == ConstantsAPI.COMMAND_OPEN_CUSTOMER_SERVICE_CHAT) {
            WXOpenCustomerServiceChat.Resp  item = (WXOpenCustomerServiceChat.Resp)resp;
            WritableMap data = Arguments.createMap();
            data.putInt("errCode", item.errCode);
            data.putInt("type", item.getType());
            data.putString("errStr", item.errStr);
            data.putString("extMsg", "");
            WechatModule.eventEmitter.emit("WXOpenCustomerServiceResp", data);
        }
        finish();
    }

    private void goToShowMsg(ShowMessageFromWX.Req showReq) {
        WXMediaMessage wxMsg = showReq.message;
        WXAppExtendObject obj = (WXAppExtendObject) wxMsg.mediaObject;
        WritableMap data = Arguments.createMap();
        data.putString("extInfo", obj.extInfo);
        data.putString("filePath", obj.filePath);
        data.putString("title", wxMsg.title);
        data.putString("description", wxMsg.description);
        WechatModule.eventEmitter.emit("ShowMessageFromWXReq", data);
    }
}
