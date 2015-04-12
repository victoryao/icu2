package com.xiaomi.xms.sales.adapter;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.xiaomi.xms.sales.ui.BaseFragment;
import com.xiaomi.xms.sales.widget.TabIndicator;

import java.util.ArrayList;
import java.util.List;

public class FragmentPagerAdapter extends PagerAdapter implements OnClickListener {
    private final List<BaseFragment> mFragments = new ArrayList<BaseFragment>();
    private final List<TabIndicator> mTabs = new ArrayList<TabIndicator>();
    private FragmentManager mFragmentManager;
    private FragmentTransaction mCurrentTransaction;
    private TabChangedListener mTabChangedListener;

    public interface TabChangedListener {
        public void onTabChanged(int position);
    }

    public void setTabChangedListener(TabChangedListener l) {
        mTabChangedListener = l;
    }

    public FragmentPagerAdapter(FragmentManager fm) {
        mFragmentManager = fm;
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((BaseFragment) object).getView() == view;
    }

    public BaseFragment getItem(int position) {
        if (position < 0 || position >= mFragments.size()) {
            return null;
        }
        return mFragments.get(position);
    }

    public void addFragment(TabIndicator tab, BaseFragment fragment) {
        mFragments.add(fragment);
        mTabs.add(tab);
        tab.setOnClickListener(this);
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        if (mCurrentTransaction != null) {
            mCurrentTransaction.commitAllowingStateLoss();
            mCurrentTransaction = null;
            mFragmentManager.executePendingTransactions();
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (mCurrentTransaction == null) {
            mCurrentTransaction = mFragmentManager.beginTransaction();
        }
        mCurrentTransaction.detach((BaseFragment) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (mCurrentTransaction == null) {
            mCurrentTransaction = mFragmentManager.beginTransaction();
        }
        BaseFragment f = getItem(position);
        mCurrentTransaction.attach(f);
        return f;
    }

    public String getTagByPosition(int position) {
        if (position < 0 || position > mFragments.size()) {
            return null;
        }
        return mFragments.get(position).getTag();
    }

    public int getPositionByTag(String tag) {
        for (int i = 0; i < mFragments.size(); i++) {
            if (TextUtils.equals(tag, mFragments.get(i).getTag())) {
                return i;
            }
        }
        return 0;
    }

    public TabIndicator getTab(int position) {
        if (position < 0 || position >= mTabs.size()) {
            return null;
        }
        return mTabs.get(position);
    }

    public int getTabIndex(TabIndicator tab) {
        return mTabs.indexOf(tab);
    }

    public void selectTab(int position) {
        for (int i = 0; i < mTabs.size(); i++) {
            if (position == i) {
                mTabs.get(i).setSelected(true);
            } else {
                mTabs.get(i).setSelected(false);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int i = 0;
        for (; i < mTabs.size(); i++) {
            if (mTabs.get(i) == (TabIndicator) v) {
                if (mTabChangedListener != null) {
                    mTabChangedListener.onTabChanged(i);
                }
                break;
            }
        }
        selectTab(i);
    }

    @Override
    public int getItemPosition(Object fragment) {
        if (getFragmentPosition((BaseFragment) fragment) != POSITION_NONE) {
            return POSITION_UNCHANGED;
        }
        return POSITION_NONE;
    }

    public int getFragmentPosition(BaseFragment fragment) {
        for (int i = 0; i < mFragments.size(); i++) {
            if ((TextUtils.equals(((BaseFragment) fragment).getTag(), mFragments.get(i).getTag()))) {
                return i;
            }
        }
        return POSITION_NONE;
    }

    /**
     * 替换Fragment
     * @param container is the viewpager
     * @param oldFragment is the old fragment to be replaced
     * @param newFragment is new fragment to add
     * @param newTag
     */
    public void replaceFragment(ViewPager container, BaseFragment oldFragment,
            BaseFragment newFragment, String newTag) {
        startUpdate(container);
        if (mCurrentTransaction == null) {
            mCurrentTransaction = mFragmentManager.beginTransaction();
        }

        int position = getFragmentPosition(oldFragment);
        mFragments.set(position, newFragment);
        mCurrentTransaction.remove(oldFragment);
        mCurrentTransaction.add(container.getId(), newFragment, newTag);
        finishUpdate(container);
        mCurrentTransaction = null;
        notifyDataSetChanged();
    }
}
