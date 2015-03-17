package com.coolweather.app.activity;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.R;
import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
	
	public static final int LEVEL_PROVINCE = 0;
	
	public static final int LEVEL_CITY = 1;
	
	public static final int LEVEL_COUNTY = 2;
	
	private ProgressDialog progressDialog;
	
	private TextView titleTextView;
	
	private ListView listView;

	private ArrayAdapter<String> adapter;
	
	private CoolWeatherDB coolWeatherDB;
	
	private List<String> dataList = new ArrayList<String>();
	
	private List<Province> provincesList;
	
	private List<City> citiesList;
	
	private List<County> countiesList;
	
	private Province selectedProvince;
	
	private City selectedCity;
	
	private County selectedCounty;
	
	/**
	 * 当前地址段的级别
	 */
	private int currentLevel;
	
	/**
	 * 是否从WeatherActivity切换城市功能中跳转
	 */
	private boolean isFromWeatherActivity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (prefs.getBoolean("city_selected", false) && !isFromWeatherActivity) {
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);	// R is com.coolweather.app.R Not android.R
		
		listView = (ListView)findViewById(R.id.list_view);
		titleTextView = (TextView)findViewById(R.id.title_text);
		
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index,
					long arg3) {
				// TODO Auto-generated method stub
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provincesList.get(index);
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					selectedCity = citiesList.get(index);
					queryCounties();
				} else if (currentLevel == LEVEL_COUNTY) {
					String countyCode = countiesList.get(index).getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
		});
		
		queryProvinces();
	}
	
	/**
	 * 查询全国所有的省
	 * 优先从本地数据库内获取
	 * 其次在指定的IP查询
	 */
	private void queryProvinces() {
		provincesList = coolWeatherDB.loadProvinces();
		
		if (provincesList.size() > 0) {
			dataList.clear();
			
			for (Province province : provincesList) {
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			
			listView.setSelection(0);
			titleTextView.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		} else {
			queryFromServer(null, "province");
		}
	}
	
	/**
	 * 查询指定的省Id下所有的市
	 */
	private void queryCities() {
		citiesList = coolWeatherDB.loadCities(selectedProvince.getId());
		
		if (citiesList.size() > 0) {
			dataList.clear();
			
			for (City city : citiesList) {
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			
			listView.setSelection(0);
			titleTextView.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		} else {
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}
	
	/**
	 * 查询指定的市Id下所有的县
	 */
	private void queryCounties() {
		countiesList = coolWeatherDB.loadCounties(selectedCity.getId());
		
		if (countiesList.size() > 0) {
			dataList.clear();
			
			for (County county : countiesList) {
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			
			listView.setSelection(0);
			titleTextView.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		} else {
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}
	
	/**
	 * 根据后缀数字编码与省市县分级，在服务器上查询数据
	 * @param code 后缀数字编码
	 * @param type 省、市、县
	 */
	private void queryFromServer(final String code, final String type) {
		String address;
		if (!TextUtils.isEmpty(code)) {
			address = "http://m.weather.com.cn/data5/city" + code + ".xml";
		} else {
			address = "http://m.weather.com.cn/data5/city.xml";
		}
		
		showProgressDialog();
		
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				boolean result = false;
				
				if ("province".equals(type)) {
					result = Utility.handleProvincesResponse(coolWeatherDB, response);
				} else if ("city".equals(type)) {
					result = Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
				} else if ("county".equals(type)) {
					result = Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
				}
				
				if (result) {
					// 通过runOnUiThread方法回到主线程处理界面更新逻辑,与handle方法类似
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							closeProgressDialog();
							
							if ("province".equals(type)) {
								queryProvinces();
							} else if ("city".equals(type)) {
								queryCities();
							} else if ("county".equals(type)) {
								queryCounties();
							}
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "获取列表失败", Toast.LENGTH_SHORT).show();
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
	 * 重写BACK键逻辑
	 * 根据当前列表的级别来判断应该退回市列表、省列表、完全退出
	 */
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
//		super.onBackPressed();
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		} else {
			if (isFromWeatherActivity) {
				Intent intent = new Intent(this, WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
}
