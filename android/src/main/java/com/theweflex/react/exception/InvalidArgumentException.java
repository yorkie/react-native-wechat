/*
 * Copyright (c) 2018. chenqiang Inc. All rights reserved.
 */

package com.theweflex.react.exception;

/**
 * 非法参数的 Exception
 *
 * @author chenqiang
 * @date 2018/8/9
 */
public class InvalidArgumentException extends Exception {
    public InvalidArgumentException() {
        super("invalid argument.");
    }
}
