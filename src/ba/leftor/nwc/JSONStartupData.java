package ba.leftor.nwc;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;

public class JSONStartupData {
	public int appVersion;
	
	public JSONUSerItem user;
	
	@SerializedName("inhabitantTypes")
	public ArrayList<JSONSpinnerItem> lstInhabitantTypes;
	
	@SerializedName("nationalities")
	public ArrayList<JSONSpinnerItem> lstNationalities;
	
	@SerializedName("tarsheedTypes")
	public ArrayList<JSONSpinnerItem> lstTarsheedTypes;
	
	@SerializedName("premiseTypes")
	public ArrayList<JSONSpinnerItem> lstPremiseTypes;
	
	@SerializedName("districts")
	public ArrayList<JSONSpinnerItem> lstDistricts;
	
	@SerializedName("waterMeterStatuses")
	public ArrayList<JSONSpinnerItem> lstWaterMeterStatuses;

	@SerializedName("violationPenalties")
	public ArrayList<JSONSpinnerItem> lstViolationPenalties;
}
