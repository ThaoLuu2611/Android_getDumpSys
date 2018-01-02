package com.example.getdumpsys;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.BadTokenException;
import android.widget.Button;

public class MainActivity extends Activity {
	Button getDumpstate;
	String LOG_TAG="thaoSysdump";
	private ProgressDialog progressDialog;
    private String sysdump_time;
    String month, day, hour, min, sec;
    //private byte[] buf = new byte[1024];
    private boolean isDumpstateRunning = false;
    Button copy;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getDumpstate = (Button)findViewById(R.id.run_dump);
		getDumpstate.setOnClickListener(mClicked);
		submit();
		copy = (Button)findViewById(R.id.copy);
		copy.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				checkCopyToSdcard();
			}
		});
		  sysdump_time = getTimeToString();
		  
	}
	private void submit() {
	    try {
	         String[] commands = {"dumpstate > /sdcard/log1.txt"};
	         Process p = Runtime.getRuntime().exec("/system/bin/sh -");
	         DataOutputStream os = new DataOutputStream(p.getOutputStream());            
	            for (String tmpCmd : commands) {
	                    os.writeBytes(tmpCmd+"\n");
	            }
	        } catch (IOException e) {
	        e.printStackTrace();
	        }
	}
	View.OnClickListener mClicked	= new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
            Log.i(LOG_TAG, "--select - run dumpstate/logcat");
            Log.i(LOG_TAG, "progress dialog show");

            /*
            if( IsRunningDumpstate() ) {
                Toast mToast = Toast.makeText(SysDump.this, "", Toast.LENGTH_SHORT);
                mToast.setText("Dumpstate is still running.\nPlease retry to get dumpstate about 2 minites later.");
                mToast.setGravity(Gravity.CENTER, 0, 0);
                mToast.show();
                return;
            }
            */

            showProgressDialog("Wait...");
       //     SendData(mOem.OEM_IPC_DUMP_BIN);
            // Implement the thread, because of the ProgressDialog.
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    File dataLogDirectory = new File("/data/log");
                    File btsnoop_log = new File("/mnt/sdcard/Android/data/btsnoop_hci.log");
                    File btsnoop_log_old = new File("/mnt/sdcard/Android/data/btsnoop_hci.log.old");
                    File hsuart_log = new File("/mnt/sdcard/Android/data/[BT]msm_serial_hs.log");
                    File hsuart_log_old = new File("/mnt/sdcard/Android/data/[BT]msm_serial_hs.log.old");

                    if (!dataLogDirectory.exists()) {
                        dataLogDirectory.mkdir();
                    }

                 
                    DoShellCmd("bugreport > /data/log/dumpState_"
                               + /* sysdump_time */getTimeToString() + ".log");

                /*    if (SystemProperties.get("persist.security.mdm.SElogs","1").equals("1")) {
                        getSEAndroidLogs(); //get SEAndroid logs
                    }
                    getTSPLogs(); //get TSP logs

                    if(btsnoop_log.exists()){
                        Log.d(LOG_TAG, "btsnoop_hci.log exists!! ");
                        outFile = "/data/log/btsnoop_hci_" + sysdump_time + ".log";
                        WriteToSDcard("/mnt/sdcard/Android/data/btsnoop_hci.log", outFile, "btsnoop_hci.log");
                    }

                    if(btsnoop_log_old.exists()){
                        Log.d(LOG_TAG, "btsnoop_hci.log.old exists!! ");
                        outFile = "/data/log/btsnoop_hci_" + sysdump_time + ".log.old";
                        WriteToSDcard("/mnt/sdcard/Android/data/btsnoop_hci.log.old", outFile, "btsnoop_hci.log.old");
                    }

                    if(hsuart_log.exists()){
                        Log.d(LOG_TAG, "[BT]msm_serial_hs.log exists!! ");
                        outFile = "/data/log/[BT]msm_serial_hs_" + sysdump_time + ".log";
                        WriteToSDcard("/mnt/sdcard/Android/data/[BT]msm_serial_hs.log", outFile, "[BT]msm_serial_hs.log");
                    }

                    if(hsuart_log_old.exists()){
                        Log.d(LOG_TAG, "[BT]msm_serial_hs.log.old exists!! ");
                        outFile = "/data/log/[BT]msm_serial_hs_" + sysdump_time + ".log.old";
                        WriteToSDcard("/mnt/sdcard/Android/data/[BT]msm_serial_hs.log.old", outFile, "[BT]msm_serial_hs.log.old");
                    }*/

                 //   mHandler.sendEmptyMessage(QUERY_DUMPSTATE_DONE);
                    hideProgressDialog();
                    
                }
            });
            thread.start();
		}
	}; 
    private void checkCopyToSdcard() {
    
        // e.d.kim 110615 _
        // copy all files and directories from /data/log to /sdcard/log
        // (not deleting after copy)
        // No need to check file name or ext any more. Just copy ALL of
        // them!
        showProgressDialog("Wait...");
        // __shlee__ 110905 Create thread and then perform copying logs
        // in there.
        // If too many log files are exist in Source directories, ANR
        // will be occured.
        Thread thread = new Thread(new Runnable() {
            public void run() {
                File dataLogDirectory = new File("/data/log");

                File sdcardLogDirectory = new File("/mnt/sdcard/log");
 

                File NVMDirectory = new File("/NVM");
                File sdcardNVMDirectory = new File("/mnt/sdcard/log/NVM");

                //if no exist sdcardLogDirectory, first make it.
                if(!sdcardLogDirectory.exists()) {
                    sdcardLogDirectory.mkdir();
                }

/*                if(com.sec.android.app.dummy.SecProductFeature_COMMON.SEC_PRODUCT_FEATURE_COMMON_RESET_REASON) {
                    File ResetreasonDirectory = new File("/data/system/users/service/data");
                    copyDirectory(ResetreasonDirectory, sdcardLogDirectory);
                }

                if(SecProductFeature_COMMON.SEC_PRODUCT_FEATURE_COMMON_ENABLE_EXT_DEBUG_CUSTOMER_SVC) {
                    File dropboxLogDirectory = new File("/data/system/dropbox");
                    File sdcardDropboxLogDirectory = new File("/mnt/sdcard/log/dropbox");
                    copyDirectory(dropboxLogDirectory, sdcardDropboxLogDirectory);

                    File dataSRLogDirectory = new File("/data/radio");
                    copyDirectory(dataSRLogDirectory, sdcardLogDirectory);
                }*/

                copyDirectory(dataLogDirectory, sdcardLogDirectory);
           /*     copyDirectory(silentLogDirectory, sdcardLogDirectory);
                copyDirectory(dataCPCrashLogDirectory, sdcardCPCrashLogDirectory);
                copyDirectory(dataCPLogNewDirectory, sdcardCPCrashLogDirectory);

                if (isMarvell) {
                    File cpCrashDumpFile = new File("data/com_DDR_RW.bin");
                    if(cpCrashDumpFile.exists()){
                        Log.d(LOG_TAG, "com_DDR_RW.bin file is exist");
                        copyDirectory(cpCrashDumpFile,sdcardNVMDirectory);
                    }else{
                        Log.i(LOG_TAG, "com_DDR_RW.bin file is not exist");
                    }
                    copyDirectory(NVMDirectory, sdcardNVMDirectory);
                }

                if (dataCPLogDirectory.getPath().contains(dataLogDirectory.getPath()) == false) {
                    copyDirectory(dataCPLogDirectory, sdcardLogDirectory);
                }

                if (dataCPLogDirectoryEfs.getPath().contains(dataLogDirectory.getPath()) == false) {
                    copyDirectory(dataCPLogDirectoryEfs, sdcardLogDirectory);
                }
*/
          /*      if (btlog.exists()) {
                    Log.i(LOG_TAG, "btlog.exists == true");
                    WriteToSDcard("/data/app/bt.log", "/mnt/sdcard/log/bt.log", "bt.log");
                }*/

         //       handler.sendEmptyMessage(0);
                Log.i(LOG_TAG,
                      "broadcast media mounted = "
                      + Environment.getExternalStorageDirectory());
                Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
                                           + Environment.getExternalStorageDirectory().getPath()));
                sendBroadcast(intent);
             //   mHandler.sendEmptyMessage(QUERY_COPY_CARD_DONE);// for
                // "copy success!"
                // pop-up
            }
        });
        thread.start();
    }
    public void copyDirectory(File src, File dest) {
        Log.d(LOG_TAG, "copyDirectory : " + src + " / " + dest);

        if (src.isDirectory()) {
            if (!dest.exists()) {
                dest.mkdir();
            }

            String[] fileList = src.list();

            if (fileList == null || fileList.length <= 0) {
                return;
            }

            for (int i = 0; i < fileList.length; i++) {
                copyDirectory(new File(src, fileList[i]), new File(dest, fileList[i]));
            }
        } else {

            FileInputStream fin = null;
            FileOutputStream fout = null;

            try {
                fin = new FileInputStream(src);
                fout = new FileOutputStream(dest);
                // Copy the bits from instream to outstream
                byte[] buffer = new byte[1024];
                int len;

                while ((len = fin.read(buffer)) > 0) {
                    fout.write(buffer, 0, len);
                }
                fout.flush();
            } catch (FileNotFoundException fnfe) {
                System.err.println("// Exception from");
                Log.d(LOG_TAG, "FileNotFoundException : " + Log.getStackTraceString(fnfe));
            } catch (Exception e) {
                Log.d(LOG_TAG, "Exception : " + Log.getStackTraceString(e));
            } finally {
                if (fin != null) {
                    try {
                        fin.close();
                    } catch (Exception e) {
                        Log.d(LOG_TAG, "Exception : " + Log.getStackTraceString(e));
                    }
                }

                if (fout != null) {
                    try {
                        fout.close();
                    } catch (Exception e) {
                        Log.d(LOG_TAG, "Exception : " + Log.getStackTraceString(e));
                    }
                }
            } // end of finally
        }
    }

    private boolean showProgressDialog(String msg) {
        Log.i(LOG_TAG, "showProgressDialog()");

        if (isFinishing()) {
            Log.i(LOG_TAG, "isFinishing()");
            return false;
        }

        if (progressDialog == null) {
            progressDialog = new ProgressDialog(MainActivity.this);
        }

        try {
            progressDialog.setMessage(msg);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        } catch (BadTokenException e) {
            Log.d(LOG_TAG, "BadTokenException");
        }

        return true;
    }
    private boolean hideProgressDialog() {
        Log.i(LOG_TAG, "hideProgressDialog()");

        if (progressDialog == null) {
            return false;
        }

        try {
            if (progressDialog.isShowing() && (progressDialog.getWindow() != null)) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            progressDialog = null;
        }

        return true;
    }
    private String getTimeToString() {
        Calendar cal = Calendar.getInstance();
        DecimalFormat df = new DecimalFormat("00", DecimalFormatSymbols.getInstance(Locale.US));
        month = df.format(cal.get(Calendar.MONTH) + 1);
        day = df.format(cal.get(Calendar.DAY_OF_MONTH));
        hour = df.format(cal.get(Calendar.HOUR_OF_DAY));
        min = df.format(cal.get(Calendar.MINUTE));
        sec = df.format(cal.get(Calendar.SECOND));
        sysdump_time = cal.get(Calendar.YEAR) + month + day + hour + min;
        Log.i(LOG_TAG, "getTimeToString : " + sysdump_time);
        return sysdump_time;
    }
    int DoShellCmd(String cmd) {
        isDumpstateRunning = true;
        Log.i(LOG_TAG, "DoShellCmd : " + cmd);
        Process p = null;
        String[] shell_command = {
            "/system/bin/sh", "-c", cmd
        };

        try {
            Log.i(LOG_TAG, "exec command");
            p = Runtime.getRuntime().exec(shell_command);
            p.waitFor();
            Log.i(LOG_TAG, "exec done");
        } catch (IOException exception) {
            Log.e(LOG_TAG, "DoShellCmd - IOException");
            return -1;
        } catch (SecurityException exception) {
            Log.e(LOG_TAG, "DoShellCmd - SecurityException");
            return -1;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return -1;
        }

        Log.i(LOG_TAG, "DoShellCmd done: " + cmd);
        return 1;
    }

}
