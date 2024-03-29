
import { NativeModules, NativeEventEmitter, Platform } from 'react-native';

const xinstallModule = NativeModules.Xinstall;
const xinstallModuleEmitter = new NativeEventEmitter(xinstallModule);

const wakeUpEventName = 'xinstallWakeUpEventName';
const wakeUpDetailEventName = 'xinstallWakeUpDetailEventName';
 
var wakeSubscription = null;

export default class Xinstall {

  static init () {
  	if (Platform.OS === "ios") {
  	    xinstallModule.initWithoutAd()
  	} else if (Platform.OS === "android") {
  	    xinstallModule.initNoAd()
  	}
  }

  static initWithAd (adParams, premissionCallback) {
    if (Platform.OS === "ios") {
      xinstallModule.initWithAd(adParams)
    } else if (Platform.OS === "android") {
      xinstallModule.initWithAd(adParams, premissionCallback)
    }
  }


  static addInstallEventListener(completion) {
    xinstallModule.addInstallEventListener(data => {
        completion(data)
      }
    )
  }

  static setLog(isOpen) {
    xinstallModule.setLog(isOpen)
  }

  static addWakeUpEventListener(completion) {

    wakeSubscription = xinstallModuleEmitter.addListener (wakeUpEventName, data => {
      completion(data)
    });

    xinstallModule.addWakeUpEventListener(data => {
        completion(data)
      }
    )
  }

  static addWakeUpDetailEventListener(completion) {
    wakeSubscription = xinstallModuleEmitter.addListener (wakeUpDetailEventName, data => {
      completion(data)
    });

    xinstallModule.addWakeUpDetailEventListener(data => {
        completion(data)
    });
  }

  static removeWakeUpEventListener () {
    wakeSubscription.remove()
  }

  static reportRegister () {
    xinstallModule.reportRegister()
  }

  static reportEventPoint (eventID, eventValue) {
    xinstallModule.reportEventPoint(eventID, eventValue)
  }

  static reportEventWhenOpenDetailInfo(eventId, eventValue, eventSubValue) {
    xinstallModule.reportEventWhenOpenDetailInfo(eventId, eventValue, eventSubValue)
  }
  
  static reportShareByXinShareId(userId) {
	xinstallModule.reportShareByXinShareId(userId);
  }
}
