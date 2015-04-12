
package com.xiaomi.xms.sales.adapter;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item.ActNode;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item.CartListNode;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item.IncastNode;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item.SupplyNode;
import com.xiaomi.xms.sales.model.ShoppingCartListInfo.Item.TitleNode;
import com.xiaomi.xms.sales.ui.IncastProductGalleryItem;
import com.xiaomi.xms.sales.ui.ShoppingActItem;
import com.xiaomi.xms.sales.ui.ShoppingListItem;
import com.xiaomi.xms.sales.ui.ShoppingSupplyItem;
import com.xiaomi.xms.sales.ui.ShoppingTitleItem;
import com.xiaomi.xms.sales.util.LogUtil;

import java.util.ArrayList;

public class ShoppingAdapter extends BaseDataAdapter<Item> {
    private final static int BG_TYPE_TOP = 1;
    private final static int BG_TYPE_MIDDLE = 2;
    private final static int BG_TYPE_BOTTOM = 3;
    private final static int BG_TYPE_SINGLE = 4;
    private final static int BG_TYPE_TITLE = 5;
    private final static int BG_TYPE_BLACK = 6;
    private boolean mHideArrow;
    private boolean mShowTopLine;
    private boolean mPaperBackground;
    private SparseIntArray mTypeMap = new SparseIntArray();
    private final static String TAG = "ShoppingAdapter";

    public ShoppingAdapter(Context context) {
        super(context);
    }

    @Override
    public View newView(Context context, Item data, ViewGroup parent) {
        View view = null;
        if (data.getType() == Item.TYPE_CARTLIST) {
            ShoppingListItem item = new ShoppingListItem(context, null);
            item.hideArrow(mHideArrow);
            item.showTopLine(mShowTopLine);
            view = item;
        } else if (data.getType() == Item.TYPE_TITLE) {
            ShoppingTitleItem item = new ShoppingTitleItem(context, null);
            view = item;
        } else if (data.getType() == Item.TYPE_SUPPLY) {
            ShoppingSupplyItem item = new ShoppingSupplyItem(context, null);
            view = item;
        } else if (data.getType() == Item.TYPE_BLACK) {
            view = LayoutInflater.from(context).inflate(R.layout.list_black_item,
                    parent, false);
        } else if (data.getType() == Item.TYPE_INCAST) {
            view = new IncastProductGalleryItem(context, null);
        } else if (data.getType() == Item.TYPE_ACT) {
            view = new ShoppingActItem(context, null);
        }
        return view;
    }

    @Override
    public int getItemViewType(int position) {
        Item item = mData.get(position);
        return item.getType();
    }

    @Override
    public int getViewTypeCount() {
        return Item.TYPE_COUNT;
    }

    @Override
    public void bindView(View view, int position, Item data) {
        LogUtil.d(TAG, "bindView:" + position + ", data type: " + data.getType());
        if (data.getType() == Item.TYPE_CARTLIST) {
            if (view instanceof ShoppingListItem) {
                ((ShoppingListItem) view).bind((CartListNode) data.getNode());
            }
        } else if (data.getType() == Item.TYPE_TITLE) {
            if (view instanceof ShoppingTitleItem) {
                ((ShoppingTitleItem) view).bind((TitleNode) data.getNode());
            }
        } else if (data.getType() == Item.TYPE_SUPPLY) {
            if (view instanceof ShoppingSupplyItem) {
                ((ShoppingSupplyItem) view).bind((SupplyNode) data.getNode());
            }
        } else if (data.getType() == Item.TYPE_INCAST) {
            if (view instanceof IncastProductGalleryItem) {
                ((IncastProductGalleryItem) view).bind((IncastNode) data.getNode());
            }
        } else if (data.getType() == Item.TYPE_ACT) {
            if (view instanceof ShoppingActItem) {
                ((ShoppingActItem) view).bind((ActNode) data.getNode());
            }
        } else if (data.getType() == Item.TYPE_BLACK) {

        }
    }

