# Tinker

# Tinker 热修复的基本使用

# 基本使用

<img src='https://github.com/cheng-peng/Tinker/blob/master/img/1.png'/>

~~~
//thinker
classpath ('com.tencent.tinker:tinker-patch-gradle-plugin:1.7.10')
~~~

<img src='https://github.com/cheng-peng/Tinker/blob/master/img/2.png'/>

~~~
compile 'com.android.support:multidex:1.0.1'

//可选，用于生成application类
provided('com.tencent.tinker:tinker-android-anno:1.7.10')
//tinker的核心库
compile('com.tencent.tinker:tinker-android-lib:1.7.10')
~~~

<img src='https://github.com/cheng-peng/Tinker/blob/master/img/3.png'/>

~~~
//apply tinker插件
apply plugin: 'com.tencent.tinker.patch'
~~~

<img src='https://github.com/cheng-peng/Tinker/blob/master/img/4.png'/>

~~~
def bakPath = file("${buildDir}/bakApk/")

ext {
    tinkerEnabled = true
    tinkerOldApkPath = "${bakPath}/app-debug-0523-18-23-22.apk"
    //proguard mapping file to build patch apk
    tinkerApplyMappingPath = "${bakPath}/app-debug-0523-18-23-22-mapping.txt"
    //resource R.txt to build patch apk, must input if there is resource changed
    tinkerApplyResourcePath = "${bakPath}/app-debug-0523-18-23-22-R.txt"
}

def getOldApkPath() {
    return ext.tinkerOldApkPath
}
def getApplyMappingPath() {
    return ext.tinkerApplyMappingPath
}
def getApplyResourceMappingPath() {
    return  ext.tinkerApplyResourcePath
}

if (ext.tinkerEnabled) {
    tinkerPatch {
        oldApk = getOldApkPath()
        ignoreWarning = false
        useSign = true

//        packageConfig {
//
//            configField("TINKER_ID", "2.0")
//        }
        buildConfig{
            tinkerId = "1.0"
            applyMapping = getApplyMappingPath()
            applyResourceMapping = getApplyResourceMappingPath()
        }

        lib {

            pattern = ["lib/armeabi/*.so"]
        }

        res {

            pattern = ["res/*", "assets/*", "resources.arsc", "AndroidManifest.xml"]

            ignoreChange = ["assetsmple_meta.txt"]

            largeModSize = 100
        }

        sevenZip {

            zipArtifact = "com.tencent.mm:SevenZip:1.1.10"
        }

        dex {

            dexMode = "jar"

            pattern = ["classes*.dex",
                       "assetscondary-dex-?.jar"]

            loader = ["com.tencent.tinker.loader.*",
                      "com.tencent.tinker.*",
                      "com.cxp.tinker.MyTinkerApplication"
            ]
        }


    }
}

android.applicationVariants.all { variant ->
    /**
     * task type, you want to bak
     */
    def taskName = variant.name

    tasks.all {
        if ("assemble${taskName.capitalize()}".equalsIgnoreCase(it.name)) {
            it.doLast {
                copy {
                    def date = new Date().format("MMdd-HH-mm-ss")
                    from "${buildDir}/outputs/apk/${project.getName()}-${taskName}.apk"
                    into bakPath
                    rename { String fileName ->
                        fileName.replace("${project.getName()}-${taskName}.apk", "${project.getName()}-${taskName}-${date}.apk")
                    }

                    from "${buildDir}/outputs/mapping/${taskName}/mapping.txt"
                    into bakPath
                    rename { String fileName ->
                        fileName.replace("mapping.txt", "${project.getName()}-${taskName}-${date}-mapping.txt")
                    }

                    from "${buildDir}/intermediates/symbols/${taskName}/R.txt"
                    into bakPath
                    rename { String fileName ->
                        fileName.replace("R.txt", "${project.getName()}-${taskName}-${date}-R.txt")
                    }
                }
            }
        }
    }
}
~~~

<img src='https://github.com/cheng-peng/Tinker/blob/master/img/5.png'/>

~~~
@SuppressWarnings("unused")
//这里的application是manifest里面的 不需要实际写出类
@DefaultLifeCycle(application = "com.cxp.tinker.MyTinkerApplication",
        flags = ShareConstants.TINKER_ENABLE_ALL,
        loadVerifyFlag = false)
//更改为继承DefaultApplicationLike
public class APP extends DefaultApplicationLike {

    public APP(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent);
    }

    //以前application写在oncreate的东西搬到这里来初始化
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);
        //you must install multiDex whatever tinker is installed!
        MultiDex.install(base);

        //installTinker after load multiDex
        //or you can put com.tencent.tinker.** to main dex
        TinkerInstaller.install(this);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callback) {
        getApplication().registerActivityLifecycleCallbacks(callback);
    }
}
~~~

<img src='https://github.com/cheng-peng/Tinker/blob/master/img/6.png'/>

<img src='https://github.com/cheng-peng/Tinker/blob/master/img/7.png'/>

~~~
private TextView tv;
private Button bt;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    tv = (TextView) findViewById(R.id.main_tv);
    bt= (Button) findViewById(R.id.main_bt);
    tv.setText("错误！~");
    tv.setTextColor(Color.RED);


}

public void  clickLis(View view){
    //进行补丁的操作，暂时用本地代替
    TinkerInstaller.onReceiveUpgradePatch(this,
            Environment.getExternalStorageDirectory().getAbsolutePath()+"/tinker/patch_signed_7zip.apk");
}
~~~

<img src='https://github.com/cheng-peng/Tinker/blob/master/img/8.png'/>

<img src='https://github.com/cheng-peng/Tinker/blob/master/img/9.png'/>

<img src='https://github.com/cheng-peng/Tinker/blob/master/img/10.png'/>

<img src='https://github.com/cheng-peng/Tinker/blob/master/img/11.png'/>

### 这个是修改后的内容，然后执行以下步骤：

<img src='https://github.com/cheng-peng/Tinker/blob/master/img/12.png'/>

<img src='https://github.com/cheng-peng/Tinker/blob/master/img/13.png'/>

### 然后就点修复，并重启，然后就OK了！

<img src='https://github.com/cheng-peng/Tinker/blob/master/img/14.png'/>

