
package com.xiaomi.xms.sales.widget.gallery;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.xiaomi.xms.sales.util.LogUtil;
import com.xiaomi.xms.sales.widget.gallery.ScaleGestureDetector.OnScaleGestureListener;

public class ZoomImageView extends ImageViewTouchBase {
    private static final String TAG = "ZoomImageView";
    private boolean mOnScale;

    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mGestureDetector;

    public interface OnImageTapListener {
        public void onImageTap();
    }

    private OnImageTapListener mImageTapListener;

    public void setOnTapListener(OnImageTapListener l) {
        mImageTapListener = l;
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), new MyGestureScaleListener());
        mGestureDetector = new GestureDetector(getContext(), new MyGestureListener(), null, true);
    }

    public boolean isZoomedOut() {
        return (getScale() - 1F) > 0.1;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        LogUtil.d(TAG, "The action is:" + event.getAction());
        if (!mOnScale) {
            mGestureDetector.onTouchEvent(event);
        }
        try {
            mScaleGestureDetector.onTouchEvent(event);
        } catch (Exception e) {
        }

        return true;
    }

    private class MyGestureScaleListener implements OnScaleGestureListener {
        private float mCurrentScale;
        private float mCurrentMiddleX;
        private float mCurrentMiddleY;

        @Override
        public boolean onScale(ScaleGestureDetector detector, float mx, float my) {
            LogUtil.d(TAG, "gesture onScale");
            float ns = getScale() * detector.getScaleFactor();
            mCurrentScale = ns;
            mCurrentMiddleX = mx;
            mCurrentMiddleY = my;

            if (detector.isInProgress()) {
                zoomToNoCenter(ns, mx, my);
            }
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            LogUtil.d(TAG, "gesture onScaleStart");
            mOnScale = true;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            if (mCurrentScale > mMaxZoom) {
                zoomToNoCenterWithAni(mCurrentScale
                                                / mMaxZoom, 1, mCurrentMiddleX,
                                                mCurrentScale);
                mCurrentScale = mMaxZoom;
                zoomToNoCenterValue(mCurrentScale, mCurrentMiddleX,
                        mCurrentMiddleY);
            } else if (mCurrentScale < mMinZoom) {
                zoomToNoCenterWithAni(mCurrentScale,
                                mMinZoom, mCurrentMiddleX, mCurrentMiddleY);
                mCurrentScale = mMinZoom;
                zoomToNoCenterValue(mCurrentScale, mCurrentMiddleX,
                        mCurrentMiddleY);
            } else {
                zoomToNoCenter(mCurrentScale, mCurrentMiddleX,
                        mCurrentMiddleY);
            }
            mOnScale = false;
        }

    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
            LogUtil.d(TAG, "gesture onScroll");
            if (mOnScale) {
                return true;
            }
            panBy(-distanceX, -distanceY);
            center(true, true);
            return true;
        }

        @Override
        public boolean onUp(MotionEvent e) {
            return super.onUp(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mImageTapListener != null) {
                mImageTapListener.onImageTap();
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // Switch between the original scale and 3x scale.
            if (mBaseZoom < 1) {
                if (getScale() > 2F) {
                    zoomTo(1f);
                } else {
                    zoomToPoint(3f, e.getX(), e.getY());
                }
            } else {
                if (getScale() > (mMinZoom + mMaxZoom) / 2f) {
                    zoomTo(mMinZoom);
                } else {
                    zoomToPoint(mMaxZoom, e.getX(),
                                        e.getY());
                }
            }
            return true;
        }
    }

}
