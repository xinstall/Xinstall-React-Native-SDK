# Android 手动集成文档

### React Native 0.60 开始，会自动 link 原生模块 ，所以只需要 配置 Xinstall 即可
在 react-native link 之后，打开 android 工程。

## 配置项目
检查 android 项目下的 settings.gradle 配置有没有包含以下内容：
project/android/settings.gradle

```
project(':xinstall-react-native').projectDir = new File(rootProject.projectDir, '../node_modules/xinstall-react-native/android')
include ':app', ':xinstall-react-native'
```
## 导入项目
检查一下 dependencies 中有没有添加 xinstall-react-native 依赖。
project/android/app/build.gradle
```
dependencies {
    implementation fileTree(dir: "libs", include: ["*.jar"])
    implementation project(':xinstall-react-native')  // 添加 xinstall 依赖
    implementation "com.facebook.react:react-native:+" 
}
```

在``package.json`` 文件中的dependencs中添加依赖，具体如下
```json
"dependencies": {
    ......// others
    "xinstall-react-native": "1.5.4"
  },
  
```

然后执行 
```shell
npx react-native link xinstall-react-native
```

## Xinstall配置

### 设置AppKey 
project/android/app/AndroidManifest.xml  
在AndroidManifest.xml的application标签内设置AppKey
```
<meta-data android:name="com.xinstall.APP_KEY"
            android:value="XINSTALL_APPKEY"/>
```

### 设置scheme 
在AndroidManifest.xml的拉起页面activity标签中添加intent-filter（一般为MainActivity），配置scheme，用于浏览器中拉起
(scheme详细获取位置：xinstall应用控制台 -> Android集成 -> Android应用配置)
```
<activity
    android:name=".MainActivity"
    android:launchMode="singleTask">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
    <intent-filter>
        <action android:name="android.intent.action.VIEW"/>
        <category android:name="android.intent.category.DEFAULT"/>
        <category android:name="android.intent.category.BROWSABLE"/>
        <data android:scheme="XINSTALL_SCHEME"/>
    </intent-filter>
</activity>
```