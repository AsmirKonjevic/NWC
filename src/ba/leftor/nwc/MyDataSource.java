package ba.leftor.nwc;

import java.sql.SQLData;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 *  layer za manipulaciju baze
 *
 */
public class MyDataSource {

	private SQLiteDatabase database;
	private MySQLiteHelper databaseHelper;
	
	private String catalogColumns[]={MySQLiteHelper.C_COLUMN_AUTO_INCREMENT,
			MySQLiteHelper.C_COLUMN_ID,
			MySQLiteHelper.C_COLUMN_TEXT,
			MySQLiteHelper.C_COLUMN_TYPE};
	
	private String premiseColumns[]={MySQLiteHelper.P_COLUMN_AUTO_INCREMENT,
			MySQLiteHelper.P_COLUMN_GPS_LATITUDE_CREATED,
			MySQLiteHelper.P_COLUMN_GPS_LONGITUDE_CREATED,
			MySQLiteHelper.P_COLUMN_GPS_ALTITUDE_CREATED,
			MySQLiteHelper.P_COLUMN_GPS_LATITUDE,
			MySQLiteHelper.P_COLUMN_GPS_LONGITUDE,
			MySQLiteHelper.P_COLUMN_GPS_ALTITUDE,
			MySQLiteHelper.P_COLUMN_HOUSE_CONNECTION_NUMBER,
			MySQLiteHelper.P_COLUMN_PREMISE_ID,
			MySQLiteHelper.P_COLUMN_STC_DB_NUMBER,
			MySQLiteHelper.P_COLUMN_SEC_DB_NUMBER,
			MySQLiteHelper.P_COLUMN_SEC_DB_NUMBER_2,
			MySQLiteHelper.P_COLUMN_SEC_DB_NUMBER_3,
			MySQLiteHelper.P_COLUMN_SEC_DB_NUMBER_4,
			MySQLiteHelper.P_COLUMN_SEC_DB_NUMBER_5,
			MySQLiteHelper.P_COLUMN_TOTAL_ELECTRICAL_METERS,
			MySQLiteHelper.P_COLUMN_FLOOR_COUNT,
			MySQLiteHelper.P_COLUMN_USE_OF_BUILDING,
			MySQLiteHelper.P_COLUMN_PREMISE_TYPE_ID,
			MySQLiteHelper.P_COLUMN_PHOTO_HOUSE1,
			MySQLiteHelper.P_COLUMN_PHOTO_HOUSE2,
			MySQLiteHelper.P_COLUMN_PHOTO_HOUSE3,
			MySQLiteHelper.P_COLUMN_PHOTO_PREMISE_CONNECTION,
			MySQLiteHelper.P_COLUMN_PHOTO_STC,
			MySQLiteHelper.P_COLUMN_PHOTO_SEC,
			MySQLiteHelper.P_COLUMN_WATER_METER_NUMBER,
			MySQLiteHelper.P_COLUMN_WATER_METER_NUMBER_2,
			MySQLiteHelper.P_COLUMN_WATER_METER_NUMBER_3,
			MySQLiteHelper.P_COLUMN_WATER_METER_NUMBER_4,
			MySQLiteHelper.P_COLUMN_WATER_METER_NUMBER_5,
			MySQLiteHelper.P_COLUMN_SYNC_ATTEMPTED,
			MySQLiteHelper.P_COLUMN_SYNC_ERROR,
			MySQLiteHelper.P_COLUMN_DISTRICT,
			MySQLiteHelper.P_COLUMN_WATER_METER_STATUS,
			MySQLiteHelper.P_COLUMN_HCN_AUTO_GENERATED,
			MySQLiteHelper.P_COLUMN_PREMISE_NAME,
			MySQLiteHelper.P_COLUMN_ONLY_IMAGES
			};

	private String waterReadingColumns[]={
			MySQLiteHelper.W_COLUMN_AUTO_INCREMENT,
			MySQLiteHelper.W_COLUMN_GPS_LATITUDE_CREATED,
			MySQLiteHelper.W_COLUMN_GPS_LONGITUDE_CREATED,
			MySQLiteHelper.W_COLUMN_GPS_ALTITUDE_CREATED,
			MySQLiteHelper.W_COLUMN_PHOTO_INSTRUMENT,
			MySQLiteHelper.W_COLUMN_HOUSE_CONNECTION_NUMBER,
			MySQLiteHelper.W_COLUMN_SEC_DB_NUMBER,
			MySQLiteHelper.W_COLUMN_WATER_METER_NUMBER,
			MySQLiteHelper.W_COLUMN_WATER_READING_NUMBER,
			MySQLiteHelper.W_COLUMN_SYNC_ATTEMPTED,
			MySQLiteHelper.W_COLUMN_SYNC_ERROR,
			MySQLiteHelper.W_COLUMN_DYNAMIC_PHOTOS,
			MySQLiteHelper.W_COLUMN_MANUAL_SOURCE,
			MySQLiteHelper.W_COLUMN_PREMISE_NAME,
			MySQLiteHelper.W_COLUMN_DISTRICT_ID,
			MySQLiteHelper.W_COLUMN_AREA_NAME
	};

