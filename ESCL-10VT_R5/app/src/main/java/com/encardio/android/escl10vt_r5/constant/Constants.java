package com.encardio.android.escl10vt_r5.constant;

import android.app.Application;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.encardio.android.escl10vt_r5.bluetooth.BluetoothService;
import com.encardio.android.escl10vt_r5.tool.Tool;
import com.encardio.android.escl10vt_r5.tool.Variable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Formatter;
import java.util.Locale;

/**
 * @author Sandeep
 */

public class Constants extends Application {

    public static final String DEFAULT_TEXT = "-----";
    public static final int SHOW_MSG = 1;
    public static final int SHOW_PARAMETER = 2;
    public static final String SHOW_MSG_KEY = "SHOW_MSG";
    public static final int INVALID_COMMAND_ERROR = 1;
    public static final int STRING_FORMAT_ERROR = 2;
    public static final int DATA_SIZE_ERROR = 3;
    public static final int COMMAND_PACKET_SIZE_ERROR = 4;
    public static final int DATA_NULL_ERROR = 5;
    public static final int RTC_DATE_TIME_ERROR = 6;
    public static final int INVALID_DATA_ERROR = 7;
    public static final int MODEM_SIGNAL_ERROR = 8;
    public static final int MODEM_BAUDRATE_SET_ERROR = 9;
    public static final int LOG_INTERVAL_UNDERFLOW_ERROR = 10;
    public static final int DATA_OUT_OF_RANGE_ERROR = 11;
    public static final int DATA_RECORD_CORRUPTED_ERROR = 12;
    public static final int MODEM_TRAP_MODE_ERROR = 13;
    public static final int MODEM_POWER_ON_ERROR = 14;
    public static final int MODEM_CME_MSG_FORMAT_ERROR = 50;
    public static final int NETWORK_FORMAT_ERROR = 51;
    public static final int MODEM_FLOW_CONTROL_ERROR = 52;
    public static final int MODEM_CHAR_SET_ERROR = 53;
    public static final int MODEM_MSG_SERVICE_ERROR = 54;
    public static final int SMS_OVER_FLOW_ERROR = 55;
    public static final int SMS_STORAGE_ERROR = 56;
    public static final int SMS_MSG_FORMAT_ERROR = 57;
    public static final int SMS_TEXT_PARA_ERROR = 58;
    public static final int SAVE_SETTINGS_ERROR = 59;
    public static final int OK_STATUS = 1000;
    public static final int MODEM_DISABLE_STATUS = 1001;
    public static final int MODEM_SIM_UNAVAILABLE_STATUS = 1002;
    public static final int MODEM_OPERATING_MODE_OFF_STATUS = 1003;
    public static final int BAROMETER_DISABLE_STATUS = 1004;
    public static final int BATTERY_DEAD_STATUS = 1005;
    public static final int MODEM_COMM_MODE_GPRS_STATUS = 1006;
    public static final int MODEM_OPERATING_MODE_ON_STATUS = 1007;
    public static final int RECORD_AVAILABLE_STATUS = 1010;
    public static final int RECORD_DOWNLOADING_COMPLETED_STATUS = 1011;
    public static final int RECORD_UNAVAILABLE_STATUS = 1012;
    public static final int FTP_PARAMETER_NOT_SET = 15;
    public static final int UNOPEN_FTP_SOCKET = 16;
    public static final int FTP_DATA_FAIL = 17;
    public static final String PREF_PHON_NAME = "PHON_PREFS";
    public static final String PREF_ADMIN_NAME = "admin_contact";
    public static final String EXTRA_DEVICE_ADDRESS = "device_address";
    private static final String temp = "";
    private static final char[] xh = null;
    public static float[] graph_data;
    public static float graph_max;
    public static float graph_min;
    public static boolean isFrequency = false;
    public static boolean connectionBreak = false;
    public static int countRecords;
    public static int tempcount;
    public static int progress = 0;
    public static String tempDownloadedData = "";
    public static Integer tag = 0;
    public static Integer tag1 = 0;
    public static Double maxTemp;
    public static Double minTemp;
    public static Double maxCorrPara;
    public static Double minCorrPara;
    public static Double maxUnCorrPara;
    public static Double minUnCorrPara;
    public static Double maxBaro;
    public static Double minBaro;
    public static Double maxVolt;
    public static Double minVolt;
    public static String minDate;
    public static String Date_min;
    public static String Date_max;
    public static String maxDate;
    public static boolean isGetDataRecieved = false;
    public static BluetoothService bluetoothService;
    public static String BLUETOOTH_ADDRESS = "";
    public static String BLUETOOTH_DEVICE = "";
    public static boolean isNewBluetoothConnection = false; // to check
    public static String BLUETOOTH_ADDRESS_FROM_LIST;
    public static int monitorInterval = 2;
    public static String endWithNewLine = "\r";
    public static String checkEndChar = endWithNewLine;
    public static Object lock = new Object();
    public static Object lockForMonitorInterval = new Object();
    public static boolean exception = false;
    public static boolean toastFromThread;
    public static long modemTurnOFFTimer = 0;
    public static String batteryInstallationDate;
    public static String url = "xxx.xxx.xxx.xxx";
    public static String userName = "";
    public static String password = "";
    public static int port = 0;
    public static String[] selectedFilesToUpload;
    // upload
    public static boolean isBTenabledByApp = false;
    public static boolean expInBluetoothConnection;
    /**
     * Default decimal places
     */
    public static int decimalPlaces = 4;
    /**
     * Default offset
     */
    public static double offset = 0;
    /**
     * The key unit for keys in intent.
     */
    public static String KEY_UNIT = "unit";
    /**
     * The key for decimal digits
     */
    public static String KEY_DECIMAL_DIGITS = "decimal_digit";
    /**
     * The key for offset
     */
    public static String KEY_OFFSET = "offset";
    /**
     * The interval Change key indicator
     */
    public static String KEY_INTERVAL = "monitor_interval";
    // public static boolean downloadCompleted;
    public static String reply = "";
    public static boolean gotReply = false;
    public static int setupInterval = 2;
    public static boolean headerstat = false;
    public static String head_para = "Parameter";
    public static int sc = -1;
    public static String downloadData = "";
    public static int responseType;
    public static String responseMsg = "";
    public static boolean downloadingDataCommand = false;
    public static boolean dataDownloadAvailble = false;
    public static boolean dataDownloadCompleted = false;
    public static boolean dataDownloadUnavailble = false;
    public static long download_Watchdog_Timer = 0;
    public static boolean mdmPwr = false;
    public static boolean mdmstatus = false;
    public static String file_Text;
    public static int graph_length;
    public static String[] graphTemperature;
    public static String[] graphCorrectedWaterLevel;
    public static String[] graphUnCorrectedWaterLevel;
    public static String[] graphDateTime;
    public static String[] graphBatteryVoltage;
    public static String[] graphBarometer;
    public static boolean battery_low = false;
    public static String[] graph_date_time;
    public static String[] graph_battery_voltage;
    public static String[] graph_frequency;
    public static String[] graph_parameter;
    public static String[] graph_temperature;
    private static File sdCard;
    private static File dir;
    private static int mYear;
    /**
     * The month.
     */
    private static int mMonth;
    /**
     * The day.
     */
    private static int mDay;
    private static char temp1;
    Boolean flag_wakeup = false;
    private String fileName;

