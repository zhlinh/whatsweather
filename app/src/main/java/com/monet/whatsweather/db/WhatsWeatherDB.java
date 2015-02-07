package com.monet.whatsweather.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.monet.whatsweather.model.City;
import com.monet.whatsweather.model.Country;
import com.monet.whatsweather.model.Province;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Monet on 2015/2/3.
 */
public class WhatsWeatherDB {
    /**
     * 数据库名
     */
    public static final String DB_NAME ="whats_weather";

    /**
     * 数据库版本
     */
    public static final int VERSION = 1;

    private static WhatsWeatherDB whatsWeatherDB; //在直接声明一个本类的实例，奇怪了?

    private SQLiteDatabase db;

    /**
     * 将构造方法私有化
     */
    private WhatsWeatherDB(Context context) {
        WhatsWeatherOpenHelper dbHelper = new WhatsWeatherOpenHelper(context,
                DB_NAME, null, VERSION);
        db = dbHelper.getWritableDatabase();
    }

    /**
     *获取WhatsWeatherDB的实例,为何用synchronized属性？
     * 还是不是很懂，将构造方法私有化，并提供一个getInstance的方法获取实例，这样就能
     * 保证全局范围内只有一个WhatsWheather实例
     */
    public synchronized static WhatsWeatherDB getInstance(Context context){
        if(whatsWeatherDB == null){            //若没有实例，则新建一个
            whatsWeatherDB = new WhatsWeatherDB(context);
        }
        return whatsWeatherDB;
    }

    /**
     * 将Province实例存储到数据库,因province_id是自动递增的，所以不用手动存储
     */
    public void saveProvince(Province province){
        if(province != null) {
            ContentValues values = new ContentValues();
            values.put("province_name", province.getProvinceName());
            values.put("province_code", province.getProvinceCode());
            db.insert("Province", null, values);
        }
    }

    /**
     *从数据库读取全国所有的省份信息
     * 此处的list是为后面activity的下拉菜单做准备的
     */
    public List<Province> loadProvinces() {
        List<Province> list = new ArrayList<Province>();
        Cursor cursor = db.query("Province", null, null, null, null, null, null );
        if (cursor.moveToFirst()) {
            do {
                Province province = new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex("id")));
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
                list.add(province);
            } while (cursor.moveToNext());
        }
        return list;
    }

    /**
     * 将City实例存储到数据库,因city_id是自动递增的，所以不用手动存储
     */
    public void saveCity(City city) {
        if (city != null) {
            ContentValues values = new ContentValues();
            values.put("city_name", city.getCityName());
            values.put("city_code", city.getCityCode());
            values.put("province_id", city.getProvinceId());
            db.insert("City", null, values);
        }
    }

    /**
     * 从数据库中读取某省下所有的城市信息
     */
    public List<City> loadCites(int provinceId) {
        List<City> list = new ArrayList<City>();
        Cursor cursor = db.query("City", null, "province_id = ?",
                new String[] {String.valueOf(provinceId)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                City city = new City();
                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                city.setProvinceId(provinceId);
                list.add(city);
                Log.i("cityIddddd", String.valueOf(city.getId()));
            } while (cursor.moveToNext());
        }
        return list;
    }

    /**
     * 将Country实例存储到数据库,因country_id是自动递增的，所以不用手动存储
     */
    public void saveCountry(Country country) {
        if (country != null) {
            ContentValues values = new ContentValues();
            values.put("country_name", country.getCountryName());
            values.put("country_code", country.getCountryCode());
            values.put("city_id", country.getCityId());
            db.insert("Country", null, values);
        }
    }

    /**
     * 从数据库读取某城市下所有的县信息
     */
    public List<Country> loadCountries(int cityId) {
        List<Country> list = new ArrayList<Country>();
        Cursor cursor = db.query("Country", null, "city_id = ?",
                new String[]{String.valueOf(cityId)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Country country = new Country();
                country.setId(cursor.getInt(cursor.getColumnIndex("id")));
                country.setCountryName(cursor.getString(cursor.getColumnIndex("country_name")));
                country.setCountryCode(cursor.getString(cursor.getColumnIndex("country_code")));
                country.setCityId(cityId);
                list.add(country);
            } while (cursor.moveToNext());
        }
        return list;
    }

}