	private String waterLeakColumns[]={MySQLiteHelper.L_COLUMN_AUTO_INCREMENT,
			MySQLiteHelper.L_COLUMN_GPS_LATITUDE_CREATED,
			MySQLiteHelper.L_COLUMN_GPS_LONGITUDE_CREATED,
			MySQLiteHelper.L_COLUMN_GPS_ALTITUDE_CREATED,
			MySQLiteHelper.L_COLUMN_PHOTO_LEAK,
			MySQLiteHelper.L_COLUMN_HOUSE_CONNECTION_NUMBER,
			MySQLiteHelper.L_COLUMN_WATER_LEAK_DESCRIPTION,
			MySQLiteHelper.L_COLUMN_WATER_TICKET_SERIAL_NUMBER,
			MySQLiteHelper.L_COLUMN_SYNC_ATTEMPTED,
			MySQLiteHelper.L_COLUMN_SYNC_ERROR,
			MySQLiteHelper.L_COLUMN_DYNAMIC_PHOTOS,
			MySQLiteHelper.L_COLUMN_MANUAL_SOURCE,
			MySQLiteHelper.L_COLUMN_ILLEGAL_CONNECT,
			MySQLiteHelper.L_COLUMN_LEAK_TYPE,
			MySQLiteHelper.L_COLUMN_VIOLATION_PENALTY,
			MySQLiteHelper.L_COLUMN_WATER_METER_READING_NUMBER,
			MySQLiteHelper.L_COLUMN_TICKET_ID,
			MySQLiteHelper.L_COLUMN_WATER_METER_NUMBER,
			MySQLiteHelper.L_COLUMN_SEC_DB_NUMBER,
			MySQLiteHelper.L_COLUMN_PREMISE_NAME,
			MySQLiteHelper.L_COLUMN_PREMISE_TYPE_ID
	};
	
	private String customerColumns[]={MySQLiteHelper.U_COLUMN_AUTO_INCREMENT,
			MySQLiteHelper.U_COLUMN_GPS_LATITUDE_CREATED,
			MySQLiteHelper.U_COLUMN_GPS_LONGITUDE_CREATED,
			MySQLiteHelper.U_COLUMN_GPS_ALTITUDE_CREATED,
			MySQLiteHelper.U_COLUMN_HOUSE_CONNECTION_NUMBER,
			MySQLiteHelper.U_COLUMN_FULL_NAME,
			MySQLiteHelper.U_COLUMN_NATIONAL_ID,
			MySQLiteHelper.U_COLUMN_DAY_EXPIRATION_DATE,
			MySQLiteHelper.U_COLUMN_YEAR_EXPIRATION_DATE,
			MySQLiteHelper.U_COLUMN_EMAIL,
			MySQLiteHelper.U_COLUMN_PHONE_LAND_LINE,
			MySQLiteHelper.U_COLUMN_MOBILE_PHONE,
			MySQLiteHelper.U_COLUMN_PO_BOX,
			MySQLiteHelper.U_COLUMN_ZIP_CODE,
			MySQLiteHelper.U_COLUMN_LAND_ID,
			MySQLiteHelper.U_COLUMN_ALTERNATE_CONTACT_NAME,
			MySQLiteHelper.U_COLUMN_CONTACT_NATIONAL_ID,
			MySQLiteHelper.U_COLUMN_CONTACT_PHONE_LAND_LINE,
			MySQLiteHelper.U_COLUMN_CONTACT_MOBILE_PHONE,
			MySQLiteHelper.U_COLUMN_CONTACT_DAY_EXPIRATION_DATE,
			MySQLiteHelper.U_COLUMN_CONTACT_YEAR_EXPIRATION_DATE,
			MySQLiteHelper.U_COLUMN_CONTACT_EMAIL,
			MySQLiteHelper.U_COLUMN_MONTH_EXPIRATION_DATE,
			MySQLiteHelper.U_COLUMN_CONTACT_MONTH_EXPIRATION_DATE,
			MySQLiteHelper.U_COLUMN_GENDER,
			MySQLiteHelper.U_COLUMN_NATIONALITY_ID,
			MySQLiteHelper.U_COLUMN_INHABITANT_TYPE_ID,
			MySQLiteHelper.U_COLUMN_TARSHEED_TYPE_ID,
			MySQLiteHelper.U_COLUMN_TARSHEED_GIVEN,
			MySQLiteHelper.U_COLUMN_PHOTO_OWNER,
			MySQLiteHelper.U_COLUMN_PHOTO_ALTERNATE,
			MySQLiteHelper.U_COLUMN_PHOTO_INSTRUMENT,
			MySQLiteHelper.U_COLUMN_CONTACT_STREET_NAME,
			MySQLiteHelper.U_COLUMN_CONTACT_ZIP_CODE,
			MySQLiteHelper.U_COLUMN_CONTACT_PO_BOX,
			MySQLiteHelper.U_COLUMN_NEW_PREMISE_LAT,
			MySQLiteHelper.U_COLUMN_NEW_PREMISE_LNG,
			MySQLiteHelper.U_COLUMN_NEW_PREMISE_ALT,
			MySQLiteHelper.U_COLUMN_SYNC_ATTEMPTED,
			MySQLiteHelper.U_COLUMN_SYNC_ERROR
			};


	//Class Constructor 
	public MyDataSource(Context context)
	{
		databaseHelper=new MySQLiteHelper(context);
	}
	
	/**
	 * Add catalog item to db
	 * @param JSONSpinnerItem item - from db
	 * @param String type - table name from server databse
	 * @return long or null if not entered
	 */
	public long addCatalogItemToDb(JSONSpinnerItem item,String type)
	{
		ContentValues values=new ContentValues();
		values.put(MySQLiteHelper.C_COLUMN_ID, item.id);
		values.put(MySQLiteHelper.C_COLUMN_TEXT, item.text);
		values.put(MySQLiteHelper.C_COLUMN_TYPE, type);
		
		return database.insert(MySQLiteHelper.TABLE_CATALOG, null, values);
	}

