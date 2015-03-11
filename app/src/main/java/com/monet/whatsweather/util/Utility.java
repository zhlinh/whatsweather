package com.monet.whatsweather.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import com.monet.whatsweather.db.WhatsWeatherDB;
import com.monet.whatsweather.model.City;
import com.monet.whatsweather.model.Country;
import com.monet.whatsweather.model.Province;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
     *使用JSONObject解析json数据
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
            int countProvince = 0;
            int countCity = 0;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                curProvinceName = jsonObject.getString("parent2");
                curCityName = jsonObject.getString("parent1");
                countryName = jsonObject.getString("name");
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
//                存储countryName和cityId
                country.setCountryName(countryName);
                country.setCityId(countCity);
                whatsWeatherDB.saveCountry(country);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

