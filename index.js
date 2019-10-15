'use strict';

import { DeviceEventEmitter, NativeModules, Platform } from 'react-native';
import { EventEmitter } from 'events';

let isAppRegistered = false;
const native = NativeModules.WeChat;

// Event emitter to dispatch request and response from WeChat.
const emitter = new EventEmitter();
const WXScene = {
  Session           : 0,  /** 聊天界面 */
  Timeline          : 1,  /** 朋友圈 */
  Favorite          : 2,  /** 收藏 */
  SpecifiedSession  : 3,  /** 指定联系人(Not Supported) */
};

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

function wrap(nativeFunc) {
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

function sendRequestAndWaitResp(fn, respType) {
  return new Promise((resolve, reject) => {
    try {
      let r = fn();
      if (r instanceof Promise) {
        r.catch(reject);
      }
    } catch (err) {
      reject(err);
    }
    emitter.once(respType, resp => {
      if (resp.errCode === 0) {
        resolve(resp);
      } else {
        reject(new WechatError(resp));
      }
    });
  });
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
export const registerApp = wrapRegisterApp(native.registerApp);

/**
 * @method registerAppWithDescription
 * @deprecated
 * @param {String} appid - the app id
 * @param {String} appdesc - the app description
 * @return {Promise}
 */
export const registerAppWithDescription = function() {
  console.warn('this function is deprecated.');
};

/**
 * Return if the wechat app is installed in the device.
 * @method isWXAppInstalled
 * @return {Promise}
 */
export const isWXAppInstalled = wrap(native.isWXAppInstalled);

/**
 * Return if the wechat application supports the api
 * @method isWXAppSupportApi
 * @return {Promise}
 */
export const isWXAppSupportApi = wrap(native.isWXAppSupportApi);

/**
 * Get the wechat app installed url
 * @method getWXAppInstallUrl
 * @return {String} the wechat app installed url
 */
export const getWXAppInstallUrl = wrap(native.getWXAppInstallUrl);

/**
 * Get the wechat api version
 * @method getApiVersion
 * @return {String} the api version string
 */
export const getApiVersion = wrap(native.getApiVersion);

/**
 * Open wechat app
 * @method openWXApp
 * @return {Promise}
 */
export const openWXApp = wrap(native.openWXApp);

/**
 * Open a mini program
 * @method openMiniProgram
 * @return {Promise}
 */
export function openMiniProgram(id, type, path) {
  return _sendRequestAndWaitResp(() => {
    wrap(native.openMiniProgram)({ id, type, path });
  }, 'LaunchMiniProgram.Resp');
}

/**
 * @method sendAuthRequest
 * @param {Array} scopes - the scopes for authentication.
 * @return {Promise}
 */
export function sendAuthRequest(scopes, state) {
  return _sendRequestAndWaitResp(() => {
    wrap(native.sendAuthRequest)(scopes, state);
  }, 'SendAuth.Resp');
}

// the internal share implmentation
function shareTo(scene, data) {
  if (data.type === 'text') {
    return wrap(native.shareText)({
      text: data.text,
      scene,
    });
  } else if (data.type === 'news') {
    return wrap(native.shareWebpage)({
      title: data.title,
      description: data.description,
      thumbImageUrl: data.thumbImageUrl,
      webpageUrl: data.webpageUrl,
      scene,
    });
  } else if (data.type === 'imageUrl' ||
    data.type === 'imageResource' ||
    data.type === 'imageFile') {
    return wrap(native.shareImage)({
      title: data.title,
      description: data.description,
      // use image as the thumb
      imageUrl: data.imageUrl,
      scene,
    });
  } else if (data.type === 'audio') {
    return wrap(native.shareMusic)({
      title: data.title,
      description: data.description,
      thumbImageUrl: data.thumbImageUrl,
      musicUrl: data.musicUrl,
      musicDataUrl: data.musicDataUrl,
      musicLowBandUrl: data.musicLowBandUrl,
      musicLowBandDataUrl: data.musicLowBandDataUrl,
      scene,
    });
  } else if (data.type === 'video') {
    return wrap(native.shareVideo)({
      title: data.title,
      description: data.description,
      thumbImageUrl: data.thumbImageUrl,
      videoUrl: data.videoUrl,
      videoLowBandUrl: data.videoLowBandUrl,
      scene,
    });
  } else if (data.type === 'file') {
    return wrap(native.shareFile)({
      // the file path to share
      filePath: data.filePath,
      scene,
    });
  } else if (data.type === 'miniprogram') {
    const miniProgram = data.miniProgram;
    return wrap(native.shareMiniProgram)({
      title: data.title,
      description: data.description,
      thumbImageUrl: data.thumbImageUrl,
      // backwards to the old version in web page.
      webpageUrl: data.webpageUrl,
      // the mini program id
      miniProgramId: miniProgram.id,
      // 0:release, 1:test, 2:debug/experimental
      miniProgramType: miniProgram.type,
      // the page path, for example: `?foo=bar`
      miniProgramPath: miniProgram.path,
      scene,
    });
  }
}

/**
 * Share something to timeline/moments/朋友圈
 * @method shareToTimeline
 * @param {Object} data
 * @param {String} data.thumbImage - Thumb image of the message, which can be a uri or a resource id.
 * @param {String} data.type - Type of this message. Could be {news|text|imageUrl|imageFile|imageResource|video|audio|file}
 * @param {String} data.webpageUrl - Required if type equals news. The webpage link to share.
 * @param {String} data.imageUrl - Provide a remote image if type equals image.
 * @param {String} data.videoUrl - Provide a remote video if type equals video.
 * @param {String} data.musicUrl - Provide a remote music if type equals audio.
 * @param {String} data.filePath - Provide a local file if type equals file.
 * @param {String} data.fileExtension - Provide the file type if type equals file.
 */
export function shareToTimeline(data) {
  return sendRequestAndWaitResp(() => shareTo(WXScene.Timeline, data), 'SendMessageToWX.Resp');
}

/**
 * Share something to a friend or group
 * @method shareToSession
 * @param {Object} data
 * @param {String} data.thumbImage - Thumb image of the message, which can be a uri or a resource id.
 * @param {String} data.type - Type of this message. Could be {news|text|imageUrl|imageFile|imageResource|video|audio|file}
 * @param {String} data.webpageUrl - Required if type equals news. The webpage link to share.
 * @param {String} data.imageUrl - Provide a remote image if type equals image.
 * @param {String} data.videoUrl - Provide a remote video if type equals video.
 * @param {String} data.musicUrl - Provide a remote music if type equals audio.
 * @param {String} data.filePath - Provide a local file if type equals file.
 * @param {String} data.fileExtension - Provide the file type if type equals file.
 */
export function shareToSession(data) {
  return sendRequestAndWaitResp(() => shareTo(WXScene.Session, data), 'SendMessageToWX.Resp');
}

/**
 * Share something to favorite
 * @method shareToFavorite
 * @param {Object} data
 * @param {String} data.thumbImage - Thumb image of the message, which can be a uri or a resource id.
 * @param {String} data.type - Type of this message. Could be {news|text|imageUrl|imageFile|imageResource|video|audio|file}
 * @param {String} data.webpageUrl - Required if type equals news. The webpage link to share.
 * @param {String} data.imageUrl - Provide a remote image if type equals image.
 * @param {String} data.videoUrl - Provide a remote video if type equals video.
 * @param {String} data.musicUrl - Provide a remote music if type equals audio.
 * @param {String} data.filePath - Provide a local file if type equals file.
 * @param {String} data.fileExtension - Provide the file type if type equals file.
 */
export function shareToFavorite(data) {
  return sendRequestAndWaitResp(() => shareTo(WXScene.Favorite, data), 'SendMessageToWX.Resp');
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
  
  // FIXME(94cstyles)
  // Android requires the type of the timeStamp field to be a string
  if (Platform.OS === 'android')
    data.timeStamp = String(data.timeStamp);

  return _sendRequestAndWaitResp(() => {
    return new Promise((resolve, reject) => {
      native.pay(data, res => {
        if (res) reject(res);
      });
    })
  }, 'PayReq.Resp');
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

