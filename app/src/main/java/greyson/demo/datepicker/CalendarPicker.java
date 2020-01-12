package greyson.demo.datepicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import greyson.demo.datepicker.calendars.DPCManager;
import greyson.demo.datepicker.entities.DPInfo;
import greyson.demo.datepicker.languages.DPLManager;
import greyson.demo.datepicker.themes.DPTManager;
import greyson.demo.datepicker.utils.SizeUtils;

/**
 * Created by Greyson
 * 此类中选中的日期字符串格式中月份和日为单位数时前面没有0
 */
public class CalendarPicker extends View {

    private final Region[][] MONTH_WEEKS_4 = new Region[4][7];
    private final Region[][] MONTH_WEEKS_5 = new Region[5][7];
    private final Region[][] MONTH_WEEKS_6 = new Region[6][7];

    private final DPInfo[][] INFO_4 = new DPInfo[4][7];
    private final DPInfo[][] INFO_5 = new DPInfo[5][7];
    private final DPInfo[][] INFO_6 = new DPInfo[6][7];

    private DPCManager mCManager = DPCManager.getInstance();
    private DPTManager mTManager = DPTManager.getInstance();
    private DPLManager mDPLManager = DPLManager.getInstance();

    protected Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG |
            Paint.LINEAR_TEXT_FLAG);
    private Scroller mScroller;

    private List<String> mDateSelected = new ArrayList<>();

    private int mThisYear;//系统时间当前年份
    private int mCurrentYear, mCurrentMonth;//日历组件正中间显示的月份
    private int mNextYear, mNextMonth;//日历组件屏幕外下面显示的月份
    private int mPreviousYear, mPreviousMonth;//日历组件屏幕外上面显示的月份

    private float mCanAutoScrollGapY = 60;
    private float mCanSignScrollGapY = 8;

    public CalendarPicker(Context context) {
        this(context, null);
    }

    public CalendarPicker(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalendarPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mScroller = new Scroller(getContext());
        mPaint.setTextAlign(Paint.Align.CENTER);
        mCanAutoScrollGapY = SizeUtils.dp2px(getContext(), (int) mCanAutoScrollGapY);

        Calendar calendar = Calendar.getInstance();
        mThisYear = mCurrentYear = calendar.get(Calendar.YEAR);
        mCurrentMonth = calendar.get(Calendar.MONTH) + 1;
        computeDate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int column;
        if (mCurrentMonth <= 0 || mCurrentYear <= 0) {
            column = 4;
        } else {
            DPInfo[][] info = mCManager.obtainDPInfo(mCurrentYear, mCurrentMonth);
            if (TextUtils.isEmpty(info[4][0].strG)) {
                column = 4;
            } else if (TextUtils.isEmpty(info[5][0].strG)) {
                column = 5;
            } else {
                column = 6;
            }
        }

        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        int measuredHeight = (int) (measuredWidth * column / 7f);
        setMeasuredDimension(measuredWidth, measuredHeight);
        System.out.println("greyson MyCalendarPicker onMeasure() measuredHeight=" + measuredHeight
                + ", column = " + column + " , cYear = " + mCurrentYear + ", cMonth= " + mCurrentMonth);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        System.out.println("greyson MyCalendarPicker onSizeChanged()");
        if (mCurrentMonth <= 0 || mCurrentYear <= 0) {
            return;
        }

        int cellWidth = w / 7;


        for (int i = 0; i < MONTH_WEEKS_4.length; i++) {
            for (int j = 0; j < MONTH_WEEKS_4[i].length; j++) {
                Region region = new Region();
                region.set((j * cellWidth), (i * cellWidth), cellWidth + (j * cellWidth),
                        cellWidth + (i * cellWidth));
                MONTH_WEEKS_4[i][j] = region;
            }
        }
        for (int i = 0; i < MONTH_WEEKS_5.length; i++) {
            for (int j = 0; j < MONTH_WEEKS_5[i].length; j++) {
                Region region = new Region();
                region.set((j * cellWidth), (i * cellWidth), cellWidth + (j * cellWidth),
                        cellWidth + (i * cellWidth));
                MONTH_WEEKS_5[i][j] = region;
            }
        }
        for (int i = 0; i < MONTH_WEEKS_6.length; i++) {
            for (int j = 0; j < MONTH_WEEKS_6[i].length; j++) {
                Region region = new Region();
                region.set((j * cellWidth), (i * cellWidth), cellWidth + (j * cellWidth),
                        cellWidth + (i * cellWidth));
                MONTH_WEEKS_6[i][j] = region;
            }
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        } else {
            requestLayout();
        }
    }

    private float mFirstTouchY;
    private float mLastTouchY;
    private int mTotalScrollY;//标志总共纵向滑动多少距离
    private int mLastTotalScrollY;//标志上一次up事件之后的总共纵向滑动多少距离
    private boolean isScrolling;
