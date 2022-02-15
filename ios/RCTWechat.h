//  Created by shihuimiao on 2021/01/22.
//  Copyright © 2021 shihuimiao. All rights reserved.

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#import <React/RCTBridgeModule.h>
#import "WXApi.h"

#define RCTLaunchFromWXEvent @"LaunchFromWX.Req"
#define RCTWXSendMessageEvent @"SendMessageToWX.Resp"
#define RCTWXSendAuthEvent @"SendAuth.Resp"
#define RCTWXLaunchMiniprogramEvent @"LaunchMiniprogram.Resp"
#define RCTWXPayEvent @"PayReq.Resp"
#define RCTWXOpenBusinessWebViewEvent @"WXOpenBusinessWebview.Resp"

@interface RCTWechat : NSObject <RCTBridgeModule, WXApiDelegate>

@property (nonatomic, copy) NSString *appId;

// handle AppDelegate openURL
+ (BOOL)handleOpenURL:(NSURL *)url;

+ (BOOL)handleOpenUniversalLink:(NSUserActivity *)userActivity;


@end
