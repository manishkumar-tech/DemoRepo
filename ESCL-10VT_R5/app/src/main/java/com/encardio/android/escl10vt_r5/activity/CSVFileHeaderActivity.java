package com.encardio.android.escl10vt_r5.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.encardio.android.escl10vt_r5.constant.Constants;
import com.encardio.android.escl10vt_r5.tool.Variable;

/**
 * @author Sandeep
 */
public class CSVFileHeaderActivity extends AppCompatActivity {

    protected static final int MESSAGE_SCAN_II_INFO_LOADING_SCREEN = 1;
    protected static final int MESSAGE_CSV_HEADER_UPDATING_SCREEN = 2;
    protected static final int MESSAGE_DOWNLOADING_SCREEN = 3;
    protected static final int MESSAGE_ERASING_MEMORY_SCREEN = 4;
    protected static final int MESSAGE_ERROR_EXCEPTION = 5;
    protected static final int SCAN_EXCEPTION = 6;
    private EditText textColumn1;
    private EditText textColumn2;
    private EditText textColumn3;
    private EditText textColumn4;
    private EditText textColumn5;
    private EditText textColumn6;
    private Button btnUpdateHeader;
    private Button btnDownloadData;
    private Button btnEraseMemory;
    private CheckBox checkboxScanCsvEnableHeader;
    private String columnValue1;
    private String columnValue2;
    private String columnValue3;
    private String columnValue4;
    private String columnValue5;
    private String columnValue6;
    private String enableHeaderStatus;
    private Constants objConstants;
    /**
     * The dialog.
     */
    private ProgressDialog dialog;
    /**
     * The system info thread.
     */
    private Thread scanInfoThread;
    private boolean expInGetScanInfo = false;
    private String error_msg;

