
package com.xiaomi.xms.sales.ui;

import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.adapter.CommentListAdapter;
import com.xiaomi.xms.sales.loader.BaseLoader;
import com.xiaomi.xms.sales.loader.BasePageLoader;
import com.xiaomi.xms.sales.loader.CommentListLoader;
import com.xiaomi.xms.sales.loader.CommentSumLoader;
import com.xiaomi.xms.sales.loader.CommentTotalLoader;
import com.xiaomi.xms.sales.model.CommentItemInfo;
import com.xiaomi.xms.sales.model.CommentSumInfo;
import com.xiaomi.xms.sales.widget.BaseListView;
import com.xiaomi.xms.sales.widget.EmptyLoadingView;

import java.util.ArrayList;

public class CommentController {

    /**
     * The loader id must be different with the loaders which are created in the
     * attached fragment.
     */
    private static final int COMMENT_LIST_LOADER = 104;
    private static final int COMMENT_SUM_LOADER = 105;
    private static final int COMMENT_TOTAL_LOADER = 106;

    private BasePageLoader<CommentListLoader.Result> mListLoader;
    private BaseLoader<CommentSumLoader.Result> mSumLoader;
    private BaseLoader<CommentTotalLoader.Result> mTotalLoader;
    private ArrayList<CommentItemInfo> mCommentInfoList;
    private BaseFragment mFragment; // the attached fragment
    private String mProductId;
    private BaseListView mListView;
    private View mCommentHeadView;
    private LinearLayout mHeadView;
    private TextView mShowCommentBtn;
    private TextView mShowProductBtn;
    private CommentListAdapter mCommentAdapter;
    private Button mGetMoreComments;
    private ViewGroup mLoadingMoreParent;
    private boolean mNeedUpdateAdapter = true;
    private int mCommentsTotal;
    private View mNoCommentView;
    private View mSummaryView;

    public interface OnShowProductListener {
        public void onShow();
    }

    private OnShowProductListener mListener;

    public void setOnShowProductListener(OnShowProductListener l) {
        mListener = l;
    }

