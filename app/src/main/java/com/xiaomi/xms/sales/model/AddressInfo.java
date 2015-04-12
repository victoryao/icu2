package com.xiaomi.xms.sales.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AddressInfo {
    private String mAddressId;
    private String mConsignee;
    private String mAddress;
    private String mZipCode;
    private String mTel;
    private String mCountry;
    private int mCountryId;
    private String mProvince;
    private int mProvinceId;
    private String mCity;
    private int mCityId;
    private String mDistrict;
    private int mDistrictId;

    public AddressInfo(String addressId, String consignee, String address, String zipCode,
            String tel, String country, int countryId, String province, int provinceId,
            String city, int cityId, String district, int districtId) {
        mAddressId = addressId;
        mConsignee = consignee;
        mAddress = address;
        mZipCode = zipCode;
        mTel = tel;
        mCountry = country;
        mCountryId = countryId;
        mProvince = province;
        mProvinceId = provinceId;
        mCity = city;
        mCityId = cityId;
        mDistrict = district;
        mDistrictId = districtId;
    }

    public String getAddressId() {
        return mAddressId;
    }

    public String getConsignee() {
        return mConsignee;
    }

    public String getAddress() {
        return mAddress;
    }

    public String getZipCode() {
        return mZipCode;
    }

    public String getTel() {
        return mTel;
    }

    public String getCountry() {
        return mCountry;
    }

    public int getCountryId() {
        return mCountryId;
    }

    public String getProvince() {
        return mProvince;
    }

    public int getProvinceId() {
        return mProvinceId;
    }

    public String getCity() {
        return mCity;
    }

    public int getCityId() {
        return mCityId;
    }

    public String getDistrict() {
        return mDistrict;
    }

    public int getDistrictId() {
        return mDistrictId;
    }

    public static ArrayList<AddressInfo> fromJSONObject(JSONObject json) throws JSONException {
        ArrayList<AddressInfo> list = null;

        if (Tags.isJSONResultOK(json)) {
            JSONArray addrJsonArray = json.getJSONArray(Tags.DATA);
            if (addrJsonArray != null) {
                list = new ArrayList<AddressInfo>();
                for (int i = 0; i < addrJsonArray.length(); i++) {
                    if (!addrJsonArray.isNull(i)) {
                        JSONObject addrJsonObject = addrJsonArray.getJSONObject(i);
                        String addrId = addrJsonObject.getString(Tags.AddressInfo.ID);
                        String consignee = addrJsonObject.getString(Tags.AddressInfo.CONSIGNEE);
                        String address = addrJsonObject.getString(Tags.AddressInfo.ADDRESS);
                        String zipCode = addrJsonObject.getString(Tags.AddressInfo.ZIPCODE);
                        String tel = addrJsonObject.getString(Tags.AddressInfo.TEL);
                        String country = addrJsonObject.getJSONObject(Tags.AddressInfo.COUNTRY)
                                    .getString(Tags.AddressInfo.AREA_NAME);
                        int countryId = Integer.parseInt(addrJsonObject.getJSONObject(
                                    Tags.AddressInfo.COUNTRY).getString(Tags.AddressInfo.AREA_ID));
                        String province = addrJsonObject.getJSONObject(
                                    Tags.AddressInfo.PROVINCE)
                                    .getString(Tags.AddressInfo.AREA_NAME);
                        int provinceId = Integer.parseInt(addrJsonObject.getJSONObject(
                                    Tags.AddressInfo.PROVINCE).getString(Tags.AddressInfo.AREA_ID));
                        String city = addrJsonObject.getJSONObject(Tags.AddressInfo.CITY)
                                    .getString(Tags.AddressInfo.AREA_NAME);
                        int cityId = Integer.parseInt(addrJsonObject.getJSONObject(
                                    Tags.AddressInfo.CITY).getString(Tags.AddressInfo.AREA_ID));
                        String district = addrJsonObject.getJSONObject(
                                    Tags.AddressInfo.DISTRICT)
                                    .getString(Tags.AddressInfo.AREA_NAME);
                        int districtId = Integer.parseInt(addrJsonObject.getJSONObject(
                                    Tags.AddressInfo.DISTRICT).getString(Tags.AddressInfo.AREA_ID));

                        list.add(new AddressInfo(addrId, consignee, address, zipCode, tel,
                                    country, countryId, province, provinceId, city, cityId,
                                    district, districtId));

                    }
                }
            }
        }

        return list;
    }
}
