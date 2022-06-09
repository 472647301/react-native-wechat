// Wechat.m

#import "Wechat.h"
#import "WXApiRequestHandler.h"

#define BUFFER_SIZE 1024 * 100

@implementation Wechat

RCT_EXPORT_MODULE()

- (dispatch_queue_t)methodQueue {
  return dispatch_get_main_queue();
}

- (UIViewController*)visibleViewController:(UIViewController*)rootViewController {
    if (rootViewController.presentedViewController == nil) {
        return rootViewController;
    }
    if ([rootViewController.presentedViewController isKindOfClass:[UINavigationController class]]) {
        UINavigationController* navigationController = (UINavigationController*)rootViewController.presentedViewController;
        UIViewController* lastViewController = [[navigationController viewControllers] lastObject];

        return [self visibleViewController:lastViewController];
    }
    if ([rootViewController.presentedViewController isKindOfClass:[UITabBarController class]]) {
        UITabBarController* tabBarController = (UITabBarController*)rootViewController.presentedViewController;
        UIViewController* selectedViewController = tabBarController.selectedViewController;

        return [self visibleViewController:selectedViewController];
    }

    UIViewController* presentedViewController = (UIViewController*)rootViewController.presentedViewController;

    return [self visibleViewController:presentedViewController];
}

- (UIViewController*)getRootView {
    UIViewController* rootView =
        [self visibleViewController:[UIApplication sharedApplication].keyWindow.rootViewController];
    return rootView;
}

// 压缩图片
- (NSData *)compressImage:(UIImage *)image toByte:(NSUInteger)maxLength {
    // Compress by quality
    CGFloat compression = 1;
    NSData *data = UIImageJPEGRepresentation(image, compression);
    if (data.length < maxLength) return data;

    CGFloat max = 1;
    CGFloat min = 0;
    for (int i = 0; i < 6; ++i) {
        compression = (max + min) / 2;
        data = UIImageJPEGRepresentation(image, compression);
        if (data.length < maxLength * 0.9) {
            min = compression;
        } else if (data.length > maxLength) {
            max = compression;
        } else {
            break;
        }
    }
    UIImage *resultImage = [UIImage imageWithData:data];
    if (data.length < maxLength) return data;

    // Compress by size
    NSUInteger lastDataLength = 0;
    while (data.length > maxLength && data.length != lastDataLength) {
        lastDataLength = data.length;
        CGFloat ratio = (CGFloat)maxLength / data.length;
        CGSize size = CGSizeMake((NSUInteger)(resultImage.size.width * sqrtf(ratio)),
                                 (NSUInteger)(resultImage.size.height * sqrtf(ratio))); // Use NSUInteger to prevent white blank
        UIGraphicsBeginImageContext(size);
        [resultImage drawInRect:CGRectMake(0, 0, size.width, size.height)];
        resultImage = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        data = UIImageJPEGRepresentation(resultImage, compression);
    }

    if (data.length > maxLength) {
        return [self compressImage:resultImage toByte:maxLength];
    }
    
    return data;
}

// 向微信终端程序注册第三方应用
RCT_EXPORT_METHOD(registerApp:(NSString *)appid
                  universalLink:(NSString *)universalLink
                  resolve:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [WXApiManager sharedManager].delegate = self;
    #if DEBUG
    [WXApi startLogByLevel:WXLogLevelDetail logBlock:^(NSString *log) {
        NSLog(@"WXSdk Log : %@", log);
    }];
    #endif
    resolve([WXApi registerApp:appid universalLink:universalLink] ? @(YES) : @(NO));
}

// 检查微信是否已被用户安装
RCT_EXPORT_METHOD(isWXAppInstalled:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    resolve([WXApi isWXAppInstalled] ? @(YES) : @(NO));
}

// 判断当前微信的版本是否支持OpenApi
RCT_EXPORT_METHOD(isWXAppSupportApi:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    resolve([WXApi isWXAppSupportApi] ? @(YES) : @(NO));
}

// 获取微信的itunes安装地址
RCT_EXPORT_METHOD(getWXAppInstallUrl:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    resolve([WXApi getWXAppInstallUrl]);
}

// 获取当前微信SDK的版本号
RCT_EXPORT_METHOD(getApiVersion:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    resolve([WXApi getApiVersion]);
}

// 打开微信
RCT_EXPORT_METHOD(openWXApp:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    resolve([WXApi openWXApp] ? @(YES) : @(NO));
}

// 微信授权登录
RCT_EXPORT_METHOD(sendAuthReq:(NSString *)scope
                  State:(NSString *)state
                 OpenID:(NSString *)openID) {
    [WXApiRequestHandler sendAuthRequestScope: scope
                                        State:state
                                       OpenID:openID
                             InViewController:[self getRootView]];
}