	/**
	 * Add premise item to db
	 * @param JSONPremiseItem item - from db
	 * @return long or null if not entered
	 */
	public long addPremiseItemToDb(JSONPremiseItem item)
	{
		ContentValues values=new ContentValues();
		values.put(MySQLiteHelper.P_COLUMN_GPS_LATITUDE_CREATED, item.gps_latitude_created);
		values.put(MySQLiteHelper.P_COLUMN_GPS_LONGITUDE_CREATED, item.gps_longitude_created);
		values.put(MySQLiteHelper.P_COLUMN_GPS_ALTITUDE_CREATED, item.gps_altitude_created);
		values.put(MySQLiteHelper.P_COLUMN_GPS_LATITUDE, item.gps_latitude);
		values.put(MySQLiteHelper.P_COLUMN_GPS_LONGITUDE, item.gps_longitude);
		values.put(MySQLiteHelper.P_COLUMN_GPS_ALTITUDE, item.gps_altitude);
		values.put(MySQLiteHelper.P_COLUMN_HOUSE_CONNECTION_NUMBER, item.house_connection_number);
		values.put(MySQLiteHelper.P_COLUMN_PREMISE_ID, item.premise_id);
		values.put(MySQLiteHelper.P_COLUMN_STC_DB_NUMBER, item.stc_db_number);
		values.put(MySQLiteHelper.P_COLUMN_SEC_DB_NUMBER, item.sec_db_number);
		values.put(MySQLiteHelper.P_COLUMN_SEC_DB_NUMBER_2, item.sec_db_number2);
		values.put(MySQLiteHelper.P_COLUMN_SEC_DB_NUMBER_3, item.sec_db_number3);
		values.put(MySQLiteHelper.P_COLUMN_SEC_DB_NUMBER_4, item.sec_db_number4);
		values.put(MySQLiteHelper.P_COLUMN_SEC_DB_NUMBER_5, item.sec_db_number5);
		values.put(MySQLiteHelper.P_COLUMN_TOTAL_ELECTRICAL_METERS, item.total_electrical_meters);
		values.put(MySQLiteHelper.P_COLUMN_FLOOR_COUNT, item.floor_count);
		values.put(MySQLiteHelper.P_COLUMN_USE_OF_BUILDING, item.use_of_building);
		values.put(MySQLiteHelper.P_COLUMN_PREMISE_TYPE_ID, item.premise_type_id);
		values.put(MySQLiteHelper.P_COLUMN_PHOTO_HOUSE1, item.photo_house1);
		values.put(MySQLiteHelper.P_COLUMN_PHOTO_HOUSE2, item.photo_house2);
		values.put(MySQLiteHelper.P_COLUMN_PHOTO_HOUSE3, item.photo_house3);
		values.put(MySQLiteHelper.P_COLUMN_PHOTO_PREMISE_CONNECTION, item.photo_premise_connection);
		values.put(MySQLiteHelper.P_COLUMN_PHOTO_STC, item.photo_stc);
		values.put(MySQLiteHelper.P_COLUMN_PHOTO_SEC, item.photo_sec);
		values.put(MySQLiteHelper.P_COLUMN_WATER_METER_NUMBER, item.water_meter_number);
		values.put(MySQLiteHelper.P_COLUMN_WATER_METER_NUMBER_2, item.water_meter_number2);
		values.put(MySQLiteHelper.P_COLUMN_WATER_METER_NUMBER_3, item.water_meter_number3);
		values.put(MySQLiteHelper.P_COLUMN_WATER_METER_NUMBER_4, item.water_meter_number4);
		values.put(MySQLiteHelper.P_COLUMN_WATER_METER_NUMBER_5, item.water_meter_number5);
		values.put(MySQLiteHelper.P_COLUMN_SYNC_ATTEMPTED, 0);
		values.put(MySQLiteHelper.P_COLUMN_DISTRICT, item.district);
		values.put(MySQLiteHelper.P_COLUMN_WATER_METER_STATUS, item.water_meter_status);
		values.put(MySQLiteHelper.P_COLUMN_HCN_AUTO_GENERATED, item.hcnAutoGenerated);
		values.put(MySQLiteHelper.P_COLUMN_PREMISE_NAME, item.premise_name);
		//if only images from images form
		values.put(MySQLiteHelper.P_COLUMN_ONLY_IMAGES, item.only_images);
		
		return database.insert(MySQLiteHelper.TABLE_PREMISE, null, values);
	}

	/**
	 * Add water reading item to db
	 * @param JSONPremiseItem item - from db
	 * @return long or null if not entered
	 */
	public long addWaterMeterReadingItemToDb(JSONWaterReadingItem item)
	{
		ContentValues values=new ContentValues();
		values.put(MySQLiteHelper.W_COLUMN_GPS_LATITUDE_CREATED, item.gps_latitude_created);
		values.put(MySQLiteHelper.W_COLUMN_GPS_LONGITUDE_CREATED, item.gps_longitude_created);
		values.put(MySQLiteHelper.W_COLUMN_GPS_ALTITUDE_CREATED, item.gps_altitude_created);
		values.put(MySQLiteHelper.W_COLUMN_HOUSE_CONNECTION_NUMBER, item.house_connection_number);
		values.put(MySQLiteHelper.W_COLUMN_SEC_DB_NUMBER, item.sec_db_number);
		values.put(MySQLiteHelper.W_COLUMN_WATER_METER_NUMBER, item.water_meter_number);
		values.put(MySQLiteHelper.W_COLUMN_PHOTO_INSTRUMENT, item.photo_water_meter);
		values.put(MySQLiteHelper.W_COLUMN_WATER_READING_NUMBER, item.water_meter_reading);
		values.put(MySQLiteHelper.W_COLUMN_SYNC_ATTEMPTED, 0);
		values.put(MySQLiteHelper.W_COLUMN_DYNAMIC_PHOTOS, item.dynamic_photos);
		values.put(MySQLiteHelper.W_COLUMN_MANUAL_SOURCE, item.manual_source);
		values.put(MySQLiteHelper.W_COLUMN_PREMISE_NAME, item.premise_name);
		values.put(MySQLiteHelper.W_COLUMN_DISTRICT_ID, item.district_id);
		values.put(MySQLiteHelper.W_COLUMN_AREA_NAME, item.area_name);
		return database.insert(MySQLiteHelper.TABLE_WATER_READING, null, values);
	}