    public static void delay(long time) {
        long curmillies = System.currentTimeMillis();
        while ((System.currentTimeMillis() - curmillies) < time) {
        }
    }

    /**
     * wait 30 seconds for reply of command.
     */
    public static void waitForReply() {
        long currentTimemill = System.currentTimeMillis();
        while (gotReply == false) {
            if (System.currentTimeMillis() - currentTimemill >= 5000) {
                // if (toastFromThread == false) {

                Constants.toastFromThread = true;
                Constants.connectionBreak = true;
                break;
            }
        }
    }

    public static void waitForReplyforMonPara() {
        long currentTimemill = System.currentTimeMillis();
        while (gotReply == false) {
            if (System.currentTimeMillis() - currentTimemill >= 70000) {

                Constants.toastFromThread = true;
                Constants.connectionBreak = true;
                break;
            }

        }
    }

    /**
     * To remove exponential expression in floating number like: 2.98 E-4 ==
     * 0.000298
     *
     * @param displacement is exponential expression
     * @param digits       are number of display digits
     * @return floating number
     */
    public static String adjustDesimalDigits(float displacement, int digits) {
        String formateddecimalDigits = null;
        try {
            digits = digits - 1; // one digit reserve for integer 0.00000
            // like;// digit = 6
            Formatter fmt = new Formatter();
            formateddecimalDigits = fmt.format(Locale.US, "%." + digits + "f",
                    displacement).toString();
            if (displacement >= 0) {
                formateddecimalDigits = " " + formateddecimalDigits; // if not
                // '-'
            }
        } catch (Exception e) {
        }
        return formateddecimalDigits;
    }

