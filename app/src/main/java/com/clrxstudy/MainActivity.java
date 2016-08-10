package com.clrxstudy;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.clrxstudy.databinding.ActivityMainBinding;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding mBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBinding.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                done();
//                done1();
            }
        });
    }

    /**
     * 查看onErrorReturn的效果:
     * onError可以拦截异常，然后自定义返回结果
     * */
    private void done() {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                int i = 6/0;
                subscriber.onNext(i);
            }
        }).onErrorReturn(new Func1<Throwable, Integer>() {
            @Override
            public Integer call(Throwable throwable) {
                Toast.makeText(MainActivity.this, "I catch this error, and I result the result! without me , it will throw exception", Toast.LENGTH_SHORT).show();
                return null;
            }
        }).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                Toast.makeText(MainActivity.this, "" + integer, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void done1() {

    }
}
