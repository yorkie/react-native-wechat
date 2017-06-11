![react-native-wechat logo](./logo.jpg?raw=true)

## Table of Contents

- [Build](#build)
- [Linking Steps](#linking-steps)
  - [Linking iOS](#linking-ios)
  - [Linking Android with Gradle](#linking-android-with-gradle)
- [API Documentation](#api-documentation)
  - [`registerApp(appid)`](#registerappappid)
  - [`registerAppWithDescription(appid, appdesc)`](#registerappappid)
  - [`isWXAppInstalled()`](#iswxappinstalled)
  - [`isWXAppSupportApi()`](#iswxappsupportapi)
  - [`getApiVersion()`](#iswxappsupportapi)
  - [`openWXApp()`](#openwxapp)
  - [`sendAuthRequest([scope[, state]])`](#sendauthrequestscope-state)
  - [`shareToTimeline(data)`](#sharetotimelinedata)
  - [`shareToSession(data)`](#sharetosessiondata)
  - [`pay(data)`](#paydata)
  - [`addListener(eventType, listener[, context])`](#addlistenereventtype-listener-context)
  - [`once(eventType, listener[, context])`](#onceeventtype-listener-context)
  - [`removeAllListeners()`](#removealllisteners)
- [Installation](#installation)
- [Community](#community)
- [Who Use It](#who-use-it)
- [Authors](#authors)
- [License](#license)

## Build

React-Native bridge static library for WeChat SDK which requires:

- [x] iOS SDK 1.7.2
- [x] Android SDK 221

And [react-native-wechat] has the following tracking data in open source world:

| type        | badge                                           |
|-------------|-------------------------------------------------|
| NPM         | [![NPM version][npm-image]][npm-url]            |
| Dependency  | [![Dependency Status][david-image]][david-url]  |
| Downloads   | [![Downloads][downloads-image]][downloads-url]  |

## Linking Steps

Before using this library to work with your app, you should follow the below steps to link this library with
your app project, _if there is something that not working, please check the list here_.

### Linking iOS

- Link `RCTWeChat` library from your `node_modules/react-native-wechat/ios` folder like react-native's 
[Linking Libraries iOS Guidance], Note: _Don't forget to add it to "Build Phases" of your target project_.

- Add the following libraries to your "Link Binary with Libraries":

    ```
    SystemConfiguration.framework
    CoreTelephony.framework
    libsqlite3.0
    libc++
    libz
    ```

- Add "URL Schema" as your app id for "URL type" in `Targets` > `info`, See the following screenshot for the view on your XCode
    ![Set URL Schema in XCode](https://res.wx.qq.com/open/zh_CN/htmledition/res/img/pic/app-access-guide/ios/image0042168b9.jpg)

- Only for iOS 9, add `wechat` and `weixin` into `LSApplicationQueriesSchemes` in `Targets` > `info` > `Custom iOS Target Properties`. Or edit `Info.plist` then add:

    ```
      <key>LSApplicationQueriesSchemes</key>
      <array>
        <string>weixin</string>
        <string>wechat</string>
      </array>
    ```


- Code the following in `AppDelegate.m` of your project to enable [LinkingIOS]

    ```objective-c
    #import "../Libraries/LinkingIOS/RCTLinkingManager.h"
    
    - (BOOL)application:(UIApplication *)application openURL:(NSURL *)url
    sourceApplication:(NSString *)sourceApplication annotation:(id)annotation
    {
      return [RCTLinkingManager application:application openURL:url
                                sourceApplication:sourceApplication annotation:annotation];
    }
    ```

### Linking Android with Gradle

- Add following lines into `android/settings.gradle`

    ```gradle
    include ':RCTWeChat'
    project(':RCTWeChat').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-wechat/android')
    ```

- Add following lines into your `android/app/build.gradle` in section `dependencies`

    ```gradle
    dependencies {
      compile project(':RCTWeChat')    // Add this line only.
    }
    ```

- Add following lines into `MainActivity.java` or `MainApplication.java`:

    ```java
    import com.theweflex.react.WeChatPackage;       // Add this line before public class MainActivity
    ...

    /**
     * A list of packages used by the app. If the app uses additional views
     * or modules besides the default ones, add more packages here.
     */
    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
        new MainReactPackage(), 
        new WeChatPackage()        // Add this line
      );
    }
    ```

- Create a package named 'wxapi' in your application package and a class named 'WXEntryActivity' in it. 
  This is required to get authorization and sharing response from wechat.

    ```java
    package your.package.wxapi;

    import android.app.Activity;
    import android.os.Bundle;
    import com.theweflex.react.WeChatModule;

    public class WXEntryActivity extends Activity {
      @Override
      protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WeChatModule.handleIntent(getIntent());
        finish();
      }
    }
    ```

- (Optional) Create a package named 'wxapi' in your application package and a class named 'WXPayEntryActivity'
  in it. This is required to get payment response from WeChat.

    ```java
    package your.package.wxapi;

    import android.app.Activity;
    import android.os.Bundle;
    import com.theweflex.react.WeChatModule;

    public class WXPayEntryActivity extends Activity {
      @Override
      protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WeChatModule.handleIntent(getIntent());
        finish();
      }
    }
    ```

- Add activity declare in your AndroidManifest.xml

    ```xml
    <manifest>
      <application>
        <activity
          android:name=".wxapi.WXEntryActivity"
          android:label="@string/app_name"
          android:exported="true"
        />
        <activity
          android:name=".wxapi.WXPayEntryActivity"
          android:label="@string/app_name"
          android:exported="true"
        />
      </application>
    </manifest>
    ```

- Add these lines to 'proguard-rules.pro':

    ```pro
    -keep class com.tencent.mm.sdk.** {
       *;
    }
    ```

## API Documentation

[react-native-wechat] supports the following methods to get information and do something functions
with WeChat app.

#### registerApp(appid)

You should call this function in global, calling over twice would throw an error.

  ```js
  // If you register here
  componentDidMount (){
    wechat.registerApp('your appid')
  }
  ```

- {String} `appid` the appid you get from WeChat dashboard
- returns {Promise} 

#### registerAppWithDescription(appid, appdesc)

Only available on iOS.

- {String} `appid` the appid you get from WeChat dashboard
- {String} `appdesc` the description of your app
- returns {Promise} 

#### isWXAppInstalled() 

Check if wechat installed in this app.

- returns {Promise} Contain the result.

#### isWXAppSupportApi()

Check if wechat support open url.

- returns {Promise}  Contain the result.

#### getApiVersion()

Get api version of WeChat SDK.

- returns {Promise}  Contain the result.

#### openWXApp()

Open WeChat app with an optional callback argument.

- returns {Promise} 

#### sendAuthRequest([scope[, state]])

Send authentication request, namely login.

- {Array|String} `scope` Scopes of auth request.
- {String} `state` the state of OAuth2
- returns {Promise}

And it returns:

| name    | type   | description                         |
|---------|--------|-------------------------------------|
| errCode | Number |                                     |
| errStr  | String | Error message if any error occurred |
| openId  | String |                                     |
| code    | String | Authorization code                  |
| url     | String | The URL string                      |
| lang    | String | The user language                   | 
| country | String | The user country                    |

#### shareToTimeline(data)

Share a message to timeline (朋友圈).

- {Object} `data` contain the message to send
    - {String} `thumbImage` Thumb image of the message, which can be a uri or a resource id.
    - {String} `type` Type of this message. Can be {news|text|imageUrl|imageFile|imageResource|video|audio|file}
    - {String} `webpageUrl` Required if type equals `news`. The webpage link to share.
    - {String} `imageUrl` Provide a remote image if type equals `image`.
    - {String} `videoUrl` Provide a remote video if type equals `video`.
    - {String} `musicUrl` Provide a remote music if type equals `audio`.
    - {String} `filePath` Provide a local file if type equals `file`.
    - {String} `fileExtension` Provide the file type if type equals `file`.

And returns:

| name    | type   | description                         |
|---------|--------|-------------------------------------|
| errCode | Number | 0 if authorization successed        |
| errStr  | String | Error message if any error occurred |

These example code need 'react-native-chat' and 'react-native-fs' plugin.

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

#### shareToSession(data)

Similar to `shareToTimeline` but send message to a friend or a groups.

#### pay(data)

Create a request to proceeding payment.

```js
const result = await WeChat.pay(
  {
    partnerId: '',  // 商家向财付通申请的商家id
    prepayId: '',   // 预支付订单
    nonceStr: '',   // 随机串，防重发
    timeStamp: '',  // 时间戳，防重发
    package: '',    // 商家根据财付通文档填写的数据和签名
    sign: ''        // 商家根据微信开放平台文档对数据做的签名
  }
);
```

It returns an object like this:

| name    | type   | description                         |
|---------|--------|-------------------------------------|
| errCode | Number | 0 if authorization successed        |
| errStr  | String | Error message if any error occurred |

#### addListener(eventType, listener[, context])

Adds a listener to be invoked when events of the specified type are emitted. An optional calling context may be provided. 

Return a object like `{remove: function}` which can be used to remove this listener.

#### once(eventType, listener[, context])

Similar to addListener, except that the listener is removed after it is invoked once.

#### removeAllListeners()

Removes all of the registered listeners, including those registered as listener maps.

## Installation

```sh
$ npm install react-native-wechat --save
```

## Community

- [Join us at gitter](https://gitter.im/weflex/react-native-wechat?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
- QQ Group: 336021910

## Who Use It
- [reading: iReading App Write In React-Native](https://github.com/attentiveness/reading)

## Authors

- [Deng Yun]
- [Xing Zhen]
- [Yorkie Liu]

## License

MIT @ [WeFlex], Inc

[react-native-wechat]: https://github.com/weflex/react-native-wechat
[npm-image]: https://img.shields.io/npm/v/react-native-wechat.svg?style=flat-square
[npm-url]: https://npmjs.org/package/react-native-wechat
[travis-image]: https://img.shields.io/travis/weflex/react-native-wechat.svg?style=flat-square
[travis-url]: https://travis-ci.org/weflex/react-native-wechat
[david-image]: http://img.shields.io/david/weflex/react-native-wechat.svg?style=flat-square
[david-url]: https://david-dm.org/weflex/react-native-wechat
[downloads-image]: http://img.shields.io/npm/dm/react-native-wechat.svg?style=flat-square
[downloads-url]: https://npmjs.org/package/react-native-wechat
[Deng Yun]: https://github.com/tdzl2003
[Xing Zhen]: https://github.com/xing-zheng
[Yorkie Liu]: https://github.com/yorkie
[WeFlex]: https://github.com/weflex
[react-native-cn]: https://github.com/reactnativecn
[WeChat SDK]: https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=1417674108&token=&lang=zh_CN
[Linking Libraries iOS Guidance]:
https://developer.apple.com/library/ios/recipes/xcode_help-project_editor/Articles/AddingaLibrarytoaTarget.html
