/*
 * Copyright (c) 2018. chenqiang Inc. All rights reserved.
 */

package com.theweflex.react;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.comori.tools.databus.DataBus;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.theweflex.react.exception.NotRegisterException;

/**
 * @author chenqiang
 * @date 2018/8/9
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    IWXAPI wxapi;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            wxapi = WXAPIFactory.createWXAPI(this, WXPresenter.getAppId());
            wxapi.handleIntent(getIntent(), this);
        } catch (NotRegisterException ignore) {
        }finally {
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        wxapi.handleIntent(intent, this);
        finish();
    }

    @Override
    public void onReq(BaseReq baseReq) {
    }

    @Override
    public void onResp(BaseResp baseResp) {
        DataBus.get().with(WeChatModule.KEY_WX_RESULT).post(baseResp);
    }

}
