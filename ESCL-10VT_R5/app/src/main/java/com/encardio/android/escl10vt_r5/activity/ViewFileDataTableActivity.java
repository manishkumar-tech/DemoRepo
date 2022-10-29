package com.encardio.android.escl10vt_r5.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.csvreader.CsvReader;
import com.encardio.android.escl10vt_r5.constant.Constants;
import com.encardio.android.escl10vt_r5.service.Item;
import com.encardio.android.escl10vt_r5.tool.Tool;
import com.encardio.android.escl10vt_r5.tool.Variable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


/**
 * @author Sandeep
 */
public class ViewFileDataTableActivity extends AppCompatActivity {


    private static final int CORRECTED = 0;
    private static final int UNCORRECTED = 1;
    private static final int BATT_VOLT = 2;
    private static final int BAROMETER = 3;
    private static final int TEMPERATURE = 4;

    private static File csvFilesDir;
    protected String selectedFile;
    protected int selectedFilePosition = 0;
    String temp = "";
    String datetime = "";
    int forFirstRecord = 0;
    int nextCounter = 0;
    float value;
    String[] spinner_parameterType_value = new String[5];
    ArrayAdapter<String> arrayAdapter_parameterType;

    ImageView iv_graph;
    ViewDataAdapter adapter;
    List<ViewDataModel> list;
    String[] date_time = null;
    String[] corrected = null;
    String[] uncorrected = null;
    String[] battery_voltage = null;
    String[] temperature = null;
    String[] barometer = null;