// 发送Text消息给微信
RCT_EXPORT_METHOD(sendText:(NSString *)text
                  InScene:(int)scene) {
    [WXApiRequestHandler sendText:text InScene:scene];
}

// 发送Photo消息给微信
RCT_EXPORT_METHOD(sendImage:(NSString *)filePath
                  TagName:(NSString *)tagName
               MessageExt:(NSString *)messageExt
                   Action:(NSString *)action
               ThumbImage:(NSString *)thumbPath
                  InScene:(int)scene) {
    NSData *imageData = [NSData dataWithContentsOfURL:[NSURL URLWithString:filePath]];
    NSData *thumbData = [NSData dataWithContentsOfURL:[NSURL URLWithString:thumbPath]];
    
    NSData *thumbImage = [self compressImage: [UIImage imageWithData:thumbData] toByte:32678];
    [WXApiRequestHandler sendImageData:imageData
                               TagName:tagName
                            MessageExt:messageExt
                                Action:action
                            ThumbImage:[UIImage imageWithData:thumbImage]
                               InScene:scene];
}

// 发送Link消息给微信
RCT_EXPORT_METHOD(sendLinkURL:(NSString *)urlString
                  TagName:(NSString *)tagName
                    Title:(NSString *)title
              Description:(NSString *)description
               ThumbImage:(NSString *)thumbPath
                  InScene:(int)scene) {
    NSData *thumbData = [NSData dataWithContentsOfURL:[NSURL URLWithString:thumbPath]];
    
    NSData *thumbImage = [self compressImage: [UIImage imageWithData:thumbData] toByte:32678];
    [WXApiRequestHandler sendLinkURL:urlString
                             TagName:tagName
                               Title:title
                         Description:description
                          ThumbImage:[UIImage imageWithData:thumbImage]
                             InScene:scene];
}

// 发送Music消息给微
RCT_EXPORT_METHOD(sendMusicURL:(NSString *)musicURL
                  dataURL:(NSString *)dataURL
                    Title:(NSString *)title
              Description:(NSString *)description
               ThumbImage:(NSString *)thumbPath
                  InScene:(int)scene) {
    NSData *thumbData = [NSData dataWithContentsOfURL:[NSURL URLWithString:thumbPath]];
    
    NSData *thumbImage = [self compressImage: [UIImage imageWithData:thumbData] toByte:32678];
    [WXApiRequestHandler sendMusicURL:musicURL
                              dataURL:dataURL
                                Title:title
                          Description:description
                           ThumbImage:[UIImage imageWithData:thumbImage]
                              InScene:scene];
}

// 发送Video消息给微信
RCT_EXPORT_METHOD(sendVideoURL:(NSString *)videoURL
                  Title:(NSString *)title
            Description:(NSString *)description
             ThumbImage:(NSString *)thumbPath
                InScene:(int)scene) {
    NSData *thumbData = [NSData dataWithContentsOfURL:[NSURL URLWithString:thumbPath]];
    
    NSData *thumbImage = [self compressImage: [UIImage imageWithData:thumbData] toByte:32678];
    [WXApiRequestHandler sendVideoURL:videoURL
                                Title:title
                          Description:description
                           ThumbImage:[UIImage imageWithData:thumbImage]
                              InScene:scene];
}

// 发送App消息给微信
RCT_EXPORT_METHOD(sendAppData:(NSString *)info
                  ExtURL:(NSString *)url
                   Title:(NSString *)title
             Description:(NSString *)description
              MessageExt:(NSString *)messageExt
           MessageAction:(NSString *)action
              ThumbImage:(NSString *)thumbPath
                 InScene:(int)scene) {
    NSData *thumbData = [NSData dataWithContentsOfURL:[NSURL URLWithString:thumbPath]];
    
    NSData *thumbImage = [self compressImage: [UIImage imageWithData:thumbData] toByte:32678];
    Byte* pBuffer = (Byte *)malloc(BUFFER_SIZE);
    memset(pBuffer, 0, BUFFER_SIZE);
    NSData* data = [NSData dataWithBytes:pBuffer length:BUFFER_SIZE];
    free(pBuffer);
    [WXApiRequestHandler sendAppContentData:data
                                    ExtInfo:info
                                     ExtURL:url
                                      Title:title
                                Description:description
                                 MessageExt:messageExt
                              MessageAction:action
                                 ThumbImage:[UIImage imageWithData:thumbImage]
                                    InScene:scene];
}

