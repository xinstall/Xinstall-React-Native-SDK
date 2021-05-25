var fs = require('fs');
var path = require('path');
const { spawnSync, spawn, fork } = require('child_process');

// 检验入参 appkey
var appKey = process.argv[2];
if(appKey == null || appKey == undefined){
	logErrorMsg("请输入 appKey 参数后重新执行");
	return;
}

// 处理入参 scheme
var scheme = process.argv[3];
if(scheme == null || scheme == undefined){
	scheme = "xi" + appKey;
}

// 保存当前工作路径（绝对路径）
const rnProjFullPath = process.cwd();


logImportantMsg("开始配置 Xinstall")
logImportantMsg("开始配置 iOS 端")

// 开始配置 iOS 端
// 查找 iOS 工程目录
if (!fs.existsSync("./ios")) {
	logErrorMsg("无法找到 iOS 端工程，请手动进行配置");
	return;
}
// 依次配置必要的文件
var configIOSResult = true;
configIOSResult = configIOSPod();
if (!configIOSResult) {
	return;
}
configIOSResult = configIOSPlist()
if (!configIOSResult) {
	return;
}
configIOSResult = configIOSAppDelegate()
if (!configIOSResult) {
	return;
}
configIOSResult = configIOSSceneDelegate()
if (!configIOSResult) {
	return;
}
logImportantMsg("结束配置 iOS 端")

logImportantMsg("开始配置 Android 端")

// 遍历 app module
findAndroidManifest(path.resolve("./android/app/src/main/"), 5, function(f, s){
	var manifestXml = f.match(/AndroidManifest\.xml/);
	if(manifestXml != null){
		configAndroidManifest(f);
	}
});

logImportantMsg("结束配置 Android 端")



// 返回 false 代表配置失败，返回 true 代表配置成功
function configIOSPod() {
	logNormalMsg("开始检查 pod 是否正常安装 xinstall-react-native");
	// 判断是否存在 Podfile 文件
	var podfileFullPath = findFile("./ios", "Podfile");
	if (podfileFullPath == "") {
		logErrorMsg("未检查到 Podfile 文件，请手动进行配置");
		return false;
	}

	// 判断是否已经 pod install xinstall-react-native
	var needPodInstall = false;
	var podfilelockFullPath = findFile("./ios", "Podfile.lock");
	if (podfilelockFullPath != "") {
		var podfilelockContent = fs.readFileSync(podfilelockFullPath,"utf-8");
		if (podfilelockContent.indexOf("xinstall-react-native") <= 0) {
			needPodInstall = true;
		}
	} else {
		needPodInstall = true;
	}

	// 如果需要执行 pod install
	if (needPodInstall) {
		logNormalMsg("开始执行 pod install");
		process.chdir("ios");
		spawnSync('pod', [`install`]);
		process.chdir(rnProjFullPath);
	}

	// 执行完 pod install 后需要再次检查 podfile.lock
	podfilelockFullPath = findFile("./ios", "Podfile.lock");
	if (podfilelockFullPath != "") {
		var podfilelockContent = fs.readFileSync(podfilelockFullPath,"utf-8");
		if (podfilelockContent.indexOf("xinstall-react-native") <= 0) {
			logErrorMsg("pod install 失败，请手动进行配置");
			return false;
		}
	} else {
		logErrorMsg("pod install 失败，请手动进行配置");
		return false;
	}

	return true;
}

