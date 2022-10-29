package com.encardio.android.escl10vt_r5.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.encardio.android.escl10vt_r5.constant.Constants;

import org.apache.commons.net.ftp.FTPClient;

// 

/**
 * The Class URL_SettingsActivity.
 *
 * @author Sandeep
 * <p>
 * Connectivity with FTP server and checks the credentials if valid
 * credentials then give permission to update the url settings like
 * update url and user name and password
 */
public class URL_SettingsActivity extends AppCompatActivity implements Runnable {
    // create a progress dialog
    /**
     * The handler.
     */
    private final Handler handler = new Handler();
    // the handler object is created to update the UI thread from another one
    /**
     * The is connection failed.
     */
    protected boolean isConnectionFailed;
    // Thread for new FTP connection
    /**
     * The thread.
     */
    Thread thread;
    // To Check the credentials
    /**
     * The dialog.
     */
    private ProgressDialog dialog;
    // To check the connection
    /**
     * The wrong credentials.
     */
    private boolean wrongCredentials;
    // Edit text references to edit the url settings
    /**
     * The edit url.
     */
    private EditText editUrl;
    /**
     * The edit port.
     */
    private EditText editPort;
    /**
     * The edit user.
     */
    private EditText editUser;
    /**
     * The edit password_of_ url.
     */
    private EditText editPassword_of_Url;
    // To get edit text information to update the url settings
    /**
     * The edit text url.
     */
    private String editTextUrl;
    /**
     * The edit text port.
     */
    private String editTextPort;
    /**
     * The edit text user name.
     */
    private String editTextUserName;
    /**
     * The edit text password.
     */
    private String editTextPassword;
    private String editcnfirmPasswrd;
    private EditText edittextcnfirmPas;

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState the saved instance state
     */
    @SuppressLint("DefaultLocale")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.url_settings);
        SharedPreferences prefUrlSettings = URL_SettingsActivity.this
                .getSharedPreferences("URL_SETTINGS", 0);
        Constants.url = prefUrlSettings.getString("URL", Constants.url);
        Constants.port = prefUrlSettings.getInt("PORT", Constants.port);
        Constants.userName = prefUrlSettings.getString("USERNAME",
                Constants.userName);
        Constants.password = prefUrlSettings.getString("PASSWORD",
                Constants.password);
        editUrl = (EditText) findViewById(R.id.editUrl);
        editPort = (EditText) findViewById(R.id.editPort);
        editUser = (EditText) findViewById(R.id.editUser);
        editPassword_of_Url = (EditText) findViewById(R.id.editPassword_of_Url);
        edittextcnfirmPas = (EditText) findViewById(R.id.editTextConfirm);
        editUrl.setText(Constants.url);
        editPort.setText(String.format("%d", Constants.port));
        editUser.setText(Constants.userName);

        // editPassword_of_Url.setText(Constants.password);

        findViewById(R.id.buttonSaveOnUrlSettings).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        try {
                            getUrlSettingsParameters();
                            if (editcnfirmPasswrd.equals(editTextPassword)) {


                                if (Constants.validateFTP_URL_AND_Password(editUser.getText().toString())) {
                                    if (Constants.validateFTP_URL_AND_Password(edittextcnfirmPas.getText().toString())) {

                                        initProgressDialog();
                                        dialog.show();
                                        // starts a new thread that will execute all the
                                        // code
                                        // inside the run() method.
                                        thread = new Thread(URL_SettingsActivity.this);
                                        thread.start();


                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                "The password should contain only Upper(A-Z), lower(a-z) characters and numbers(0-9)", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            "The FTP User should contain only Upper(A-Z), lower(a-z) characters and numbers(0-9)", Toast.LENGTH_LONG).show();
                                }


                            } else
                                Toast.makeText(getApplicationContext(),
                                        "Password should be same..", Toast.LENGTH_LONG)
                                        .show();

                        } catch (Exception ignored) {
                        }
                    }
                });
        findViewById(R.id.buttonCancelOnUrlSettings).setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(),
                                UploadFilesActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
    }

    protected void getUrlSettingsParameters() {
        editTextUrl = (editUrl).getText().toString();
        editTextPort = (editPort).getText().toString();
        editTextUserName = (editUser).getText().toString();
        editTextPassword = (editPassword_of_Url).getText().toString();
        editcnfirmPasswrd = (edittextcnfirmPas).getText().toString();
    }

    private void initProgressDialog() {
        // create a dialog
        dialog = new ProgressDialog(this);
        // set the title of the dialog
        dialog.setTitle("Checking Credentials");
        // Set if the dialog can be skipped
        dialog.setCancelable(false);
        // Set if the dialog doesn't have a estimated time to be dismissed
        // dialog.setIndeterminate(true);
        dialog.setMessage("	Please Wait.....");
    }

    @Override
    public void run() {
        // upload the files
        try {
            checkCredentials();
        } catch (Exception ignored) {
        }
        // update the canvas from this thread using the Handler object
        handler.post(new Runnable() {
            @Override
            public void run() {

                if (thread != null) {
                    thread.interrupt();
                    thread = null;
                }
                if (isConnectionFailed) {
                    dialog.dismiss();
                    Toast.makeText(
                            URL_SettingsActivity.this.getApplicationContext(),
                            "Unable to connect to server!!!", Toast.LENGTH_LONG)
                            .show();
                    Toast.makeText(
                            URL_SettingsActivity.this.getApplicationContext(),
                            "Please check Network, URL and Port.",
                            Toast.LENGTH_LONG).show();

                } else if (wrongCredentials) {
                    dialog.dismiss();
                    Toast.makeText(
                            URL_SettingsActivity.this.getApplicationContext(),
                            "Username or password is incorrect!!!",
                            Toast.LENGTH_LONG).show();
                } else {
                    dialog.dismiss();
                    Toast.makeText(
                            URL_SettingsActivity.this.getApplicationContext(),
                            "URL settings updated successfully ",
                            Toast.LENGTH_LONG).show();
                    saveUrlSettings();
                }
            }
        });
    }

    protected void checkCredentials() {
        isConnectionFailed = false;
        String url = editTextUrl;
        int port = Integer.parseInt(editTextPort);
        String userName = editTextUserName;
        String password = editTextPassword;
        try {
            FTPClient con = new FTPClient();
            try {
                con.setConnectTimeout(30000); // 1 minute
                con.connect(url, port);

            } catch (Exception e1) {
                isConnectionFailed = true;
                return;
            }
            try {
                wrongCredentials = !con.login(userName, password);
                con.logout();
                con.disconnect();
            } catch (Exception ignored) {
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * updates the URl settings.
     */
    protected void saveUrlSettings() {
        SharedPreferences.Editor prefUrlSettings = URL_SettingsActivity.this
                .getSharedPreferences("URL_SETTINGS", 0).edit();
        prefUrlSettings.putString("URL", editTextUrl);
        prefUrlSettings.putInt("PORT", Integer.parseInt(editTextPort));
        prefUrlSettings.putString("USERNAME", editTextUserName);
        prefUrlSettings.putString("PASSWORD", editTextPassword);
        prefUrlSettings.apply();
        Intent intent = new Intent(getApplicationContext(),
                UploadFilesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}