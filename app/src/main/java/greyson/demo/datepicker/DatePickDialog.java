package greyson.demo.datepicker;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import greyson.demo.datepicker.languages.DPLManager;
import greyson.demo.datepicker.utils.SizeUtils;

import static android.view.Gravity.CENTER;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by Greyson
 */
public class DatePickDialog extends Dialog {
    public static final int MODE_DATE_AND_TIME = 10;
    public static final int MODE_DATE_ONLY = 1;
    public static final int MODE_TIME_ONLY = 2;

    private boolean hasInit;
    private int mMode;
    private DPLManager mDPLManager = DPLManager.getInstance();

    private ViewGroup clDatePicker;
    private TextView mTvOnlyOneSwitch;
    private RadioGroup rgSwitchDateTime;
    private RadioButton cbDateBtn;
    private RadioButton cbTimeBtn;
    private TextView mTvConfirm;

    private FrameLayout mFlContentPanel;//包含日期、时间选择组件的面板
    private View vInflater;
    private LinearLayout mLlCalendarPicker;//包含星期组件的日期选择面板
    private LinearLayout mLlWeek;
    private CalendarPicker mCalendarPicker;
    private TimePicker mTimePicker;

    private String selectedDateStr;
    private String selectedTimeStr;

    private OnDatePickListener onDatePickListener;

    /**
     * 该构造方法默认使用 {@link #MODE_DATE_AND_TIME} 模式，即显示日期和时间视图，不会有默认选中的日期和时间的效果
     * ，可通过 {@link #setSelectedDate(Date)} 或 {@link #setSelectedDate(String, String)} 方法去自定义
     * 选中的日期和时间。
     *
     * @param context dialog所属的上下文对象
     */
    public DatePickDialog(Context context) {
        this(context, MODE_DATE_AND_TIME);
    }

    /**
     * 参考 {@link #DatePickDialog(Context)}
     *
     * @param context
     * @param mode    指定dialog的显示模式，有 {@link #MODE_DATE_AND_TIME}显示日期和时间、{@link #MODE_DATE_ONLY}
     *                只显示日期、{@link #MODE_TIME_ONLY}只显示时间
     */
    public DatePickDialog(Context context, int mode) {
        this(context, R.style.ActionSheetDialogStyle, mode);
    }

    public DatePickDialog(Context context, int themeResId, int mode) {
        super(context, themeResId);
        mMode = mode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_date_pick);
        initLayoutParams();

        clDatePicker = findViewById(R.id.cl_select_date);
        mTvOnlyOneSwitch = findViewById(R.id.tv_only_one_switch);
        rgSwitchDateTime = findViewById(R.id.rg_switch_date_time);
        cbDateBtn = findViewById(R.id.cb_date_btn);
        cbTimeBtn = findViewById(R.id.cb_time_btn);
        mTvConfirm = findViewById(R.id.tv_confirm);
        mTvConfirm.setText(mDPLManager.titleEnsure());
        mFlContentPanel = findViewById(R.id.fl_content_panel);

        vInflater = new View(getContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(MATCH_PARENT, 0);
        mFlContentPanel.addView(vInflater, params);

        setListener();
        if (mMode != -1) {
            updateView();
        }
        hasInit = true;
    }

