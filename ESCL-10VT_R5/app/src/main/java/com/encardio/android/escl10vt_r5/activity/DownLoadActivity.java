package com.encardio.android.escl10vt_r5.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.encardio.android.escl10vt_r5.constant.Constants;
import com.encardio.android.escl10vt_r5.tool.Tool;
import com.encardio.android.escl10vt_r5.tool.Variable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Sandeep
 */

public class DownLoadActivity extends AppCompatActivity {

    private static String downloadFile_lastRecord_dateTime;
    private static String compositeFile_lastRecord_dateTime;
    private final int MESSAGE_DOWNLOAD_SCREEN = 10;
    private final int MESSAGE_DOWNLOAD_EXCEPTION_SCREEN = 30;
    protected Thread dialogThread;

    private TextView txt_TotalRecords;

    private TextView txt_TotalRecordsFromLastDownload;
    private TextView txt_TotalRecordsFromLastUpload;
    private RadioButton btnRadio_FromBegining;
    private RadioButton btnRadio_FromLastDownload;

    private Button buttonSaveFile;
    private Constants constantS;

    private int totalRecordToDownload = 0;

    private int mYear;
    private int mMonth;
    private int mDay;
    private int mHour;
    private int mMinute;
    private String fileName = "";
    private String compositeFileName;
    private File sdCard;
    private File dir;
    private ProgressDialog dialog;
    private Thread NORthread;
    private short statusCodeGetData;
    private short statusCodeNoOfRec;
    private short statusCodeSensId;
    private String error_msg = null;
    @SuppressLint("HandlerLeak")
    private final Handler setTextHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (dialogThread != null) {
                dialogThread.interrupt();
                dialogThread = null;
            }

