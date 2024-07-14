package cn.touchair.audiobox;

import android.app.Application;
import android.content.Context;

import java.lang.ref.WeakReference;

import cn.touchair.audiobox.common.Settings;

public class App extends Application {

    private static WeakReference<Context> _applicationContextRef;

    @Override
    public void onCreate() {
        super.onCreate();
        _applicationContextRef = new WeakReference<>(getApplicationContext());
        Settings.initialize(getApplicationContext());
    }

    public static Context requireApplicationContext() {
        Context applicationContext = _applicationContextRef.get();
        if (applicationContext == null) {
            throw new RuntimeException("Application has been released!");
        }
        return applicationContext;
    }
}