    @Override
    public void updateData(ArrayList<Item> data) {
        super.updateData(data);
        LogUtil.d(TAG, "updateData: data size is" + data.size());
        mTypeMap.clear();
        boolean inArea = false;
        int n = 0;
        for (int i = 0; i < data.size(); i++) {
            Item item = data.get(i);
            if (item.getType() == Item.TYPE_BLACK) {
                mTypeMap.put(i, BG_TYPE_BLACK);
                if (n == 1) {
                    mTypeMap.put(i - 1, BG_TYPE_SINGLE);
                } else {
                    mTypeMap.put(i - 1, BG_TYPE_BOTTOM);
                }
                break;
            }
            if (item.getType() != Item.TYPE_TITLE) {
                if (inArea == false) {
                    inArea = true;
                    mTypeMap.put(i, BG_TYPE_TOP);
                } else {
                    mTypeMap.put(i, BG_TYPE_MIDDLE);
                }
                n++;
            } else {
                inArea = false;
                if (i - 1 > 0) {
                    if (n == 1) {
                        mTypeMap.put(i - 1, BG_TYPE_SINGLE);
                    } else {
                        mTypeMap.put(i - 1, BG_TYPE_BOTTOM);
                    }
                }
                mTypeMap.put(i, BG_TYPE_TITLE);
                n = 0;
            }
        }
    }

    public void hideArrow(boolean isHide) {
        mHideArrow = isHide;
    }

    public void showTopLine(boolean isShow) {
        mShowTopLine = isShow;
    }

    public void showPaperBackground() {
        mPaperBackground = true;
    }

    public void updateTitleAndBlack() {
        if (mData != null) {
            mDataValid = true;
            for (int i = 0; i < mData.size(); i++) {
                Item item = mData.get(i);
                if (item.getType() == Item.TYPE_BLACK || item.getType() == Item.TYPE_TITLE) {
                    mData.remove(item);
                }
            }
            notifyDataSetChanged();
        } else {
            mDataValid = false;
            notifyDataSetInvalidated();
        }
    }

    @Override
    protected void bindBackground(View view, int position) {
        int type = getItemViewType(position);

        if (type == Item.TYPE_INCAST || type == Item.TYPE_ACT) {
            return;
        }

        if (mPaperBackground) {
            view.setBackgroundResource(R.drawable.order_view_body);
            return;
        }

        int bgType = mTypeMap.get(position);
        View bgView;
        if (type == Item.TYPE_CARTLIST) {
            bgView = ((ShoppingListItem) view).getContainer();
        } else if (type == Item.TYPE_SUPPLY) {
            ShoppingSupplyItem supplyItem = (ShoppingSupplyItem) view;
            bgView = view;
            if (bgType == BG_TYPE_TOP) {
                supplyItem.setArrowBackgroundResource(R.drawable.op_arrow_up_bg);
            } else if (bgType == BG_TYPE_MIDDLE) {
                supplyItem.setArrowBackgroundResource(R.drawable.op_arrow_middle_bg);
            } else if (bgType == BG_TYPE_BOTTOM) {
                supplyItem.setArrowBackgroundResource(R.drawable.op_arrow_bottom_bg);
            } else if (bgType == BG_TYPE_SINGLE) {
                supplyItem.setArrowBackgroundResource(R.drawable.op_arrow_single_n);
            }
        } else {
            bgView = view;
        }
        LogUtil.d(TAG, "bindBackground: " + position + ", size is " + mData.size());
        int background = 0;
        LogUtil.i(TAG, bgType + "");
        if (bgType == BG_TYPE_TOP) {
            background = mPaperBackground ? R.drawable.order_view_body
                    : R.drawable.cartlist_list_item_top_bg;
        } else if (bgType == BG_TYPE_MIDDLE) {
            background = mPaperBackground ? R.drawable.order_view_body
                    : R.drawable.cartlist_list_item_middle_bg;
        } else if (bgType == BG_TYPE_BOTTOM) {
            background = mPaperBackground ? R.drawable.order_view_body
                    : R.drawable.cartlist_list_item_bottom_bg;
        } else if (bgType == BG_TYPE_TITLE) {
            bgView.setBackgroundDrawable(null);
        } else if (bgType == BG_TYPE_SINGLE) {
            background = mPaperBackground ? R.drawable.order_view_body
                    : R.drawable.cartlist_list_item_single_bg;
        } else if (bgType == BG_TYPE_BLACK) {
            bgView.setBackgroundDrawable(null);
        }
        bgView.setBackgroundResource(background);
    }

    @Override
    public boolean isEnabled(int position) {
        int bgType = mTypeMap.get(position);
        if (bgType == BG_TYPE_TITLE || bgType == BG_TYPE_BLACK) {
            return false;
        }
        return true;
    }
}
