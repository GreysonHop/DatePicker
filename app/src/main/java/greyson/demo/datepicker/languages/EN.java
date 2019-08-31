package greyson.demo.datepicker.languages;

import java.util.Locale;

/**
 * 英文的默认实现类
 * 如果你想实现更多的语言请参考Language{@link DPLManager}
 * <p>
 * The implementation class of english.
 * You can refer to Language{@link DPLManager} if you want to define more language.
 *
 * @author AigeStudio 2015-03-28
 */
public class EN extends DPLManager {

    public EN(Locale locale) {
        this.mLocale = locale;
    }

    @Override
    public String[] titleMonth() {
        return new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    }

    @Override
    public String titleEnsure() {
        return "Ok";
    }

    @Override
    public String titleBC() {
        return "B.C.";
    }

    @Override
    public String[] titleWeek() {
        return new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    }

    @Override
    public String getDateFormatStr() {
        return "MMM d, yyyy";
    }

}