    /**
     * @param paraValue_para1 format the decimal values. call in view data screen
     * @return String
     */
    public static String setDecimalDigits(String paraValue_para1) {
        try {


            float paraValue = Float.parseFloat(paraValue_para1);
            if (0.0000f == paraValue) // xxxx.xxxx
            {
                paraValue_para1 = String.format(Locale.US, "%1." + decimalPlaces + "f",
                        paraValue);

            } else if ((0.0001f <= paraValue) && (paraValue <= 10000.0f)) // xxxx.xxxx
            {
                paraValue_para1 = String.format(Locale.US, "%1." + decimalPlaces + "f",
                        paraValue);

            } else {
                if ((-0.0001f >= paraValue) && (paraValue >= -10000.0f)) // xxxx.xxxx
                {
                    paraValue_para1 = String.format(Locale.US,
                            "%1." + decimalPlaces + "f", paraValue);
                } else
                    paraValue_para1 = String.format(Locale.US,
                            "%1." + decimalPlaces + "E", paraValue);
            }
            if (Float.isNaN(paraValue)) {
                Variable.isFileFormatCorrupted = true;
            }

        } catch (NumberFormatException e) {
            Variable.isFileFormatCorrupted = true;
            decimalPlaces = 4;
            e.printStackTrace();
        }
        return paraValue_para1;
    }

    /**
     * @param paraValue_para1 ,decimalPlaces format the decimal values. call in
     *                        AlarmEventActivity and BarometerSettingActivity
     * @return String
     */
    public static String setDecimalDigits(String paraValue_para1,
                                          int decimalPlaces) {
        try {
            Float paraValue = Float.parseFloat(paraValue_para1);
            if (0.0000f == paraValue) // xxxx.xxxx
            {
                paraValue_para1 = String.format(Locale.US, "%1." + decimalPlaces + "f",
                        paraValue);
            } else if ((0.0001f <= paraValue) && (paraValue <= 10000.0f)) // xxxx.xxxx
            {
                paraValue_para1 = String.format(Locale.US, "%1." + decimalPlaces + "f",
                        paraValue);
            } else {
                if ((-0.0001f >= paraValue) && (paraValue >= -10000.0f)) // xxxx.xxxx
                {
                    paraValue_para1 = String.format(Locale.US,
                            "%1." + decimalPlaces + "f", paraValue);
                } else {
                    paraValue_para1 = String.format(Locale.US,
                            "%1." + decimalPlaces + "E", paraValue);
                }
            }
            if (Float.isNaN(paraValue)) {
                Variable.isFileFormatCorrupted = true;
            }


        } catch (NumberFormatException e) {
            Variable.isFileFormatCorrupted = true;
            decimalPlaces = 4;
            e.printStackTrace();
        }
        return paraValue_para1;
    }