	/**
	 * Add water leak item to db
	 * @param JSONPremiseItem item - from db
	 * @return long or null if not entered
	 */
	public long addWaterLeakItemToDb(JSONReportLeakItem item)
	{
		ContentValues values=new ContentValues();
		values.put(MySQLiteHelper.L_COLUMN_GPS_LATITUDE_CREATED, item.gps_latitude_created);
		values.put(MySQLiteHelper.L_COLUMN_GPS_LONGITUDE_CREATED, item.gps_longitude_created);
		values.put(MySQLiteHelper.L_COLUMN_GPS_ALTITUDE_CREATED, item.gps_altitude_created);
		values.put(MySQLiteHelper.L_COLUMN_HOUSE_CONNECTION_NUMBER, item.house_connection_number);
		values.put(MySQLiteHelper.L_COLUMN_PHOTO_LEAK, item.photo_water_leak);
		values.put(MySQLiteHelper.L_COLUMN_WATER_LEAK_DESCRIPTION, item.description);
		values.put(MySQLiteHelper.L_COLUMN_WATER_TICKET_SERIAL_NUMBER, item.ticket_serial_number);
		values.put(MySQLiteHelper.L_COLUMN_SYNC_ATTEMPTED, 0);
		values.put(MySQLiteHelper.L_COLUMN_DYNAMIC_PHOTOS, item.dynamic_photos);
		values.put(MySQLiteHelper.L_COLUMN_MANUAL_SOURCE, item.manual_source);
		values.put(MySQLiteHelper.L_COLUMN_ILLEGAL_CONNECT, item.illegal_connect);
		values.put(MySQLiteHelper.L_COLUMN_LEAK_TYPE, item.leak_type);
		values.put(MySQLiteHelper.L_COLUMN_VIOLATION_PENALTY, item.violation_penalty);
		values.put(MySQLiteHelper.L_COLUMN_WATER_METER_READING_NUMBER, item.water_meter_reading_number);
		values.put(MySQLiteHelper.L_COLUMN_TICKET_ID, item.ticket_id);
		
		values.put(MySQLiteHelper.L_COLUMN_WATER_METER_NUMBER, item.water_meter_number);
		values.put(MySQLiteHelper.L_COLUMN_SEC_DB_NUMBER, item.sec_db_number);
		values.put(MySQLiteHelper.L_COLUMN_PREMISE_NAME, item.premise_name);
		values.put(MySQLiteHelper.L_COLUMN_PREMISE_TYPE_ID, item.premise_type_id);
		return database.insert(MySQLiteHelper.TABLE_WATER_LEAK, null, values);
	}

	
	/**
	 * Add premise item to db
	 * @param JSONCustomerItem item - from db
	 * @return long or null if not entered
	 */
	public long addCustomerItemToDb(JSONCustomerItem item)
	{
		ContentValues values=new ContentValues();
		values.put(MySQLiteHelper.U_COLUMN_GPS_LATITUDE_CREATED,item.gps_latitude_created);
		values.put(MySQLiteHelper.U_COLUMN_GPS_LONGITUDE_CREATED,item.gps_longitude_created);
		values.put(MySQLiteHelper.U_COLUMN_GPS_ALTITUDE_CREATED,item.gps_altitude_created);
		values.put(MySQLiteHelper.U_COLUMN_HOUSE_CONNECTION_NUMBER,item.house_connection_number);
		values.put(MySQLiteHelper.U_COLUMN_FULL_NAME,item.full_name);
		values.put(MySQLiteHelper.U_COLUMN_NATIONAL_ID,item.national_id);
		values.put(MySQLiteHelper.U_COLUMN_DAY_EXPIRATION_DATE,item.day_expiration_date);
		values.put(MySQLiteHelper.U_COLUMN_YEAR_EXPIRATION_DATE,item.year_expiration_date);
		values.put(MySQLiteHelper.U_COLUMN_EMAIL,item.email);
		values.put(MySQLiteHelper.U_COLUMN_PHONE_LAND_LINE,item.phone_land_line);
		values.put(MySQLiteHelper.U_COLUMN_MOBILE_PHONE,item.mobile_phone);
		values.put(MySQLiteHelper.U_COLUMN_PO_BOX,item.po_box);
		values.put(MySQLiteHelper.U_COLUMN_ZIP_CODE,item.zip_code);
		values.put(MySQLiteHelper.U_COLUMN_LAND_ID,item.land_id);
		values.put(MySQLiteHelper.U_COLUMN_ALTERNATE_CONTACT_NAME,item.alternate_contact_name);
		values.put(MySQLiteHelper.U_COLUMN_CONTACT_NATIONAL_ID,item.contact_national_id);
		values.put(MySQLiteHelper.U_COLUMN_CONTACT_PHONE_LAND_LINE,item.contact_phone_land_line);
		values.put(MySQLiteHelper.U_COLUMN_CONTACT_MOBILE_PHONE,item.contact_mobile_phone);
		values.put(MySQLiteHelper.U_COLUMN_CONTACT_DAY_EXPIRATION_DATE,item.contact_day_expiration_date);
		values.put(MySQLiteHelper.U_COLUMN_CONTACT_YEAR_EXPIRATION_DATE,item.contact_year_expiration_date);
		values.put(MySQLiteHelper.U_COLUMN_CONTACT_EMAIL,item.contact_email);
		values.put(MySQLiteHelper.U_COLUMN_MONTH_EXPIRATION_DATE,item.month_expiration_date);
		values.put(MySQLiteHelper.U_COLUMN_CONTACT_MONTH_EXPIRATION_DATE,item.contact_month_expiration_date);
		values.put(MySQLiteHelper.U_COLUMN_GENDER,item.gender);
		values.put(MySQLiteHelper.U_COLUMN_NATIONALITY_ID,item.nationality_id);
		values.put(MySQLiteHelper.U_COLUMN_INHABITANT_TYPE_ID,item.inhabitant_type_id);
		values.put(MySQLiteHelper.U_COLUMN_TARSHEED_TYPE_ID,item.tarsheed_type_id);
		values.put(MySQLiteHelper.U_COLUMN_TARSHEED_GIVEN,item.tarsheed_given);
		values.put(MySQLiteHelper.U_COLUMN_PHOTO_OWNER,item.photo_owner);
		values.put(MySQLiteHelper.U_COLUMN_PHOTO_ALTERNATE,item.photo_alternate);
		values.put(MySQLiteHelper.U_COLUMN_PHOTO_INSTRUMENT, item.photo_instrument);
		values.put(MySQLiteHelper.U_COLUMN_CONTACT_STREET_NAME,item.contact_street_name);
		values.put(MySQLiteHelper.U_COLUMN_CONTACT_ZIP_CODE,item.contact_zip_code);
		values.put(MySQLiteHelper.U_COLUMN_CONTACT_PO_BOX,item.contact_po_box);
		values.put(MySQLiteHelper.U_COLUMN_NEW_PREMISE_LAT,item.newPremiseLat);
		values.put(MySQLiteHelper.U_COLUMN_NEW_PREMISE_LNG,item.newPremiseLng);
		values.put(MySQLiteHelper.U_COLUMN_NEW_PREMISE_ALT,item.newPremiseAlt);
		values.put(MySQLiteHelper.U_COLUMN_SYNC_ATTEMPTED, 0);
		
		return database.insert(MySQLiteHelper.TABLE_CUSTOMER, null, values);
	}

	
	/**
	 * DELETE METHODS FOR TABLES CATALOG;PREMISE;CUSTOMER
	 */
	public void deleteAllCatalogItems()
	{
		database.delete(MySQLiteHelper.TABLE_CATALOG, null, null);
	}

