
require 'json'
pjson = JSON.parse(File.read('package.json'))

Pod::Spec.new do |s|

  s.name            = "xinstall-react-native"
  s.version         = "1.1.6"
  s.homepage        = "https://github.com/xinstall/Xinstall-React-Native-SDK"
  s.summary         = "xinstall"
  s.author          = "xinstall"

  s.ios.deployment_target = '9.0'

  s.source          = { :git => "https://github.com/xinstall/Xinstall-React-Native-SDK.git" }
  s.source_files    = 'ios/RNXinstall/*.{h,m}'
  s.preserve_paths  = "*.js"
  s.frameworks      = 'UIKit','Foundation'
  s.vendored_libraries = "ios/RNXinstall/*.a"

  s.dependency 'React'

end
