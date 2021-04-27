package com.xinstall.rn;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.xinstall.XInstall;
import com.xinstall.listener.XInstallAdapter;
import com.xinstall.listener.XWakeUpAdapter;
import com.xinstall.model.XAppData;

import java.util.Map;

public class Xinstall extends ReactContextBaseJavaModule {
    ReactContext context;

    public Xinstall(final ReactApplicationContext reactContext) {
        super(reactContext);
        context = reactContext;

        reactContext.addActivityEventListener(new ActivityEventListener() {
            @Override
            public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {

            }

            @Override
            public void onNewIntent(Intent intent) {
                getWakeUp(intent, null);
            }
        });
    }


    @ReactMethod
    public void addWakeUpEventListener(final Callback successBack) {
        Log.d("XinstallModule", "getWakeUp");
        Activity currentActivity = getCurrentActivity();
        if (currentActivity != null) {
            Intent intent = currentActivity.getIntent();
            getWakeUp(intent, successBack);
        }
    }

    private void getWakeUp(Intent intent, final Callback callback) {
        XInstall.getWakeUpParam(intent, new XWakeUpAdapter() {
            @Override
            public void onWakeUp(XAppData xAppData) {
                if (xAppData != null) {
                    Log.d("XinstallModule", "getWakeUpParam : data = " + xAppData.toJsonObject().toString());
                    String channelCode = xAppData.getChannelCode();
                    Map<String, String> extraData = xAppData.getExtraData();
                    String timeSpan = xAppData.getTimeSpan();

                    WritableMap params = Arguments.createMap();
                    params.putString("channel", channelCode);
                    params.putString("timeSpan", timeSpan);
                    WritableMap data = Arguments.createMap();
                    data.putString("co", extraData.get("co"));
                    data.putString("uo", extraData.get("uo"));

                    params.putMap("data",data);
                    if (callback == null) {
                        getReactApplicationContext()
                                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                .emit("xinstallWakeUpEventName", params);
                    } else {
                        callback.invoke(params);
                    }
                }
            }

        });
    }

    @Override
    public void initialize() {
        super.initialize();
        XInstall.init(context);
    }

    @Override
    public String getName() {
        return "Xinstall";
    }

    @ReactMethod
    public void addInstallEventListener(final Callback callback) {
        Log.d("XinstallModule", "getInstall");
        XInstall.getInstallParam(new XInstallAdapter() {
            @Override
            public void onInstall(XAppData xAppData) {
                try {
                    Log.d("XinstallModule", "getInstallParam : data = " + xAppData.toJsonObject().toString());
                    String channelCode = xAppData.getChannelCode();
                    Map<String, String> extraData = xAppData.getExtraData();
                    String timeSpan = xAppData.getTimeSpan();
                    boolean firstFetch = xAppData.isFirstFetch();

                    WritableMap params = Arguments.createMap();
                    params.putString("channel", channelCode);
                    params.putString("timeSpan", timeSpan);
                    params.putBoolean("isFirstFetch", firstFetch);
                    WritableMap data = Arguments.createMap();
                    data.putString("co", extraData.get("co"));
                    data.putString("uo", extraData.get("uo"));

                    params.putMap("data",data);
                    callback.invoke(params);
                } catch (Exception e) {
                    callback.invoke(e);
                }
            }
        });
    }

    @ReactMethod
    public void reportRegister() {
        Log.d("XinstallModule", "reportRegister");
        XInstall.reportRegister();
    }

    @ReactMethod
    public void reportEventPoint(String pointId, Integer pointValue) {
        Log.d("XinstallModule", "reportEventPoint");
        if (!TextUtils.isEmpty(pointId)) {
            XInstall.reportPoint(pointId, pointValue);
        }
    }
}
