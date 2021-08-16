# React Native 接入
> 【重要说明】：从 v1.5.0 版本（含）开始，调用  Xinstall 模块的任意方法前，必须先调用一次初始化方法（init 或者 initWithAd），否则将导致其他方法无法正常调用。
>
> 从 v1.5.0 以下升级到 v1.5.0 以上版本后，需要自行修改代码调用初始化方法，Xinstall 模块无法在升级后自动兼容。

## 一、概述

Xinstall 支持 React Native 应用接入，你可以使用 NPM 包管理器及 React Native 配套工具进行快速集成。
对应的 NPM 包名为：xinstall_react_native，你可以在 [这里](https://www.npmjs.com/package/xinstall-react-native) 找到 NPM 中对应的包页面。

xinstall_react_native 模块封装了 Xinstall 官方 SDK，是集智能传参、快速安装、一键拉起、客户来源统计等功能，帮您提高拉新转化率、安装率和多元化精确统计渠道效果的产品。为用户提供点击、安装、注册、留存、活跃等精准统计报表，并可实时排重，杜绝渠道流量猫腻，大大降低运营成本。 具体详细介绍可前往 [Xinstall 官网](https://xinstall.com/) 进行查看。

![本文结构](https://cdn.xinstall.com/DCloudUniapp%E7%B4%A0%E6%9D%90/v1.0.0/step0.png)



## 二、如何接入

### 1、在 React Native 工程中安装模块

在需要集成的 React Native 工程根目录下，执行：

```shell
npm install xinstall-react-native --save
```

如果该工程使用的 React Native 版本 < 0.60.0 那么需要执行 link 命令：

```
npx react-native link xinstall-react-native
```

接下来需要针对 iOS 端进行 pod 安装 xinstall 的包：

```shell
// 在 React Native 工程根目录下依次执行
cd ios
pod install
cd ..
```



【注意】如果 pod install 失败，或者您的 iOS 工程是通过 pod 集成 React Native，那么可以按照如下步骤进行 iOS 端集成：

* 在 ios/Podfile 文件中添加本地 path：

  ```ruby
  pod 'xinstall-react-native', :path => '../node_modules/xinstall-react-native'
  ```

* 然后在 ios/ 目录下执行：

  ```shell
  pod install
  ```

  

### 2、创建 Xinstall 应用

进入 [Xinstall 官网](https://xinstall.com/) 注册账号，并在控制台中创建一个对应的应用，应用名字可以任意填写：

![创建应用](https://cdn.xinstall.com/DCloudUniapp%E7%B4%A0%E6%9D%90/v1.0.0/step4.png)

注意记录 Xinstall 中新创建应用的 appkey（后续配置需要用到）：

![记录appkey](https://cdn.xinstall.com/DCloudUniapp%E7%B4%A0%E6%9D%90/v1.0.0/step5.png)

接入过程中如有任何疑问或者困难，可以随时联系 [Xinstall 官方客服](https://xinstall.com/) 在线解决。



### 3、初始化配置

#### 3.1、获取必要参数

* appKey：从 Xinstall 平台获取的 AppKey，在上一步骤中可以获取到
* scheme：详细获取位置：Xinstall 应用控制台 -> Android下载配置 中获取



#### 3.2、自动初始化 xinstall-react-native 模块

进入 React Native 工程根目录，执行：

```shell
npm run initxinstall <appKey> <scheme>
```

【注意】：appKey 和 scheme 分别替换成 3.1 中具体的值（两边的尖括号不需要，去掉即可）

> 如果执行这步时，终端中如果因为报错而导致自动初始化失败，那么可以参考这篇文章进行：[手动初始化 xinstall-react-native 模块](https://github.com/xinstall/Xinstall-React-Native-SDK/tree/master/doc)



#### 3.3、Universal links 相关配置（针对 iOS）

**Xinstall** 通过 Universal links（iOS ≥ 9）技术，在 App 已安装的情况下，从各种浏览器（包括微信、QQ、新浪微博、钉钉等主流社交软件的内置浏览器）拉起 App 并传递动态参数，避免重复安装。

**开启 Associated Domains 服务**

对于 iOS，为确保能正常使用一键拉起功能，AppID 必须开启 Associated Domains 功能，请到苹果开发者网站，选择 “Certificate, Identifiers & Profiles”，选择 iOS 对应的 AppID，开启 Associated Domains：

![开启 Associated Domains 服务](https://cdn.xinstall.com/DCloudUniapp%E7%B4%A0%E6%9D%90/v1.0.0/step9.png)

为刚才开发关联域名功能的 App ID 创建新的（或更新现有的）描述文件，下载并导入到 Xcode 中（通过 Xcode 自动生成的描述文件，可跳过这一步）：

![更新描述文件](https://doc.xinstall.com/integrationGuide/iOS2.png)

**配置 Universal links 关联域名**

在 Xcode 中配置 Xinstall 为当前应用生成的关联域名（Associated Domains）：**applinks:xxxx.xinstall.top** 和 **applinks:xxxx.xinstall.net**

> 具体的关联域名可在 Xinstall管理后台 - 对应的应用控制台 - iOS下载配置 页面中找到

![配置 Universal links 关联域名](https://doc.xinstall.com/ReactNative/res/3.png)



### 4、导出包上传 Xinstall

代码集成完毕后，需要通过 React Native 导出 iOS 和 Android 离线包（.ipa 和 .apk），并上传 Xinstall 控制台里对应的 App：

**示例图片（iOS 端）：** ![img](https://cdn.xinstall.com/DCloudUniapp%E7%B4%A0%E6%9D%90/v1.0.0/step11.png)

**示例图片（Android 端）：** ![img](https://cdn.xinstall.com/DCloudUniapp%E7%B4%A0%E6%9D%90/v1.0.0/step12.png)

上传完包后，需要进入 「iOS应用适配」和「Android应用配置」中选择下载的包的版本

**示例图片（iOS 端）：** ![img](https://cdn.xinstall.com/DCloudUniapp%E7%B4%A0%E6%9D%90/v1.0.0/step13.png)

![img](https://cdn.xinstall.com/DCloudUniapp%E7%B4%A0%E6%9D%90/v1.0.0/step14.png)

**示例图片（Android 端）：** ![img](https://cdn.xinstall.com/DCloudUniapp%E7%B4%A0%E6%9D%90/v1.0.0/step15.png)

![img](https://cdn.xinstall.com/DCloudUniapp%E7%B4%A0%E6%9D%90/v1.0.0/step16.png)

> 注意：每次上传完新的 ipa 或者 apk 包后，均需要进入「iOS应用适配」和「Android应用配置」中重新选择下载的包的版本





## 三、如何使用

### 1、快速下载和一键拉起

如果只需要快速下载功能和一键拉起，无需其它功能（携带参数安装、渠道统计），完成初始化配置即可。其他影响因素如下图
![](https://xinstall-static-pro.oss-cn-hangzhou.aliyuncs.com/APICloud%E7%B4%A0%E6%9D%90/v1.1.0/xinstall_yjlqksaztj.png)



### 2、初始化 Xinstall 模块

> 注意：从 v1.5.0 版本开始，在调用 Xinstall 插件的任意方法之前，必须调用一次初始化方法，只需要调用一次即可，不需要反复调用。
>
> v1.5.0 之前的版本会在启动时自动初始化，无需调用，也无法调用。

#### init

初始化方法。在调用 Xinstall 插件其他方法之前必须调用一次该方法，否则其他方法均无法正常执行。

**示例代码**

`init()`

**入参说明**：无需主动传入参数

**回调说明**：无需传入回调函数

**调用示例**

```javascript
// 在文件头部导入 xinstall 模块
import xinstall from 'xinstall-react-native'
// 在代码中调用 init 方法初始化
xinstall.init();
```

**可用性**

Android系统，iOS系统

可提供的 1.5.0 及更高版本



### 3、携带参数安装/唤起

> 注意：调用该功能对应接口时需要在 Xinstall 中为对应 App 开通专业版服务

在 APP 需要安装参数时（由 web 网页中传递过来的，如邀请码、游戏房间号等动态参数），调用此方法，在回调中获取 web 中传递过来的参数，参数在 App 被一键唤起（拉起），或在快速下载第一次打开应用时候，会传递过来，App端可以分别通过 `addWakeUpEventListener ` 和 `addInstallEventListener` 两个方法进行获取

**在需要使用到 js 文件中，必须导入 xinstall 模块：**

```javascript
// 例如在 App.js 文件中
import xinstall from 'xinstall-react-native'
```

#### addWakeUpEventListener

添加唤醒应用事件监听者。添加 `addWakeUpEventListener` 监听,当从其他应用一键唤起（拉起）本App时候，监听回调函数里可保存唤醒数据供后续业务使用。

**示例代码**

`addWakeUpEventListener(callback)`

**入参说明**：callback 为唤醒回调数据

**回调说明**：传入监听回调 callback(result)

result：

类型：JSON对象

内部字段：

```
// 如果唤醒时没有携带任何参数，result 为一个空 json 对象：
{}

// 如果唤醒时有任意参数，result 为 json 对象，内部字段为：
{
    "channelCode":"渠道编号",  // 字符串类型。渠道编号，没有渠道编号时为 ""
    "data":{                                    // 对象类型。唤起时携带的参数。
        "co":{                              // co 为唤醒页面中通过 Xinstall Web SDK 中的点击按钮传递的数据，key & value 均可自定义，key & value 数量不限制
            "自定义key1":"自定义value1", 
            "自定义key2":"自定义value2"
        },
        "uo":{                              // uo 为唤醒页面 URL 中 ? 后面携带的标准 GET 参数，key & value 均可自定义，key & value 数量不限制
            "自定义key1":"自定义value1",
            "自定义key2":"自定义value2"
        }
    }
}
```

**调用示例**

请在 `componentDidMount` 方法中添加监听：

```javascript
componentDidMount() {
  // 该方法用于监听 App 通过 Univeral link 或 scheme 拉起后获取唤醒参数
  xinstall.addWakeUpEventListener(result => {
    // 回调函数将在合适的时机被调用，这里编写拿到渠道编号以及唤醒参数后的业务逻辑代码
    
    // 空对象时代表唤醒了，但是没有任何参数传递进来
    if (JSON.stringify(result) == '{}') {
      // 业务逻辑
    } else {
      var channelCode = result.channelCode;
      var data = result.data;
      var co = data.co;
      var uo = data.uo;
      // 根据获取到的数据做对应业务逻辑
    }
  })
}
```

在 `componentWillUnmount` 方法中移除监听：

```javascript
componentWillUnmount() {
  xinstall.removeWakeUpEventListener()
}
```

**补充说明**

此方法用于获取动态唤醒参数，通过动态参数，在拉起APP时，获取由 web 网页中传递过来的，如邀请码、游戏房间号等自定义参数，通过注册监听后，获取 web 端传过来的自定义参数。请严格遵循示例中的调用顺序，否则可能导致获取不到唤醒参数。

**可用性**

Android系统，iOS系统

可提供的 1.0.0 及更高版本




#### addInstallEventListener

添加携带参数安装事件监听者。快速安装完成后，在 App 启动时（一般为入口程序处）需要通过 `addInstallEventListener ` 该方法添加监听者。监听回调函数里可保存安装参数供后续业务使用。

**示例代码**

`addInstallEventListener(callback)`

**入参说明**：callback 为安装回调数据

**回调说明**：传入监听回调 callback(result)

result：

类型：JSON对象

内部字段：

```
// 如果没有获取到安装时携带的参数，result 为一个空 json 对象：
{}

// 获取到了安装时携带的参数，result 为 json 对象，内部字段为：
{
    "channelCode":"渠道编号",  // 字符串类型。渠道编号，没有渠道编号时为 ""
    "data":{                                    // 对象类型。安装时携带的参数。
        "co":{                              // co 为唤醒页面中通过 Xinstall Web SDK 中的点击按钮传递的数据，key & value 均可自定义，key & value 数量不限制
            "自定义key1":"自定义value1", 
            "自定义key2":"自定义value2"
        },
        "uo":{                              // uo 为唤醒页面 URL 中 ? 后面携带的标准 GET 参数，key & value 均可自定义，key & value 数量不限制
            "自定义key1":"自定义value1",
            "自定义key2":"自定义value2"
        }
    },
    "timeSpan": 12, // 数字类型。代表下载页面上点击开始下载按钮与第一次打开App时的时间间隔，单位为秒
    "isFirstFetch": true // boolean类型。代表是否为第一次获取到安装参数，只有第一次获取到时为 true
}
```

**调用示例**

```js
xinstall.addInstallEventListener(result => {
  // 回调函数将在合适的时机被调用，这里编写拿到渠道编号以及携带参数后的业务逻辑代码
    
  // 空对象时代表没有获取到安装参数
  if (JSON.stringify(result) == '{}') {
    // 业务逻辑
  } else {
    var channelCode = result.channelCode;
    var data = result.data;
    var co = data.co;
    var uo = data.uo;
    var timeSpan = result.timeSpan;
    var isFirstFetch = result.isFirstFetch;
    // 根据获取到的数据做对应业务逻辑
  }
})
```

**补充说明**

此接口用于获取动态安装参数，测试时候建议卸载再安装正确获取参数，在 APP 需要个性化安装参数时（由 web 网页中传递过来的，如邀请码、游戏房间号等自定义参数），在回调中获取参数，可实现跳转指定页面、统计渠道数据等。**调用该函数的时机建议越早越好，尽量在程序启动时进行注册，以免错过回调时机。**

**可用性**

Android系统，iOS系统

可提供的 1.0.0 及更高版本



### 4、渠道统计

> 注意：调用该功能对应接口时需要在 Xinstall 中为对应 App 开通专业版服务

#### 4.1、注册量统计

在业务中合适的时机（一般指用户注册）调用指定方法上报注册量



#### reportRegister

**示例代码**

`reportRegister()`

**入参说明**：无需主动传入参数

**调用示例**

``` js
xinstall.reportRegister()
```

**补充说明**

Xinstall 会自动完成安装量、留存率、活跃量、在线时长等渠道统计数据的上报工作，如需统计每个渠道的注册量（对评估渠道质量很重要），可根据自身的业务规则，在确保用户完成 app 注册的情况下，调用 `reportRegister()` 上报注册量。 在 Xinstall 平台即可看到注册量。

**可用性**

Android系统，iOS系统

可提供的 1.0.0 及更高版本



#### 4.2、事件统计

事件统计，主要用来统计终端用户对于某些特殊业务的使用效果，如充值金额，分享次数，广告浏览次数等等。

调用接口前，需要先进入 Xinstall 管理后台**事件统计**然后点击新增事件。



#### reportEventPoint

**示例代码**

`reportEventPoint(eventId, eventValue)`

**入参说明**：需要主动传入2个参数

- eventId：类型：字符串；描述：事件ID，与 Xinstall 后台创建的事件 ID 对应
- eventValue：类型：数字类型；描述：事件值

**调用示例**

``` js
xinstall.reportPoint('createOrder', 13);
```

**补充说明**

只有 Xinstall 后台创建事件统计，并且代码中传递的事件ID与后台创建的ID一致时，上报数据才会被统计。

**可用性**

Android系统，iOS系统

可提供的 1.0.0 及更高版本



### 5、广告平台渠道功能

>  如果您在 Xinstall 管理后台对应 App 中，**只使用「自建渠道」，而不使用「广告平台渠道」，则无需进行本小节中额外的集成工作**，也能正常使用 Xinstall 提供的其他功能。
>
>  注意：根据目前已有的各大主流广告平台的统计方式，目前 iOS 端和 Android 端均需要用户授权并获取一些设备关键值后才能正常进行 [ 广告平台渠道 ] 的统计，如 IDFA / OAID / GAID 等，对该行为敏感的 App 请慎重使用该功能。

#### 5.1、配置工作

**iOS 端：**

在 Xcode 中打开 iOS 端的工程，在 `Info.plist` 文件中配置一个权限作用声明（如果不配置将导致 App 启动后马上闪退）：

```xml
<key>NSUserTrackingUsageDescription</key>
<string>这里是针对获取 IDFA 的作用描述，请根据实际情况填写</string>
```

**Android 端：**

相关接入可以参考广告平台联调指南中的[《Android集成指南》](https://doc.xinstall.com/AD/AndroidGuide.html)

1. 接入IMEI需要额外的全下申请，需要在`AndroidManifest`中添加权限声明

   ```java
   <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
   ```

2. 如果使用OAID，因为内部使用反射获取oaid 参数，所以都需要外部用户接入OAID SDK 。具体接入参考[《Android集成指南》](https://doc.xinstall.com/AD/AndroidGuide.html)



#### 5.2、更换初始化方法

**使用新的 initWithAd 方法，替代原先的 init 方法来进行模块的初始化**

> iOS 端由于 React Native 官方没有提供获取 IDFA 的模块，故我们这里需要使用第三方的 NPM 模块来获取 IDFA。经过多方面测试，目前比较推荐使用 [@sparkfabrik/react-native-idfa-aaid](https://www.npmjs.com/package/@sparkfabrik/react-native-idfa-aaid) 这个模块来获取 IDFA，下文的代码示例中也将使用该模块进行说明。

#### initWithAd

**入参说明**：需要主动传入参数，JSON对象

入参内部字段：

* iOS 端：
  <table>
         <tr>
             <th>参数名</th>
             <th>参数类型</th>
             <th>描述 </th>
         </tr>
         <tr>
             <th>idfa</th>
             <th>字符串</th>
             <th>iOS 系统中的广告标识符</th>
         </tr>
     </table>


* Android 端：

  <table>
            <tr>
                <th>参数名</th>
                <th>参数类型</th>
                <th>描述 </th>
            </tr>
            <tr>
                <th>adEnabled</th>
                <th>boolean</th>
                <th>是否使用广告功能</th>
            </tr>
    				<tr>
                <th>oaid （可选）</th>
                <th>string</th>
                <th>OAID</th>
            </tr>
    				<tr>
                <th>gaid（可选）</th>
                <th>string</th>
                <th>GaID(google Ad ID)</th>
            </tr>
        </table>



**回调说明**：无需传入回调函数

**调用示例**

```javascript
import {Platform} from 'react-native';
import xinstall from 'xinstall-react-native';
import ReactNativeIdfaAaid, { AdvertisingInfoResponse } from '@sparkfabrik/react-native-idfa-aaid';

// 由于 iOS 和 Android 两端需要传入的参数不同，故需要根据平台进行判断，传入不同的参数
if (Platform.OS === 'ios') {
    ReactNativeIdfaAaid.getAdvertisingInfo()
    .then((res: AdvertisingInfoResponse) => {
      if (!res.isAdTrackingLimited) {
        console.log("成功获取到 IDFA");
        xinstall.initWithAd(res.id);
      } else {
        console.log("没有获取 IDFA 的权限");
        // 没有获取到 IDFA 的时候也需要执行 initWithAd 方法，否则无法正常使用 xinstall 模块
        xinstall.initWithAd("");
      }
    })
    .catch((err) => {
      console.log("获取 IDFA 出现异常：" + err);
      // 获取出现异常的时候也需要执行 initWithAd 方法，否则无法正常使用 xinstall 模块
      xinstall.initWithAd("");
    });
} else if (uni.getSystemInfoSync().platform == 'android') {
	    var that = this
  	  function permissionBackFun() {
  	  	   // --  xinstall.addWakeUpEventListener 或者 xinstall.addInstallEventListener 等操作
  	  }
     // oaid和gaid 为选传，不传则代表使用SDK自动去获取（SDK内不包括OAID SDK，需要自己接入）
  	 xinstall.initWithAd({adEnable:true,oaid:"测试oaid",gaid:"测试gaid"});
     // 如果希望在完成初始化，立即执行之后的步骤可以通过 下列代码实现-------------------------
     // xinstall.initWithAd({adEnable:true,oaid:"测试oaid",gaid:"测试gaid"},permissionBackFun);
     // -----------------------------------------------------------------------------
  
  
}
```



**可用性**

Android系统，iOS系统

可提供的 1.5.0 及更高版本



#### 5.3、上架须知

**在使用了广告平台渠道后，若您的 App 需要上架，请认真阅读本段内容。**

##### 5.3.1 iOS 端：上架 App Store

1. 如果您的 App 没有接入苹果广告（即在 App 中显示苹果投放的广告），那么在提交审核时，在广告标识符中，请按照下图勾选：

![IDFA](https://cdn.xinstall.com/iOS_SDK%E7%B4%A0%E6%9D%90/IDFA_7.png)



1. 在 App Store Connect 对应 App —「App隐私」—「数据类型」选项中，需要选择：**“是，我们会从此 App 中收集数据”**：

![AppStore_IDFA_1](https://cdn.xinstall.com/iOS_SDK%E7%B4%A0%E6%9D%90/IDFA_1.png)

在下一步中，勾选「设备 ID」并点击【发布】按钮：

![AppStore_IDFA_2](https://cdn.xinstall.com/iOS_SDK%E7%B4%A0%E6%9D%90/IDFA_2.png)

点击【设置设备 ID】按钮后，在弹出的弹框中，根据实际情况进行勾选：

- 如果您仅仅是接入了 Xinstall 广告平台而使用了 IDFA，那么只需要勾选：**第三方广告**
- 如果您在接入 Xinstall 广告平台之外，还自行使用 IDFA 进行别的用途，那么在勾选 **第三方广告** 后，还需要您根据您的实际使用情况，进行其他选项的勾选

![AppStore_IDFA_3](https://cdn.xinstall.com/iOS_SDK%E7%B4%A0%E6%9D%90/IDFA_3.png)

![AppStore_IDFA_4](https://cdn.xinstall.com/iOS_SDK%E7%B4%A0%E6%9D%90/IDFA_4.png)

勾选完成后点击【下一步】按钮，在 **“从此 App 中收集的设备 ID 是否与用户身份关联？”** 选项中，请根据如下情况进行选择：

- 如果您仅仅是接入了 Xinstall 广告平台而使用了 IDFA，那么选择 **“否，从此 App 中收集的设备 ID 未与用户身份关联”**
- 如果您在接入 Xinstall 广告平台之外，还自行使用 IDFA 进行别的用途，那么请根据您的实际情况选择对应的选项

![AppStore_IDFA_5](https://cdn.xinstall.com/iOS_SDK%E7%B4%A0%E6%9D%90/IDFA_5.png)

最后，在弹出的弹框中，选择 **“是，我们会将设备 ID 用于追踪目的”**，并点击【发布】按钮：

![AppStore_IDFA_6](https://cdn.xinstall.com/iOS_SDK%E7%B4%A0%E6%9D%90/IDFA_6.png)





## 四、如何测试功能

参考官方文档 [测试集成效果](https://doc.xinstall.com/integrationGuide/comfirm.html)



## 五、更多 Xinstall 进阶功能
若您想要自定义下载页面，或者查看数据报表等进阶功能，请移步 [Xinstall 官网](https://xinstall.com/) 查看对应文档。

若您在集成过程中如有任何疑问或者困难，可以随时联系 [Xinstall 官方客服](https://xinstall.com/) 在线解决。

