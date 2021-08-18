# react-native-wechat ![Node.js CI](https://github.com/shm-open/react-native-wechat/workflows/Node.js%20CI/badge.svg)

[React Native] bridging library that integrates WeChat SDKs

本库是在 [react-native-wechat](https://github.com/yorkie/react-native-wechat) 基础上进行重写; 优化了类型定义，方法调用，并且使用`CocoaPods`和`gradle`来管理原生模块依赖的`wechat SDK`.

## Table of Contents

-   [Getting Started](#getting-started)
-   [API Documentation](#api-documentation)
-   [Installation](#installation)
-   [Community](#community)
-   [Authors](#authors)
-   [License](#license)

## Getting Started

`$ npm install @shm-open/react-native-wechat`

-   [Build setup on iOS](./docs/build-setup-ios.md)
-   [Build setup on Android](./docs/build-setup-android.md)

## API Documentation

[react-native-wechat] uses Promises, therefore you can use `Promise`
or `async/await` to manage your dataflow.

#### registerApp(appId, universalLink)

-   `appId` {String} the appId you get from WeChat dashboard
-   `universalLink` {String} the iOS universalLink setting
-   returns {Boolean} explains if your application is registered done

This method should be called once globally.

```js
import * as WeChat from 'react-native-wechat';

WeChat.registerApp('your wxid', 'your universal setting');
```

#### isWXAppInstalled()

-   returns {Boolean} if WeChat is installed.

Check if the WeChat app is installed on the device.

#### isWXAppSupportApi()

-   returns {Boolean} Contains the result.

Check if wechat support open url.

#### getApiVersion()

-   returns {String} Contains the result.

Get the WeChat SDK api version.

#### openWXApp()

-   returns {Boolean}

Open the WeChat app from your application.

#### sendAuthRequest(scope, state)

-   `scope` {String} Scopes of auth request.
    snsapi_userinfo or snsapi_base
-   `state` {String} 用于保持请求和回调的状态，授权请求后原样带回给第三方
-   returns {Object}

Send authentication request, and it returns an object with the
following fields:

| field   | type   | description                         |
| ------- | ------ | ----------------------------------- |
| errCode | Number | Error Code                          |
| errStr  | String | Error message if any error occurred |
| code    | String | Authorization code                  |
| state   | String | state_wx_login                      |

#### pay(payload)

-   `partnerId` {String} 商家向财付通申请的商家 id
-   `prepayId` {String} 预支付订单 ID
-   `nonceStr` {String} 随机串，防重发
-   `timeStamp` {String} 时间戳，防重发
-   `package` {String} 商家根据财付通文档填写的数据和签名
-   `sign` {String} 商家根据微信开放平台文档对数据做的签名
-   returns {Object}

Sends request for proceeding payment, then returns an object:

| name    | type   | description                         |
| ------- | ------ | ----------------------------------- |
| errCode | Number | 0 if pay successed                  |
| errStr  | String | Error message if any error occurred |

## Installation

```sh
$ npm install react-native-wechat --save
```

## License

MIT
