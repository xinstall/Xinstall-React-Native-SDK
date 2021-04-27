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
  NSDictionary *callbackRet = @{
      @"channelCode" : channelCode?:@"",
      @"data" : data?:@{}
  };
    
    [self sendEventWithName:kXinstallWakeUpEventName body:callbackRet];
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
            callbackRet = @{
                @"channelCode" : @"",
                @"data" : @{},
                @"isFirstFetch" : @NO
            };
        } else {
            // 数据处理下
            callbackRet = @{
                @"channelCode" : installData.channelCode?:@"",
                @"data" : installData.data?:@{},
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
