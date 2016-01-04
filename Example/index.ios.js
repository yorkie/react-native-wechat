/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 */
'use strict';

var React = require('react-native');
var {
  AppRegistry,
  StyleSheet,
  Text,
  View,
} = React;
var WeChat = require('../index');

var Example = React.createClass({
  render: function() {
    console.log(WeChat);
    console.log('getApiVersion', typeof WeChat.getApiVersion);
    console.log('getWXAppInstallUrl', typeof WeChat.getWXAppInstallUrl);
    console.log('sendRequest', typeof WeChat.sendRequest);
    console.log('registerApp', typeof WeChat.registerApp);
    console.log('sendErrorCommonResponse', typeof WeChat.sendErrorCommonResponse);
    console.log('sendErrorUserCancelResponse', typeof WeChat.sendErrorUserCancelResponse);
    console.log('sendAuthRequest', typeof WeChat.sendAuthRequest);
    console.log('getWXAppInstallUrl', typeof WeChat.getWXAppInstallUrl);
    console.log('openWXApp', typeof WeChat.openWXApp);
    console.log('registerAppWithDescription', typeof WeChat.registerAppWithDescription);
    console.log('isWXAppSupportApi', typeof WeChat.isWXAppSupportApi);
    console.log('isWXAppInstalled', typeof WeChat.isWXAppInstalled);
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          Please go to debugger by Cmd+D
        </Text>
      </View>
    );
  }
});

var styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});

AppRegistry.registerComponent('Example', () => Example);
