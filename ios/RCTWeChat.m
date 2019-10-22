//
//  RCTWeChat.m
//  RCTWeChat
//
//  Created by Yorkie Liu on 10/16/15.
//  Copyright © 2015 WeFlex. All rights reserved.
//

#import "RCTWeChat.h"
#import "WXApiObject.h"
#import <React/RCTEventDispatcher.h>
#import <React/RCTBridge.h>
#import <React/RCTLog.h>
#import <React/RCTImageLoader.h>

// Define error messages
#define NOT_REGISTERED (@"registerApp required.")
#define INVOKE_FAILED (@"WeChat API invoke returns false.")

@implementation RCTWeChat

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE()

- (instancetype)init
{
    self = [super init];
    if (self)
    {
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(handleOpenURL:)
                                                     name:@"RCTOpenURLNotification"
                                                   object:nil];
    }
    return self;
}

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (BOOL)handleOpenURL:(NSNotification *)aNotification
{
    NSString * aURLString =  [aNotification userInfo][@"url"];
    NSURL * aURL = [NSURL URLWithString:aURLString];

    if ([WXApi handleOpenURL:aURL delegate:self])
    {
        return YES;
    }
    else
    {
        return NO;
    }
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

- (UIImage *)getImageFromUrl:(NSString *)url {
    NSData * data = [NSData dataWithContentsOfURL:[NSURL URLWithString:url]];
    return [UIImage imageWithData:data];
}

- (NSData *)compressImage:(UIImage *)image toByte:(NSUInteger)maxLength {
    CGFloat compression = 1;
    NSData *data = UIImageJPEGRepresentation(image, compression);
    if (data.length < maxLength)
    {
        return data;
    }

    CGFloat max = 1;
    CGFloat min = 0;
    for (int i = 0; i < 6; ++i)
    {
        compression = (max + min) / 2;
        data = UIImageJPEGRepresentation(image, compression);
        if (data.length < maxLength * 0.9)
        {
            min = compression;
        }
        else if (data.length > maxLength)
        {
            max = compression;
        }
        else
        {
            break;
        }
    }
    UIImage *resultImage = [UIImage imageWithData:data];
    if (data.length < maxLength) return data;

    // Compress by size
    NSUInteger lastDataLength = 0;
    while (data.length > maxLength && data.length != lastDataLength)
    {
        lastDataLength = data.length;
        CGFloat ratio = (CGFloat)maxLength / data.length;
        CGSize size = CGSizeMake((NSUInteger)(resultImage.size.width * sqrtf(ratio)),
                                 (NSUInteger)(resultImage.size.height * sqrtf(ratio)));
        UIGraphicsBeginImageContext(size);
        [resultImage drawInRect:CGRectMake(0, 0, size.width, size.height)];
        resultImage = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        data = UIImageJPEGRepresentation(resultImage, compression);
    }

    if (data.length > maxLength)
    {
        return [self compressImage:resultImage toByte:maxLength];
    }
    
    return data;
}

RCT_EXPORT_METHOD(registerApp:(NSString *)appid
                  :(NSString *)universalLink
                  :(RCTResponseSenderBlock)callback)
{
    self.appId = appid;
    callback(@[[WXApi registerApp:appid universalLink:universalLink] ? [NSNull null] : INVOKE_FAILED]);
}

RCT_EXPORT_METHOD(isWXAppInstalled:(RCTResponseSenderBlock)callback)
{
    callback(@[[NSNull null], @([WXApi isWXAppInstalled])]);
}

RCT_EXPORT_METHOD(isWXAppSupportApi:(RCTResponseSenderBlock)callback)
{
    callback(@[[NSNull null], @([WXApi isWXAppSupportApi])]);
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
    callback(@[([WXApi openWXApp] ? [NSNull null] : INVOKE_FAILED)]);
}

RCT_EXPORT_METHOD(openMiniProgram:(NSDictionary *)data
                  :(RCTResponseSenderBlock)callback)
{
    WXLaunchMiniProgramReq *launchMiniProgramReq = [WXLaunchMiniProgramReq object];
    launchMiniProgramReq.userName = data[@"userName"];
    launchMiniProgramReq.path = data[@"path"];
    launchMiniProgramReq.miniProgramType = [data[@"miniProgramType"] integerValue];
    void ( ^ completion )( BOOL );
    completion = ^( BOOL success )
    {
        callback(@[success ? [NSNull null] : INVOKE_FAILED]);
        return;
    };
    [WXApi sendReq:launchMiniProgramReq completion:completion];
}

RCT_EXPORT_METHOD(sendRequest:(NSString *)openid
                  :(RCTResponseSenderBlock)callback)
{
    BaseReq* req = [[BaseReq alloc] init];
    req.openID = openid;
    // callback(@[[WXApi sendReq:req] ? [NSNull null] : INVOKE_FAILED]);
    void ( ^ completion )( BOOL );
    completion = ^( BOOL success )
    {
        callback(@[success ? [NSNull null] : INVOKE_FAILED]);
        return;
    };
    [WXApi sendReq:req completion:completion];
}

RCT_EXPORT_METHOD(sendAuthRequest:(NSString *)scope
                  :(NSString *)state
                  :(RCTResponseSenderBlock)callback)
{
    SendAuthReq* req = [[SendAuthReq alloc] init];
    req.scope = scope;
    req.state = state;
    void ( ^ completion )( BOOL );
    completion = ^( BOOL success )
    {
        callback(@[success ? [NSNull null] : INVOKE_FAILED]);
        return;
    };
    [WXApi sendReq:req completion:completion];
}

RCT_EXPORT_METHOD(sendSuccessResponse:(RCTResponseSenderBlock)callback)
{
    BaseResp* resp = [[BaseResp alloc] init];
    resp.errCode = WXSuccess;
    void ( ^ completion )( BOOL );
    completion = ^( BOOL success )
    {
        callback(@[success ? [NSNull null] : INVOKE_FAILED]);
        return;
    };
    [WXApi sendResp:resp completion:completion];
}

RCT_EXPORT_METHOD(sendErrorCommonResponse:(NSString *)message
                  :(RCTResponseSenderBlock)callback)
{
    BaseResp* resp = [[BaseResp alloc] init];
    resp.errCode = WXErrCodeCommon;
    resp.errStr = message;
    void ( ^ completion )( BOOL );
    completion = ^( BOOL success )
    {
        callback(@[success ? [NSNull null] : INVOKE_FAILED]);
        return;
    };
    [WXApi sendResp:resp completion:completion];
}

RCT_EXPORT_METHOD(sendErrorUserCancelResponse:(NSString *)message
                  :(RCTResponseSenderBlock)callback)
{
    BaseResp* resp = [[BaseResp alloc] init];
    resp.errCode = WXErrCodeUserCancel;
    resp.errStr = message;
    void ( ^ completion )( BOOL );
    completion = ^( BOOL success )
    {
        callback(@[success ? [NSNull null] : INVOKE_FAILED]);
        return;
    };
    [WXApi sendResp:resp completion:completion];
}

- (BOOL)sendShareRequestWithMedia:(NSObject *)media (NSDictionary *)data (RCTResponseSenderBlock)callback
{
    NSString *thumbURL = data[@"thumbImageUrl"];
    NSData *thumb = NULL;
    if (thumbURL != NULL)
    {
        thumb = [self compressImage:thumb toByte:32678];
    }
    return [self sendShareRequestInternal:NO
                                     text:NULL
                                    media:media
                                    thumb:thumb
                                     data:data
                                 callback:callback];
}

- (BOOL)sendShareRequestWithText:(NSString *)text (RCTResponseSenderBlock)callback
{
    return [self sendShareRequestInternal:YES
                                     text:text
                                    media:NULL
                                    thumb:NULL
                                     data:data
                                 callback:callback];
}

- (BOOL)sendShareRequestInternal:(BOOL)bText
                                :(NSString *)text
                                :(NSObject *)media
                                :(NSData *)thumb
                                :(NSDictionary *)data
                                :(RCTResponseSenderBlock)callback
{
    SendMessageToWXReq *req = [[SendMessageToWXReq alloc] init];
    req.bText = bText;
    req.scene = data[@"scene"] || WXSceneSession;

    if (req.bText == YES)
    {
        req.text = text || @"";
    }
    else
    {
        WXMediaMessage *message = [WXMediaMessage message];
        if (media != NULL)
        {
            message.mediaObject = media;
        }
        if (thumb != NULL)
        {
            message.thumbData = thumb;
        }
        if (data[@"title"] != NULL)
        {
            message.title = data[@"title"];
        }
        if (data[@"description"] != NULL)
        {
            message.description = data[@"description"];
        }
        if (data[@"mediaTagName"] != NULL)
        {
            message.mediaTagName = data[@"mediaTagName"];
        }
        if (data[@"messageAction"] != NULL)
        {
            message.messageAction = data[@"messageAction"];
        }
        if (data[@"messageExt"] != NULL)
        {
            message.messageExt = data[@"messageExt"];
        }
        req.message = message;
    }

    void ( ^ completion )( BOOL );
    completion = ^( BOOL success )
    {
        callback(@[success ? [NSNull null] : INVOKE_FAILED]);
        return;
    };
    [WXApi sendReq:req completion:completion];
}

RCT_EXPORT_METHOD(shareText:(NSDictionary *)data
                  :(RCTResponseSenderBlock)callback)
{
    [self sendShareRequestWithText:data[@"text"] callback:callback];
}

RCT_EXPORT_METHOD(shareImage:(NSDictionary *)data
                  :(RCTResponseSenderBlock)callback)
{
    NSString *imageUrl = data[@"imageUrl"];
    if (imageUrl == NULL  || [imageUrl isEqual:@""]) {
        callback([NSArray arrayWithObject:@"shareImage: The value of ImageUrl cannot be empty."]);
        return;
    }
    NSRange range = [imageUrl rangeOfString:@"."];
    if (range.length == 0)
    {
        callback([NSArray arrayWithObject:@"shareImage: ImageUrl value, Could not find file suffix."]);
        return;
    }
    WXImageObject *media = [WXImageObject object];
    UIImage *image = [self getImageFromUrl:imageUrl];
    media.imageData = UIImageJPEGRepresentation(image, 1);

    NSData *thumb = [self compressImage:image toByte:32678];
    [self sendShareRequestInternal:NO
                              text:NULL
                             media:media
                             thumb:thumb
                              data:data
                          callback:callback];
}

RCT_EXPORT_METHOD(shareMusic:(NSDictionary *)data
                  :(RCTResponseSenderBlock)callback)
{
    WXMusicObject *media = [WXMusicObject object];
    media.musicUrl = data[@"musicUrl"];
    media.musicLowBandUrl = data[@"musicLowBandUrl"];
    media.musicDataUrl = data[@"musicDataUrl"];
    media.musicLowBandDataUrl = data[@"musicLowBandDataUrl"];
    [self sendShareRequestWithMedia:media data:data callback:callback];
}

RCT_EXPORT_METHOD(shareVideo:(NSDictionary *)data
                  :(RCTResponseSenderBlock)callback)
{
    WXVideoObject *media = [WXVideoObject object];
    media.videoUrl = data[@"videoUrl"];
    media.videoLowBandUrl = data[@"videoLowBandUrl"];
    [self sendShareRequestWithMedia:media data:data callback:callback];
}

RCT_EXPORT_METHOD(shareWebpage:(NSDictionary *)data
                  :(RCTResponseSenderBlock)callback)
{
    WXWebpageObject *media = [WXWebpageObject object];
    media.webpageUrl = data[@"webpageUrl"];
    [self sendShareRequestWithMedia:media data:data callback:callback];
}

RCT_EXPORT_METHOD(shareFile:(NSDictionary *)data
                  :(RCTResponseSenderBlock)callback)
{
    WXFileObject *media = [WXFileObject object];
    // TODO
    [self sendShareRequestWithMedia:media data:data callback:callback];
}

RCT_EXPORT_METHOD(shareMiniProgram:(NSDictionary *)data
                  :(RCTResponseSenderBlock)callback)
{
    WXMiniProgramObject *media = [WXMiniProgramObject object];
    media.webpageUrl = data[@"webpageUrl"];
    media.miniProgramType = data[@"miniProgramType"] || WXMiniProgramTypeRelease;
    media.userName = data[@"userName"];
    media.path = data[@"path"];
    media.withShareTicket = data[@"withShareTicket"];
    [self sendShareRequestWithMedia:media data:data callback:callback];
}

RCT_EXPORT_METHOD(pay:(NSDictionary *)data
                  :(RCTResponseSenderBlock)callback)
{
    PayReq* req             = [PayReq new];
    req.partnerId           = data[@"partnerId"];
    req.prepayId            = data[@"prepayId"];
    req.nonceStr            = data[@"nonceStr"];
    req.timeStamp           = [data[@"timeStamp"] unsignedIntValue];
    req.package             = data[@"package"];
    req.sign                = data[@"sign"];
    void ( ^ completion )( BOOL );
    completion = ^( BOOL success )
    {
        callback(@[success ? [NSNull null] : INVOKE_FAILED]);
        return;
    };
    [WXApi sendReq:req completion:completion];
}

#pragma mark - wx callback

- (void)onReq:(BaseReq*)req
{
    // TODO(Yorkie)
}

- (void)onResp:(BaseResp*)resp
{
    if ([resp isKindOfClass:[SendMessageToWXResp class]])
    {
        SendMessageToWXResp *r = (SendMessageToWXResp *)resp;
    
        NSMutableDictionary *body = @{@"errCode":@(r.errCode)}.mutableCopy;
        body[@"type"] = RCTWXSendMessageEvent;
        body[@"errStr"] = r.errStr;
        body[@"lang"] = r.lang;
        body[@"country"] =r.country;
        [self.bridge.eventDispatcher sendDeviceEventWithName:RCTWXEventName body:body];
    }
    else if ([resp isKindOfClass:[SendAuthResp class]])
    {
        SendAuthResp *r = (SendAuthResp *)resp;
        NSMutableDictionary *body = @{@"errCode":@(r.errCode)}.mutableCopy;
        body[@"type"] = RCTWXSendAuthEvent;
        body[@"errStr"] = r.errStr;
        body[@"state"] = r.state;
        body[@"lang"] = r.lang;
        body[@"country"] =r.country;
    
        if (resp.errCode == WXSuccess)
        {
            if (self.appId && r)
            {
                [body addEntriesFromDictionary:@{@"appid":self.appId, @"code":r.code}];
                [self.bridge.eventDispatcher sendDeviceEventWithName:RCTWXEventName body:body];
            }
        }
        else
        {
            [self.bridge.eventDispatcher sendDeviceEventWithName:RCTWXEventName body:body];
        }
    }
    else if ([resp isKindOfClass:[WXLaunchMiniProgramResp class]])
    {
        WXLaunchMiniProgramResp *r = (WXLaunchMiniProgramResp *)resp;
        NSMutableDictionary *body = @{@"errCode":@(r.errCode)}.mutableCopy;
        body[@"type"] = RCTWXLaunchMiniProgramEvent;
        body[@"data"] = r.extMsg;
        [self.bridge.eventDispatcher sendDeviceEventWithName:RCTWXEventName body:body];
    }
    else if ([resp isKindOfClass:[PayResp class]])
    {
        PayResp *r = (PayResp *)resp;
        NSMutableDictionary *body = @{@"errCode":@(r.errCode)}.mutableCopy;
        body[@"type"] = RCTWXPayEvent;
        body[@"errStr"] = r.errStr;
        body[@"type"] = @(r.type);
        body[@"returnKey"] =r.returnKey;
        [self.bridge.eventDispatcher sendDeviceEventWithName:RCTWXEventName body:body];
    }
}

@end