// 发送表情给微信
RCT_EXPORT_METHOD(sendEmotionData:(NSString *)filePath
              ThumbImage:(NSString *)thumbPath
                 InScene:(int)scene) {
    NSData *emoticonData = [NSData dataWithContentsOfURL:[NSURL URLWithString:filePath]];
    NSData *thumbData = [NSData dataWithContentsOfURL:[NSURL URLWithString:thumbPath]];
    
    NSData *thumbImage = [self compressImage: [UIImage imageWithData:thumbData] toByte:32678];
    [WXApiRequestHandler sendEmotionData:emoticonData
                              ThumbImage:[UIImage imageWithData:thumbImage]
                                 InScene:scene];
}

// 发送文件消息给微信
RCT_EXPORT_METHOD(sendFileData:(NSString *)filePath
                  fileExtension:(NSString *)extension
                          Title:(NSString *)title
                    Description:(NSString *)description
                     ThumbImage:(NSString *)thumbPath
                        InScene:(int)scene) {
    NSData *fileData = [NSData dataWithContentsOfURL:[NSURL URLWithString:filePath]];
    NSData *thumbData = [NSData dataWithContentsOfURL:[NSURL URLWithString:thumbPath]];
    
    NSData *thumbImage = [self compressImage: [UIImage imageWithData:thumbData] toByte:32678];
    [WXApiRequestHandler sendFileData:fileData
                        fileExtension:extension
                                Title:title
                          Description:description
                           ThumbImage:[UIImage imageWithData:thumbImage]
                              InScene:scene];
}

// 添加卡券至卡包
RCT_EXPORT_METHOD(addCardsToCardPackage:(NSString *)appid
                  cardIds:(NSArray *)cardIds
                  cardExts:(NSArray *)cardExts) {
    [WXApiRequestHandler addCardsToCardPackage:appid cardIds:cardIds cardExts:cardExts];
}

// 选择卡券
RCT_EXPORT_METHOD(chooseCard:(NSString *)appid
                  cardSign:(NSString *)cardSign
                  nonceStr:(NSString *)nonceStr
                  signType:(NSString *)signType
                 timestamp:(UInt32)timestamp) {
    [WXApiRequestHandler chooseCard:appid cardSign:cardSign nonceStr:nonceStr signType:signType timestamp:timestamp];
}

// 选择发票
RCT_EXPORT_METHOD(chooseInvoice:(NSString *)appid
                  cardSign:(NSString *)cardSign
                  nonceStr:(NSString *)nonceStr
                  signType:(NSString *)signType
                 timestamp:(int)timestamp) {
    [WXApiRequestHandler chooseInvoice:appid cardSign:cardSign nonceStr:nonceStr signType:signType timestamp:timestamp];
}

// 小程序分享
RCT_EXPORT_METHOD(sendMiniProgramWebpageUrl:(NSString *)webpageUrl
                  userName:(NSString *)userName
                      path:(NSString *)path
                     title:(NSString *)title
               Description:(NSString *)description
                ThumbImage:(NSString *)thumbImagePath
               hdImageData:(NSString *)hdImagePath
           withShareTicket:(BOOL)withShareTicket
                   InScene:(int)scene) {
    NSData *hdImageData = [NSData dataWithContentsOfURL:[NSURL URLWithString:hdImagePath]];
    NSData *thumbData = [NSData dataWithContentsOfURL:[NSURL URLWithString:thumbImagePath]];
    
    NSData *thumbImage = [self compressImage: [UIImage imageWithData:thumbData] toByte:32678];
    [WXApiRequestHandler sendMiniProgramWebpageUrl:webpageUrl userName:userName path:path title:title Description:description ThumbImage:[UIImage imageWithData:thumbImage] hdImageData:hdImageData withShareTicket:withShareTicket InScene:scene];
}

// 订阅消息
RCT_EXPORT_METHOD(subscription:(NSString *)text
                  templateId:(NSString *)templateId
                  reserved:(NSString *)reserved) {
    UInt32 scene = (UInt32)[text integerValue];
    WXSubscribeMsgReq *req = [[WXSubscribeMsgReq alloc] init];
    req.scene = scene;
    req.templateId = templateId;
    req.reserved = reserved;
    
    [WXApi sendReq:req completion:nil];
}

// 拉起小程序
RCT_EXPORT_METHOD(launchMiniProgramWithUserName:(NSString *)userName
                  path:(NSString *)path) {
    [WXApiRequestHandler launchMiniProgramWithUserName:userName path:path];
}

// 跳转到微信客服会话
RCT_EXPORT_METHOD(openCustomerService:(NSString *)corpId
                  url:(NSString *)url) {
    WXOpenCustomerServiceReq *req = [[WXOpenCustomerServiceReq alloc] init];
    req.corpid = corpId;    //企业ID
    req.url = url;            //客服URL
    [WXApi sendReq:req completion:nil];
}

#pragma mark - WXApiManagerDelegate