function configIOSPlist() {
	logNormalMsg("开始配置 plist 文件");

	var configSuccess = false;
	findFileWithProcess("./ios", "Pods", "Info.plist", (plistFullPath) => {

		// 在 plist 中插入 appKey
		var plistContent = fs.readFileSync(plistFullPath,"utf-8");
		if (plistContent.indexOf('<string>BNDL</string>') > -1) {
			return;
		}

		var startContent = plistContent.match(/<plist .*>\n?<dict>/);
		// 已经配置过，则直接更改 appkey，如果没有配置过，则新增配置进去
		if (plistContent.indexOf('<key>com.xinstall.APP_KEY</key>') > -1) {
			var appKeyContent = plistContent.match(/<key>com.xinstall.APP_KEY<\/key>\n?.*<string>.*<\/string>/);
			plistContent = plistContent.replace(appKeyContent[0], "\n\t<key>com.xinstall.APP_KEY</key>" + "\n\t<string>" + appKey + "</string>");
		} else {
			plistContent = plistContent.replace(startContent[0], startContent[0] + "\n\t<key>com.xinstall.APP_KEY</key>" + "\n\t<string>" + appKey + "</string>");
		}

		// 在 plist 中插入 scheme
		startContent = plistContent.match(/<plist .*>\n?<dict>/);
		// 检查是否有 URL scheme 存在
		if (plistContent.indexOf('<key>CFBundleURLTypes</key>') > -1) {
			var appKeyContent = plistContent.match(/<key>CFBundleURLTypes<\/key>\n?.*<array>/);
			plistContent = plistContent.replace(appKeyContent[0], appKeyContent[0]
				+ "\n\t\t<dict>"
				+ "\n\t\t\t<key>CFBundleTypeRole</key>"
				+ "\n\t\t\t<string>Editor</string>"
				+ "\n\t\t\t<key>CFBundleURLName</key>"
				+ "\n\t\t\t<string>xinstall</string>"
				+ "\n\t\t\t<key>CFBundleURLSchemes</key>"
				+ "\n\t\t\t<array>"
				+ "\n\t\t\t\t<string>" + scheme + "</string>"
				+ "\n\t\t\t</array>"
				+ "\n\t\t</dict>"
			);
		} else {
			plistContent = plistContent.replace(startContent[0], startContent[0]
				+ "\n\t<key>CFBundleURLTypes</key>"
				+ "\n\t<array>"
				+ "\n\t\t<dict>"
				+ "\n\t\t\t<key>CFBundleTypeRole</key>"
				+ "\n\t\t\t<string>Editor</string>"
				+ "\n\t\t\t<key>CFBundleURLName</key>"
				+ "\n\t\t\t<string>xinstall</string>"
				+ "\n\t\t\t<key>CFBundleURLSchemes</key>"
				+ "\n\t\t\t<array>"
				+ "\n\t\t\t\t<string>" + scheme + "</string>"
				+ "\n\t\t\t</array>"
				+ "\n\t\t</dict>"
				+ "\n\t</array>"
			);
		}

		fs.writeFileSync(plistFullPath, plistContent, "utf-8");
		configSuccess = true;

	});

	if (configSuccess == false) {
		logErrorMsg("未找到 Info.plist，请手动进行配置");
	}

	return configSuccess;
}