    public static String dataToascii(String data) {
        String s = "";
        for (int i = 0; i < data.length(); i++) {
            int x = data.charAt(i);
            System.out.println("hfh " + x);
            s = s + " " + intDecimalToHexByte(x);
            System.out.println("ssssss " + s);
        }
        return calculateCRC16(s);
    }

    public static String intDecimalToHexByte(int decimalNumber) {

        String hex = "";
        String temp2 = "";
        int temp1;
        while (decimalNumber > 0) {
            temp1 = decimalNumber % 16;
            temp2 = "" + temp1;
            decimalNumber = decimalNumber / 16;
            switch (temp1) {
                case 10:
                    temp2 = "A";
                    break;
                case 11:
                    temp2 = "B";
                    break;
                case 12:
                    temp2 = "C";
                    break;
                case 13:
                    temp2 = "D";
                    break;
                case 14:
                    temp2 = "E";
                    break;
                case 15:
                    temp2 = "F";
                    break;
            }
            hex = temp2 + hex;
        }
        while (hex.length() < 2) {
            hex = "0" + hex;
        }
        return hex;
    }

    //for CRC methods
    public static String calculateCRC16(String data_str) {
        String givenCommand = data_str;
        String crc_str = "";
        data_str = stringToHexString(data_str);
        System.out.println("ft" + data_str);
        int crc = 0;
        int char_ptr = 0;
        char bit_count;
        int data_length = data_str.length();
        System.out.println(" length " + data_length);
        // initialise crc
        crc = 0xFFFF;
        // loop through entire packet
        do {
            // Exclusive-ORthe byte with the crc
            crc = crc ^ (data_str.charAt(char_ptr));
            // loop through all 8 data bits
            bit_count = 0;
            do {
                // if the LSB is 1, Shift the crc and XOR the polymask with the crc
                if ((crc % 2) == 1) {
                    crc = crc >> 1;
                    crc = crc ^ 0xA001;
                } else {
                    crc = crc >> 1;
                }
                bit_count++;
            } while (bit_count < 8);
            char_ptr++;
        } while (char_ptr < data_length);
        // crc_str = String.valueOf(crc);
        crc_str = intDecimalToHexByte(crc);
        while (crc_str.length() < 4) {
            crc_str = "0" + crc_str;
        }
        System.out.println("CRC = " + crc_str);

        return crc_str;
    }

