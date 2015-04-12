package com.xiaomi.xms.sales.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.SimpleAdapter;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.adapter.OrderViewProductSnAdapter;
import com.xiaomi.xms.sales.ui.RASSTestFragment.OnOrderStatusChangedListener;
import com.xiaomi.xms.sales.widget.BaseListView;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;

public class ICUHistoryFragment extends BaseFragment {

	private static final String TAG = "RiskPatientFragment";
	private Button mConfirmButton;
	private BaseListView mListView;
	private View mListFooterView;
	private View mListRadioButtonFooterView;
	private View mActionContainer;
	private View mradioGroupContainer;
	private OrderViewProductSnAdapter mAdapter;
	private EmptyLoadingView mLoadingView;
	private String mOrderId;
	private String mOrderStatus;
	private OnOrderStatusChangedListener mOrderStatusListener;
	private HashMap<Integer, String> mFormDefaultValue = new HashMap<Integer, String>();
	private Bundle mBundle;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		// 生成动态数组，加入数据
		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ItemText1", "4");
		map.put("ItemText2", "1");
		map.put("ItemText3", "3");
		map.put("ItemText4", "1");
		listItem.add(map);

		// 生成适配器的Item和动态数组对应的元素
		SimpleAdapter listItemAdapter = new SimpleAdapter(getActivity(),
				listItem,// 数据源
				R.layout.history_list_item,// ListItem的XML实现
				// 动态数组与ImageItem对应的子项
				new String[] { "ItemText1", "ItemText2", "ItemText3",
						"ItemText4" },
				// ImageItem的XML文件里面的一个ImageView,两个TextView ID
				new int[] { R.id.ItemText1, R.id.ItemText2, R.id.ItemText3,
						R.id.ItemText4 });

		// 添加并且显示
		mListView.setAdapter(listItemAdapter);
		mListView.setBackgroundResource(R.drawable.list_item_middle_bg);
		mActionContainer.setVisibility(View.VISIBLE);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.return_choice_fragment,
				container, false);
		mListView = (BaseListView) view.findViewById(android.R.id.list);
		mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
		view.findViewById(R.id.rass_confirm_btn);
		mListFooterView = inflater.inflate(R.layout.return_order_detail_footer,
				null, false);
		mListFooterView.setVisibility(View.VISIBLE);

		mListView
				.setPadding(
						getResources().getDimensionPixelSize(
								R.dimen.list_item_padding),
						0,
						getResources().getDimensionPixelSize(
								R.dimen.list_item_padding),
						getResources().getDimensionPixelSize(
								R.dimen.list_item_padding));
		mActionContainer = view.findViewById(R.id.action_container);

		mConfirmButton = (Button) view.findViewById(R.id.rass_confirm_btn);

		mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);
		mLoadingView.setEmptyText(R.string.order_list_empty);

		return view;
	}


}
