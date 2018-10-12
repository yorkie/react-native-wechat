'use strict';

import {DeviceEventEmitter, NativeModules, Platform} from 'react-native';
import {EventEmitter} from 'events';

let isAppRegistered = false;
const {WeChat} = NativeModules;

// Event emitter to dispatch request and response from WeChat.
const emitter = new EventEmitter();

DeviceEventEmitter.addListener('WeChat_Resp', resp => {
    emitter.emit(resp.type, resp);
});

function wrapRegisterApp(nativeFunc) {
    if (!nativeFunc) {
        return undefined;
    }
    return (...args) => {
        if (isAppRegistered) {
            // FIXME(Yorkie): we ignore this error if AppRegistered is true.
            return Promise.resolve(true);
        }
        isAppRegistered = true;
        return new Promise((resolve, reject) => {
            nativeFunc.apply(null, [
                ...args,
                (error, result) => {
                    if (!error) {
                        return resolve(result);
                    }
                    if (typeof error === 'string') {
                        return reject(new Error(error));
                    }
                    reject(error);
                },
            ]);
        });
    };
}

function wrapApi(nativeFunc) {
    if (!nativeFunc) {
        return undefined;
    }
    return (...args) => {
        if (!isAppRegistered) {
            return Promise.reject(new Error('registerApp required.'));
        }
        return new Promise((resolve, reject) => {
            nativeFunc.apply(null, [
                ...args,
                (error, result) => {
                    if (!error) {
                        return resolve(result);
                    }
                    if (typeof error === 'string') {
                        return reject(new Error(error));
                    }
                    reject(error);
                },
            ]);
        });
    };
}

/**
 * `addListener` inherits from `events` module
 * @method addListener
 * @param {String} eventName - the event name
 * @param {Function} trigger - the function when event is fired
 */
export const addListener = emitter.addListener.bind(emitter);

/**
 * `once` inherits from `events` module
 * @method once
 * @param {String} eventName - the event name
 * @param {Function} trigger - the function when event is fired
 */
export const once = emitter.once.bind(emitter);

/**
 * `removeAllListeners` inherits from `events` module
 * @method removeAllListeners
 * @param {String} eventName - the event name
 */
export const removeAllListeners = emitter.removeAllListeners.bind(emitter);

/**
 * @method registerApp
 * @param {String} appid - the app id
 * @return {Promise}
 */
export const registerApp = wrapRegisterApp(WeChat.registerApp);

/**
 * @method registerAppWithDescription
 * @param {String} appid - the app id
 * @param {String} appdesc - the app description
 * @return {Promise}
 */
export const registerAppWithDescription = wrapRegisterApp(
    WeChat.registerAppWithDescription,
);

/**
 * Return if the wechat app is installed in the device.
 * @method isWXAppInstalled
 * @return {Promise}
 */
export const isWXAppInstalled = wrapApi(WeChat.isWXAppInstalled);

/**
 * Return if the wechat application supports the api
 * @method isWXAppSupportApi
 * @return {Promise}
 */
export const isWXAppSupportApi = wrapApi(WeChat.isWXAppSupportApi);

/**
 * Get the wechat app installed url
 * @method getWXAppInstallUrl
 * @return {String} the wechat app installed url
 */
export const getWXAppInstallUrl = wrapApi(WeChat.getWXAppInstallUrl);

/**
 * Get the wechat api version
 * @method getApiVersion
 * @return {String} the api version string
 */
export const getApiVersion = wrapApi(WeChat.getApiVersion);

/**
 * Open wechat app
 * @method openWXApp
 * @return {Promise}
 */
export const openWXApp = wrapApi(WeChat.openWXApp);

// wrap the APIs
const nativeShareToTimeline = wrapApi(WeChat.shareToTimeline);
const nativeLaunchMini = wrapApi(WeChat.launchMini);
const nativeShareToSession = wrapApi(WeChat.shareToSession);
const nativeSendAuthRequest = wrapApi(WeChat.sendAuthRequest);

/**
 * @method sendAuthRequest
 * @param {Array} scopes - the scopes for authentication.
 * @return {Promise}
 */
export function sendAuthRequest(scopes, state) {
    return new Promise((resolve, reject) => {
        WeChat.sendAuthRequest(scopes, state, () => {
        });
        emitter.once('SendAuth.Resp', resp => {
            if (resp.errCode === 0) {
                resolve(resp);
            } else {
                reject(new WechatError(resp));
            }
        });
    });
}

/**
 * Share something to timeline/moments/朋友圈
 * @method shareToTimeline
 * @param {Object} data
 * @param {String} data.thumbImage - Thumb image of the message, which can be a uri or a resource id.
 * @param {String} data.type - Type of this message. Could be {news|text|imageUrl|imageFile|imageResource|video|audio|file|mini}
 * @param {String} data.webpageUrl - Required if type equals news or mini. The webpage link to share.
 * @param {String} data.userName - 小程序的原生id.
 * @param {String} data.path - 小程序页面的路径.
 * @param {String} data.hdImageData - 小程序节点高清大图，小于128k.
 * @param {Boolean} data.withShareTicket - 是否使用带 shareTicket 的转发
 * @param {Integer} data.miniProgramType - 分享小程序的版本（0-正式，1-开发，2-体验）
 * @param {String} data.webpageUrl - Required if type equals news. The webpage link to share.
 * @param {String} data.webpageUrl - Required if type equals news. The webpage link to share.
 * @param {String} data.imageUrl - Provide a remote image if type equals image.
 * @param {String} data.videoUrl - Provide a remote video if type equals video.
 * @param {String} data.musicUrl - Provide a remote music if type equals audio.
 * @param {String} data.filePath - Provide a local file if type equals file.
 * @param {String} data.fileExtension - Provide the file type if type equals file.
 */