    public CommentController(BaseFragment fragment, String productId, BaseListView listView,
            LinearLayout headView, LinearLayout loadingMoreParent, Button showComment,
            Button showProduct, EmptyLoadingView loadingView) {
        mFragment = fragment;
        mProductId = productId;
        mListView = listView;
        mHeadView = headView;
        mShowCommentBtn = showComment;
        mShowProductBtn = showProduct;
        mLoadingMoreParent = loadingMoreParent;
        mCommentAdapter = new CommentListAdapter(mFragment.getActivity());
        mShowCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTab(mShowProductBtn, mShowCommentBtn);
                mNeedUpdateAdapter = true;
                showComment();
            }
        });
        mShowProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTab(mShowCommentBtn, mShowProductBtn);
                mHeadView.removeView(mCommentHeadView);
                if (mListener != null) {
                    mListener.onShow();
                }
            }
        });
        // initialize the loading more comments button
        LayoutInflater inflater = mFragment.getLayoutInflater(null);
        LinearLayout getMoreLayout = (LinearLayout) inflater.inflate(R.layout.product_detail_loading_more, null, false);
        mGetMoreComments = (Button) getMoreLayout.findViewById(R.id.product_detail_loading_more);
        mGetMoreComments.setText(R.string.acquaintance_more);
        mGetMoreComments.setVisibility(View.GONE);
        mGetMoreComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Here, the internal page id is already set to next,
                // so directly load next page.
                mNeedUpdateAdapter = false;
                mListLoader.forceLoad();
            }
        });
        getMoreLayout.removeAllViews();
        // initialize loaders
        mFragment.getLoaderManager().initLoader(COMMENT_TOTAL_LOADER, null, mTotalLoaderCB);
        mFragment.getLoaderManager().initLoader(COMMENT_LIST_LOADER, null, mListLoaderCB);
        mFragment.getLoaderManager().initLoader(COMMENT_SUM_LOADER, null, mSumLoaderCB);
        mTotalLoader.setProgressNotifiable(loadingView);
        mListLoader.setProgressNotifiable(loadingView);
        mSumLoader.setProgressNotifiable(loadingView);
        mTotalLoader.forceLoad();
        mListLoader.forceLoad();
        mSumLoader.forceLoad();
    }

    private void toggleTab(TextView from, TextView to) {
        to.setEnabled(false);
        to.setTextColor(mFragment.getResources().getColor(R.color.primary_text_color));
        from.setEnabled(true);
        from.setTextColor(mFragment.getResources().getColor(R.color.secondary_text_color));
    }

    /**
     * Before calling this method, the required data should be ready.
     */
    private void showComment() {
        // update header view
        if (mCommentHeadView != null) {
            if (mCommentsTotal != 0) {
                mSummaryView.setVisibility(View.VISIBLE);
                mNoCommentView.setVisibility(View.GONE);
            } else {
                mSummaryView.setVisibility(View.GONE);
                mNoCommentView.setVisibility(View.VISIBLE);
            }
            mHeadView.removeView(mCommentHeadView);
            mHeadView.addView(mCommentHeadView);
        }
        // update footer view
        mLoadingMoreParent.removeAllViews();
        mLoadingMoreParent.addView(mGetMoreComments);
        // update adapter
        if (mNeedUpdateAdapter) {
            mListView.setAdapter(mCommentAdapter);
            mListView.setOnItemClickListener(null);
        }
    }

    private LoaderCallbacks<?> mListLoaderCB = new LoaderCallbacks<CommentListLoader.Result>() {
        @Override
        public Loader<CommentListLoader.Result> onCreateLoader(int id, Bundle arg1) {
            if (id == COMMENT_LIST_LOADER) {
                mListLoader = new CommentListLoader(mFragment.getActivity(), mProductId);
                return mListLoader;
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<CommentListLoader.Result> arg0, CommentListLoader.Result result) {
            if (0 == result.getCount())
                return;
            // prepare required adapter
            mCommentInfoList = result.mCommentInfoList;
            // check next page
            if (!mListLoader.hasNextPage() || mCommentInfoList.size() == result.mTotalCount) {
                mGetMoreComments.setVisibility(View.GONE);
                mCommentInfoList.get(mCommentInfoList.size() - 1).setBottomLineVisibility(View.VISIBLE);
            } else {
                mGetMoreComments.setVisibility(View.VISIBLE);
                mCommentInfoList.get(mCommentInfoList.size() - 1).setBottomLineVisibility(View.GONE);
            }
            mCommentAdapter.updateData(mCommentInfoList);
            // if need show comments, update ui
            if (!mShowCommentBtn.isEnabled()) {
                showComment();
            }
        }

        @Override
        public void onLoaderReset(Loader<CommentListLoader.Result> arg0) {
        }
    };

    private LoaderCallbacks<?> mSumLoaderCB = new LoaderCallbacks<CommentSumLoader.Result>() {
        @Override
        public Loader<CommentSumLoader.Result> onCreateLoader(int id, Bundle arg1) {
            if (id == COMMENT_SUM_LOADER) {
                mSumLoader = new CommentSumLoader(mFragment.getActivity(), mProductId);
                return mSumLoader;
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<CommentSumLoader.Result> arg0, CommentSumLoader.Result result) {
            CommentSumInfo info = result.mSummaryInfo;
            if (info == null)
                return;
            // prepare required view
            if (mCommentHeadView == null) {
                LayoutInflater inflater = mFragment.getLayoutInflater(null);
                mCommentHeadView = inflater.inflate(R.layout.comment_sum, null, false);
                mNoCommentView = mCommentHeadView.findViewById(R.id.no_comment_view);
                mSummaryView = mCommentHeadView.findViewById(R.id.comment_sum_view);
            }
            TextView mainGoodPercent = (TextView) mCommentHeadView.findViewById(R.id.comment_sum_good_percents);
            TextView goodPercent = (TextView) mCommentHeadView.findViewById(R.id.comment_good_percent);
            TextView generalPercent = (TextView) mCommentHeadView.findViewById(R.id.comment_general_percent);
            TextView badPercent = (TextView) mCommentHeadView.findViewById(R.id.comment_bad_percent);
            ProgressBar goodBar = (ProgressBar) mCommentHeadView.findViewById(R.id.comment_bar_good);
            ProgressBar generalBar = (ProgressBar) mCommentHeadView.findViewById(R.id.comment_bar_general);
            ProgressBar badBar = (ProgressBar) mCommentHeadView.findViewById(R.id.comment_bar_bad);
            goodPercent.setText(mFragment.getString(R.string.comment_percent, info.getGood()));
            generalPercent.setText(mFragment.getString(R.string.comment_percent, info.getGeneral()));
            badPercent.setText(mFragment.getString(R.string.comment_percent, info.getBad()));
            mainGoodPercent.setText("" + info.getGood());
            goodBar.setProgress(info.getGood());
            generalBar.setProgress(info.getGeneral());
            badBar.setProgress(info.getBad());
            mCommentsTotal = info.getTotal();
            // if need show comments, update ui
            if (!mShowCommentBtn.isEnabled()) {
                showComment();
            }
        }

        @Override
        public void onLoaderReset(Loader<CommentSumLoader.Result> arg0) {
        }
    };

    private LoaderCallbacks<?> mTotalLoaderCB = new LoaderCallbacks<CommentTotalLoader.Result>() {
        @Override
        public Loader<CommentTotalLoader.Result> onCreateLoader(int id, Bundle arg1) {
            if (id == COMMENT_TOTAL_LOADER) {
                mTotalLoader = new CommentTotalLoader(mFragment.getActivity(), mProductId);
                return mTotalLoader;
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<CommentTotalLoader.Result> arg0, CommentTotalLoader.Result result) {
            mCommentsTotal = result.mTotal;
            mShowCommentBtn.setText(mFragment.getString(R.string.comment, result.mTotal));
        }

        @Override
        public void onLoaderReset(Loader<CommentTotalLoader.Result> arg0) {
        }
    };
}