    private void initLayoutParams() {
        Window window = getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = MATCH_PARENT;
            lp.height = WRAP_CONTENT;
            window.setAttributes(lp);
            window.setWindowAnimations(R.style.animBottomMenu);
        }
    }

    /**
     * 改变Dialog的显示模式，并更新视图。这样做方便在同一个界面里面需要使用不同模式的dialog时，不用重新创建新的dialog对象。
     *
     * @param toBeMode
     */
    public void changeMode(int toBeMode) {
        checkInit();

        if (toBeMode == mMode) {
            return;
        }
        mMode = toBeMode;
        updateView();
    }

    private void updateView() {
        switch (mMode) {
            case MODE_DATE_AND_TIME:
                setTimePanel();
                setCalendarPanel();
                rgSwitchDateTime.setVisibility(View.VISIBLE);
                mTvOnlyOneSwitch.setVisibility(View.GONE);

                if (rgSwitchDateTime.getCheckedRadioButtonId() == cbDateBtn.getId()) {
                    mLlCalendarPicker.setVisibility(View.VISIBLE);
                    mTimePicker.setVisibility(View.GONE);
                } else {
                    cbDateBtn.setChecked(true);
                }
                if (TextUtils.isEmpty(cbTimeBtn.getText().toString())) {
                    cbTimeBtn.setText(mTimePicker.getSelectedHour() + ":" + mTimePicker.getSelectedMinute());
                }
                break;

            case MODE_DATE_ONLY:
                setCalendarPanel();
                rgSwitchDateTime.setVisibility(View.GONE);
                mTvOnlyOneSwitch.setVisibility(View.VISIBLE);
                mTvOnlyOneSwitch.setText(cbDateBtn.getText());
                clDatePicker.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.grey_date_picker));

                if (mTimePicker != null) {
                    mTimePicker.setVisibility(View.GONE);
                }
                mLlCalendarPicker.setVisibility(View.VISIBLE);
                break;

            case MODE_TIME_ONLY:
                setTimePanel();
                rgSwitchDateTime.setVisibility(View.GONE);
                mTvOnlyOneSwitch.setVisibility(View.VISIBLE);
                mTvOnlyOneSwitch.setText(cbTimeBtn.getText());
                clDatePicker.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));

                if (mLlCalendarPicker != null) {
                    mLlCalendarPicker.setVisibility(View.GONE);
                }
                mTimePicker.setVisibility(View.VISIBLE);
                if (TextUtils.isEmpty(cbTimeBtn.getText().toString())) {
                    mTvOnlyOneSwitch.setText(mTimePicker.getSelectedHour() + ":" + mTimePicker.getSelectedMinute());
                }
                break;
        }
    }

    private void initCalendarPanel() {
        mLlCalendarPicker = new LinearLayout(getContext());
        mLlCalendarPicker.setOrientation(LinearLayout.VERTICAL);

        mCalendarPicker = new CalendarPicker(getContext());
        mCalendarPicker.setBackgroundColor(Color.WHITE);
        mCalendarPicker.setOnDayClickListener(((clickDay, selectedDays) -> {
            if (TextUtils.isEmpty(clickDay)) {
                return;
            }

            selectedDateStr = clickDay;
            String[] dateDatas = clickDay.split("-");

            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            calendar.set(Calendar.YEAR, Integer.valueOf(dateDatas[0]));
            calendar.set(Calendar.MONTH, Integer.valueOf(dateDatas[1]) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(dateDatas[2]));
            cbDateBtn.setText(DPLManager.getInstance().getDateFormat().format(calendar.getTime()));
            mTvOnlyOneSwitch.setText(DPLManager.getInstance().getDateFormat().format(calendar.getTime()));

        }));

        mLlWeek = new LinearLayout(getContext());
        LinearLayout.LayoutParams lpWeek = new LinearLayout.LayoutParams(0, WRAP_CONTENT);
        lpWeek.weight = 1;
        for (int i = 0; i < mDPLManager.titleWeek().length; i++) {
            TextView tvWeek = new TextView(getContext());
            tvWeek.setText(mDPLManager.titleWeek()[i]);
            tvWeek.setGravity(CENTER);
            tvWeek.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            tvWeek.setTextColor(Color.parseColor("#283851"));
            tvWeek.setPadding(0, SizeUtils.dp2px(getContext(), 2), 0, SizeUtils.dp2px(getContext(), 2));
            mLlWeek.addView(tvWeek, lpWeek);
        }

    }

    private void setCalendarPanel() {
        if (mLlCalendarPicker == null) {
            initCalendarPanel();

            mLlCalendarPicker.addView(mLlWeek);
            mLlCalendarPicker.addView(mCalendarPicker);
            mFlContentPanel.addView(mLlCalendarPicker);
        }
    }

    private void initTimePanel() {
        mTimePicker = new TimePicker(getContext());
        mTimePicker.setGravity(CENTER);
        mTimePicker.setOnWheelListener(new TimePicker.OnWheelListener() {
            @Override
            public void onHourWheeled(int index, String hour) {
                String timeStr = cbTimeBtn.getText().toString();
                if (TextUtils.isEmpty(timeStr)) {
                    timeStr = "00:" + mTimePicker.getSelectedMinute();
                }
                selectedTimeStr = timeStr.replaceFirst("^\\w{2}(?=:)", hour);

                cbTimeBtn.setText(selectedTimeStr);
                mTvOnlyOneSwitch.setText(selectedTimeStr);
            }

            @Override
            public void onMinuteWheeled(int index, String minute) {
                String timeStr = cbTimeBtn.getText().toString();
                if (TextUtils.isEmpty(timeStr)) {
                    timeStr = mTimePicker.getSelectedHour() + ":00";
                }
                selectedTimeStr = timeStr.replaceFirst("(?<=:)\\w{2}$", minute);

                cbTimeBtn.setText(selectedTimeStr);
                mTvOnlyOneSwitch.setText(selectedTimeStr);
            }
        });
    }

    private void setTimePanel() {
        if (mTimePicker == null) {
            initTimePanel();
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(WRAP_CONTENT, SizeUtils.dp2px(getContext(), 200));
            params.gravity = CENTER;
            mFlContentPanel.addView(mTimePicker, params);
        }
    }

    private void setListener() {
        rgSwitchDateTime.setOnCheckedChangeListener((group, checkedId) -> {
            if (cbDateBtn.getId() == checkedId) {
                checkCbDateBtn(true);

            } else if (cbTimeBtn.getId() == checkedId) {
                checkCbDateBtn(false);

            }
        });

        mTvConfirm.setOnClickListener(clickView -> {
            if (selectedDateStr == null) {
                Toast.makeText(getContext(), "请选择日期", Toast.LENGTH_SHORT).show();
                return;
            }
            if (onDatePickListener != null) {
                onDatePickListener.onDatePick(selectedDateStr, selectedTimeStr);
            }
        });
    }

    /**
     * 修改时间选择器中分钟数的间隔，如gap=1时，数据为：0,1,2,3...59；gap为15时，数据为：0,15,30,45。
     *
     * @param gap 分钟数的间隔，小于1的整数都会被处理为1。
     */
    public void changeMinuteGap(int gap) {
        if (!hasInit) {
            return;
        }

        String str = mTimePicker.updateMinuteDateWithGap(gap <= 0 ? 1 : gap);
        String timeStr = cbTimeBtn.getText().toString();
        if (TextUtils.isEmpty(timeStr)) {
            timeStr = mTimePicker.getSelectedHour() + ":00";
        }
        selectedTimeStr = timeStr.replaceFirst("(?<=:)\\w{2}$", str);

        cbTimeBtn.setText(selectedTimeStr);
        mTvOnlyOneSwitch.setText(selectedTimeStr);
    }

    /**
     * 设置默认选中的日期，并且显示所在月的视图，类似于{@link #setSelectedDate(Date date)}
     *
     * @param selectedDateStr 字符串类型的日期，格式为"****-**-**"；为null时则采用组件内的默认值
     *                        ，月、日为一位数时前面可以不补0，如2019-7-1
     * @param selectedTimeStr 时间格式为"**:**"；为null时则采用组件内的默认值
     *                        ，数字为一位数时前面必须补0，如01:15
     */
    public void setSelectedDate(String selectedDateStr, String selectedTimeStr) {
        checkInit();

        boolean needUpdate = false;
        String formatSelectedDateStr = "";

        if (!TextUtils.isEmpty(selectedDateStr)) {
            try {
                String dateStrToParse;
                SimpleDateFormat dateFormat;
                if (!TextUtils.isEmpty(selectedTimeStr)) {
                    dateStrToParse = String.format("%s,%s", selectedDateStr, selectedTimeStr);
                    dateFormat = new SimpleDateFormat("yyyy-MM-dd,HH:mm", Locale.CHINA);
                } else {
                    dateStrToParse = selectedDateStr;
                    dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
                }
                formatSelectedDateStr = DPLManager.getInstance().getDateFormat().format(dateFormat.parse(dateStrToParse));
                this.selectedDateStr = selectedDateStr;
                needUpdate = true;
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(String.format("传入的日期\"%s\"格式有问题! ", selectedDateStr));
            }
        }

        if (!TextUtils.isEmpty(selectedTimeStr)) {
            this.selectedTimeStr = selectedTimeStr;
            needUpdate = true;
        }
        if (needUpdate) {
            updateValue(formatSelectedDateStr);
        }
    }

    /**
     * 设置默认选中的日期，并且显示所在月的视图
     *
     * @param date
     */
    public void setSelectedDate(Date date) {
        checkInit();

        if (date == null) {
            return;
        }

        this.selectedDateStr = new SimpleDateFormat("yyyy-M-d", Locale.CHINA).format(date);
        this.selectedTimeStr = new SimpleDateFormat("HH:mm", Locale.CHINA).format(date);
        updateValue(DPLManager.getInstance().getDateFormat().format(date));
    }

    private void updateValue(String formatSelectedDateStr) {
        cbDateBtn.setText(formatSelectedDateStr);
        cbTimeBtn.setText(selectedTimeStr);
        if (mMode == MODE_DATE_AND_TIME) {
            mCalendarPicker.setSelectedDay(selectedDateStr);
            mTimePicker.setSelectedTime(selectedTimeStr);

        } else if (mMode == MODE_DATE_ONLY) {
            mTvOnlyOneSwitch.setText(formatSelectedDateStr);
            mCalendarPicker.setSelectedDay(selectedDateStr);

        } else if (mMode == MODE_TIME_ONLY) {
            mTvOnlyOneSwitch.setText(selectedTimeStr);
            mTimePicker.setSelectedTime(selectedTimeStr);
        }
    }

    /**
     * 显示某个月的视图，不选中任一天，也不清除已选中的日期
     *
     * @param year  要显示的月份所在的年份
     * @param month 要显示的月份
     */
    public void showMonth(int year, int month) {
        checkInit();
        mCalendarPicker.setShowMonth(year, month);
    }

    private void checkCbDateBtn(boolean isChecked) {
        if (isChecked) {
            cbTimeBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.blue_date_picker));
            cbDateBtn.setTextColor(Color.WHITE);
            mLlCalendarPicker.setVisibility(View.VISIBLE);
            mTimePicker.setVisibility(View.GONE);

            clDatePicker.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.grey_date_picker));
            vInflater.getLayoutParams().height = 0;

        } else {
            cbTimeBtn.setTextColor(Color.WHITE);
            cbDateBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.blue_date_picker));
            mLlCalendarPicker.setVisibility(View.GONE);
            mTimePicker.setVisibility(View.VISIBLE);

            clDatePicker.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
            vInflater.getLayoutParams().height = mLlCalendarPicker.getHeight();
        }
    }

    public void setOnDatePickListener(OnDatePickListener onDatePickListener) {
        this.onDatePickListener = onDatePickListener;
    }

    public interface OnDatePickListener {
        /**
         * @param date the date selected is in format of "****-**-**".
         *             年月日格式如"2019-7-12", "2019-11-1"
         * @param time the time selected is in format of "**:**".
         *             时分格式如"20:15", "09:00"
         */
        void onDatePick(String date, String time);
    }

    private boolean checkInit() {
        if (!hasInit) throw new RuntimeException("you had never invoked the show()!");
        else return true;
    }
}
