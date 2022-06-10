declare module "@byron-react-native/wechat" {
  /*
   * @brief 请求发送场景
   */
  export enum WXScene {
    /**
     * 聊天界面
     */
    WXSceneSession = 0,
    /**
     * 朋友圈
     */
    WXSceneTimeline = 1,
    /**
     * 收藏
     */
    WXSceneFavorite = 2,
    /**
     * 指定联系人
     */
    WXSceneSpecifiedSession = 3,
    /**
     * 状态
     */
    WXSceneState = 4,
  }
  export interface WXBaseResp {
    errCode: number;
    errStr: string;
    type: number;
  }
  export interface WXSendAuthResp extends WXBaseResp {
    code: string;
    state: string;
    lang: string;
    country: string;
  }
  export interface WXImageParams {
    filePath: string;
    tagName: string;
    messageExt: string;
    action: string;
    thumbPath: string;
    scene: WXScene;
  }
  export interface WXLinkURLParams {
    url: string;
    tagName: string;
    title: string;
    description: string;
    thumbPath: string;
    scene: WXScene;
  }
  export interface WXMusicURLParams {
    url: string;
    dataURL: string;
    title: string;
    description: string;
    thumbPath: string;
    scene: WXScene;
  }
  export interface WXVideoURLParams {
    url: string;
    title: string;
    description: string;
    thumbPath: string;
    scene: WXScene;
  }
  export interface WXSubscribeMsgResp extends WXBaseResp {
    templateId: string;
    scene: WXScene;
    action: string;
    reserved: string;
    openId: string;
  }
  class WXSdk {
    /**
     * @brief WXApi的成员函数，向微信终端程序注册第三方应用
     * @attention 需要在每次启动第三方应用程序时调用
     * @param appid 微信开发者ID
     * @param universalLink 微信开发者Universal Link
     * @returns 成功返回YES，失败返回NO
     */
    static registerApp(appid: string, universalLink: string): Promise<boolean>;
    /**
     * @brief 检查微信是否已被用户安装
     * @returns 微信已安装返回YES，未安装返回NO
     */
    static isWXAppInstalled(): Promise<boolean>;
    /**
     * @brief 判断当前微信的版本是否支持OpenApi
     * @returns 支持返回YES，不支持返回NO
     */
    static isWXAppSupportApi(): Promise<boolean>;
    /**
     * @brief 获取微信的itunes安装地址
     * @returns 微信的安装地址字符串
     */
    static getWXAppInstallUrl(): Promise<string>;
    /**
     * @brief 获取当前微信SDK的版本号
     * @returns 返回当前微信SDK的版本号
     */
    static getApiVersion(): Promise<string>;
    /**
     * @brief 打开微信
     * @returns 成功返回YES，失败返回NO。
     */
    static openWXApp(): Promise<boolean>;
    /**
     * 第三方程序要向微信申请认证
     * @param scope 字符串长度不能超过1K
     * @param state 字符串长度不能超过1K
     * @param openID 字符串长度不能超过1K
     */
    static sendAuth(
      scope: string,
      state: string,
      openID: string
    ): Promise<WXSendAuthResp>;
    /**
     * 发送Text消息给微信
     * @param text 文本长度必须大于0且小于10K
     * @param scene 请求发送场景
     */
    static sendText(text: string, scene: WXScene): Promise<WXBaseResp>;
    /**
     * 发送Photo消息给微信
     */
    static sendImage(params: WXImageParams): Promise<WXBaseResp>;
    /**
     * 发送Link消息给微信
     */
    static sendLinkURL(params: WXLinkURLParams): Promise<WXBaseResp>;
    /**
     * 发送Music消息给微
     */
    static sendMusicURL(params: WXMusicURLParams): Promise<WXBaseResp>;
    /**
     * 发送Video消息给微信
     */
    static sendVideoURL(params: WXVideoURLParams): Promise<WXBaseResp>;
    /**
     * 订阅消息
     */
    static subscription(
      scene: string,
      templateId: string,
      reserved: string,
      cb: (data: WXSubscribeMsgResp) => void
    ): { remove: () => void };
    /**
     * 跳转到微信客服会话
     */
    static openCustomerService(
      corpId: string,
      url: string
    ): Promise<WXBaseResp & { extMsg: string }>;
    static addListener(
      cb: (data: {
        extInfo: string;
        title: string;
        description: string;
      }) => void
    ): { remove: () => void };
  }
  export default WXSdk;
}
