/* eslint-disable import/no-unused-modules */
import { ListenerHolder, ListenerSubscription } from '@shm-open/utilities';
import { DeviceEventEmitter, EmitterSubscription, NativeModules } from 'react-native';
import {
    WechatPay,
    WechatEntrust,
    WechatShareImage,
    WechatShareMiniprogram,
    WechatShareText,
    WechatShareWebPage,
    WechatShareVideo,
    WechatShareMusic,
    WXErrCodeEnum,
    WechatOpenMiniprogram,
    WechatError,
    BaseWXResp,
    SendMessageToWXResp,
    LaunchMiniprogramResp,
    OpenBusinessWebViewResp,
    PayResp,
    SendAuthResp,
} from './wechatInterface';

export {
    WechatMiniprogramTypeEnum,
    WechatSceneEnum,
    WechatShareBase,
    WechatShareMiniprogram,
    WechatOpenMiniprogram,
    WechatShareText,
    WechatShareImage,
    WechatShareWebPage,
    WechatShareVideo,
    WechatShareMusic,
    WechatPay,
    WechatEntrust,
    WXErrCodeEnum,
    WechatError,
    BaseWXResp,
    SendMessageToWXResp,
    SendAuthResp,
    LaunchMiniprogramResp,
    PayResp,
    OpenBusinessWebViewResp,
} from './wechatInterface';

type EventType =
    | 'SendMessageToWX.Resp'
    | 'LaunchMiniprogram.Resp'
    | 'PayReq.Resp'
    | 'WXOpenBusinessWebview.Resp'
    | 'SendAuth.Resp';

/**
 * 微信是否已安装
 */
export function isWXAppInstalled(): Promise<boolean> {
    return NativeModules.Wechat.isWXAppInstalled();
}

/**
 * 注册wechat
 */
export function registerApp(appId: string, universalLink: string): Promise<void> {
    return NativeModules.Wechat.registerApp(appId, universalLink);
}

/**
 * 判断当前微信的版本是否支持
 */
export function isWXAppSupportApi(): Promise<boolean> {
    return NativeModules.Wechat.isWXAppSupportApi();
}

/**
 * 微信的安装地址字符串[only iOS]
 */
export function getWXAppInstallUrl(): Promise<string> {
    return new Promise((resolve, reject) => {
        NativeModules.Wechat.getWXAppInstallUrl((error: string, installURL: string) => {
            if (!error) {
                resolve(installURL);
            } else {
                reject(new WechatError(WXErrCodeEnum.WXErrCodeCommon, error));
            }
        });
    });
}

/**
 * 返回当前微信SDK的版本号[only iOS]
 */
export function getApiVersion(): Promise<string> {
    return new Promise((resolve, reject) => {
        NativeModules.Wechat.getApiVersion((error: string, version: string) => {
            if (!error) {
                resolve(version);
            } else {
                reject(new WechatError(WXErrCodeEnum.WXErrCodeCommon, error));
            }
        });
    });
}

/**
 * 打开微信
 */
export function openWXApp(): Promise<void> {
    return NativeModules.Wechat.openWXApp();
}

function promiseWrap<T extends BaseWXResp>(
    eventType: EventType,
    callWXApi: () => Promise<T>,
): Promise<T> {
    return new Promise((resolve, reject) => {
        // 异步结果，微信部分API成功结果没有回调。
        let subscription: EmitterSubscription;
        const listener = (resp: T) => {
            if (resp.errCode === WXErrCodeEnum.WXSuccess) {
                resolve(resp);
            } else {
                reject(new WechatError(resp.errCode, resp.errStr));
            }
            if (subscription) {
                subscription.remove();
                subscription = null;
            }
        };
        subscription = DeviceEventEmitter.addListener(eventType, listener);
        // 同步结果，微信同步成功回调不可信，同步失败回调可信。
        // TODO: do we need a timeout?
        callWXApi().catch((error: WechatError) => {
            if (subscription) {
                subscription.remove();
                subscription = null;
            }
            reject(error);
        });
    });
}

/**
 * @method 微信登录授权
 * @param scopes - 第三方程序要向微信申请认证，并请求某些权限
 * @param state - 第三方程序本身用来标识其请求的唯一性，最后跳转回第三方程序时，由微信终端回传。
 * @return Promise
 */
