#
#  Be sure to run `pod spec lint RCTWeChat.podspec' to ensure this is a
#  valid spec and to remove all comments including this before submitting the spec.
#
#  To learn more about Podspec attributes see http://docs.cocoapods.org/specification.html
#  To see working Podspecs in the CocoaPods repo see https://github.com/CocoaPods/Specs/
#

require 'json'
package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name         = "react-native-wechat"
  s.version      = package['version']
  s.summary      = "Wechat library"
  s.description  = package['description']
  s.author       = package['author']
  s.homepage     = package['homepage']
  s.license      = "MIT"
  s.platform     = :ios, "9.0"
  s.source       = { :git => "https://github.com/yorkie/react-native-wechat.git", :tag => "#{s.version}" }
  s.source_files  = "ios/*.{h,m}"
  s.dependency "React"
  s.dependency "WechatOpenSDK", '1.8.7.1'
  s.requires_arc = true
end
