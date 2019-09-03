# Build Setup for Android

Copy lines to `android/settings.gradle`:

```gradle
include ':RCTWeChat'
project(':RCTWeChat').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-wechat/android')
```

Copy lines to `android/app/build.gradle`

```gradle
dependencies {
  implementation project(':RCTWeChat') // Add this line
}
```

Copy lines to `proguard-rules.pro`:

```pro
-keep class com.tencent.mm.sdk.** {
  *;
}
```

Then update `MainActivity.java` or `MainApplication.java`:

```java
import com.theweflex.react.WeChatPackage; // Add this line

  @Override
  protected List<ReactPackage> getPackages() {
    @SuppressWarnings("UnnecessaryLocalVariable")
    List<ReactPackage> packages = new PackageList(this).getPackages();
    // Packages that cannot be autolinked yet can be added manually here, for example:
    // packages.add(new MyReactNativePackage());
    packages.add(new WeChatPackage()); // Add this line
    return packages;
  }
```

**Integrating with login and share**

If you are going to integrate login or share functions, you need to 
create a package named 'wxapi' in your application package and a class 
named `WXEntryActivity` in it.

```java
package your.package.wxapi;

import android.app.Activity;
import android.os.Bundle;
import com.theweflex.react.WeChatModule;

public class WXEntryActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    WeChatModule.handleIntent(getIntent());
    finish();
  }
}
```

Then add the following node to `AndroidManifest.xml`:

```xml
<manifest>
  <application>
    <activity
      android:name=".wxapi.WXEntryActivity"
      android:label="@string/app_name"
      android:exported="true"
    />
  </application>
</manifest>
```

**Integrating the WeChat Payment**

If you are going to integrate payment functionality by using this library, then
create a package named also `wxapi` in your application package and a class named
`WXPayEntryActivity`, this is used to bypass the response to JS level:

```java
package your.package.wxapi;

import android.app.Activity;
import android.os.Bundle;
import com.theweflex.react.WeChatModule;

public class WXPayEntryActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    WeChatModule.handleIntent(getIntent());
    finish();
  }
}
```

Then add the following node to `AndroidManifest.xml`:

```xml
<manifest>
  <application>
    <activity
      android:name=".wxapi.WXPayEntryActivity"
      android:label="@string/app_name"
      android:exported="true"
    />
  </application>
</manifest>
```
