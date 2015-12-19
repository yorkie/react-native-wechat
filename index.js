import { DeviceEventEmitter, NativeModules } from 'react-native';

const WeChat = NativeModules.WeChat;

// Translate a pure object or message into a Error instance.
function translateError(err) {
  if (typeof(err) == 'object' && !(err instanceof Error)) {
    const ret = new Error(err.message);
    ret.errCode = err.errCode || err.err;
  } else if (typeof(err) == 'string') {
    throw new Error(err);)
  } else {
    throw err;
  }
}

// Save callback and wait for future event.
const authCallbackList = {};
function waitForAuthResponse(state) {
  return new Promise((resolve, reject) => {
    authCallbackList[state] = result => {
      if (result.errCode != 0) {
        // Failed.
        const err = new Error(result.errMsg);
        err.code = result.errCode;
        reject(err);
      } else {
        resolve(result);
      }
    }
  }).catch(translateError);;
}
function onWeChatResponse(resp) {
  if (resp.type == 'SendAuth.Resp') {
    const callback = authCallbackList[resp.state];
    delete authCallbackList[resp.state];
    return callback && callback(resp);
  }
}
DeviceEventEmitter.addListener('WeChat_Resp', onWeChatResponse);

function assertSuccessed(result) {
  if (!result) {
    throw new Error("WeChat API returns false.(failed)");
  }
}

export function registerApp(appid, callback = assertSuccessed) {
  return new Promise((resolve) => {
    WeChat.registerApp(appid, resolve)
  }).then(callback);
}

export function registerAppWithDescription(appid, appdesc, callback = assertSuccessed) {
  return new Promise((resolve) => {
    WeChat.registerAppWithDescription(appid, appdesc, resolve)
  }).then(callback);
}

export function isWXAppinstalled(callback = assertSuccessed) {
  return new Promise((resolve) => {
    WeChat.isWXAppinstalled(resolve);
  }).then(callback);
}

export function isWXAppSupportApi(callback = assertSuccessed) {
  return new Promise((resolve) => {
    WeChat.isWXAppSupportApi(resolve);
  }).then(callback);
}

export function getApiVersion(callback) {
  const ret = new Promise((resolve, reject) => {
    WeChat.getApiVersion((err, result)=>{
      err ? reject(err) : resolve(result);
    });
  }).catch(translateError);
  return callback ? ret.then(
    result=>callback(null, result),
    callback
  ) : ret;
}

export function openWXApp(callback = assertSuccessed) {
  return new Promise((resolve) => {
    WeChat.openWXApp(resolve);
  }).then(callback);
}

export function sendAuthRequest(state, callback) {
  if (typeof(state) == 'function') {
    callback = state;
    state = undefined;
  }
  if (!state) {
    // Generate a random, unique state.
    state = Math.random().toString(16).substr(2) + "_" + new Date().getTime();
  }
  const ret = new Promise((resolve) => {
    if (authCallbackList[state]) {
      throw new Error('Last request of state `' + state + '` is not responsed yet.');
    }
    WeChat.openWXApp(resolve);
  }).then(assumeSuccessed)
    .then(()=>waitForAuthResponse(state));
  return callback ? ret.then(
    result=>callback(null, result),
    callback
  ) : ret;
}

export function sendRequest(openid, callback = assertSuccessed){
  return new Promise(resolve=>{
    WeChat.sendRequest(openid, resolve);
  }).then(callback);
}

export function sendSuccessResponse(callback = assertSuccessed){
  return new Promise(resolve=>{
    WeChat.sendSuccessResponse(resolve);
  }).then(callback);
}

export function sendErrorCommonResponse(message, callback = assertSuccessed){
  return new Promise(resolve=>{
    WeChat.sendErrorCommonResponse(message, resolve);
  }).then(callback);
}

export function sendErrorUserCancelResponse(message, callback = assertSuccessed){
  return new Promise(resolve=>{
    WeChat.sendErrorUserCancelResponse(message, resolve);
  }).then(callback);
}