	public void deletePremiseItem(int aid)
	{
		database.delete(MySQLiteHelper.TABLE_PREMISE, MySQLiteHelper.P_COLUMN_AUTO_INCREMENT + "=" + aid, null);
	}
	
	public void deleteWaterMeterReadingItem(int aid)
	{
		database.delete(MySQLiteHelper.TABLE_WATER_READING, MySQLiteHelper.W_COLUMN_AUTO_INCREMENT + "=" + aid, null);
	}

	public void deleteWaterLeakItem(int aid)
	{
		database.delete(MySQLiteHelper.TABLE_WATER_LEAK, MySQLiteHelper.L_COLUMN_AUTO_INCREMENT + "=" + aid, null);
	}

	public void deleteCustomerItem(int aid)
	{
		database.delete(MySQLiteHelper.TABLE_CUSTOMER, MySQLiteHelper.U_COLUMN_AUTO_INCREMENT + "=" + aid, null);
	}

	
	/**
	 * GET CATALOG ITEMS
	 * @param type
	 * @return arraylist of items
	 */
	public ArrayList<JSONSpinnerItem>getCatalogItems(String type)
	{
		ArrayList<JSONSpinnerItem> itemsList=new ArrayList<JSONSpinnerItem>();
		
		String sort=null;
		if(type.equals("premise_type"))
			sort=MySQLiteHelper.C_COLUMN_AUTO_INCREMENT + " ASC";
		else 
			sort=MySQLiteHelper.C_COLUMN_TEXT + " ASC";
		
		Cursor cursor=database.query(MySQLiteHelper.TABLE_CATALOG, catalogColumns, MySQLiteHelper.C_COLUMN_TYPE+"='"+type+"'", null, null, null, sort);
		
		cursor.moveToFirst();
		
		while(!cursor.isAfterLast())
		{
			JSONSpinnerItem item=cursorToItem(cursor);
			itemsList.add(item);
			cursor.moveToNext();
		}
		
		return itemsList;
	}
	
	
	