export function shareToTimeline(data) {
    return new Promise((resolve, reject) => {
        nativeShareToTimeline(data);
        emitter.once('SendMessageToWX.Resp', resp => {
            if (resp.errCode === 0) {
                resolve(resp);
            } else {
                reject(new WechatError(resp));
            }
        });
    });
}

/**
 * 打开小程序
 * @method launchMini
 * @param
 * @param {String} userName - 拉起的小程序的username
 * @param {Integer} miniProgramType - 拉起小程序的类型. 0-正式版 1-开发版 2-体验版
 * @param {String} path - 拉起小程序页面的可带参路径，不填默认拉起小程序首页
 */
export function launchMini({userName, miniProgramType = 0, path = ''}) {
    return new Promise((resolve, reject) => {
        if (miniProgramType !== 0 && miniProgramType !== 1 && miniProgramType !== 2) {
            reject(new WechatError({errStr: '拉起小程序的类型不对，0-正式版 1-开发版 2-体验版', errCode: -1}))
            return
        }
        nativeLaunchMini({userName, miniProgramType, path});
        emitter.once('WXLaunchMiniProgramReq.Resp', resp => {
            if (resp.errCode === 0) {
                resolve(resp);
            } else {
                reject(new WechatError(resp));
            }
        });
    });
}

/**
 * Share something to a friend or group
 * @method shareToSession
 * @param {Object} data
 * @param {String} data.thumbImage - Thumb image of the message, which can be a uri or a resource id.
 * @param {String} data.type - Type of this message. Could be {news|text|imageUrl|imageFile|imageResource|video|audio|file}
 * @param {String} data.webpageUrl - Required if type equals news. The webpage link to share.
 * @param {String} data.userName - 小程序的原生id.
 * @param {String} data.path - 小程序页面的路径.
 * @param {String} data.hdImageData - 小程序节点高清大图，小于128k.
 * @param {Boolean} data.withShareTicket - 是否使用带 shareTicket 的转发
 * @param {Integer} data.miniProgramType - 分享小程序的版本（0-正式，1-开发，2-体验）
 * @param {String} data.hdImageData - 小程序节点高清大图，小于128k.
 * @param {String} data.imageUrl - Provide a remote image if type equals image.
 * @param {String} data.videoUrl - Provide a remote video if type equals video.
 * @param {String} data.musicUrl - Provide a remote music if type equals audio.
 * @param {String} data.filePath - Provide a local file if type equals file.
 * @param {String} data.fileExtension - Provide the file type if type equals file.
 */
export function shareToSession(data) {
    return new Promise((resolve, reject) => {
        nativeShareToSession(data);
        emitter.once('SendMessageToWX.Resp', resp => {
            if (resp.errCode === 0) {
                resolve(resp);
            } else {
                reject(new WechatError(resp));
            }
        });
    });
}

/**
 * wechat pay
 * @param {Object} data
 * @param {String} data.partnerId
 * @param {String} data.prepayId
 * @param {String} data.nonceStr
 * @param {String} data.timeStamp
 * @param {String} data.package
 * @param {String} data.sign
 * @returns {Promise}
 */
export function pay(data) {
    // FIXME(Yorkie): see https://github.com/yorkie/react-native-wechat/issues/203
    // Here the server-side returns params in lowercase, but here SDK requires timeStamp
    // for compatibility, we make this correction for users.
    function correct(actual, fixed) {
        if (!data[fixed] && data[actual]) {
            data[fixed] = data[actual];
            delete data[actual];
        }
    }

    correct('prepayid', 'prepayId');
    correct('noncestr', 'nonceStr');
    correct('partnerid', 'partnerId');
    correct('timestamp', 'timeStamp');

    return new Promise((resolve, reject) => {
        WeChat.pay(data, result => {
            if (result) reject(result);
        });
        emitter.once('PayReq.Resp', resp => {
            if (resp.errCode === 0) {
                resolve(resp);
            } else {
                reject(new WechatError(resp));
            }
        });
    });
}

/**
 * promises will reject with this error when API call finish with an errCode other than zero.
 */
export class WechatError extends Error {
    constructor(resp) {
        const message = resp.errStr || resp.errCode.toString();
        super(message);
        this.name = 'WechatError';
        this.code = resp.errCode;

        // avoid babel's limition about extending Error class
        // https://github.com/babel/babel/issues/3083
        if (typeof Object.setPrototypeOf === 'function') {
            Object.setPrototypeOf(this, WechatError.prototype);
        } else {
            this.__proto__ = WechatError.prototype;
        }
    }
}

