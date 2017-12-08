require 'json'
pjson = JSON.parse(File.read('package.json'))

Pod::Spec.new do |s|

  s.name            = "RNFS"
  s.version         = pjson["version"]
  s.homepage        = "https://github.com/tianling536/react-native-wechat"
  s.summary         = pjson["description"]
  s.license         = pjson["license"]
  s.author          = { "Johannes Lumpe" => "johannes@lum.pe" }

  s.ios.deployment_target = '8.0'

  s.source          = { :git => "https://github.com/tianling536/react-native-wechat", :tag => "v#{s.version}" }
  s.source_files    = '*.{h,m}'
  s.preserve_paths  = "**/*.js"

  s.dependency 'React'

end