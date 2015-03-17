package com.coolweather.app.activity;

import com.coolweather.app.R;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity {

	private LinearLayout weatherInfoLayout;
	
	/**
	 * 显示城市名称
	 */
	private TextView cityNameTextView;
	
	/**
	 * 发布时间
	 */
	private TextView publishTextView;
	
	/**
	 * 显示天气描述信息
	 */
	private TextView weatherDespTextView;
	
	private TextView temp1TextView;
	
	private TextView temp2TextView;
	
	/**
	 * 当前日期
	 */
	private TextView currentDateTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		
		weatherInfoLayout = (LinearLayout)findViewById(R.id.weather_info_layout);
		cityNameTextView = (TextView)findViewById(R.id.city_name);
		publishTextView = (TextView)findViewById(R.id.publish_text);
		weatherDespTextView = (TextView)findViewById(R.id.weather_desp);
		temp1TextView = (TextView)findViewById(R.id.temp1);
		temp2TextView = (TextView)findViewById(R.id.temp2);
		currentDateTextView = (TextView)findViewById(R.id.current_date);
		
		String countyCode = getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			publishTextView.setText("同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameTextView.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		} else {
			showWeather();
		}
	}
	
	/**
	 * 通过完整的省市县代码获取最终的天气代码
	 * @param countyCode
	 */
	private void queryWeatherCode(String countyCode) {
		String address = "http://m.weather.com.cn/data5/city" + countyCode + ".xml";
		queryFromServer(address, "countyCode");
	}
	
	private void quertWeatherInfo(String weatherCode) {
		String address = "http://m.weather.com.cn/atad/" + weatherCode + ".html";
		queryFromServer(address, "weatherCode");
	}
	
	private void queryFromServer(final String address, final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						String[] array = response.split("\\|");
						if (array != null && array.length > 0) {
							String weatherCode = array[1];
							quertWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)) {
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							showWeather();
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
						publishTextView.setText("同步失败");
					}
				});
			}
		});
	}
	
	/**
	 * 从SharedPreferences同步存储的数据
	 */
	private void showWeather() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameTextView.setText(pref.getString("city_name", ""));
		temp1TextView.setText(pref.getString("temp1", ""));
		temp2TextView.setText(pref.getString("temp2", ""));
		weatherDespTextView.setText(pref.getString("weather_desp", ""));
		publishTextView.setText("今天" + pref.getString("publish_time", "") + "发布");
		currentDateTextView.setText(pref.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameTextView.setVisibility(View.VISIBLE);
	}
}
