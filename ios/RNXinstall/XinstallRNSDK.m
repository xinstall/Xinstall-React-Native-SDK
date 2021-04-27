//
//  XinstallRNSDK.m
//  XinstallRNSDK
//
//  Created by Xinstall on 2020/12/16.
//  Copyright © 2020 shu bao. All rights reserved.
//

#import "XinstallRNSDK.h"

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

@interface XinstallRNSDK ()<XinstallDelegate>

/// 注册唤醒参数的 js 回调
@property (nonatomic, copy) RCTResponseSenderBlock registeredWakeUpCallback;
/// 保存唤醒参数，因为唤醒的时机可能早于js注册唤醒
@property (nonatomic, strong) XinstallData *wakeUpData;
/// 通过 scheme 唤醒的情况下，执行 -application:openURL:options: 方法时，该对象还没有创建，必须在创建后再进行处理，所以通过这个参数来辨别
@property (nonatomic, assign, getter=isLaunchSchemeUsed) BOOL launchSchemeUsed;

@end


@implementation XinstallRNSDK

//@synthesize bridge = _bridge;

RCT_EXPORT_MODULE(Xinstall);

- (instancetype)init {
  if (self = [super init]) {
      [XinstallSDK initWithDelegate:self];
  }
  return self;
}


#pragma mark - XinstallDelegate Methods

- (void)xinstall_getWakeUpParams:(XinstallData *)appData {
  self.wakeUpData = appData;
  [self invokeRegisteredWakeUpCallbackWithChannelCode:appData.channelCode data:appData.data];
}

#pragma mark - private Methods

- (void)invokeRegisteredWakeUpCallbackWithChannelCode:(NSString *)channelCode data:(NSDictionary *)data {
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
    
    [self sendEventWithName:kXinstallWakeUpEventName body:callbackRet];
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
  return @[kXinstallWakeUpEventName];
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
  self.registeredWakeUpCallback = callback;

  if (self.wakeUpData) {
      [self invokeRegisteredWakeUpCallbackWithChannelCode:self.wakeUpData.channelCode data:self.wakeUpData.data];
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

@end
