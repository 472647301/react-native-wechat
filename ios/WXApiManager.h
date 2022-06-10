//
//  WXApiManager.h
//  react-native-wechat
//
//  Created by 朱文波 on 2022/6/7.
//

#import <Foundation/Foundation.h>
#import "WXApi.h"

@protocol WXApiManagerDelegate <NSObject>

@optional

- (void)managerDidRecvShowMessageReq:(ShowMessageFromWXReq *)request;

- (void)managerDidRecvMessageResponse:(SendMessageToWXResp *)response;

- (void)managerDidRecvAuthResponse:(SendAuthResp *)response;

- (void)managerDidRecvSubscribeMsgResponse:(WXSubscribeMsgResp *)response;

- (void)managerDidRecvOpenCustomerService:(WXOpenCustomerServiceResp *)response;

@end

@interface WXApiManager : NSObject<WXApiDelegate>

@property (nonatomic, assign) id<WXApiManagerDelegate> delegate;

+ (instancetype)sharedManager;

@end

