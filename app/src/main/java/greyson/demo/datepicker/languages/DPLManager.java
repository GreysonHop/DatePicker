package greyson.demo.datepicker.languages;

import android.text.TextUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * 语言对象抽象父类
 * DatePicker暂且支持中文和英文两种显示语言
 * 如果你需要定义更多的语言可以新建自己的语言类并继承Language重写其方法即可
 * 同时你需要在Language的单例方法{@link #getInstance()}的分支语句中添加自己的语言类判断
 * <p>
 * The abstract of language.
 * The current language only two support chinese and english in DatePicker.
 * If you need more language you want,you can define your own language class and extends Language
 * override all method.
 * Also you must add a judge of your language in branching statement of single case method{@link #getInstance()}
 *
 * @author AigeStudio 2015-03-26
 */
public abstract class DPLManager {
    private static DPLManager sLanguage;
    private SimpleDateFormat mSimpleDateFormat;
    protected Locale mLocale;

    /**
     * 获取日历语言管理器
     * <p>
     * Get DatePicker language manager
     *
     * @return 日历语言管理器 DatePicker language manager
     */
    public static DPLManager getInstance() {
        if (null == sLanguage) {
            Locale locale = Locale.getDefault();
            String language = locale.getLanguage().toLowerCase();
            if (language.equals("zh")) {
                sLanguage = new CN(locale);
            } else {
                sLanguage = new EN(locale);
            }
        }
        return sLanguage;
    }

    /**
     * 月份标题显示
     * <p>
     * Titles of month
     *
     * @return 长度为12的月份标题数组 Array in 12 length of month titles
     */
    public abstract String[] titleMonth();

    /**
     * 确定按钮文本
     * <p>
     * Text of ensure button
     *
     * @return Text of ensure button
     */
    public abstract String titleEnsure();

    /**
     * 公元前文本
     * <p>
     * Text of B.C.
     *
     * @return Text of B.C.
     */
    public abstract String titleBC();

    /**
     * 星期标题显示
     * <p>
     * Titles of week
     *
     * @return 长度为7的星期标题数组 Array in 7 length of week titles
     */
    public abstract String[] titleWeek();

    /**
     * 获取日历的显示格式，如“2017-09-01”，“Jul 2, 2017”
     *
     * @return
     */
    public abstract String getDateFormatStr();

    /**
     * 获取日历的显示格式，如“2017-09-01”，“Jul 2, 2017”
     *
     * @return
     */
    public DateFormat getDateFormat() {
        if (mSimpleDateFormat == null) {
            mSimpleDateFormat = new SimpleDateFormat(getDateFormatStr(), mLocale);
        }
        return mSimpleDateFormat;
    }

    public boolean isSameLanguage(Locale locale) {
        return TextUtils.equals(locale.getDisplayLanguage(), mLocale.getDisplayLanguage());
    }

    public void setLocale(Locale locale) {
        mLocale = locale;
        mSimpleDateFormat = new SimpleDateFormat(getDateFormatStr(), locale);
    }
}
