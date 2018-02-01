package com.alexzaitsev.meternumberpicker;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class MeterView extends LinearLayout {

    private static final int DEFAULT_NUMBER_OF_BLACK = 5;
    private static final int DEFAULT_NUMBER_OF_RED = 0;
    private static final int DEFAULT_BLACK_COLOR = 0xFF000000;
    private static final int DEFAULT_RED_COLOR = 0xFFCC0000;
    private static final boolean DEFAULT_ENABLED = true;

    private int numberOfFirst = DEFAULT_NUMBER_OF_BLACK;
    private int numberOfSecond = DEFAULT_NUMBER_OF_RED;
    private int firstColor = DEFAULT_BLACK_COLOR;
    private int secondColor = DEFAULT_RED_COLOR;
    private boolean enabled = DEFAULT_ENABLED;

    private int pickerStyleId = -1;

    public MeterView(Context context) {
        super(context);
        init(context, null);
    }

    public MeterView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MeterView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MeterView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        setOrientation(HORIZONTAL);
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MeterView, 0, 0);
            numberOfFirst = typedArray.getInt(R.styleable.MeterView_mv_numberOfFirst, numberOfFirst);
            numberOfSecond = typedArray.getInt(R.styleable.MeterView_mv_numberOfSecond, numberOfSecond);
            firstColor = typedArray.getColor(R.styleable.MeterView_mv_firstColor, firstColor);
            secondColor = typedArray.getColor(R.styleable.MeterView_mv_secondColor, secondColor);
            pickerStyleId = typedArray.getResourceId(R.styleable.MeterView_mv_pickerStyle, pickerStyleId);
            enabled = typedArray.getBoolean(R.styleable.MeterView_mv_enabled, enabled);
            typedArray.recycle();
        }
        populate(context);
    }

    private void populate(Context context) {
        for (int i = 0; i < numberOfFirst + numberOfSecond; i++) {
            MeterNumberPicker meterNumberPicker = createPicker(context);
            meterNumberPicker.setBackgroundColor(i < numberOfFirst ? firstColor : secondColor);
            meterNumberPicker.setEnabled(isEnabled());
            LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            lp.weight = 1;
            addView(meterNumberPicker, lp);
        }
    }

    private MeterNumberPicker createPicker(Context context) {
        return pickerStyleId == -1 ? new MeterNumberPicker(context) : new MeterNumberPicker(context, pickerStyleId);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return !enabled || super.onInterceptTouchEvent(ev);
    }

    /**
     * Returns current value of the widget. Works only if "mnp_max" is not bigger then 9.
     * For other cases you have to extend this view for now.
     */
    public int getValue() {
        int result = 0;
        int koeff = getChildCount();
        for (int i = 0; i < getChildCount(); i++) {
            MeterNumberPicker picker = (MeterNumberPicker) getChildAt(i);
            result += picker.getValue() * Math.pow(10, --koeff);
        }
        return result;
    }

    /**
     * Sets current value to the widget. Works only if "mnp_max" is not bigger then 9.
     * For other cases you have to extend this view for now.
     */
    public void setValue(int value) {
        int koeff = getChildCount();
        for (int i = 0; i < getChildCount(); i++) {
            MeterNumberPicker picker = (MeterNumberPicker) getChildAt(i);
            int number = (int) (value / Math.pow(10, --koeff));
            if (i == 0 && number > 9) {
                throw new IllegalArgumentException("Number of digits cannot be greater then pickers number");
            }
            value -= number * Math.pow(10, koeff);
            picker.setValue(number);
        }
    }
}
