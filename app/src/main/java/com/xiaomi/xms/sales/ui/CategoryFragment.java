
package com.xiaomi.xms.sales.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.ShopApp;
import com.xiaomi.xms.sales.activity.BaseActivity;
import com.xiaomi.xms.sales.activity.MainActivity;
import com.xiaomi.xms.sales.activity.ProductActivity;
import com.xiaomi.xms.sales.activity.SearchResultActivity;
import com.xiaomi.xms.sales.activity.SecondCategoryActivity;
import com.xiaomi.xms.sales.adapter.BaseDataAdapter;
import com.xiaomi.xms.sales.adapter.CategoryAdapter;
import com.xiaomi.xms.sales.adapter.CategoryListAdapter;
import com.xiaomi.xms.sales.adapter.SearchHintListAdapter;
import com.xiaomi.xms.sales.loader.CategoryLoader;
import com.xiaomi.xms.sales.loader.CategoryLoader.Result;
import com.xiaomi.xms.sales.model.CategoryInfo;
import com.xiaomi.xms.sales.request.HostManager;
import com.xiaomi.xms.sales.request.Request;
import com.xiaomi.xms.sales.util.Constants;
import com.xiaomi.xms.sales.util.Utils;
import com.xiaomi.xms.sales.widget.BaseListView;
import com.xiaomi.xms.sales.widget.BaseListView.OnLayoutListener;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class CategoryFragment extends BaseFragment implements
        LoaderCallbacks<CategoryLoader.Result>, OnLayoutListener {
    private final static int CATEGORY_LOADER = 0;

    private EmptyLoadingView mLoadingView;
    private BaseListView mListView;
    private CategoryAdapter mCategoryAdapter;
    private CategoryListAdapter mCategoryListAdapter;
    private UiHandler handler = new UiHandler(this);

    private View mTopView;
    private View mTopViewContent;
    private View mFakeBg;
    private View mFakeSearchView;
    private boolean mIsFakeSearchViewPositioned;
    private static final int TRUE_DATA_ITEM_IDX = 2;
    private Bitmap mFakeTopBmp;
    private int mTitleHeight;

    private static final int ANIMATION_DURATION = 300;
    private static final int ACTION_DURATION = 400;
    private static final int ACTION_DURATION_FOR_RESUME = 100;
    private View mSearchUi;
    private View mSearchButton;
    private AutoCompleteTextView mInput;
    private boolean mShouldSelectAllInput;
    private View mSearchEmptyArea;
    private ListView mSearchHintListView;
    private BaseDataAdapter<SpannableString> mSearchHintListAdapter;
    private ArrayList<SpannableString> mHotWords;
    private HintLoadTask mHintLoadTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.category_fragment, container, false);
        mFakeBg = view.findViewById(R.id.fake_bg);
        mListView = (BaseListView) view.findViewById(android.R.id.list);
        int listPadding = (int) ShopApp.getContext().getResources()
                .getDimension(R.dimen.list_item_padding);
        mListView.setPadding(0, listPadding / 2, 0, listPadding);
        mSearchUi = view.findViewById(R.id.search_ui);
        mSearchUi.setOnClickListener(mSearchClickListener);

        mTopView = inflater.inflate(R.layout.fake_top_for_search, null);
        mTopViewContent = mTopView.findViewById(R.id.top_view_content);
        mTopViewContent.setBackgroundColor(ShopApp.getContext().getResources()
                .getColor(R.color.fake_search_background));
        mListView.addHeaderView(mTopView);

        mFakeSearchView = inflater.inflate(R.layout.fake_search_view, null);
        mIsFakeSearchViewPositioned = false;
        View fakeInput = mFakeSearchView.findViewById(R.id.fake_input);
        fakeInput.setOnClickListener(mSearchClickListener);
        View fakeButton = mFakeSearchView.findViewById(R.id.fake_button);
        fakeButton.setOnClickListener(mSearchClickListener);
        mFakeSearchView.setBackgroundColor(ShopApp.getContext().getResources()
                .getColor(R.color.fake_search_background));
        mListView.addHeaderView(mFakeSearchView);
        mListView.setOnLayoutListener(this);

        mCategoryAdapter = new CategoryAdapter(getActivity());
        mCategoryListAdapter = new CategoryListAdapter(getActivity(),
                mCategoryAdapter,
                mItemClickListner);
        mListView.setAdapter(mCategoryListAdapter);
        ArrayList<Object> data = new ArrayList<Object>();
        data.add(1);
        mCategoryListAdapter.updateData(data);

        mSearchButton = mSearchUi.findViewById(R.id.search_button);
        mSearchButton.setOnClickListener(mSearchClickListener);
        mInput = (AutoCompleteTextView) mSearchUi.findViewById(R.id.input);
        mInput.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mSearchButton.performClick();
                    return true;
                }
                return false;
            }
        });
        mInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                loadHint();
            }
        });
        mSearchEmptyArea = mSearchUi.findViewById(R.id.empty_area);
        mSearchEmptyArea.setOnClickListener(mSearchClickListener);
        mSearchHintListView = (ListView) mSearchEmptyArea.findViewById(android.R.id.list);
        mSearchHintListView.addHeaderView(inflater.inflate(R.layout.search_hint_list_top, null));
        mSearchHintListAdapter = new SearchHintListAdapter(getActivity());
        mSearchHintListView.setAdapter(mSearchHintListAdapter);
        mSearchHintListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int positon, long id) {
                View v = view.findViewById(R.id.word);
                if (v != null && v instanceof TextView) {
                    doSearch(((TextView) v).getText().toString());
                }
            }
        });

        mLoadingView = (EmptyLoadingView) view.findViewById(R.id.loading);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(CATEGORY_LOADER, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mShouldSelectAllInput && mSearchUi.getVisibility() == View.VISIBLE) {
            handler.sendEmptyMessageDelayed(UiHandler.MSG_SHOW_SEARCH, ACTION_DURATION_FOR_RESUME);
        }
        mShouldSelectAllInput = false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mFakeTopBmp != null) {
            mFakeTopBmp.recycle();
            mFakeTopBmp = null;
        }
    }

    @Override
    public Loader onCreateLoader(int id, Bundle bundle) {
        if (id == CATEGORY_LOADER) {
            mLoader = new CategoryLoader(getActivity(), null);
            mLoader.setProgressNotifiable(mLoadingView);
            return mLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Result> loader, Result data) {
        mCategoryAdapter.updateData(data.mCategoryInfos);
    }

    @Override
    public void onLoaderReset(Loader<Result> loader) {
    }

    @Override
    public void beforeOnLyaout() {
        if (mCategoryAdapter.hasItemViewBound && !mIsFakeSearchViewPositioned) {
            mListView.setOnLayoutListener(null);
            mIsFakeSearchViewPositioned = true;
            mListView.setSelectionFromTop(
                    TRUE_DATA_ITEM_IDX,
                    mTopView.getHeight()
                            - (int) ShopApp.getContext().getResources()
                                    .getDimension(R.dimen.fake_search_layout_diff));
        }
    }

    private OnItemClickListener mItemClickListner = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            CategoryInfo mCategoryInfo = (CategoryInfo) mCategoryAdapter.getItem(position);
            if (mCategoryInfo.hasChildren()) {
                Intent intent = new Intent(getActivity(), SecondCategoryActivity.class);
                if (!TextUtils.isEmpty(mCategoryInfo.getCategoryId())) {
                    intent.putExtra(Constants.Intent.EXTRA_CATEGORY_ID,
                            mCategoryInfo.getCategoryId());
                    intent.putExtra(Constants.Intent.EXTRA_CATEGORY_NAME, mCategoryInfo.getName());
                    getActivity().startActivity(intent);
                }
            } else {
                Intent intent = new Intent(getActivity(), ProductActivity.class);
                if (!TextUtils.isEmpty(mCategoryInfo.getCategoryId())) {
                    intent.putExtra(Constants.Intent.EXTRA_CATEGORY_ID,
                            mCategoryInfo.getCategoryId());
                    intent.putExtra(Constants.Intent.EXTRA_CATEGORY_NAME, mCategoryInfo.getName());
                    intent.putExtra(Constants.Intent.EXTRA_CATEGORY_DATA_TYPE, mCategoryInfo.getDataType());
                    getActivity().startActivity(intent);
                }
            }

        }
    };

    private OnClickListener mSearchClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fake_input:
                case R.id.fake_button:
                    showSearchUi(true);
                    break;
                case R.id.empty_area:
                    showSearchUi(false);
                    break;
                case R.id.search_button:
                    String str = mInput.getText().toString().trim();
                    if (!TextUtils.isEmpty(str)) {
                        doSearch(str);
                    }
                    break;
            }
        }
    };

    private void doSearch(String str) {
        mShouldSelectAllInput = true;
        Intent intent = new Intent(getActivity(), SearchResultActivity.class);
        intent.putExtra(Constants.Intent.EXTRA_SEARCH_RESULT_KEYWORD, str);
        getActivity().startActivity(intent);
    }

    public void showSearchUi(boolean show) {
        View titleBar = ((BaseActivity) getActivity()).getTitleBarContainer();
        View tabs = ((MainActivity) getActivity()).tabs;
        if (show) {
            if (mFakeTopBmp == null) {
                int w = 0, h = 0, titleBmpH = 0, tabsBmpH = (int) ShopApp.getContext()
                        .getResources().getDimension(R.dimen.tabs_visible_height);
                titleBar.setDrawingCacheEnabled(true);
                tabs.setDrawingCacheEnabled(true);
                Bitmap titleBmp = titleBar.getDrawingCache();
                Bitmap tabsBmp = tabs.getDrawingCache();
                if (titleBmp != null) {
                    w = titleBmp.getWidth();
                    mTitleHeight = titleBmpH = titleBmp.getHeight();
                    h += titleBmpH;
                }
                if (tabsBmp != null) {
                    if (w == 0) {
                        w = tabsBmp.getWidth();
                    }
                    h += tabsBmpH;
                }
                mFakeTopBmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
                Canvas canvas = new Canvas(mFakeTopBmp);
                Paint paint = new Paint();
                if (titleBmp != null) {
                    canvas.drawBitmap(titleBmp, 0, 0, paint);
                    titleBmp.recycle();
                }
                titleBar.setDrawingCacheEnabled(false);
                if (tabsBmp != null) {
                    paint.setColor(ShopApp.getContext().getResources()
                            .getColor(R.color.fake_search_background));
                    canvas.drawRect(0, titleBmpH, w, titleBmpH + tabsBmpH, paint);
                    canvas.drawBitmap(tabsBmp, 0, titleBmpH, paint);
                    tabsBmp.recycle();
                }
                tabs.setDrawingCacheEnabled(false);
            }
            mCategoryListAdapter.showCoverView(true);
            mFakeBg.setVisibility(View.VISIBLE);
            mFakeBg.setLayoutParams(new LayoutParams(mFakeTopBmp.getWidth(), mFakeTopBmp
                    .getHeight()));
            mFakeBg.setBackgroundDrawable(new BitmapDrawable(mFakeTopBmp));
            mTopView.setLayoutParams(new AbsListView.LayoutParams(mFakeTopBmp.getWidth(),
                    mFakeTopBmp.getHeight()));
            mTopViewContent.setBackgroundColor(ShopApp.getContext().getResources()
                    .getColor(R.color.transparent));
            titleBar.setVisibility(View.GONE);
            tabs.setVisibility(View.GONE);
            int scrollDis = mTitleHeight
                    + mFakeSearchView.getTop()
                    + (int) ShopApp.getContext().getResources()
                            .getDimension(R.dimen.fake_search_scroll_up_diff);
            mListView.smoothScrollBy(scrollDis, ANIMATION_DURATION);
            handler.sendEmptyMessageDelayed(UiHandler.MSG_SHOW_SEARCH, ACTION_DURATION);
            loadHint();
        } else {
            mInput.setText("");
            Utils.SoftInput.hide(getActivity(), mInput.getWindowToken());
            mSearchUi.setVisibility(View.GONE);
            int scrollDis = -mFakeTopBmp.getHeight()
                    + (int) ShopApp.getContext().getResources()
                            .getDimension(R.dimen.fake_search_scroll_down_diff);
            mListView.smoothScrollBy(scrollDis, ANIMATION_DURATION);
            handler.sendEmptyMessageDelayed(UiHandler.MSG_HIDE_SEARCH, ACTION_DURATION);
        }
    }

    public boolean isOnSearchUi() {
        return mSearchUi != null && View.VISIBLE == mSearchUi.getVisibility();
    }

    private static class UiHandler extends Handler {
        static final int MSG_SHOW_SEARCH = 0;
        static final int MSG_HIDE_SEARCH = 1;

        WeakReference<CategoryFragment> ref;

        public UiHandler(CategoryFragment obj) {
            ref = new WeakReference<CategoryFragment>(obj);
        }

        public void handleMessage(Message msg) {
            CategoryFragment obj = ref.get();
            if (obj != null) {
                if (MSG_SHOW_SEARCH == msg.what) {
                    obj.mCategoryListAdapter.showCoverView(false);
                    obj.mSearchUi.setVisibility(View.VISIBLE);
                    obj.mInput.setSelection(0, obj.mInput.getText().length());
                    obj.mInput.setFocusable(true);
                    obj.mInput.setFocusableInTouchMode(true);
                    obj.mInput.requestFocus();
                    Utils.SoftInput.show(obj.getActivity(), obj.mInput);
                } else if (MSG_HIDE_SEARCH == msg.what) {
                    ((BaseActivity) obj.getActivity()).getTitleBarContainer().setVisibility(
                            View.VISIBLE);
                    ((MainActivity) obj.getActivity()).tabs.setVisibility(View.VISIBLE);
                    obj.mFakeBg.setVisibility(View.GONE);
                    obj.mListView.removeHeaderView(obj.mTopView);
                    obj.mListView.removeHeaderView(obj.mFakeSearchView);
                    obj.mTopView = LayoutInflater.from(obj.getActivity()).inflate(
                            R.layout.fake_top_for_search,
                            null);
                    obj.mTopViewContent = obj.mTopView.findViewById(R.id.top_view_content);
                    obj.mTopViewContent.setBackgroundColor(ShopApp.getContext().getResources()
                            .getColor(R.color.fake_search_background));
                    obj.mFakeSearchView.setBackgroundColor(ShopApp.getContext().getResources()
                            .getColor(R.color.fake_search_background));
                    obj.mListView.setAdapter(null);
                    obj.mListView.addHeaderView(obj.mTopView);
                    obj.mListView.addHeaderView(obj.mFakeSearchView);
                    obj.mListView.setAdapter(obj.mCategoryListAdapter);
                }
            }
        }
    }

    private void loadHint() {
        if (mHintLoadTask != null) {
            mHintLoadTask.cancel(true);
        }
        String kw = mInput.getText().toString();
        if (TextUtils.isEmpty(kw) && mHotWords != null) {

            mSearchHintListAdapter.updateData(mHotWords);
            return;
        }
        mHintLoadTask = new HintLoadTask(kw);
        mHintLoadTask.execute();
    }

    private class HintLoadTask extends AsyncTask<Void, Void, ArrayList<String>> {

        private String mKeyword;
        private boolean mIsHot;

        private static final String JSON_TAG_DATA = "data";

        public HintLoadTask(String keyword) {
            mKeyword = keyword;
            mIsHot = TextUtils.isEmpty(mKeyword);
        }

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            Request r = null;
            if (mIsHot) {
                r = new Request(HostManager.getHotSearch());
            } else {
                r = new Request(HostManager.getExpandSearch());
                r.addParam("word", mKeyword);
            }
            if (r.getStatus() == Request.STATUS_OK) {
                JSONObject json = r.requestJSON();
                ArrayList<String> result = new ArrayList<String>();
                try {
                    if (mIsHot) {
                        json = json.getJSONObject(JSON_TAG_DATA);
                        ArrayList<HotWord> hotWords = new ArrayList<HotWord>();
                        Iterator<String> keys = json.keys();
                        while (keys.hasNext()) {
                            HotWord hw = new HotWord();
                            hw.word = keys.next();
                            hw.weight = json.getInt(hw.word);
                            hotWords.add(hw);
                        }
                        Collections.sort(hotWords, new Comparator<HotWord>() {

                            @Override
                            public int compare(HotWord lhs, HotWord rhs) {
                                return rhs.weight - lhs.weight;
                            }
                        });
                        for (HotWord hw : hotWords) {
                            result.add(hw.word);
                        }
                    } else {
                        JSONArray array = json.getJSONArray(JSON_TAG_DATA);
                        int len = array.length();
                        for (int i = 0; i < len; i++) {
                            result.add(array.getString(i));
                        }
                    }
                    return result;
                } catch (Exception e) {
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            if (result != null) {
                ArrayList<SpannableString> data = new ArrayList<SpannableString>();
                for (String s : result) {
                    data.add(new SpannableString(s));
                }
                if (mIsHot) {
                    mHotWords = data;
                }
                if (mInput.getText().toString().equals(mKeyword)) {
                    if (!TextUtils.isEmpty(mKeyword)) {
                        int len = mKeyword.length();
                        int dataLen = result.size();
                        for (int i = 0; i < dataLen; i++) {
                            if (result.get(i).startsWith(mKeyword)) {
                                data.get(i).setSpan(
                                        new ForegroundColorSpan(ShopApp.getContext().getResources()
                                                .getColor(R.color.highlight_text_color)), 0, len,
                                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            }
                        }
                    }
                    mSearchHintListAdapter.updateData(data);
                }
            } else {
                mSearchHintListAdapter.updateData(null);
            }
        }

    }

    private static class HotWord {
        public String word;
        public int weight;
    }
}