	/**
	 * GET PREMISE ITEMS
	 * @param integer limit
	 * @return arraylist of items
	 */
	public ArrayList<JSONPremiseItem>getPremiseItems(int limit)
	{
		ArrayList<JSONPremiseItem> itemsList=new ArrayList<JSONPremiseItem>();
		
		Cursor cursor;
		if(limit == -1){
			cursor=database.query(MySQLiteHelper.TABLE_PREMISE, premiseColumns,  MySQLiteHelper.P_COLUMN_SYNC_ATTEMPTED+"=0", null, null, null, MySQLiteHelper.P_COLUMN_AUTO_INCREMENT + " ASC");
		} else {
			cursor=database.query(MySQLiteHelper.TABLE_PREMISE, premiseColumns, MySQLiteHelper.P_COLUMN_SYNC_ATTEMPTED+"=0", null, null, null, MySQLiteHelper.P_COLUMN_AUTO_INCREMENT + " ASC",""+limit+"");
		}
		
		cursor.moveToFirst();
		
		while(!cursor.isAfterLast())
		{
			JSONPremiseItem item=cursorToPremiseItem(cursor);
			itemsList.add(item);
			cursor.moveToNext();
		}
		
		return itemsList;
	}
	
	
	/**
	 * GET WATER READING ITEMS
	 * @param integer limit
	 * @return arraylist of items
	 */
	public ArrayList<JSONWaterReadingItem>getWaterReadingItems(int limit)
	{
		ArrayList<JSONWaterReadingItem> itemsList=new ArrayList<JSONWaterReadingItem>();
		
		Cursor cursor;
		if(limit == -1){
			cursor=database.query(MySQLiteHelper.TABLE_WATER_READING, waterReadingColumns,  MySQLiteHelper.W_COLUMN_SYNC_ATTEMPTED+"=0", null, null, null, MySQLiteHelper.W_COLUMN_AUTO_INCREMENT + " ASC");
		} else {
			cursor=database.query(MySQLiteHelper.TABLE_WATER_READING, waterReadingColumns, MySQLiteHelper.W_COLUMN_SYNC_ATTEMPTED+"=0", null, null, null, MySQLiteHelper.W_COLUMN_AUTO_INCREMENT + " ASC",""+limit+"");
		}
		
		cursor.moveToFirst();
		
		while(!cursor.isAfterLast())
		{
			JSONWaterReadingItem item=cursorToWaterMeterReadingItem(cursor);
			itemsList.add(item);
			cursor.moveToNext();
		}
		
		return itemsList;
	}	
	
	
	/**
	 * GET WATER LEAK ITEMS
	 * @param integer limit
	 * @return arraylist of items
	 */
	public ArrayList<JSONReportLeakItem>getWaterLeakItems(int limit)
	{
		ArrayList<JSONReportLeakItem> itemsList=new ArrayList<JSONReportLeakItem>();
		
		Cursor cursor;
		if(limit == -1){
			cursor=database.query(MySQLiteHelper.TABLE_WATER_LEAK, waterLeakColumns,  MySQLiteHelper.L_COLUMN_SYNC_ATTEMPTED+"=0", null, null, null, MySQLiteHelper.L_COLUMN_AUTO_INCREMENT + " ASC");
		} else {
			cursor=database.query(MySQLiteHelper.TABLE_WATER_LEAK, waterLeakColumns, MySQLiteHelper.L_COLUMN_SYNC_ATTEMPTED+"=0", null, null, null, MySQLiteHelper.L_COLUMN_AUTO_INCREMENT + " ASC",""+limit+"");
		}
		
		cursor.moveToFirst();
		
		while(!cursor.isAfterLast())
		{
			JSONReportLeakItem item=cursorToWaterLeakItem(cursor);
			itemsList.add(item);
			cursor.moveToNext();
		}
		
		return itemsList;
	}	
	
	/**
	 * GET CUSTOMER ITEMS
	 * @param int limit
	 * @return arraylist of items
	 */
	public ArrayList<JSONCustomerItem>getCustomerItems(int limit)
	{
		ArrayList<JSONCustomerItem> itemsList=new ArrayList<JSONCustomerItem>();
		
		Cursor cursor;
		if(limit == -1) {
			cursor=database.query(MySQLiteHelper.TABLE_CUSTOMER, customerColumns, MySQLiteHelper.U_COLUMN_SYNC_ATTEMPTED+"=0", null, null, null, MySQLiteHelper.U_COLUMN_AUTO_INCREMENT + " ASC");
		} else {
			cursor=database.query(MySQLiteHelper.TABLE_CUSTOMER, customerColumns, MySQLiteHelper.U_COLUMN_SYNC_ATTEMPTED+"=0", null, null, null, MySQLiteHelper.U_COLUMN_AUTO_INCREMENT + " ASC",""+limit);
		}
		
		cursor.moveToFirst();
		
		while(!cursor.isAfterLast())
		{
			JSONCustomerItem item=cursorToCustomerItem(cursor);
			itemsList.add(item);
			cursor.moveToNext();
		}
		
		return itemsList;
	}
	
	public ArrayList<JSONCustomerItem>getBadCustomerItems(int limit)
	{
		ArrayList<JSONCustomerItem> itemsList=new ArrayList<JSONCustomerItem>();
		
		Cursor cursor;
		if(limit == -1) {
			cursor=database.query(MySQLiteHelper.TABLE_CUSTOMER, customerColumns, MySQLiteHelper.U_COLUMN_SYNC_ATTEMPTED+"<>0", null, null, null, MySQLiteHelper.U_COLUMN_AUTO_INCREMENT + " ASC");
		} else {
			cursor=database.query(MySQLiteHelper.TABLE_CUSTOMER, customerColumns, MySQLiteHelper.U_COLUMN_SYNC_ATTEMPTED+"<>0", null, null, null, MySQLiteHelper.U_COLUMN_AUTO_INCREMENT + " ASC",""+limit);
		}
		
		cursor.moveToFirst();
		
		while(!cursor.isAfterLast())
		{
			JSONCustomerItem item=cursorToCustomerItem(cursor);
			itemsList.add(item);
			cursor.moveToNext();
		}
		
		return itemsList;
	}
	
