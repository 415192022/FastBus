# FastBus
基于RxJava的编译时注解事件框架

## 安装指南
![New Version](https://jitpack.io/v/415192022/FastBus.svg)
### Step 1
在工程Project的根目录的 build.gradle 中添加 jitpack 的 maven 仓库
```
allprojects {
    repositories {
    	...
	    maven { url 'https://jitpack.io' }
    }
}
```
### Step 2
在app build.gradle 添加如下依赖，并在文件中添加kotlin apt 
```
apply plugin: 'kotlin-kapt'
```
```
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    ......
    // FastBus 依赖
    implementation 'com.github.415192022:FastBus:v0.0.1'
    annotationProcessor 'com.github.415192022.FastBus:processor:v0.0.1'
    kapt 'com.github.415192022.FastBus:processor:v0.0.1'
}
```
### Step 3
在使用到 FastBus 提供的注解的 module 中的 build.gradle 添加如下依赖

#### Java module
```
apply plugin: 'com.android.library'

android {
    ......
    defaultConfig {
        ......
        /**
         * Java 的 Module 使用该方式进行编译时注解扫描, 不兼容 Kotlin 文件
         */
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [moduleName: project.getName(), libName: "app"]
            }
        }
    }
}
```
## 基础功能
### 一) 初始化
FastBus 的初始化操作通过 **FastBus.init()** 方法执行, 推荐在 BaseApplication 的 onCreate 中进行
```
public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 在 BaseApplication 中调用 init 方法执行初始化操作
        FastBus.init(AndroidSchedulers.mainThread(), "app")
    }
}
```
### 二) 注册 在页面生命周期内 一般为onCreate
```
private FastBusBinder mFastBusBinder = null;

mFastBusBinder = FastBus.bind(this);
```
### 三） 解绑 一般为页面销毁  如果不解绑可能回导致内存泄漏  一般为onDestroy
```
FastBus.unBind(mFastBusBinder);
```
### 四） 发送事件 
```
FastBus.post("自定义的EventKey", object);
```
### 五） 接收事件 
```
@Receive("自定义的EventKey")
    public void onLoginSuccess(Object object) {
    }
```