/*
 * Copyright (c) 2018. chenqiang Inc. All rights reserved.
 */

package com.theweflex.react.exception;

/**
 * 未注册的 Exception
 *
 * @author chenqiang
 * @date 2018/8/9
 */
public class NotRegisterException extends Exception {

    public NotRegisterException() {
        super("registerApp required.");
    }
}
