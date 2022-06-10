# react-native-wechat.podspec

require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-wechat"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = <<-DESC
                  react-native-wechat
                   DESC
  s.homepage     = "https://github.com/472647301/react-native-wechat"
  # brief license entry:
  s.license      = "MIT"
  # optional - use expanded license entry instead:
  # s.license    = { :type => "MIT", :file => "LICENSE" }
  s.authors      = { "Byron" => "byron.zhuwenbo@gmail.com" }
  s.platforms    = { :ios => "9.0" }
  s.source       = { :git => "https://github.com/472647301/react-native-wechat.git", :tag => "#{s.version}" }

  s.vendored_libraries = "ios/OpenSDK1.9.2_NoPay/libWeChatSDK.a"
  s.source_files = "ios/**/*.{h,c,cc,cpp,m,mm,swift}"
  s.requires_arc = true

  s.dependency "React"
  # ...
end

