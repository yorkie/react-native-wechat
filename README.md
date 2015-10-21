# react-native-wechat

React native for WeChat, this is still under in-progress to develop.
This module binds WeChat's iOS SDK.

## Install

```sh
$ npm install react-native-wechat --save
```

## Linking in your XCode project

1. Link `RCTWeChat` library from your `node_modules/react-native-wechat/ios` folder like its
  [described here](http://facebook.github.io/react-native/docs/linking-libraries-ios.html).
  Don't forget to add it to "Build Phases" of project.
2. Added the following libraries to your "Link Binary With Libraries":
  - CoreTelephony.framework
  - libsqlite3.0
  - libc++
  - libz
3. Done

## API Documentation

#### registerApp(appid)

- {String} `appid`

#### registerAppWithDescription(appid, appdesc)

- {String} `appid`
- {String} `appdesc`

#### isWXAppInstalled(callback) 

> get if wechat installed in this app

- {Function} `callback` - the result callback.
  - {Boolean} `installed` - the result.

#### isWXAppSupportApi(callback)

> get if wechat support open url.

- {Function} `callback` - the same to `isWXAppInstalled`.

#### getApiVersion(callback)

> get api version

- {Function} `callback`
  - {Error} `err` the error if something went wrong
  - {String} `version` the result

#### openWXApp(callback)

> open wechat app

- {Function} `callback` callback

#### sendRequest(openid, callback)

> send request to wechat

- {String} `openid` the user openid
- {Function} `callback` the callback

#### sendAuthRequest(state, callback)

> auth request

- {String} `state` the state of OAuth2
- {Function} `callback` callback

#### sendSuccessResponse(callback)

> send a success response to wechat

- {Function} `callback` callback

#### sendErrorCommonResponse(message, callback)

> send an error response to wechat

- {String} `message` the error message
- {Function} `callback` callback

#### sendErrorUserCancelResponse(message, callback)

> send an error becaosue cancelation by user to wechat

- {String} `message` the error message
- {Function} `callback` callback

For more details, visit [WeChat SDK Documentation](https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=1417674108&token=&lang=zh_CN)

## License

MIT @ WeFlex,Inc

