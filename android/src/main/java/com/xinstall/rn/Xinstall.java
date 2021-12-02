package com.xinstall.rn;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.xinstall.XINConfiguration;
import com.xinstall.XInstall;
import com.xinstall.listener.XInstallAdapter;
import com.xinstall.listener.XWakeUpAdapter;
import com.xinstall.model.XAppData;
import com.xinstall.model.XAppError;

import java.util.Iterator;
import java.util.Map;

public class Xinstall extends ReactContextBaseJavaModule {
    private static final String TAG = "XinstallRNSDK";

    private boolean mInitialized = false;
    private boolean hasCallInit = false;

    public static Integer wakeupType = 0;
    private ReactContext context;

    private Callback wakeupCallback = null;
    private Intent wakeupIntent = null;
    private Activity wakeupActivity = null;



    private static final Handler UIHandler = new Handler(Looper.getMainLooper());

    private  static void runInUIThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // 当前线程为UI主线程
            runnable.run();
        } else {
            UIHandler.post(runnable);
        }
    }

    public Xinstall(final ReactApplicationContext reactContext) {
        super(reactContext);
        context = reactContext;

        reactContext.addActivityEventListener(new ActivityEventListener() {
            @Override
            public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {

            }

            @Override
            public void onNewIntent(Intent intent) {
                if (mInitialized) {
                    Activity currentActivity = getCurrentActivity();
                    if (currentActivity != null) {
                        getWakeUp(currentActivity,intent, null);
                    }
                } else {
                    wakeupActivity = getCurrentActivity();
                    if (wakeupActivity != null) {
                        wakeupIntent = intent;
                    }
                    wakeupCallback = null;
                }
            }
        });

    }

    @ReactMethod
    public void setLog(final boolean isOpen) {
        runInUIThread(new Runnable() {
            @Override
            public void run() {
                XInstall.setDebug(isOpen);
            }
        });
    }

    @ReactMethod
    public void initNoAd() {
        Log.d(TAG,"initNoAd method");
        runInUIThread(new Runnable() {
            @Override
            public void run() {
                initNoAdInMain();
            }
        });
    }

    private void initNoAdInMain() {
        hasCallInit = true;
        XInstall.init(context);
        xinitialized();
    }

    @ReactMethod
    public void initWithAd(ReadableMap params, final Callback premissionBackBlock) {
        hasCallInit = true;
        XINConfiguration configuration = XINConfiguration.Builder();
        boolean adEnable = true;
        if (params.hasKey("adEnable")) {
            adEnable = params.getBoolean("adEnable");
            configuration.adEnable(adEnable);
        }

        if (params.hasKey("oaid") && (params.getString("oaid")).length() > 0) {
            String oaid = params.getString("oaid");
            configuration.oaid(oaid);
        }

        if (params.hasKey("gaid") && params.getString("gaid").length() > 0) {
            String gaid = params.getString("gaid");
            configuration.gaid(gaid);
        }

        boolean isPremission = false;
        if (params.hasKey("isPremission")) {
            isPremission = params.getBoolean("isPremission");
        }

        if (isPremission) {
            XInstall.initWithPermission(context.getCurrentActivity(), configuration, new Runnable() {
                @Override
                public void run() {
                    xinitialized();
                    if (premissionBackBlock != null) {
                        premissionBackBlock.invoke("");
                    }
                }
            });
        } else {
            XInstall.init(context.getCurrentActivity(),configuration);
            xinitialized();
            if (premissionBackBlock != null) {
                premissionBackBlock.invoke("");
            }
        }

    }

    private void xinitialized() {
        mInitialized = true;
        if (wakeupIntent != null && wakeupActivity != null) {
            if (wakeupType == 1) {
                XInstall.getWakeUpParam(wakeupActivity,wakeupIntent, new XWakeUpAdapter() {
                    @Override
                    public void onWakeUp(XAppData xAppData) {
                        if (xAppData != null) {
                            WritableMap params = xData2Map(xAppData, false);
                            if (wakeupCallback == null) {
                                getReactApplicationContext()
                                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                        .emit("xinstallWakeUpEventName", params);
                            } else {
                                wakeupCallback.invoke(params);
                            }
                        }
                        wakeupCallback = null;
                        wakeupActivity = null;
                        wakeupIntent = null;
                    }
                });
            } else if (wakeupType == 2) {
                XInstall.getWakeUpParamEvenErrorAlsoCallBack(wakeupActivity, wakeupIntent, new XWakeUpAdapter() {
                    @Override
                    public void onWakeUpFinish(XAppData xAppData, XAppError xAppError) {
                        super.onWakeUpFinish(xAppData, xAppError);
                        WritableMap params = xDataHasErrorMap(xAppData,xAppError);
                        if (wakeupCallback == null) {
                            getReactApplicationContext()
                                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                    .emit("xinstallWakeUpDetailEventName", params);
                        } else {
                            wakeupCallback.invoke(params);
                        }
                        wakeupCallback = null;
                        wakeupActivity = null;
                        wakeupIntent = null;
                    }
                });
            }
        } else {
            wakeupActivity = null;
            wakeupIntent = null;
            wakeupCallback = null;
        }
    }

    @ReactMethod
    public void addWakeUpDetailEventListener(final Callback successBack) {
        Log.d(TAG, "getWakeUpDetail");
        runInUIThread(new Runnable() {
            @Override
            public void run() {
                if (wakeupType == 1) {
                    Log.d(TAG, "addWakeUpEventListener 与 addWakeUpDetailEventListener 为互斥方法，择一选择使用");
                }
                wakeupType = 2;

                addWakeUpDetailEventListenerInMain(successBack);
            }
        });
    }

    @ReactMethod
    public void addWakeUpEventListener(final Callback successBack) {
        Log.d(TAG, "getWakeUp");


        runInUIThread(new Runnable() {
            @Override
            public void run() {
                if (wakeupType == 2) {
                    Log.d(TAG, "addWakeUpEventListener 与 addWakeUpDetailEventListener 为互斥方法，择一选择使用");
                }
                wakeupType = 1;

                addWakeUpEventListenerInMain(successBack);
            }
        });
    }

    private void  addWakeUpDetailEventListenerInMain(final Callback successBack) {
        if (!hasCallInit) {
            Log.d(TAG, "未执行SDK 初始化方法, SDK 需要手动初始化(初始方法为 init 和 initWithAd !");
            return;
        }
        if (mInitialized) {
            Activity currentActivity = getCurrentActivity();
            if (currentActivity != null) {
                Intent intent = currentActivity.getIntent();
                getWakeUp(currentActivity,intent, successBack);
            }
        } else {
            wakeupCallback = successBack;
            wakeupActivity = getCurrentActivity();
            if (wakeupActivity != null) {
                wakeupIntent = wakeupActivity.getIntent();
            }
        }
    }

    private void addWakeUpEventListenerInMain(final Callback successBack) {
        if (!hasCallInit) {
            Log.d(TAG, "未执行SDK 初始化方法, SDK 需要手动初始化(初始方法为 init 和 initWithAd !");
            return;
        }
        if (mInitialized) {
            Activity currentActivity = getCurrentActivity();
            if (currentActivity != null) {
                Intent intent = currentActivity.getIntent();
                getWakeUp(currentActivity,intent, successBack);
            }
        } else {
            wakeupCallback = successBack;
            wakeupActivity = getCurrentActivity();
            if (wakeupActivity != null) {
                wakeupIntent = wakeupActivity.getIntent();
            }
        }
    }

    private void getWakeUp(Activity activity, Intent intent, final Callback callback) {
        if (wakeupType == 1) {
            XInstall.getWakeUpParam(activity,intent, new XWakeUpAdapter() {
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
        } else if (wakeupType == 2) {
            XInstall.getWakeUpParamEvenErrorAlsoCallBack(activity, intent, new XWakeUpAdapter() {
                @Override
                public void onWakeUpFinish(XAppData xAppData, XAppError xAppError) {
                    super.onWakeUpFinish(xAppData, xAppError);

                    WritableMap params = xDataHasErrorMap(xAppData,xAppError);

                    if (callback == null) {
                        getReactApplicationContext()
                                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                .emit("xinstallWakeUpDetailEventName", params);
                    } else {
                        callback.invoke(params);
                    }
                }
            });
        }
    }

    @ReactMethod
    public void addInstallEventListener(final Callback callback) {
        Log.d(TAG, "getInstall");
        runInUIThread(new Runnable() {
            @Override
            public void run() {
                addInstallEventListenerInMain(callback);
            }
        });
    }

    private void addInstallEventListenerInMain(final  Callback callback) {
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
        Log.d(TAG, "reportRegister");
        XInstall.reportRegister();
    }

    @ReactMethod
    public void reportEventPoint(String pointId, Integer pointValue) {
        Log.d(TAG, "reportEventPoint");
        if (!TextUtils.isEmpty(pointId)) {
            XInstall.reportEvent(pointId, pointValue);
        }
    }

    @ReactMethod
    public void reportShareByXinShareId(String userId) {
        Log.d(TAG,"reportShareByXinShareId");
        if (TextUtils.isEmpty(userId)) {
            Log.d(TAG,"reportShareByXinShareId 方法中，userId 为必传参数");
        } else {
            XInstall.reportShareByXinShareId(userId);
        }
    }

    @Override
    public String getName() {
        return "Xinstall";
    }




    private WritableMap xData2Map(XAppData xAppData, boolean isInit) {
        if (xAppData == null) {
            return  Arguments.createMap();
        }
        Log.d("XinstallModule", "getInstallParam : data = " + (xAppData != null?xAppData.toJsonObject().toString():""));

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
                JSONObject jsonObject = JSON.parseObject(uo);
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
            data.putMap("co", Arguments.createMap());
        } else {
            try {
                WritableMap coMap = Arguments.createMap();
                JSONObject jsonObject = JSON.parseObject(co);
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

    private  WritableMap xDataHasErrorMap(XAppData data, XAppError xAppError) {
        WritableMap wakeUpData = xData2Map(data, false);
        WritableMap error = Arguments.createMap();
        if (xAppError != null) {
            error.putString("errorType",xAppError.getErrorCode());
            error.putString("errorMsg",xAppError.getErrorMsg());
        }
        WritableMap result = Arguments.createMap();
        result.putMap("wakeUpData",wakeUpData);
        result.putMap("error",error);
        return  result;
    }
}
