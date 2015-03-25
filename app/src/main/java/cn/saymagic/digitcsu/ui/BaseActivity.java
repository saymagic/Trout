package cn.saymagic.digitcsu.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;

import com.afollestad.materialdialogs.MaterialDialog;
import com.umeng.analytics.MobclickAgent;

import cn.saymagic.digitcsu.R;

/**
 * Created by saymagic on 15/3/21.
 */
public class BaseActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    protected void showLoginUnknowError() {
        showError(R.string.unknow_result_login_failed);
    }

    protected void showLogoutUnknowError() {
        showError(R.string.unknow_result_logout_failed);
    }

    protected void showError(String tip) {
        new MaterialDialog.Builder(this)
                .title(R.string.tip)
                .content(tip)
                .positiveText(R.string.cancel)
                .negativeText(R.string.exit)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {

                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        finish();
                    }
                })
                .show();
    }

    protected void showError(int tip) {
        new MaterialDialog.Builder(this)
                .title(R.string.tip)
                .content(tip)
                .positiveText(R.string.cancel)
                .negativeText(R.string.exit)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {

                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        finish();
                    }
                })
                .show();
    }

}
