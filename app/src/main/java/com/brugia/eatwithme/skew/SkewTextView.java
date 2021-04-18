package com.brugia.eatwithme.skew;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

public class SkewTextView extends androidx.appcompat.widget.AppCompatTextView {

    Context context;
    String text = "EatWithMe";

    public SkewTextView(Context context) {
        super(context);
        this.context = context;
    }

    public SkewTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public SkewTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        setText(text);
        setTextSize(40);
        canvas.skew(30f, 1.0f);
        RotateAnimation rotate = new RotateAnimation(-30, 30, 0, 0, 0, 0);
        startAnimation(rotate);

    }
}
