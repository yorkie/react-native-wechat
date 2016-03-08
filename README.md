![react-native-wechat logo](./logo.jpg?raw=true)

React-Native bridge static library for WeChat SDK.

- [x] iOS
- [x] Android

## Join us at Gitter

[![Join the chat at https://gitter.im/weflex/react-native-wechat](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/weflex/react-native-wechat?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

## Installation

```sh
$ npm install react-native-wechat --save
```

## iOS: Linking in your XCode project

- Link `RCTWeChat` library from your `node_modules/react-native-wechat/ios` folder like its
  [described here](http://facebook.github.io/react-native/docs/linking-libraries-ios.html).
  Don't forget to add it to "Build Phases" of project.
- Added the following libraries to your "Link Binary With Libraries":
  - [x] SystemConfiguration.framework
  - [x] CoreTelephony.framework
  - [x] libsqlite3.0
  - [x] libc++
  - [x] libz
- add `URL Schema` as your app id for `URL type` in `Targets - info`
  ![Set URL Schema in XCode](https://res.wx.qq.com/open/zh_CN/htmledition/res/img/pic/app-access-guide/ios/image0042168b9.jpg)
- for iOS 9 support, add `wechat` and `weixin` into `LSApplicationQueriesSchemes` in 'Targets - info - Custom iOS Target Properties'

Note: Make sure you have these code in `AppDelegate.m` to enable [LinkingIOS](https://facebook.github.io/react-native/docs/linkingios.html#handling-deep-links)
```objective-c
- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url
  sourceApplication:(NSString *)sourceApplication annotation:(id)annotation
{
  return [RCTLinkingManager application:application openURL:url
                            sourceApplication:sourceApplication annotation:annotation];
}
```
## Android: Linking to your gradle Project

- Add following lines into `android/settings.gradle`

```
include ':RCTWeChat'
project(':RCTWeChat').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-wechat/android')
```

- Add following lines into your `android/app/build.gradle` in section `dependencies`

```
...
dependencies {
   ...
   compile project(':RCTWeChat')    // Add this line only.
}
```
- Add following lines into `MainActivity.java`

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
        new MainReactPackage()
        , new WeChatPackage()        // Add this line
    );
}
```

- Create a package named 'wxapi' in your application package and a class named 'WXEntryActivity' in it. This is needed to get request and response from wechat.

```java
package your.package.wxapi;

import android.app.Activity;
import android.os.Bundle;

import com.theweflex.react.WeChatModule;

public class WXEntryActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WeChatModule.handleIntent(getIntent());
        finish();
    }
}
```

- Add activity declare in your AndroidManifest.xml

```
<manifest>
  ...
  <application>
    ...
    <!-- 微信Activity -->
    <activity
      android:name=".wxapi.WXEntryActivity"
      android:label="@string/app_name"
      android:exported="true"
      />
  </application>
</manifest>
```

- Add these lines to 'proguard-rules.pro':

```
-keep class com.tencent.mm.sdk.** {
   *;
}
```

## API Documentation

#### registerApp(appid)

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

Send authentication request.

- {Array|String} `scope` Scopes of auth request.
- {String} `state` the state of OAuth2
- returns {Promise}

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

These example code need 'react-native-chat' and 'react-native-fs' plugin.
```js
import * as WeChat from 'react-native-wechat';
import fs from 'react-native-fs';
var resolveAssetSource = require('resolveAssetSource'); // along with Image component
// Code example to share text message:
try {
    var result = await  WeChat.shareToTimeline({type: 'text', description: 'I\'m Wechat, :)'});
    console.log('share text message to time line successful', result);
}
catch (e) {
    console.log('share text message to time line failed', e);
}

// Code example to share image url:
// Share raw http(s) image from web will always fail with unknown reason, please use image file or image resource instead
try {
    var result = await WeChat.shareToTimeline({
        type: 'imageUrl',
        title: 'web image',
        description: 'share web image to time line',
        mediaTagName: 'email signature',
        messageAction: undefined,
        messageExt: undefined,
        imageUrl: 'http://www.ncloud.hk/email-signature-262x100.png'
    });
    console.log('share image url to time line successful', result);
}
catch (e) {
    console.log('share image url to time line failed', e);
}

// Code example to share image file:
try {
    var rootPath = fs.DocumentDirectoryPath;
    var savePath = rootPath + '/email-signature-262x100.png'; // like /var/mobile/Containers/Data/Application/B1308E13-35F1-41AB-A20D-3117BE8EE8FE/Documents/email-signature-262x100.png

    await fs.downloadFile('http://www.ncloud.hk/email-signature-262x100.png', savePath);

    var result = await WeChat.shareToTimeline({
        type: 'imageFile',
        title: 'image file download from network',
        description: 'share image file to time line',
        mediaTagName: 'email signature',
        messageAction: undefined,
        messageExt: undefined,
        imageUrl: savePath
    });

    console.log('share image file to time line successful', result);
}
catch (e) {
    console.log('share image file to time line failed', e);
}

// Code example to share image resource:
try {
    var imageResource = require('./email-signature-262x100.png');
    var result = await WeChat.shareToTimeline({
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
    console.log('share resource image to time line failed', e);
}
```

#### shareToSession(data)

Similar to `shareToTimeline` but send message to a friend or a groups.

#### addListener(eventType, listener[, context])

Adds a listener to be invoked when events of the specified type are emitted. An optional calling context may be provided. 

Return a object like `{remove: function}` which can be used to remove this listener.

#### once(eventType, listener[, context])

Similar to addListener, except that the listener is removed after it is invoked once.

#### removeAllListeners()

Removes all of the registered listeners, including those registered as listener maps.

## Event Types:

#### SendAuth.Resp

Receive result for sendAuthRequest
    - errCode {int} 
    - errStr {String} Error message if any error occured.
    - openId {String} 
    - code {String} Authorize code
    - url {String}
    - lang {String}
    - country {String}

#### SendMessageToWX.Resp

Receive result for shareToTimeline and shareToSession
    - errCode {int} be 0 if auth successed.
    - errStr {String} Error message if any error occured.

For more details, visit [WeChat SDK Documentation](https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=1417674108&token=&lang=zh_CN)

## Authors

- [Yorkie Liu](https://github.com/yorkie) from [WeFlex](https://github.com/weflex)
- [Deng Yun](https://github.com/tdzl2003) from [React-Native-CN](https://github.com/reactnativecn)

## License

MIT @ WeFlex,Inc
