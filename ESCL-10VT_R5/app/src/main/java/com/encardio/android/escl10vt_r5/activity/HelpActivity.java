package com.encardio.android.escl10vt_r5.activity;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.encardio.android.escl10vt_r5.service.AdjustScreen;

/**
 * @author Sandeep
 */
public class HelpActivity extends ListActivity {
    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    private AdjustScreen objAdjustScreen = null;
    private int height, width;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use our own list adapter

        objAdjustScreen = new AdjustScreen(HelpActivity.this);
        height = objAdjustScreen.getHeightOfScreen();
        width = objAdjustScreen.getWidthOfScreen();

        setListAdapter(new SpeechListAdapter(this));
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        ((SpeechListAdapter) getListAdapter()).toggle(position);
    }

    /**
     * A sample ListAdapter that presents content from arrays of speeches and text.
     */
    private class SpeechListAdapter extends BaseAdapter {
        /**
         * Remember our context so we can use it when constructing views.
         */
        private final Context mContext;
        /**
         * Our data, part 1.
         */
        private final String[] mTitles = {"1.	Unable to connect data logger over Bluetooth", "2.	Bluetooth connection Failure",
                "3.	System Information", "4.	Monitor Data", "5.	Scanning", "6.	Downloading logged data",
                "7.	Viewing Downloaded Data", "8.	Uploading files", "9.	Files not uploading on FTP server",
                "10. Files storage on SD card"};
        /**
         * Our data, part 2.
         */
        private final String[] mDialogue = {
                // "1.	Establishing Bluetooth connection",
                "		Check that mobile phone's Bluetooth function" + "\n\t\tis enabled.\n"
                        /*+ "\n\t\tlogger."
						+ " \n\t\tTurn ON the Bluetooth modem."
						+ " \n\t\tEnable Bluetooth connection from"
						+ " \n\t\tphones wireless settings.\n"*/
                        + "		Check that Bluetooth modem dongle is"
                        + "\n\t\tconnected to datalogger and is turned"
                        + "\n\t\tON.\n"
                        + "		Check if distance between datalogger and"
                        + "\n\t\tmobile phone is within Bluetooth range (less "
                        + "\n\t\tthan 5 meters).\n"
						/*+ " \n\t\tin Bluetooth Device list on Phone�s screen"
						+ "  \n\t\tand select it for pairing.\n"*/
                        + "		Check if datalogger's Bluetooth modem is "
                        + "\n\t\tproperly paired with mobile phone.\n"
                        /*+ " \n\t\tproviding passkey = 69836776\n"*/
                        + "		Check that Bluetooth modems battery is not "
                        + "\n\t\tdischarged.\n"
                        /*+ " \n\t\tready to use.\n"*/
                        + "		Check if datalogger's Bluetooth modem is"
                        + "\n\t\tconfigured correctly. Baud rate should "
                        + "\n\t\tbe 115200 and hardware flow control should"
                        + "\n\t\tbe OFF.\n"
                        + "		Turn OFF the Bluetooth modem then turn "
                        + "\n\t\tin ON after a few seconds. "
                        + "\n\t\tReconnect mobile phone to datalogger.\n"
                        + "		Reset data logger by shorting RST jumper "
                        + "\n\t\tonce using tweezers near data loggers "
                        + "\n\t\tserial port connector.\n",
                // "2.	Bluetooth connection Failure",
                "		Phones Bluetooth may not be Enabled.\n"
                        + "		Bluetooth modem may not be properly "
                        + " \n\t\tinserted into Data logger socket.\n"
                        + "		Bluetooth modem may not be paired with"
                        + " \n\t\tphone.\n"
                        + "		Bluetooth modem may be out of Bluetooth "
                        + " \n\t\trange from Phone.\n"
                        + "		Bluetooth modems battery may be \n\t\tdischarged.\n"
                        + "		Data logger battery may be discharged.\n",
                // 3. System Information
                "		Data logger Info. ,Sensor information,"
                        + " \n\t\tSampling, Battery, Bluetooth modem "
                        + " \n\t\tand Phone information can be seen on"
                        + " \n\t\tSystem Information screen.\n ",
                // 4. Monitor Data
                "		 Data can be monitored from Monitor"
                        + " \n\t\tParameter menu.\n"
                        + "		This feature is mostly used for diagnostic" + " \n\t\tpurpose.\n"
                        /*+ "		Parameters can be monitored from " + " \n\t\tMonitor Parameter menu.\n"*/
                        + "		This feature is used to check sensor data" + " \n\t\tor connectivity.\n"
                        + "		Monitor log interval can be set by editing"
                        + " \n\t\tmonitor interval from monitor screen.\n",
                // "5. Scanning
                "		Data logger scanning can be started or"
                        + " \n\t\tstopped using Scan screen.\n"
                        + "		Logging interval can be set using Scan \n\t\tscreen.\n"
                        + "		Data logger memory full action can be set.\n"
                        + "		Data logger storage memory can be erased.\n",
                // "6. Downloading logged data
                "		Logged data can be downloaded into phone "
                        + " \n\t\tusing Download Data option from"
                        + " \n\t\tScan screen-->Logger memory option"
                        + " \n\t\t-->Download Data.\n"
                        + "		Press Download & Save button from "
                        + " \n\t\tDownload screen to download "
                        + " \n\t\tand save logged data into phones memory.\n"
                        + "		Downloaded data are saved in CSV format"
                        + " \n\t\tat 'ESCL10VTR5 Files' folder on SD card.\n",
                // 7. Viewing Downloaded Data
                "		Downloaded logged data can be viewed by"
                        + " \n\t\tpressing View Data button from Home "
                        + " \n\t\tscreen or from download.\n"
                        + "		Select file and choose parameter to view "
                        + " \n\t\tlogged data at View Data screen.\n",
                // "8. Uploading files
                "		Data logger files can be uploaded to remote"
                        + " \n\t\tfolder on FTP server using 'Upload Files' menu"
                        + " \n\t\toption.\n"
                        + "		Type URL settings carefully while resetting the"
                        + " \n\t\tURL settings.\n",
                // "9.	Files not uploading on FTP server",
                "		Check if internet connection is available and"
                        + " \n\t\t working properly.\n"
                        + "		Check whether URL and port settings are"
                        + " \n\t\tcorrectly configured.\n"
                        + "		Check GSM/GPRS signal strength on 'monitor"
                        + " \n\t\tparameter' screen.\n"
                        + "		Check Battery voltage and health on 'system"
                        + " \n\t\tinfo' screen.\n"
                        + "		Check if GPRS service is not deactivated by "
                        + " \n\t\tSIM card service provider for any reason.\n ",
                // 10. Files storage on SD card
                "		Downloaded data are saved in CSV format at"
                        + " \n\t\t'ESCL10VTR5 Files' folder on SD card.\n"};
        /**
         * Our data for small screen 320 X 480, part 2.
         */
        private final String[] mDialogueSmallScreen = {
                // "1.	Unable to connect data logger over Bluetooth",
                "		Check that mobile phone's Bluetooth function is enabled.\n"
						/*+ "\n\t\tlogger."
						+ " \n\t\tTurn ON the Bluetooth modem."
						+ " \n\t\tEnable Bluetooth connection from"
						+ " \n\t\tphone wireless settings.\n"*/
                        + "		Check that Bluetooth modem dongle is connected to datalogger and is turned ON.\n"
                        + "		Check if distance between datalogger and mobile phone is within Bluetooth range (less than 5 meters).\n"
						/*+ " \n\t\tin Bluetooth Device list on Phones screen"
						+ "  \n\t\tand select it for pairing.\n"*/
                        + "		Check if datalogger's Bluetooth modem is properly paired with mobile phone.\n"
                        /*+ " \n\t\tproviding passkey = 69836776\n"*/
                        + "		Check that Bluetooth modems battery is not discharged.\n"
                        /*+ " \n\t\tready to use.\n"*/
                        + "		Check if datalogger's Bluetooth modem is configured correctly. Baud rate should be 115200 and hardware flow control should be OFF.\n"
                        + "		Turn OFF the Bluetooth modem then turn in ON after a few seconds. Reconnect mobile phone to datalogger.\n"
                        + "		Reset data logger by shorting RST jumper once using tweezers near data loggers serial port connector.\n",
                // "2.	Bluetooth connection Failure",
                "		Phones Bluetooth may not be Enabled.\n"
                        + "		Bluetooth modem may not be properly "
                        + " \n\t\tinserted into Data logger socket.\n"
                        + "		Bluetooth modem may not be paired with"
                        + " \n\t\tphone.\n" + "		Bluetooth modem may be out of "
                        + " \n\t\tBluetooth range from Phone.\n"
                        + "		Bluetooth modems battery may be \n\t\tdischarged.\n"
                        + "		Data logger battery may be discharged.\n",
                // 3. System Information
                "		Data logger, Bluetooth modem and Phone"
                        + " \n\t\tinformation can be seen on System "
                        + " \n\t\tInformation screen.\n",
                // 4. Monitor Data
                "		Raw data can be monitored from"
                        + " \n\t\tMonitor Raw Data menu.\n"
                        + "		This feature is mostly used for diagnostic"
                        + " \n\t\tpurpose.\n"
                        + "		Parameters can be monitored from "
                        + " \n\t\tMonitor Parameter menu.\n"
                        + "		This feature is used to check sensor data"
                        + " \n\t\tor connectivity.\n"
                        + "		Monitor log interval can be set by editing"
                        + " \n\t\tmonitor interval from monitor screen.\n",
                // "5. Scanning
                "		Data logger scanning can be started or"
                        + " \n\t\tstopped using Scan screen.\n"
                        + "		Logging interval can be set using Scan \n\t\tscreen.\n"
                        + "		Data logger memory full action can be set.\n"
                        + "		Data logger storage memory can be erased.\n",
                // "6. Downloading logged data
                "		Logged data can be downloaded into "
                        + " \n\t\tphone using Download Data option "
                        + " \n\t\tfrom Scan screen.\n"
                        + "		Press Save button from Download screen "
                        + " \n\t\tto download and save logged data "
                        + " \n\t\tinto phones memory.\n"
                        + "		Downloaded data are saved in CSV format"
                        + " \n\t\tat 'ESCL10VTR5 Files' folder on SD card.\n",
                // 7. Viewing Downloaded Data
                "		Downloaded logged data can be viewed by"
                        + " \n\t\tpressing View Data button from main "
                        + " \n\t\tmenu.\n"
                        + "		Select file and choose parameter to view "
                        + " \n\t\tlogged data at View Data screen.\n",
                // "8. Uploading files
                "		Data logger files can be uploaded to "
                        + " \n\t\tremote folder on FTP server using 'Upload "
                        + " \n\t\tFiles' menu option.\n"
                        + "		Type URL settings carefully while resetting "
                        + " \n\t\tthe URL settings.\n",
                // "9.	Files not uploading on FTP server",
                "		Mobile network may be disable.\n"
                        + "		Mobile network may not be available.\n"
                        + "		URL or Port may be incorrect.\n",
                // 10. Files storage on SD card
                "		Downloaded data are saved in CSV format "
                        + " \n\t\tat 'ESCL10VTR5 Files' folder on SD card.\n"};
        /**
         * Our data, part 3.
         */
        private final boolean[] mExpanded = {true, false, false, false, false, false, false, false, false, false};

        /**
         * Instantiates a new speech list adapter.
         *
         * @param context the context
         */
        public SpeechListAdapter(Context context) {
            mContext = context;
        }

        /**
         * The number of items in the list is determined by the number of speeches in our array.
         *
         * @return the count
         * @see android.widget.ListAdapter#getCount()
         */
        public int getCount() {
            return mTitles.length;
        }

        /**
         * Since the data comes from an array, just returning the index is sufficent to get at the data. If we were
         * using a more complex data structure, we would return whatever object represents one row in the list.
         *
         * @param position the position
         * @return the item
         * @see android.widget.ListAdapter#getItem(int)
         */
        public Object getItem(int position) {
            return position;
        }

        /**
         * Use the array index as a unique id.
         *
         * @param position the position
         * @return the item id
         * @see android.widget.ListAdapter#getItemId(int)
         */
        public long getItemId(int position) {
            return position;
        }

        /**
         * Make a SpeechView to hold each row.
         *
         * @param position    the position
         * @param convertView the convert view
         * @param parent      the parent
         * @return the view
         * @see android.widget.ListAdapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            SpeechView sv;


            if (convertView == null) {
                if (height <= AdjustScreen.SCREEN_HEIGHT && width <= AdjustScreen.SCREEN_WIDTH) {
                    sv = new SpeechView(mContext, mTitles[position], mDialogueSmallScreen[position], mExpanded[position]);
                } else
                    sv = new SpeechView(mContext, mTitles[position], mDialogue[position], mExpanded[position]);
                // sv.setBackgroundColor(R.drawable.LAY_BG_COL);
                // sv.setBackgroundColor(Color.RED);
            } else {
                sv = (SpeechView) convertView;
                sv.setTitle(mTitles[position]);
                if (height <= AdjustScreen.SCREEN_HEIGHT && width <= AdjustScreen.SCREEN_WIDTH) {
                    sv.setDialogue(mDialogueSmallScreen[position]);
                } else
                    sv.setDialogue(mDialogue[position]);
                sv.setExpanded(mExpanded[position]);
            }
            return sv;
        }

        /**
         * Toggle.
         *
         * @param position the position
         */
        public void toggle(int position) {
            mExpanded[position] = !mExpanded[position];
            notifyDataSetChanged();
        }
    }

    /**
     * We will use a SpeechView to display each speech. It's just a LinearLayout with two text fields.
     */
    private class SpeechView extends LinearLayout {
        /**
         * The title.
         */
        private final TextView mTitle;
        /**
         * The dialogue.
         */
        private final TextView mDialogue;

        /**
         * Instantiates a new speech view.
         *
         * @param context  the context
         * @param title    the title
         * @param dialogue the dialogue
         * @param expanded the expanded
         */
        public SpeechView(Context context, String title, String dialogue, boolean expanded) {
            super(context);
            this.setOrientation(VERTICAL);
            this.setBackgroundColor(0xFF227799);
			/*TextView tv=new TextView(getApplicationContext());
		    tv.setText("Troubleshoot/Help");

		    addView(tv, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 40));*/
            mTitle = new TextView(context);
            mTitle.setGravity(Gravity.CENTER_VERTICAL);
            mTitle.setText(title);
            mTitle.setTextColor(0xFF00ff00);
            addView(mTitle, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 70));
            mDialogue = new TextView(context);
            mDialogue.setText(dialogue);
            mDialogue.setTextColor(0xFFffff00);

            addView(mDialogue, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            mDialogue.setVisibility(expanded ? VISIBLE : GONE);
        }

        /**
         * Convenience method to set the title of a SpeechView.
         *
         * @param title the new title
         */
        public void setTitle(String title) {
            mTitle.setText(title);
        }

        /**
         * Convenience method to set the dialogue of a SpeechView.
         *
         * @param words the new dialogue
         */
        public void setDialogue(String words) {
            mDialogue.setText(words);
        }

        /**
         * Convenience method to expand or hide the dialogue.
         *
         * @param expanded the new expanded
         */
        public void setExpanded(boolean expanded) {
            mDialogue.setVisibility(expanded ? VISIBLE : GONE);
        }
    }
}
