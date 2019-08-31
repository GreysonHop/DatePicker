package greyson.demo.datepicker;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by Greyson
 */
public class DatePickDialog extends Dialog {

    private Context activityContext;
    private DPLManager mDPLManager = DPLManager.getInstance();

    private ViewGroup clDatePicker;
    private RadioGroup rgSwitchDateTime;
    private RadioButton cbDateBtn;
    private RadioButton cbTimeBtn;
    private TextView mTvConfirm;

    private View vInflater;
    private LinearLayout mLlCalendarPicker;
    private LinearLayout mLlWeek;
    private CalendarPicker myCalendarPicker;
    private TimePicker myTimePicker;

    private String selectedDateStr;
    private String selectedTimeStr;

    private OnDatePickListener onDatePickListener;

    public DatePickDialog(Context context) {
        this(context, R.style.ActionSheetDialogStyle);
    }

    public DatePickDialog(Context context, int themeResId) {
        super(context, themeResId);
        activityContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_date_pick);
        initLayoutParams();

        clDatePicker = findViewById(R.id.cl_select_date);
        rgSwitchDateTime = findViewById(R.id.rg_switch_date_time);
        cbDateBtn = findViewById(R.id.cb_date_btn);
        cbTimeBtn = findViewById(R.id.cb_time_btn);
        mTvConfirm = findViewById(R.id.tv_confirm);
        mTvConfirm.setText(mDPLManager.titleEnsure());

        vInflater = findViewById(R.id.v_inflater);
        mLlCalendarPicker = findViewById(R.id.ll_calendar_picker);
        mLlWeek = findViewById(R.id.ll_week);
        myCalendarPicker = findViewById(R.id.myCalendarPicker);
        myTimePicker = findViewById(R.id.myTimePicker);

        LinearLayout.LayoutParams lpWeek = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        lpWeek.weight = 1;
        for (int i = 0; i < mDPLManager.titleWeek().length; i++) {
            TextView tvWeek = new TextView(getContext());
            tvWeek.setText(mDPLManager.titleWeek()[i]);
            tvWeek.setGravity(Gravity.CENTER);
            tvWeek.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            tvWeek.setTextColor(Color.parseColor("#283851"));
            mLlWeek.addView(tvWeek, lpWeek);
        }

        setListener();
        setSelectedDate(new Date());//default to select today
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

    private void setListener() {
        rgSwitchDateTime.setOnCheckedChangeListener((group, checkedId) -> {
            if (cbDateBtn.getId() == checkedId) {
                mLlWeek.setVisibility(View.VISIBLE);
//                clDatePicker.setBackgroundColor(getContext().getResources().getColor(R.color.grey_date_picker));
                clDatePicker.setBackgroundColor(Color.parseColor("#F5F9FC"));
                checkCbDateBtn(true);

                vInflater.getLayoutParams().height = 0;

            } else if (cbTimeBtn.getId() == checkedId) {
                mLlWeek.setVisibility(View.GONE);
                clDatePicker.setBackgroundColor(getContext().getResources().getColor(R.color.white));
                checkCbDateBtn(false);

                vInflater.getLayoutParams().height = mLlCalendarPicker.getHeight();
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

        myCalendarPicker.setOnDayClickListener(((clickDay, selectedDays) -> {
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
        }));

        myTimePicker.setOnWheelListener(new TimePicker.OnWheelListener() {
            @Override
            public void onHourWheeled(int index, String hour) {
                String timeStr = cbTimeBtn.getText().toString();
                selectedTimeStr = timeStr.replaceFirst("^\\w{2}(?=:)", hour);
                cbTimeBtn.setText(selectedTimeStr);
            }

            @Override
            public void onMinuteWheeled(int index, String minute) {
                String timeStr = cbTimeBtn.getText().toString();
                selectedTimeStr = timeStr.replaceFirst("(?<=:)\\w{2}$", minute);
                cbTimeBtn.setText(selectedTimeStr);
            }
        });
    }

    /**
     * 设置默认选中的日期，并且显示所在月的视图
     *
     * @param selectedDateStr 字符串类型的日期，格式为"****-**-**"；为null时则采用组件内的默认值
     *                        ，月、日为一位数时前面可以不补0，如2019-7-1
     * @param selectedTimeStr 时间格式为"**:**"；为null时则采用组件内的默认值
     *                        ，数字为一位数时前面必须补0，如01:15
     */
    public void setSelectedDate(String selectedDateStr, String selectedTimeStr) {
        if (selectedDateStr != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            Date selectDate;
            try {
                selectDate = dateFormat.parse(selectedDateStr);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(String.format("传入的日期\"%s\"格式有问题! ", selectedDateStr));
                return;
            }

//            cbDateBtn.setText(String.format(Locale.CHINA, "%tY年%tm月%td日", selectDate, selectDate, selectDate));
            cbDateBtn.setText(DPLManager.getInstance().getDateFormat().format(selectDate));
            this.selectedDateStr = selectedDateStr;
            myCalendarPicker.setSelectedDay(selectedDateStr);
        }

        if (selectedTimeStr != null) {
            cbTimeBtn.setText(selectedTimeStr);
            this.selectedTimeStr = selectedTimeStr;
            myTimePicker.setSelectedTime(selectedTimeStr);
        }
    }

    /**
     * 设置默认选中的日期，并且显示所在月的视图
     *
     * @param date
     */
    public void setSelectedDate(Date date) {
        if (date == null) {
            return;
        }
        String selectedDateStr = new SimpleDateFormat("yyyy-M-d", Locale.CHINA).format(date);
        cbDateBtn.setText(DPLManager.getInstance().getDateFormat().format(date));
        this.selectedDateStr = selectedDateStr;
        myCalendarPicker.setSelectedDay(selectedDateStr);

        String selectedTimeStr = new SimpleDateFormat("HH:mm", Locale.CHINA).format(date);
        cbTimeBtn.setText(selectedTimeStr);
        this.selectedTimeStr = selectedTimeStr;
        myTimePicker.setSelectedTime(selectedTimeStr);
    }

    /**
     * 显示某个月的视图，不选中任一天，也不清除已选中的日期
     *
     * @param year  要显示的月份所在的年份
     * @param month 要显示的月份
     */
    public void showMonth(int year, int month) {
        myCalendarPicker.setShowMonth(year, month);
    }

    private void checkCbDateBtn(boolean isChecked) {
        if (isChecked) {
//            cbTimeBtn.setTextColor(getContext().getResources().getColor(R.color.blue_date_picker));
            cbTimeBtn.setTextColor(Color.parseColor("#3E82FB"));
            cbDateBtn.setTextColor(Color.WHITE);
            myCalendarPicker.setVisibility(View.VISIBLE);
            myTimePicker.setVisibility(View.GONE);

        } else {
            cbTimeBtn.setTextColor(Color.WHITE);
//            cbDateBtn.setTextColor(getContext().getResources().getColor(R.color.blue_date_picker));
            cbDateBtn.setTextColor(Color.parseColor("#3E82FB"));
            myCalendarPicker.setVisibility(View.GONE);
            myTimePicker.setVisibility(View.VISIBLE);
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

}
