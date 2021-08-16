package com.xinstall.rn;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
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

import java.util.Iterator;
import java.util.Map;

public class Xinstall extends ReactContextBaseJavaModule {
    private static final String TAG = "XinstallRNSDK";

    private boolean mInitialized = false;
    private boolean hasCallInit = false;

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
                Activity activity = getCurrentActivity();
                getWakeUp(activity,intent, null);
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
        if (wakeupIntent != null && wakeupActivity != null&&wakeupCallback != null) {
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

        } else {
            wakeupActivity = null;
            wakeupIntent = null;
            wakeupCallback = null;
        }
    }

    @ReactMethod
    public void addWakeUpEventListener(final Callback successBack) {
        Log.d(TAG, "getWakeUp");
        runInUIThread(new Runnable() {
            @Override
            public void run() {
                addWakeUpEventListenerInMain(successBack);
            }
        });

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

    @Override
    public String getName() {
        return "Xinstall";
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
            data.putMap("co", Arguments.createMap());
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
}
