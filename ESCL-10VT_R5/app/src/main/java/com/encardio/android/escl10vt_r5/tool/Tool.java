package com.encardio.android.escl10vt_r5.tool;


import android.annotation.SuppressLint;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Tool {
    public static String getFileName() {

        int mYear;
        int mMonth;
        int mDay;

        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        String fileName = "";
        try {
            String year = String.valueOf(mYear);
            String date = (new StringBuilder()

                    .append(year.substring(2, 4)).append(pad(mMonth + 1))
                    .append(pad(mDay))).toString();

            fileName = Variable.loggerID + "_" + Variable.loggerSerialNumber + "_" + date
                    + "_info" + ".txt";
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
            fileName = "";
            return fileName;
        }
    }

    public static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + c;
    }

    public static String pad(String number, int digit) {
        while (number.length() < digit) {
            number = "0" + number;
        }
        return number;
    }

    public static int HHHMMSSTosecond(String time) {
        int hour;
        int min;
        int sec;

        String temp = time;
        String tempTime = "";
        tempTime = temp.substring(0, temp.indexOf(":"));
        hour = Integer.parseInt(tempTime);
        temp = temp.substring(temp.indexOf(":") + 1);
        tempTime = temp.substring(0, temp.indexOf(":"));
        min = Integer.parseInt(tempTime);
        tempTime = temp.substring(temp.indexOf(":") + 1);
        sec = Integer.parseInt(tempTime);
        sec = hour * 60 * 60 + (min * 60 + sec);
        return sec;
    }

    public static String getUTC_Offset() {
        String sign;
        long offset = Calendar.getInstance().getTimeZone().getRawOffset();
        if (offset < 0) {
            sign = "-";
            offset = offset * -1;
        } else {
            sign = "+";
        }
        offset = offset / 1000;
        int hour = (int) offset / 3600;
        offset = offset % 3600;
        int min = (int) offset / 60;
        return sign + pad(hour) + ":" + pad(min);
    }



    public static boolean isHeader(String data, int index, String inputFormat) {
        try {
            String[] tmp = data.split(",");
            DateFormat inputDateFormat = new SimpleDateFormat(inputFormat);
            Date date = inputDateFormat.parse(tmp[index]);
            new SimpleDateFormat(inputFormat).format(date);

            return false;
        } catch (Exception e) {
            return true;
        }
    }

    // mis-clicking prevention, using threshold of 2000 ms
    public static void controlButtonDebouncing(final Button btn) {
        btn.setClickable(false);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                btn.setClickable(true);
            }
        }, 2000);
    }

    public static void controlImageViewDebouncing(final ImageView iv) {
        iv.setClickable(false);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                iv.setClickable(true);
            }
        }, 2000);
    }

    static SimpleDateFormat input_sdf;
    static SimpleDateFormat output_sdf;
    static Date date;


    @SuppressLint("SimpleDateFormat")
    public static String convertDateTimeFormat(String inputFormat, String outputFormat, String date_time) {
        try {

            input_sdf = new SimpleDateFormat(inputFormat);
            output_sdf = new SimpleDateFormat(outputFormat);
            date = input_sdf.parse(date_time);
            date_time = output_sdf.format(date);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return date_time;
    }

}