    public static String stringToHexString(String data) {
        String temp = "";

        String hexStr = "";

        String[] tempStr = data.split(" ");
        for (int i = 0; i < tempStr.length; i++) {
            if (tempStr[i].length() > 1) {

                temp = tempStr[i].trim();
                try {
                    hexStr = hexStr + (char) Integer.parseInt(temp, 16);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return hexStr;
    }

    public static boolean validateFTP_URL_AND_Password(String str) {

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if ((c >= 48 && c <= 57) || ((c >= 65 && c <= 90)) || ((c >= 97 && c <= 122))) {
            } else {
                return false;
            }
        }
        return true;
    }

    public boolean scanStart_Stop(String status) {
        try {
            wakeUpDL();
            sendCMDgetFullRLY("SCAN,\"" + status + "\"");
            if (Constants.OK_STATUS == Constants.sc) {
                if (status.equalsIgnoreCase("STOP")) {
                    Variable.scanStatus = false;
                } else {
                    Variable.scanStatus = true;
                }
                createConfigFile();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String sendCMDgetRLY(String command) {
        try {
            send(command);
            waitForReply();
            if (gotReply) {
                String replyMsg = Constants.reply;
                return replyMsg;
            }
        } catch (Exception e) {
            try {
                throw e;
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        return null;
    }

    public String sendCommandAndGetReplyforMonPara(String command) {
        try {
            send(command);
            waitForReplyforMonPara();
            if (gotReply) {
                String replyMsg = Constants.reply;

                return replyMsg;
            }
        } catch (Exception e) {
            try {
                throw e;
            } catch (Exception e1) {

                e1.printStackTrace();
            }
        }

        return null;
    }

    public String sendCMDgetFullRLY(String command) {
        try {
            send(command);
            waitForReply();
            if (gotReply) {
                String replyMsg = null;
                replyMsg = Constants.reply;
                replyMsg = replyMsg.trim();

                System.out.println(replyMsg);
                return replyMsg;
            }
        } catch (Exception e) {
        }
        // return replyMsg;
        return null;
    }

    public String removeDQ(String text) {
        if ((text != null)) {
            if (text.contains("\"")) {
                text = text.substring(1, text.length() - 1).trim();
            }
            return text;
        }
        return text;
    }

    public void send(String msg) {
        try {

            if (!flag_wakeup) {
                sc = 9999;
                Constants.gotReply = false;
                msg = "$" + msg;
            }
            char postchar_CR, postchar_LF;
            postchar_CR = 0x0D;
            postchar_LF = 0x0A;
            StringBuffer commandPass = new StringBuffer();
            Log.e("CMD ", "" + msg);
            commandPass.append(msg);
            commandPass.append(postchar_CR);
            commandPass.append(postchar_LF);
            bluetoothService.write((commandPass.toString()).getBytes());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void wakeUpDataLoggerConnection() {
        flag_wakeup = true;
        send("\0\0\0\0\0");
        flag_wakeup = false;
        long curmillies = System.currentTimeMillis();
        while ((System.currentTimeMillis() - curmillies) < 1000) ;

    }


    public boolean wakeUpDL() {
        try {
            send("\0\0\0\0\0");
            long curmillies = System.currentTimeMillis();
            while ((System.currentTimeMillis() - curmillies) < 300) {
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public boolean checkScanStatus() {

        wakeUpDL();
        String scanStatus = removeDQ(sendCMDgetRLY("SCAN,\"?\""));
        if (sc == Constants.OK_STATUS) {
            Variable.scanStatus = !scanStatus.equalsIgnoreCase("STOP");
        }
        return Variable.scanStatus;

    }

    public void createConfigFile() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {

            sdCard = Environment.getExternalStorageDirectory();

            dir = new File(sdCard.getAbsolutePath() + "/ESCL10VTR5");
            dir.mkdirs();
            dir = new File(sdCard.getAbsolutePath() + "/ESCL10VTR5/Config File");
            dir.mkdirs();
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            Toast.makeText(getApplicationContext(),
                    "SD Card is available for read only...", Toast.LENGTH_LONG)
                    .show();
        } else {
            Toast.makeText(getApplicationContext(),
                    "SD Card is not available...", Toast.LENGTH_LONG).show();

        }

        generateFile();
    }

    private void generateFile() {

        fileName = Tool.getFileName();
        if (!fileName.equals("")) {

            dataDownloadCompleted = false;
            Constants.downloadData = "";

            Constants.tempDownloadedData = "";
            removeDQ(sendCMDgetRLY("SETUPINF,\"?\""));

            Constants.download_Watchdog_Timer = System.currentTimeMillis();
            while (!dataDownloadCompleted) { // wait till download complete
                if ((System.currentTimeMillis() - download_Watchdog_Timer) > 100000) {

                    Constants.expInBluetoothConnection = true;
                    String error_msg = "Data couldn't be downloaded from datalogger due to connection error...";
                    break;
                }
            }
            if (dataDownloadCompleted) {

                if (downloadData != null && downloadData.length() > 2) {

                    File configFile = new File(dir, fileName);
                    try {
                        FileWriter writer = new FileWriter(configFile);
                        configFile.createNewFile();
                        PrintWriter pw = new PrintWriter(writer);
                        pw.print(downloadData);
                        pw.flush();
                        pw.close();
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

}
