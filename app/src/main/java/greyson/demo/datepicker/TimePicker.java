package greyson.demo.datepicker;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import greyson.demo.datepicker.utils.SizeUtils;
import greyson.demo.datepicker.wheelView.ArrayWheelAdapter;
import greyson.demo.datepicker.wheelView.LineConfig;
import greyson.demo.datepicker.wheelView.OnItemPickListener;
import greyson.demo.datepicker.wheelView.WheelView;

/**
 * Created by Greyson
 */
public class TimePicker extends LinearLayout {

    private final WheelView mHourView = new WheelView(getContext());
    private final WheelView mMinuteView = new WheelView(getContext());
    private ArrayList<String> mHourList = new ArrayList<>();
    private ArrayList<String> mMinuteList = new ArrayList<>();

    private OnWheelListener onWheelListener;

    private String mSelectedHour, mSelectedMinute;
    private int mMinuteGap;//分钟选择器中分钟数之间的间隔，如平时显示的0,1,2...59，间隔为1

    public TimePicker(Context context) {
        this(context, 1);
    }

    /**
     * @param context
     * @param minuteGap 修改时间选择器中分钟数的间隔（必须是大于等于1的整数），默认为1
     */
    public TimePicker(Context context, int minuteGap) {
        this(context, null, 0, minuteGap);
    }

    public TimePicker(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0, 1);
    }

    public TimePicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 1);
    }

    public TimePicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int minuteGap) {
        super(context, attrs, defStyleAttr);
        mMinuteGap = minuteGap <= 0 ? 1 : minuteGap;
        init();
    }

    private void init() {
        setGravity(Gravity.CENTER);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;


        for (int i = 0; i <= 23; i++) {
            mHourList.add(fillZero(i));
        }

        mHourView.setCanLoop(false);
        mHourView.setTypeface(Typeface.SERIF);
        mHourView.setDividerType(LineConfig.DividerType.FILL);
        mHourView.setAdapter(new ArrayWheelAdapter<>(mHourList));
        mSelectedHour = mHourView.getCurrentItem();
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
        lableLP.bottomMargin = SizeUtils.dp2px(getContext(), 3);
        lableLP.leftMargin = SizeUtils.dp2px(getContext(), 29);
        lableLP.rightMargin = SizeUtils.dp2px(getContext(), 27);
        labelView.setLayoutParams(lableLP);
        labelView.setTextColor(Color.parseColor("#283851"));
        labelView.setTextSize(getResources().getDimensionPixelSize(R.dimen.text_wheel_time_colon));
        labelView.setText(":");
        addView(labelView);

        //分钟
        mMinuteView.setCanLoop(false);
        mMinuteView.setTypeface(Typeface.DEFAULT);
        mSelectedMinute = updateMinuteDateWithGap(mMinuteGap);

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

    /**
     * 以新的间隔，更新分钟滚轮的数据，如gap=1时，数据为：0,1,2,3...59；gap为15时，数据为：0,15,30,45
     *
     * @param gap 间隔数大小
     * @return 更新数据后当前选中的分钟数
     */
    public String updateMinuteDateWithGap(int gap) {
        mMinuteGap = gap;
        mMinuteList.clear();
        for (int i = 0; i < 60; i = i + gap) {
            mMinuteList.add(fillZero(i));
        }
        mMinuteView.setAdapter(new ArrayWheelAdapter<>(mMinuteList));

        if (gap > 1) {
            mSelectedMinute = fillZero(getNearMinuteInCurrGap(mSelectedMinute));
        }
        int selectedIndex = mMinuteList.indexOf(mSelectedMinute);
        mMinuteView.setCurrentItem(selectedIndex);
        return mSelectedMinute;
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

    public String getSelectedHour() {
        return mSelectedHour;
    }

    public String getSelectedMinute() {
        return mSelectedMinute;
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

        if (!timeStr.matches("^(([0,1][0-9])|(2[0-3])):([0-5][0-9])$")) {//检查传入的时间是否合法
            return;
        }


        if (mMinuteGap == 15) {
            if (timeStr.matches("^(([0,1][0-9])|(2[0-3])):((00)|(15)|(30)|(45))$")) {//传入的时间的分钟数刚好是15分为间隔
                String[] times = timeStr.split(":");
                mSelectedHour = times[0];
                mSelectedMinute = times[1];
                mHourView.setCurrentItem(mHourList.indexOf(mSelectedHour));
                mMinuteView.setCurrentItem(mMinuteList.indexOf(mSelectedMinute));

            } else {
                String[] times = timeStr.split(":");
                mSelectedHour = times[0];
                mHourView.setCurrentItem(mHourList.indexOf(mSelectedHour));

//              minuteIndex = timeInt / 15 + 1;
                mSelectedMinute = fillZero(getNearMinuteInCurrGap(times[1]));
                int minuteIndex = mMinuteList.indexOf(mSelectedMinute);
                mMinuteView.setCurrentItem(minuteIndex);

                if (onWheelListener != null) {//因为传入的分钟数不符合15分为间隔而进行了"取整"，所以要通过外面更新目前选中的分钟数
                    onWheelListener.onMinuteWheeled(minuteIndex, mSelectedMinute);
                }
            }
        } else {
            String[] times = timeStr.split(":");
            mSelectedHour = times[0];
            mSelectedMinute = times[1];
            mHourView.setCurrentItem(mHourList.indexOf(mSelectedHour));
            mMinuteView.setCurrentItem(mMinuteList.indexOf(mSelectedMinute));
        }
    }

    /**
     * 参考{@link #getNearMinuteInCurrGap(int)}
     *
     * @param minute
     * @return
     */
    private int getNearMinuteInCurrGap(String minute) {
        int minuteInt = 0;
        try {
            minuteInt = Integer.valueOf(minute);
        } catch (Exception e) {
        }
        return getNearMinuteInCurrGap(minuteInt);
    }

    /**
     * 通过已给的分钟数，转换成当前分钟间隔数下最接近的某个数值，如当{@link #mMinuteGap}=15时，17转为15，23转为30。
     *
     * @param minute 想要转为的分钟数
     * @return 当前分钟间隔下的分钟数据列表中的一项
     */
    private int getNearMinuteInCurrGap(int minute) {
        int nearMinute = 0;
        int scale = minute / mMinuteGap;
        int remainder = minute % mMinuteGap;
        if (remainder == 0) {
            nearMinute = minute;
        } else if (remainder <= mMinuteGap / 2) {
            nearMinute = scale * mMinuteGap;
        } else {
            nearMinute = (scale + 1) * mMinuteGap;
        }
        return nearMinute;
    }

    public void setOnWheelListener(OnWheelListener onWheelListener) {
        this.onWheelListener = onWheelListener;
    }

    public interface OnWheelListener {

        void onHourWheeled(int index, String hour);

        void onMinuteWheeled(int index, String minute);

    }
}
