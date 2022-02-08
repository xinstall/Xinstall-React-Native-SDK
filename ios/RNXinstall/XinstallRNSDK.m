//
//  XinstallRNSDK.m
//  XinstallRNSDK
//
//  Created by Xinstall on 2020/12/16.
//  Copyright © 2021 shu bao. All rights reserved.
//

#import "XinstallRNSDK.h"
#import "XinstallRNConfig.h"
#import <AdServices/AAAttribution.h>

#if __has_include(<React/RCTBridge.h>)
  #import <React/RCTEventDispatcher.h>
  #import <React/RCTRootView.h>
  #import <React/RCTBridge.h>
  #import <React/RCTLog.h>
  #import <React/RCTEventEmitter.h>
#elif __has_include("React/RCTBridge.h")
  #import "React/RCTEventDispatcher.h"
  #import "React/RCTRootView.h"
  #import "React/RCTBridge.h"
  #import "React/RCTLog.h"
  #import "React/RCTEventEmitter.h"
#elif __has_include("RCTBridge.h")
  #import "RCTEventDispatcher.h"
  #import "RCTRootView.h"
  #import "RCTBridge.h"
  #import "RCTLog.h"
  #import "RCTEventEmitter.h"
#endif

static NSString * const kXinstallWakeUpEventName = @"xinstallWakeUpEventName";
static NSString * const kXinstallWakeUpDetailEventName = @"xinstallWakeUpDetailEventName";

/// 注册 唤醒监听 类型
typedef NS_ENUM(NSInteger, XinstallRNSDKWakeUpListenerType) {
    XinstallRNSDKWakeUpListenerTypeUnknow = 0,           // 未知类型，一般为没有注册过唤醒监听
    XinstallRNSDKWakeUpListenerTypeWithoutDetail,        // 不包含错误的类型，只有获取唤醒参数成功时回调
    XinstallRNSDKWakeUpListenerTypeWithDetail            // 包含错误的类型，获取唤醒参数成功或者失败时都会回调
};

@interface XinstallRNSDK ()<XinstallDelegate>

/// 注册唤醒参数的 js 回调
@property (nonatomic, copy) RCTResponseSenderBlock registeredWakeUpCallback;
/// 注册 唤醒监听 类型
@property (nonatomic, assign) XinstallRNSDKWakeUpListenerType wakeUpListenerType;
/// 保存唤醒参数，因为唤醒的时机可能早于js注册唤醒
@property (nonatomic, strong) XinstallData *wakeUpData;
/// 保存唤醒错误信息，因为唤醒的时机可能早于js注册唤醒
@property (nonatomic, strong) XinstallError *wakeUpError;
/// 通过 scheme 唤醒的情况下，执行 -application:openURL:options: 方法时，该对象还没有创建，必须在创建后再进行处理，所以通过这个参数来辨别
@property (nonatomic, assign, getter=isLaunchSchemeUsed) BOOL launchSchemeUsed;

@end


@implementation XinstallRNSDK

//@synthesize bridge = _bridge;

RCT_EXPORT_MODULE(Xinstall);

- (instancetype)init {
  if (self = [super init]) {
      if (XinstallThirdVersionFlag.length > 0) {}
      if (XinstallThirdPlatformFlag.length > 0) {}
  }
  return self;
}

// TODO 本期先不加，下期添加
//+ (BOOL)requiresMainQueueSetup {
//    return YES;
//}


#pragma mark - XinstallDelegate Methods

- (void)xinstall_getWakeUpParams:(XinstallData *)appData error:(nullable XinstallError *)error {
  self.wakeUpData = appData;
  self.wakeUpError = error;
  [self invokeRegisteredWakeUpCallbackWithChannelCode:appData.channelCode data:appData.data error:error];
}

- (NSString *)xiSdkThirdVersion {
    return XinstallThirdVersion;
}

- (NSInteger)xiSdkType {
    return XinstallThirdPlatform;
}

#pragma mark - private Methods

