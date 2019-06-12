<img height="200" src="./weixin.png?raw=true">

# React-Native-Wechat

[React Native] bridging library that integrates WeChat SDKs:

- [x] iOS SDK 1.7.2
- [x] Android SDK 221

[react-native-wechat] has the following tracking data in the open source world:

| NPM | Dependency | Downloads | Build |
|-----|------------|-----------|-------|
| [![NPM version][npm-image]][npm-url] | [![Dependency Status][david-image]][david-url] | [![Downloads][downloads-image]][downloads-url] | [![Build Status][travis-image]][travis-url] |

## Table of Contents

- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Installation](#installation)
- [Community](#community)
- [Authors](#authors)
- [License](#license)

## Getting Started

- [Build setup on iOS](./docs/build-setup-ios.md)
- [Build setup on Android](./docs/build-setup-android.md)

## API Documentation

[react-native-wechat] uses Promises, therefore you can use `Promise`
or `async/await` to manage your dataflow.

#### registerApp(appid)

- `appid` {String} the appid you get from WeChat dashboard
- returns {Boolean} explains if your application is registered done

This method should be called once globally.

```js
import * as WeChat from 'react-native-wechat';

WeChat.registerApp('appid');
```

#### registerAppWithDescription(appid, description)

- `appid` {String} the appid you get from WeChat dashboard
- `description` {String} the description of your app
- returns {Boolean} explains if your application is registered done

This method is only available on iOS.

#### isWXAppInstalled() 

- returns {Boolean} if WeChat is installed.

Check if the WeChat app is installed on the device.

#### isWXAppSupportApi()

- returns {Boolean} Contains the result.

Check if wechat support open url.

#### getApiVersion()

- returns {String} Contains the result.

Get the WeChat SDK api version.

#### openWXApp()

- returns {Boolean} 

Open the WeChat app from your application.

#### sendAuthRequest([scope[, state]])

- `scope` {Array|String} Scopes of auth request.
- `state` {String} the state of OAuth2
- returns {Object}

Send authentication request, and it returns an object with the 
following fields:

| field   | type   | description                         |
|---------|--------|-------------------------------------|
| errCode | Number | Error Code                          |
| errStr  | String | Error message if any error occurred |
| openId  | String |                                     |
| code    | String | Authorization code                  |
| url     | String | The URL string                      |
| lang    | String | The user language                   |
| country | String | The user country                    |

#### class `ShareMetadata`

- `title` {String}  title of this message. 
- `type` {Number} type of this message. Can be {news|text|imageUrl|imageFile|imageResource|video|audio|file}
- `thumbImage` {String} Thumb image of the message, which can be a uri or a resource id.
- `description` {String} The description about the sharing.
- `webpageUrl` {String} Required if type equals `news`. The webpage link to share.
- `imageUrl` {String} Provide a remote image if type equals `image`.
- `videoUrl` {String} Provide a remote video if type equals `video`.
- `musicUrl` {String} Provide a remote music if type equals `audio`.
- `filePath` {String} Provide a local file if type equals `file`.
- `fileExtension` {String} Provide the file type if type equals `file`.

#### shareToTimeline(message)

- `message` {ShareMetadata} This object saves the metadata for sharing
- returns {Object}

Share a `ShareMetadata` message to timeline(朋友圈) and returns:

| name    | type   | description                         |
|---------|--------|-------------------------------------|
| errCode | Number | 0 if authorization successed        |
| errStr  | String | Error message if any error occurred |

The following examples require the 'react-native-chat' and 'react-native-fs' packages.

```js
import * as WeChat from 'react-native-wechat';
import fs from 'react-native-fs';
let resolveAssetSource = require('resolveAssetSource');

// Code example to share text message:
try {
  let result = await WeChat.shareToTimeline({
    type: 'text', 
    description: 'hello, wechat'
  });
  console.log('share text message to time line successful:', result);
} catch (e) {
  if (e instanceof WeChat.WechatError) {
    console.error(e.stack);
  } else {
    throw e;
  }
}

// Code example to share image url:
// Share raw http(s) image from web will always fail with unknown reason, please use image file or image resource instead
try {
  let result = await WeChat.shareToTimeline({
    type: 'imageUrl',
    title: 'web image',
    description: 'share web image to time line',
    mediaTagName: 'email signature',
    messageAction: undefined,
    messageExt: undefined,
    imageUrl: 'http://www.ncloud.hk/email-signature-262x100.png'
  });
  console.log('share image url to time line successful:', result);
} catch (e) {
  if (e instanceof WeChat.WechatError) {
    console.error(e.stack);
  } else {
    throw e;
  }
}

// Code example to share image file:
try {
  let rootPath = fs.DocumentDirectoryPath;
  let savePath = rootPath + '/email-signature-262x100.png';
  console.log(savePath);
  
  /*
   * savePath on iOS may be:
   *  /var/mobile/Containers/Data/Application/B1308E13-35F1-41AB-A20D-3117BE8EE8FE/Documents/email-signature-262x100.png
   *
   * savePath on Android may be:
   *  /data/data/com.wechatsample/files/email-signature-262x100.png
   **/
  await fs.downloadFile('http://www.ncloud.hk/email-signature-262x100.png', savePath);
  let result = await WeChat.shareToTimeline({
    type: 'imageFile',
    title: 'image file download from network',
    description: 'share image file to time line',
    mediaTagName: 'email signature',
    messageAction: undefined,
    messageExt: undefined,
    imageUrl: "file://" + savePath // require the prefix on both iOS and Android platform
  });
  console.log('share image file to time line successful:', result);
} catch (e) {
  if (e instanceof WeChat.WechatError) {
    console.error(e.stack);
  } else {
    throw e;
  }
}

// Code example to share image resource:
try {
  let imageResource = require('./email-signature-262x100.png');
  let result = await WeChat.shareToTimeline({
    type: 'imageResource',
    title: 'resource image',
    description: 'share resource image to time line',
    mediaTagName: 'email signature',
    messageAction: undefined,
    messageExt: undefined,
    imageUrl: resolveAssetSource(imageResource).uri
  });
  console.log('share resource image to time line successful', result);
}
catch (e) {
  if (e instanceof WeChat.WechatError) {
    console.error(e.stack);
  } else {
    throw e;
  }
}

// Code example to download an word file from web, then share it to WeChat session
// only support to share to session but time line
// iOS code use DocumentDirectoryPath
try {
  let rootPath = fs.DocumentDirectoryPath;
  let fileName = 'signature_method.doc';
  /*
   * savePath on iOS may be:
   *  /var/mobile/Containers/Data/Application/B1308E13-35F1-41AB-A20D-3117BE8EE8FE/Documents/signature_method.doc
   **/ 
  let savePath = rootPath + '/' + fileName;

  await fs.downloadFile('https://open.weixin.qq.com/zh_CN/htmledition/res/assets/signature_method.doc', savePath);
  let result = await WeChat.shareToSession({
    type: 'file',
    title: fileName, // WeChat app treat title as file name
    description: 'share word file to chat session',
    mediaTagName: 'word file',
    messageAction: undefined,
    messageExt: undefined,
    filePath: savePath,
    fileExtension: '.doc'
  });
  console.log('share word file to chat session successful', result);
} catch (e) {
  if (e instanceof WeChat.WechatError) {
    console.error(e.stack);
  } else {
    throw e;
  }
}

//android code use ExternalDirectoryPath
try {
  let rootPath = fs.ExternalDirectoryPath;
  let fileName = 'signature_method.doc';
  /*
   * savePath on Android may be:
   *  /storage/emulated/0/Android/data/com.wechatsample/files/signature_method.doc
   **/
  let savePath = rootPath + '/' + fileName;
  await fs.downloadFile('https://open.weixin.qq.com/zh_CN/htmledition/res/assets/signature_method.doc', savePath);
  let result = await WeChat.shareToSession({
    type: 'file',
    title: fileName, // WeChat app treat title as file name
    description: 'share word file to chat session',
    mediaTagName: 'word file',
    messageAction: undefined,
    messageExt: undefined,
    filePath: savePath,
    fileExtension: '.doc'
  });
  console.log('share word file to chat session successful', result);
}
catch (e) {
  if (e instanceof WeChat.WechatError) {
    console.error(e.stack);
  } else {
    throw e;
  }
}
```

#### shareToSession(message)

- `message` {ShareMetadata} This object saves the metadata for sharing
- returns {Object}

Similar to `shareToTimeline` but sends the message to a friend or chat group.

#### pay(payload)

- `payload` {Object} the payment data
  - `partnerId` {String} 商家向财付通申请的商家ID
  - `prepayId` {String} 预支付订单ID
  - `nonceStr` {String} 随机串
  - `timeStamp` {String} 时间戳
  - `package` {String} 商家根据财付通文档填写的数据和签名
  - `sign` {String} 商家根据微信开放平台文档对数据做的签名
- returns {Object}

Sends request for proceeding payment, then returns an object:

| name    | type   | description                         |
|---------|--------|-------------------------------------|
| errCode | Number | 0 if authorization successed        |
| errStr  | String | Error message if any error occurred |

## Installation

```sh
$ npm install react-native-wechat --save
```
## Partners

[React Native Starter Kit](https://reactnativestarter.com/) - is a mobile starter kit that allows your team to fully focus on development of the features that set your product apart from the competitors instead of building your app from scratch.

## Community

#### IRC

<a href="http://qm.qq.com/cgi-bin/qm/qr?k=cg3irEFCGxjkm2YJCt5V9OeJA1pNo5Ui"><img width="200" src="./qrcode_qq.jpg"></a>

#### Tutorials

- [react-native-wechat微信组件的使用](http://www.jianshu.com/p/3f424cccb888)
- [超详细React Native实现微信好友/朋友圈分享功能-Android/iOS双平台通用](http://www.jianshu.com/p/ce5439dd1f52)
- [柳轩涤俗 - 微信登录](http://www.cnblogs.com/zhangdw/p/6194345.html)

#### Who's using it

<a href="https://github.com/attentiveness/reading"><img height="80" width="80" src="https://raw.githubusercontent.com/attentiveness/reading/master/Reading_Logo.png"></a>
<a href="https://github.com/lipeiwei-szu/ReactNativeOne"><img height="80" width="80" src="http://android-artworks.25pp.com/fs08/2017/05/22/3/110_ed42e5c8f701ae26be6b0c423cb51858_con_130x130.png"></a>

## Authors

| GitHub        | Role       | Email                 |
|---------------|------------|-----------------------|
| [@yorkie]     | Author     | yorkiefixer@gmail.com |
| [@xing-zheng] | Emeriti    |                       |
| [@tdzl2003]   | Emeriti    | tdzl2003@gmail.com    |

[@yorkie]: https://github.com/yorkie
[@xing-zheng]: https://github.com/xing-zheng
[@tdzl2003]: https://github.com/tdzl2003

## License

MIT

[react-native-wechat]: https://github.com/yorkie/react-native-wechat
[npm-image]: https://img.shields.io/npm/v/react-native-wechat.svg?style=flat-square
[npm-url]: https://npmjs.org/package/react-native-wechat
[travis-image]: https://img.shields.io/travis/yorkie/react-native-wechat.svg?style=flat-square
[travis-url]: https://travis-ci.org/yorkie/react-native-wechat
[david-image]: http://img.shields.io/david/yorkie/react-native-wechat.svg?style=flat-square
[david-url]: https://david-dm.org/yorkie/react-native-wechat
[downloads-image]: http://img.shields.io/npm/dm/react-native-wechat.svg?style=flat-square
[downloads-url]: https://npmjs.org/package/react-native-wechat
[React Native]: https://github.com/facebook/react-native
[react-native-cn]: https://github.com/reactnativecn
[WeChat SDK]: https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=1417674108&token=&lang=zh_CN
[Linking Libraries iOS Guidance]: https://developer.apple.com/library/ios/recipes/xcode_help-project_editor/Articles/AddingaLibrarytoaTarget.html
