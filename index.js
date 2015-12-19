import { DeviceEventEmitter, NativeModules } from 'react-native';

let registered = false;
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

export function registerApp(appid, callback) {
  const ret =  new Promise((resolve, reject) => {
    if (registered){
      return reject(new Error("This app is already registered."));
    }
    registered = true;
    WeChat.registerApp(appid, resolve)
  }).then(assertSuccessed);
  if (callback){
    ret.then(result=>callback(result), err=>callback(false));
  }
  return ret;
}

export function registerAppWithDescription(appid, appdesc, callback) {
  const ret = new Promise((resolve, reject) => {
    if (registered){
      return reject(new Error("This app is already registered."));
    }
    WeChat.registerAppWithDescription(appid, appdesc, resolve)
  }).then(assertSuccessed);
  if (callback){
    ret.then(result=>callback(result), err=>callback(false));
  }
  return ret;
}

export function isWXAppinstalled(callback) {
  const ret = new Promise((resolve, reject) => {
    if (!registered){
      return reject(new Error("Must call registerApp() first."));
    }
    WeChat.isWXAppinstalled(resolve);
  });
  if (callback){
    ret.then(result=>callback(result), err=>callback(false));
  }
  return ret;
}

export function isWXAppSupportApi(callback = assertSuccessed) {
  const ret =  new Promise((resolve, reject) => {
    if (!registered){
      return reject(new Error("Must call registerApp() first."));
    }
    WeChat.isWXAppSupportApi(resolve);
  });
  if (callback){
    ret.then(result=>callback(result), err=>callback(false));
  }
  return ret;
}

export function getApiVersion(callback) {
  const ret = new Promise((resolve, reject) => {
    if (!registered){
      return reject(new Error("Must call registerApp() first."));
    }
    WeChat.getApiVersion((err, result)=>{
      err ? reject(err) : resolve(result);
    });
  }).catch(translateError);
  if (callback){
    ret.then(result=>callback(null, result), callback);
  }
  return ret;
}

export function openWXApp(callback = assertSuccessed) {
  const ret = new Promise((resolve, reject) => {
    if (!registered){
      return reject(new Error("Must call registerApp() first."));
    }
    WeChat.openWXApp(resolve);
  })
  if (callback){
    ret.then(result=>callback(result), err=>callback(false));
  }
  return ret;
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
  const ret = new Promise((resolve, reject) => {
    if (!registered){
      return reject(new Error("Must call registerApp() first."));
    }
    if (authCallbackList[state]) {
      throw new Error('Last request of state `' + state + '` is not responsed yet.');
    }
    WeChat.openWXApp(resolve);
  }).then(assertSuccessed)
    .then(()=>waitForAuthResponse(state));
  if (callback){
    ret.then(result=>callback(null, result), callback);
  }
  return ret;
}

export function sendRequest(openid, callback){
  const ret = new Promise((resolve, reject) => {
    if (!registered){
      return reject(new Error("Must call registerApp() first."));
    }
    WeChat.sendRequest(openid, resolve);
  }).then(assertSuccessed);
  if (callback){
    ret.then(result=>callback(null, result), callback);
  }
  return ret;
}

export function sendSuccessResponse(callback){
  const ret = new Promise((resolve, reject) => {
    if (!registered){
      return reject(new Error("Must call registerApp() first."));
    }
    WeChat.sendSuccessResponse(resolve);
  }).then(assertSuccessed);
  if (callback){
    ret.then(result=>callback(null, result), callback);
  }
  return ret;
}

export function sendErrorCommonResponse(message, callback){
  const ret = new Promise((resolve, reject) => {
    if (!registered){
      return reject(new Error("Must call registerApp() first."));
    }
    WeChat.sendErrorCommonResponse(message, resolve);
  }).then(assertSuccessed);
  if (callback){
    ret.then(result=>callback(null, result), callback);
  }
  return ret;
}

export function sendErrorUserCancelResponse(message, callback){
  const ret = new Promise((resolve, reject) => {
    WeChat.sendErrorUserCancelResponse(message, resolve);
  }).then(assertSuccessed);
  if (callback){
    ret.then(result=>callback(null, result), callback);
  }
  return ret;
}