- (void)invokeRegisteredWakeUpCallbackWithChannelCode:(NSString *)channelCode data:(NSDictionary *)data error:(XinstallError *)error {
  if (self.registeredWakeUpCallback == nil) { return; }

    // 数据处理下
    NSDictionary *completedData = [self handleInstallInnerData:data];
    
    NSDictionary *callbackRet = @{
      @"channelCode" : channelCode?:@"",
      @"data" : completedData,
      @"timeSpan" : @(0)
    };
    
    // 如果唤醒参数里没有任何数据，那么就直接返回空对象
    // channelCode 是否有效
    BOOL isChannleCodeValid = channelCode.length > 0;
    // co 是否有效
    BOOL isCoValid = YES;
    // uo 是否有效
    BOOL isUoValid = YES;
    // data 里是否有其他参数
    BOOL isContainOtherData = completedData.allKeys.count > 2;
    
    if ([completedData[@"co"] isKindOfClass:[NSString class]] && [completedData[@"co"] length] == 0) {
        isCoValid = NO;
    }
    
    if ([completedData[@"co"] isKindOfClass:[NSDictionary class]] && [completedData[@"co"] allKeys].count == 0) {
        isCoValid = NO;
    }
    
    if ([completedData[@"uo"] isKindOfClass:[NSString class]] && [completedData[@"uo"] length] == 0) {
        isUoValid = NO;
    }
    
    if ([completedData[@"uo"] isKindOfClass:[NSDictionary class]] && [completedData[@"uo"] allKeys].count == 0) {
        isUoValid = NO;
    }

    // 全部都无效的时候，返回 {}
    if (!(isChannleCodeValid || isCoValid || isUoValid || isContainOtherData)) {
        callbackRet = @{};
    }
    
    switch (self.wakeUpListenerType) {
        case XinstallRNSDKWakeUpListenerTypeWithDetail:
        {
            if (self.wakeUpData) {          // 有唤醒数据，会回调
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self sendEventWithName:kXinstallWakeUpDetailEventName body:@{ @"wakeUpData" : callbackRet, @"error" : @{}}];
                });
            } else if (self.wakeUpError) {  // 有唤醒错误信息，也会回调
                NSDictionary *dicError = @{
                    @"errorType" : @(self.wakeUpError.type),
                    @"errorMsg" : self.wakeUpError.errorMsg
                };
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self sendEventWithName:kXinstallWakeUpDetailEventName body:@{ @"wakeUpData" : @{}, @"error" : dicError}];
                });
            }
        }
            break;
        case XinstallRNSDKWakeUpListenerTypeWithoutDetail:
        {
            // 必须拿到了唤醒数据，才会回调
            if (self.wakeUpData) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self sendEventWithName:kXinstallWakeUpEventName body:callbackRet];
                });
            }
        }
            break;
        case XinstallRNSDKWakeUpListenerTypeUnknow:
        {
            // 什么都不做
        }
            break;
    }
}

/**
 处理 XinstallData.data 数据，添加缺省值
 */
- (NSDictionary *)handleInstallInnerData:(NSDictionary *)installInnerData {
    // 构建一个空 map（名字叫：mdicData）
    NSMutableDictionary *mdicData = [NSMutableDictionary dictionary];
    // 如果底层 SDK 给到的 XinstallData.data 是 map 类型，就把这个数据先加入空 map
    if ([installInnerData isKindOfClass:[NSDictionary class]]) {
        [mdicData addEntriesFromDictionary:installInnerData];
    }
    // 判断 mdicData 里有没有 co 参数，如果没有，就是缺省了，那么就设定 co = {}
    if (mdicData[@"co"] == nil) {
        mdicData[@"co"] = @{};
    }
    // 判断 mdicData 里有没有 uo 参数，如果没有，就是缺省了，那么就设定 co = {}
    if (mdicData[@"uo"] == nil) {
        mdicData[@"uo"] = @{};
    }
    // 判断 mdicData 里的 co 参数是不是字符串类型，如果是，并且是空字符串，那么也作为缺省处理
    if ([mdicData[@"co"] isKindOfClass:[NSString class]] && [mdicData[@"co"] length] == 0) {
        mdicData[@"co"] = @{};
    }
    // 判断 mdicData 里的 uo 参数是不是字符串类型，如果是，并且是空字符串，那么也作为缺省处理
    if ([mdicData[@"uo"] isKindOfClass:[NSString class]] && [mdicData[@"uo"] length] == 0) {
        mdicData[@"uo"] = @{};
    }
    // 以上逻辑处理完成后，mdicData 里必定有 co 和 uo 两个 key，并且缺省情况下 这两个key对应的 value 为 {}
    return [mdicData mutableCopy];
}

#pragma mark - ReactNative 接口 Methods

- (NSArray<NSString *> *)supportedEvents
{
  return @[kXinstallWakeUpEventName, kXinstallWakeUpDetailEventName];
}

RCT_EXPORT_METHOD(setLog:(BOOL)isOpen)
{
    [XinstallSDK setShowLog:isOpen];
}

RCT_EXPORT_METHOD(initWithoutAd)
{
    [XinstallSDK initWithDelegate:self];
}

