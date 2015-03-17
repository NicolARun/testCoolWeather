package com.coolweather.app.util;

import android.text.TextUtils;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;

public class Utility {

	/**
	 * chrome: view-source:http://m.weather.com.cn/data5/city.xml
	 * 获取省的信息
	 * @param coolWeatherDB
	 * @param response
	 * @return
	 */
	public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB, String response) {
		if (!TextUtils.isEmpty(response)) {
			String[] allProvinces = response.split(",");
			
			if (allProvinces != null && allProvinces.length > 0) {
				for (String p : allProvinces) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceName(array[1]);
					province.setProvinceCode(array[0]);
					
					coolWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * chrome: view-source:http://m.weather.com.cn/data5/city21.xml
	 * 湖北 province id = 21
	 * 获取某省节点下的所有市的信息
	 * @param coolWeatherDB
	 * @param response
	 * @param provinceId
	 * @return
	 */
	public synchronized static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB, String response, int provinceId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCities = response.split(",");
			
			if (allCities != null && allCities.length > 0) {
				for (String c : allCities) {
					String[] array = c.split("\\|");
					
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					
					coolWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * chrome: view-source:http://m.weather.com.cn/data5/city2001.xml
	 * 武汉 city id = 01
	 * 获取某市节点下所有县的信息
	 * @param coolWeatherDB
	 * @param response
	 * @param cityId
	 * @return
	 */
	public synchronized static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB, String response, int cityId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCounties = response.split(",");
			
			if (allCounties != null && allCounties.length > 0) {
				for (String c : allCounties) {
					String[] array = c.split("\\|");
					
					County county = new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					
					coolWeatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}
	
}