    private short statusCodeColumn1;
    private short statusCodeColumn2;
    private short statusCodeColumn3;
    private short statusCodeColumn4;
    private short statusCodeColumn5;
    private short statusCodeColumn6;
    private short statusCodeEnableHeader;
    /**
     * The set text handler.
     */
    @SuppressLint("HandlerLeak")
    private final Handler setTextHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (scanInfoThread != null) {
                scanInfoThread.interrupt();
                scanInfoThread = null;
            }
            switch (msg.what) {
                case MESSAGE_SCAN_II_INFO_LOADING_SCREEN: {
                    dialog.dismiss();
                    if (Constants.toastFromThread)
                        showDialog();
                    else if (Constants.battery_low)
                        battryLowDialog();
                    else
                        displayData();
                    if (expInGetScanInfo) {
                        expInGetScanInfo = false;
                        Toast.makeText(
                                CSVFileHeaderActivity.this,
                                Constants.sc
                                        + ": "
                                        + "Unable to read Logger Information !!\nPlease try again...",
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                }

                case MESSAGE_CSV_HEADER_UPDATING_SCREEN:
                    dialog.dismiss();
                    if (Constants.battery_low)
                        battryLowDialog();
                    else if (!Constants.toastFromThread) {
                        if (expInGetScanInfo
                                || statusCodeColumn1 != Constants.OK_STATUS
                                || statusCodeColumn2 != Constants.OK_STATUS
                                || statusCodeColumn3 != Constants.OK_STATUS
                                || statusCodeColumn4 != Constants.OK_STATUS
                                || statusCodeColumn5 != Constants.OK_STATUS
                                || statusCodeColumn6 != Constants.OK_STATUS
                                || statusCodeEnableHeader != Constants.OK_STATUS) {
                            expInGetScanInfo = false;
                            Toast.makeText(
                                    CSVFileHeaderActivity.this,
                                    Constants.sc
                                            + ": "
                                            + "Unable to update CSV header Information !!\nPlease try again...",
                                    Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(CSVFileHeaderActivity.this,
                                    "Successfully updated...", Toast.LENGTH_SHORT)
                                    .show();
                    } else
                        showDialog();
                    break;
                case MESSAGE_DOWNLOADING_SCREEN:
                    dialog.dismiss();

                    break;
                case MESSAGE_ERASING_MEMORY_SCREEN:
                    dialog.dismiss();
                    if (Constants.battery_low)
                        battryLowDialog();
                    else if (!Constants.toastFromThread) {
                        if (expInGetScanInfo) {
                            expInGetScanInfo = false;
                            Toast.makeText(getApplicationContext(),
                                    Constants.sc + ": " + error_msg,
                                    Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(CSVFileHeaderActivity.this,
                                    "Memory erased successfully...",
                                    Toast.LENGTH_SHORT).show();
                    } else
                        showDialog();
                    break;
                case MESSAGE_ERROR_EXCEPTION:
                    dialog.dismiss();
                    if (expInGetScanInfo) {
                        expInGetScanInfo = false;
                        Toast.makeText(getApplicationContext(),
                                error_msg,
                                Toast.LENGTH_SHORT).show();

                    }
                    break;
                case SCAN_EXCEPTION:

                    if (Constants.battery_low)
                        battryLowDialog();
                    else if (Constants.connectionBreak)
                        showDialog();
                    else
                        Toast.makeText(
                                CSVFileHeaderActivity.this,
                                Constants.sc + ": "
                                        + "Unable to get scan status...",
                                Toast.LENGTH_LONG).show();

                    break;


            }
            expInGetScanInfo = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.csv_file_header);

        textColumn1 = findViewById(R.id.txtColumnValue1);
        textColumn2 = findViewById(R.id.txtColumnValue2);
        textColumn3 = findViewById(R.id.txtColumnValue3);
        textColumn4 = findViewById(R.id.txtColumnValue4);
        textColumn5 = findViewById(R.id.txtColumnValue5);
        textColumn6 = findViewById(R.id.txtColumnValue6);

        btnUpdateHeader = findViewById(R.id.buttonUpdateHeader);
        btnDownloadData = findViewById(R.id.buttonDownloadData);
        btnEraseMemory = findViewById(R.id.buttonEraseMem);
        checkboxScanCsvEnableHeader = findViewById(R.id.checkBoxEnableHeader);

        objConstants = new Constants();
        initProgressDialog("Loading Scan Information !!!");
        dialog.show();
        scanInfoThread = new Thread() {
            public void run() {
                // get scan parameters from data logger
                readScanParameter();
                // sending msg

                if (expInGetScanInfo) {
                    Message msg = setTextHandler
                            .obtainMessage(MESSAGE_ERROR_EXCEPTION);
                    setTextHandler.sendMessage(msg);
                } else {
                    Message msg = setTextHandler
                            .obtainMessage(MESSAGE_SCAN_II_INFO_LOADING_SCREEN);
                    setTextHandler.sendMessage(msg);
                }
            }
        };
        scanInfoThread.start();

        btnUpdateHeader.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                btnUpdateHeader.setClickable(false);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        btnUpdateHeader.setClickable(true);
                    }
                }, 2000);

                if (objConstants.checkScanStatus()) {
                    if (Constants.sc != Constants.OK_STATUS) {
                        Message msg = setTextHandler
                                .obtainMessage(SCAN_EXCEPTION);
                        setTextHandler.sendMessage(msg);
                    } else {
                        String wrn_msg = "Please stop scan before updation.."
                                + "\n" + "Do you Wish to stop scan?";
                        new AlertDialog.Builder(CSVFileHeaderActivity.this)

                                .setTitle(R.string.warning)
                                .setMessage(wrn_msg)
                                .setIcon(R.drawable.warning_icon)
                                .setPositiveButton("Yes",
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                objConstants.wakeUpDL();
                                                objConstants
                                                        .sendCMDgetFullRLY("SCAN,\"STOP\"");
                                                short statusCodeSS = (short) Constants.sc;
                                                if (statusCodeSS == Constants.OK_STATUS) {
                                                    Variable.scanStatus = false;
                                                    Toast.makeText(
                                                            CSVFileHeaderActivity.this,
                                                            "Scan Stopped",
                                                            Toast.LENGTH_SHORT)
                                                            .show();
                                                    updateCSV();
                                                } else {
                                                    Message msg = setTextHandler
                                                            .obtainMessage(SCAN_EXCEPTION);
                                                    setTextHandler
                                                            .sendMessage(msg);
                                                }
                                            }
                                        }).setNegativeButton("NO", null).show();
                    }
                } else {
                    if (Constants.sc != Constants.OK_STATUS) {
                        Message msg = setTextHandler
                                .obtainMessage(SCAN_EXCEPTION);
                        setTextHandler.sendMessage(msg);
                    } else {
                        updateCSV();
                    }
                }
            }
        });

        btnEraseMemory.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Ask the user if they want to quit
                new androidx.appcompat.app.AlertDialog.Builder(CSVFileHeaderActivity.this)
                        .setIcon(R.drawable.earse)
                        .setTitle("Erase Data")
                        .setMessage(
                                "Are you sure to erase memory?\nIt will erase all recorded data from memory.")
                        .setPositiveButton(R.string.yes,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(
                                            DialogInterface dialog1, int which) {
                                        scanCheck();
                                        if (objConstants.checkScanStatus()) {
                                            if (Constants.sc != Constants.OK_STATUS) {
                                                Message msg = setTextHandler
                                                        .obtainMessage(SCAN_EXCEPTION);
                                                setTextHandler.sendMessage(msg);
                                            } else {

                                                String wrn_msg = "Please stop scan before updation.."
                                                        + "\n"
                                                        + "Do you Wish to stop scan?";
                                                new androidx.appcompat.app.AlertDialog.Builder(
                                                        CSVFileHeaderActivity.this)
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

                                                                        objConstants
                                                                                .wakeUpDL();
                                                                        objConstants
                                                                                .sendCMDgetFullRLY("SCAN,\"STOP\"");
                                                                        short statusCodeSS = (short) Constants.sc;
                                                                        if (statusCodeSS == Constants.OK_STATUS) {
                                                                            Variable.scanStatus = false;
                                                                            Toast.makeText(
                                                                                    CSVFileHeaderActivity.this,
                                                                                    "Scan Stopped",
                                                                                    Toast.LENGTH_SHORT)
                                                                                    .show();
                                                                            updateErase();
                                                                        }
                                                                    }
                                                                })
                                                        .setNegativeButton("NO",
                                                                null).show();
                                            }
                                        } else {
                                            if (Constants.sc != Constants.OK_STATUS) {
                                                Message msg = setTextHandler
                                                        .obtainMessage(SCAN_EXCEPTION);
                                                setTextHandler.sendMessage(msg);
                                            } else
                                                updateErase();
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
        });

        btnDownloadData.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CSVFileHeaderActivity.this,
                        DownLoadActivity.class);

                startActivity(intent);
            }
        });

    }

    private void updateErase() {

        initProgressDialog("Erasing Data Logger Memory...");
        dialog.show();
        if (scanInfoThread != null) {
            scanInfoThread.interrupt();
            scanInfoThread = null;
        }
        scanInfoThread = new Thread() {
            public void run() {
                try {
                    objConstants.wakeUpDL();
                    objConstants.sendCMDgetRLY("ERASE");
                    Message msg = setTextHandler
                            .obtainMessage(MESSAGE_ERASING_MEMORY_SCREEN);
                    setTextHandler.sendMessage(msg);
                } catch (Exception e) {
                    Message msg = setTextHandler
                            .obtainMessage(MESSAGE_ERROR_EXCEPTION);
                    setTextHandler.sendMessage(msg);
                }
            }
        };
        scanInfoThread.start();

    }

    private void updateCSV() {

        if (!checkEmptyHeader()) {
            initProgressDialog("Updating Header Information !!!");
            dialog.show();
            scanInfoThread = new Thread() {
                public void run() {
                    updateCSVHeader();
                    Message msg = setTextHandler
                            .obtainMessage(MESSAGE_CSV_HEADER_UPDATING_SCREEN);
                    setTextHandler.sendMessage(msg);
                }
            };
            scanInfoThread.start();
        } else {
            if (expInGetScanInfo) {
                expInGetScanInfo = false;
                Toast.makeText(getApplicationContext(), error_msg,
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void scanCheck() {
        objConstants.wakeUpDL();
        String reply = objConstants.removeDQ(objConstants
                .sendCMDgetRLY("SCAN,\"?\""));
        short statsCode = (short) Constants.sc;
        if (statsCode == Constants.OK_STATUS) {
            Variable.scanStatus = reply.trim().equalsIgnoreCase("START");
        } else
            Variable.scanStatus = false;
    }

    /**
     * Read scan parameter.
     */
    private void readScanParameter() {
        try {
            if (Variable.isConnected) {
                objConstants.wakeUpDL();

                columnValue1 = objConstants
                        .sendCMDgetRLY("HEADCOL,1,\"?\"");
                statusCodeColumn1 = (short) Constants.sc;
                if (Constants.sc == Constants.OK_STATUS) {
                    columnValue2 = objConstants
                            .sendCMDgetRLY("HEADCOL,2,\"?\"");
                    statusCodeColumn2 = (short) Constants.sc;
                }
                if (Constants.sc == Constants.OK_STATUS) {
                    columnValue3 = objConstants
                            .sendCMDgetRLY("HEADCOL,3,\"?\"");
                    statusCodeColumn3 = (short) Constants.sc;
                }
                if (Constants.sc == Constants.OK_STATUS) {
                    columnValue4 = objConstants
                            .sendCMDgetRLY("HEADCOL,4,\"?\"");
                    statusCodeColumn4 = (short) Constants.sc;
                }
                if (Constants.sc == Constants.OK_STATUS) {
                    columnValue5 = objConstants
                            .sendCMDgetRLY("HEADCOL,5,\"?\"");
                    statusCodeColumn5 = (short) Constants.sc;
                }
                if (Constants.sc == Constants.OK_STATUS) {
                    columnValue6 = objConstants
                            .sendCMDgetRLY("HEADCOL,6,\"?\"");
                    statusCodeColumn6 = (short) Constants.sc;
                }
                if (Constants.sc == Constants.OK_STATUS) {
                    enableHeaderStatus = objConstants
                            .removeDQ(objConstants
                                    .sendCMDgetRLY("CSVHEAD,\"?\""));
                    statusCodeEnableHeader = (short) Constants.sc;
                }
            }

        } catch (Exception e) {
            expInGetScanInfo = true;
            error_msg = "Error in Scan parameter...";
            e.printStackTrace();

        }
    }

    private void displayData() {
        try {
            String[] csvColumn1Array = columnValue1.split(",");
            int col1 = Integer.parseInt(csvColumn1Array[1]);
            if (statusCodeColumn1 == Constants.OK_STATUS) {
                if (col1 == 1) {
                    textColumn1.setEnabled(false);
                    textColumn1.setFocusable(false);
                    textColumn1.setClickable(false);
                }
                textColumn1.setText(csvColumn1Array[2].substring(1,
                        csvColumn1Array[2].length() - 1));
            } else {
                textColumn1.setEnabled(false);
                textColumn1.setText("");
                textColumn1.setFocusable(false);
                textColumn1.setClickable(false);
            }
            if (statusCodeColumn2 == Constants.OK_STATUS) {
                csvColumn1Array = columnValue2.split(",");
                col1 = Integer.parseInt(csvColumn1Array[1]);
                if (col1 == 1) {
                    textColumn2.setEnabled(false);
                    textColumn2.setFocusable(false);
                    textColumn2.setClickable(false);
                }
                textColumn2.setText(csvColumn1Array[2].substring(1,
                        csvColumn1Array[2].length() - 1));
            } else {
                textColumn2.setEnabled(false);
                textColumn2.setText("");
                textColumn2.setFocusable(false);
                textColumn2.setClickable(false);
            }
            if (statusCodeColumn3 == Constants.OK_STATUS) {
                csvColumn1Array = columnValue3.split(",");
                col1 = Integer.parseInt(csvColumn1Array[1]);
                if (col1 == 1) {
                    textColumn3.setEnabled(false);
                    textColumn3.setFocusable(false);
                    textColumn3.setClickable(false);
                }
                textColumn3.setText(csvColumn1Array[2].substring(1,
                        csvColumn1Array[2].length() - 1));
            } else {
                textColumn3.setEnabled(false);
                textColumn3.setText("");
                textColumn3.setFocusable(false);
                textColumn3.setClickable(false);
            }
            if (statusCodeColumn4 == Constants.OK_STATUS) {
                csvColumn1Array = columnValue4.split(",");
                col1 = Integer.parseInt(csvColumn1Array[1]);
                if (col1 == 1) {
                    textColumn4.setEnabled(false);
                    textColumn4.setFocusable(false);
                    textColumn4.setClickable(false);
                }
                textColumn4.setText(csvColumn1Array[2].substring(1,
                        csvColumn1Array[2].length() - 1));
            } else {
                textColumn4.setEnabled(false);
                textColumn4.setText("");
                textColumn4.setFocusable(false);
                textColumn4.setClickable(false);
            }
            if (statusCodeColumn5 == Constants.OK_STATUS) {
                csvColumn1Array = columnValue5.split(",");
                col1 = Integer.parseInt(csvColumn1Array[1]);
                if (col1 == 1) {
                    textColumn5.setEnabled(false);
                    textColumn5.setFocusable(false);
                    textColumn5.setClickable(false);
                }
                textColumn5.setText(csvColumn1Array[2].substring(1,
                        csvColumn1Array[2].length() - 1));
            } else {
                textColumn5.setEnabled(false);
                textColumn5.setText("");
                textColumn5.setFocusable(false);
                textColumn5.setClickable(false);
            }
            if (statusCodeColumn6 == Constants.OK_STATUS) {
                csvColumn1Array = columnValue6.split(",");
                col1 = Integer.parseInt(csvColumn1Array[1]);
                if (col1 == 1) {
                    textColumn6.setEnabled(false);
                    textColumn6.setFocusable(false);
                    textColumn6.setClickable(false);
                }
                textColumn6.setText(csvColumn1Array[2].substring(1,
                        csvColumn1Array[2].length() - 1));
            } else {
                textColumn6.setEnabled(false);
                textColumn6.setText("");
                textColumn6.setFocusable(false);
                textColumn6.setClickable(false);
            }
            if (statusCodeEnableHeader == Constants.OK_STATUS) {
                checkboxScanCsvEnableHeader.setChecked(enableHeaderStatus.trim().equalsIgnoreCase("ENABLE"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateCSVHeader() {
        try {
            objConstants.wakeUpDL();
            if (textColumn1.isEnabled()) {
                objConstants.sendCMDgetRLY("HEADCOL,1,0,\""
                        + textColumn1.getText().toString().trim() + "\"");
                statusCodeColumn1 = (short) Constants.sc;
            }

            if (textColumn2.isEnabled()) {

                objConstants.sendCMDgetRLY("HEADCOL,2,0,\""
                        + textColumn2.getText().toString().trim() + "\"");
                statusCodeColumn2 = (short) Constants.sc;

            }
            if (textColumn3.isEnabled()) {

                objConstants.sendCMDgetRLY("HEADCOL,3,0,\""
                        + textColumn3.getText().toString().trim() + "\"");
                statusCodeColumn3 = (short) Constants.sc;

            }
            if (textColumn4.isEnabled()) {

                objConstants.sendCMDgetRLY("HEADCOL,4,0,\""
                        + textColumn4.getText().toString().trim() + "\"");
                statusCodeColumn4 = (short) Constants.sc;

            }
            if (textColumn5.isEnabled()) {

                objConstants.sendCMDgetRLY("HEADCOL,5,0,\""
                        + textColumn5.getText().toString().trim() + "\"");
                statusCodeColumn5 = (short) Constants.sc;

            }
            if (textColumn6.isEnabled()) {

                objConstants.sendCMDgetRLY("HEADCOL,6,0,\""
                        + textColumn6.getText().toString().trim() + "\"");
                statusCodeColumn6 = (short) Constants.sc;

            }
            if (checkboxScanCsvEnableHeader.isChecked()) {

                objConstants.sendCMDgetRLY("CSVHEAD,\"ENABLE\"");
                statusCodeEnableHeader = (short) Constants.sc;
                Constants.headerstat = true;
                Constants.head_para = textColumn4.getText().toString();

            } else {

                objConstants.sendCMDgetRLY("CSVHEAD,\"DISABLE\"");
                statusCodeEnableHeader = (short) Constants.sc;
                Constants.headerstat = false;
                Constants.head_para = "Parameter";

            }
        } catch (Exception e) {
            expInGetScanInfo = true;
            e.printStackTrace();
        }
    }

    private boolean checkEmptyHeader() {
        try {
            if (textColumn1.isEnabled()) {
                if (textColumn1.getText().toString() != null
                        && textColumn1.getText().toString().trim().length() > 0) {
                } else {
                    textColumn1.requestFocus();
                    error_msg = "Column 1 can't be empty...";
                    expInGetScanInfo = true;
                    return true;
                }
            }

            if (textColumn2.isEnabled()) {
                if (textColumn2.getText().toString() != null
                        && textColumn2.getText().toString().trim().length() > 0) {
                } else {
                    textColumn2.requestFocus();
                    error_msg = "Column 2 can't be empty...";
                    expInGetScanInfo = true;
                    return true;
                }
            }

            if (textColumn3.isEnabled()) {
                if (textColumn3.getText().toString() != null
                        && textColumn3.getText().toString().trim().length() > 0) {
                } else {
                    textColumn3.requestFocus();
                    error_msg = "Column 3 can't be empty...";
                    expInGetScanInfo = true;
                    return true;
                }
            }

            if (textColumn4.isEnabled()) {
                if (textColumn4.getText().toString() != null
                        && textColumn4.getText().toString().trim().length() > 0) {
                } else {
                    textColumn4.requestFocus();
                    error_msg = "Column 4 can't be empty...";
                    expInGetScanInfo = true;
                    return true;
                }
            }

            if (textColumn5.isEnabled()) {
                if (textColumn5.getText().toString() != null
                        && textColumn5.getText().toString().trim().length() > 0) {
                } else {
                    textColumn5.requestFocus();
                    error_msg = "Column 5 can't be empty...";
                    expInGetScanInfo = true;
                    return true;
                }
            }

            if (textColumn6.isEnabled()) {
                if (textColumn6.getText().toString() != null
                        && textColumn6.getText().toString().trim().length() > 0) {
                } else {
                    textColumn6.requestFocus();
                    error_msg = "Column 6 can't be empty...";
                    expInGetScanInfo = true;
                    return true;
                }
            }

        } catch (Exception ignored) {
        }
        return false;
    }

    protected void battryLowDialog() {

        Constants.battery_low = false;
        new AlertDialog.Builder(CSVFileHeaderActivity.this)
                .setIcon(R.drawable.battery0).setTitle(Constants.responseMsg)
                .setMessage("Please replace battery immediately !!!")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int which) {

                        Intent intent = new Intent(getApplicationContext(),
                                HomeActivity.class);
                        // intent.putExtra("", Constants.statusCode);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }

                }).show();

    }

    protected void showDialog() {
        // TODO Auto-generated method stub
        new AlertDialog.Builder(CSVFileHeaderActivity.this)
                .setIcon(R.drawable.error).setTitle("Connection")
                .setMessage("Device connection lost !").setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int which) {
                        Intent intent = new Intent(getApplicationContext(),
                                HomeActivity.class);
                        // intent.putExtra("", Constants.statusCode);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }

                }).show();
    }

    /**
     * To initialize progress bar & set dialog title.
     */
    private void initProgressDialog(String msg) {
        // create a dialog
        dialog = new ProgressDialog(this);
        // set the title of the dialog
        dialog.setTitle(msg);
        // Set if the dialog can be skipped
        dialog.setCancelable(false);
        dialog.setMessage("Please Wait.....");
    }

    /**
     * Exit from application if select yes on dialog box otherwise show same
     * screen.
     *
     * @param keyCode the key code
     * @param event   the event
     * @return true, if successful
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            try {
                if (scanInfoThread != null) {
                    scanInfoThread.interrupt();
                    scanInfoThread = null;
                }
                CSVFileHeaderActivity.this.finish();
                objConstants = null;
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(),
                        Toast.LENGTH_SHORT).show();
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }


}