RCT_EXPORT_METHOD(initWithAd:(id)adConfig)
{
    NSString *idfa;
    NSString *asaToken;
    if ([adConfig isKindOfClass:[NSString class]]) {
        idfa = adConfig;
    } else if ([adConfig isKindOfClass:[NSDictionary class]]) {
        idfa = adConfig[@"idfa"];
        if ([adConfig[@"asa"] boolValue]) {
            if (@available(iOS 14.3, *)) {
                NSError *error;
                asaToken = [AAAttribution attributionTokenWithError:&error];
            }
        }
    }
    
    [XinstallSDK initWithDelegate:self idfa:idfa asaToken:asaToken];
}

RCT_EXPORT_METHOD(addInstallEventListener:(RCTResponseSenderBlock)callback)
{
    
    [[XinstallSDK defaultManager] getInstallParamsWithCompletion:^(XinstallData * _Nullable installData, XinstallError * _Nullable error) {
        NSDictionary *callbackRet = nil;
        // 出现错误时返回空数据，目前安卓没有错误，所以无法统一返回错误信息
        if (error) {
            callbackRet = @{};
        } else {
            // 数据处理下
            callbackRet = @{
                @"channelCode" : installData.channelCode?:@"",
                @"data" : [self handleInstallInnerData:installData.data],
                @"timeSpan" : @(installData.timeSpan),
                @"isFirstFetch" : @(installData.isFirstFetch)
            };
        }
        dispatch_async(dispatch_get_main_queue(), ^{
            callback(@[callbackRet]);
        });
    }];
}

RCT_EXPORT_METHOD(addWakeUpEventListener:(RCTResponseSenderBlock)callback)
{
    // 如果已经注册了带错误的唤醒监听，那么这个注册就不再生效
    if (self.wakeUpListenerType == XinstallRNSDKWakeUpListenerTypeWithDetail) {
        return;
    }

    // 当前没有注册过回调时，可以快速调用。APICloud由于唤醒机制的特殊性，在每次唤醒时都会注册一次回调，这里需要判断一下，以免回调2次
    BOOL couldQuickInvoke = (self.registeredWakeUpCallback == nil);
    // 设定类型和回调
    self.wakeUpListenerType = XinstallRNSDKWakeUpListenerTypeWithoutDetail;
    self.registeredWakeUpCallback = callback;
    
    if (couldQuickInvoke) {
        if (self.wakeUpData) {
            [self invokeRegisteredWakeUpCallbackWithChannelCode:self.wakeUpData.channelCode data:self.wakeUpData.data error:nil];
        }
    }

    // 触发一下 scheme 启动参数
    if (self.isLaunchSchemeUsed == NO) {
        NSURL *url = self.bridge.launchOptions[UIApplicationLaunchOptionsURLKey];
        if (url) {
            self.launchSchemeUsed = YES;
            [XinstallSDK handleSchemeURL:url];
        }
    }
}

RCT_EXPORT_METHOD(addWakeUpDetailEventListener:(RCTResponseSenderBlock)callback)
{
    // 当前没有注册过回调时，可以快速调用。APICloud由于唤醒机制的特殊性，在每次唤醒时都会注册一次回调，这里需要判断一下，以免回调2次
    BOOL couldQuickInvoke = (self.registeredWakeUpCallback == nil);
    // 设定类型和回调
    self.wakeUpListenerType = XinstallRNSDKWakeUpListenerTypeWithDetail;
    self.registeredWakeUpCallback = callback;

    if (couldQuickInvoke) {
        if (self.wakeUpData || self.wakeUpError) {
            [self invokeRegisteredWakeUpCallbackWithChannelCode:self.wakeUpData.channelCode data:self.wakeUpData.data error:self.wakeUpError];
        }
    }

    // 触发一下 scheme 启动参数
    if (self.isLaunchSchemeUsed == NO) {
        NSURL *url = self.bridge.launchOptions[UIApplicationLaunchOptionsURLKey];
        if (url) {
            self.launchSchemeUsed = YES;
            [XinstallSDK handleSchemeURL:url];
        }
    }
}

RCT_EXPORT_METHOD(reportRegister)
{
    [XinstallSDK reportRegister];
}

RCT_EXPORT_METHOD(reportEventPoint:(NSString *)eventID pointValue:(NSInteger)eventValue)
{
    [[XinstallSDK defaultManager] reportEventPoint:eventID eventValue:eventValue];
}

RCT_EXPORT_METHOD(reportEventWhenOpenDetailInfo:(NSString *)eventId eventValue:(NSInteger)eventValue eventSubValue:(NSString *)eventSubValue)
{
    [[XinstallSDK defaultManager] reportEventWhenOpenDetailInfoWithEventPoint:eventId eventValue:eventValue  subValue:eventSubValue];
}

RCT_EXPORT_METHOD(reportShareByXinShareId:(NSString *)xinShareId)
{
    [[XinstallSDK defaultManager] reportShareByXinShareId:xinShareId];
}

@end
