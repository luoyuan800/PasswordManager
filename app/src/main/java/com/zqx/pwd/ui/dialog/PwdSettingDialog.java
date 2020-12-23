package com.zqx.pwd.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.zqx.pwd.R;
import com.zqx.pwd.dao.AccountDao;
import com.zqx.pwd.event.PwdChangedEvent;
import com.zqx.pwd.model.bean.AccountBean;
import com.zqx.pwd.model.manager.EncryptManager;
import com.zqx.pwd.presenter.AccountsPresenter;
import com.zqx.pwd.util.SharedPreferencesUtil;
import com.zqx.pwd.global.Spkey;
import com.zqx.pwd.util.StringUtil;
import com.zqx.pwd.util.ToastUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ZhangQixiang on 2017/2/18.
 */
public class PwdSettingDialog extends Dialog implements TextView.OnEditorActionListener {

    @BindView(R.id.et_pwd1)
    EditText mEtPwd1;
    @BindView(R.id.et_pwd2)
    EditText mEtPwd2;
    @BindView(R.id.et_pwd0)
    EditText mEtPwd0;
    private String mPwd0; //CheckInActivity传进来的原始密码
    private Context context;

    public PwdSettingDialog(Context context, String pwd) {
        super(context);
        mPwd0 = pwd;
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_pwd_setting);
        ButterKnife.bind(this);
        initWindowParams();
        initView();
        this.context = context;
    }

    private void initView() {
        if (TextUtils.isEmpty(mPwd0)) {
            mEtPwd0.setVisibility(View.GONE);
        }else{
            mEtPwd0.setText(mPwd0);
        }

        mEtPwd1.setOnEditorActionListener(this);
    }

    private void initWindowParams() {
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
    }

    @OnClick({R.id.btn_cancel, R.id.btn_confirm})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel:
                dismiss();
                break;
            case R.id.btn_confirm:
                doConfirm();
                break;
        }
    }

    private void doConfirm() {
        String pwd0 = mEtPwd0.getText().toString().trim();
        final String pwd1 = mEtPwd1.getText().toString().trim();
        String pwd2 = mEtPwd2.getText().toString().trim();
        if (!TextUtils.equals(mPwd0, pwd0)) {
            ToastUtil.show("原密码错误");
            return;
        }
        if (StringUtil.hasEmpty(pwd1, pwd2)) {
            ToastUtil.show("不能有空");
            return;
        }
        if (!TextUtils.equals(pwd1, pwd2)) {
            ToastUtil.show("两次密码输入不一致");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<AccountBean> beans = AccountDao.getAllAccounts(context);
                if(beans.size() > 0){
                    EncryptManager.decryptAccountSet(beans);
                }
                SharedPreferencesUtil.saveString(Spkey.PWD, pwd1);
                EncryptManager.init(pwd1);
                for(AccountBean bean : beans) {
                    EncryptManager.encryptAccount(bean);
                }
                AccountDao.saveAccounts(context, beans);
            }
        }).start();
        EventBus.getDefault().post(new PwdChangedEvent(pwd1));
        dismiss();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE && v.getId() == R.id.et_pwd2) {
            doConfirm();
        }
        return true;
    }
}


