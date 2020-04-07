package com.plugin.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.plugin.logsdk.LogHelper;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = findViewById(R.id.tv_click);
        LogHelper.getInstance();
        init(textView);
        TextView textView1 = findViewById(R.id.tv_click2);
        textView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "点击按钮2", Toast.LENGTH_LONG).show();
            }
        });
    }

    @SuppressWarnings("Convert2Lambda")
    private void init(TextView textView) {
        textView.setOnClickListener((view) -> {
            Toast.makeText(view.getContext(), "点击按钮1", Toast.LENGTH_LONG).show();
            startActivity(new Intent(view.getContext(), SecondActivity.class));
        });
    }
}