	public ArrayList<JSONPremiseItem>getBadPremiseItems(int limit)
	{
		ArrayList<JSONPremiseItem> itemsList=new ArrayList<JSONPremiseItem>();
		
		Cursor cursor;
		if(limit == -1) {
			cursor=database.query(MySQLiteHelper.TABLE_PREMISE, premiseColumns, MySQLiteHelper.U_COLUMN_SYNC_ATTEMPTED+"<>0", null, null, null, MySQLiteHelper.P_COLUMN_AUTO_INCREMENT + " ASC");
		} else {
			cursor=database.query(MySQLiteHelper.TABLE_PREMISE, premiseColumns, MySQLiteHelper.U_COLUMN_SYNC_ATTEMPTED+"<>0", null, null, null, MySQLiteHelper.P_COLUMN_AUTO_INCREMENT + " ASC",""+limit);
		}
		
		cursor.moveToFirst();
		
		while(!cursor.isAfterLast())
		{
			JSONPremiseItem item=cursorToPremiseItem(cursor);
			itemsList.add(item);
			cursor.moveToNext();
		}
		
		return itemsList;
	}
	
	
	public void setPremiseError(int pid,String error){
		ContentValues cv = new ContentValues();
		cv.put(MySQLiteHelper.P_COLUMN_SYNC_ATTEMPTED,1);
		cv.put(MySQLiteHelper.P_COLUMN_SYNC_ERROR,error);
		database.update(MySQLiteHelper.TABLE_PREMISE, cv, "aid="+pid, null);
	}

	public void setWaterMeterReadingError(int pid,String error){
		ContentValues cv = new ContentValues();
		cv.put(MySQLiteHelper.W_COLUMN_SYNC_ATTEMPTED,1);
		cv.put(MySQLiteHelper.W_COLUMN_SYNC_ERROR,error);
		database.update(MySQLiteHelper.TABLE_WATER_READING, cv, "aid="+pid, null);
	}


	public void setWaterLeakError(int pid,String error){
		ContentValues cv = new ContentValues();
		cv.put(MySQLiteHelper.L_COLUMN_SYNC_ATTEMPTED,1);
		cv.put(MySQLiteHelper.L_COLUMN_SYNC_ERROR,error);
		database.update(MySQLiteHelper.TABLE_WATER_LEAK, cv, "aid="+pid, null);
	}

	public void setCustomerError(int cid,String error){
		ContentValues cv = new ContentValues();
		cv.put(MySQLiteHelper.U_COLUMN_SYNC_ATTEMPTED,1);
		cv.put(MySQLiteHelper.U_COLUMN_SYNC_ERROR,error);
		database.update(MySQLiteHelper.TABLE_CUSTOMER, cv, "aid="+cid, null);
	}

	/**
	 * GET COUNT OF ITEMS I TABLES CATALOG;PREMISE;CUSTOMER
	 * @return
	 */
	public int catalogItemCount(){
		Cursor cursor=database.query(MySQLiteHelper.TABLE_CATALOG, catalogColumns, null, null, null, null, null);
		return cursor.getCount();
	}
	
	public int catalogDistrictItemCount(){
		Cursor cursor=database.query(MySQLiteHelper.TABLE_CATALOG, catalogColumns, MySQLiteHelper.C_COLUMN_TYPE+"='district'", null, null, null, null);
		return cursor.getCount();
	}

	public int catalogWaterMeterStatusItemCount(){
		Cursor cursor=database.query(MySQLiteHelper.TABLE_CATALOG, catalogColumns, MySQLiteHelper.C_COLUMN_TYPE+"='water_meter_status'", null, null, null, null);
		return cursor.getCount();
	}


	public int premiseItemCount(){
		Cursor cursor=database.query(MySQLiteHelper.TABLE_PREMISE, premiseColumns, MySQLiteHelper.P_COLUMN_SYNC_ATTEMPTED+"=0", null, null, null, null);
		return cursor.getCount();
	}

	public int waterMeterReadingItemCount(){
		Cursor cursor=database.query(MySQLiteHelper.TABLE_WATER_READING, waterReadingColumns, MySQLiteHelper.W_COLUMN_SYNC_ATTEMPTED+"=0", null, null, null, null);
		return cursor.getCount();
	}

	public int waterLeakItemCount(){
		Cursor cursor=database.query(MySQLiteHelper.TABLE_WATER_LEAK, waterLeakColumns, MySQLiteHelper.L_COLUMN_SYNC_ATTEMPTED+"=0", null, null, null, null);
		return cursor.getCount();
	}

	public int customerItemCount(){
		Cursor cursor=database.query(MySQLiteHelper.TABLE_CUSTOMER, customerColumns, MySQLiteHelper.U_COLUMN_SYNC_ATTEMPTED+"=0", null, null, null, null);
		return cursor.getCount();
	}

	
	
	private JSONSpinnerItem cursorToItem(Cursor cursor)
	{
		JSONSpinnerItem item=new JSONSpinnerItem();
		item.id=cursor.getInt(1);
		item.text=cursor.getString(2);
		return item;
	}
	
