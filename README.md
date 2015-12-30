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
  - [x] CoreTelephony.framework
  - [x] libsqlite3.0
  - [x] libc++
  - [x] libz

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
...
import com.theweflex.react.WeChatPackage;       // Add this line before public class MainActivity

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ...
        mReactInstanceManager = ReactInstanceManager.builder()
            .setApplication(getApplication())
            .setBundleAssetName("index.android.bundle")
            .setJSMainModuleName("index.android")
            .addPackage(new MainReactPackage())
            .addPackage(new WeChatPackage())        // Add this line
            .setUseDeveloperSupport(BuildConfig.DEBUG)
            .setInitialLifecycleState(LifecycleState.RESUMED)
            .build();
    }
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
    - {String} `type` Type of this message. Can be {news|text|image|video|audio|file}
    - {String} `webpageUrl` Required if type equals `news`. The webpage link to share.
    - {String} `imageUrl` Provide a remote image if type equals `image`.
    - {String} `videoUrl` Provide a remote video if type equals `video`.
    - {String} `musicUrl` Provide a remote music if type equals `audio`.
    - {String} `filePath` Provide a local file if type equals `file`.

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
