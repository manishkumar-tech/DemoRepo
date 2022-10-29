package com.encardio.android.escl10vt_r5.service;

/**
 * @author Sandeep
 */
public class TimeSort implements Comparable<Object> {

    private String myTime;
    private String timeRgex = ":";
    private String hour;
    private String minute;
    private String timeFormat;

    public TimeSort(String time) {
        this.myTime = time;
        setTimeValues();
    }

    /**
     * @return the myTime
     */
    public String getMyTime() {

        return myTime;
    }

    /**
     * @param myTime the myTime to set
     */
    public void setMyTime(String myTime) {

        this.myTime = myTime;
    }

    /**
     * @return the timeRgex
     */
    public String getTimeRgex() {

        return timeRgex;
    }

    /**
     * @param timeRgex the timeRgex to set
     */
    public void setTimeRgex(String timeRgex) {

        this.timeRgex = timeRgex;
    }

    /**
     * @return the hour
     */
    public String getHour() {

        return hour;
    }

    /**
     * @param hour the hour to set
     */
    public void setHour(String hour) {

        this.hour = hour;
    }

    /**
     * @return the minute
     */
    public String getMinute() {

        return minute;
    }

    /**
     * @param minute the minute to set
     */
    public void setMinute(String minute) {

        this.minute = minute;
    }

    /**
     * @return the timeFormat
     */
    public String getTimeFormat() {

        return timeFormat;
    }

    /**
     * @param timeFormat the timeFormat to set
     */
    public void setTimeFormat(String timeFormat) {

        this.timeFormat = timeFormat;
    }

    public void setTimeValues() {

        String[] timeSplit = this.myTime.split(this.timeRgex);
        setMinute(timeSplit[1]);
        setHour(timeSplit[0]);
    }

    public int compare(String objInteger, String cmpareInteger) {

        if (new Integer(objInteger) < new Integer(cmpareInteger)) {
            // /* instance lesser than seconds received */
            return -1;
        } else if (new Integer(objInteger) > new Integer(cmpareInteger)) {
            // /* instance greater than received */
            return 1;
        }
        /* instance == received */
        return 0;
    }

    public int compareTo(Object obj) {

        TimeSort tmp = (TimeSort) obj;
        if (compare(this.hour, tmp.hour) == 0) {

            if (compare(this.minute, tmp.minute) == -1) {
                return -1;
            } else if (compare(this.minute, tmp.minute) == 1) {
                return 1;
            } else {

                return 0;

            }
        } else if (compare(this.hour, tmp.hour) == -1) {
            return -1;
        } else {
            // /* instance greater than received */
            return 1;
        }
    }

    public String toString() {
        return myTime;
    }
}

