重要!
SDK1.7
1. 支持兼容ipv6
2. 修复若干问题增强稳定性

SDK1.6.3
1. xCode7.2 构建的sdk包。
2. 请使用xCode7.2进行编译。
3. 需要在Build Phases中Link  Security.framework
4. 修复若干小问题。

SDK1.6.2
1、xCode7.1 构建的sdk包
2、请使用xCode7.1进行编译

SDK1.6.1
1、修复armv7s下,bitcode可能编译不过
2、解决warning

SDK1.6
1、iOS 9系统策略更新，限制了http协议的访问，此外应用需要在“Info.plist”中将要使用的URL Schemes列为白名单，才可正常检查其他应用是否安装。
受此影响，当你的应用在iOS 9中需要使用微信SDK的相关能力（分享、收藏、支付、登录等）时，需要在“Info.plist”里增加如下代码：
<key>LSApplicationQueriesSchemes</key>
<array>
<string>weixin</string>
</array>
<key>NSAppTransportSecurity</key>
<dict>
<key>NSAllowsArbitraryLoads</key>
<true/>
</dict>
2、开发者需要在工程中链接上 CoreTelephony.framework
3、解决bitcode编译不过问题

SDK1.5
1、废弃safeSendReq:接口，使用sendReq:即可。
2、新增+(BOOL) sendAuthReq:(SendAuthReq*) req viewController : (UIViewController*) viewController delegate:(id<WXApiDelegate>) delegate;
支持未安装微信情况下Auth,具体见WXApi.h接口描述
3、微信开放平台新增了微信模块用户统计功能，便于开发者统计微信功能模块的用户使用和活跃情况。开发者需要在工程中链接上:SystemConfiguration.framework,libz.dylib,libsqlite3.0.dylib。