	private JSONWaterReadingItem cursorToWaterMeterReadingItem(Cursor cursor)
	{
		JSONWaterReadingItem item=new JSONWaterReadingItem();
		item.id=cursor.getInt(0);
		item.gps_latitude_created=cursor.getString(1);
		item.gps_longitude_created=cursor.getString(2);
		item.gps_altitude_created=cursor.getString(3);
		item.photo_water_meter=cursor.getString(4);
		item.house_connection_number=cursor.getString(5);
		item.sec_db_number=cursor.getString(6);
		item.water_meter_number=cursor.getString(7);
		//item.water_meter_number2=cursor.getString(8);
		item.water_meter_reading=cursor.getString(8);
		item.dynamic_photos=cursor.getString(11);	
		item.manual_source=cursor.getInt(12);
		item.premise_name=cursor.getString(13);
		item.district_id=cursor.getInt(14);
		item.area_name=cursor.getString(15);
		return item;
	}


	
private JSONReportLeakItem cursorToWaterLeakItem(Cursor cursor)
	{
		JSONReportLeakItem item=new JSONReportLeakItem();
		item.id=cursor.getInt(0);
		item.gps_latitude_created=cursor.getString(1);
		item.gps_longitude_created=cursor.getString(2);
		item.gps_altitude_created=cursor.getString(3);
		item.photo_water_leak=cursor.getString(4);
		item.house_connection_number=cursor.getString(5);
		item.description=cursor.getString(6);
		item.ticket_serial_number=cursor.getString(7);
		item.dynamic_photos=cursor.getString(10);	
		item.manual_source=cursor.getInt(11);	
		item.illegal_connect=cursor.getInt(12);
		item.leak_type=cursor.getInt(13);
		item.violation_penalty=cursor.getString(14);	
		item.water_meter_reading_number=cursor.getString(15);
		item.ticket_id=cursor.getString(16);	
		item.water_meter_number=cursor.getString(17);
		//item.water_meter_number2=cursor.getString(18);
		item.sec_db_number=cursor.getString(19);
		item.premise_name=cursor.getString(20);
		item.premise_type_id=cursor.getString(21);
		return item;
	}
	
	private JSONPremiseItem cursorToPremiseItem(Cursor cursor)
	{
		JSONPremiseItem item=new JSONPremiseItem();
		item.id=cursor.getInt(0);
		item.gps_latitude_created=cursor.getString(1);
		item.gps_longitude_created=cursor.getString(2);
		item.gps_altitude_created=cursor.getString(3);
		item.gps_latitude=cursor.getString(4);
		item.gps_longitude=cursor.getString(5);
		item.gps_altitude=cursor.getString(6);
		item.house_connection_number=cursor.getString(7);
		item.premise_id=cursor.getString(8);
		item.stc_db_number=cursor.getString(9);
		item.sec_db_number=cursor.getString(10);
		item.sec_db_number2=cursor.getString(11);
		item.sec_db_number3=cursor.getString(12);
		item.sec_db_number4=cursor.getString(13);
		item.sec_db_number5=cursor.getString(14);
		item.total_electrical_meters=cursor.getString(15);
		item.floor_count=cursor.getString(16);
		item.use_of_building=cursor.getString(17);
		item.premise_type_id=cursor.getString(18);
		item.photo_house1=cursor.getString(19);
		item.photo_house2=cursor.getString(20);
		item.photo_house3=cursor.getString(21);
		item.photo_premise_connection=cursor.getString(22);
		item.photo_stc=cursor.getString(23);
		item.photo_sec=cursor.getString(24);
		item.water_meter_number=cursor.getString(25);
	    item.water_meter_number2=cursor.getString(26);
	    item.water_meter_number3=cursor.getString(27);
	    item.water_meter_number4=cursor.getString(28);
	    item.water_meter_number5=cursor.getString(29);
		item.district=cursor.getString(30);
		item.errors=cursor.getString(31);
		item.water_meter_status=cursor.getString(32);
		item.hcnAutoGenerated=cursor.getString(33);
		item.premise_name=cursor.getString(34);
		item.only_images=cursor.getInt(35);
		return item;
	}
	
	private JSONCustomerItem cursorToCustomerItem(Cursor cursor)
	{
		JSONCustomerItem item=new JSONCustomerItem();
		item.id=cursor.getInt(0);
		item.gps_latitude_created=cursor.getString(1);
		item.gps_longitude_created=cursor.getString(2);
		item.gps_altitude_created=cursor.getString(3);
		item.house_connection_number=cursor.getString(4);
		item.full_name=cursor.getString(5);
		item.national_id=cursor.getString(6);
		item.day_expiration_date=cursor.getString(7);
		item.year_expiration_date=cursor.getString(8);
		item.email=cursor.getString(9);
		item.phone_land_line=cursor.getString(10);
		item.mobile_phone=cursor.getString(11);
		item.po_box=cursor.getString(12);
		item.zip_code=cursor.getString(13);
		item.land_id=cursor.getString(14);
		item.alternate_contact_name=cursor.getString(15);
		item.contact_national_id=cursor.getString(16);
		item.contact_phone_land_line=cursor.getString(17);
		item.contact_mobile_phone=cursor.getString(18);
		item.contact_day_expiration_date=cursor.getString(19);
		item.contact_year_expiration_date=cursor.getString(20);
		item.contact_email=cursor.getString(21);
		item.month_expiration_date=cursor.getString(22);
		item.contact_month_expiration_date=cursor.getString(23);
		item.gender=cursor.getString(24);
		item.nationality_id=cursor.getString(25);
		item.inhabitant_type_id=cursor.getString(26);
		item.tarsheed_type_id=cursor.getString(27);
		item.tarsheed_given=cursor.getString(28);
		item.photo_owner=cursor.getString(29);
		item.photo_alternate=cursor.getString(30);
		item.photo_instrument=cursor.getString(31);
		item.contact_street_name=cursor.getString(32);
		item.contact_zip_code=cursor.getString(33);
		item.contact_po_box=cursor.getString(34);
		item.newPremiseLat=cursor.getString(35);
		item.newPremiseLng=cursor.getString(36);
		item.newPremiseAlt=cursor.getString(37);
		item.errors=cursor.getString(39);
		return item;
	}
	
	public void open() throws SQLException
	{
		database=databaseHelper.getWritableDatabase();
	}
	
	public void close()
	{
		databaseHelper.close();
	}
	
}

