declare module "react-native-wechat" {
  export interface Response {
    errCode?: number;
    errStr?: string;
  }
  export interface AuthResponse extends Response {
    openId?: string;
    code?: string;
    url?: string;
    lang?: string;
    country?: string;
  }
  export interface ShareMetadata {
    type:
      | "news"
      | "text"
      | "imageUrl"
      | "imageFile"
      | "imageResource"
      | "video"
      | "audio"
      | "file"
      | "miniprogram";
    thumbImage?: string;
    description?: string;
    webpageUrl?: string;
    imageUrl?: string;
    videoUrl?: string;
    musicUrl?: string;
    filePath?: string;
    fileExtension?: string;
  }
  export interface PaymentLoad {
    partnerId: string;
    prepayId: string;
    nonceStr: string;
    timeStamp: string;
    package: string;
    sign: string;
  }
  export function registerApp(appId: string, universalLink?: string): Promise<boolean>;
  export function registerAppWithDescription(
    appId: string,
    desc: string
  ): Promise<boolean>;
  export function isWXAppInstalled(): Promise<boolean>;
  export function isWXAppSupportApi(): Promise<boolean>;
  export function getApiVersion(): Promise<string>;
  export function openWXApp(): Promise<boolean>;
  export function openMiniProgram(id: string, type?: number, path?: string): Promise<Response>;
  export function sendAuthRequest(scope: string | string[], state?: string): Promise<AuthResponse>;

  export function shareToTimeline(msg: ShareMetadata): Promise<Response>;
  export function shareToSession(msg: ShareMetadata): Promise<Response>;
  export function shareToMiniProgram(msg: ShareMetadata): Promise<Response>;
  
  export function pay(payload: PaymentLoad): Promise<Response>;
}
