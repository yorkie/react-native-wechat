import { DeviceEventEmitter, NativeModules } from 'react-native';
import promisify from 'es6-promisify';
const WeChat = NativeModules.WeChat;

// Translate a pure object or message into a Error instance.
function translateError(err, result) {
  if (!err) {
    this.resolve(result);
  }
  if (typeof err === 'object') {
    if (err instanceof Error) {
      return this.reject(ret);
    }
    return this.reject(Object.assign(new Error(err.message), { errCode: err.errCode }));
  } else if (typeof(err) === 'string') {
    return this.reject(new Error(err));
  }
  this.reject(Object.assign(new Error(), { origin: err }));
}

// Save callback and wait for future event.
const authCallbackList = {};
function waitForAuthResponse(state) {
  return new Promise((resolve, reject) => {
    authCallbackList[state] = result => {
      if (result.errCode !== 0) {
        const err = new Error(result.errMsg);
        err.errCode = result.errCode;
        reject(err);
      } else {
        resolve(result);
      }
    };
  });
}

DeviceEventEmitter.addListener('WeChat_Resp', resp => {
  if (resp.type === 'SendAuth.Resp') {
    const callback = authCallbackList[resp.state];
    delete authCallbackList[resp.state];
    return callback && callback(resp);
  }
  if (__DEV__) {
    throw new Error('Unsupported response type: ' + resp.type);
  }
});

export const registerApp = promisify(WeChat.registerApp, translateError);

export const registerAppWithDescription = promisify(WeChat.registerAppWithDescription, translateError);

export const isWXAppInstalled = promisify(WeChat.isWXAppInstalled, translateError);

export const isWXAppSupportApi = promisify(WeChat.isWXAppSupportApi, translateError);

export const getApiVersion = promisify(WeChat.getApiVersion, translateError);

export const openWXApp = promisify(WeChat.openWXApp, translateError);

export const nativeSendAuthRequest = promisify(WeChat.sendAuthRequest, translateError);

export function sendAuthRequest(scopes, state) {
  // Generate a random, unique state if not provided.
  const _state = state || Math.random().toString(16).substr(2) + '_' + new Date().getTime();
  return nativeSendAuthRequest(scopes || 'snsapi_userinfo', _state)
    .then(() => waitForAuthResponse(state, resolve));
}
