//
//  RCTWechat.m
//  RCTWechat
//
//  Created by shihuimiao on 2021/01/22.
//  Copyright © 2021 shihuimiao. All rights reserved.
//

#import "RCTWechat.h"
#import "WXApiObject.h"
#import <React/RCTEventDispatcher.h>
#import <React/RCTBridge.h>
#import <React/RCTLog.h>
#import <React/RCTImageLoader.h>

// Define error messages
#define NOT_REGISTERED (@"registerApp required")
#define INVOKE_FAILED (@"Wechat API invoke returns false")

// pending LaunchFromWX event if wechat not register
static BOOL isWeChatRegister = NO;
static NSString *pendingWeChatDelegateURL = nil;

static inline NSString* getWXCommonErrorCode() {
    return [NSString stringWithFormat:@"%d", WXErrCodeCommon];
}

@implementation RCTWechat

@synthesize bridge = _bridge;
static RCTWechat *instanceManager = nil;

RCT_EXPORT_MODULE(Wechat)

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

// APPDelegate handleOpenURL通知比module初始化要早
+ (void)initialize {
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(listenerNotification:)
                                                 name:@"RCTOpenURLNotification"
                                               object:nil];
}

+ (BOOL)listenerNotification:(NSNotification *)aNotification {
    NSString *aURLString =  [aNotification userInfo][@"url"];
    if (!isWeChatRegister) {
        pendingWeChatDelegateURL = aURLString;
        return NO;
    }
    
    // 已经注册过wechat，delegate交给module的实例去
    NSURL *aURL = [NSURL URLWithString:aURLString];
    if ([WXApi handleOpenURL:aURL delegate:instanceManager])
    {
        return YES;
    }
    else
    {
        return NO;
    }
}

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (BOOL)handleOpenURL:(NSNotification *)aNotification
{
    NSString * aURLString =  [aNotification userInfo][@"url"];
    if (!isWeChatRegister) {
        pendingWeChatDelegateURL = aURLString;
        return NO;
    }
    NSURL *aURL = [NSURL URLWithString:aURLString];
    if ([WXApi handleOpenURL:aURL delegate:self])
    {
        return YES;
    }
    else
    {
        return NO;
    }
}

- (void)handleWechatDeepLink {
    if (pendingWeChatDelegateURL) {
        [WXApi handleOpenURL:[NSURL URLWithString:pendingWeChatDelegateURL] delegate:self];
        pendingWeChatDelegateURL = nil;
        return;
    }
}

- (void)createImageRequest:(NSString *)url
                  callback:(RCTImageLoaderCompletionBlock)callback
{
    if (!url) {
        NSError *error = [NSError errorWithDomain:@"com.wechat.share"
                                             code:-1
                                         userInfo:@{NSLocalizedDescriptionKey:@"The value of url cannot be empty"}];
        callback(error, nil);
        return;
    }
    NSURL *imageURL = [NSURL URLWithString:url];
    if (!imageURL) {
        NSError *error = [NSError errorWithDomain:@"com.wechat.share"
                                             code:-1
                                         userInfo:@{NSLocalizedDescriptionKey:@"imageURL is invalid"}];
        callback(error, nil);
        return;
    }
    NSURLRequest *imageRequest = [NSURLRequest requestWithURL:imageURL];
    [[self.bridge moduleForClass:[RCTImageLoader class]] loadImageWithURLRequest:imageRequest callback:callback];
}


