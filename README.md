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

- `isWXAppInstalled(callback)` get if wechat installed in this app
  - {Function} `callback` - the result callback.
    - {Boolean} `installed` - the result.
- `isWXAppSupportApi(callback)` get if wechat support open url.
  - {Function} `callback` - the same to `isWXAppInstalled`.
- `getApiVersion(callback)` get api version
  - {Function} `callback`
    - {Error} `err` the error if something went wrong
    - {String} `version` the result
- `openWXApp(callback)` open wechat app
  - {Function} `callback` callback
- `sendAuthRequest(callback)` auth request
  - {Function} `callback` callback

For more details, visit https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=1417674108&token=&lang=zh_CN

## License

MIT @ WeFlex,Inc

