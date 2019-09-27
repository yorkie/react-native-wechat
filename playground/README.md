# Playground for react-native-wechat

Make sure you have the development environment set up with this [Tutorial](https://facebook.github.io/react-native/docs/getting-started) at first.

首先确保已经按[教程](https://facebook.github.io/react-native/docs/getting-started)配置好 React Native 本地开发环境。

## How to run

```sh
$ git clone this repo
$ cd playground
$ yarn install or npm install
```

Ensure emulator is ready or mobile device is connected to your machine:

运行前先开启模拟器或连接真机调试。

```sh
$ npm run start or react-native start --reset-cache
```

iOS:

```sh
$ cd ios && pod install
```

Add the following libraries to your "Link Binary with Libraries" in Targets > Build Phases in Xcode :

点击 Xcode 左侧 Targets > Build Phases 中的 Link Binary with Libraries 选项添加以下库：


- [x] `SystemConfiguration.framework`
- [x] `CoreTelephony.framework`
- [x] `libsqlite3.0`
- [x] `libc++`
- [x] `libz`

```sh
$ react-native run-ios
```

Android:

```sh
$ react-native run-android
```
