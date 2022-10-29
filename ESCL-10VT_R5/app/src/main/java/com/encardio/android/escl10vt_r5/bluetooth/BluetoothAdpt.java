package com.encardio.android.escl10vt_r5.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.encardio.android.escl10vt_r5.activity.DeviceListActivity;
import com.encardio.android.escl10vt_r5.activity.R;
import com.encardio.android.escl10vt_r5.constant.Constants;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Sandeep
 * This is the main Activity that displays the current commands and their reply.
 */
public class BluetoothAdpt extends AppCompatActivity {
    // Debugging
    /**
     * Message types sent from the BluetoothService Handler.
     */
    public static final int MESSAGE_STATE_CHANGE = 1;
    /**
     * The Constant MESSAGE_READ.
     */
    public static final int MESSAGE_READ = 2;
    /**
     * The Constant MESSAGE_WRITE.
     */
    public static final int MESSAGE_WRITE = 3;
    /**
     * The Constant MESSAGE_DEVICE_NAME.
     */
    public static final int MESSAGE_DEVICE_NAME = 4;
    /**
     * The Constant MESSAGE_TOAST.
     */
    public static final int MESSAGE_TOAST = 5;
    public static final int CONNECTION_BREAK = 6;
    /**
     * Key names received from the BluetoothService Handler
     */
    public static final String DEVICE_NAME = "device_name";
    /**
     * The Constant TOAST.
     */
    public static final String TOAST = "toast";
    /**
     * The Constant REQUEST_CONNECT_DEVICE.
     */
    public static final int REQUEST_CONNECT_DEVICE = 1;
    /**
     * The Constant TAG.
     */
    private static final String TAG = "BluetoothChat";
    // Intent request codes
    /**
     * The Constant D.
     */
    private static final boolean D = true;
    /**
     * The Constant REQUEST_ENABLE_BT.
     */
    private static final int REQUEST_ENABLE_BT = 2;
    // Layout Views
    /**
     * The title.
     */
    private TextView mTitle;
    /**
     * The conversation view.
     */
    private ListView mConversationView;
    /**
     * The out edit text.
     */
    private EditText mOutEditText;
    /**
     * The send button.
     */
    private Button mSendButton;
    // Name of the connected device
    /**
     * The connected device name.
     */
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    /**
     * The conversation array adapter.
     */
    private ArrayAdapter<String> mConversationArrayAdapter;
    /**
     * The handler.
     */
    private final Handler mHandler = new Handler() {
        /*
         * (non-Javadoc)
         *
         * @see android.os.Handler#handleMessage(android.os.Message)
         */
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (D)
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            mTitle.setText(R.string.title_connected_to);
                            mTitle.append(mConnectedDeviceName);
                            mConversationArrayAdapter.clear();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            mTitle.setText(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            mTitle.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mConversationArrayAdapter.add("Command:  " + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    boolean cutString = false;
                    String actualCommandReply = "";
                    // String readMessage = new String(readBuf, 0, msg.arg1);
                    String readMessage = new String(readBuf);
                    Toast.makeText(getApplicationContext(), " readMessage in BluetoothAdpt:  " + readMessage,
                            Toast.LENGTH_SHORT).show();
                    for (int i = 0; i < readMessage.length(); i++) {
                        if ((readMessage.charAt(i) == 0x0D) && (readMessage.charAt(i + 1) == 0x0A)) { // up to
                            // cr(0x0D)lf(0x0A)
                            cutString = true;
                            break;
                        }
                    }
                    if (cutString == true) {
                        int msgLength = readMessage.indexOf(0x0D);
                        actualCommandReply = readMessage.substring(0, msgLength);// up to cr(0x0d) and
                        // removed char for
                        // cr(0x0D)lf(0x0A)EAN-26>
                    }
                    mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + actualCommandReply);
                    actualCommandReply = null;
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT)
                            .show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;


            }
        }
    };
    // Array adapter for Pairing devices
    /**
     * The log array list.
     */
    private ArrayList<String> logArrayList;
    // String buffer for outgoing messages
    /**
     * The array adapter.
     */
    private ArrayAdapter<String> mArrayAdapter;
    // Local Bluetooth adapter
    /**
     * The out string buffer.
     */
    private StringBuffer mOutStringBuffer;
    /**
     * The write listener.
     */
    private final TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the
            // message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            if (D)
                Log.i(TAG, "END onEditorAction");
            return true;
        }
    };
    /**
     * The bluetooth adapter.
     */
    private BluetoothAdapter mBluetoothAdapter = null;
    /**
     * The check_reply.
     */
    private Constants check_reply; // = (CheckReply)getApplicationContext();

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (D)

            // Set up the window layout

//		setContentView(R.layout.main_bluetooth);
            check_reply = new Constants();
        // Set up the custom title

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available...", Toast.LENGTH_LONG).show();
            finish();
            return;
        }


    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onStart()
     */
    @Override
    public void onStart() {
        super.onStart();
        if (D)

            // If BT is not on, request that it be enabled.
            // setupChat() will then be called during onActivityResult
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                // Otherwise, setup the chat session
            } else {
            /*if(Constants.bluetoothService == null)
				setupChat();*/
            }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onResume()
     */
    @Override
    public synchronized void onResume() {
        super.onResume();
        if (D)


            if (Constants.bluetoothService != null) {
                // Only if the state is STATE_NONE, do we know that we haven't
                // started already
                if (Constants.bluetoothService.getState() == BluetoothService.STATE_NONE) {
                    // Start the Bluetooth chat services
                    Constants.bluetoothService.start();
                }
            }
    }


    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onPause()
     */
    @Override
    public synchronized void onPause() {
        super.onPause();

    }


    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onStop()
     */
    @Override
    public void onStop() {
        super.onStop();

    }

    // The action listener for the EditText widget, to listen for the return key

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onDestroy()
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (Constants.bluetoothService != null)
            Constants.bluetoothService.stop();

    }
    // The Handler that gets information back from the BluetoothService

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (Constants.bluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothService to write
            byte[] send = message.getBytes();
            Constants.bluetoothService.write(send);
            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D)
            Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    try {
                        Constants.bluetoothService.connect(device);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    //	setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    // finish();
                }
        }
    }


    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return false;
    }
}