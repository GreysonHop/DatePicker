package greyson.demo.datepicker.utils;

import android.content.Context;
import android.util.TypedValue;

/**
 * Create by Greyson on 2019/10/19
 */
public class SizeUtils {
    public static int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp
                , context.getResources().getDisplayMetrics());
    }

    public static int sp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dp
                , context.getResources().getDisplayMetrics());
    }
}
