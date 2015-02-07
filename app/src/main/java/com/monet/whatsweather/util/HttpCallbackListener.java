package com.monet.whatsweather.util;

/**
 * Created by Monet on 2015/2/4.
 */
public interface HttpCallbackListener {
    void onFinish(String response);

    void onError(Exception e);
}
