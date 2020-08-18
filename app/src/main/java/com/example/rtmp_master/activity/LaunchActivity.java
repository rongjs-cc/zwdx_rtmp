package com.example.rtmp_master.activity;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.example.rtmp_master.config.VideoConfig;
import com.example.rtmp_master.R;

public class LaunchActivity extends BaseActivity implements View.OnClickListener {

    private EditText editName;
    private EditText editPassword;
    private EditText editRoomNum;
    private EditText editPath;
    private Button btnLogin;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_launch;
    }

    @Override
    protected void initView() {
        editName=findViewById(R.id.lunact_edit_name);
        editPassword = (EditText) findViewById(R.id.lunact_edit_password);
        editRoomNum=findViewById(R.id.lunact_edit_roomnum);
        editPath = (EditText) findViewById(R.id.lunact_edit_path);
        btnLogin = (Button) findViewById(R.id.lunact_btn_login);
        btnLogin.setOnClickListener(this);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void doEvent() {
        editPath.setText(VideoConfig.SERVER_URL);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lunact_btn_login:
                method_login();
                break;

        }
    }

    private void method_login() {
//        LoginPresenter loginPresenter = new LoginPresenter(this);
//        String name = editName.getText().toString().trim();
//        String password = editPassword.getText().toString().trim();
//        loginPresenter.login(name,password);
        String path = editPath.getText().toString().trim();
        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra("PATH",path);
        startActivity(intent);
    }

}
