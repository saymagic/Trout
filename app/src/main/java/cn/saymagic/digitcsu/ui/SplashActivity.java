package cn.saymagic.digitcsu.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.saymagic.digitcsu.Constant;
import cn.saymagic.digitcsu.MainApplication;
import cn.saymagic.digitcsu.R;
import cn.saymagic.digitcsu.ui.view.RoundedImageView;

public class SplashActivity extends BaseActivity {

    @InjectView(R.id.splash_id)
    RelativeLayout mSplashId;
    @InjectView(R.id.iv_main_left_head)
    RoundedImageView mIvMainLeftHead;
    @InjectView(R.id.logo_name)
    TextView mLogoName;
    @InjectView(R.id.logo_tip)
    TextView mLogoTip;

    private static boolean mIsFirstCreate = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.inject(this);
        if(!TextUtils.isEmpty(MainApplication.getSpUtil().getValue(Constant.SP_ACCOUNT_ID,""))){
            mLogoName.setText(MainApplication.getSpUtil().getValue(Constant.SP_ACCOUNT_ID,""));
            mLogoTip.setText(R.string.welcome_you);
        }
        //应用第一次启动，做一些提示性的工作
        if (MainApplication.getSpUtil().isFirstStart()) {
            new MaterialDialog.Builder(this)
                    .title(R.string.tip)
                    .content(R.string.first_start_tip_content)
                    .positiveText(R.string.goto_login)
                    .negativeText(R.string.exit)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            gotoLoginActivity();
                        }
                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            SplashActivity.this.finish();
                        }
                    })
                    .show();
        } else {
            //渐变展示启动屏
            AlphaAnimation alphaAnimation = new AlphaAnimation(0.3f,1.0f);
            alphaAnimation.setDuration(1000);
            mSplashId.startAnimation(alphaAnimation);
            alphaAnimation.setAnimationListener(new Animation.AnimationListener()
            {
                @Override
                public void onAnimationEnd(Animation arg0) {
                    gotoLoginActivity();
                }
                @Override
                public void onAnimationRepeat(Animation animation) {}
                @Override
                public void onAnimationStart(Animation animation) {}

            });
        }
    }

    private void gotoLoginActivity() {
        Intent i = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_up_in,
                R.anim.slide_down_out);
        this.finish();
    }
}
