//
//  WXApiRequestHandler.h
//  react-native-wechat
//
//  Created by 朱文波 on 2022/6/7.
//

#import <Foundation/Foundation.h>
#import "WXApiObject.h"

@interface WXApiRequestHandler : NSObject

+ (void)sendText:(NSString *)text
         InScene:(enum WXScene)scene
         completion:(void (^ __nullable)(BOOL success))completion;

+ (void)sendImageData:(NSData *)imageData
              TagName:(NSString *)tagName
           MessageExt:(NSString *)messageExt
               Action:(NSString *)action
           ThumbImage:(UIImage *)thumbImage
              InScene:(enum WXScene)scene
           completion:(void (^ __nullable)(BOOL success))completion;

+ (void)sendLinkURL:(NSString *)urlString
            TagName:(NSString *)tagName
              Title:(NSString *)title
        Description:(NSString *)description
         ThumbImage:(UIImage *)thumbImage
            InScene:(enum WXScene)scene
         completion:(void (^ __nullable)(BOOL success))completion;

+ (void)sendMusicURL:(NSString *)musicURL
             dataURL:(NSString *)dataURL
               Title:(NSString *)title
         Description:(NSString *)description
          ThumbImage:(UIImage *)thumbImage
             InScene:(enum WXScene)scene
          completion:(void (^ __nullable)(BOOL success))completion;

+ (void)sendVideoURL:(NSString *)videoURL
               Title:(NSString *)title
         Description:(NSString *)description
          ThumbImage:(UIImage *)thumbImage
             InScene:(enum WXScene)scene
          completion:(void (^ __nullable)(BOOL success))completion;

+ (void)sendAuthRequestScope:(NSString *)scope
                       State:(NSString *)state
                      OpenID:(NSString *)openID
            InViewController:(UIViewController *)viewController
                  completion:(void (^ __nullable)(BOOL success))completion;

@end
