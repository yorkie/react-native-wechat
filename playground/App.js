/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */

import React, { Fragment, Component } from 'react';
import {
  TouchableOpacity,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  StatusBar,
  Platform,
  View,
  Text,
  Alert
} from 'react-native';

import {
  Header,
  Colors
} from 'react-native/Libraries/NewAppScreen';

import * as WeChat from 'react-native-wechat';

export default class App extends Component {
  shareOptions = {
    title: 'playground',
    description: '微信分享测试',
    thumbImage: 'https://i.loli.net/2019/09/03/62FauzAY37gsEXV.png',
    type: 'news',
    webpageUrl: 'https://github.com/yorkie/react-native-wechat'
  }
  constructor(props) {
    super(props);
    this.state = {
      apiVersion: null,
      isWXAppInstalled: false,
      wxAppInstallUrl: null,
      isWXAppSupportApi: false
    }
  }
  handleOpenApp () {
    if (this.state.isWXAppInstalled) {
      return WeChat.openWXApp();
    } else {
      Alert.alert('没有安装微信，请安装之后重试');
    }
  }
  handleShareToSession () {
    if (this.state.isWXAppInstalled) {
      WeChat.shareToSession(this.shareOptions).catch((error) => {
        Alert.alert(error.message);
      });
    } else {
      Alert.alert('没有安装微信，请安装之后重试');
    }
  }
  handleShareToMoment () {
    if (this.state.isWXAppInstalled) {
      WeChat.shareToTimeline(this.shareOptions).catch((error) => {
        Alert.alert(error.message);
      });
    } else {
      Alert.alert('没有安装微信，请安装之后重试');
    }
  }
  async componentDidMount() {
    try {
      WeChat.registerApp('your wexin AppID'); // Replace with your AppID
      this.setState({
        apiVersion: await WeChat.getApiVersion(),
        wxAppInstallUrl: Platform.OS === 'ios' ? await WeChat.getWXAppInstallUrl(): null,
        isWXAppSupportApi: await WeChat.isWXAppSupportApi(),
        isWXAppInstalled: await WeChat.isWXAppInstalled()
      });
    } catch (e) {
      console.error(e);
    }
  }
  render() {
    const { apiVersion } = this.state;
    return (
      <Fragment>
        <StatusBar barStyle="dark-content" />
        <SafeAreaView>
          <ScrollView
            contentInsetAdjustmentBehavior="automatic"
            style={styles.scrollView}>
            <Header />
            <View style={styles.body}>
              <View style={styles.sectionContainer}>
                <Text style={styles.highlight}>
                  ApiVersion: <Text>{ apiVersion }</Text>
                </Text>
                <TouchableOpacity style={styles.button} onPress={() => this.handleOpenApp()}>
                  <Text>打开微信</Text>
                </TouchableOpacity>
                <TouchableOpacity style={styles.button} onPress={() => this.handleShareToSession()}>
                  <Text>分享至微信好友</Text>
                </TouchableOpacity>
                <TouchableOpacity style={styles.button} onPress={() => this.handleShareToMoment()}>
                  <Text>分享至朋友圈</Text>
                </TouchableOpacity>
              </View>
            </View>
          </ScrollView>
        </SafeAreaView>
      </Fragment>
    );
  }
};

const styles = StyleSheet.create({
  scrollView: {
    backgroundColor: Colors.lighter
  },
  body: {
    backgroundColor: Colors.white
  },
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24
  },
  highlight: {
    fontSize: 24,
    fontWeight: '600',
    color: Colors.black,
    textAlign: 'center'
  },
  button: {
    flex: 1,
    margin: 10,
    borderWidth: 1,
    borderRadius: 30,
    paddingVertical: 12,
    alignItems: 'center',
    justifyContent: 'center',
    borderColor: Colors.black
  }
});
