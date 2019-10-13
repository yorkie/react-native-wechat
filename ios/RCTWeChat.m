/**
* @Author: little-snow-fox
* @Date: 2019-10-10
*/

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
    if (self) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleOpenURL:) name:@"RCTOpenURLNotification" object:nil];
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
    } else {
        return NO;
    }
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

// 获取网络图片的公共方法
- (UIImage *) getImageFromURL:(NSString *)fileURL {
    UIImage * result;
    NSData * data = [NSData dataWithContentsOfURL:[NSURL URLWithString:fileURL]];
    result = [UIImage imageWithData:data];
    return result;
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
                                 (NSUInteger)(resultImage.size.height * sqrtf(ratio)));
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

RCT_EXPORT_METHOD(registerApp:(NSString *)appid
                  :(NSString *)universalLink
                  :(RCTResponseSenderBlock)callback)
{
    self.appId = appid;
    callback(@[[WXApi registerApp:appid universalLink:universalLink] ? [NSNull null] : INVOKE_FAILED]);
}

// RCT_EXPORT_METHOD(registerAppWithDescription:(NSString *)appid
//                   :(NSString *)appdesc
//                   :(RCTResponseSenderBlock)callback)
// {
//     callback(@[[WXApi registerApp:appid withDescription:appdesc] ? [NSNull null] : INVOKE_FAILED]);
// }

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

// 分享文本
RCT_EXPORT_METHOD(shareText:(NSDictionary *)data
                  :(RCTResponseSenderBlock)callback)
{
    SendMessageToWXReq *req = [[SendMessageToWXReq alloc] init];
    req.bText = YES;
    req.text = data[@"text"];
    req.scene = data[@"scene"] || WXSceneSession;
    void ( ^ completion )( BOOL );
    completion = ^( BOOL success )
    {
        callback(@[success ? [NSNull null] : INVOKE_FAILED]);
        return;
    };
    [WXApi sendReq:req completion:completion];
}

// 分享图片
RCT_EXPORT_METHOD(shareImage:(NSDictionary *)data
                  :(RCTResponseSenderBlock)callback)
{
    NSString *imageUrl = data[@"imageUrl"];
    if (imageUrl == NULL  || [imageUrl isEqual:@""]) {
        callback([NSArray arrayWithObject:@"shareImage: The value of ImageUrl cannot be empty."]);
        return;
    }
    NSRange range = [imageUrl rangeOfString:@"."];
    if ( range.length == 0)
    {
        callback([NSArray arrayWithObject:@"shareImage: ImageUrl value, Could not find file suffix."]);
        return;
    }
    
    // 根据路径下载图片
    UIImage *image = [self getImageFromURL:imageUrl];
    // 从 UIImage 获取图片数据
    NSData *imageData = UIImageJPEGRepresentation(image, 1);
    // 用图片数据构建 WXImageObject 对象
    WXImageObject *imageObject = [WXImageObject object];
    imageObject.imageData = imageData;
    
    WXMediaMessage *message = [WXMediaMessage message];
    // 利用原图压缩出缩略图，确保缩略图大小不大于32KB
    message.thumbData = [self compressImage: image toByte:32678];
    message.mediaObject = imageObject;
    message.title = data[@"title"];
    message.description = data[@"description"];

    SendMessageToWXReq *req = [[SendMessageToWXReq alloc] init];
    req.bText = NO;
    req.message = message;
    req.scene = data[@"scene"] || WXSceneSession;
    void ( ^ completion )( BOOL );
    completion = ^( BOOL success )
    {
        callback(@[success ? [NSNull null] : INVOKE_FAILED]);
        return;
    };
    [WXApi sendReq:req completion:completion];
}

