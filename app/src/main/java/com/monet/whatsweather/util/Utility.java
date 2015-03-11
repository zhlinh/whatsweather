package com.monet.whatsweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.monet.whatsweather.db.WhatsWeatherDB;
import com.monet.whatsweather.model.City;
import com.monet.whatsweather.model.Country;
import com.monet.whatsweather.model.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Monet on 2015/2/4.
 */
public class Utility {
    /**
     * 解析和处理服务器返回的省级数据，
     */
    public synchronized static boolean handleProvincesResponse(WhatsWeatherDB whatsWeatherDB,
                                                               String response) {
        if (!TextUtils.isEmpty(response)) {
//            需要把首尾的{}去掉，故用substring
            String[] allProvince = response.substring(1,response.length()-1).split(",");
            if (allProvince != null && allProvince.length > 0) {
                for (String p : allProvince) {
                    String[] array = p.split(":");
                    Province province = new Province();
//                    需要把首尾的""去掉，故用substring
                    province.setProvinceCode(array[0].substring(1, array[0].length() - 1));
                    province.setProvinceName(array[1].substring(1, array[1].length() - 1));
//                    将解析出来的数据存储到Province表
                    whatsWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据，
     */
    public synchronized static boolean handleCitiesResponse(WhatsWeatherDB whatsWeatherDB,
                                    String response, int provinceId, String provinceCode) {

        if (!TextUtils.isEmpty(response)) {
//          需要把首尾的{}去掉，故用substring
            String[] allCities = response.substring(1, response.length() - 1).split(",");
            if (allCities != null && allCities.length > 0) {
                for (String c : allCities) {
                    String[] array = c.split(":");
                    City city = new City();
//                需要把首尾的""去掉，故用substring,且要加上之前省的代号
                    city.setCityCode(provinceCode + (array[0].substring(1, array[0].length() - 1)));
                    city.setCityName(array[1].substring(1, array[1].length() - 1));
                    city.setProvinceId(provinceId);
//                    将解析出来的数据存储到City表
                    whatsWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的县级数据，
     */
    public synchronized static boolean handleCountriesResponse(WhatsWeatherDB whatsWeatherDB,
                                      String response, int cityId, String cityCode) {
        if (!TextUtils.isEmpty(response)) {
//            需要把首尾的{}去掉，故用substring
            String[] allCounties = response.substring(1,response.length()-1).split(",");
            if (allCounties != null && allCounties.length > 0) {
                for (String c : allCounties) {
                    String[] array = c.split(":");
                    Country country = new Country();
//            需要把首尾的""去掉，故用substring，且要加上之前市的代号
                    country.setCountryCode(cityCode + (array[0].substring(1, array[0].length() - 1)));
                    country.setCountryName(array[1].substring(1, array[1].length() - 1));
                    country.setCityId(cityId);
//                    将解析出来的数据存储到Country表
                    whatsWeatherDB.saveCountry(country);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析服务器返回的天气情况的JSON数据，并将解析出的数据存储到本地
     */
    public static void handleWeatherResponse(Context context, String response) {
        try {
            JSONObject jsonObject1 = new JSONObject(response);
            JSONArray jsonArray1 = jsonObject1.getJSONArray("weather");
            JSONObject weatherInfo = jsonArray1.getJSONObject(0);
            String cityName = weatherInfo.getString("city_name");
            String weathercode = weatherInfo.getString("city_id");
            String lastUpdate = weatherInfo.getString("last_update");
//            只保留时间，去掉日期
            String[] array1 = lastUpdate.split("T");
            String[] array2 = array1[1].split("\\+");//对于shift输入的字符需要在前面加上“\\”
            String publishTime = array2[0];

            JSONArray future = weatherInfo.getJSONArray("future");
            JSONObject today = future.getJSONObject(0);
            String weatherDesp = today.getString("text");
            String temper1 = today.getString("low");
            String temper2 = today.getString("high");

            saveWeatherInfo(context, cityName, weathercode, temper1, temper2, weatherDesp, publishTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将服务器返回的所有天气数据存储到SharedPreferences文件中
     */
    public static void saveWeatherInfo(Context context, String cityName, String weatherCode,
             String temper1, String temper2, String weatherDesp, String publishTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected", true);
        editor.putString("city_name", cityName);
        editor.putString("weather_code", weatherCode);
        editor.putString("temper1", temper1);
        editor.putString("temper2", temper2);
        editor.putString("weather_desp", weatherDesp);
        editor.putString("publish_time", publishTime);
        editor.putString("current_date", sdf.format(new Date()));
        editor.commit();
    }

    /**
     * 从asset路径下读取对应文件转String输出
     */
    public static String getFileData(Context mContext, String fileName) {
        // TODO Auto-generated method stub
        StringBuilder sb = new StringBuilder();
        AssetManager am = mContext.getAssets();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    am.open(fileName)));
            String next = "";
            while (null != (next = br.readLine())) {
                sb.append(next);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            sb.delete(0, sb.length());
        }
        return sb.toString().trim();
    }


    /**
     *使用JSONObject解析本地的json数据
     * "id":"CHBJ000000", "name":"北京", "en":"Beijing", "parent1":"北京", "parent2":"直辖市", "parent3":"中国" },
     */
    public static void parseJSONWithJSONObject(WhatsWeatherDB whatsWeatherDB,String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            String preCityName = "";
            String curCityName = "";
            String preProvinceName = "";
            String curProvinceName = "";
            String countryName = "";
            String countryCode = "";
            int countProvince = 0;
            int countCity = 0;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                curProvinceName = jsonObject.getString("parent2");
                curCityName = jsonObject.getString("parent1");
                countryName = jsonObject.getString("name");
                countryCode = jsonObject.getString("id");
                if (!curProvinceName.equals(preProvinceName)) {
                    Province province = new Province();
//                  只存储一项provinceName，因不需要也没有provinceCode了
                    province.setProvinceName(curProvinceName);
                    whatsWeatherDB.saveProvince(province);
                    preProvinceName = curProvinceName;
                    countProvince = countProvince + 1;
                }
                if (!curCityName.equals(preCityName)) {
                    City city = new City();
//                  存储cityName和provinceId
                    city.setCityName(curCityName);
                    city.setProvinceId(countProvince);
                    whatsWeatherDB.saveCity(city);
                    preCityName = curCityName;
                    countCity = countCity + 1;
                }
                Country country = new Country();
//                存储countryCode，countryName和cityId
                country.setCountryCode(countryCode);
                country.setCountryName(countryName);
                country.setCityId(countCity);
                whatsWeatherDB.saveCountry(country);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

