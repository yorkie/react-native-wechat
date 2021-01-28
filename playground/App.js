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
    Alert,
} from 'react-native';

import { Header, Colors } from 'react-native/Libraries/NewAppScreen';

import * as Wechat from 'react-native-wechat';
import * as WechatType from 'react-native-wechat/lib/wechatInterface';

export default class App extends Component {
    shareOptions = {
        title: 'playground',
        description: '微信分享测试',
        thumbImageUrl: 'https://i.loli.net/2019/09/03/62FauzAY37gsEXV.png',
        type: 'news',
        webpageUrl: 'https://github.com/yorkie/react-native-wechat',
    };
    constructor(props) {
        super(props);
        this.state = {
            apiVersion: null,
            isWXAppInstalled: false,
            wxAppInstallUrl: null,
            isWXAppSupportApi: false,
        };
    }
    handleOpenApp() {
        if (this.state.isWXAppInstalled) {
            return Wechat.openWXApp();
        } else {
            Alert.alert('没有安装微信，请安装之后重试');
        }
    }
    handleWechatAuth() {
        Wechat.sendAuthRequest('snsapi_userinfo', 'state_wx_login').then((data) => {
            Alert.alert(data);
        });
    }
    handleWechatPay() {
        const payData = {
            partnerId: 'string',
            prepayId: 'string',
            nonceStr: 'string',
            timeStamp: 'string',
            package: 'string',
            sign: 'string',
        };
        Wechat.pay(payData)
            .then((data) => {
                Alert.alert(data);
            })
            .catch((error) => {
                Alert.alert(error);
            });
    }
    handleShareText() {
        if (this.state.isWXAppInstalled) {
            const shareData = {
                text: 'share text',
                scene: WechatType.WechatSceneEnum.SESSION,
            };
            Wechat.shareText(shareData)
                .then((data) => {
                    Alert.alert(data);
                })
                .catch((error) => {
                    Alert.alert(error);
                });
        } else {
            Alert.alert('没有安装微信，请安装之后重试');
        }
    }
    handleShareImage() {
        if (this.state.isWXAppInstalled) {
            const shareData = {
                imageUrl: 'https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png',
                scene: WechatType.WechatSceneEnum.SESSION,
            };
            Wechat.shareImage(shareData).catch((error) => {
                Alert.alert(error);
            });
        } else {
            Alert.alert('没有安装微信，请安装之后重试');
        }
    }
    handleShareWebPage() {
        const shareData = {
            title: '百度',
            description: '百度一下,你就知道',
            webpageUrl: 'https://www.baidu.com',
            thumbImageUrl: 'https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png',
            scene: WechatType.WechatSceneEnum.SESSION,
        };
        Wechat.shareWebpage(shareData).catch((error) => {
            Alert.alert(error);
        });
    }
    handleOpenMiniprogram() {
        const shareData = {
            userName: 'gh_b5158a3849b4',
            path: '/pages/webview/index?url=',
            miniprogramType: WechatType.WechatMiniprogramTypeEnum.RELEASE,
        };
        Wechat.openMiniprogram(shareData).catch((error) => {
            Alert.alert(error);
        });
    }
    handleShareMiniprogram() {
        const shareData = {
            title: '分享小程序',
            description: '分享小程序description',
            webpageUrl: 'https://www.baidu.com',
            thumbImageUrl: 'https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png',
            miniprogram: {
                userName: 'gh_b5158a3849b4',
                path: '/pages/webview/index?url=',
                miniProgramType: WechatType.WechatMiniprogramTypeEnum.RELEASE,
            },
        };
        Wechat.shareMiniprogram(shareData).catch((error) => {
            Alert.alert(error);
        });
    }
    handleShareVideo() {
        const shareData = {
            title: '分享Video',
            description: '分享description',
            videoUrl: 'http://music.163.com/song/media/outer/url?id=447925558.mp3',
            thumbImageUrl: 'https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png',
            scene: WechatType.WechatSceneEnum.SESSION,
        };
        Wechat.shareVideo(shareData)
            .then((data) => {
                Alert.alert(data);
            })
            .catch((error) => {
                Alert.alert(error);
            });
    }
    handleShareMusic() {
        const shareData = {
            title: '分享Music',
            description: '分享description',
            musicUrl: 'http://music.163.com/song/media/outer/url?id=447925558.mp3',
            thumbImageUrl: 'https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png',
            scene: WechatType.WechatSceneEnum.SESSION,
        };
        Wechat.shareMusic(shareData)
            .then((data) => {
                Alert.alert(data);
            })
            .catch((error) => {
                Alert.alert(error);
            });
    }
    componentDidMount() {
        try {
            Wechat.registerApp('your wxid', 'your universal link'); // Replace with your AppID
            Wechat.isWXAppInstalled().then((data) => {
                this.setState({
                    isWXAppInstalled: data,
                });
            });
            Wechat.isWXAppSupportApi((error, data) => {
                if (!error) {
                    this.setState({
                        isWXAppSupportApi: data,
                    });
                }
            });
            if (Platform.OS === 'ios') {
                Wechat.getApiVersion().then((data) => {
                    this.setState({
                        apiVersion: data,
                    });
                });
                Wechat.getWXAppInstallUrl().then((data) => {
                    this.setState({
                        getWXAppInstallUrl: data,
                    });
                });
            }
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
                        style={styles.scrollView}
                    >
                        <Header />
                        <View style={styles.body}>
                            <View style={styles.sectionContainer}>
                                <Text style={styles.highlight}>
                                    ApiVersion: <Text>{apiVersion}</Text>
                                </Text>
                                <TouchableOpacity
                                    style={styles.button}
                                    onPress={() => this.handleOpenApp()}
                                >
                                    <Text>打开微信</Text>
                                </TouchableOpacity>
                                <TouchableOpacity
                                    style={styles.button}
                                    onPress={() => this.handleWechatAuth()}
                                >
                                    <Text>微信登录授权</Text>
                                </TouchableOpacity>
                                <TouchableOpacity
                                    style={styles.button}
                                    onPress={() => this.handleWechatPay()}
                                >
                                    <Text>微信支付</Text>
                                </TouchableOpacity>
                                <TouchableOpacity
                                    style={styles.button}
                                    onPress={() => this.handleShareText()}
                                >
                                    <Text>分享文本</Text>
                                </TouchableOpacity>
                                <TouchableOpacity
                                    style={styles.button}
                                    onPress={() => this.handleShareImage()}
                                >
                                    <Text>分享图片</Text>
                                </TouchableOpacity>
                                <TouchableOpacity
                                    style={styles.button}
                                    onPress={() => this.handleShareWebPage()}
                                >
                                    <Text>分享网页</Text>
                                </TouchableOpacity>
                                <TouchableOpacity
                                    style={styles.button}
                                    onPress={() => this.handleShareVideo()}
                                >
                                    <Text>分享Video</Text>
                                </TouchableOpacity>
                                <TouchableOpacity
                                    style={styles.button}
                                    onPress={() => this.handleShareMusic()}
                                >
                                    <Text>分享Music</Text>
                                </TouchableOpacity>
                                <TouchableOpacity
                                    style={styles.button}
                                    onPress={() => this.handleOpenMiniprogram()}
                                >
                                    <Text>打开小程序</Text>
                                </TouchableOpacity>
                                <TouchableOpacity
                                    style={styles.button}
                                    onPress={() => this.handleShareMiniprogram()}
                                >
                                    <Text>分享小程序</Text>
                                </TouchableOpacity>
                            </View>
                        </View>
                    </ScrollView>
                </SafeAreaView>
            </Fragment>
        );
    }
}

const styles = StyleSheet.create({
    scrollView: {
        width: '100%',
        height: '100%',
        backgroundColor: Colors.lighter,
    },
    body: {
        width: '100%',
        height: '100%',
        backgroundColor: Colors.white,
    },
    sectionContainer: {
        marginTop: 32,
        paddingHorizontal: 24,
    },
    highlight: {
        fontSize: 24,
        fontWeight: '600',
        color: Colors.black,
        textAlign: 'center',
    },
    button: {
        flex: 1,
        margin: 10,
        borderWidth: 1,
        borderRadius: 30,
        paddingVertical: 12,
        alignItems: 'center',
        justifyContent: 'center',
        borderColor: Colors.black,
    },
});
