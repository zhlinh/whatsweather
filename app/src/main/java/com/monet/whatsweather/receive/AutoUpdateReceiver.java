package com.monet.whatsweather.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.monet.whatsweather.service.AutoUpdateService;

/**
 * Created by Monet on 2015/3/11.
 */
public class AutoUpdateReceiver extends BroadcastReceiver {
    /**
     * 在onReceive方法中再次启动AutoUpdateService就可以实现后台定时更新功能了
     */

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, AutoUpdateService.class);
        context.startService(i);
    }
}