// 分享音乐
RCT_EXPORT_METHOD(shareMusic:(NSDictionary *)data
                  :(RCTResponseSenderBlock)callback)
{
    WXMusicObject *musicObject = [WXMusicObject object];
    musicObject.musicUrl = data[@"musicUrl"];
    musicObject.musicLowBandUrl = data[@"musicLowBandUrl"];
    musicObject.musicDataUrl = data[@"musicDataUrl"];
    musicObject.musicLowBandDataUrl = data[@"musicLowBandDataUrl"];

    WXMediaMessage *message = [WXMediaMessage message];
    message.title = data[@"title"];
    message.description = data[@"description"];
    NSString *thumbImageUrl = data[@"thumbImageUrl"];
    if (thumbImageUrl != NULL && ![thumbImageUrl isEqual:@""]) {
        // 根据路径下载图片
        UIImage *image = [self getImageFromURL:thumbImageUrl];
        message.thumbData = [self compressImage: image toByte:32678];
    }
    message.mediaObject = musicObject;
    SendMessageToWXReq *req = [[SendMessageToWXReq alloc] init];
    req.bText = NO;
    req.message = message;
    req.scene = data[@"scene"] || WXSceneSession;
    void ( ^ completion )( BOOL );
    completion = ^( BOOL success )
    {
        callback(@[success ? [NSNull null] : INVOKE_FAILED]);
        return;
    };
    [WXApi sendReq:req completion:completion];
}

// 分享视频
RCT_EXPORT_METHOD(shareVideo:(NSDictionary *)data
                  :(RCTResponseSenderBlock)callback)
{
    WXVideoObject *videoObject = [WXVideoObject object];
    videoObject.videoUrl = data[@"videoUrl"];
    videoObject.videoLowBandUrl = data[@"videoLowBandUrl"];
    WXMediaMessage *message = [WXMediaMessage message];
    message.title = data[@"title"];
    message.description = data[@"description"];
    NSString *thumbImageUrl = data[@"thumbImageUrl"];
    if (thumbImageUrl != NULL && ![thumbImageUrl isEqual:@""]) {
        UIImage *image = [self getImageFromURL:thumbImageUrl];
        message.thumbData = [self compressImage: image toByte:32678];
    }
    message.mediaObject = videoObject;
    SendMessageToWXReq *req = [[SendMessageToWXReq alloc] init];
    req.bText = NO;
    req.message = message;
    req.scene = data[@"scene"] || WXSceneSession;
    void ( ^ completion )( BOOL );
    completion = ^( BOOL success )
    {
        callback(@[success ? [NSNull null] : INVOKE_FAILED]);
        return;
    };
    [WXApi sendReq:req completion:completion];
}

// 分享网页
RCT_EXPORT_METHOD(shareWebpage:(NSDictionary *)data
                  :(RCTResponseSenderBlock)callback)
{
    WXWebpageObject *webpageObject = [WXWebpageObject object];
    webpageObject.webpageUrl = data[@"webpageUrl"];
    WXMediaMessage *message = [WXMediaMessage message];
    message.title = data[@"title"];
    message.description = data[@"description"];
    NSString *thumbImageUrl = data[@"thumbImageUrl"];
    if (thumbImageUrl != NULL && ![thumbImageUrl isEqual:@""]) {
        UIImage *image = [self getImageFromURL:thumbImageUrl];
        message.thumbData = [self compressImage: image toByte:32678];
    }
    message.mediaObject = webpageObject;
    SendMessageToWXReq *req = [[SendMessageToWXReq alloc] init];
    req.bText = NO;
    req.message = message;
    req.scene = data[@"scene"] || WXSceneSession;
    void ( ^ completion )( BOOL );
    completion = ^( BOOL success )
    {
        callback(@[success ? [NSNull null] : INVOKE_FAILED]);
        return;
    };
    [WXApi sendReq:req completion:completion];
}

// 分享小程序
RCT_EXPORT_METHOD(shareMiniProgram:(NSDictionary *)data
                  :(RCTResponseSenderBlock)callback)
{
    WXMiniProgramObject *object = [WXMiniProgramObject object];
    object.webpageUrl = data[@"webpageUrl"];
    object.userName = data[@"userName"];
    object.path = data[@"path"];
    NSString *hdImageUrl = data[@"hdImageUrl"];
    if (hdImageUrl != NULL && ![hdImageUrl isEqual:@""]) {
        UIImage *image = [self getImageFromURL:hdImageUrl];
        // 压缩图片到小于128KB
        object.hdImageData = [self compressImage: image toByte:131072];
    }
    object.withShareTicket = data[@"withShareTicket"];
    object.miniProgramType = data[@"miniProgramType"] || WXMiniProgramTypeRelease;
    WXMediaMessage *message = [WXMediaMessage message];
    message.title = data[@"title"];
    message.description = data[@"description"];
    // 兼容旧版本节点的图片，小于32KB，新版本优先
    // 使用WXMiniProgramObject的hdImageData属性
    NSString *thumbImageUrl = data[@"thumbImageUrl"];
    if (thumbImageUrl != NULL && ![thumbImageUrl isEqual:@""]) {
        UIImage *image = [self getImageFromURL:thumbImageUrl];
        message.thumbData = [self compressImage: image toByte:32678];
    }
    message.mediaObject = object;
    SendMessageToWXReq *req = [[SendMessageToWXReq alloc] init];
    req.bText = NO;
    req.message = message;
    req.scene = data[@"scene"] || WXSceneSession;
    void ( ^ completion )( BOOL );
    completion = ^( BOOL success )
    {
        callback(@[success ? [NSNull null] : INVOKE_FAILED]);
        return;
    };
    [WXApi sendReq:req completion:completion];
}

