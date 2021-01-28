# Build Setup for iOS

## Installation

```sh
$ cd your project iOS path
$ pod install
```

#### URL Schemes
Add "URL Scheme" as your wechat appId for "URL type" in Targets > info, See 
the following screenshot for the view on your XCode:

![Set URL Scheme in XCode](https://i.loli.net/2019/08/31/yUD2F5MrPKjngo3.jpg)

Cannot go back to APP from WeChat without configuration.  
如果不配置，就无法从微信重新回到APP。    


#### LSApplicationQueriesSchemes
On iOS 9+, add `wechat`、 `weixin` and `weixinULAPI` into `LSApplicationQueriesSchemes` in 
`Targets` > `info` > `Custom iOS Target Properties`. Or edit `Info.plist` 
then add:

```xml
<key>LSApplicationQueriesSchemes</key>
<array>
  <string>weixin</string>
  <string>wechat</string>
  <string>weixinULAPI</string>
</array>
```
If not configured, apple will prevent you from jumping to WeChat due to security permissions.  
如果不配置，因为安全权限问题，苹果会阻止你跳转到微信。  
</br>

#### AppDelegate
copy the following in `AppDelegate.m`:  

Wechat callback function, If not configured, When sharing is called, it appears "connecting" and then bounces back.  
微信回调方法，如果不配置，分享的时候微信会出现"正在连接"，然后直接弹回APP。

```objc
- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url
            options:(NSDictionary<NSString*, id> *)options
{
    return [RCTLinkingManager application:application openURL:url options:options];
}

- (BOOL)application:(UIApplication *)application continueUserActivity:(NSUserActivity *)userActivity restorationHandler:(void (^)(NSArray<id<UIUserActivityRestoring>> * _Nullable))restorationHandler {
    return [RCTLinkingManager application:application continueUserActivity:userActivity restorationHandler:restorationHandler];
}
```

#### Universal Links config
Universal Links 如果不配置，就无法从微信重新回到APP。<br>
在`选中project -> TARGETS -> Signing&Capabilities` 然后点击`+`添加一个`Associated Domains`, 在里面填写`Universal Links`配置的网址<br>

Apple Universal Link: [https://developer.apple.com/library/archive/documentation/General/Conceptual/AppSearch/UniversalLinks.html#//apple_ref/doc/uid/TP40016308-CH12-SW1](https://developer.apple.com/library/archive/documentation/General/Conceptual/AppSearch/UniversalLinks.html#//apple_ref/doc/uid/TP40016308-CH12-SW1)