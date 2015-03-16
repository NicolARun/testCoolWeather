package com.coolweather.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class CoolWeatherOpenHelper extends SQLiteOpenHelper {

	public CoolWeatherOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CREATE_PROVINCE);
		db.execSQL(CREATE_CITY);
		db.execSQL(CREATE_COUNTY);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	/**
	 * Province(省) Create Table
	 */
	public static final String CREATE_PROVINCE = "CREATE TABLE Province ("
			+ "id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "province_name TEXT, "
			+ "province_code TEXT)";
	
	/**
	 * City(市) Create Table
	 */
	public static final String CREATE_CITY = "CREATE TABLE City ("
			+ "id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "city_name TEXT, "
			+ "city_code TEXT, "
			+ "province_id INTEGER)";
	
	/**
	 * County(县) Create Table
	 */
	public static final String CREATE_COUNTY = "CREATE TABLE County ("
			+ "id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "county_name TEXT, "
			+ "county_code TEXT, "
			+ "city_id INTEGER)";
	
}
