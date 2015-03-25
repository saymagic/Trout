package cn.saymagic.digitcsu.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.andexert.library.RippleView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Callable;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.saymagic.digitcsu.Constant;
import cn.saymagic.digitcsu.MainApplication;
import cn.saymagic.digitcsu.R;
import cn.saymagic.digitcsu.ar.AsyncTaskWarpper;
import cn.saymagic.digitcsu.ar.AsyncWorkResult;
import cn.saymagic.digitcsu.listener.NotifyListener;
import cn.saymagic.digitcsu.net.DigitConnectionFactory;
import cn.saymagic.digitcsu.ui.view.DeletableEditText;
import cn.saymagic.digitcsu.util.NetWorkUtil;
import cn.saymagic.digitcsu.util.T;

public class LoginActivity extends BaseActivity {

    @InjectView(R.id.rv_input_id)
    RippleView mRvInputId;
    @InjectView(R.id.input_id)
    DeletableEditText mInputId;
    @InjectView(R.id.rv_input_password)
    RippleView mRvInputPassword;
    @InjectView(R.id.input_password)
    DeletableEditText mInputPassword;
    @InjectView(R.id.btn_login)
    Button mBtnLogin;
    @InjectView(R.id.tv_try_to_loginout)
    TextView mTvTryToLoginout;
    @InjectView(R.id.pb_login_waiting)
    ProgressBar mPbLoginWaiting;

