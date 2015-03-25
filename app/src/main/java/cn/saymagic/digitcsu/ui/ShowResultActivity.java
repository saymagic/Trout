package cn.saymagic.digitcsu.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.andexert.library.RippleView;
import com.umeng.fb.FeedbackAgent;
import com.umeng.update.UmengUpdateAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.saymagic.digitcsu.R;
import cn.saymagic.digitcsu.adapter.CommonAdapter;
import cn.saymagic.digitcsu.adapter.ViewHolder;
import cn.saymagic.digitcsu.ar.AsyncTaskWarpper;
import cn.saymagic.digitcsu.ar.AsyncWorkResult;
import cn.saymagic.digitcsu.listener.NotifyListener;
import cn.saymagic.digitcsu.net.DigitConnectionFactory;
import cn.saymagic.digitcsu.util.T;

public class ShowResultActivity extends BaseActivity {

    @InjectView(R.id.lv_result)
    ListView mLvResult;
    @InjectView(R.id.rv_logout)
    RippleView mRvLogout;
    @InjectView(R.id.btn_logout)
    Button mBtnLogout;
    @InjectView(R.id.tv_about)
    TextView mTvAbout;
    @InjectView(R.id.pb_sr_waiting)
    ProgressBar mPbSrWaiting;


    public static final String INTRNT_EXTRA_NAME = "result";
    private JSONObject mJsonObject;
    private List<String> mlistData = new ArrayList<String>();
    private FeedbackAgent agent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_result);
        ButterKnife.inject(this);
        initData();
        initView();
        //检查更新
        UmengUpdateAgent.update(this);
        //实例化反馈接口
        agent = new FeedbackAgent(this);
        agent.sync();
        agent.setWelcomeInfo(getString(R.string.feedback_tip));
    }

    private void initData() {
        try {
            mJsonObject = new JSONObject(getIntent().getStringExtra(INTRNT_EXTRA_NAME));
            mlistData.add("账户总流量: " + mJsonObject.getInt("totalflow") + "MB");
            mlistData.add("已用流量: " + mJsonObject.getInt("usedflow") + "MB");
            mlistData.add("剩余流量: " + mJsonObject.getInt("surplusflow") + "MB");
            mlistData.add("剩余金额: " + mJsonObject.getDouble("surplusmoney") + "元");
            mlistData.add("当前IP地址: " + mJsonObject.getString("userIntranetAddress"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        mLvResult.setAdapter(new CommonAdapter<String>(getApplicationContext(),
                mlistData, R.layout.item_text) {
            @Override
            public void convert(ViewHolder viewHolder, String item) {
                viewHolder.setText(R.id.list_item_text, item);
            }
        });
    }

    @OnClick(R.id.btn_logout)
    /**
     * 这个函数和LoginActivity的下线函数有了重复代码，下期重构掉或者统一下线入口。
     */
    public void onLogoutBtnClicked() {
        showProgressbar();
        AsyncTaskWarpper.getInstance().doAsyncWork(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return DigitConnectionFactory.getInstance(ShowResultActivity.this).doLogout();
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
                                    T.showLong(ShowResultActivity.this, R.string.logout_success);
                                    gotoLoginActivity();
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

    @OnClick(R.id.tv_about)
    public void onAboutClicked() {
        new MaterialDialog.Builder(this)
                .items(R.array.about_array_tip)
                .positiveText(R.string.be_sure)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        switch (which) {
                            case 0:
                                openBrowser("http://blog.saymagic.cn");
                                break;
                            case 1:
                                openBrowser("https://github.com/saymagic/digitcsu");
                                break;
                            case 2:
                                agent.startFeedbackActivity();
                                break;
                            default:
                                break;

                        }
                    }
                })
                .show();
    }

    private void dismissProgressbar() {
        if (mPbSrWaiting != null && mPbSrWaiting.isShown()) {
            mPbSrWaiting.setVisibility(View.GONE);
        }
    }

    private void showProgressbar() {
        mPbSrWaiting.setVisibility(View.VISIBLE);
    }

    private void gotoLoginActivity() {
        Intent i = new Intent(ShowResultActivity.this, LoginActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_up_in,
                R.anim.slide_down_out);
        this.finish();
    }

    private void openBrowser(String url) {
        Uri uri = Uri.parse(url);
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }
}
