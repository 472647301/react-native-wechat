// main index.js

import { NativeModules, NativeEventEmitter } from "react-native";

const { Wechat } = NativeModules;
const emitter = new NativeEventEmitter(Wechat);

export default class WxSdk {
  static subs = {};

  static registerApp(appid, universalLink) {
    return Wechat.registerApp(appid, universalLink);
  }
  static isWXAppInstalled() {
    return Wechat.isWXAppInstalled();
  }
  static isWXAppSupportApi() {
    return Wechat.isWXAppSupportApi();
  }
  static getWXAppInstallUrl() {
    return Wechat.getWXAppInstallUrl();
  }
  static getApiVersion() {
    return Wechat.getApiVersion();
  }
  static openWXApp() {
    return Wechat.openWXApp();
  }
  static async sendAuth(scope, state, openID) {
    const event = "SendAuthResp";
    Wechat.sendAuthReq(scope, state, openID);
    return new Promise((resolve) => {
      this.subs[event] = emitter.addListener(event, (data) => {
        resolve(data);
      });
    }).then((data) => {
      this.subs[event].remove();
      return data;
    });
  }
  static async sendText(text, scene) {
    const event = "SendMessageToWXResp";
    Wechat.sendText(text, scene);
    return new Promise((resolve) => {
      this.subs[event] = emitter.addListener(event, (data) => {
        resolve(data);
      });
    }).then((data) => {
      this.subs[event].remove();
      return data;
    });
  }
  static async sendImage(params) {
    const event = "SendMessageToWXResp";
    const { filePath, tagName, messageExt, action, thumbPath, scene } = params;
    Wechat.sendImage(filePath, tagName, messageExt, action, thumbPath, scene);
    return new Promise((resolve) => {
      this.subs[event] = emitter.addListener(event, (data) => {
        resolve(data);
      });
    }).then((data) => {
      this.subs[event].remove();
      return data;
    });
  }
  static async sendLinkURL(params) {
    const event = "SendMessageToWXResp";
    const { url, tagName, title, description, thumbPath, scene } = params;
    Wechat.sendLinkURL(url, tagName, title, description, thumbPath, scene);
    return new Promise((resolve) => {
      this.subs[event] = emitter.addListener(event, (data) => {
        resolve(data);
      });
    }).then((data) => {
      this.subs[event].remove();
      return data;
    });
  }
  static async sendMusicURL(params) {
    const event = "SendMessageToWXResp";
    const { url, dataURL, title, description, thumbPath, scene } = params;
    Wechat.sendMusicURL(url, dataURL, title, description, thumbPath, scene);
    return new Promise((resolve) => {
      this.subs[event] = emitter.addListener(event, (data) => {
        resolve(data);
      });
    }).then((data) => {
      this.subs[event].remove();
      return data;
    });
  }
  static async sendVideoURL(params) {
    const event = "SendMessageToWXResp";
    const { url, title, description, thumbPath, scene } = params;
    Wechat.sendVideoURL(url, title, description, thumbPath, scene);
    return new Promise((resolve) => {
      this.subs[event] = emitter.addListener(event, (data) => {
        resolve(data);
      });
    }).then((data) => {
      this.subs[event].remove();
      return data;
    });
  }
  static async sendAppData(params) {
    const event = "SendMessageToWXResp";
    const {
      info,
      url,
      title,
      description,
      messageExt,
      action,
      thumbPath,
      scene,
    } = params;
    Wechat.sendAppData(
      info,
      url,
      title,
      description,
      messageExt,
      action,
      thumbPath,
      scene
    );
    return new Promise((resolve) => {
      this.subs[event] = emitter.addListener(event, (data) => {
        resolve(data);
      });
    }).then((data) => {
      this.subs[event].remove();
      return data;
    });
  }
  static async sendEmotionData(filePath, thumbPath, scene) {
    const event = "SendMessageToWXResp";
    Wechat.sendEmotionData(filePath, thumbPath, scene);
    return new Promise((resolve) => {
      this.subs[event] = emitter.addListener(event, (data) => {
        resolve(data);
      });
    }).then((data) => {
      this.subs[event].remove();
      return data;
    });
  }
  static subscription(scene, templateId, reserved, cb) {
    Wechat.subscription(scene, templateId, reserved);
    this.subs["WXSubscribeMsgResp"] = emitter.addListener(
      "WXSubscribeMsgResp",
      cb
    );
    return this.subs["WXSubscribeMsgResp"];
  }
  static async openCustomerService(corpId, url) {
    const event = "WXOpenCustomerServiceResp";
    Wechat.openCustomerService(corpId, url);
    return new Promise((resolve) => {
      this.subs[event] = emitter.addListener(event, (data) => {
        resolve(data);
      });
    }).then((data) => {
      this.subs[event].remove();
      return data;
    });
  }
  static addListener(cb) {
    this.subs["ShowMessageFromWXReq"] = emitter.addListener(
      "ShowMessageFromWXReq",
      cb
    );
    return this.subs["ShowMessageFromWXReq"]
  }
}

export const WXScene = {
  WXSceneSession: 0,
  WXSceneTimeline: 1,
  WXSceneFavorite: 2,
  WXSceneSpecifiedSession: 3,
  WXSceneState: 4,
};
