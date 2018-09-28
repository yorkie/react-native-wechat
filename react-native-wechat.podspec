#
#  Be sure to run `pod spec lint react-native-qq.podspec' to ensure this is a
#  valid spec and to remove all comments including this before submitting the spec.
#
#  To learn more about Podspec attributes see http://docs.cocoapods.org/specification.html
#  To see working Podspecs in the CocoaPods repo see https://github.com/CocoaPods/Specs/
#

Pod::Spec.new do |s|
  s.name         = "react-native-wechat"
  s.version      = "1.9.10"
  s.summary      = "React-Native(iOS/Android) functionalities include WeChat Login, Share, Favorite and Payment {QQ: 336021910}"
  s.description  = <<-DESC
  React-Native(iOS/Android) functionalities include WeChat Login, Share, Favorite and Payment {QQ: 336021910}
   DESC
  s.author       = { "weflex" => "336021910@qq.com" }
  s.homepage     = "https://github.com/weflex/react-native-wechat"
  s.license      = "MIT"
  s.platform     = :ios, "9.0"
  s.source       = { :git => "https://github.com/weflex/react-native-wechat.git", :tag => "master" }
  s.source_files  = "ios/*.{h,m}"
  s.dependency "React"
  s.vendored_libraries = "ios/libWeChatSDK.a"
  s.requires_arc = true
  s.frameworks = 'SystemConfiguration','CoreTelephony'
  s.library = 'sqlite3','c++','z'
end
