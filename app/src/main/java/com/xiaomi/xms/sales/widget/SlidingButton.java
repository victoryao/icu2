package com.xiaomi.xms.sales.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.CheckBox;

import com.xiaomi.xms.sales.R;

/**
 * SlidingButton is an iphone-style toggle checkbox. The user can click it to
 * toggle the button's on/off status. Also they can drag the slider to toggle
 * the button's on/off status.
 */
public class SlidingButton extends CheckBox {
    private static final int MSG_ANIMATE = 1000;
    private static final int MSG_TOGGLING_ANIMATE = 1001;

    private static final int ANIMATION_FRAME_DURATION = 1000 / 60;
    private static final int TAP_THRESHOLD = 6;
    private static final float MAXIMUM_MINOR_VELOCITY = 150.0f;
    private static final int ANIMATION_TOGGLINE_FRAMES = 20;

    private Drawable mFrame;                        // Change from BitmapDrawable to Drawable for V5
    private BitmapDrawable mSlider;
    private BitmapDrawable mPressedSlider;
    private BitmapDrawable mActiveSlider;
    private BitmapDrawable mOnDisable;
    private BitmapDrawable mOffDisable;

    // For V5 start
    private Bitmap mSlideOff;
    private Paint mSlideOffPaint;
    private Bitmap mSlideOn;
    private Paint mSlideOnPaint;
    private Drawable mSlideMask;
    // For V5 end

    private int[] mBarSlice;
    private int[] mAlphaPixels;
    private int mWidth;
    private int mHeight;
    private int mSliderWidth;
    private int mSliderPositionStart;               // Add for V5
    private int mSliderPositionEnd;                 // Change from mSliderPosition to mSliderEndPosition for V5
    private boolean bDoAlphaAnimation = false;      // Add for V5
    private boolean mAnimating = false;
    private int mSliderOffset;
    private int mLastX;
    private int mOriginalTouchPointX;
    private boolean mTracking;
    private boolean mSliderMoved;

    private final Handler mHandler = new SlidingHandler();

    private long mAnimationLastTime;
    private float mAnimationPosition;
    private long mCurrentAnimationTime;
    private float mAnimatedVelocity = 150.0f;
    private int mTapThreshold;

    private long mCurrentTogglingAnimationTime;

    public static interface OnCheckedChangedListener {
        void onCheckedChanged(boolean isChecked);
    }

    private OnCheckedChangedListener mOnCheckedChangedListener = null;

    public void setOnCheckedChangedListener(OnCheckedChangedListener listener) {
        mOnCheckedChangedListener = listener;
    }

    public SlidingButton(Context context) {
        this(context, null);
    }

