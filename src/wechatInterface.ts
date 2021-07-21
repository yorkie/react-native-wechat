/**
 * 微信小程序类型
 */
export enum WechatMiniprogramTypeEnum {
    // 正式版
    RELEASE = 0,
    // 测试版
    TEST = 1,
    // 体验版
    PREVIEW = 2,
}

/**
 * 分享场景
 */
export enum WechatSceneEnum {
    // 聊天界面
    SESSION = 0,
    // 朋友圈
    TIMELINE = 1,
    // 收藏
    FAVORITE = 2,
}

/**
 * 分享消息基础类型
 */
export interface WechatShareBase {
    // 标题
    title?: string;
    // 描述
    description?: string;
}

/**
 * 小程序分享类型
 */
export interface WechatShareMiniprogram extends WechatShareBase {
    // 兼容低版本的网页链接(长度不超过10KB)
    webpageUrl: string;
    // 小程序预览图url
    thumbImageUrl: string;
    miniprogram: WechatOpenMiniprogram;
}

/**
 * 打开小程序
 */
export interface WechatOpenMiniprogram {
    // 小程序userName
    userName: string;
    // 小程序页面路径
    path: string;
    // 小程序的类型
    miniprogramType?: WechatMiniprogramTypeEnum;
}

/**
 * 分享文本
 */
export interface WechatShareText extends WechatShareBase {
    text: string;
    // 发送场景
    scene: WechatSceneEnum;
}

/**
 * 分享图片
 */
export interface WechatShareImage extends WechatShareBase {
    // 图片url
    imageUrl: string;
    // 发送场景
    scene: WechatSceneEnum;
}

/**
 * 分享网页
 */
export interface WechatShareWebPage extends WechatShareBase {
    // 网页地址
    webpageUrl: string;
    // 图片地址
    thumbImageUrl: string;
    // 发送场景
    scene: WechatSceneEnum;
}

/**
 * 分享Video [videoUrl和videoLowBandUrl不能同时为空]
 */
export interface WechatShareVideo extends WechatShareBase {
    // 视频链接
    videoUrl?: string;
    // 供低带宽的环境下使用的视频链接
    videoLowBandUrl?: string;
    // 缩略图
    thumbImageUrl?: string;
}

/**
 * 分享Music [videoUrl和videoLowBandUrl不能同时为空]
 */
export interface WechatShareMusic extends WechatShareBase {
    // 音频网页的URL地址
    musicUrl?: string;
    // 供低带宽环境下使用的音频网页URL地址
    musicLowBandUrl?: string;
    // 音频数据的URL地址
    musicDataUrl?: string;
    // 供低带宽环境下使用的音频数据URL地址
    musicLowBandDataUrl?: string;
    // 缩略图
    thumbImageUrl?: string;
}

/**
 * 微信支付
 */
export interface WechatPay {
    // 商家向财付通申请的商家id
    // eslint-disable-next-line @typescript-eslint/naming-convention
    partnerId: string;
    // 预支付订单
    // eslint-disable-next-line @typescript-eslint/naming-convention
    prepayId: string;
    // 随机串，防重发
    nonceStr: string;
    // 时间戳，防重发
    timeStamp: string;
    // 商家根据财付通文档填写的数据和签名
    package: string;
    // 商家根据微信开放平台文档对数据做的签名
    sign: string;
}

/**
 * 微信签约
 */
export interface WechatEntrust {
    // eslint-disable-next-line @typescript-eslint/naming-convention
    preEntrustWebId: string;
}

/**
 * 错误码
 */
export enum WXErrCodeEnum {
    // eslint-disable-next-line @typescript-eslint/naming-convention
    WXSuccess = 0, // 成功
    // eslint-disable-next-line @typescript-eslint/naming-convention
    WXErrCodeCommon = -1, // 普通错误类型
    // eslint-disable-next-line @typescript-eslint/naming-convention
    WXErrCodeUserCancel = -2, // 用户点击取消并返回
    // eslint-disable-next-line @typescript-eslint/naming-convention
    WXErrCodeSentFail = -3, // 发送失败
    // eslint-disable-next-line @typescript-eslint/naming-convention
    WXErrCodeAuthDeny = -4, // 授权失败
    // eslint-disable-next-line @typescript-eslint/naming-convention
    WXErrCodeUnsupport = -5, // 微信不支持
    // eslint-disable-next-line @typescript-eslint/naming-convention
    WXErrBan = -6, // Android禁止
}

/**
 * promises will reject with this error when API call finish with an errCode other than zero.
 */
export class WechatError extends Error {
    public code: WXErrCodeEnum;

    constructor(code: WXErrCodeEnum, message: string) {
        super(message);
        this.name = 'WechatError';
        this.code = code;
    }
}

// 基础返回值
export interface BaseWXResp {
    errCode: WXErrCodeEnum;
    errStr: string;
}

// SendMessageToWXResp 返回值定义
export interface SendMessageToWXResp extends BaseWXResp {
    lang?: string;
    country?: string;
}

// SendAuthResp 返回值定义
export interface SendAuthResp extends BaseWXResp {
    lang?: string;
    country?: string;
    state?: string;
    // eslint-disable-next-line @typescript-eslint/naming-convention
    appId?: string;
    code?: string;
}

// LaunchMiniprogramResp 返回值定义
export interface LaunchMiniprogramResp extends BaseWXResp {
    // 微信关闭窗口后返回给APP的自定义信息
    extMsg?: string;
}

// PayResp 返回值定义
export interface PayResp extends BaseWXResp {
    // 财付通返回给商家的信息
    returnKey?: string;
}

// PayResp 返回值定义
export interface OpenBusinessWebViewResp extends BaseWXResp {
    // 第三方程序自定义简单数据，微信终端会回传给第三方程序处理
    resultInfo?: string;
    // 网页业务类型
    businessType?: number;
}