export function sendAuthRequest(
    scopes: 'snsapi_userinfo' | 'snsapi_base',
    state?: string,
): Promise<SendAuthResp> {
    return promiseWrap<SendAuthResp>('SendAuth.Resp', () =>
        NativeModules.Wechat.sendAuthRequest(scopes, state),
    );
}

/**
 * @method 分享文本
 * @param shareData - 数据
 * @return Promise
 */
export function shareText(shareData: WechatShareText): Promise<SendMessageToWXResp> {
    return promiseWrap<SendMessageToWXResp>('SendMessageToWX.Resp', () =>
        NativeModules.Wechat.shareText(shareData),
    );
}

/**
 * @method 分享图片
 * @param shareData - 数据
 * @return Promise
 */
export function shareImage(shareData: WechatShareImage): Promise<SendMessageToWXResp> {
    return promiseWrap<SendMessageToWXResp>('SendMessageToWX.Resp', () =>
        NativeModules.Wechat.shareImage(shareData),
    );
}

/**
 * @method 分享网页(媒体数据)
 * @param shareData - 数据
 * @return Promise
 */
export function shareWebpage(shareData: WechatShareWebPage): Promise<SendMessageToWXResp> {
    return promiseWrap<SendMessageToWXResp>('SendMessageToWX.Resp', () =>
        NativeModules.Wechat.shareWebpage(shareData),
    );
}

/**
 * @method 分享小程序
 * @param shareData - 数据
 * @return Promise
 */
export function shareMiniprogram(shareData: WechatShareMiniprogram): Promise<SendMessageToWXResp> {
    return promiseWrap<SendMessageToWXResp>('SendMessageToWX.Resp', () =>
        NativeModules.Wechat.shareMiniprogram(shareData),
    );
}

/**
 * @method 分享Video
 * @param shareData - 数据
 * @return Promise
 */
export function shareVideo(shareData: WechatShareVideo): Promise<SendMessageToWXResp> {
    return promiseWrap<SendMessageToWXResp>('SendMessageToWX.Resp', () =>
        NativeModules.Wechat.shareVideo(shareData),
    );
}

/**
 * @method 分享Music
 * @param shareData - 数据
 * @return Promise
 */
export function shareMusic(shareData: WechatShareMusic): Promise<SendMessageToWXResp> {
    return promiseWrap<SendMessageToWXResp>('SendMessageToWX.Resp', () =>
        NativeModules.Wechat.shareMusic(shareData),
    );
}

/**
 * @method 打开小程序
 * @param shareData - 数据
 * @return Promise
 */
export function openMiniprogram(shareData: WechatOpenMiniprogram): Promise<LaunchMiniprogramResp> {
    return promiseWrap<LaunchMiniprogramResp>('LaunchMiniprogram.Resp', () =>
        NativeModules.Wechat.openMiniprogram(shareData),
    );
}

/**
 * @method 微信支付
 * @param payData - 数据
 * @return Promise
 */
export function pay(payData: WechatPay): Promise<PayResp> {
    return promiseWrap<PayResp>('PayReq.Resp', () => NativeModules.Wechat.pay(payData));
}

/**
 * @method 微信签约
 * @param entrustData - 数据
 * @return Promise
 */
export function entrust(entrustData: WechatEntrust): Promise<OpenBusinessWebViewResp> {
    return promiseWrap<OpenBusinessWebViewResp>('WXOpenBusinessWebview.Resp', () =>
        NativeModules.Wechat.entrust(entrustData),
    );
}

// Wechat handler APP
export interface LaunchFromWXReq {
    lang?: string;
    country?: string;
    extInfo?: string;
}
type LaunchFromWXReqEventType = 'LaunchFromWX.Req';
type LaunchFromWXReqEventHandler = (data: LaunchFromWXReq) => void;

type WechatEventType = LaunchFromWXReqEventType;
type WechatEventHandler = LaunchFromWXReqEventHandler;
const handlerHolder = new ListenerHolder<WechatEventType, WechatEventHandler>();

export function addWechatListener(
    type: LaunchFromWXReqEventType,
    listener: LaunchFromWXReqEventHandler,
);

export function addWechatListener(
    type: WechatEventType,
    listener: WechatEventHandler,
): ListenerSubscription {
    return handlerHolder.addListener(type, listener);
}

DeviceEventEmitter.addListener('LaunchFromWX.Req', (data) => {
    handlerHolder.dispatch('LaunchFromWX.Req', data);
});