    private String accountId;
    private String accountPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);
        restoreAccountInfo();
    }

    @OnClick(R.id.btn_login)
    public void onLoginBtnClicked() {
        accountId = mInputId.getText().toString().trim();
        accountPassword = mInputPassword.getText().toString().trim();
        if (TextUtils.isEmpty(accountId)) {
            doTextViewEmptyTip(mInputId);
        } else if (TextUtils.isEmpty(accountPassword)) {
            doTextViewEmptyTip(mInputPassword);
        } else {
            gotoLoginPerform();
        }
    }

    @OnClick(R.id.tv_try_to_loginout)
    public void onLoginoutTextViewClicked() {
        showProgressbar();
        AsyncTaskWarpper.getInstance().doAsyncWork(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return DigitConnectionFactory.getInstance(LoginActivity.this).doLogout();
            }
        }, new NotifyListener() {
            @Override
            public void onNotify(Object result) {
            }
        }, new NotifyListener() {
            @Override
            public void onNotify(Object result) {
                dismissProgressbar();
                AsyncWorkResult ar = (AsyncWorkResult) result;
                String json = (String) ar.getArgs()[0];
                if (!TextUtils.isEmpty(ar.getArgs()[0].toString())) {
                    try {
                        JSONObject jsonObject = new JSONObject((String) ar.getArgs()[0]);
                        String[] arr = getResources().getStringArray(R.array.logout_tip);
                        int resultCode = -1;
                        if (jsonObject.has("resultCode") && (resultCode = jsonObject.getInt("resultCode")) < arr.length) {
                            switch (resultCode) {
                                case 0:
                                    T.showLong(LoginActivity.this, R.string.logout_success);
                                    break;
                                default:
                                    showError(arr[resultCode]);
                            }
                        } else
                            showLoginUnknowError();
                    } catch (JSONException e) {
                        showLoginUnknowError();
                    }
                } else {
                    showLogoutUnknowError();
                }
            }
        });
    }

    /**
     * 跳转到LoginActivity之前做的准备工作,会对当前的网络情况做一个判断，然后确定是否跳转
     */
    private void gotoLoginPerform() {
        if (!NetWorkUtil.checkWifiConnection(this)) {
            //Wifi未连接，弹出提示连接框
            new MaterialDialog.Builder(this)
                    .title(R.string.tip)
                    .content(R.string.not_connected_wifi)
                    .positiveText(R.string.goto_connect)
                    .negativeText(R.string.exit)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            //打开Wifi设置页面
                            startActivityForResult(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS), 0);
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            LoginActivity.this.finish();
                        }
                    })
                    .show();
        } else {
            /**
             *
             * 登陆地流程分为两步，第一步去尝试去加载一个网址来连接数字中南登陆页面，获取页面中brasAddress和userIntranetAddress两个值。
             * 第两步，将加密的密码和账号连同第一步获取的两个值一起post给数字中南登陆页，实现登陆。
             */
            showProgressbar();
            AsyncTaskWarpper.getInstance().doAsyncWork(new Callable<Object>() {
                @Override
                public Object call() throws Exception {

                    saveAccountInfo();
                    return DigitConnectionFactory.getInstance(LoginActivity.this).doLogin(accountId, accountPassword);
                }
            }, new NotifyListener() {
                @Override
                public void onNotify(Object result) {
                    T.showLong(LoginActivity.this, "取消");
                }
            }, new NotifyListener() {
                //登陆后的回调
                @Override
                public void onNotify(Object result) {
                    dismissProgressbar();
                    AsyncWorkResult ar = (AsyncWorkResult) result;
                    //有返回结果的情况
                    if (!TextUtils.isEmpty((String) ar.getArgs()[0])) {
                        try {
                            JSONObject jsonObject = new JSONObject((String) ar.getArgs()[0]);
                            String[] arr = getResources().getStringArray(R.array.login_tip);
                            int resultCode = -1;
                            if (jsonObject.has("resultCode") && (resultCode = jsonObject.getInt("resultCode")) < arr.length) {
                                switch (resultCode) {
                                    //0代表登陆成功，10代表密码简单，这两种情况都是登陆成功地提示。
                                    case 0:
                                    case 10:
                                        T.showLong(LoginActivity.this, R.string.login_success);
                                        gotoShowResultActivity(jsonObject.toString());
                                        break;
                                    case 1:
                                        if (jsonObject.has("resultDescribe"))
                                            showError(jsonObject.getString("resultDescribe"));
                                        break;
                                    default:
                                        showError(arr[resultCode]);
                                }
                            } else
                                showLoginUnknowError();
                        } catch (JSONException e) {
                            showLoginUnknowError();
                        }
                    } else {
                        showLoginUnknowError();
                    }
                }
            });
        } /**else {
         new MaterialDialog.Builder(this)
         .title(R.string.tip)
         .content(R.string.online_content_tip)
         .positiveText(R.string.compulsion_login)
         .negativeText(R.string.exit)
         .callback(new MaterialDialog.ButtonCallback() {
        @Override public void onPositive(MaterialDialog dialog) {
        gotoLoginActivity();
        }

        @Override public void onNegative(MaterialDialog dialog) {
        SplashActivity.this.finish();
        }
        })
         .show();
         }**/
    }

    private void restoreAccountInfo() {
        accountId = MainApplication.getSpUtil().getValue(Constant.SP_ACCOUNT_ID, "");
        accountPassword = MainApplication.getSpUtil().getValue(Constant.SP_ACCOUNT_PWD, "");
        mInputId.setText(accountId);
        mInputPassword.setText(accountPassword);
    }

    private void saveAccountInfo() {
        MainApplication.getSpUtil().setValue(Constant.SP_ACCOUNT_ID, accountId);
        MainApplication.getSpUtil().setValue(Constant.SP_ACCOUNT_PWD, accountPassword);
    }

    private void doTextViewEmptyTip(DeletableEditText tv) {
        tv.setShakeAnimation();
        tv.requestFocus();
        T.showLong(this, getResources().getString(R.string.textview_empty));
    }

    private void gotoShowResultActivity(String jsonResult) {
        Intent intent = new Intent();
        intent.setClass(this, ShowResultActivity.class);
        intent.putExtra(ShowResultActivity.INTRNT_EXTRA_NAME, jsonResult);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_up_in,
                R.anim.slide_down_out);
        this.finish();
    }

    private void dismissProgressbar() {
        if (mPbLoginWaiting != null && mPbLoginWaiting.isShown()) {
            mPbLoginWaiting.setVisibility(View.GONE);
        }
    }

    private void showProgressbar() {
        mPbLoginWaiting.setVisibility(View.VISIBLE);
    }
}
