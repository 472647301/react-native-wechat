// main index.js

import { NativeModules, NativeEventEmitter, Platform } from "react-native";

const { Wechat } = NativeModules;
const emitter = new NativeEventEmitter(Platform.OS === "ios" ? Wechat : null);

export default class WxSdk {
  static subs = {};

  static registerApp = (appid, universalLink) => {
    return Wechat.registerApp(appid, universalLink);
  };
  static isWXAppInstalled = () => {
    return Wechat.isWXAppInstalled();
  };
  static isWXAppSupportApi = () => {
    return Wechat.isWXAppSupportApi();
  };
  static getWXAppInstallUrl = () => {
    return Wechat.getWXAppInstallUrl();
  };
  static getApiVersion = () => {
    return Wechat.getApiVersion();
  };
  static openWXApp = () => {
    return Wechat.openWXApp();
  };
  static sendAuth = async (scope, state, openID) => {
    const event = "SendAuthResp";
    const res = await Wechat.sendAuthReq(scope, state, openID);
    if (!res) return void 0
    return new Promise((resolve) => {
      this.subs[event] = emitter.addListener(event, (data) => {
        resolve(data);
      });
    }).then((data) => {
      this.subs[event].remove();
      return data;
    });
  };
  static sendText = async (text, scene) => {
    const event = "SendMessageToWXResp";
    const res = await Wechat.sendText(text, scene);
    if (!res) return void 0
    return new Promise((resolve) => {
      this.subs[event] = emitter.addListener(event, (data) => {
        resolve(data);
      });
    }).then((data) => {
      this.subs[event].remove();
      return data;
    });
  };
  static sendImage = async (params) => {
    const event = "SendMessageToWXResp";
    const { filePath, tagName, messageExt, action, thumbPath, scene } = params;
    const res = await Wechat.sendImage(filePath, tagName, messageExt, action, thumbPath, scene);
    if (!res) return void 0
    return new Promise((resolve) => {
      this.subs[event] = emitter.addListener(event, (data) => {
        resolve(data);
      });
    }).then((data) => {
      this.subs[event].remove();
      return data;
    });
  };
  static sendLinkURL = async (params) => {
    const event = "SendMessageToWXResp";
    const { url, tagName, title, description, thumbPath, scene } = params;
    const res = await Wechat.sendLinkURL(url, tagName, title, description, thumbPath, scene);
    if (!res) return void 0
    return new Promise((resolve) => {
      this.subs[event] = emitter.addListener(event, (data) => {
        resolve(data);
      });
    }).then((data) => {
      this.subs[event].remove();
      return data;
    });
  };
  static sendMusicURL = async (params) => {
    const event = "SendMessageToWXResp";
    const { url, dataURL, title, description, thumbPath, scene } = params;
    const res = await Wechat.sendMusicURL(url, dataURL, title, description, thumbPath, scene);
    if (!res) return void 0
    return new Promise((resolve) => {
      this.subs[event] = emitter.addListener(event, (data) => {
        resolve(data);
      });
    }).then((data) => {
      this.subs[event].remove();
      return data;
    });
  };
  static sendVideoURL = async (params) => {
    const event = "SendMessageToWXResp";
    const { url, title, description, thumbPath, scene } = params;
    const res = await Wechat.sendVideoURL(url, title, description, thumbPath, scene);
    if (!res) return void 0
    return new Promise((resolve) => {
      this.subs[event] = emitter.addListener(event, (data) => {
        resolve(data);
      });
    }).then((data) => {
      this.subs[event].remove();
      return data;
    });
  };
  static subscription = (scene, templateId, reserved, cb) => {
    const res = await Wechat.subscription(scene, templateId, reserved);
    if (!res) return void 0
    this.subs["WXSubscribeMsgResp"] = emitter.addListener(
      "WXSubscribeMsgResp",
      cb
    );
    return this.subs["WXSubscribeMsgResp"];
  };
  static openCustomerService = async (corpId, url) => {
    const event = "WXOpenCustomerServiceResp";
    const res = await Wechat.openCustomerService(corpId, url);
    if (!res) return void 0
    return new Promise((resolve) => {
      this.subs[event] = emitter.addListener(event, (data) => {
        resolve(data);
      });
    }).then((data) => {
      this.subs[event].remove();
      return data;
    });
  };
  static pay = async (params) => {
    if (Platform.OS !== 'android') {
      return
    }
    return Wechat.pay(params);
  };
  static addListener = (cb) => {
    this.subs["ShowMessageFromWXReq"] = emitter.addListener(
      "ShowMessageFromWXReq",
      cb
    );
    return this.subs["ShowMessageFromWXReq"];
  };
}

export const WXScene = {
  WXSceneSession: 0,
  WXSceneTimeline: 1,
  WXSceneFavorite: 2,
  WXSceneSpecifiedSession: 3,
  WXSceneState: 4,
};
