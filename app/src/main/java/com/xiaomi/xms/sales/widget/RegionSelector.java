
package com.xiaomi.xms.sales.widget;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.adapter.RegionAdapter;
import com.xiaomi.xms.sales.cache.RegionCache;
import com.xiaomi.xms.sales.cache.RegionCache.QueryCallback;
import com.xiaomi.xms.sales.db.DBContract.Region;
import com.xiaomi.xms.sales.util.LogUtil;

public class RegionSelector extends RelativeLayout {
    private static final String TAG = "RegionSelector";
    private static final int UNSELECTED = 0;

    private Context mContext;
    private Spinner mProvinceSpinner;
    private Spinner mCitySpinner;
    private Spinner mDistrictSpinner;
    private RegionAdapter mProvinceAdapter = null;
    private RegionAdapter mCityAdapter = null;
    private RegionAdapter mDistrictAdapter = null;

    private OnSelectedListener mListener;
    private int mOldProvinceId;
    private int mOldCityId;
    private int mOldDistrictId;

    public RegionSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.city_selector, this, true);
    }

    public int getProvince() {
        return (Integer) mProvinceSpinner.getSelectedView().getTag();
    }

    public int getCity() {
        return (Integer) mCitySpinner.getSelectedView().getTag();
    }

    public int getDistrict() {
        return (Integer) mDistrictSpinner.getSelectedView().getTag();
    }

    public interface OnSelectedListener {
        public void onProvinceSelected(long id);

        public void onCitySelected(long id);

        public void onDistrictSelected(long id);
    }

    public void setOnSelectedListener(OnSelectedListener listener) {
        mListener = listener;
    }

    public void removeOnSelectedListener() {
        mListener = null;
    }

    public void set(int provinceId, int cityId, int districtId) {
        mOldProvinceId = provinceId;
        mOldCityId = cityId;
        mOldDistrictId = districtId;
        if (mProvinceAdapter != null) {
            mProvinceSpinner.setSelection(findPositionInCursor(mProvinceAdapter.getCursor(),
                    provinceId));
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mProvinceSpinner = (Spinner) findViewById(R.id.address_province);
        mCitySpinner = (Spinner) findViewById(R.id.address_city);
        mDistrictSpinner = (Spinner) findViewById(R.id.address_district);
        RegionCache.getInstance(mContext).getProvince(mGetProvinceOnCreateCallback);
        mCitySpinner.setEnabled(false);
        mDistrictSpinner.setEnabled(false);
    }

    private int findPositionInCursor(Cursor cursor, int id) {
        if (cursor.moveToFirst()) {
            do {
                if (id == cursor.getInt(cursor.getColumnIndex(Region._ID))) {
                    return cursor.getPosition();
                }
            } while (cursor.moveToNext());
        }
        return -1;
    }

    private Cursor prependHeader(Cursor cursor, String name) {
        MatrixCursor header = new MatrixCursor(new String[] {
                Region._ID, Region.NAME
        });
        header.addRow(new Object[] {
                -1, name
        });
        if (cursor == null) {
            return header;
        }
        return new MergeCursor(new Cursor[] {
                header, cursor
        });
    }

    private QueryCallback mGetProvinceOnCreateCallback = new QueryCallback() {
        @Override
        public void queryComplete(Cursor cursor) {
            if (cursor == null) {
                LogUtil.w(TAG, "get province null.");
            } else {
                if (cursor.getCount() == 0) {
                    cursor.close();
                    return;
                }
                LogUtil.d(TAG, "get province ok.");
                mProvinceAdapter = new RegionAdapter(mContext, prependHeader(
                        cursor, mContext.getString(R.string.address_province_select)));
                mProvinceSpinner.setAdapter(mProvinceAdapter);
                mProvinceSpinner.setOnItemSelectedListener(mProvinceListener);
                mCitySpinner.setOnItemSelectedListener(mCityListener);
                mDistrictSpinner.setOnItemSelectedListener(mDistrictListener);
                if (mOldProvinceId > 0) {
                    mProvinceSpinner.setSelection(findPositionInCursor(
                            mProvinceAdapter.getCursor(), mOldProvinceId));
                    mOldProvinceId = -1;
                }
            }
        }
    };

    private OnItemSelectedListener mProvinceListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            LogUtil.d(TAG, "province selected:" + id);
            if (id > 0) {
                mCitySpinner.setEnabled(true);
                if (mListener != null) {
                    mListener.onProvinceSelected(id);
                }
            } else {
                mCitySpinner.setEnabled(false);
            }
            mCitySpinner.setSelection(UNSELECTED);
            mDistrictSpinner.setEnabled(false);
            RegionCache.getInstance(mContext).getCityByProvinceId(
                    id, new QueryCallback() {
                        @Override
                        public void queryComplete(Cursor cursor) {
                            cursor = prependHeader(cursor,
                                    mContext.getString(R.string.address_city_select));
                            if (mCityAdapter == null) {
                                LogUtil.d(TAG, "new city adapter");
                                mCityAdapter = new RegionAdapter(mContext, cursor);
                                mCitySpinner.setAdapter(mCityAdapter);
                            } else {
                                mCityAdapter.changeCursor(cursor);
                            }
                            if (mOldCityId > 0) {
                                mCitySpinner.setSelection(findPositionInCursor(cursor,
                                        mOldCityId));
                                mOldCityId = -1;
                            }
                        }
                    });
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

    private OnItemSelectedListener mCityListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            LogUtil.d(TAG, "city selected:" + id);
            if (id > 0) {
                mDistrictSpinner.setEnabled(true);
                if (mListener != null) {
                    mListener.onCitySelected(id);
                }
            } else {
                mDistrictSpinner.setEnabled(false);
            }
            mDistrictSpinner.setSelection(UNSELECTED);
            RegionCache.getInstance(mContext).getDistrictByCityId(id,
                    new QueryCallback() {
                        @Override
                        public void queryComplete(Cursor cursor) {
                            cursor = prependHeader(cursor,
                                    mContext.getString(R.string.address_district_select));
                            if (mDistrictAdapter == null) {
                                LogUtil.d(TAG, "new district adapter");
                                mDistrictAdapter = new RegionAdapter(mContext, cursor);
                                mDistrictSpinner.setAdapter(mDistrictAdapter);
                            } else {
                                mDistrictAdapter.changeCursor(cursor);
                            }
                            if (mOldDistrictId > 0) {
                                mDistrictSpinner.setSelection(findPositionInCursor(cursor,
                                        mOldDistrictId));
                                mOldDistrictId = -1;
                            }
                        }
                    });
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    };

    private OnItemSelectedListener mDistrictListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            LogUtil.d(TAG, "district selected:" + id);
            if (id <= 0) {
                return;
            }
            if (mListener != null) {
                mListener.onDistrictSelected(id);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };
}
