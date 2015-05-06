package com.yunarta.hackernews.api;

/**
 * Created by yunarta on 7/5/15.
 */
public class RestAPIException extends Exception {

    public RestAPIException() {
    }

    public RestAPIException(String detailMessage) {
        super(detailMessage);
    }

    public RestAPIException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public RestAPIException(Throwable throwable) {
        super(throwable);
    }
}
