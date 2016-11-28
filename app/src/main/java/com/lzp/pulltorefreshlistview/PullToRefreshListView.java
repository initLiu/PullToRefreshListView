package com.lzp.pulltorefreshlistview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by SKJP on 2016/11/24.
 */

public class PullToRefreshListView extends ListView {

    public static final String TAG = PullToRefreshListView.class.getSimpleName();

    private PullRefreshHeaderLayout pullRefreshHeader;
    private int pullRefreshHeaderHeight;

    private TextView txtRefreshMsg;
    private ProgressBar proBarLoading;
    private ImageView imagePull;

    private RotateAnimation flipAnimation,reverseFlipAnimation;

    private static enum STATE {IDLE, PULL_REFRESS, RELEASE_REFRESH, LOADING}

    private STATE mState = STATE.IDLE;
    private int mTouchSlop;
    private float mLastMotionY, mEndMotionY;
    public static final float PULL_FRESH_RATE = 2.5f;
    public final int MIN_REFRESH_DISTANCE;

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 100) {
                completLoading();
            }
        }
    };

    public PullToRefreshListView(Context context) {
        this(context, null);
    }

    public PullToRefreshListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullToRefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        MIN_REFRESH_DISTANCE = pullRefreshHeaderHeight + 100;
    }

    private void init() {
        pullRefreshHeader = (PullRefreshHeaderLayout) LayoutInflater.from(getContext()).inflate(R
                .layout
                .pull_refresh_header, null);

        txtRefreshMsg = (TextView) pullRefreshHeader.findViewById(R.id.refresh_msg_text);
        proBarLoading = (ProgressBar) pullRefreshHeader.findViewById(R.id.refresh_progress);
        imagePull = (ImageView) pullRefreshHeader.findViewById(R.id.refresh_image);

        measureView(pullRefreshHeader);
        pullRefreshHeaderHeight = pullRefreshHeader.getMeasuredHeight();
        pullRefreshHeader.setPaddingTop(-1 * pullRefreshHeaderHeight);
        addHeaderView(pullRefreshHeader);
        ViewConfiguration config = ViewConfiguration.get(getContext());
        mTouchSlop = config.getScaledTouchSlop();

        flipAnimation = new RotateAnimation(0,180,RotateAnimation.RELATIVE_TO_SELF,0.5f,RotateAnimation.RELATIVE_TO_SELF,0.5f);
        flipAnimation.setInterpolator(new LinearInterpolator());
        flipAnimation.setDuration(250);
        flipAnimation.setFillAfter(true);

        reverseFlipAnimation = new RotateAnimation(-180,0,RotateAnimation.RELATIVE_TO_SELF,0.5f,RotateAnimation.RELATIVE_TO_SELF,0.5f);
        reverseFlipAnimation.setInterpolator(new LinearInterpolator());
        reverseFlipAnimation.setDuration(250);
        reverseFlipAnimation.setFillAfter(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isFirstItemVisible() && mState == STATE.IDLE) {
                    mLastMotionY = ev.getY();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                doMoveEvent(ev);
                break;
            case MotionEvent.ACTION_UP:
                doUpEvent(ev);
                break;
            default:
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void doUpEvent(MotionEvent ev) {
        switch (mState) {
            case PULL_REFRESS:
                resetRefreshHeaderWithAnim();
                break;
            case RELEASE_REFRESH:
                stateToLoading();
                break;
        }
    }

    private void doMoveEvent(MotionEvent ev) {
        if (isFirstItemVisible()) {
            float deltaY = ev.getY() - mLastMotionY;
            switch (mState) {
                case IDLE:
                    if (deltaY > 0 && deltaY > mTouchSlop) {
                        mState = STATE.PULL_REFRESS;
                    } else if (deltaY <= 0) {
                        resetRefreshHeader();
                    }
                    break;
                case PULL_REFRESS:
                    if (deltaY > 0 && deltaY > mTouchSlop) {
                        int topPadding = (int) ((deltaY - pullRefreshHeaderHeight) /
                                PULL_FRESH_RATE);
                        if (pullRefreshHeader.getBottom() >= MIN_REFRESH_DISTANCE) {
                            mState = STATE.RELEASE_REFRESH;
                            updateRefreshHeader();
                        }
                        updateRefreshHeaderTopPadding(topPadding);
                    }
                    break;
                case RELEASE_REFRESH:
                    if (Math.abs(deltaY) > mTouchSlop) {
                        int topPadding = (int) ((deltaY - pullRefreshHeaderHeight) /
                                PULL_FRESH_RATE);
                        if (pullRefreshHeader.getBottom() < MIN_REFRESH_DISTANCE) {
                            mState = STATE.PULL_REFRESS;
                            updateRefreshHeader();
                        }
                        updateRefreshHeaderTopPadding(topPadding);
                    }
                    break;
                case LOADING:
                    if (Math.abs(deltaY) > mTouchSlop) {
                        int topPadding = (int) ((deltaY - pullRefreshHeaderHeight) /
                                PULL_FRESH_RATE);
                        updateRefreshHeaderTopPadding(topPadding);
                    }
                    break;
            }
        }

    }

    private void updateRefreshHeaderTopPadding(int topPadding) {
        pullRefreshHeader.setPaddingTop(topPadding);
    }

    private void stateToLoading() {
        mState = STATE.LOADING;
        ObjectAnimator animator = ObjectAnimator.ofInt(pullRefreshHeader, "paddingTop",
                pullRefreshHeaderHeight);
        animator.setDuration(500);
        animator.start();
        updateRefreshHeader();
        handler.sendEmptyMessageDelayed(100, 2000);
    }

    private void completLoading() {
        smoothScrollToPosition(0);
        resetRefreshHeader();
    }

    private void resetRefreshHeader() {
        mState = STATE.IDLE;
        updateRefreshHeaderTopPadding(-1 * pullRefreshHeaderHeight);
        updateRefreshHeader();
    }

    private void resetRefreshHeaderWithAnim() {
        mState = STATE.IDLE;
        ObjectAnimator animator = ObjectAnimator.ofInt(pullRefreshHeader, "paddingTop",
                -1 * pullRefreshHeaderHeight);
        animator.setDuration(500);
        animator.start();
        updateRefreshHeader();
    }

    public void updateRefreshHeader() {
        String msg = "";
        switch (mState) {
            case RELEASE_REFRESH:
                msg = "释放立即刷新";
                imagePull.setVisibility(View.VISIBLE);
                proBarLoading.setVisibility(View.INVISIBLE);
                Animation animation = AnimationUtils.loadAnimation(getContext(),R.anim.rotate1);
                animation.setFillAfter(true);
                imagePull.clearAnimation();
                imagePull.startAnimation(animation);
//                imagePull.clearAnimation();
//                imagePull.startAnimation(flipAnimation);
                Log.e(TAG,"RELEASE_REFRESH");
                break;
            case PULL_REFRESS:
                animation = AnimationUtils.loadAnimation(getContext(),R.anim.rotate2);
                animation.setFillAfter(true);
                imagePull.clearAnimation();
                imagePull.startAnimation(animation);
//                imagePull.clearAnimation();
//                imagePull.startAnimation(reverseFlipAnimation);
                Log.e(TAG,"PULL_REFRESS");
            case IDLE:
                msg = "下拉刷新";
                imagePull.setVisibility(View.VISIBLE);
                proBarLoading.setVisibility(View.INVISIBLE);
                break;
            case LOADING:
                msg = "正在刷新...";
                imagePull.clearAnimation();
                imagePull.setVisibility(View.INVISIBLE);
                proBarLoading.setVisibility(View.VISIBLE);
                break;
        }
        txtRefreshMsg.setText(msg);
        pullRefreshHeader.invalidate();
    }

    private void measureView(View view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup
                    .LayoutParams.WRAP_CONTENT);
        }
        int widthMeasureSpec = getChildMeasureSpec(MeasureSpec.UNSPECIFIED, 0, params.width);
        int height = params.height;
        int heightMeasureSpec;
        if (height > 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        } else {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        view.measure(widthMeasureSpec, heightMeasureSpec);
    }

    private boolean isFirstItemVisible() {
        final Adapter adapter = getAdapter();
        if (adapter == null || adapter.isEmpty()) {
            View emptyView = getEmptyView();
            if (emptyView != null && emptyView.getVisibility() == View.VISIBLE) {
                return true;
            } else if (getChildCount() > 0) {
                if (getFirstVisiblePosition() == 0) {
//                    View firstVisibleChild = getChildAt(0);
//                    if (firstVisibleChild != null) {
//                        return firstVisibleChild.getTop() - getPaddingTop() >= getTop();
//                    }
                    return true;
                } else {
                    return false;
                }
            }
            return true;
        } else if (getFirstVisiblePosition() == 0) {
//            View firstVisibleChild = getChildAt(0);
//            if (firstVisibleChild != null) {
//                return firstVisibleChild.getTop() - getPaddingTop() >= getTop();
//            }
            return true;
        }
        return false;
    }
}












