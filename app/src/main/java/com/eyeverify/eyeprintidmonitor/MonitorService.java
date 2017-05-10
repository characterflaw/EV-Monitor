package com.eyeverify.eyeprintidmonitor;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;



/*
    http://stackoverflow.com/questions/18125241/how-to-get-data-from-service-to-activity
 */


/**
 * Created by jeffdavey on 5/5/17.
 */
public class MonitorService extends Service {

    public static final String TAG = MonitorService.class.getSimpleName();

    enum AppStatus { UNKNOWN, STOPPED, STARTING, RUNNING_FG, RUNNING_BG }

    private boolean mInitialized = false;
    private final IBinder mBinder = new LocalBinder();
    private IServiceCallback mCallback = null;

    private String mPackageId;


    private Timer mTimer = null;
    private final Handler mHandler = new Handler();
    private String mForeground = null;
    private ArrayList<HashMap<String,Object>> mProcessList;
    private Date mSplit = null;

    private AppStatus mCurStatus = AppStatus.UNKNOWN;
    private AppStatus mLastStatus = AppStatus.UNKNOWN;

    private boolean mMonitorEnabled;

    public static final int SERVICE_PERIOD = 5000;

    @Override
    public void onCreate() {
        super.onCreate();
        mInitialized = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if(mInitialized) {
            return mBinder;
        }

        return null;
    }

    /*
        Called and set by the calling Activity (MainActivity)
     */
    public void setCallback(IServiceCallback callback) {
        this.mCallback = callback;
    }

    public void setPackageId(String pkgid) {
        mPackageId = pkgid;
    }

    public void enableMonitor(boolean flag) {
        mMonitorEnabled = flag;
    }

    public interface IServiceCallback {
        void sendResults(AppStatus status, long uptime);
    }

    public class LocalBinder extends Binder {
        MonitorService getService() {
            // Return this instance of the service so clients can call public methods
            return MonitorService.this;
        }
    }

    public void start() {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new MonitoringTimerTask(), 500, SERVICE_PERIOD);
        }
    }

    public void stop() {
        mTimer.cancel();
        mTimer.purge();
        mTimer = null;
    }

    private class MonitoringTimerTask extends TimerTask {
        @Override
        public void run() {

            if (mMonitorEnabled) {
                if (Utility.isAppRunning(getBaseContext(), mPackageId, ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)) {
                    if (mLastStatus == AppStatus.UNKNOWN) {
                        mCurStatus = AppStatus.STARTING;
                    } else {
                        mCurStatus = AppStatus.RUNNING_FG;
                    }
                } else {

                    if (Utility.isAppRunning(getBaseContext(), mPackageId, ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND)) {
                        mCurStatus = AppStatus.RUNNING_BG;
                        //  TODO running in background, pull into foreground
                    } else {
                        mCurStatus = AppStatus.STOPPED;
                        //  TODO start the data collection app
                    }
                }

                if (mLastStatus != mCurStatus) {
                    Log.d(TAG, "Data Collection app state: " + mCurStatus.toString());
                    mLastStatus = mCurStatus;
                }
            }
        }
    }
}
