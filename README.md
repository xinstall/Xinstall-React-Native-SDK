# React Native 接入
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
* scheme：详细获取位置：Xinstall 应用控制台-> Android 集成-> 功能集成 中获取



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


### 2、携带参数安装/唤起

> 注意：调用该功能对应接口时需要在 Xinstall 中为对应 App 开通专业版服务

在 APP 需要安装参数时（由 web 网页中传递过来的，如邀请码、游戏房间号等动态参数），调用此方法，在回调中获取 web 中传递过来的参数，参数在 App 被一键唤起（拉起），或在快速下载第一次打开应用时候，会传递过来，App端可以分别通过 `addWakeUpEventListener ` 和 `addInstallEventListener` 两个方法进行获取

#### 2.1、在需要使用到 js 文件中，必须导入 xinstall 模块：

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
{
    'channelCode': '渠道编号', //渠道编号
    'data': '唤醒携带的参数'   //有携带参数，则返回数据，没有则为空
}
```

**调用示例**

请在 `componentDidMount` 方法中添加监听：

```javascript
componentDidMount() {
  // 该方法用于监听 App 通过 Univeral link 或 scheme 拉起后获取唤醒参数
  xinstall.addWakeUpEventListener(result => {
    // 回调函数将在合适的时机被调用，这里编写拿到渠道编号以及唤醒参数后的业务逻辑代码
    var channelCode = result.channelCode;
    var data = result.data;
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
{
    'channelCode': '渠道编号', 			//渠道编号
    'data': '个性化安装携带的参数',   //有携带参数，则返回数据，没有则为空
    'isFirstFetch': true  // true 或者 false。代表是否为第一次获取到安装参数，只有第一次获取到时为 true
}
```

**调用示例**

```js
xinstall.addInstallEventListener(result => {
    // 回调函数将在合适的时机被调用，这里编写拿到渠道编号以及携带参数后的业务逻辑代码
    var channelCode = result.channelCode;
    var data = result.data;
    var isFirstFetch = result.isFirstFetch;
})
```

**补充说明**

此接口用于获取动态安装参数，测试时候建议卸载再安装正确获取参数，在 APP 需要个性化安装参数时（由 web 网页中传递过来的，如邀请码、游戏房间号等自定义参数），在回调中获取参数，可实现跳转指定页面、统计渠道数据等。**调用该函数的时机建议越早越好，尽量在程序启动时进行注册，以免错过回调时机。**

**可用性**

Android系统，iOS系统

可提供的 1.0.0 及更高版本



### 3、高级数据统计

> 注意：调用该功能对应接口时需要在 Xinstall 中为对应 App 开通专业版服务



#### 3.1、注册量统计

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



#### 3.2、事件统计

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




## 三、导出apk/ipa包并上传

参考官网文档

[iOS集成-导出ipa包并上传](https://doc.xinstall.com/integrationGuide/iOSIntegrationGuide.html#%E5%9B%9B%E3%80%81%E5%AF%BC%E5%87%BAipa%E5%8C%85%E5%B9%B6%E4%B8%8A%E4%BC%A0)

[Android集成-导出apk包并上传](https://doc.xinstall.com/integrationGuide/AndroidIntegrationGuide.html#%E5%9B%9B%E3%80%81%E5%AF%BC%E5%87%BAapk%E5%8C%85%E5%B9%B6%E4%B8%8A%E4%BC%A0)



## 四、如何测试功能

参考官方文档 [测试集成效果](https://doc.xinstall.com/integrationGuide/comfirm.html)



## 五、更多 Xinstall 进阶功能
若您想要自定义下载页面，或者查看数据报表等进阶功能，请移步 [Xinstall 官网](https://xinstall.com/) 查看对应文档。

若您在集成过程中如有任何疑问或者困难，可以随时联系 [Xinstall 官方客服](https://xinstall.com/) 在线解决。


