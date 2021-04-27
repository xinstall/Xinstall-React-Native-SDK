package com.xinstall.rn;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
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

import java.util.Iterator;
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
                    WritableMap params = xData2Map(xAppData, false);
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

    private WritableMap xData2Map(XAppData xAppData, boolean isInit) {
        Log.d("XinstallModule", "getInstallParam : data = " + xAppData.toJsonObject().toString());

        String channelCode = xAppData.getChannelCode();
        String timeSpan = xAppData.getTimeSpan();
        boolean firstFetch = xAppData.isFirstFetch();

        WritableMap params = Arguments.createMap();
        params.putString("channelCode", channelCode);
        params.putString("timeSpan", timeSpan);
        if (isInit) {
            params.putBoolean("isFirstFetch", firstFetch);
        }

        Map<String, String> extraData = xAppData.getExtraData();
        WritableMap data = Arguments.createMap();
        //通过链接后面携带的参数或者通过webSdk初始化传入的data值。
        String uo = extraData.get("uo");
        if (uo.trim().equals("")) {
            data.putMap("uo", Arguments.createMap());
        } else {
            try {
                WritableMap uoMap = Arguments.createMap();
                com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(uo);
                Iterator<Map.Entry<String, Object>> iterator = jsonObject.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Object> next = iterator.next();
                    uoMap.putString(next.getKey(), (String) next.getValue());
                }
                data.putMap("uo", uoMap);
            } catch (Exception e) {
                data.putMap("uo", Arguments.createMap());
            }

        }
        //webSdk初始，在buttonId里面定义的按钮点击携带数据
        String co = extraData.get("co");
        if (co.trim().equals("")) {
            data.putMap("co",  Arguments.createMap());
        } else {
            try {
                WritableMap coMap = Arguments.createMap();
                com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(co);
                Iterator<Map.Entry<String, Object>> iterator = jsonObject.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Object> next = iterator.next();
                    coMap.putString(next.getKey(), (String) next.getValue());
                }
                data.putMap("co", coMap);
            } catch (Exception e) {
                data.putMap("co", Arguments.createMap());
            }
        }

        params.putMap("data", data);
        return params;
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
                    WritableMap params = xData2Map(xAppData, true);
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