            switch (msg.what) {
                case MESSAGE_DOWNLOAD_SCREEN:
                    dialog.dismiss();
                    Constants.downloadingDataCommand = false;
                    if (Constants.dataDownloadCompleted) {
                        Constants.dataDownloadCompleted = false;
                        Toast.makeText(DownLoadActivity.this,
                                "Data downloaded successfully !!!",
                                Toast.LENGTH_LONG).show();
                        buttonSaveFile.setEnabled(false);

                        alertForEraseData();
                    } else if (Constants.dataDownloadUnavailble) {
                        Constants.dataDownloadUnavailble = false;
                        Toast.makeText(DownLoadActivity.this,
                                Constants.responseMsg, Toast.LENGTH_LONG).show();

                    }
                    break;
                case MESSAGE_DOWNLOAD_EXCEPTION_SCREEN:
                    dialog.dismiss();
                    if (Constants.expInBluetoothConnection) {
                        Constants.expInBluetoothConnection = false;

                    }
                    Constants.downloadingDataCommand = false;
                    Toast.makeText(DownLoadActivity.this,
                            Constants.sc + ": " + error_msg,
                            Toast.LENGTH_LONG).show();
                    break;
            }

        }
    };


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_menu);

        txt_TotalRecords = findViewById(R.id.txt_TotalRecords);
        txt_TotalRecordsFromLastDownload = findViewById(R.id.txt_TotalRecordsFromLastDownload);
        txt_TotalRecordsFromLastUpload = findViewById(R.id.txt_TotalRecordsFromLastUpload);

        btnRadio_FromBegining = findViewById(R.id.btnRadio_FromBegining);
        btnRadio_FromLastDownload = findViewById(R.id.btnRadio_FromLastDownload);

        buttonSaveFile = findViewById(R.id.buttonSaveFile);
        checkExistenceofSDCard();
        constantS = new Constants();
        fileName = getFileName();

        try {
            getNoOfRecords();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Constants.toastFromThread) {
            if (dialog.isShowing())
                dialog.dismiss();
            showDialog();
        } else if (Constants.battery_low)
            battryLowDialog();
        else

            findViewById(R.id.buttonViewData).setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View v) {
                            System.gc();
                            Intent intent = new Intent(v.getContext(),
                                    ViewFileDataTableActivity.class);

                            startActivity(intent);
                        }
                    });
        findViewById(R.id.buttonUploadFile).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(),
                                UploadFilesActivity.class);
                        startActivity(intent);
                    }
                });

        buttonSaveFile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Constants.downloadingDataCommand = true;
                Constants.dataDownloadCompleted = false;
                Constants.dataDownloadAvailble = false;
                Constants.dataDownloadUnavailble = false;
                downloadData();
            }
        });
    }

    protected void showDialog() {

        new androidx.appcompat.app.AlertDialog.Builder(DownLoadActivity.this)
                .setIcon(R.drawable.error).setTitle("Connection")
                .setMessage("Device connection lost !").setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int which) {
                        Intent intent = new Intent(getApplicationContext(),
                                HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }

                }).show();
        Looper.loop();
    }

    protected void showDialogGPRS_Mode() {

        new androidx.appcompat.app.AlertDialog.Builder(DownLoadActivity.this)
                .setIcon(R.drawable.info).setTitle("Upload Running")
                .setMessage("Busy in Upload !").setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int which) {

                    }

                }).show();
        Looper.loop();
    }

    protected void battryLowDialog() {

        Constants.battery_low = false;

        new androidx.appcompat.app.AlertDialog.Builder(DownLoadActivity.this)
                .setIcon(R.drawable.battery0).setTitle(Constants.responseMsg)
                .setMessage("Please replace battery immediately !!!")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int which) {

                        Intent intent = new Intent(getApplicationContext(),
                                HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }

                }).show();
        Looper.loop();
    }

    public void checkExistenceofSDCard() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // creating a file directory
            sdCard = Environment.getExternalStorageDirectory();
            // dir = new File(sdCard.getAbsolutePath() + "/Encardio rite");
            // dir.mkdirs();
            dir = new File(sdCard.getAbsolutePath() + "/ESCL10VTR5");
            dir.mkdirs();
            dir = new File(sdCard.getAbsolutePath() + "/ESCL10VTR5/CSV Files");
            dir.mkdirs();
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            Toast.makeText(DownLoadActivity.this,
                    "SD Card is available for read only...", Toast.LENGTH_LONG)
                    .show();
            buttonSaveFile.setEnabled(false);
        } else {
            Toast.makeText(DownLoadActivity.this,
                    "SD Card is not available...", Toast.LENGTH_LONG).show();
            buttonSaveFile.setEnabled(false);
        }

    }

    /**
     * For download data
     */

    private void downloadData() {

        if (btnRadio_FromBegining.isChecked()) {
            try {
                totalRecordToDownload = Integer.parseInt(Variable.totalNumberOfRecord);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                totalRecordToDownload = Integer.parseInt(Variable.totalNumberOfRecordFromLastDownload);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        if (totalRecordToDownload > 0) {
            initProgressDialog();
            dialog.show();
            dialog.setProgressNumberFormat("");
            dialogThread = new Thread() {
                public void run() {
                    saveFileData();

                    if (Constants.dataDownloadCompleted) {
                        Message msg = setTextHandler
                                .obtainMessage(MESSAGE_DOWNLOAD_SCREEN);
                        setTextHandler.sendMessage(msg);
                    } else if (Constants.dataDownloadUnavailble) {
                        Message msg = setTextHandler
                                .obtainMessage(MESSAGE_DOWNLOAD_SCREEN);
                        setTextHandler.sendMessage(msg);
                    } else if ((Constants.expInBluetoothConnection)
                            || (Constants.battery_low)) {
                        Message msg = setTextHandler
                                .obtainMessage(MESSAGE_DOWNLOAD_EXCEPTION_SCREEN);
                        setTextHandler.sendMessage(msg);
                    }
                }

            };
            dialogThread.start();
            buttonSaveFile.setEnabled(false);
        } else {
            emptyRecord();
        }
    }

    private void emptyRecord() {

        new androidx.appcompat.app.AlertDialog.Builder(DownLoadActivity.this)
                .setIcon(R.drawable.empty_data).setTitle("No record found !")
                .setMessage("Data logger memory is empty.").setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int which) {
                        dialog.dismiss();
                    }

                }).show();
    }

    protected void saveFileData() {
        System.gc();
        int temp1, temp = 0;
        Constants.tempcount = 1;
        Constants.progress = 0;
        dialog.setProgress(Constants.progress);
        if (totalRecordToDownload > 0) {
            Constants.countRecords = 0;
            temp = totalRecordToDownload / 90;
            temp1 = 0;
            if (temp == 0) {
                temp = 1;
            }

            constantS.wakeUpDL();
            Constants.downloadData = "";

            Constants.tempDownloadedData = "";


            if (btnRadio_FromBegining.isChecked()) {
                constantS.removeDQ(constantS
                        .sendCMDgetFullRLY("GETDATA,0,\"?\""));
            } else {
                constantS.removeDQ(constantS
                        .sendCMDgetFullRLY("GETDATA,1,\"?\""));
            }

            if (Constants.MODEM_COMM_MODE_GPRS_STATUS == Constants.sc) {
                if (dialog.isShowing())
                    dialog.dismiss();
                Looper.prepare();
                showDialogGPRS_Mode();
            } else {
                if (Constants.gotReply) {
                    try {
                        Constants.delay(100);

                        if (Constants.dataDownloadUnavailble)
                            return;

                        if (Constants.dataDownloadAvailble) {
                            Constants.download_Watchdog_Timer = System
                                    .currentTimeMillis();
                            while (!Constants.dataDownloadCompleted) { // wait till
                                // download
                                // complete
                                if ((System.currentTimeMillis() - Constants.download_Watchdog_Timer) > 30000) {
                                    Constants.expInBluetoothConnection = true;
                                    error_msg = "Data couldn't be downloaded from datalogger due to connection error...";
                                    break;
                                } else {
                                    // show progress for downloaded data
                                    temp1 = Constants.countRecords / temp;
                                    if (Constants.progress != temp1) {
                                        Constants.progress = temp1;
                                        dialog.setProgress(Constants.progress);
                                    }
                                }

                            }
                            Log.e("------------", "------------");
                            if (Constants.dataDownloadCompleted) {
                                String downloadData = Constants.downloadData;
                                if (downloadData != null) {
                                    generateCsvFile(downloadData);

                                    Constants.progress = Constants.progress + 5;
                                    dialog.setProgress(Constants.progress);
                                }
                            }

                        }
                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                } else {
                    if (dialog.isShowing())
                        dialog.dismiss();
                    Looper.prepare();
                    showDialog();
                }
            }


        }
    }

    protected void getNoOfRecords() {
        final Handler recordHandler = new Handler();
        dialog = new ProgressDialog(this);
        dialog.setTitle("Fetching No. Of Records");
        dialog.setCancelable(true);
        dialog.setMessage("Please Wait.....");
        dialog.show();
        NORthread = new Thread() {
            public void run() {
                try {

                    constantS.wakeUpDL();

                    Variable.totalNumberOfRecord = constantS.removeDQ(constantS
                            .sendCMDgetRLY("NOOFREC,0,\"?\""));

                    if (Constants.OK_STATUS == Constants.sc) {
                        Variable.totalNumberOfRecordFromLastDownload = constantS.removeDQ(constantS
                                .sendCMDgetRLY("NOOFREC,1,\"?\""));
                    }
                    if (Constants.OK_STATUS == Constants.sc) {
                        Variable.totalNumberOfRecordFromLastUpload = constantS.removeDQ(constantS
                                .sendCMDgetRLY("NOOFREC,2,\"?\""));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                recordHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!Constants.toastFromThread) {

                            txt_TotalRecords.setText("" + Variable.totalNumberOfRecord);
                            txt_TotalRecordsFromLastDownload.setText("" + Variable.totalNumberOfRecordFromLastDownload);
                            txt_TotalRecordsFromLastUpload.setText("" + Variable.totalNumberOfRecordFromLastUpload);

                            dialog.dismiss();
                            NORthread = null;
                        }
                    }
                });
            }
        };
        NORthread.start();
    }

    private void generateCsvFile(String fileData) {
        try {
            File csvfile = new File(dir, fileName);
            FileWriter writer = new FileWriter(csvfile);

            PrintWriter pw = new PrintWriter(writer);
            // Write to file for the first row
            pw.print(fileData);
            // Flush the output to the file
            pw.flush();
            // Close the Print Writer
            pw.close();
            // Close the File Writer
            writer.close();

            if (!checkCompositeFileExistence()) {
                File csvcompositefile = new File(dir, compositeFileName);
                FileWriter writercomposite = new FileWriter(csvcompositefile);

                // writercomposite.append(fileData);
                PrintWriter pws = new PrintWriter(writercomposite);
                // Write to file for the first row
                pws.print(fileData);
                // Flush the output to the file
                pws.flush();
                // Close the Print Writer
                pws.close();
                // Close the File Writer
                writercomposite.close();
            } else {
                if (CheckCSVAppend()) // is append required
                {
                    appendToCompositeFile();
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void appendToCompositeFile() {


        String tempDataToAppend = "";
        String record = "";
        String recordDateTime = "";

        String appendData = ""; // data to append
        String[] lines = Constants.downloadData.split("\n");
        for (int i = 0; i < lines.length; i++) {

            try {
                if (lines[i].length() > 2) {

                    if (Tool.isHeader(lines[i], 1, "yy/MM/dd HH:mm")) {

                    } else {

                        record = lines[i].trim();
                        recordDateTime = "20" + record.substring(record.indexOf(",") + 1,
                                record.indexOf(":") + 3);

                        Log.e("recordDateTime", "" + recordDateTime);
                        Log.e("lastRecord_dateTime", "" + compositeFile_lastRecord_dateTime);

                        SimpleDateFormat dateFormat = new SimpleDateFormat(
                                "yyyy/MM/dd HH:mm");
                        Date now = Calendar.getInstance().getTime();
                        Date serverDate_1 = now;
                        Date serverDate_2 = now;

                        try {
                            serverDate_1 = dateFormat
                                    .parse(compositeFile_lastRecord_dateTime);
                            serverDate_2 = dateFormat.parse(recordDateTime);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        long millis = (serverDate_2.getTime() - serverDate_1
                                .getTime());
                        // 1000ms * 60s * 2m
                        if (millis > 0) {
                            tempDataToAppend = tempDataToAppend + record + "\n";
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        appendData = appendData + tempDataToAppend;

        try {
            // String data1 = appendData;

            FileWriter fw;
            try {
                File file1 = new File(dir, compositeFileName);
                file1.createNewFile();
                fw = new FileWriter(file1, true);
                PrintWriter pw = new PrintWriter(fw);
                // Write to file for the first row
                pw.print(appendData);
                // Flush the output to the file
                pw.flush();
                // Close the Print Writer
                pw.close();
                // Close the File Writer
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean checkCompositeFileExistence() {
        for (File f : dir.listFiles()) {
            if (f.isFile())
                if (f.getName().contains(compositeFileName))
                    return true;
        }
        return false;
    }

    public boolean CheckCSVAppend() {
        boolean isAppendRequired = false;
        try {
            File csvfile = new File(dir, fileName);
            File compositeCSV = new File(dir, compositeFileName);


            downloadFile_lastRecord_dateTime = getLastRecordDateTimeFromCsvFile(csvfile);
            compositeFile_lastRecord_dateTime = getLastRecordDateTimeFromCsvFile(compositeCSV);


            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy/MM/dd HH:mm");
            Date now = Calendar.getInstance().getTime();
            Date serverDate_1 = now;
            Date serverDate_2 = now;

            try {
                serverDate_1 = dateFormat
                        .parse(compositeFile_lastRecord_dateTime);
                serverDate_2 = dateFormat
                        .parse(downloadFile_lastRecord_dateTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            long millis = (serverDate_2.getTime() - serverDate_1.getTime());

            if (millis > 0) {
                isAppendRequired = true; // GREATER THAN
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("isAppendRequired", "" + isAppendRequired);
        return isAppendRequired;
    }

    public String getLastRecordDateTimeFromCsvFile(File file) {
        String temp = "";
        String lastRecord = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((temp = br.readLine()) != null) {
                if (temp.length() > 2) {
                    lastRecord = temp;
                }
            }
            lastRecord = "20" + lastRecord.substring(lastRecord.indexOf(",") + 1,
                    lastRecord.indexOf(":") + 3);
            Log.e("lastRecord", "" + lastRecord);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lastRecord;
    }

    private void initProgressDialog() {
        // create a dialog
        dialog = new ProgressDialog(DownLoadActivity.this);
        // set the title of the dialog
        dialog.setTitle("Downloading Data");
        dialog.setIcon(R.drawable.download_icon);
        // Set if the dialog can be skipped
        dialog.setCancelable(false);
        // dialog.setMax(100);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMessage("Please Wait.....");
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Handle the back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            DownLoadActivity.this.finish();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void alertForEraseData() {
        if (dialogThread != null) {
            dialogThread.interrupt();
            dialogThread = null;
        }
        // Ask the user if they want to quit
        new AlertDialog.Builder(this)
                .setTitle("Erase Memory !")
                .setIcon(R.drawable.earse)
                .setMessage("Do you want to erase logger memory?")
                .setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {

                                if (constantS.checkScanStatus()) {
                                    if (Constants.sc != Constants.OK_STATUS) {
                                        if (Constants.battery_low)
                                            battryLowDialog();
                                        else if (Constants.connectionBreak)
                                            showDialog();
                                        else
                                            Toast.makeText(
                                                    DownLoadActivity.this,
                                                    Constants.sc
                                                            + ": Unable to get scan status...",
                                                    Toast.LENGTH_LONG).show();
                                    } else {
                                        String wrn_msg = "Please stop scan before updation.."
                                                + "\n"
                                                + "Do you Wish to stop scan?";
                                        new androidx.appcompat.app.AlertDialog.Builder(
                                                DownLoadActivity.this)
                                                .setIcon(
                                                        R.drawable.warning_icon)
                                                .setTitle(R.string.warning)
                                                .setMessage(wrn_msg)
                                                .setPositiveButton(
                                                        "Yes",
                                                        new DialogInterface.OnClickListener() {

                                                            @Override
                                                            public void onClick(
                                                                    DialogInterface dialog,
                                                                    int which) {
                                                                constantS
                                                                        .wakeUpDL();
                                                                constantS
                                                                        .sendCMDgetFullRLY("SCAN,\"STOP\"");
                                                                short statusCodeSS = (short) Constants.sc;
                                                                if (statusCodeSS == Constants.OK_STATUS) {
                                                                    Variable.scanStatus = false;
                                                                    Toast.makeText(
                                                                            DownLoadActivity.this,
                                                                            "Scan Stopped",
                                                                            Toast.LENGTH_SHORT)
                                                                            .show();
                                                                    areYouSureToErase();
                                                                } else {
                                                                    if (Constants.battery_low)
                                                                        battryLowDialog();
                                                                    else if (Constants.connectionBreak)
                                                                        showDialog();
                                                                    else
                                                                        Toast.makeText(
                                                                                DownLoadActivity.this,
                                                                                Constants.sc
                                                                                        + ": Unable to get scan status...",
                                                                                Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        })
                                                .setNegativeButton("NO", null)
                                                .show();
                                    }
                                } else {
                                    if (Constants.sc != Constants.OK_STATUS) {
                                        if (Constants.battery_low)
                                            battryLowDialog();
                                        else if (Constants.connectionBreak)
                                            showDialog();
                                        else
                                            Toast.makeText(
                                                    DownLoadActivity.this,
                                                    Constants.sc
                                                            + ": Unable to get scan status...",
                                                    Toast.LENGTH_LONG).show();
                                    } else
                                        areYouSureToErase();
                                }
                            }
                        })
                .setNegativeButton(R.string.no,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {

                            }
                        }).show();
    }

    private void areYouSureToErase() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setIcon(R.drawable.warning_icon)
                .setTitle("Erase Data Logger Memory !!!")
                .setMessage("Are you sure?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        constantS.sendCMDgetRLY("ERASE");

                        try {
                            getNoOfRecords();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (Constants.toastFromThread) {
                            showDialog();
                        } else if (Constants.battery_low) {
                            battryLowDialog();
                        } else {
                            Toast.makeText(DownLoadActivity.this,
                                    "Data erased successfully ",
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {

                            }
                        }).show();
    }

    private String getFileName() {
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        System.gc();

        constantS.wakeUpDL();
        String sensorId = constantS.removeDQ(constantS
                .sendCMDgetRLY("SN,\"?\""));
        if (Constants.gotReply) {
            statusCodeSensId = (short) Constants.sc;
            sensorId = sensorId.trim();
            String year = String.valueOf(mYear);
            String date = (new StringBuilder()
                    // Month is 0 based so add 1
                    .append(year.substring(2, 4)).append(Tool.pad(mMonth + 1))
                    .append(Tool.pad(mDay))).toString();
            String time = Tool.pad(mHour) +
                    Tool.pad(mMinute);
            fileName = Variable.loggerID + "_" + sensorId + "_" + date + "_" + time
                    + ".csv";

            compositeFileName = Variable.loggerID + "_" + sensorId + ".csv";

        }

        return fileName;
    }


}