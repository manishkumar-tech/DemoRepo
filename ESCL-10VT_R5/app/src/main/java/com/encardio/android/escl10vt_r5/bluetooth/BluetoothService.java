package com.encardio.android.escl10vt_r5.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.encardio.android.escl10vt_r5.constant.Constants;
import com.encardio.android.escl10vt_r5.tool.Variable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

// TODO: Auto-generated Javadoc

/*
 * @author Sandeep
 */
public class BluetoothService extends Service {

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    private static final String TAG = "BluetoothService";
    private static final boolean D = true;
    private static final String NAME = "BluetoothAdpt";

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static int mState;

    private final BluetoothAdapter mAdapter;

    private final Handler mHandler;
    private final String response = "";
    // connections
    Handler handler = new Handler();
    // connection
    /**
     * The accept thread.
     */
    private AcceptThread mAcceptThread;
    /**
     * The connect thread.
     */
    private ConnectThread mConnectThread;
    /**
     * The connected thread.
     */
    private ConnectedThread mConnectedThread;

    // device

    /**
     * Constructor. Prepares a new BluetoothAdpt session.
     *
     * @param context   The UI Activity Context
     * @param handler   A Handler to send messages back to the UI Activity
     * @param constants the constants
     */
    public BluetoothService(Context context, Handler handler,
                            Constants constants) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }

    /**
     * Return the current connection state.
     *
     * @return the state
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Set the current state of the chat connection.
     *
     * @param state An integer defining the current connection state
     */
    // private synchronized void setState(int state) {
    public synchronized void setState(int state) {
        if (D)
            Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(BluetoothAdpt.MESSAGE_STATE_CHANGE, state, -1)
                .sendToTarget();
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        if (D)
            Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(STATE_LISTEN);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) throws IOException {
        if (D)
            Log.d(TAG, "connect to: " + device);
        try {
            // Cancel any thread attempting to make a connection
            if (mState == STATE_CONNECTING) {
                if (mConnectThread != null) {
                    mConnectThread.cancel();
                    mConnectThread = null;
                }
            }
            // Cancel any thread currently running a connection
            if (mConnectedThread != null) {
                mConnectedThread.cancel();
                mConnectedThread = null;
            }
            // Start the thread to connect with the given device
            try {
                mConnectThread = new ConnectThread(device);
                mConnectThread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            setState(STATE_CONNECTING);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection.
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket,
                                       BluetoothDevice device) {
        if (D)
            Log.d(TAG, "connected");
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        // Cancel the accept thread because we only want to connect to one
        // device
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(BluetoothAdpt.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothAdpt.DEVICE_NAME, device.getName());
        bundle.putBoolean("DEVICE_CONNECTING", true);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        setState(STATE_CONNECTED);

    }

    /**
     * Stop all threads.
     */
    public synchronized void stop() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mAcceptThread != null) {
            mAcceptThread.interrupt();
            mAcceptThread = null;
        }
        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner.
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        try {
            // Create temporary object
            ConnectedThread r;
            // Synchronize a copy of the ConnectedThread
            synchronized (this) {
                if (mState != STATE_CONNECTED)
                    return;
                r = mConnectedThread;
            }
            // Perform the write unsynchronized
            r.write(out);
        } catch (Exception e) {
            e.printStackTrace();
            Message msg = mHandler.obtainMessage(BluetoothAdpt.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            // bundle.putBoolean("CONNECTION_FAILED", true);
            bundle.putString(BluetoothAdpt.TOAST,
                    "Bluetooth Communication Failed...");
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }

    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(STATE_LISTEN);
        // Constants.bluetoothService = null;
        Constants.BLUETOOTH_ADDRESS = null;
        Constants.BLUETOOTH_DEVICE = null;
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(BluetoothAdpt.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putBoolean("CONNECTION_FAILED", true);
        bundle.putString(BluetoothAdpt.TOAST, "Unable to connect to device...");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() throws IOException {
        Variable.isConnected = false;
        setState(STATE_LISTEN);
        Constants.bluetoothService = null;
        Constants.BLUETOOTH_ADDRESS = null;
        Constants.BLUETOOTH_DEVICE = null;
        Constants.isNewBluetoothConnection = false;
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(BluetoothAdpt.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString("toast", "Bluetooth connection lost...");
        bundle.putBoolean("CONNECTION_LOST", true);
        msg.setData(bundle);
        mHandler.sendMessage(msg);

    }

    @Override
    public IBinder onBind(Intent arg0) {

        return null;
    }

    private class AcceptThread extends Thread {

        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            // Create a new listening server socket
            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmServerSocket = tmp;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Thread#run()
         */
        public void run() {
            if (D)
                Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null;
            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {

                try {

                    if (mmServerSocket != null) {
                        // This is a blocking call and will only return on a
                        // successful connection or an exception
                        socket = mmServerSocket.accept();
                    } else
                        break;
                } catch (IOException e) {

                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                                Constants.bluetoothService = null;
                                Constants.BLUETOOTH_ADDRESS = null;
                                Constants.BLUETOOTH_DEVICE = null;
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate
                                // new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }
                }
            }
            if (D)
                Log.i(TAG, "END mAcceptThread");
        }

        /**
         * Cancel Connection by closing bluetooth socket.
         */
        public void cancel() {
            if (D)
                Log.d(TAG, "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection with a
     * device. It runs straight through; the connection either succeeds or
     * fails.
     */
    private class ConnectThread extends Thread {
        /**
         * The mm socket.
         */
        private final BluetoothSocket mmSocket;
        /**
         * The mm device.
         */
        private final BluetoothDevice mmDevice;

        /**
         * Instantiates a new connect thread.
         *
         * @param device the device
         */
        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Thread#run()
         */
        public void run() {

            setName("ConnectThread");
            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();

            } catch (IOException e) {
                e.printStackTrace();
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                    BluetoothService.this.start();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                // Start the service over to restart listening mode

                return;
            }
            // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }
            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        /**
         * Cancel Connection by closing bluetooth socket.
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * This thread runs during a connection with a remote device. It handles all
     * incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        /**
         * The mm socket.
         */
        private final BluetoothSocket mmSocket;
        /**
         * The mm in stream.
         */
        private final InputStream mmInStream;
        /**
         * The mm out stream.
         */
        private final OutputStream mmOutStream;
        //String tempDownloadedData = "";
        public int tempcount = 0;
        boolean replyReceived = false;
        String temp;
        // /** The fc. */
        // FileChannel fc;
        boolean isCompleteMsgRcvd = false;

        public ConnectedThread(BluetoothSocket socket) {
            // Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {

            int bufferSize = 4096;
            byte[] buffer = new byte[bufferSize];
            StringBuffer replyMsgBuffer = new StringBuffer(); // local buffer
            // for storing
            // reply msg.
            String replyMsg = ""; // to store reply msg
            int len = -1;

            boolean stopConnectedThread = false;
            Log.e("BLUETOOTH", "" + Constants.sc);
            while (!stopConnectedThread) {
                try {

                    len = mmInStream.read(buffer);

                    while (((len > -1)) || (isCompleteMsgRcvd)) {
                        try {
                            temp = "";
                            if (isCompleteMsgRcvd == false) {
                                replyMsg = new String(buffer, 0, len);

                                replyMsgBuffer.append(replyMsg);
                            }
                            isCompleteMsgRcvd = replyMsgBuffer.toString().contains(
                                    Constants.checkEndChar) == true;
                            if (isCompleteMsgRcvd == true) {
                                len = -1;
                                String tempReply = "";
                                tempReply = replyMsgBuffer
                                        .substring(
                                                0,
                                                replyMsgBuffer
                                                        .indexOf(Constants.checkEndChar));

                                temp = replyMsgBuffer.substring(replyMsgBuffer
                                                .indexOf(Constants.checkEndChar) + 1,
                                        replyMsgBuffer.length());
                                if (tempReply.contains("$")) {
                                    Constants.sc = Integer
                                            .parseInt(tempReply.substring(
                                                    tempReply.indexOf("$") + 1,
                                                    tempReply.indexOf("$") + 5));

                                    Log.d("STC ", "" + Constants.sc);
                                    if (Constants.sc == Constants.RECORD_AVAILABLE_STATUS) {
                                        Constants.countRecords++;
                                        Constants.download_Watchdog_Timer = System
                                                .currentTimeMillis();
                                    }

                                    Constants.reply = "";
                                    if ((tempReply.substring(
                                            tempReply.indexOf("$") + 1).length()) > 4) {

                                        tempReply = tempReply.replace("\n", "")
                                                .trim();

                                        Constants.reply = (tempReply.substring(
                                                tempReply.indexOf("$") + 6)).trim();
                                        Log.d("RLY ", "" + Constants.reply);

                                        if (Constants.sc == Constants.RECORD_AVAILABLE_STATUS) {
                                            char postchar_CR, postchar_LF;
                                            postchar_CR = 0x0D;
                                            postchar_LF = 0x0A;
                                            Constants.reply = (Constants.reply
                                                    + postchar_CR + postchar_LF);
                                            Constants.dataDownloadAvailble = true;
                                            tempcount++;
                                            if (tempcount >= 1000) {
                                                Constants.downloadData = Constants.downloadData
                                                        + Constants.tempDownloadedData + Constants.reply;
                                                tempcount = 0;
                                                Constants.tempDownloadedData = "";
                                            } else {
                                                Constants.tempDownloadedData = Constants.tempDownloadedData
                                                        + Constants.reply;
                                            }
                                        }
                                    }

                                    if (Constants.sc == Constants.RECORD_DOWNLOADING_COMPLETED_STATUS) {
                                        Constants.downloadData = Constants.downloadData
                                                + Constants.tempDownloadedData;
                                    }

                                    Constants.gotReply = true;
                                    replyReceived = true;

                                    if (Constants.sc == Constants.RECORD_AVAILABLE_STATUS) {
                                        Constants.dataDownloadAvailble = true;
                                    }
                                    if (Constants.sc == Constants.RECORD_DOWNLOADING_COMPLETED_STATUS) {
                                        Constants.sc = Constants.OK_STATUS;
                                        Constants.dataDownloadCompleted = true;
                                    }
                                    if (Constants.sc == Constants.RECORD_UNAVAILABLE_STATUS) {
                                        Constants.responseMsg = Constants.sc + ": Record not found !!";
                                    }
                                    if (Constants.sc == Constants.INVALID_COMMAND_ERROR) {
                                        Constants.responseMsg = Constants.sc + ": Invalid command !!";
                                    }
                                    if (Constants.sc == Constants.STRING_FORMAT_ERROR) {
                                        Constants.responseMsg = Constants.sc + ": Invalid String format !!";
                                    }
                                    if (Constants.sc == Constants.DATA_SIZE_ERROR) {
                                        Constants.responseMsg = Constants.sc + ": Invalid data size !!";
                                    }
                                    if (Constants.sc == Constants.COMMAND_PACKET_SIZE_ERROR) {
                                        Constants.responseMsg = Constants.sc + ": Invalid packet size !!";
                                    }
                                    if (Constants.sc == Constants.DATA_NULL_ERROR) {
                                        Constants.responseMsg = Constants.sc + ": Data size found NULL !!";
                                    }
                                    if (Constants.sc == Constants.RTC_DATE_TIME_ERROR) {
                                        Constants.responseMsg = Constants.sc + ": RTC error !!";
                                    }
                                    if (Constants.sc == Constants.INVALID_DATA_ERROR) {
                                        Constants.responseMsg = Constants.sc + " :Invalid data !!";
                                    }
                                    if (Constants.sc == Constants.MODEM_SIGNAL_ERROR) {
                                        Constants.responseMsg = Constants.sc + ": Unable to read modem signal !!";
                                    }
                                    if (Constants.sc == Constants.MODEM_BAUDRATE_SET_ERROR) {
                                        Constants.responseMsg = Constants.sc + "Unable to set modem baud rate !!";
                                    }
                                    if (Constants.sc == Constants.LOG_INTERVAL_UNDERFLOW_ERROR) {
                                        Constants.responseMsg = Constants.sc + ": Log interval less than 5 sec !!";
                                    }
                                    if (Constants.sc == Constants.DATA_OUT_OF_RANGE_ERROR) {
                                        Constants.responseMsg = Constants.sc + ": Data value out of range !!";
                                    }
                                    if (Constants.sc == Constants.DATA_RECORD_CORRUPTED_ERROR) {
                                        Constants.responseMsg = Constants.sc + ": Correpted record found !!";
                                    }
                                    if (Constants.sc == Constants.MODEM_TRAP_MODE_ERROR) {
                                        Constants.responseMsg = Constants.sc + ": Unable to change Modem trap mode !!";
                                    }
                                    if (Constants.sc == Constants.MODEM_POWER_ON_ERROR) {
                                        Constants.responseMsg = Constants.sc + ": Unable to turn on Modem power !!";
                                    }
                                    if (Constants.sc == Constants.MODEM_CME_MSG_FORMAT_ERROR) {
                                        Constants.responseMsg = Constants.sc + ": Modem command AT+CMEE=1 !!";
                                    }
                                    if (Constants.sc == Constants.NETWORK_FORMAT_ERROR) {
                                        Constants.responseMsg = Constants.sc + " Modem command AT+CREG=1 !!";
                                    }
                                    if (Constants.sc == Constants.MODEM_FLOW_CONTROL_ERROR) {
                                        Constants.responseMsg = Constants.sc + ": Modem command AT\\Q3 !!";
                                    }
                                    if (Constants.sc == Constants.MODEM_CHAR_SET_ERROR) {
                                        Constants.responseMsg = Constants.sc + ": modem command AT+CSCS=\"GSM\" !!";
                                    }
                                    if (Constants.sc == Constants.MODEM_MSG_SERVICE_ERROR) {
                                        Constants.responseMsg = Constants.sc + ": Modem command AT+CSMS=1 !!";
                                    }
                                    if (Constants.sc == Constants.SMS_OVER_FLOW_ERROR) {
                                        Constants.responseMsg = Constants.sc + ": Modem command AT^SMGO=1 !!";
                                    }
                                    if (Constants.sc == Constants.SMS_STORAGE_ERROR) {
                                        Constants.responseMsg = Constants.sc + ": Modem command AT+CPMS=\"MT\",\"MT\",\"MT\" !!";
                                    }
                                    if (Constants.sc == Constants.SMS_MSG_FORMAT_ERROR) {
                                        Constants.responseMsg = Constants.sc + ": Modem command AT+CMGF=1 !!";
                                    }
                                    if (Constants.sc == Constants.SMS_TEXT_PARA_ERROR) {
                                        Constants.responseMsg = Constants.sc + ": Modem command AT+CSDH=1 !!";
                                    }
                                    if (Constants.sc == Constants.SAVE_SETTINGS_ERROR) {
                                        Constants.responseMsg = Constants.sc + ": Modem command AT&W !!";
                                    }
                                    if (Constants.sc == Constants.MODEM_DISABLE_STATUS) {
                                        Constants.responseMsg = Constants.sc + ": Modem not found !!";
                                    }
                                    if (Constants.sc == Constants.MODEM_SIM_UNAVAILABLE_STATUS) {
                                        Constants.responseMsg = Constants.sc + ": Sim card not found !!";
                                    }
                                    if (Constants.sc == Constants.MODEM_OPERATING_MODE_OFF_STATUS) {
                                        Constants.responseMsg = Constants.sc + ": Modem operating mode OFF !!";
                                    }
                                    if (Constants.sc == Constants.BAROMETER_DISABLE_STATUS) {
                                        Constants.responseMsg = Constants.sc + ": Barometer not found !!";
                                    }
                                    if (Constants.sc == Constants.BATTERY_DEAD_STATUS) {
                                        Constants.battery_low = true;
                                        Constants.responseMsg = Constants.sc + ": Battery low detected !!";
                                    }
                                    if (Constants.sc == Constants.MODEM_COMM_MODE_GPRS_STATUS) {
                                        Constants.responseMsg = Constants.sc + ": Datalogger is buzy in uploading data to Remote FTP server. Please retry after few minutes!!";//GPRS communication mode
                                    }
                                    if (Constants.sc == Constants.MODEM_OPERATING_MODE_ON_STATUS) {
                                        Constants.responseMsg = Constants.sc + ": Modem operating mode ON !!";
                                    }
                                }
                                replyMsgBuffer.delete(0,
                                        replyMsgBuffer.length());
                                replyMsgBuffer.setLength(0);
                                replyMsgBuffer.append(temp);

                            } else {
                                len = mmInStream.read(buffer);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    stopConnectedThread = true;
                    Constants.gotReply = false;
                    try {
                        connectionLost();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    BluetoothService.this.start();
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(BluetoothAdpt.MESSAGE_WRITE, -1, -1,
                        buffer).sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