- (void)managerDidRecvShowMessageReq:(ShowMessageFromWXReq *)req {
    WXMediaMessage *msg = req.message;
    [self sendEventWithName:@"WXMediaMessage" body:@{
        @"type": @(req.type),
        @"openID": req.openID,
        @"title": msg.title,
        @"description": msg.description,
        @"mediaTagName": msg.mediaTagName,
        @"messageExt": msg.messageExt,
        @"messageAction": msg.messageAction,
        /**
         * 多媒体数据对象，可以为WXImageObject，WXMusicObject，WXVideoObject，WXWebpageObject等。
         * 暂时不解析
         */
        @"mediaObject": @{
            
        }
    }];
}

- (void)managerDidRecvLaunchFromWXReq:(LaunchFromWXReq *)req {
    WXMediaMessage *msg = req.message;
    [self sendEventWithName:@"WXMediaMessage" body:@{
        @"type": @(req.type),
        @"openID": req.openID,
        @"title": msg.title,
        @"description": msg.description,
        @"mediaTagName": msg.mediaTagName,
        @"messageExt": msg.messageExt,
        @"messageAction": msg.messageAction,
        /**
         * 多媒体数据对象，可以为WXImageObject，WXMusicObject，WXVideoObject，WXWebpageObject等。
         * 暂时不解析
         */
        @"mediaObject": @{
            
        }
    }];
}

- (void)managerDidRecvMessageResponse:(SendMessageToWXResp *)response {
    [self sendEventWithName:@"SendMessageToWXResp" body:@{
        @"errCode": @(response.errCode),
        @"type": @(response.type),
        @"errStr": response.errStr ? response.errStr : @"",
    }];
}

- (void)managerDidRecvAddCardResponse:(AddCardToWXCardPackageResp *)response {
    [self sendEventWithName:@"AddCardToWXCardPackageResp" body:@{
        @"errCode": @(response.errCode),
        @"type": @(response.type),
        @"errStr": response.errStr ? response.errStr : @"",
        @"cardAry": response.cardAry
    }];
}

- (void)managerDidRecvChooseCardResponse:(WXChooseCardResp *)response {
    [self sendEventWithName:@"WXChooseCardResp" body:@{
        @"errCode": @(response.errCode),
        @"type": @(response.type),
        @"errStr": response.errStr ? response.errStr : @"",
        @"cardAry": response.cardAry
    }];
}

- (void)managerDidRecvChooseInvoiceResponse:(WXChooseInvoiceResp *)response {
    [self sendEventWithName:@"WXChooseInvoiceResp" body:@{
        @"errCode": @(response.errCode),
        @"type": @(response.type),
        @"errStr": response.errStr ? response.errStr : @"",
        @"cardAry": response.cardAry
    }];
}

- (void)managerDidRecvAuthResponse:(SendAuthResp *)response {
    [self sendEventWithName:@"SendAuthResp" body:@{
        @"code": response.code,
        @"state": response.state,
        @"errCode": @(response.errCode),
        @"type": @(response.type),
        @"errStr": response.errStr ? response.errStr : @"",
        @"lang": response.lang ? response.lang : @"",
        @"country": response.country ? response.country : @""
    }];
}

- (void)managerDidRecvSubscribeMsgResponse:(WXSubscribeMsgResp *)response
{
    [self sendEventWithName:@"WXSubscribeMsgResp" body: @{
        @"errCode": @(response.errCode),
        @"type": @(response.type),
        @"errStr": response.errStr ? response.errStr : @"",
        @"templateId": response.templateId,
        @"scene": @(response.scene),
        @"action": response.action,
        @"reserved": response.reserved,
        @"openId": response.openId
    }];
}

- (void)managerDidRecvLaunchMiniProgram:(WXLaunchMiniProgramResp *)response
{
    [self sendEventWithName:@"WXLaunchMiniProgramResp" body: @{
        @"errCode": @(response.errCode),
        @"type": @(response.type),
        @"errStr": response.errStr ? response.errStr : @"",
        @"extMsg": response.extMsg
    }];
}

- (void)managerDidRecvOpenCustomerService:(WXOpenCustomerServiceResp *)response
{
    [self sendEventWithName:@"WXOpenCustomerServiceResp" body: @{
        @"errCode": @(response.errCode),
        @"type": @(response.type),
        @"errStr": response.errStr ? response.errStr : @"",
        @"extMsg": response.extMsg
    }];
}

- (NSArray<NSString *> *)supportedEvents {
  return @[
      @"SendAuthResp",
      @"SendMessageToWXResp",
      @"AddCardToWXCardPackageResp",
      @"WXChooseCardResp",
      @"WXChooseInvoiceResp",
      @"WXMediaMessage",
      @"WXSubscribeMsgResp",
      @"WXLaunchMiniProgramResp",
      @"WXOpenCustomerServiceResp"
  ];
}

@end
