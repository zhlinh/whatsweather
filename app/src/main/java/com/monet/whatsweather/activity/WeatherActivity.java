package com.monet.whatsweather.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.monet.whatsweather.R;
import com.monet.whatsweather.util.HttpCallbackListener;
import com.monet.whatsweather.util.HttpUtil;
import com.monet.whatsweather.util.Utility;

public class WeatherActivity extends Activity {

    private LinearLayout weatherInfoLayout;

    /**
     * 用于显示城市名
     */
    private TextView cityNameText;
    /**
     * 用于显示发布时间
     */
    private TextView publishText;
    /**
     * 用于显示天气描述信息
     */
    private TextView weatherDespText;
    /**
     * 用于显示气温Low
     */
    private TextView temper1Text;
    /**
     * 用于显示气温high
     */
    private TextView temper2Text;
    /**
     * 用于显示当前日期
     */
    private TextView currentDateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        //初始化各控件
        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText = (TextView) findViewById(R.id.city_name);
        publishText = (TextView) findViewById(R.id.publish_text);
        weatherDespText = (TextView) findViewById(R.id.weather_desp);
        temper1Text = (TextView) findViewById(R.id.temper1);
        temper2Text = (TextView) findViewById(R.id.temper2);
        currentDateText = (TextView) findViewById(R.id.current_date);
        String countryCode = getIntent().getStringExtra("country_code");
        if (!TextUtils.isEmpty(countryCode)) {
            //有县级代号就去查询天气
            publishText.setText("同步中...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherInfo(countryCode);
        }
        else {
            //没有县级代号时就直接显示本地天气
            showWeather();
        }
    }

    /**
     * 查询县级天气代号所对应的天气
     */
    private void queryWeatherInfo(String countryCode) {
        String address = "https://api.thinkpage.cn/v2/weather/future.json?city="
                + countryCode + "&language=zh-chs&unit=c&key=HAFSRXGF7B";
        queryFromServer(address, "countryCode");
    }

    /**
     * 根据传入的地址和类型区向服务器查询天气信息
     */
    private void queryFromServer(final String address, final String type) {
        HttpUtil.sendHttpRequest(address,new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if ("countryCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {
                        //处理从服务器返回的天气信息
                        Utility.handleWeatherResponse(WeatherActivity.this,response);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showWeather();
                            }
                        });
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败");
                    }
                });
            }
        });
    }

    /**
     * 从ShraedPreferences文件中读取存储的天气信息，并显示在界面上
     */
    private void showWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(prefs.getString("city_name", ""));
        temper1Text.setText(prefs.getString("temper1", "") + "℃");
        temper2Text.setText(prefs.getString("temper2", "") + "℃");
        weatherDespText.setText(prefs.getString("weather_desp", ""));
        publishText.setText("今天" + prefs.getString("publish_time", "") + "发布");
        currentDateText.setText(prefs.getString("current_date", ""));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
    }


}
