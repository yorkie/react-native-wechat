import { DeviceEventEmitter, NativeModules } from 'react-native';
import promisify from 'es6-promisify';
import { EventEmitter } from 'events';

let isAppRegistered = false;
const {WeChat} = NativeModules;

// Event emitter to dispatch request and response from WeChat.
const emitter = new EventEmitter();

DeviceEventEmitter.addListener('WeChat_Resp', resp => {
  emitter.emit(resp.type, resp);
});

// export methods for emitter.
export const addListener = emitter.addListener.bind(emitter);
export const once = emitter.once.bind(emitter);
export const removeAllListeners = emitter.removeAllListeners.bind(emitter);

// Used only with promisify. Transform callback to promise result.
function translateError(err, result) {
  if (!err) {
    return this.resolve(result);
  }
  if (typeof err === 'string') {
    return this.reject(new Error(err));
  }
  this.reject(err);
}

function wrapRegisterApp(nativeFunc) {
  if (!nativeFunc) {
    return undefined;
  }
  const promisified = promisify(nativeFunc, translateError);
  return (...args) => {
    if (isAppRegistered) {
      return Promise.reject(new Error('App is already registered.'));
    }
    isAppRegistered = true;
    return promisified(...args);
  };
}

function wrapApi(nativeFunc) {
  if (!nativeFunc) {
    return undefined;
  }
  const promisified = promisify(nativeFunc, translateError);
  return (...args) => {
    if (!isAppRegistered) {
      return Promise.reject(new Error('registerApp required.'));
    }
    return promisified(...args);
  };
}

export const registerApp = wrapRegisterApp(WeChat.registerApp);

export const registerAppWithDescription = wrapRegisterApp(WeChat.registerAppWithDescription);

export const isWXAppInstalled = wrapApi(WeChat.isWXAppInstalled);

export const isWXAppSupportApi = wrapApi(WeChat.isWXAppSupportApi);

export const getWXAppInstallUrl = wrapApi(WeChat.getWXAppInstallUrl);

export const getApiVersion = wrapApi(WeChat.getApiVersion);

export const openWXApp = wrapApi(WeChat.openWXApp);

const nativeShareToTimeline = wrapApi(WeChat.shareToTimeline);

const nativeShareToSession = wrapApi(WeChat.shareToSession);

const nativeSendAuthRequest = wrapApi(WeChat.sendAuthRequest);

export function sendAuthRequest(scopes, state) {
  return new Promise((resolve, reject) => {
    WeChat.sendAuthRequest(scopes, state,() => {});
    emitter.on('SendAuth.Resp', (resp) => {
      const result = resp.errCode;
      if(result === 0){
        const code = resp.code;
        resolve(code);
      }else{
        const errorCode = result;
        reject(errorCode);
      }
    });
  });
}

export function shareToTimeline(data) {
  return nativeShareToTimeline(data);
}

export function shareToSession(data) {
  return nativeShareToSession(data);
}
