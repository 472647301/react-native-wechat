# react-native-wechat
 *暂时还有部分方法没完善
 
## Getting started

`$ yarn add @byron-react-native/wechat`

### iOS

```javascript
// （1）配置URL Scheme
<>在XCode中，选择你的工程设置项，选中“TARGETS”一栏，在“info”标签栏的“URL type”
添加一条新的“URL scheme”，新的scheme = wxappid。如果您使用的是XCode3或者更低的版本，
则需要在plist文件中添加。</>

// （2）配置LSApplicationQueriesSchemes
<key>LSApplicationQueriesSchemes</key>
	<array>
    <string>weixin</string>
    <string>weixinULAPI</string>
  </array>

// （3）AppDelegate.mm 添加
#import "WXApiManager.h" 
#import "QQApiManager.h" // 如果有集成qq

// weixin qq
- (BOOL)application:(UIApplication *)app openURL:(NSURL *)url options:(NSDictionary<UIApplicationOpenURLOptionsKey,id> *)options
{
  // 如果有集成qq
  if ([TencentOAuth CanHandleOpenURL:url]) {
    return [TencentOAuth HandleOpenURL:url];
  }
  return [WXApi handleOpenURL:url delegate:[WXApiManager sharedManager]]; 
}

- (BOOL)application:(UIApplication *)application continueUserActivity:(NSUserActivity *)userActivity restorationHandler:(void(^)(NSArray<id<UIUserActivityRestoring>> * __nullable restorableObjects))restorationHandler
{
  // 如果有集成qq
  if ([TencentOAuth CanHandleUniversalLink:userActivity.webpageURL]) {
    return [TencentOAuth HandleUniversalLink:userActivity.webpageURL];
  }
  return [WXApi handleOpenUniversalLink:userActivity delegate:[WXApiManager sharedManager]];
}


```

### Android
```javascript
// app/build.gradle add
implementation "com.tencent.mm.opensdk:wechat-sdk-android:+"

// Create WXEntryActivity
import android.app.Activity;
import android.os.Bundle;

import com.byronwechat.WechatModule;

public class WXEntryActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WechatModule.handleIntent(getIntent());
        finish();
    }
}

// AndroidManifest.xml add
<activity
    android:name=".wxapi.WXEntryActivity"
    android:label="@string/app_name"
    android:theme="@android:style/Theme.Translucent.NoTitleBar"
    android:exported="true"
    android:taskAffinity="your package name"
    android:launchMode="singleTask">
</activity>

```

### Api
```javascript
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
): Promise<WXSendAuthResp | undefined>;
/**
 * 发送Text消息给微信
 * @param text 文本长度必须大于0且小于10K
 * @param scene 请求发送场景
 */
static sendText(
  text: string,
  scene: WXScene
): Promise<WXBaseResp | undefined>;
/**
 * 发送Photo消息给微信
 */
static sendImage(params: WXImageParams): Promise<WXBaseResp | undefined>;
/**
 * 发送Link消息给微信
 */
static sendLinkURL(
  params: WXLinkURLParams
): Promise<WXBaseResp | undefined>;
/**
 * 发送Music消息给微
 */
static sendMusicURL(
  params: WXMusicURLParams
): Promise<WXBaseResp | undefined>;
/**
 * 发送Video消息给微信
 */
static sendVideoURL(
  params: WXVideoURLParams
): Promise<WXBaseResp | undefined>;
/**
 * 订阅消息
 */
static subscription(
  scene: string,
  templateId: string,
  reserved: string,
  cb: (data: WXSubscribeMsgResp) => void
): Promise<{ remove: () => void } | undefined>;
/**
 * 跳转到微信客服会话
 */
static openCustomerService(
  corpId: string,
  url: string
): Promise<(WXBaseResp & { extMsg: string }) | undefined>;
static pay(params: Partial<WXPayParams>): Promise<boolean>;
static addListener(
  cb: (data: {
    extInfo: string;
    title: string;
    description: string;
  }) => void
): { remove: () => void };
```