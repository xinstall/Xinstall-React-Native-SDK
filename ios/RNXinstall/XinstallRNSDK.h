//
//  XinstallRNSDK.h
//  XinstallRNSDK
//
//  Created by Xinstall on 2020/12/16.
//  Copyright © 2020 shu bao. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "XinstallSDK.h"


#if __has_include(<React/RCTBridgeModule.h>)
    #import <React/RCTBridgeModule.h>
    #import <React/RCTEventEmitter.h>
#elif __has_include("React/RCTBridgeModule.h")
    #import "React/RCTBridgeModule.h"
    #import "React/RCTEventEmitter.h"
#elif __has_include("RCTBridgeModule.h")
    #import "RCTBridgeModule.h"
    #import "RCTEventEmitter.h"
#endif

NS_ASSUME_NONNULL_BEGIN

@interface XinstallRNSDK : RCTEventEmitter <RCTBridgeModule>

@end

NS_ASSUME_NONNULL_END
