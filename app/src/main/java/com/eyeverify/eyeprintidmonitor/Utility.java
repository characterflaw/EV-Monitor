package com.eyeverify.eyeprintidmonitor;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by jeffdavey on 5/8/17.
 */

public class Utility {

    public static final String TAG = Utility.class.getSimpleName();
    public static final int kIMPORTANCE_FOREGROUND = 0;
    public static final int kIMPORTANCE_BACKGROUND = 0;
    public static final String PATTERN_PKG_ID = "([a-z_]{1}[a-z0-9_]*(\\.[a-z_]{1}[a-z0-9_]*)*)";



    public static boolean validatePackageId(final String pid) {

        Pattern pattern = Pattern.compile(PATTERN_PKG_ID);
        Matcher matcher = pattern.matcher(pid);

        return matcher.matches();
    }


    public static List<String> getRunningPackagesList(Context context) {

        List<String> listPackageIds = new ArrayList<>();

        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    listPackageIds.add(activeProcess);
                }
            }
        }

        return listPackageIds;
    }

    public static boolean isRunningInForeground(Context context) {

        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(context.getPackageName())) {
                        //If your app is the process in foreground, then it's not in running in background
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static boolean isAppRunning(Context context, String pid, int importance) {

        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == importance) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.startsWith(pid)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /*
        Time that an application is running
        How long an app has been running

        http://stackoverflow.com/questions/3903650/time-that-an-application-is-running
        I think this only reports on service
     */
    public long getProcessUptime(Context context, String pid) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        long currentMillis = Calendar.getInstance().getTimeInMillis();
        long activeMillis = -1;

//        Calendar cal = Calendar.getInstance();

        for (ActivityManager.RunningServiceInfo info : services) {

            if (info.process.contains("flamingo"))
                Log.d("yyz", info.process);

            if (info.process.equals(pid)) {
                activeMillis = currentMillis - info.activeSince;
                break;
            }
        }

        return activeMillis;
    }

}
