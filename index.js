
import { NativeModules, NativeEventEmitter } from 'react-native';

const xinstallModule = NativeModules.Xinstall;
const xinstallModuleEmitter = new NativeEventEmitter(xinstallModule);

const wakeUpEventName = 'xinstallWakeUpEventName'

var wakeSubscription = null;

export default class Xinstall {


  static addInstallEventListener(completion) {
    xinstallModule.addInstallEventListener(data => {
        completion(data)
      }
    )
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

  static removeWakeUpEventListener () {
    wakeSubscription.remove()
  }

  static reportRegister () {
    xinstallModule.reportRegister()
  }

  static reportEventPoint (eventID, eventValue) {
    xinstallModule.reportEventPoint(eventID, eventValue)
  }
}
