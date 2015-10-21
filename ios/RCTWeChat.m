//
//  RCTWeChat.m
//  RCTWeChat
//
//  Created by Yorkie Liu on 10/16/15.
//  Copyright © 2015 WeFlex. All rights reserved.
//

#import "Base/RCTLog.h"
#import "RCTWeChat.h"
#import "WXApi.h"
#import "WXApiObject.h"

@implementation RCTWeChat

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(registerApp
                  :(NSString *)appid
                  :(RCTResponseSenderBlock)callback)
{
    [WXApi registerApp:appid];
    callback(@[[NSNull null]]);
}

RCT_EXPORT_METHOD(registerAppWithDescription
                  :(NSString *)appid
                  :(NSString *)appdesc
                  :(RCTResponseSenderBlock)callback)
{
    [WXApi registerApp:appid withDescription:appdesc];
    callback(@[[NSNull null]]);
}

RCT_EXPORT_METHOD(isWXAppInstalled:(RCTResponseSenderBlock)callback)
{
    callback(@[@([WXApi isWXAppInstalled])]);
}

RCT_EXPORT_METHOD(isWXAppSupportApi:(RCTResponseSenderBlock)callback)
{
    callback(@[@([WXApi isWXAppSupportApi])]);
}

RCT_EXPORT_METHOD(getWXAppInstallUrl:(RCTResponseSenderBlock)callback)
{
    callback(@[[NSNull null], [WXApi getWXAppInstallUrl]]);
}

RCT_EXPORT_METHOD(getApiVersion:(RCTResponseSenderBlock)callback)
{
    callback(@[[NSNull null], [WXApi getApiVersion]]);
}

RCT_EXPORT_METHOD(openWXApp:(RCTResponseSenderBlock)callback)
{
    callback(@[@([WXApi openWXApp])]);
}

RCT_EXPORT_METHOD(sendRequest
                  :(NSString *)openid
                  :(RCTResponseSenderBlock)callback)
{
    BaseReq* req = [[BaseReq alloc] init];
    req.openID = openid;
    [WXApi sendReq:req];
    callback(@[[NSNull null]]);
}

RCT_EXPORT_METHOD(sendAuthRequest
                  :(NSString *)state
                  :(RCTResponseSenderBlock)callback)
{
    SendAuthReq* req = [[SendAuthReq alloc] init];
    req.scope = @"snsapi_userinfo";
    req.state = state;
    [WXApi sendReq:req];
    callback(@[[NSNull null]]);
}

RCT_EXPORT_METHOD(sendSuccessResponse
                  :(RCTResponseSenderBlock)callback)
{
    BaseResp* resp = [[BaseResp alloc] init];
    resp.errCode = WXSuccess;
    [WXApi sendResp:resp];
    callback(@[[NSNull null]]);
}

RCT_EXPORT_METHOD(sendErrorCommonResponse
                  :(NSString *)message
                  :(RCTResponseSenderBlock)callback)
{
    BaseResp* resp = [[BaseResp alloc] init];
    resp.errCode = WXErrCodeCommon;
    resp.errStr = message;
    [WXApi sendResp:resp];
    callback(@[[NSNull null]]);
}

RCT_EXPORT_METHOD(sendErrorUserCancelResponse
                  :(NSString *)message
                  :(RCTResponseSenderBlock)callback)
{
    BaseResp* resp = [[BaseResp alloc] init];
    resp.errCode = WXErrCodeUserCancel;
    resp.errStr = message;
    [WXApi sendResp:resp];
    callback(@[[NSNull null]]);
}

@end
