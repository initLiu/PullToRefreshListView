package com.lzp.pulltorefreshlistview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by SKJP on 2016/11/25.
 */

public class PullRefreshHeaderLayout extends RelativeLayout {
    private int paddingTop;

    public PullRefreshHeaderLayout(Context context) {
        super(context);
    }

    public PullRefreshHeaderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullRefreshHeaderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int getPaddingTop() {
        return paddingTop;
    }

    public void setPaddingTop(int paddingTop) {
        this.paddingTop = paddingTop;
        setPadding(0, paddingTop, 0, 0);
        invalidate();
    }
}
