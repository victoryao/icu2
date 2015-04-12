
package com.xiaomi.xms.sales.model;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CategoryInfo {
    private String mCategoryId;
    private String mCategoryName;
    private Image mCategoryPhoto;
    private boolean hasChildren;
    private String mDataType;

    public CategoryInfo(String categoryId, String name, Image photo,
            boolean hasChildren) {
        mCategoryId = categoryId;
        mCategoryName = name;
        mCategoryPhoto = photo;
        this.hasChildren = hasChildren;
    }

    public String getName() {
        return mCategoryName;
    }

    public Image getPhoto() {
        return mCategoryPhoto;
    }

    public boolean hasChildren() {
        return hasChildren;
    }

    public String getCategoryId() {
        return mCategoryId;
    }

    public String getDataType() {
        return mDataType;
    }
    public void setDataType(String dataType) {
        mDataType = dataType;
    }
    public static ArrayList<CategoryInfo> valueOf(JSONObject json) throws JSONException {
        ArrayList<CategoryInfo> list = null;
        if (Tags.isJSONResultOK(json)) {
            JSONArray cateJsonArray = json.getJSONObject(Tags.DATA)
                        .getJSONArray(Tags.CategoryTree.CHILDREN);
            if (cateJsonArray != null) {
                list = new ArrayList<CategoryInfo>();
                for (int i = 0; i < cateJsonArray.length(); i++) {
                    if (!cateJsonArray.isNull(i)) {
                        JSONObject cateJsonObject = cateJsonArray.getJSONObject(i);
                        String cateId = cateJsonObject.getString(Tags.CategoryTree.CAT_ID);
                        // 屏蔽红米手机显示
                        if (TextUtils.equals("99", cateId)) continue;
                        String cateName = cateJsonObject.getString(Tags.CategoryTree.CAT_NAME);
                        int childCount = cateJsonObject.getInt(Tags.CategoryTree.HAS_CHILDREN);
                        boolean hasChildren = false;
                        if(childCount > 0) {
                            hasChildren = true;
                        }
                        String dataType = cateJsonObject.getString(Tags.CategoryTree.DATA_TYPE);
                        String imageUrl = cateJsonObject.optString(Tags.CategoryTree.IMAGE_URLS);
                        CategoryInfo categoryInfo = new CategoryInfo(cateId, cateName,
                                new Image(imageUrl), hasChildren);
                        categoryInfo.setDataType(dataType);
                        list.add(categoryInfo);
                    }
                }
            }
        }

        return list;
    }
}
