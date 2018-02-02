// Type definitions for react-native-wechat 1.9.9
// Project: https://github.com/yorkie/react-native-wechat
// Definitions by: acrazing <https://github.com/acrazing>
// TypeScript Version: 2.6

declare module 'react-native-wechat' {
  namespace wechat {
    export class WechatError extends Error {
      readonly name: 'WechatError'
      code: number
    }

    export interface AuthResponse {
      errCode: number;
      errStr: string;
      openId: string;
      code: string;
      url: string;
      lang: string;
      country: string;
    }

    export interface ShareMetadata {
      type: 'news' | 'text' | 'imageUrl' | 'imageFile' | 'imageResource' | 'video' | 'audio' | 'file';
      thumbImage?: string;
      description?: string;
      webpageUrl?: string;
      imageUrl?: string;
      videoUrl?: string;
      musicUrl?: string;
      filePath?: string;
      fileExtension?: string;
    }

    export interface ShareResponse {
      errCode: number;
      errStr: string;
    }

    export interface PayPayload {
      partnerId: string;
      prepayId: string;
      nonceStr: string;
      timeStamp: string;
      package: string;
      sign: string;
    }

    export interface PayResponse {
      errCode: number;
      errStr: string;
    }

    export function registerApp(appId: string): Promise<boolean>

    export function registerAppWithDescription(appId: string, description: string): Promise<boolean>

    export function isWXAppInstalled(): Promise<boolean>

    export function isWXAppSupportApi(): Promise<boolean>

    export function getApiVersion(): Promise<string>

    export function openWXApp(): Promise<boolean>

    export function sendAuthRequest(scope?: string | string[], state?: string): Promise<AuthResponse>

    export function shareToTimeline(message: ShareMetadata): Promise<ShareResponse>

    export function shareToSession(message: ShareMetadata): Promise<ShareResponse>

    export function pay(payload: PayPayload): Promise<PayResponse>
  }

  export = wechat
}
