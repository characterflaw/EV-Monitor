package com.eyeverify.eyeprintidmonitor;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends Activity implements CompoundButton.OnCheckedChangeListener{

    public static final String TAG = MainActivity.class.getSimpleName();

    private String mPackageId;

    /*
        http://stackoverflow.com/questions/20416610/app-to-monitor-other-apps-on-android
     */
    private ArrayList<HashMap<String,Object>> mProcessList;
    private MonitorService mBackgroundService;

    private EditText mEtPackageId;
    private CheckBox mCbMonitor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // TODO: provide your layout

        mEtPackageId = (EditText)findViewById(R.id.etPackageId);
        mCbMonitor = (CheckBox)findViewById(R.id.cbMonitor);
        mCbMonitor.setOnCheckedChangeListener(this);

        this.bindService(
                new Intent(this, MonitorService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE);

    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MonitorService.LocalBinder binder = (MonitorService.LocalBinder)service;
            mBackgroundService = binder.getService();
//            mBackgroundService.setCallback(MainActivity.this);
//            mBackgroundService.setCallback(null);
//            mBackgroundService.setPackageId("com.eyeverify.evsample");
            mBackgroundService.enableMonitor(false);
            mBackgroundService.start();
        }

        @Override
        public void onServiceDisconnected(ComponentName className)
        {
            mBackgroundService = null;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        mCbMonitor.setChecked(false);

        if (mBackgroundService != null) {
            mBackgroundService.setCallback(null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBackgroundService != null) {
            mBackgroundService.setCallback(null);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (mBackgroundService != null) {
            String pid = mEtPackageId.getText().toString();
            if (!isChecked) {
                if (Utility.validatePackageId(pid)) {
                    mPackageId = pid;
                    mBackgroundService.setPackageId(mPackageId);
                    mBackgroundService.enableMonitor(isChecked);
                } else {
                    Log.d(TAG, "String does not look like a valid Package ID");
                }
            } else {
                mBackgroundService.enableMonitor(false);
            }
        }
    }

}
