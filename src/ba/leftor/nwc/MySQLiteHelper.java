package ba.leftor.nwc;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

	/**defini���imo tabele i kolone tabela 
	 * 1.tabela: catalog - predstavlja tabelu u kojoj ce se smjestat sifrarnici za spinnere
	 *   ----(id,text,type) -- type je ime tabele u bazi na serveru (inhabitant_type,nationality...)
	 * 
	 */
	public static final String TABLE_CATALOG="catalog";
	public static final String C_COLUMN_AUTO_INCREMENT="aid";
	public static final String C_COLUMN_ID="id";
	public static final String C_COLUMN_TEXT="text";
	public static final String C_COLUMN_TYPE="type";

	public static final String TABLE_PREMISE="premise";
	public static final String P_COLUMN_AUTO_INCREMENT="aid";
	public static final String P_COLUMN_GPS_LATITUDE_CREATED="gps_lat_created";
	public static final String P_COLUMN_GPS_LONGITUDE_CREATED="gps_long_created";
	public static final String P_COLUMN_GPS_ALTITUDE_CREATED="gps_alt_created";
	public static final String P_COLUMN_GPS_LATITUDE="gps_lat";
	public static final String P_COLUMN_GPS_LONGITUDE="gps_long";
	public static final String P_COLUMN_GPS_ALTITUDE="gps_alt";
	public static final String P_COLUMN_HOUSE_CONNECTION_NUMBER="hcn";
	public static final String P_COLUMN_PREMISE_ID="pid";
	public static final String P_COLUMN_STC_DB_NUMBER="stc";
	public static final String P_COLUMN_SEC_DB_NUMBER="sec";
	public static final String P_COLUMN_SEC_DB_NUMBER_2="sec2";
	public static final String P_COLUMN_SEC_DB_NUMBER_3="sec3";
	public static final String P_COLUMN_SEC_DB_NUMBER_4="sec4";
	public static final String P_COLUMN_SEC_DB_NUMBER_5="sec5";
	public static final String P_COLUMN_TOTAL_ELECTRICAL_METERS="tot_meters";
	public static final String P_COLUMN_FLOOR_COUNT="floor_count";
	public static final String P_COLUMN_PREMISE_NAME="premise_name";
	public static final String P_COLUMN_USE_OF_BUILDING="use_of_build";
	public static final String P_COLUMN_PREMISE_TYPE_ID="ptid";
	public static final String P_COLUMN_PHOTO_HOUSE1="photoh1";
	public static final String P_COLUMN_PHOTO_HOUSE2="photoh2";
	public static final String P_COLUMN_PHOTO_HOUSE3="photoh3";
	public static final String P_COLUMN_PHOTO_PREMISE_CONNECTION="photopcn";
	public static final String P_COLUMN_PHOTO_STC="photostc";
	public static final String P_COLUMN_PHOTO_SEC="photosec";
	public static final String P_COLUMN_WATER_METER_NUMBER="water_meter_num";
	public static final String P_COLUMN_WATER_METER_NUMBER_2="water_meter_num2";
	public static final String P_COLUMN_WATER_METER_NUMBER_3="water_meter_num3";
	public static final String P_COLUMN_WATER_METER_NUMBER_4="water_meter_num4";
	public static final String P_COLUMN_WATER_METER_NUMBER_5="water_meter_num5";
	public static final String P_COLUMN_DISTRICT="district";
	public static final String P_COLUMN_WATER_METER_STATUS="water_meter_status";
	public static final String P_COLUMN_HCN_AUTO_GENERATED="hcn_auto_generated";
	public static final String P_COLUMN_SYNC_ATTEMPTED="sync_attempt";
	public static final String P_COLUMN_SYNC_ERROR="sync_err";
	public static final String P_COLUMN_ONLY_IMAGES="only_images";

	
	
	public static final String TABLE_CUSTOMER="customer";
	public static final String U_COLUMN_AUTO_INCREMENT="aid";
	public static final String U_COLUMN_GPS_LATITUDE_CREATED="gps_lat_created";
	public static final String U_COLUMN_GPS_LONGITUDE_CREATED="gps_long_created";
	public static final String U_COLUMN_GPS_ALTITUDE_CREATED="gps_alt_created";
	public static final String U_COLUMN_HOUSE_CONNECTION_NUMBER="hcn";
	public static final String U_COLUMN_FULL_NAME="full_name";
	public static final String U_COLUMN_NATIONAL_ID="national_id";
	public static final String U_COLUMN_DAY_EXPIRATION_DATE="dayExpirationDate";
	public static final String U_COLUMN_YEAR_EXPIRATION_DATE="yearExpirationDate";
	public static final String U_COLUMN_EMAIL="email";
	public static final String U_COLUMN_PHONE_LAND_LINE="phone_land_line";
	public static final String U_COLUMN_MOBILE_PHONE="mobile_phone";
	public static final String U_COLUMN_PO_BOX="po_box";
	public static final String U_COLUMN_ZIP_CODE="zip_code";
	public static final String U_COLUMN_LAND_ID="land_id";
	public static final String U_COLUMN_ALTERNATE_CONTACT_NAME="alternate_contact_name";
	public static final String U_COLUMN_CONTACT_NATIONAL_ID="contact_national_id";
	public static final String U_COLUMN_CONTACT_PHONE_LAND_LINE="contact_phone_land_line";
	public static final String U_COLUMN_CONTACT_MOBILE_PHONE="contact_mobile_phone";
	public static final String U_COLUMN_CONTACT_DAY_EXPIRATION_DATE="contactDayExpirationDate";
	public static final String U_COLUMN_CONTACT_YEAR_EXPIRATION_DATE="contactYearExpirationDate";
	public static final String U_COLUMN_CONTACT_EMAIL="contact_email";
	public static final String U_COLUMN_MONTH_EXPIRATION_DATE="monthExpirationDate";
	public static final String U_COLUMN_CONTACT_MONTH_EXPIRATION_DATE="contactMonthExpirationDate";
	public static final String U_COLUMN_GENDER="gender";
	public static final String U_COLUMN_NATIONALITY_ID="nationality_id";
	public static final String U_COLUMN_INHABITANT_TYPE_ID="inhabitant_type_id";
	public static final String U_COLUMN_TARSHEED_TYPE_ID="tarsheed_type_id";
	public static final String U_COLUMN_TARSHEED_GIVEN="tarsheed_given";
	public static final String U_COLUMN_PHOTO_OWNER="photo_owner";
	public static final String U_COLUMN_PHOTO_ALTERNATE="photo_alternate";
	public static final String U_COLUMN_SYNC_ATTEMPTED="sync_attempt";
	public static final String U_COLUMN_PHOTO_INSTRUMENT="photoins";
	
	public static final String U_COLUMN_CONTACT_STREET_NAME ="contact_street_name";
	public static final String U_COLUMN_CONTACT_ZIP_CODE="contact_zip_code";
	public static final String U_COLUMN_CONTACT_PO_BOX="contact_po_box";
	
	public static final String U_COLUMN_NEW_PREMISE_LAT="new_premise_lat";
	public static final String U_COLUMN_NEW_PREMISE_LNG="new_premise_lng";
	public static final String U_COLUMN_NEW_PREMISE_ALT="new_premise_alt";
	
	public static final String U_COLUMN_SYNC_ERROR="sync_err";

	
	
	public static final String TABLE_WATER_READING="water_reading";
	public static final String W_COLUMN_AUTO_INCREMENT="aid";
	public static final String W_COLUMN_GPS_LATITUDE_CREATED="gps_lat_created";
	public static final String W_COLUMN_GPS_LONGITUDE_CREATED="gps_long_created";
	public static final String W_COLUMN_GPS_ALTITUDE_CREATED="gps_alt_created";
	public static final String W_COLUMN_HOUSE_CONNECTION_NUMBER="hcn";
	public static final String W_COLUMN_SEC_DB_NUMBER="sec";
	public static final String W_COLUMN_WATER_METER_NUMBER="wmn";
	
	public static final String W_COLUMN_WATER_READING_NUMBER="wrn";
	public static final String W_COLUMN_PREMISE_NAME="premise_name";
	public static final String W_COLUMN_PHOTO_INSTRUMENT="photo";
	public static final String W_COLUMN_SYNC_ATTEMPTED="sync_attempt";
	public static final String W_COLUMN_SYNC_ERROR="sync_err";
	public static final String W_COLUMN_DYNAMIC_PHOTOS="dynamic_photos";
	public static final String W_COLUMN_MANUAL_SOURCE="manual";
	public static final String W_COLUMN_DISTRICT_ID="district_id";
	public static final String W_COLUMN_AREA_NAME="area_name";
	
	public static final String TABLE_WATER_LEAK="water_leak";
	public static final String L_COLUMN_AUTO_INCREMENT="aid";
	public static final String L_COLUMN_GPS_LATITUDE_CREATED="gps_lat_created";
	public static final String L_COLUMN_GPS_LONGITUDE_CREATED="gps_long_created";
	public static final String L_COLUMN_GPS_ALTITUDE_CREATED="gps_alt_created";
	public static final String L_COLUMN_HOUSE_CONNECTION_NUMBER="hcn";
	public static final String L_COLUMN_WATER_LEAK_DESCRIPTION="leak_description";
	public static final String L_COLUMN_PHOTO_LEAK="photo_leak";
	public static final String L_COLUMN_SYNC_ATTEMPTED="sync_attempt";
	public static final String L_COLUMN_SYNC_ERROR="sync_err";
	public static final String L_COLUMN_DYNAMIC_PHOTOS="dynamic_photos";
	public static final String L_COLUMN_WATER_TICKET_SERIAL_NUMBER="ticket_serial_number";
	public static final String L_COLUMN_MANUAL_SOURCE="manual";
	public static final String L_COLUMN_ILLEGAL_CONNECT="illegal_connect";
	public static final String L_COLUMN_LEAK_TYPE="leak_type";
	public static final String L_COLUMN_VIOLATION_PENALTY="violation_penalty";
	public static final String L_COLUMN_WATER_METER_READING_NUMBER="water_meter_reading_number";
	public static final String L_COLUMN_TICKET_ID="ticket_id";
	public static final String L_COLUMN_WATER_METER_NUMBER="water_meter_number";
	
	public static final String L_COLUMN_SEC_DB_NUMBER="sec_db_number";
	public static final String L_COLUMN_PREMISE_NAME="premise_name";
	public static final String L_COLUMN_PREMISE_TYPE_ID="premise_type_id";
	
	public static final String DATABASE_NAME="nwcuae.db";
	public static final int DATABASE_VERSION=32;
	
	
	//Database creation sql statment
	/** CATALOG TABLE FOR STORING SPINNER LISTS **/
	public static final String DATABASE_CREATE_CATALOG="create table "+ TABLE_CATALOG + "(" + 
			C_COLUMN_AUTO_INCREMENT + " integer primary key AUTOINCREMENT, " + 
			C_COLUMN_ID + " integer not null, " + 
			C_COLUMN_TEXT + " VARCHAR(100) not null, "+ 
			C_COLUMN_TYPE + " VARCHAR(100) not null" + 
		");";
	
			
	/** PREMISE TABLE FOR STORING OFFLINE PREMISE DATA **/
	public static final String DATABASE_CREATE_PREMISE="create table "+ TABLE_PREMISE + "(" + 
			P_COLUMN_AUTO_INCREMENT + " integer primary key AUTOINCREMENT, " + 
			P_COLUMN_GPS_LATITUDE_CREATED + " VARCHAR(100), " +
			P_COLUMN_GPS_LONGITUDE_CREATED + " VARCHAR(100), " +
			P_COLUMN_GPS_ALTITUDE_CREATED + " VARCHAR(100), " +
			P_COLUMN_GPS_LATITUDE + " VARCHAR(100), " +
			P_COLUMN_GPS_LONGITUDE + " VARCHAR(100), " +
			P_COLUMN_GPS_ALTITUDE + " VARCHAR(100), " +
			P_COLUMN_HOUSE_CONNECTION_NUMBER + " VARCHAR(100), " +
			P_COLUMN_PREMISE_ID + " VARCHAR(100), " +
			P_COLUMN_STC_DB_NUMBER + " VARCHAR(100), " +
			P_COLUMN_SEC_DB_NUMBER + " VARCHAR(100), " +
			P_COLUMN_SEC_DB_NUMBER_2 + " VARCHAR(100)," +
			P_COLUMN_SEC_DB_NUMBER_3 + " VARCHAR(100)," +
			P_COLUMN_SEC_DB_NUMBER_4 + " VARCHAR(100)," +
			P_COLUMN_SEC_DB_NUMBER_5 + " VARCHAR(100)," +
			P_COLUMN_TOTAL_ELECTRICAL_METERS + " VARCHAR(100), " +
			P_COLUMN_FLOOR_COUNT + " VARCHAR(100), " +
			P_COLUMN_PREMISE_NAME + " VARCHAR(50), " +
			P_COLUMN_USE_OF_BUILDING + " VARCHAR(100), " +
			P_COLUMN_PREMISE_TYPE_ID + " VARCHAR(100), " +
			P_COLUMN_PHOTO_HOUSE1 + " VARCHAR(100), " +
			P_COLUMN_PHOTO_HOUSE2 + " VARCHAR(100), " +
			P_COLUMN_PHOTO_HOUSE3 + " VARCHAR(100), " +
			P_COLUMN_PHOTO_PREMISE_CONNECTION + " VARCHAR(100), " +
			P_COLUMN_PHOTO_STC + " VARCHAR(100), " +
			P_COLUMN_PHOTO_SEC + " VARCHAR(100)," + 
			P_COLUMN_WATER_METER_NUMBER + " VARCHAR(100)," + 
			P_COLUMN_WATER_METER_NUMBER_2 + " VARCHAR(100)," +
			P_COLUMN_WATER_METER_NUMBER_3 + " VARCHAR(100)," +
			P_COLUMN_WATER_METER_NUMBER_4 + " VARCHAR(100)," +
			P_COLUMN_WATER_METER_NUMBER_5 + " VARCHAR(100)," +
			P_COLUMN_SYNC_ATTEMPTED + " integer," + 
			P_COLUMN_SYNC_ERROR + " TEXT," + 
			P_COLUMN_DISTRICT + " VARCHAR(50)," + 
			P_COLUMN_WATER_METER_STATUS + " VARCHAR(10)," + 
			P_COLUMN_HCN_AUTO_GENERATED + " VARCHAR(10)," + 
			P_COLUMN_ONLY_IMAGES + " integer" + 
		");";
	
	/** WTER METER READING TABLE **/
	public static final String DATABASE_CREATE_WATER_READING="create table "+ TABLE_WATER_READING + "(" + 
			W_COLUMN_AUTO_INCREMENT + " integer primary key AUTOINCREMENT, " + 
			W_COLUMN_GPS_LATITUDE_CREATED + " VARCHAR(100), " +
			W_COLUMN_GPS_LONGITUDE_CREATED + " VARCHAR(100), " +
			W_COLUMN_GPS_ALTITUDE_CREATED + " VARCHAR(100), " +
			W_COLUMN_PHOTO_INSTRUMENT + " VARCHAR(100), " +
			W_COLUMN_HOUSE_CONNECTION_NUMBER + " VARCHAR(100), " +
			W_COLUMN_SEC_DB_NUMBER + " VARCHAR(100), " +
			W_COLUMN_WATER_METER_NUMBER + " VARCHAR(100), " +
			W_COLUMN_WATER_READING_NUMBER + " VARCHAR(100), " + 
			W_COLUMN_SYNC_ATTEMPTED + " integer, " + 
			W_COLUMN_SYNC_ERROR + " TEXT, " + 
			W_COLUMN_DYNAMIC_PHOTOS + " TEXT, " + 
			W_COLUMN_MANUAL_SOURCE + " integer, " + 
			W_COLUMN_PREMISE_NAME + " VARCHAR(50), " +
			W_COLUMN_DISTRICT_ID + " integer, " + 
			W_COLUMN_AREA_NAME + " VARCHAR(50) " +
		");";

	/** WTER METER LEAK TABLE **/
	public static final String DATABASE_CREATE_WATER_LEAK="create table "+ TABLE_WATER_LEAK + "(" + 
			L_COLUMN_AUTO_INCREMENT + " integer primary key AUTOINCREMENT, " + 
			L_COLUMN_GPS_LATITUDE_CREATED + " VARCHAR(100), " +
			L_COLUMN_GPS_LONGITUDE_CREATED + " VARCHAR(100), " +
			L_COLUMN_GPS_ALTITUDE_CREATED + " VARCHAR(100), " +
			L_COLUMN_PHOTO_LEAK + " VARCHAR(100), " +
			L_COLUMN_HOUSE_CONNECTION_NUMBER + " VARCHAR(100), " +
			L_COLUMN_WATER_LEAK_DESCRIPTION + " TEXT, " + 
			L_COLUMN_WATER_TICKET_SERIAL_NUMBER + " TEXT, "+
			L_COLUMN_SYNC_ATTEMPTED + " integer, " + 
			L_COLUMN_SYNC_ERROR + " TEXT," + 
			L_COLUMN_DYNAMIC_PHOTOS + " TEXT, " + 
			L_COLUMN_MANUAL_SOURCE + " integer, " + 
			L_COLUMN_ILLEGAL_CONNECT + " integer, " + 
			L_COLUMN_LEAK_TYPE + " integer, " + 
			L_COLUMN_VIOLATION_PENALTY + " TEXT," + 
			L_COLUMN_WATER_METER_READING_NUMBER + " TEXT, " + 
			L_COLUMN_TICKET_ID + " VARCHAR(50), " +
			L_COLUMN_WATER_METER_NUMBER + " VARCHAR(50), " +
			L_COLUMN_SEC_DB_NUMBER + " VARCHAR(50), " +
			L_COLUMN_PREMISE_NAME + " VARCHAR(50), " +
			L_COLUMN_PREMISE_TYPE_ID + " VARCHAR(50) " +
		");";
	
		
	/**CUSTOMER TABLE FOR STORING OFFLINE CUSTOMER DATA**/
	public static final String DATABASE_CREATE_CUSTOMER="create table "+ TABLE_CUSTOMER + "(" + 
			U_COLUMN_AUTO_INCREMENT + " integer primary key AUTOINCREMENT, " + 
			U_COLUMN_GPS_LATITUDE_CREATED + " VARCHAR(100), " +
			U_COLUMN_GPS_LONGITUDE_CREATED + " VARCHAR(100), " +
			U_COLUMN_GPS_ALTITUDE_CREATED + " VARCHAR(100), " +
			U_COLUMN_HOUSE_CONNECTION_NUMBER + " VARCHAR(100), " +
			U_COLUMN_FULL_NAME + " VARCHAR(100), " +
			U_COLUMN_NATIONAL_ID + " VARCHAR(100), " +
			U_COLUMN_DAY_EXPIRATION_DATE + " VARCHAR(100), " +
			U_COLUMN_YEAR_EXPIRATION_DATE + " VARCHAR(100), " +
			U_COLUMN_EMAIL + " VARCHAR(100), " +
			U_COLUMN_PHONE_LAND_LINE + " VARCHAR(100), " +
			U_COLUMN_MOBILE_PHONE + " VARCHAR(100), " +
			U_COLUMN_PO_BOX + " VARCHAR(100), " +
			U_COLUMN_ZIP_CODE + " VARCHAR(100), " +
			U_COLUMN_LAND_ID + " VARCHAR(100), " +
			U_COLUMN_ALTERNATE_CONTACT_NAME + " VARCHAR(100), " +
			U_COLUMN_CONTACT_NATIONAL_ID + " VARCHAR(100), " +
			U_COLUMN_CONTACT_PHONE_LAND_LINE + " VARCHAR(100), " +
			U_COLUMN_CONTACT_MOBILE_PHONE + " VARCHAR(100), " +
			U_COLUMN_CONTACT_DAY_EXPIRATION_DATE + " VARCHAR(100), " +
			U_COLUMN_CONTACT_YEAR_EXPIRATION_DATE + " VARCHAR(100), " +
			U_COLUMN_CONTACT_EMAIL + " VARCHAR(100), " +
			U_COLUMN_MONTH_EXPIRATION_DATE + " VARCHAR(100), " +
			U_COLUMN_CONTACT_MONTH_EXPIRATION_DATE + " VARCHAR(100), " +
			U_COLUMN_GENDER + " VARCHAR(100), " +
			U_COLUMN_NATIONALITY_ID + " VARCHAR(100), " +
			U_COLUMN_INHABITANT_TYPE_ID + " VARCHAR(100), " +
			U_COLUMN_TARSHEED_TYPE_ID + " VARCHAR(100), " +
			U_COLUMN_TARSHEED_GIVEN + " VARCHAR(100), " +
			U_COLUMN_PHOTO_OWNER + " VARCHAR(100), " +
			U_COLUMN_PHOTO_ALTERNATE + " VARCHAR(100)," +
			U_COLUMN_PHOTO_INSTRUMENT + " VARCHAR(100), " +
			U_COLUMN_CONTACT_STREET_NAME + " VARCHAR(100), " +
			U_COLUMN_CONTACT_ZIP_CODE + " VARCHAR(100), " +
			U_COLUMN_CONTACT_PO_BOX + " VARCHAR(100), " +
			U_COLUMN_NEW_PREMISE_LAT + " VARCHAR(100), " +
			U_COLUMN_NEW_PREMISE_LNG + " VARCHAR(100), " +
			U_COLUMN_NEW_PREMISE_ALT + " VARCHAR(100), " +
			U_COLUMN_SYNC_ATTEMPTED + " integer," + 
			U_COLUMN_SYNC_ERROR + " TEXT" + 
		");";


	
	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE_CATALOG);
		database.execSQL(DATABASE_CREATE_PREMISE);
		database.execSQL(DATABASE_CREATE_WATER_READING);
		database.execSQL(DATABASE_CREATE_WATER_LEAK);
		database.execSQL(DATABASE_CREATE_CUSTOMER);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		Log.w(
			MySQLiteHelper.class.getName(),
			"Upgrading from old version "+oldVersion + " to new version "+ newVersion +
			" which will destroy all data");
		db.execSQL("DROP TABLE IF EXISTS "+TABLE_CATALOG);
		db.execSQL("DROP TABLE IF EXISTS "+TABLE_CUSTOMER);
		db.execSQL("DROP TABLE IF EXISTS "+TABLE_WATER_READING);
		db.execSQL("DROP TABLE IF EXISTS "+TABLE_WATER_LEAK);
		db.execSQL("DROP TABLE IF EXISTS "+TABLE_PREMISE);
		onCreate(db);
		
	}

}