//   private  int verticalIndex;//标志纵向滑动几次

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        System.out.println("greyson MyCalendarPicker onTouchEvent() action = " + event.getAction() + " , cYear= " + mCurrentYear + " , cMonth=" + mCurrentMonth);
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mFirstTouchY = y;
                break;

            case MotionEvent.ACTION_MOVE:
//                mTotalScrollY += y - mLastTouchY;

                if (isScrolling) {
                    mTotalScrollY = mLastTotalScrollY + (int) (mFirstTouchY - y);
                    smoothScrollTo(0, mTotalScrollY);
                } else if (Math.abs(y - mFirstTouchY) > mCanSignScrollGapY) {
                    isScrolling = true;
                }
                break;

            case MotionEvent.ACTION_UP:
                if (!isScrolling) {
                    dealClickEvent((int) event.getX(), (int) event.getY());
                    break;
                }
                if (y - mFirstTouchY > mCanAutoScrollGapY) {//auto slide down
                    if (mCurrentMonth == 1) {
                        mCurrentYear--;
                        mCurrentMonth = 12;
                    } else {
                        mCurrentMonth--;
                    }
                    computeDate();
                    mTotalScrollY = mLastTotalScrollY - getHeight();

                } else if (mFirstTouchY - y > mCanAutoScrollGapY) {//auto slide up
                    if (mCurrentMonth == 12) {
                        mCurrentYear++;
                        mCurrentMonth = 1;
                    } else {
                        mCurrentMonth++;
                    }
                    computeDate();
                    mTotalScrollY = mLastTotalScrollY + getHeight();
                } else {
                    /* 没有切页时不会顺滑地滚回原来的位置的bug，是因为没加下面这句代码，会导致后面的smoothScrollTo方法
                    要滚向的目标位置不是原位置mLastTotalScrollY，而是手指起来时的位置，而这种情况下后面就会：因为还是
                    当前月份，并且滚动的目标位置不变，所以直接刷新View和数据，也就形成了直接“跳回”原来月份视图、没有慢慢
                    滚动的动画的现象 */
                    mTotalScrollY = mLastTotalScrollY;
                }
                smoothScrollTo(0, mTotalScrollY);
                mLastTotalScrollY = mTotalScrollY;
                isScrolling = false;
                break;
        }
        mLastTouchY = y;
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        System.out.println("greyson MyCalendarPicker onDraw()");
        if (mCurrentMonth <= 0 || mCurrentYear <= 0) {
            return;
        }

        drawMonthData(canvas, 0, mLastTotalScrollY - getMeasuredHeight(), mPreviousYear, mPreviousMonth);
        drawMonthData(canvas, 0, mLastTotalScrollY, mCurrentYear, mCurrentMonth);
        drawMonthData(canvas, 0, mLastTotalScrollY + getMeasuredHeight(), mNextYear, mNextMonth);
    }

    /**
     * 画某年某月的日历视图
     *
     * @param canvas
     * @param x
     * @param y
     * @param year
     * @param month
     */
    private void drawMonthData(Canvas canvas, int x, int y, int year, int month) {
        canvas.save();
//        canvas.translate(x, y);
        int yOffset;
        int myHeight;
        DPInfo[][] info = mCManager.obtainDPInfo(year, month);
        DPInfo[][] result;
        Region[][] tmp;
        if (TextUtils.isEmpty(info[4][0].strG)) {
            tmp = MONTH_WEEKS_4;
            arrayClear(INFO_4);
            result = arrayCopy(info, INFO_4);
            myHeight = MONTH_WEEKS_4[0][0].getBounds().height() * 4;
        } else if (TextUtils.isEmpty(info[5][0].strG)) {
            tmp = MONTH_WEEKS_5;
            arrayClear(INFO_5);
            result = arrayCopy(info, INFO_5);
            myHeight = MONTH_WEEKS_5[0][0].getBounds().height() * 5;
        } else {
            tmp = MONTH_WEEKS_6;
            arrayClear(INFO_6);
            result = arrayCopy(info, INFO_6);
            myHeight = MONTH_WEEKS_6[0][0].getBounds().height() * 6;
        }

        if (year > mCurrentYear) {
            yOffset = mLastTotalScrollY + getMeasuredHeight();
        } else if (year < mCurrentYear) {
            yOffset = mLastTotalScrollY - myHeight;
        } else if (month > mCurrentMonth) {
            yOffset = mLastTotalScrollY + getMeasuredHeight();
        } else if (month < mCurrentMonth) {
            yOffset = mLastTotalScrollY - myHeight;
        } else {
            yOffset = mLastTotalScrollY;
        }

        canvas.translate(0, yOffset);

        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length; j++) {
                drawMonthData(canvas, tmp[i][j].getBounds(), info[i][j], year, month);
            }
        }
        drawFrame(canvas, tmp[0][0].getBounds(), tmp.length);
        canvas.restore();
    }

    private void drawMonthData(Canvas canvas, Rect rect, DPInfo info, int year, int month) {
//        drawBG(canvas, rect, info);
        drawDayText(canvas, rect, info, year, month);
//        if (isFestivalDisplay) drawFestival(canvas, rect, info.strF, info.isFestival);
//        drawDecor(canvas, rect, info);
    }

    private void drawDayText(Canvas canvas, Rect rect, DPInfo dpInfo, int year, int month) {
        String strDay = dpInfo.strG;
        boolean isMonthFirstDay = TextUtils.equals("1", strDay);
        boolean isToday = dpInfo.isToday;
        boolean isWeekend = dpInfo.isWeekend;
        boolean isSelectedDay = isSelectedDay(year, month, strDay);

        //画背景
        if (isSelectedDay) {
            mPaint.setColor(Color.parseColor("#3E82FB"));
            canvas.drawRect(rect, mPaint);
        } else if (isToday) {
            mPaint.setColor(Color.parseColor("#AAC8FF"));
            canvas.drawRect(rect, mPaint);
        }

        float y;
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        if (isToday) {
            mPaint.setColor(isSelectedDay ? Color.WHITE : isWeekend ? mTManager.colorWeekend() : mTManager.colorG());
            mPaint.setTextSize(SizeUtils.dp2px(getContext(), 14));
            if (DPLManager.getInstance().isSameLanguage(Locale.CHINA)) {//temporary deal
                canvas.drawText("今天", rect.centerX(), rect.centerY() - fontMetrics.bottom, mPaint);
            } else {
                canvas.drawText("Today", rect.centerX(), rect.centerY() - fontMetrics.bottom, mPaint);
            }

            y = rect.centerY() - fontMetrics.ascent;
        } else {
            y = rect.centerY() - fontMetrics.top / 2 - fontMetrics.bottom / 2;
        }

//        if (!isFestivalDisplay)
//            y = rect.centerY() + Math.abs(mPaint.ascent()) - (mPaint.descent() - mPaint.ascent()) / 2F;

        if (isMonthFirstDay) {
            String monthFirstDay = mDPLManager.titleMonth()[month - 1];
            mPaint.setColor(isSelectedDay ? Color.WHITE : Color.parseColor("#3E82FB"));
            if (isToday) {
                mPaint.setTextSize(SizeUtils.dp2px(getContext(), 14));
                canvas.drawText(monthFirstDay, rect.centerX(), y, mPaint);

            } else if (mThisYear != year) {

                mPaint.setColor(isSelectedDay ? Color.WHITE : Color.parseColor("#3E82FB"));

                mPaint.setTextSize(SizeUtils.dp2px(getContext(), 14));
                canvas.drawText(monthFirstDay, rect.centerX(), rect.centerY() - fontMetrics.bottom, mPaint);
                mPaint.setTextSize(SizeUtils.dp2px(getContext(), 12));
                y = rect.centerY() - fontMetrics.ascent;
                canvas.drawText(String.valueOf(year), rect.centerX(), y, mPaint);

            } else {
                String monthNumber;
                String monthSign;
                if (DPLManager.getInstance().isSameLanguage(Locale.CHINA)) {
                    monthNumber = monthFirstDay.substring(0, monthFirstDay.length() - 1);
                    monthSign = monthFirstDay.substring(monthFirstDay.length() - 1);

                } else {
                    monthNumber = monthFirstDay.substring(0, 1);
                    monthSign = monthFirstDay.substring(1);
                }

                //测量“十一月”中“十一”字，或者“Jul”中“J”字的大小
                Rect monthNumberRect = new Rect();
                mPaint.setTextSize(SizeUtils.dp2px(getContext(), 18));
                mPaint.getTextBounds(monthNumber, 0, monthNumber.length(), monthNumberRect);

                //测量“月”字，或“ul”字的大小
                Rect monthSignRect = new Rect();
                mPaint.setTextSize(SizeUtils.dp2px(getContext(), 10));
                mPaint.getTextBounds(monthSign, 0, monthSign.length(), monthSignRect);

                //计算两种字体的位置
                int monthNumberX = rect.centerX() - (monthSignRect.width() / 2);
                int monthSignX = monthNumberRect.width() / 2 + rect.centerX();

                canvas.drawText(monthSign, monthSignX, y, mPaint);

                mPaint.setTextSize(SizeUtils.dp2px(getContext(), 18));
                canvas.drawText(monthNumber, monthNumberX, y, mPaint);
            }
        } else {
            mPaint.setTextSize(SizeUtils.dp2px(getContext(), 14));
            mPaint.setColor(isSelectedDay ? Color.WHITE : isWeekend ? mTManager.colorWeekend() : mTManager.colorG());
            canvas.drawText(strDay, rect.centerX(), y, mPaint);
        }
    }

    /**
     * 设置选中的年月日，并且显示所在月的视图
     *
     * @param dateStr
     */
    public void setSelectedDay(String dateStr) {
        if (dateStr == null) {
            return;
        }

        mDateSelected.clear();
        mDateSelected.add(dateStr.replaceAll("(?<=\\d-)0", ""));

        String[] dates = dateStr.split("-");
        setShowMonth(Integer.valueOf(dates[0]), Integer.valueOf(dates[1]));
    }

    private boolean isSelectedDay(int year, int month, String day) {
        String date = String.format("%d-%d-%s", year, month, day);
        for (String s : mDateSelected) {
            if (TextUtils.equals(s, date)) return true;
        }
        return false;
    }

    //绘制条框
    private void drawFrame(Canvas canvas, Rect cellRect, int columnNumber) {
        mPaint.setColor(mTManager.colorGridLine());
        int cellWith = cellRect.width();
        int lineLength;
        canvas.drawLine(0, cellWith, cellWith * 7, cellWith, mPaint);
        canvas.drawLine(0, cellWith * 2, cellWith * 7, cellWith * 2, mPaint);
        canvas.drawLine(0, cellWith * 3, cellWith * 7, cellWith * 3, mPaint);
        lineLength = cellWith * 4;
        if (columnNumber > 4) {
            canvas.drawLine(0, cellWith * 4, cellWith * 7, cellWith * 4, mPaint);
            lineLength = cellWith * 5;
        }
        if (columnNumber > 5) {
            canvas.drawLine(0, cellWith * 5, cellWith * 7, cellWith * 5, mPaint);
            lineLength = cellWith * 6;
        }
        canvas.drawLine(cellWith, 0, cellWith, lineLength, mPaint);
        canvas.drawLine(cellWith * 2, 0, cellWith * 2, lineLength, mPaint);
        canvas.drawLine(cellWith * 3, 0, cellWith * 3, lineLength, mPaint);
        canvas.drawLine(cellWith * 4, 0, cellWith * 4, lineLength, mPaint);
        canvas.drawLine(cellWith * 5, 0, cellWith * 5, lineLength, mPaint);
        canvas.drawLine(cellWith * 6, 0, cellWith * 6, lineLength, mPaint);
    }

    private void arrayClear(DPInfo[][] info) {
        for (DPInfo[] anInfo : info) {
            Arrays.fill(anInfo, null);
        }
    }

    private DPInfo[][] arrayCopy(DPInfo[][] src, DPInfo[][] dst) {
        for (int i = 0; i < dst.length; i++) {
            System.arraycopy(src[i], 0, dst[i], 0, dst[i].length);
        }
        return dst;
    }

    private void smoothScrollTo(int fx, int fy) {
        int dx = fx - mScroller.getFinalX();
        int dy = fy - mScroller.getFinalY();
        smoothScrollBy(dx, dy);
    }

    private void smoothScrollBy(int dx, int dy) {
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx, dy, 500);
        invalidate();
    }

    private void dealClickEvent(int x, int y) {
        DPInfo[][] info = mCManager.obtainDPInfo(mCurrentYear, mCurrentMonth);
        Region[][] tmp;
        if (TextUtils.isEmpty(info[4][0].strG)) {
            tmp = MONTH_WEEKS_4;
        } else if (TextUtils.isEmpty(info[5][0].strG)) {
            tmp = MONTH_WEEKS_5;
        } else {
            tmp = MONTH_WEEKS_6;
        }
        for (int i = 0; i < tmp.length; i++) {
            for (int j = 0; j < tmp[i].length; j++) {
                Region region = tmp[i][j];
                String currentDay;
                if (TextUtils.isEmpty(currentDay = info[i][j].strG)) {
                    continue;
                }

                if (!region.contains(x, y)) {
                    continue;
                }

                final String date = mCurrentYear + "-" + mCurrentMonth + "-" + currentDay;
                /*if (mDateSelected.contains(date)) {
                    mDateSelected.remove(date);
                } else {
                    mDateSelected.add(date);
                }*///屏蔽多选逻辑
                mDateSelected.clear();
                mDateSelected.add(date);
                if (mOnDayClickListener != null) {
                    mOnDayClickListener.onDayClickListener(date, mDateSelected);
                }
                invalidate();
//                Toast.makeText(getContext(), "you click: " + mCurrentYear + "年" + mCurrentMonth + "月" + currentDay + "日", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 显示某年某月的视图
     *
     * @param year  要显示的视图所在的年份
     * @param month 要显示的视图所在的月份
     */
    void setShowMonth(int year, int month) {
        mCurrentYear = year;
        mCurrentMonth = month;
        mTotalScrollY = 0;
        mLastTotalScrollY = 0;

        if (mScroller != null) {
            mScroller.setFinalX(0);
            mScroller.setFinalY(0);
        }
//        buildRegion();
        computeDate();
        requestLayout();
        invalidate();
    }

    /**
     * 设置新的当前年月后，计算上一个月或下一个月的所在年月值
     */
    private void computeDate() {
        if (mCurrentMonth == 12) {
            mNextYear = mCurrentYear + 1;
            mNextMonth = 1;
        } else {
            mNextYear = mCurrentYear;
            mNextMonth = mCurrentMonth + 1;
        }

        if (mCurrentMonth == 1) {
            mPreviousYear = mCurrentYear - 1;
            mPreviousMonth = 12;
        } else {
            mPreviousYear = mCurrentYear;
            mPreviousMonth = mCurrentMonth - 1;
        }

        /*if (null != onDateChangeListener) {
            onDateChangeListener.onYearChange(centerYear);
            onDateChangeListener.onMonthChange(centerMonth);
        }*/
    }

    interface OnDayClickListener {
        /**
         * 格式都为“****-**-**”
         *
         * @param clickDay     点击的日期
         * @param selectedDays 所有已经选择的日期，月和日前面不补0，如2019-7-1
         */
        void onDayClickListener(String clickDay, List<String> selectedDays);
    }

    private OnDayClickListener mOnDayClickListener;

    public void setOnDayClickListener(OnDayClickListener onDayClickListener) {
        this.mOnDayClickListener = onDayClickListener;
    }

    public OnDayClickListener getOnDayClickListener() {
        return this.mOnDayClickListener;
    }
}
