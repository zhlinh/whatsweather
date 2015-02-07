package com.monet.whatsweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.monet.whatsweather.R;
import com.monet.whatsweather.db.WhatsWeatherDB;
import com.monet.whatsweather.model.City;
import com.monet.whatsweather.model.Country;
import com.monet.whatsweather.model.Province;
import com.monet.whatsweather.util.HttpCallbackListener;
import com.monet.whatsweather.util.HttpUtil;
import com.monet.whatsweather.util.Utility;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Monet on 2015/2/4.
 */
public class ChooseAreaActivity extends Activity {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTRY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private WhatsWeatherDB whatsWeatherDB;
    private List<String> dataList = new ArrayList<String>();

    /**
     * 省、市、县列表
     */
    private List<Province> provinceList;
    private List<City> cityList;
    private List<Country> countryList;

    /**
     * 选中的省份、城市，及当前选中的级别
     */
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        listView = (ListView) findViewById(R.id.list_view);
        titleText = (TextView) findViewById(R.id.title_text);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        whatsWeatherDB = WhatsWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    Log.i("Click", String.valueOf(selectedCity.getId()));
                    queryCountries();
                }
            }
        });
        queryProvince();   // 加载省级数据
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryProvince() {
        provinceList=whatsWeatherDB.loadProvinces();
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromServer(null, "province");
        }
    }

    /**
     * 查询选中省中所有的市，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCities() {
        cityList = whatsWeatherDB.loadCites(selectedProvince.getId());
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        } else {
            queryFromServer(selectedProvince.getProvinceCode(), "city");
        }
    }

    /**
     * 查询选中市中的所有县，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCountries() {
        countryList = whatsWeatherDB.loadCountries(selectedCity.getId());
        Log.i("selectedCityId", String.valueOf(selectedCity.getId()));
        if (countryList.size() > 0) {
            dataList.clear();
            for (Country country : countryList) {
                dataList.add(country.getCountryName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTRY;
        } else {
            queryFromServer(selectedCity.getCityCode(), "country");
        }
    }

    /**
     * 根据传入的代号和类型从服务器上查询省市县数据
     */
    private void queryFromServer(final String code, final String type) {
        String address;
        if (TextUtils.isEmpty(code)) {
            address = "http://weather.com.cn/data/city3jdata/china.html";
        } else if ("city".equals(type)) {
            address = "http://weather.com.cn/data/city3jdata/provshi/" + code + ".html";
        } else {
            address = "http://weather.com.cn/data/city3jdata/station/" + code + ".html";
        }
        showProgressDialog();

        /**
         * 运用回调函数的方法(第10章)，sendHttpRequest常用用法，HttpCallbackListener()是自己写的接口
         * 注意此处的onFinish()方法和onError方法最终还是在子线程中进行的，因此不能进行UI操作
         * 若想进行UI操作还得使用异步处理方式，即runOnUiThread,简便多了
         * 印象中第9章里(P346)的异步处理用的是Handler类，子线程中handler.sendMessage(message)
         */
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvincesResponse(whatsWeatherDB, response);
                } else if ("city".equals(type)) {
//                    因要传进去两个参数，Id和provinCode
                    result = Utility.handleCitiesResponse(whatsWeatherDB, response,
                            selectedProvince.getId(), selectedProvince.getProvinceCode());
                } else if ("country".equals(type)) {
//                    因要传进去两个参数，Id和cityCode
                    result = Utility.handleCountriesResponse(whatsWeatherDB, response,
                            selectedCity.getId(), selectedCity.getCityCode());
                }
                if (result) {
//                    通过runOnUiThread()方法回到主线程中处理逻辑,看来是Android提供的方法
//                    UI的操作必须在主线程中
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvince();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("country".equals(type)) {
                                queryCountries();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
//                通过runOnUiThread()方法回到主线程中处理逻辑
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this, "加载失败",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    /**
     * 捕获back键，根据当前的级别来判断，此时应该返回到市列表，省列表，还是直接退出
     */
    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTRY) {
            queryCities();
        }else if (currentLevel == LEVEL_CITY) {
            queryProvince();
        } else {
            finish();
        }
    }

}