    private TextView textTableHeaderParameter;
    private ArrayList<Item> files;
    private String absolutePath;
    private File[] file;
    private Spinner spinnerFileName;
    private Spinner spinner_parameterType;
    private RecyclerView rv_view_data;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.view_filedata);

            iv_graph = findViewById(R.id.iv_graph);
            textTableHeaderParameter = findViewById(R.id.textFilePara);
            spinnerFileName = findViewById(R.id.spinnerFileName);
            spinner_parameterType = findViewById(R.id.spinner_parameterType);
            rv_view_data = findViewById(R.id.rv_view_data);

            files = new ArrayList<Item>();
            File sdCard = Environment.getExternalStorageDirectory();
            csvFilesDir = new File(sdCard.getAbsolutePath() + "/ESCL10VTR5/CSV Files");

            if (serachAllCSVFile()) {
                absolutePath = csvFilesDir.getAbsolutePath();

                getFileData(selectedFilePosition);

                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                layoutManager.setOrientation(RecyclerView.VERTICAL);
                rv_view_data.setLayoutManager(layoutManager);

                list = new ArrayList<>();
                adapter = new ViewDataAdapter(list);
                rv_view_data.setAdapter(adapter);

                if (csvFilesDir.listFiles() != null) {
                    add_file_in_spinner();
                }
                showListData(0);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        spinnerFileName
                .setOnItemSelectedListener(new OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view,
                            int position, long id) {

                        getFileData(position);
                        showListData(0);
                        spinner_parameterType.setSelection(0);
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> arg0) {
                    }
                });

        spinner_parameterType.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                showListData(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        iv_graph.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (Variable.isFileFormatCorrupted) {
                    alert();
                } else {
                    try {
                        if (csvFilesDir.listFiles().length == 0) {
                            Toast.makeText(getApplicationContext(),
                                    "CSV File not available ", Toast.LENGTH_LONG).show();
                        } else {
                            Intent graph_intent = new Intent(getApplicationContext(), GraphView2.class);
                            graph_intent.putExtra("YAXIS", textTableHeaderParameter.getText().toString());
                            startActivity(graph_intent);
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(),
                                "Corrupted data found ", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }

    public File[] finder(File dir) {
        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".csv");
            }
        });
    }


    public boolean serachAllCSVFile() {
        file = finder(csvFilesDir);
        try {
            if (csvFilesDir.listFiles() == null || csvFilesDir.listFiles().length == 0) {
                Toast.makeText(getApplicationContext(),
                        "CSV File not available ", Toast.LENGTH_LONG).show();
                return false;
            } else {

                Arrays.sort(file, new Comparator<File>() {
                    public int compare(File f1, File f2) {
                        return Long.valueOf(f1.lastModified()).compareTo(
                                f2.lastModified());
                    }
                });

                ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                        this, R.layout.spinner_drop_down_selected);
                adapter.setDropDownViewResource(R.layout.spinner_drop_down);
                String shortFileName = "";
                files.clear();
                int numFiles = file.length;

                if (numFiles > 0) {
                    for (int i = file.length - 1; i >= 0; i--) {
                        shortFileName = file[i].getName();
                        files.add(new Item((i), shortFileName));
                        adapter.add(shortFileName);
                    }
                    spinnerFileName.setAdapter(adapter);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Exception occurred ", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void showListData(int RowData) {
        Variable.isFileFormatCorrupted = false;
        forFirstRecord = 0;
        try {
            list = new ArrayList<>();
            switch (RowData) {
                case CORRECTED: {

                    textTableHeaderParameter.setText(Variable.header_corr_water_level);
                    Constants.file_Text = Variable.header_corr_water_level;

                    for (int i = 0; i < date_time.length; i++) {
                        if (date_time[i] != null) {
                            try {
                                value = Float.parseFloat(corrected[i].trim());
                                Constants.graph_data[i] = value;
                                if (forFirstRecord == 0) {
                                    Constants.graph_max = value;
                                    Constants.graph_min = value;
                                    forFirstRecord++;
                                } else {
                                    if (Constants.graph_max < value) {
                                        Constants.graph_max = value;
                                    }
                                    if (Constants.graph_min > value) {
                                        Constants.graph_min = value;
                                    }
                                }
                            } catch (Exception ignored) {
                            }
                            list.add(new ViewDataModel(date_time[i], Constants.setDecimalDigits(corrected[i].trim())));
                        }
                    }
                    break;
                }
                case UNCORRECTED: {
                    textTableHeaderParameter.setText(Variable.header_uncom_water_level);
                    Constants.file_Text = Variable.header_uncom_water_level;
                    for (int i = 0; i < date_time.length; i++) {
                        if (date_time[i] != null) {
                            try {
                                value = Float.parseFloat(uncorrected[i].trim());
                                Constants.graph_data[i] = value;
                                if (forFirstRecord == 0) {
                                    Constants.graph_max = value;
                                    Constants.graph_min = value;
                                    forFirstRecord++;
                                } else {
                                    if (Constants.graph_max < value) {
                                        Constants.graph_max = value;
                                    }
                                    if (Constants.graph_min > value) {
                                        Constants.graph_min = value;
                                    }
                                }
                            } catch (Exception ignored) {
                            }

                            list.add(new ViewDataModel(date_time[i], Constants.setDecimalDigits(uncorrected[i])));
                        }
                    }
                    break;
                }
                case BAROMETER: {
                    textTableHeaderParameter.setText(Variable.header_barometric_pressure);
                    Constants.file_Text = Variable.header_barometric_pressure;
                    for (int i = 0; i < date_time.length; i++) {
                        if (date_time[i] != null) {
                            try {
                                value = Float.parseFloat(barometer[i].trim());
                                Constants.graph_data[i] = value;

                                if (forFirstRecord == 0) {
                                    Constants.graph_max = value;
                                    Constants.graph_min = value;
                                    forFirstRecord++;
                                } else {
                                    if (Constants.graph_max < value) {
                                        Constants.graph_max = value;
                                    }
                                    if (Constants.graph_min > value) {
                                        Constants.graph_min = value;
                                    }
                                }
                            } catch (Exception ignored) {
                            }

                            list.add(new ViewDataModel(date_time[i], Constants.setDecimalDigits(barometer[i], 1)));
                        }
                    }
                    break;
                }
                case BATT_VOLT: {
                    textTableHeaderParameter.setText(Variable.header_battery_voltage);
                    Constants.file_Text = Variable.header_battery_voltage;
                    for (int i = 0; i < date_time.length; i++) {
                        if (date_time[i] != null) {
                            try {
                                value = Float.parseFloat(battery_voltage[i].trim());
                                Constants.graph_data[i] = value;
                                if (forFirstRecord == 0) {
                                    Constants.graph_max = value;
                                    Constants.graph_min = value;
                                    forFirstRecord++;
                                } else {
                                    if (Constants.graph_max < value) {
                                        Constants.graph_max = value;
                                    }
                                    if (Constants.graph_min > value) {
                                        Constants.graph_min = value;
                                    }
                                }
                            } catch (Exception ignored) {
                            }

                            list.add(new ViewDataModel(date_time[i], Constants.setDecimalDigits(battery_voltage[i], 2)));
                        }
                    }
                    break;
                }

                case TEMPERATURE: {
                    textTableHeaderParameter.setText(Variable.header_temperature);
                    Constants.file_Text = Variable.header_temperature;
                    for (int i = 0; i < date_time.length; i++) {
                        if (date_time[i] != null) {
                            try {
                                value = Float.parseFloat(temperature[i].trim());
                                Constants.graph_data[i] = value;
                                if (forFirstRecord == 0) {
                                    Constants.graph_max = value;
                                    Constants.graph_min = value;
                                    forFirstRecord++;
                                } else {
                                    if (Constants.graph_max < value) {
                                        Constants.graph_max = value;
                                    }
                                    if (Constants.graph_min > value) {
                                        Constants.graph_min = value;
                                    }
                                }
                            } catch (Exception ignored) {
                            }

                            list.add(new ViewDataModel(date_time[i], Constants.setDecimalDigits(temperature[i], 2)));
                        }
                    }
                    break;
                }
            }
            adapter = new ViewDataAdapter(list);
            rv_view_data.setAdapter(adapter);
        } catch (Exception e) {
            Variable.isFileFormatCorrupted = true;
            e.printStackTrace();
        }
    }


    private void add_file_in_spinner() {

        try {

            if (csvFilesDir.listFiles().length == 0) {
                spinnerFileName.setPrompt("No File Available");
            } else {
                spinnerFileName.setPrompt("Choose a file");
            }
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                    this, R.layout.spinner_drop_down_selected);
            adapter.setDropDownViewResource(R.layout.spinner_drop_down);
            String shortFileName = "";
            int numFiles = file.length;
            if (numFiles > 0) {
                for (int i = file.length - 1; i >= 0; i--) {
                    shortFileName = file[i].getName();
                    adapter.add(shortFileName);
                }
                spinnerFileName.setAdapter(adapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void alert() {

        new AlertDialog.Builder(ViewFileDataTableActivity.this)
                .setTitle("Warning")
                .setIcon(R.drawable.warning_icon)
                .setMessage(
                        "Data is corrupted")
                .setPositiveButton(
                        "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog,
                                    int which) {
                            }
                        }).show();

    }

    public final int numberOfRecord(String fileName) {

        int num = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            while ((temp = br.readLine()) != null) {
                if (temp.length() > 2) {
                    num++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return num;
    }


    protected void getFileData(int position) {


        try {
            int dataLength;

            selectedFile = files.get(position).getFile();

            try {

                dataLength = numberOfRecord(absolutePath + File.separator + selectedFile);
                Constants.graph_length = dataLength;
                nextCounter = dataLength - 1;
                CsvReader products = new CsvReader(absolutePath + File.separator + selectedFile);


                date_time = new String[dataLength];
                battery_voltage = new String[dataLength];
                corrected = new String[dataLength];
                uncorrected = new String[dataLength];
                temperature = new String[dataLength];
                barometer = new String[dataLength];

                Constants.graph_date_time = new String[dataLength];
                Constants.graph_data = new float[dataLength];

                Variable.header_date_time = "Date / Time";
                Variable.header_corr_water_level = "Corr. Water Level (m)";
                Variable.header_uncom_water_level = "Uncom. Water Level (m)";
                Variable.header_battery_voltage = "Battery Voltage (V)";
                Variable.header_barometric_pressure = "Barometric Pressure";
                Variable.header_temperature = "Temperature (°C)";

                while (products.readRecord()) {
                    try {
                        datetime = products.get(1);

                        if (Tool.isHeader(datetime, 0, "dd/MM/yy HH:mm")) {

                            Variable.header_date_time = datetime;
                            Variable.header_battery_voltage = products.get(3);
                            Variable.header_temperature = products.get(4);
                            Variable.header_corr_water_level = products.get(5);
                            Variable.header_barometric_pressure = products.get(6);
                            Variable.header_uncom_water_level = products.get(7);

                            nextCounter--;
                            Constants.graph_length = Constants.graph_length - 1;
                        } else {

                            date_time[nextCounter] = datetime.trim();
                            battery_voltage[nextCounter] = products.get(3);
                            temperature[nextCounter] = products.get(4);
                            corrected[nextCounter] = products.get(5);
                            barometer[nextCounter] = products.get(6);
                            uncorrected[nextCounter] = products.get(7);
                            nextCounter--;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                spinner_parameterType_value[0] = Variable.header_corr_water_level;
                spinner_parameterType_value[1] = Variable.header_uncom_water_level;
                spinner_parameterType_value[2] = Variable.header_battery_voltage;
                spinner_parameterType_value[3] = Variable.header_barometric_pressure;
                spinner_parameterType_value[4] = Variable.header_temperature;

                arrayAdapter_parameterType = new ArrayAdapter<>(this,
                        R.layout.spinner_drop_down_selected, spinner_parameterType_value);
                arrayAdapter_parameterType.setDropDownViewResource(R.layout.spinner_drop_down);
                spinner_parameterType.setAdapter(arrayAdapter_parameterType);

                Constants.graph_date_time = date_time;

                System.gc();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            Variable.isFileFormatCorrupted = true;
            alert();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            System.gc();
            ViewFileDataTableActivity.this.finish();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
