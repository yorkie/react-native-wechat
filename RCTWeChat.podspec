#
#  Be sure to run `pod spec lint RCTWeChat.podspec' to ensure this is a
#  valid spec and to remove all comments including this before submitting the spec.
#
#  To learn more about Podspec attributes see http://docs.cocoapods.org/specification.html
#  To see working Podspecs in the CocoaPods repo see https://github.com/CocoaPods/Specs/
#

Pod::Spec.new do |s|
  s.name         = "RCTWeChat"
  s.version      = "1.10.0"
  s.summary      = "React-Native(iOS/Android) functionalities include WeChat Login, Share, Favorite and Payment {QQ: 336021910}"
  s.description  = <<-DESC
  React-Native(iOS/Android) functionalities include WeChat Login, Share, Favorite and Payment {QQ: 336021910}
   DESC
  s.author       = { "yorkie" => "yorkiefixer@gmail.com" }
  s.homepage     = "https://github.com/yorkie/react-native-wechat"
  s.license      = "MIT"
  s.platform     = :ios, "9.0"
  s.source       = { :git => "https://github.com/yorkie/react-native-wechat.git", :tag => "master" }
  
  s.source_files  = "ios/*.{h,m}"
  
  s.dependency "React"
  s.dependency "WechatOpenSDK", "1.8.4"
  s.requires_arc = true
end
