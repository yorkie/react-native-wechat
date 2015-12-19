
import { DeviceEventEmitter, NativeModules } from 'react-native';

const WeChat = NativeModules.WeChat;
let isAppRegistered = false;

function translateError(err) {
	const ret = new Error(err.message);
	ret.err = err.err;
}

function assertSuccessed(result) {
	if (!result){
		throw new Error("WeChat API returns false.(failed)");
	}
}

function assertRegistered(){
	if (!isAppRegistered){
		throw new Error("Must call registerApp at first.");
	}
}


export function registerApp(appid, callback = assertSuccessed) {
	isAppRegistered = true;
	return new Promise( (resolve) => {
		WeChat.registerApp(appid, resolve)
	}).then(callback);
}

export function registerAppWithDescription(appid, appdesc, callback = assertSuccessed) {
	return new Promise( (resolve) => {
		WeChat.registerAppWithDescription(appid, appdesc, resolve)
	}).then(callback);
}

export function isWXAppinstalled(callback = assertSuccessed) {
	return new Promise( (resolve) => {
		assertRegistered();
		WeChat.isWXAppinstalled(callback);
	}).then(callback);
}

export function isWXAppSupportApi(callback = assertSuccessed) {
	return new Promise( (resolve) => {
		assertRegistered();
		WeChat.isWXAppSupportApi(callback);
	}).then(callback);
}

export function getApiVersion(callback) {
	const ret = new Promise( (resolve) => {
		assertRegistered();
		WeChat.getApiVersion(callback);
	})
	return callback ? ret.then(callback) : ret;
}

export function openWXApp(callback = assertSuccessed) {
	return new Promise( (resolve) => {
		assertRegistered();
		WeChat.openWXApp(callback);
	}).then(callback);
}


var authCallbackList = {};

function waitForAuthResponse(state, result){
	if (!result){
		throw new Error("Failed to call SendReq");
	}
	return new Promise((resolve, reject) => {
		authCallbackList[state] = result => {
			if (result.errCode != 0){
				// Failed.
				const err = new Error(result.errMsg);
				err.code = result.errCode;
				reject(err);
			} else {
				resolve(result);
			}
		}
	});
}

export function sendAuthRequest(state, callback) {
	if (typeof(state) == 'function'){
		callback = state;
		state = undefined;
	}
	if (!state){
		state = Math.random().toString(16).substr(2)+"_"+ new Date().getTime();
	}
	return new Promise( (resolve) => {
		assertRegistered();
		if (authCallbackList[state]){
			throw new Error('Last request of state `'+state+'` is not responsed yet.');
		}
		WeChat.sendAuthRequest(state, resolve);
	})
		.then(result=>waitForAuthResponse(state, result))
		.then(callback);
}

function onWeChatResponse(resp){
	if (resp.type == 'SendAuth.Resp'){
		const callback = authCallbackList[resp.state];
		delete authCallbackList[resp.state];
		return callback && callback(resp);
	}
}
DeviceEventEmitter.addListener('WeChat_Resp', onWeChatResponse);
