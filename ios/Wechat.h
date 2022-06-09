// Wechat.h

#import "WXApiObject.h"
#import "WXApiManager.h"
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface Wechat : RCTEventEmitter <RCTBridgeModule, WXApiManagerDelegate>

@end