function configIOSAppDelegate() {
	// 查找 AppDelegate.m 文件
	logNormalMsg("开始配置 AppDelegate.m");
	var appDelegateFileFullPath = findFile("./ios", "AppDelegate.m");
	if (appDelegateFileFullPath == "") {
		logErrorMsg("未找到 AppDelegate.m，请手动进行配置");
		return false;
	}
	var appDelegateContent = fs.readFileSync(appDelegateFileFullPath, "utf-8");

	// 在 AppDelegate.m 中导入头文件
	logNormalMsg("开始在 AppDelegate.m 中导入头文件");
	if (appDelegateContent.indexOf('XinstallRNSDK.h') > -1) {
		logErrorMsg("iOS 工程中已经配置过 Xinstall，停止配置 iOS，请手动进行配置");
		return false;
	}

	appDelegateContent = appDelegateContent.replace("#import \"AppDelegate.h\"","#import \"AppDelegate.h\"\n#import <xinstall-react-native/XinstallRNSDK.h>");

	// 在 AppDelegate.m 中插入 [XinstallSDK continueUserActivity:userActivity] 代码
	logNormalMsg("开始在 AppDelegate.m 中导入必要代码");
	if (appDelegateContent.indexOf('[XinstallSDK continueUserActivity') > -1) {
		logErrorMsg("iOS 工程中已经配置过 Xinstall，停止配置 iOS，请手动进行配置");
		return false;
	}

	// 写入或者修改 -application:continueUserActivity:restorationHandler: 方法
	var continueUserActivityContent = appDelegateContent.match(/\n.*continueUserActivity:\(NSUserActivity.*\n?\{/);
	if (continueUserActivityContent == null) {
		var impContent = appDelegateContent.match(/@implementation AppDelegate/);
		appDelegateContent = appDelegateContent.replace(impContent[0], impContent[0] + "\n" + "\n" + "- (BOOL)application:(UIApplication *)application continueUserActivity:(NSUserActivity *)userActivity restorationHandler:(void (^)(NSArray<id<UIUserActivityRestoring>> * _Nullable))restorationHandler {\n\t[XinstallSDK continueUserActivity:userActivity];\n\treturn YES;\n}");
	} else {
		appDelegateContent = appDelegateContent.replace(continueUserActivityContent[0], continueUserActivityContent[0] + "\n\t[XinstallSDK continueUserActivity:userActivity];");
	}

	// 写入或者修改 -application:openURL:sourceApplication:annotation: 方法
	var openUrlSourceApplicationContent = appDelegateContent.match(/\n.*sourceApplication:\(.*\n?\{/);
	if (openUrlSourceApplicationContent == null) {
		var impContent = appDelegateContent.match(/@implementation AppDelegate/);
		appDelegateContent = appDelegateContent.replace(impContent[0], impContent[0] + "\n" + "\n" + "- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url sourceApplication:(nullable NSString *)sourceApplication annotation:(id)annotation {\n\t[XinstallSDK handleSchemeURL:url];\n\treturn YES;\n}");
	} else {
		appDelegateContent = appDelegateContent.replace(openUrlSourceApplicationContent[0], openUrlSourceApplicationContent[0] + "\n\t[XinstallSDK handleSchemeURL:url];");
	}

	// 写入或者修改 -application:openURL:options: 方法
	var openUrlOptionsContent = appDelegateContent.match(/\n.*application.*\(.*openURL.*\(.*options.*\(.*\n?\{/);
	if (openUrlOptionsContent == null) {
		var impContent = appDelegateContent.match(/@implementation AppDelegate/);
		appDelegateContent = appDelegateContent.replace(impContent[0], impContent[0] + "\n" + "\n" + "- (BOOL)application:(UIApplication *)app openURL:(NSURL *)url options:(NSDictionary<UIApplicationOpenURLOptionsKey,id> *)options {\n\t[XinstallSDK handleSchemeURL:url];\n\treturn YES;\n}");
	} else {
		appDelegateContent = appDelegateContent.replace(openUrlOptionsContent[0], openUrlOptionsContent[0] + "\n\t[XinstallSDK handleSchemeURL:url];");
	}


	fs.writeFileSync(appDelegateFileFullPath, appDelegateContent, "utf-8");
	return true;
}


function configIOSSceneDelegate() {
	// 查找 SceneDelegate.m 文件
	logNormalMsg("开始配置 SceneDelegate.m");
	// 检查 SceneDelegate.m 文件是否存在，不存在则不处理，该文件不存在是个正常现象，不算配置错误
	var sceneDelegateFileFullPath = findFile("./ios", "SceneDelegate.m");
	if (sceneDelegateFileFullPath == "") {
		return true;
	}

	// 检查是否已经配置过，如果已经配置过则不再处理
	var sceneDelegateContent = fs.readFileSync(sceneDelegateFileFullPath, "utf-8");
	if (sceneDelegateContent.indexOf('XinstallSDK continueUserActivity') > -1) {
		logErrorMsg("iOS 工程中已经配置过 Xinstall，停止配置 iOS，请手动进行配置");
		return false;
	}

	// 在 SceneDelegate.m 中导入头文件
	logNormalMsg("开始在 SceneDelegate.m 中导入头文件");
	if (sceneDelegateContent.indexOf('XinstallRNSDK.h') > -1) {
		logErrorMsg("iOS 工程中已经配置过 Xinstall，停止配置 iOS，请手动进行配置");
		return false;
	}

	sceneDelegateContent = sceneDelegateContent.replace("#import \"SceneDelegate.h\"","#import \"SceneDelegate.h\"\n#import <xinstall-react-native/XinstallRNSDK.h>");

	// 判断 scene:continueUserActivity: 方法
	logNormalMsg("开始在 SceneDelegate.m 中导入必要代码");
	var continueUserActivityContent = sceneDelegateContent.match(/\n.*continueUserActivity:\(NSUserActivity.*\n?\{/);
	if (continueUserActivityContent == null) {
		var impContent = sceneDelegateContent.match(/@implementation SceneDelegate/);
		sceneDelegateContent = sceneDelegateContent.replace(impContent[0], impContent[0]
			+ "\n"
			+ "\n"
			+ "- (void)scene:(UIScene *)scene continueUserActivity:(NSUserActivity *)userActivity {"
			+ "\n\tif (![XinstallSDK continueUserActivity:userActivity]) {"
			+ "\n\t\t// 其他第三方处理"
			+ "\n\t}"
			+ "\n}"
		);
	} else {
		sceneDelegateContent = sceneDelegateContent.replace(continueUserActivityContent[0], continueUserActivityContent[0]
		  + "\n\tif (![XinstallSDK continueUserActivity:userActivity]) {"
		  + "\n\t\t// 其他第三方处理"
		  + "\n\t}"
	 	);
	}

	// 判断 scene:willConnectToSession:options: 方法
	var willConnectToSessionContent = sceneDelegateContent.match(/\n.*willConnectToSession:\(UISceneSession.*\n?\{/);
	if (willConnectToSessionContent == null) {
		var impContent = sceneDelegateContent.match(/@implementation SceneDelegate/);
		sceneDelegateContent = sceneDelegateContent.replace(impContent[0], impContent[0]
			+ "\n"
			+ "\n"
			+ "- (void)scene:(UIScene *)scene willConnectToSession:(UISceneSession *)session options:(UISceneConnectionOptions *)connectionOptions {"
			+ "\n\tif (connectionOptions.userActivities.count > 0 ){"
			+ "\n\t\t[connectionOptions.userActivities enumerateObjectsUsingBlock:^(NSUserActivity * _Nonnull obj, BOOL * _Nonnull stop) {"
			+ "\n\t\t\tif ([XinstallSDK continueUserActivity:obj]) {"
			+ "\n\t\t\t\t*stop = YES;"
			+ "\n\t\t\t}"
			+ "\n\t\t}];"
			+ "\n\t}"
			+ "\n"
			+ "\n\tfor (UIOpenURLContext *urlcontext in connectionOptions.URLContexts) {"
			+ "\n\t\t[XinstallSDK handleSchemeURL:urlcontext.URL];"
			+ "\n\t}"
			+ "\n}"
		);
	} else {
		sceneDelegateContent = sceneDelegateContent.replace(willConnectToSessionContent[0], willConnectToSessionContent[0]
			+ "\n\tif (connectionOptions.userActivities.count > 0 ){"
			+ "\n\t\t[connectionOptions.userActivities enumerateObjectsUsingBlock:^(NSUserActivity * _Nonnull obj, BOOL * _Nonnull stop) {"
			+ "\n\t\t\tif ([XinstallSDK continueUserActivity:obj]) {"
			+ "\n\t\t\t\t*stop = YES;"
			+ "\n\t\t\t}"
			+ "\n\t\t}];"
			+ "\n\t}"
			+ "\n"
			+ "\n\tfor (UIOpenURLContext *urlcontext in connectionOptions.URLContexts) {"
			+ "\n\t\t[XinstallSDK handleSchemeURL:urlcontext.URL];"
			+ "\n\t}"
		 );
	}

	var openURLContextsContent = sceneDelegateContent.match(/\n.*openURLContexts:\(NSSet.*\n?\{/);
	if (openURLContextsContent == null) {
		var impContent = sceneDelegateContent.match(/@implementation SceneDelegate/);
		sceneDelegateContent = sceneDelegateContent.replace(impContent[0], impContent[0]
			+ "\n"
			+ "\n"
			+ "- (void)scene:(UIScene *)scene openURLContexts:(NSSet<UIOpenURLContext *> *)URLContexts API_AVAILABLE(ios(13.0)) {"
			+ "\n\tfor (UIOpenURLContext *urlcontext in URLContexts) {"
			+ "\n\t\t[XinstallSDK handleSchemeURL:urlcontext.URL];"
			+ "\n\t}"
			+ "\n}"
		);
	} else {
		sceneDelegateContent = sceneDelegateContent.replace(openURLContextsContent[0], openURLContextsContent[0]
			+ "\n\tfor (UIOpenURLContext *urlcontext in URLContexts) {"
			+ "\n\t\t[XinstallSDK handleSchemeURL:urlcontext.URL];"
			+ "\n\t}"
	 	);
	}


	fs.writeFileSync(sceneDelegateFileFullPath, sceneDelegateContent, "utf-8");
	return true;
}




/**
 * 给定一个文件路径，以及一个需要查找的文件名字，在该路径下递归查找该文件
 *
 * @param  {string} rootDirPath 需要查找的根部文件夹路径
 * @param  {string} desFileName 需要查找的目标文件名字
 * @return {string} 为 "" 时代表没有找到该文件，不为空时返回该文件的绝对路径
 */
function findFile (rootDirPath, desFileName) {
  // console.log("开始查找：" + rootDirPath)

  if (!fs.existsSync(rootDirPath)) {
    return "";
  }

  var stats = fs.statSync(rootDirPath);
  if (!stats.isDirectory()) {
    return "";
  }

	var desFileFullPath = "";
	var filesArray = fs.readdirSync(rootDirPath);
	for (var i = 0; i < filesArray.length; i++) {
		var fileName = filesArray[i];
		if (desFileFullPath != "") {
			break;
		}
    var fullFilePath = path.join(rootDirPath, fileName);
    var stats = fs.statSync(fullFilePath);
    if (stats.isDirectory()) {
      desFileFullPath = findFile(fullFilePath, desFileName);
    } else {
      if (desFileName == fileName) {
        desFileFullPath = fullFilePath;
        // console.log("已经找到：" + desFileFullPath)
      }
    }
	}

  return desFileFullPath;
}

function findFileWithProcess(rootDirPath, excludeDirName, desFileName, handler) {
	if (!fs.existsSync(rootDirPath)) {
    return;
  }

  var stats = fs.statSync(rootDirPath);
  if (!stats.isDirectory()) {
		return;
  }

	if (handler == undefined || typeof(handler) != "function") {
		return;
	}


	var filesArray = fs.readdirSync(rootDirPath);
	for (var i = 0; i < filesArray.length; i++) {
		var fileName = filesArray[i];

    var fullFilePath = path.join(rootDirPath, fileName);
    var stats = fs.statSync(fullFilePath);
    if (stats.isDirectory()) {
			if (fullFilePath.indexOf(excludeDirName) == -1) {
				findFileWithProcess(fullFilePath, excludeDirName, desFileName, handler);
			}

    } else {
      if (desFileName == fileName) {
				handler(fullFilePath);
      }
    }
	}
}

/**
 * 格式化输出重要日志
 *
 * @param  {string} message 日志信息
 */
function logImportantMsg(message) {
	console.log(`\n >>>>>>>>>>>>>>> ${message} <<<<<<<<<<<<<<<`);
}

/**
 * 格式化输出一般日志
 *
 * @param  {string} message 日志信息
 */
function logNormalMsg(message) {
	console.log(`\n -> ${message}`);
}

/**
 * 格式化输出错误日志
 *
 * @param  {string} message 日志信息
 */
function logErrorMsg(message) {
	console.log(`\n ！！！！！！！！！！ ${message} ！！！！！！！！！！`);
}


//-------------------  Android配置  -------------------//
//配置AndroidManifest.xml中的appkey和scheme
function configAndroidManifest(path){
	var err = false;
	var rf = fs.readFileSync(path, "utf-8");
	var alreadyConfigAppkey = rf.match(/com.xinstall.APP_KEY/);
	if(alreadyConfigAppkey == null){
		var matchApplication = rf.match(/\n.*<\/application>/);
		if(matchApplication != null){
			rf = rf.replace(matchApplication[0], "\n\t\t<meta-data "
			+ "\n\t\t\tandroid:name=\"com.xinstall.APP_KEY\""
			+ "\n\t\t\tandroid:value=\"" + appKey + "\"/>"
			+ matchApplication[0]);
		}else{
			logNormalMsg("没有匹配到 </application>");
			err = true;
		}
	}else{
		logNormalMsg("Android平台appkey已配置，如需修改请手动配置");
	}
	var alreadyConfigScheme = rf.match(/<!-- xinstall scheme -->/);
	if(alreadyConfigScheme == null){
		var matchLauncherIntent = rf.match(/\n.*<intent-filter>\n.*android.intent.action.MAIN.*\n.*android.intent.category.LAUNCHER.*\n.*<\/intent-filter>/);
		if(matchLauncherIntent != null){
			rf = rf.replace(matchLauncherIntent[0], matchLauncherIntent[0]
			+ "\n\t\t\t<!-- xinstall scheme -->"
			+ "\n\t\t\t<intent-filter>"
			+ "\n\t\t\t\t<action android:name=\"android.intent.action.VIEW\"/>"
			+ "\n\t\t\t\t<category android:name=\"android.intent.category.DEFAULT\"/>"
			+ "\n\t\t\t\t<category android:name=\"android.intent.category.BROWSABLE\"/>"
			+ "\n\t\t\t\t<data android:scheme=\"" + scheme + "\"/>"
			+ "\n\t\t\t</intent-filter>");
		}else{
			logNormalMsg("没有匹配到 LAUNCHER intent-filter");
			err =true;
		}
	}else{
		logNormalMsg("Android平台scheme已配置，如需修改请手动配置");
	}
	fs.writeFileSync(path, rf, "utf-8");

	if(err){
		logNormalMsg(path + " 配置有问题，请参考文档手动修改\n");
	}
}




//-------------------  Android配置  -------------------//

function findAndroidManifest(file, deep, handler){
	var stats = fs.statSync(file);
	handler(file, stats);

	// 遍历子目录
	if (deep > 0 &&　stats.isDirectory()) {
		var files = fullPath(file, fs.readdirSync(file));
		deep = deep-1;
		files.forEach(function (f) {
			findAndroidManifest(f, deep, handler);
		});
	}
}

function fullPath(dir, files) {
  return files.map(function (f) {
    return path.join(dir, f);
  });
}