    public SlidingButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs, defStyle);
    }

    public SlidingButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyle) {
        setDrawingCacheEnabled(false);
        final float density = getResources().getDisplayMetrics().density;
        mTapThreshold = (int) (TAP_THRESHOLD * density + 0.5f);

        // Change from BitmapDrawable to Drawable for V5
        Resources res = context.getResources();
        mFrame = res.getDrawable(R.drawable.sliding_btn_frame);//(Drawable) a.getDrawable(R.styleable.SlidingButton_buttonFrame);
        mSlider = (BitmapDrawable) res.getDrawable(R.drawable.sliding_btn_slider);//(BitmapDrawable) a.getDrawable(R.styleable.SlidingButton_buttonSlider);
        mPressedSlider = (BitmapDrawable) res.getDrawable(R.drawable.sliding_btn_slider_pressed);//(BitmapDrawable) a.getDrawable(R.styleable.SlidingButton_buttonSliderPressed);
        mOnDisable = (BitmapDrawable) res.getDrawable(R.drawable.sliding_btn_on_disable);//(BitmapDrawable) a.getDrawable(R.styleable.SlidingButton_buttonOnDisable);
        mOffDisable = (BitmapDrawable) res.getDrawable(R.drawable.sliding_btn_off_disable);//(BitmapDrawable) a.getDrawable(R.styleable.SlidingButton_buttonOffDisable);

        mWidth = mFrame.getIntrinsicWidth();
        mHeight = mFrame.getIntrinsicHeight();

        mActiveSlider = mSlider;
        mSliderWidth = Math.min(mWidth, mSlider.getIntrinsicWidth());
        mSliderPositionStart = 0;
        mSliderPositionEnd = mWidth - mSliderWidth;
        // Add from V5
        mSliderOffset = mSliderPositionStart;

        // For V5 start
        BitmapDrawable slideOff = (BitmapDrawable)res.getDrawable(R.drawable.sliding_btn_off);//a.getDrawable(R.styleable.SlidingButton_buttonBarOff);
        mSlideOff = Bitmap.createScaledBitmap(slideOff.getBitmap(),
                mWidth * 2 - mSliderWidth,
                mHeight,
                true);

        BitmapDrawable slidingOn = (BitmapDrawable) res.getDrawable(R.drawable.sliding_btn_on);//(R.styleable.SlidingButton_buttonBarOn);
        mSlideOn = Bitmap.createScaledBitmap(slidingOn.getBitmap(),
                    mWidth * 2 - mSliderWidth,
                    mHeight, true);
        mSlideMask = res.getDrawable(R.drawable.sliding_btn_mask);//(Drawable) a.getDrawable(R.styleable.SlidingButton_buttonMask);
        // For V5 end

        mFrame.setBounds(0, 0, mWidth, mHeight);
        mOnDisable.setBounds(0, 0, mWidth, mHeight);
        mOffDisable.setBounds(0, 0, mWidth, mHeight);

        // scale the mask to match size of frame
        mAlphaPixels = new int[mWidth * mHeight];
        Bitmap source = ((BitmapDrawable)res.getDrawable(R.drawable.sliding_btn_mask)).getBitmap();
        Bitmap alphaCutter = Bitmap.createScaledBitmap(
                source,
                mWidth,
                mHeight,
                false);
        alphaCutter.getPixels(mAlphaPixels, 0, mWidth, 0, 0, mWidth, mHeight);
        if (alphaCutter != source) {
            alphaCutter.recycle();
        }

        // scale the bottom bar to match size of frame
        mBarSlice = new int[mWidth * mHeight];

        mSlideOffPaint = new Paint();
        mSlideOnPaint = new Paint();
    }

    @Override
    public void setChecked(boolean checked) {
        boolean oldState = isChecked();
        super.setChecked(checked);
        mActiveSlider = mSlider;
        mSliderOffset = checked ? mSliderPositionEnd : mSliderPositionStart;

        if (oldState != checked) {
            if (bDoAlphaAnimation) {
                doTogglingAnimation(0);
            } else {
                mSlideOnPaint.setAlpha(checked ? 255 : 0);
                mSlideOffPaint.setAlpha(!checked ? 255 : 0);
                invalidate();
            }
        }
    }

    @Override
    public void setButtonDrawable(Drawable d) {
        //delibrately do nothing
        return;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (! isEnabled())
            return false;

        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        Rect sliderFrame = new Rect(mSliderOffset, 0, mSliderOffset + mSliderWidth, mHeight);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (sliderFrame.contains(x, y)) {
                    mTracking = true;
                    mActiveSlider = mPressedSlider;
                    invalidate();
                } else {
                    mTracking = false;
                }
                mLastX = x;
                mOriginalTouchPointX = x;
                mSliderMoved = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mTracking) {
                    moveSlider(x - mLastX);
                    mLastX = x;
                    if (Math.abs(x - mOriginalTouchPointX) >= mTapThreshold) {
                        mSliderMoved = true;
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mTracking) {
                    if (!mSliderMoved) {
                        animateToggle();
                    } else {
                        if (mSliderOffset >= mSliderPositionStart && mSliderOffset <= mSliderPositionEnd / 2) {
                            animateOff();
                        } else {
                            animateOn();
                        }
                    }
                } else {
                    animateToggle();
                }
                mTracking = false;
                mSliderMoved = false;
                break;

            case MotionEvent.ACTION_CANCEL:
                mTracking = false;
                mSliderMoved = false;
                break;
        }

        return true;
    }

    private void animateToggle() {
        if (isChecked()) {
            animateOff();
        } else {
            animateOn();
        }
    }

    private void animateOn() {
        performFling(MAXIMUM_MINOR_VELOCITY);
        invalidate();
    }

    private void animateOff() {
        performFling(-MAXIMUM_MINOR_VELOCITY);
        invalidate();
    }

    private void moveSlider(int offsetX) {
        // check the edge condition
        mSliderOffset += offsetX;
        if (mSliderOffset < mSliderPositionStart) {
            mSliderOffset = mSliderPositionStart;
        } else if (mSliderOffset > mSliderPositionEnd) {
            mSliderOffset = mSliderPositionEnd;
        }
        invalidate();
    }

    private void performFling(float velocity) {
        mAnimating = true;
        mAnimationPosition = 0;
        mAnimatedVelocity = velocity;
        long now = SystemClock.uptimeMillis();
        mAnimationLastTime = now;
        mCurrentAnimationTime = now + ANIMATION_FRAME_DURATION;
        mHandler.removeMessages(MSG_ANIMATE);
        mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE), mCurrentAnimationTime);
    }

    private void doAnimation() {
        if (!mAnimating) {
            return;
        }
        incrementAnimation();
        moveSlider((int) mAnimationPosition);
        if (mSliderOffset <= mSliderPositionStart || mSliderOffset >= mSliderPositionEnd) {
            mHandler.removeMessages(MSG_ANIMATE);
            mAnimating = false;
            bDoAlphaAnimation = true;
            setChecked(mSliderOffset >= mSliderPositionEnd);
            if (mOnCheckedChangedListener != null) {
                mOnCheckedChangedListener.onCheckedChanged(isChecked());
            }
        } else {
            mCurrentAnimationTime += ANIMATION_FRAME_DURATION;
            mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE), mCurrentAnimationTime);
        }
    }

    private void incrementAnimation() {
        long now = SystemClock.uptimeMillis();
        float t = (now - mAnimationLastTime) / 1000.0f; // ms -> s
        final float position = mAnimationPosition;
        mAnimationPosition = position + mAnimatedVelocity * t;
        mAnimationLastTime = now;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isEnabled()) {
            if (isChecked()) {
                mOnDisable.draw(canvas);
            } else {
                mOffDisable.draw(canvas);
            }
        } else {
            // For V5 start
            // draw the background (on or off)
            drawSlidingBar(canvas);
            // draw the frame
            mFrame.draw(canvas);
            // draw mask
            mSlideMask.draw(canvas);
            // draw the slider
            mActiveSlider.setBounds(mSliderOffset, 0, mSliderWidth + mSliderOffset, mHeight);
            mActiveSlider.draw(canvas);
            // V5 end
        }
    }

    // Change code for V5 (do not cut bitmap everytime)
    private void drawSlidingBar(Canvas canvas) {
        // get the bar slice
        int barOffset = mSliderPositionEnd - mSliderOffset;

        if (mSlideOnPaint.getAlpha() != 0) {
            mSlideOn.getPixels(mBarSlice, 0, mWidth, barOffset, 0, mWidth, mHeight);

            // cut the edge of bar slice
            cutEdge(mWidth, mHeight, mBarSlice);
            canvas.drawBitmap(mBarSlice, 0, mWidth, 0, 0, mWidth, mHeight, true, mSlideOnPaint);
        }

        if (mSlideOffPaint.getAlpha() != 0) {
            mSlideOff.getPixels(mBarSlice, 0, mWidth, barOffset, 0, mWidth, mHeight);

            // cut the edge of bar slice
            cutEdge(mWidth, mHeight, mBarSlice);
            canvas.drawBitmap(mBarSlice, 0, mWidth, 0, 0, mWidth, mHeight, true, mSlideOffPaint);
        }
    }

    private void cutEdge(int baseWidth, int baseHeight, int[] basePixels) {
        // get the mask for cutting edges of content of the base
        int sRGBMask = 0x00ffffff;
        int sAlphaShift = 24;

        for (int i = baseWidth * baseHeight - 1; i >= 0; i--) {
            basePixels[i] = basePixels[i]
                    & sRGBMask
                    + (((basePixels[i] >>> sAlphaShift) * (mAlphaPixels[i] >>> sAlphaShift) / 0xff) << sAlphaShift);
        }
    }

    private void doTogglingAnimation(int frame) {
        if (mSlideOn == mSlideOff) {
            // Needs no animation if the two slide are same
            return;
        }

        mHandler.removeMessages(MSG_TOGGLING_ANIMATE);
        if (frame == 0) {
            mCurrentTogglingAnimationTime = SystemClock.uptimeMillis();
        }

        if (frame < ANIMATION_TOGGLINE_FRAMES) {
            ++frame;
            int alpha = 255 * frame / ANIMATION_TOGGLINE_FRAMES;
            if (isChecked()) {
                mSlideOffPaint.setAlpha(255 - alpha);
                mSlideOnPaint.setAlpha(alpha);
            } else {
                mSlideOffPaint.setAlpha(alpha);
                mSlideOnPaint.setAlpha(255 - alpha);
            }

            mCurrentTogglingAnimationTime += ANIMATION_FRAME_DURATION;
            Message msg = mHandler.obtainMessage(MSG_TOGGLING_ANIMATE, frame, 0);
            mHandler.sendMessageAtTime(msg, mCurrentTogglingAnimationTime);
            invalidate();
        }
        bDoAlphaAnimation = false;
    }

    private class SlidingHandler extends Handler {
        public void handleMessage(Message m) {
            switch (m.what) {
                case MSG_ANIMATE:
                    doAnimation();
                    break;
                case MSG_TOGGLING_ANIMATE:
                    doTogglingAnimation(m.arg1);
                    break;
            }
        }
    }
}