RCT_EXPORT_METHOD(launchMiniProgram:(NSDictionary *)data
                  :(RCTResponseSenderBlock)callback)
{
    WXLaunchMiniProgramReq *launchMiniProgramReq = [WXLaunchMiniProgramReq object];
    // 拉起的小程序的username
    launchMiniProgramReq.userName = data[@"userName"];
    // 拉起小程序页面的可带参路径，不填默认拉起小程序首页
    launchMiniProgramReq.path = data[@"path"];
    // 拉起小程序的类型
    launchMiniProgramReq.miniProgramType = [data[@"miniProgramType"] integerValue];
    void ( ^ completion )( BOOL );
    completion = ^( BOOL success )
    {
        callback(@[success ? [NSNull null] : INVOKE_FAILED]);
        return;
    };
    [WXApi sendReq:launchMiniProgramReq completion:completion];
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

-(void) onReq:(BaseReq*)req
{
    // TODO(Yorkie)
}

-(void) onResp:(BaseResp*)resp
{
    if([resp isKindOfClass:[SendMessageToWXResp class]])
    {
        SendMessageToWXResp *r = (SendMessageToWXResp *)resp;
    
        NSMutableDictionary *body = @{@"errCode":@(r.errCode)}.mutableCopy;
        body[@"errStr"] = r.errStr;
        body[@"lang"] = r.lang;
        body[@"country"] =r.country;
        body[@"type"] = @"SendMessageToWX.Resp";
        [self.bridge.eventDispatcher sendDeviceEventWithName:RCTWXEventName body:body];
    } else if ([resp isKindOfClass:[SendAuthResp class]]) {
        SendAuthResp *r = (SendAuthResp *)resp;
        NSMutableDictionary *body = @{@"errCode":@(r.errCode)}.mutableCopy;
        body[@"errStr"] = r.errStr;
        body[@"state"] = r.state;
        body[@"lang"] = r.lang;
        body[@"country"] =r.country;
        body[@"type"] = @"SendAuth.Resp";
    
        if (resp.errCode == WXSuccess) {
            if (self.appId && r) {
            // ios第一次获取不到appid会卡死，加个判断OK
            [body addEntriesFromDictionary:@{@"appid":self.appId, @"code":r.code}];
            [self.bridge.eventDispatcher sendDeviceEventWithName:RCTWXEventName body:body];
            }
        }
        else {
            [self.bridge.eventDispatcher sendDeviceEventWithName:RCTWXEventName body:body];
        }
    } else if ([resp isKindOfClass:[PayResp class]]) {
        PayResp *r = (PayResp *)resp;
        NSMutableDictionary *body = @{@"errCode":@(r.errCode)}.mutableCopy;
        body[@"errStr"] = r.errStr;
        body[@"type"] = @(r.type);
        body[@"returnKey"] =r.returnKey;
        body[@"type"] = @"PayReq.Resp";
        [self.bridge.eventDispatcher sendDeviceEventWithName:RCTWXEventName body:body];
    } else if ([resp isKindOfClass:[WXLaunchMiniProgramResp class]]){
        WXLaunchMiniProgramResp *r = (WXLaunchMiniProgramResp *)resp;
        NSMutableDictionary *body = @{@"errCode":@(r.errCode)}.mutableCopy;
        body[@"errStr"] = r.errStr;
        body[@"extMsg"] = r.extMsg;
        body[@"type"] = @"WXLaunchMiniProgramReq.Resp";
        [self.bridge.eventDispatcher sendDeviceEventWithName:RCTWXEventName body:body];
    }
}

@end
