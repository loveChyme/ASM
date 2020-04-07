package com.plugin.test

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.second_act.*
/**
 * <p />Author: chenming
 * <p />E-mail: cm1@erongdu.com
 * <p />Date: 2020-04-02 15:05
 * <p />Description:
 */
class SecondActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.second_act)
        tv_tips.setOnClickListener {
            Toast.makeText(this, "onclick", Toast.LENGTH_LONG).show()
        }
    }

}