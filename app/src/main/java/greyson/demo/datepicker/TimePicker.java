package greyson.demo.datepicker;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import greyson.demo.datepicker.utils.MeasureUtil;
import greyson.demo.datepicker.wheelView.ArrayWheelAdapter;
import greyson.demo.datepicker.wheelView.LineConfig;
import greyson.demo.datepicker.wheelView.OnItemPickListener;
import greyson.demo.datepicker.wheelView.WheelView;

/**
 * Created by Greyson
 */
public class TimePicker extends LinearLayout {

    final WheelView mHourView = new WheelView(getContext());
    final WheelView mMinuteView = new WheelView(getContext());
    private ArrayList<String> mHourList = new ArrayList<>();
    private ArrayList<String> mMinuteList = new ArrayList<>();

    private OnWheelListener onWheelListener;

    private String mSelectedHour, mSelectedMinute;

    public TimePicker(Context context) {
        this(context, null);
    }

    public TimePicker(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimePicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setGravity(Gravity.CENTER);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;


        for (int i = 1; i <= 24; i++) {
            mHourList.add(fillZero(i));
        }

        for (int i = 0; i < 60; i = i + 15) {
            mMinuteList.add(fillZero(i));
        }

        mHourView.setCanLoop(true);
//        mHourView.setSelectedTextColor(textColorFocus);
//        mHourView.setUnSelectedTextColor(textColorNormal);
        mHourView.setDividerType(LineConfig.DividerType.FILL);
        mHourView.setAdapter(new ArrayWheelAdapter<>(mHourList));
//        mHourView.setCurrentItem(mSelectedHourIndex);
//        mHourView.setLineConfig(lineConfig);
        mHourView.setLayoutParams(layoutParams);
        mHourView.setOnItemPickListener(new OnItemPickListener<String>() {
            @Override
            public void onItemPicked(int index, String item) {
                mSelectedHour = item;
                if (onWheelListener != null) {
                    onWheelListener.onHourWheeled(index, item);
                }
               /* if (!canLinkage) {
                    return;
                }
//                changeMinuteData(trimZero(item));
                mMinuteView.setAdapter(new ArrayWheelAdapter<>(minutes));
                mMinuteView.setCurrentItem(mSelectedMinuteIndex);*/
            }
        });
        addView(mHourView);

        TextView labelView = new TextView(getContext());
        LayoutParams lableLP = new LayoutParams(layoutParams.width, layoutParams.height);
        lableLP.gravity = layoutParams.gravity;
        lableLP.leftMargin = MeasureUtil.dp2px(getContext(), 29);
        lableLP.rightMargin = MeasureUtil.dp2px(getContext(), 27);
        labelView.setLayoutParams(lableLP);
        labelView.setTextColor(Color.parseColor("#283851"));
        labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        labelView.setText(":");
        addView(labelView);

        //分钟
        mMinuteView.setCanLoop(true);
//        mMinuteView.setTextSize(textSize);//must be called before setDateList
//        mMinuteView.setSelectedTextColor(textColorFocus);
//        mMinuteView.setUnSelectedTextColor(textColorNormal);
        mMinuteView.setAdapter(new ArrayWheelAdapter<>(mMinuteList));
//        mMinuteView.setCurrentItem(mSelectedMinuteIndex);
        mMinuteView.setDividerType(LineConfig.DividerType.FILL);
//        mMinuteView.setLineConfig(lineConfig);
        mMinuteView.setLayoutParams(layoutParams);
        addView(mMinuteView);
        mMinuteView.setOnItemPickListener(new OnItemPickListener<String>() {
            @Override
            public void onItemPicked(int index, String item) {
                mSelectedMinute = item;
                if (onWheelListener != null) {
                    onWheelListener.onMinuteWheeled(index, item);
                }
            }
        });
    }


    /*private void changeMinuteData(int selectedHour) {
        if (startHour == endHour) {
            if (startMinute > endMinute) {
                int temp = startMinute;
                startMinute = endMinute;
                endMinute = temp;
            }
            for (int i = startMinute; i <= endMinute; i+= stepMinute) {
                minutes.add(DateUtils.fillZero(i));
            }
        } else if (selectedHour == startHour) {
            for (int i = startMinute; i <= 59; i+= stepMinute) {
                minutes.add(DateUtils.fillZero(i));
            }
        } else if (selectedHour == endHour) {
            for (int i = 0; i <= endMinute; i+= stepMinute) {
                minutes.add(DateUtils.fillZero(i));
            }
        } else {
            for (int i = 0; i <= 59; i+= stepMinute) {
                minutes.add(DateUtils.fillZero(i));
            }
        }
        if (minutes.indexOf(selectedMinute) == -1) {
            //当前设置的分钟不在指定范围，则默认选中范围开始的分钟
            selectedMinute = minutes.get(0);
        }
    }*/

    public static int trimZero(@NonNull String text) {
        try {
            if (text.startsWith("0")) {
                text = text.substring(1);
            }
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @NonNull
    public static String fillZero(int number) {
        return number < 10 ? "0" + number : String.valueOf(number);
    }

    /**
     * 设置选中的时间
     *
     * @param timeStr 时间格式：01:30 / 11:45
     */
    public void setSelectedTime(String timeStr) {
        if (timeStr == null) {
            return;
        }

        if (timeStr.matches("^((0[1-9])|(1[0-9])|(2[0-4])):((00)|(15)|(30)|(45))$")) {
            String[] times = timeStr.split(":");
            mSelectedHour = times[0];
            mSelectedMinute = times[1];
            mHourView.setCurrentItem(mHourList.indexOf(mSelectedHour));
            mMinuteView.setCurrentItem(mMinuteList.indexOf(mSelectedMinute));

        } else if (timeStr.matches("^((0[1-9])|(1[0-9])|(2[0-4])):([0-5][0-9])$")) {
            String[] times = timeStr.split(":");
            mSelectedHour = times[0];
            mHourView.setCurrentItem(mHourList.indexOf(mSelectedHour));

            int minuteIndex = 0;
            try {
                int timeInt = Integer.valueOf(times[1]);
                minuteIndex = timeInt / 15 + 1;
                if (minuteIndex == 4) {
                    minuteIndex--;
                }
            } catch (Exception e) {
            }

            mSelectedMinute = mMinuteList.get(minuteIndex);
            mMinuteView.setCurrentItem(minuteIndex);

            if (onWheelListener != null) {
                onWheelListener.onMinuteWheeled(minuteIndex, mSelectedMinute);
            }
        }
    }

    public void setOnWheelListener(OnWheelListener onWheelListener) {
        this.onWheelListener = onWheelListener;
    }

    public interface OnWheelListener {

        void onHourWheeled(int index, String hour);

        void onMinuteWheeled(int index, String minute);

    }
}