/// 图片压缩
/// @param image 待压缩的图片
/// @param maxLength  压缩到多大(单位：byte)
- (NSData *)compressImage:(UIImage *)image toByte:(NSUInteger)maxLength
{
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


/// wechat sdk register
/// @param callback callback
RCT_EXPORT_METHOD(registerApp:(NSString *)appid
                  universalLink:(NSString *)universalLink
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    instanceManager = self;
    self.appId = appid;
    
    BOOL registerAPP = [WXApi registerApp:appid universalLink:universalLink];
    if (registerAPP) {
        isWeChatRegister = YES;
        resolve([NSNull null]);
    }
    else {
        reject(getWXCommonErrorCode(), INVOKE_FAILED, nil);
    }
    [self handleWechatDeepLink];
}

RCT_EXPORT_METHOD(isWXAppInstalled:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    resolve(@([WXApi isWXAppInstalled]));
}

RCT_EXPORT_METHOD(isWXAppSupportApi:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    resolve(@([WXApi isWXAppSupportApi]));
}

// only iOS
RCT_EXPORT_METHOD(getWXAppInstallUrl:(RCTResponseSenderBlock)callback)
{
    callback(@[[NSNull null], [WXApi getWXAppInstallUrl]]);
}

// only iOS
RCT_EXPORT_METHOD(getApiVersion:(RCTResponseSenderBlock)callback)
{
    callback(@[[NSNull null], [WXApi getApiVersion]]);
}

RCT_EXPORT_METHOD(openWXApp:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    BOOL open = [WXApi openWXApp];
    if (open) {
        resolve([NSNull null]);
    }
    else {
        reject(getWXCommonErrorCode(), INVOKE_FAILED, nil);
    }
}

RCT_EXPORT_METHOD(openMiniprogram:(NSDictionary *)data
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    WXLaunchMiniProgramReq *launchMiniProgramReq = [WXLaunchMiniProgramReq object];
    launchMiniProgramReq.userName = data[@"userName"];
    launchMiniProgramReq.path = data[@"path"];
    if (![data objectForKey:@"miniprogramType"]) {
        launchMiniProgramReq.miniProgramType = WXMiniProgramTypeRelease;
    }
    else {
        launchMiniProgramReq.miniProgramType = [data[@"miniprogramType"] integerValue];
    }

    [WXApi sendReq:launchMiniProgramReq completion:^(BOOL success) {
        if (success) {
            resolve([NSNull null]);
        }
        else {
            reject(getWXCommonErrorCode(), INVOKE_FAILED, nil);
        }
    }];
}

RCT_EXPORT_METHOD(sendAuthRequest:(NSString *)scope
                  state:(NSString *)state
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    SendAuthReq *req = [[SendAuthReq alloc] init];
    req.scope = scope;
    req.state = state;

    [WXApi sendAuthReq:req
        viewController:RCTKeyWindow().rootViewController
              delegate:self
            completion:^(BOOL success) {
        if (success) {
            resolve([NSNull null]);
        }
        else {
            reject(getWXCommonErrorCode(), INVOKE_FAILED, nil);
        }
    }];
}


///  微信签约支付
/// @param callback  callback
RCT_EXPORT_METHOD(entrust:(NSDictionary *)data
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    NSString *preEntrustWebId = [data objectForKey:@"preEntrustWebId"];
    if (!preEntrustWebId) {
        reject(getWXCommonErrorCode(), @"entrust: The value of preEntrustWebId cannot be empty", nil);
        return;
    }
    WXOpenBusinessWebViewReq *req = [[WXOpenBusinessWebViewReq alloc] init];
    req.businessType = 12; //固定值
    NSMutableDictionary *queryInfoDic = [NSMutableDictionary dictionary];
    [queryInfoDic setObject:preEntrustWebId forKey:@"pre_entrustweb_id"];
    req.queryInfoDic = queryInfoDic;
    
    [WXApi sendReq:req completion:^(BOOL success) {
        if (success) {
            resolve([NSNull null]);
        }
        else {
            reject(getWXCommonErrorCode(), INVOKE_FAILED, nil);
        }
    }];
}

RCT_EXPORT_METHOD(shareText:(NSDictionary *)data
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    SendMessageToWXReq *req = [[SendMessageToWXReq alloc] init];
    req.bText = YES;
    req.text = [data objectForKey:@"text"];
    req.scene = [data objectForKey:@"scene"] ? [[data objectForKey:@"scene"] intValue] : WXSceneSession;
    [WXApi sendReq:req completion:^(BOOL success) {
        if (success) {
            resolve([NSNull null]);
        }
        else {
            reject(getWXCommonErrorCode(), INVOKE_FAILED, nil);
        }
    }];
}

RCT_EXPORT_METHOD(shareImage:(NSDictionary *)data
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    [self createImageRequest:[data objectForKey:@"imageUrl"] callback:^(NSError *err, UIImage *image) {
        if (err) {
            reject(getWXCommonErrorCode(), @"the imageUrl download failed", nil);
            return;
        }
        NSData *imageData = UIImageJPEGRepresentation(image, 1);
        WXImageObject *imageObject = [WXImageObject object];
        imageObject.imageData = imageData;

        WXMediaMessage *message = [WXMediaMessage message];
        message.mediaObject = imageObject;
        message.title = data[@"title"];
        message.description = data[@"description"];

        SendMessageToWXReq *req = [[SendMessageToWXReq alloc] init];
        req.bText = NO;
        req.message = message;
        req.scene = [data[@"scene"] intValue];
        [WXApi sendReq:req completion:^(BOOL success) {
            if (success) {
                resolve([NSNull null]);
            }
            else {
                reject(getWXCommonErrorCode(), INVOKE_FAILED, nil);
            }
        }];
    }];
}

RCT_EXPORT_METHOD(shareMusic:(NSDictionary *)data
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    [self createImageRequest:[data objectForKey:@"thumbImageUrl"] callback:^(NSError *err, UIImage *image) {
        if (err) {
            reject(getWXCommonErrorCode(), @"the imageUrl download failed", nil);
            return;
        }
        WXMusicObject *media = [WXMusicObject object];
        media.musicUrl = data[@"musicUrl"];
        media.musicLowBandUrl = data[@"musicLowBandUrl"];
        media.musicDataUrl = data[@"musicDataUrl"];
        media.musicLowBandDataUrl = data[@"musicLowBandDataUrl"];
        
        WXMediaMessage *message = [WXMediaMessage message];
        message.title = data[@"title"];
        message.description = data[@"description"];
        [message setThumbImage:image];
        message.mediaObject = media;
        
        SendMessageToWXReq *req = [[SendMessageToWXReq alloc] init];
        req.bText = NO;
        req.message = message;
        req.scene = data[@"scene"] ? [data[@"scene"] intValue] : WXSceneSession;
        [WXApi sendReq:req completion:^(BOOL success) {
            if (success) {
                resolve([NSNull null]);
            }
            else {
                reject(getWXCommonErrorCode(), INVOKE_FAILED, nil);
            }
        }];
    }];
}

RCT_EXPORT_METHOD(shareVideo:(NSDictionary *)data
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    [self createImageRequest:[data objectForKey:@"thumbImageUrl"] callback:^(NSError *err, UIImage *image) {
        WXVideoObject *media = [WXVideoObject object];
        media.videoUrl = data[@"videoUrl"];
        media.videoLowBandUrl = data[@"videoLowBandUrl"];
        
        WXMediaMessage *message = [WXMediaMessage message];
        message.title = data[@"title"];
        message.description = data[@"description"];
        [message setThumbImage:image];
        message.mediaObject = media;
        
        SendMessageToWXReq *req = [[SendMessageToWXReq alloc] init];
        req.bText = NO;
        req.message = message;
        req.scene = data[@"scene"] ? [data[@"scene"] intValue] : WXSceneSession;
        [WXApi sendReq:req completion:^(BOOL success) {
            if (success) {
                resolve([NSNull null]);
            }
            else {
                reject(getWXCommonErrorCode(), INVOKE_FAILED, nil);
            }
        }];
    }];
}

RCT_EXPORT_METHOD(shareWebpage:(NSDictionary *)data
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    NSString *imageURL = [data objectForKey:@"thumbImageUrl"];
    [self createImageRequest:imageURL callback:^(NSError *err, UIImage *image) {
        WXWebpageObject *webpageObject = [WXWebpageObject object];
        webpageObject.webpageUrl = [data objectForKey:@"webpageUrl"];
        WXMediaMessage *message = [WXMediaMessage message];
        message.title = [data objectForKey:@"title"];
        message.description = [data objectForKey:@"description"];
        [message setThumbImage:image];
        message.mediaObject = webpageObject;
        
        SendMessageToWXReq *req = [[SendMessageToWXReq alloc] init];
        req.bText = NO;
        req.message = message;
        req.scene = data[@"scene"] ? [data[@"scene"] intValue] : WXSceneSession;
        [WXApi sendReq:req completion:^(BOOL success) {
            if (success) {
                resolve([NSNull null]);
            }
            else {
                reject(getWXCommonErrorCode(), INVOKE_FAILED, nil);
            }
        }];
    }];
}

RCT_EXPORT_METHOD(shareFile:(NSDictionary *)data
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    WXFileObject *media = [WXFileObject object];
    media.fileData = [NSData dataWithContentsOfFile:data[@"filePath"]];
    media.fileExtension = data[@"fileExtension"];
    
    SendMessageToWXReq *req = [[SendMessageToWXReq alloc] init];
    req.bText = NO;
    req.message = media;
    req.scene = data[@"scene"] ? [data[@"scene"] intValue] : WXSceneSession;
    [WXApi sendReq:req completion:^(BOOL success) {
        if (success) {
            resolve([NSNull null]);
        }
        else {
            reject(getWXCommonErrorCode(), INVOKE_FAILED, nil);
        }
    }];
}

RCT_EXPORT_METHOD(shareMiniprogram:(NSDictionary *)data
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    NSString *hdImageUrl = data[@"thumbImageUrl"] ? data[@"thumbImageUrl"] : @"";
    [self createImageRequest:hdImageUrl callback:^(NSError *err, UIImage *image) {
        NSDictionary *miniprogram = [data objectForKey:@"miniprogram"];
        WXMiniProgramObject *object = [WXMiniProgramObject object];
        object.userName = [miniprogram objectForKey:@"userName"];
        object.path = [miniprogram objectForKey:@"path"];
        // 压缩图片到小于128KB
        object.hdImageData = image ? [self compressImage:image toByte:131072] : nil;
        object.miniProgramType = [miniprogram objectForKey:@"miniprogramType"] ? [[miniprogram objectForKey:@"miniprogramType"] integerValue] : WXMiniProgramTypeRelease;
        object.webpageUrl = [data objectForKey:@"webpageUrl"];

        WXMediaMessage *message = [WXMediaMessage message];
        message.title = data[@"title"];
        message.description = data[@"description"];
        message.mediaObject = object;

        SendMessageToWXReq *req = [[SendMessageToWXReq alloc] init];
        req.bText = NO;
        req.message = message;
        req.scene = data[@"scene"] ? [data[@"scene"] intValue] : WXSceneSession;

        [WXApi sendReq:req completion:^(BOOL success) {
            if (success) {
                resolve([NSNull null]);
            }
            else {
                reject(getWXCommonErrorCode(), INVOKE_FAILED, nil);
            }
        }];
    }];
}

RCT_EXPORT_METHOD(pay:(NSDictionary *)data
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    PayReq *req = [[PayReq alloc] init];
    req.partnerId = data[@"partnerId"];
    req.prepayId = data[@"prepayId"];
    req.nonceStr = data[@"nonceStr"];
    req.timeStamp = [data[@"timeStamp"] unsignedIntValue];
    req.package = data[@"package"];
    req.sign = data[@"sign"];

    [WXApi sendReq:req completion:^(BOOL success) {
        if (success) {
            resolve([NSNull null]);
        }
        else {
            reject(getWXCommonErrorCode(), INVOKE_FAILED, nil);
        }
    }];
}

#pragma mark - wx callback

- (void)onReq:(BaseReq*)req
{
    if ([req isKindOfClass:[LaunchFromWXReq class]]) {
        LaunchFromWXReq *launchReq = (LaunchFromWXReq *)req;
        NSString *appParameter = launchReq.message.messageExt;
        NSMutableDictionary *body = [[NSMutableDictionary alloc] init];
        body[@"type"] = @"LaunchFromWX.Req";
        body[@"lang"] =  launchReq.lang;
        body[@"country"] = launchReq.country;
        body[@"extInfo"] = appParameter;
        
        [self.bridge.eventDispatcher sendDeviceEventWithName:RCTLaunchFromWXEvent body:body];
    }
}

- (void)onResp:(BaseResp*)resp
{
    if ([resp isKindOfClass:[SendMessageToWXResp class]])
    {
        SendMessageToWXResp *r = (SendMessageToWXResp *)resp;
    
        NSMutableDictionary *body = @{@"errCode":@(r.errCode)}.mutableCopy;
        body[@"errStr"] = r.errStr;
        body[@"lang"] = r.lang;
        body[@"country"] = r.country;
        [self.bridge.eventDispatcher sendDeviceEventWithName:RCTWXSendMessageEvent body:body];
    }
    else if ([resp isKindOfClass:[SendAuthResp class]])
    {
        SendAuthResp *r = (SendAuthResp *)resp;
        NSMutableDictionary *body = @{@"errCode":@(r.errCode)}.mutableCopy;
        body[@"errStr"] = r.errStr;
        body[@"state"] = r.state;
        body[@"lang"] = r.lang;
        body[@"country"] = r.country;
        body[@"code"] = r.code;
    
        [self.bridge.eventDispatcher sendDeviceEventWithName:RCTWXSendAuthEvent body:body];
    }
    else if ([resp isKindOfClass:[WXLaunchMiniProgramResp class]])
    {
        WXLaunchMiniProgramResp *r = (WXLaunchMiniProgramResp *)resp;
        NSMutableDictionary *body = @{@"errCode":@(r.errCode)}.mutableCopy;
        body[@"data"] = r.extMsg;
        [self.bridge.eventDispatcher sendDeviceEventWithName:RCTWXLaunchMiniprogramEvent body:body];
    }
    else if ([resp isKindOfClass:[PayResp class]])
    {
        PayResp *r = (PayResp *)resp;
        NSMutableDictionary *body = @{@"errCode":@(r.errCode)}.mutableCopy;
        body[@"errStr"] = r.errStr;
        body[@"returnKey"] = r.returnKey;
        [self.bridge.eventDispatcher sendDeviceEventWithName:RCTWXPayEvent body:body];
    }
    else if ([resp isKindOfClass:[WXOpenBusinessWebViewResp class]]) {
        WXOpenBusinessWebViewResp *business = (WXOpenBusinessWebViewResp *)resp;
        NSMutableDictionary *body = @{@"errCode":@(business.errCode)}.mutableCopy;
        body[@"errStr"] = business.errStr;
        body[@"result"] = business.result;
        body[@"businessType"] = @(business.businessType);
        [self.bridge.eventDispatcher sendDeviceEventWithName:RCTWXOpenBusinessWebViewEvent body:body];
    }
}

@end
