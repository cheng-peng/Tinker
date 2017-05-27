package com.cxp.tinker;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tencent.tinker.lib.tinker.TinkerInstaller;

public class MainActivity extends AppCompatActivity {

    private TextView tv;
    private Button bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.main_tv);
        bt= (Button) findViewById(R.id.main_bt);
        tv.setText("成功！~");
        tv.setTextColor(Color.GREEN);
        bt.setVisibility(View.GONE);

    }

    public void  clickLis(View view){
        //进行补丁的操作，暂时用本地代替
        TinkerInstaller.onReceiveUpgradePatch(this,
                Environment.getExternalStorageDirectory().getAbsolutePath()+"/tinker/patch_signed_7zip.apk");
    }
}
