package greyson.demo.datepicker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private DatePickDialog datePickDialog;
    private String selectedDate = "2019-06-02";
    private String selectedTime = "17:15";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        datePickDialog = new DatePickDialog(this);
        datePickDialog.setOnDatePickListener((dateStr, timeStr) -> {
            selectedDate = dateStr;
            selectedTime = timeStr;
            Toast.makeText(this, "你选择的时间：" + dateStr + " " + timeStr, Toast.LENGTH_SHORT).show();
        });
    }

    public void showAll(View view) {
        datePickDialog.show();
//        datePickDialog.setSelectedDate(selectedDate, selectedTime);
        datePickDialog.changeMode(DatePickDialog.MODE_DATE_AND_TIME);
        datePickDialog.setSelectedDate(new Date());
    }

    public void showTime(View view) {
        datePickDialog.show();
        datePickDialog.changeMode(DatePickDialog.MODE_TIME_ONLY);
    }

    public void showCalendar(View view) {
        datePickDialog.show();
        datePickDialog.changeMode(DatePickDialog.MODE_DATE_ONLY);
    }
}
