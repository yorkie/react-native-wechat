declare module "react-native-wechat" {
  export function registerApp(appId: string): Promise<boolean>;
  export function registerAppWithDescription(
    appId: string,
    desc: string
  ): Promise<boolean>;
  export function isWXAppInstalled(): Promise<boolean>;
  export function isWXAppSupportApi(): Promise<boolean>;
  export function getApiVersion(): Promise<string>;
  export function openWXApp(): Promise<boolean>;
  export interface AuthResponse {
    errCode?: number;
    errStr?: string;
    openId?: string;
    code?: string;
    url?: string;
    lang?: string;
    country?: string;
  }
  export function sendAuthRequest(
    scope: string | string[],
    state?: string
  ): Promise<AuthResponse>;
  export interface ShareMetadata {
    type:
      | "news"
      | "text"
      | "imageUrl"
      | "imageFile"
      | "imageResource"
      | "video"
      | "audio"
      | "file";
    thumbImage?: string;
    title?: string;
    description?: string;
    webpageUrl?: string;
    imageUrl?: string;
    videoUrl?: string;
    musicUrl?: string;
    filePath?: string;
    fileExtension?: string;
  }
  export function shareToTimeline(
    message: ShareMetadata
  ): Promise<{ errCode?: number; errStr?: string }>;
  export function shareToSession(
    message: ShareMetadata
  ): Promise<{ errCode?: number; errStr?: string }>;
  export interface PaymentLoad {
    partnerId: string;
    prepayId: string;
    nonceStr: string;
    timeStamp: string;
    package: string;
    sign: string;
  }
  export function pay(
    payload: PaymentLoad
  ): Promise<{ errCode?: number; errStr?: string }>;
}
