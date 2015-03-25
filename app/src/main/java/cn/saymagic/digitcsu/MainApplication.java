package cn.saymagic.digitcsu;

import android.app.Application;

import cn.saymagic.digitcsu.util.SPUtil;


/**
 * Created by saymagic on 15/3/18.
 */
public class MainApplication extends Application {
    private static MainApplication instance;
    private static SPUtil spUtil;

    public MainApplication() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        spUtil = new SPUtil(this);
    }

    public synchronized static MainApplication getInstance() {
        if (null == instance) {
            instance = new MainApplication();
        }
        return instance;
    }

    public static SPUtil getSpUtil(){
        return spUtil;
    }

}
