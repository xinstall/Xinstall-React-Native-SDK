# iOS手动集成文档
## 一、pod 安装 xinstall
进入 React Native 根目录，再进入 ios 目录，修改 Podfile 文件，在需要集成的 target 下新增：

```
pod 'xinstall-react-native', :path => '../node_modules/xinstall-react-native'
```

然后在 ios 目录下执行：

```
pod install
```



## 二、初始化

在 Info.plist 文件中配置 appKey 键值对，如下：

```xml
<key>com.xinstall.APP_KEY</key>
<string>这里替换成你的appKey</string>
```

在 Info.plist 文件中配置 scheme 键值对，如下：

* 如果当前 Info.plist 中没有 `<key>CFBundleURLTypes</key>`  ，那么直接新增：

```xml
<key>CFBundleURLTypes</key>
	<array>
		<dict>
			<key>CFBundleTypeRole</key>
			<string>Editor</string>
			<key>CFBundleURLName</key>
			<string>xinstall</string>
			<key>CFBundleURLSchemes</key>
			<array>
				<string>这里替换成你的scheme</string>
			</array>
		</dict>
	</array>
```

如果当前 Info.plist 中已经有 `<key>CFBundleURLTypes</key>`  ，那么在如下位置新增：

```xml
<key>CFBundleURLTypes</key>
	<array>
		// 下方是新增的内容，记得删除这行注释
		<dict>
			<key>CFBundleTypeRole</key>
			<string>Editor</string>
			<key>CFBundleURLName</key>
			<string>xinstall</string>
			<key>CFBundleURLSchemes</key>
			<array>
				<string>这里替换成你的scheme</string>
			</array>
		</dict>
		// 上方是新增的内容，记得删除这行注释
		<dict>
			<key>CFBundleTypeRole</key>
			<string>Editor</string>
			<key>CFBundleURLName</key>
			<string>weibo</string>
			<key>CFBundleURLSchemes</key>
			<array>
				<string>xxxxxxx</string>
			</array>
		</dict>
	</array>
```

在 AppDelegate.m 中，增加头文件的引用：

```objective-c
#import <xinstall-react-native/XinstallRNSDK.h>
```

## 三、配置 Universal Links 相关代码

在 AppDelegate 中添加通用链接 (Universal Link) 回调方法，委托给 XinstallSDK 来处理

```objective-c
- (BOOL)application:(UIApplication *)application continueUserActivity:(NSUserActivity *)userActivity restorationHandler:(void (^)(NSArray * _Nullable))restorationHandler{
  [XinstallSDK continueUserActivity:userActivity];
  return YES;
}
```

**注意iOS13之后如果有SceneDelegate, 则调用的方法需要放SceneDelegate,低版本暂不支持SceneDelegate，可以直接跳过该配置**

```objective-c
// 先在 SceneDelegate.m 中导入头文件
#import <xinstall-react-native/XinstallRNSDK.h>

// 注意iOS13之后如果有SceneDelegate, 则调用的方法需要放SceneDelegate,低版本暂不支持SceneDelegate，可以直接跳过该配置
- (void)scene:(UIScene *)scene willConnectToSession:(UISceneSession *)session options:(UISceneConnectionOptions *)connectionOptions  API_AVAILABLE(ios(13.0)) {
    // 走scene 如果是universal Link 冷启动不会调SceneDelegate 的 - (void)scene:(UIScene *)scene continueUserActivity:(NSUserActivity *)userActivity 方法
    if (connectionOptions.userActivities.count > 0 ){
        [connectionOptions.userActivities enumerateObjectsUsingBlock:^(NSUserActivity * _Nonnull obj, BOOL * _Nonnull stop) {
            if ([XinstallSDK continueUserActivity:obj]) {
                 *stop = YES;
            }
        }];
    }
}

- (void)scene:(UIScene *)scene continueUserActivity:(NSUserActivity *)userActivity {
  if (![XinstallSDK continueUserActivity:userActivity]) {
    // 其他第三方回调 
  }
}
```



## 四、配置 scheme 相关代码

```objective-c
// iOS9 以上会优先走这个方法
- (BOOL)application:(UIApplication *)app openURL:(NSURL *)url options:(NSDictionary<UIApplicationOpenURLOptionsKey,id> *)options {
	// 处理通过Xinstall URL SchemeURL 唤起App的数据
	[XinstallSDK handleSchemeURL:url];
	return YES;
}

// iOS9 以下调用这个方法
- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url sourceApplication:(nullable NSString *)sourceApplication annotation:(id)annotation {
	// 处理通过Xinstall URL SchemeURL 唤起App的数据
	[XinstallSDK handleSchemeURL:url];
	return YES;
}
```

**注意iOS13之后如果有SceneDelegate, 则调用的方法需要放SceneDelegate, 低版本暂不支持SceneDelegate, 可以直接跳过该配置**

```objective-c
- (void)scene:(UIScene *)scene willConnectToSession:(UISceneSession *)session options:(UISceneConnectionOptions *)connectionOptions  API_AVAILABLE(ios(13.0)) {
    // scheme相关代码
    for (UIOpenURLContext *urlcontext in connectionOptions.URLContexts) {
            [XinstallSDK handleSchemeURL:urlcontext.URL];
    }
}

- (void)scene:(UIScene *)scene openURLContexts:(NSSet<UIOpenURLContext *> *)URLContexts API_AVAILABLE(ios(13.0)) {
    for (UIOpenURLContext *urlcontext in URLContexts) {
    	// scheme相关代码
        [XinstallSDK handleSchemeURL:urlcontext.URL];
    }
}
```



> 手动集成完成后，需要继续根据集成文档完成所有集成步骤后才可正常进行测试。
>
> 手动集成过程中如有任何疑问，可以联系 [Xinstall 客服](https://www.xinstall.com/) 进行协助处理。



