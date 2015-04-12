package com.xiaomi.xms.sales.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;

import com.xiaomi.xms.sales.R;
import com.xiaomi.xms.sales.adapter.AttentionImageAdapter;
import com.xiaomi.xms.sales.util.Constants;

/**
 * 
 * @author 空山不空 扩展Gallery组件，设置滑动一次只加载一张图片，并且， 如果是第一张图片时，向左滑动会提示“已到第一页”
 *         如果是最后一张图片时，向右滑动会提示“已到第后页”
 */
public class GalleryExt extends Gallery {
	boolean is_first = false;
	boolean is_last = false;
	private Button beginBtn;
	private ImageView checkedDot;

	public GalleryExt(Context context) {
		super(context);
	}

	public GalleryExt(Context context, AttributeSet paramAttributeSet) {
		super(context, paramAttributeSet);

	}

	private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2) {
		return e2.getX() > e1.getX();
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// 通过重构onFling方法，使Gallery控件滑动一次只加载一张图片
		// 获取适配器
		AttentionImageAdapter ia = (AttentionImageAdapter) this.getAdapter();
		// 得到当前图片在图片资源中的位置
		int p = ia.getOwnposition();
		if(checkedDot != null){
			if (Constants.picSet.contains(p)) {
				checkedDot.setVisibility(View.VISIBLE);
			} else {
				checkedDot.setVisibility(View.GONE);
			}
		}
		// 图片的总数量
		int count = ia.getCount();
		int kEvent;

		if (isScrollingLeft(e1, e2)) {
			// Check if scrolling left
			if (p == 0 && is_first) {
				// 在第一页并且再往左移动的时候，提示
				Toast.makeText(this.getContext(), "已到第一页", Toast.LENGTH_SHORT)
						.show();
			} else if (p == 0) {
				// 到达第一页时，把is_first设置为true
				is_first = true;
			} else {
				is_last = false;
			}

			kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
		} else {
			// Otherwise scrolling right
			if (p == count - 1 && is_last) {
				Toast.makeText(this.getContext(), "已到最后一页", Toast.LENGTH_SHORT)
						.show();
				beginBtn.setVisibility(View.VISIBLE);
				if (((AttentionImageAdapter) this.getAdapter()).getImgres().length > 5) {
					beginBtn.setText(R.string.picture_test_end);
				}
			} else if (p == count - 1) {
				is_last = true;
			} else {
				is_first = false;
			}

			kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
		}
		onKeyDown(kEvent, null);
		return true;
	}

	public Button getBeginBtn() {
		return beginBtn;
	}

	public void setBeginBtn(Button beginBtn) {
		this.beginBtn = beginBtn;
	}

	public ImageView getCheckedDot() {
		return checkedDot;
	}

	public void setCheckedDot(ImageView checkedDot) {
		this.checkedDot = checkedDot;
	}

}