package com.monet.whatsweather.model;

/**
 * Created by Monet on 2015/2/3.
 */
public class Country {
    private int id;
    private String countryName;
    private String countryCode;
    private int cityId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public int getCityId() {
        return cityId;
    }

    /**居然会把传进来的cityId写成cityCode，导致cityId一直无法设置，一直为0
     * 该错误用Log找了很久。。。发现是Id不对，无法查询到数据库。。进而。。。面壁中......
     */
    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
