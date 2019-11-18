package com.serenegiant.usbcameratest;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jyx on 2016/10/23.
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    public static final String TAG = "CrashHandler";
    private Context mContext;
    private static CrashHandler crashHandler;
    private Thread.UncaughtExceptionHandler defaultHandler;
    private DateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private Map<String, String> infos = new HashMap<String, String>();

    private CrashHandler() {
    }

    public static CrashHandler newsIntance() {

        if (crashHandler == null) {
            synchronized (CrashHandler.class) {
                if (crashHandler == null) {
                    crashHandler = new CrashHandler();
                }
            }
        }

        return crashHandler;
    }

    public void init(Context context) {
        mContext = context;
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        if (!handleException(ex) && defaultHandler != null) {
            defaultHandler.uncaughtException(thread, ex);
        }else {
            defaultHandler.uncaughtException(thread, ex);
        }
    }

    private boolean handleException(Throwable ex) {

        if (ex == null) {
            return false;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "很抱歉,程序出现异常", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }).start();

        collectPackageeInfo();

        saveCrashInfo2File(ex);
        return true;
    }

    private void collectPackageeInfo() {
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (packageInfo != null) {
                infos.put("versionName", packageInfo.versionName == null ? "null" : packageInfo.versionName);
                infos.put("versionCode", packageInfo.versionCode + "");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void saveCrashInfo2File(Throwable ex) {

        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            sb.append(entry.getKey() + " = " + entry.getValue() + "\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            String time = format.format(new Date());
            String appName = mContext.getResources().getString(R.string.app_name);
            String fileName = "crash-" + time + "-" + appName + ".txt";
            String path = Environment.getExternalStorageDirectory() + "/firempacrash/";
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(path + fileName);
            fos.write(sb.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing file...", e);
        }

    }

}
