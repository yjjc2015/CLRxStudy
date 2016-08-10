package com.clrxstudy;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.clrxstudy.databinding.ActivityMainBinding;
import com.jakewharton.rxbinding.view.RxView;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

/**RxJava异常处理(错误处理操作)*/
public class ErrorActivity extends AppCompatActivity {
    private ActivityMainBinding mBinding;
    private int globalCount = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        mBinding.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                done();
//                done1();
//                retry();
//                retry2();
                retryWhen();
            }
        });

        preventThrottle();
    }

    /**
     * 1.
     * 查看onErrorReturn的效果:
     * onError可以拦截异常，然后自定义返回结果
     * 没有onError也不会报错
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
                Toast.makeText(ErrorActivity.this, "I catch this error, and I result the result! without me , it will throw exception", Toast.LENGTH_SHORT).show();
                return null;
            }
        }).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                Toast.makeText(ErrorActivity.this, "" + integer, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 2.研究onErrorResultNext的效果
     * 不能捕获异常，仍要调用onError(没有onError就会报错)
     * */
    public void done1() {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                int i = 6/0;
                subscriber.onNext(i);
            }
        }).onErrorResumeNext(new Func1<Throwable, Observable<? extends Integer>>() {
            @Override
            public Observable<? extends Integer> call(Throwable throwable) {
                Toast.makeText(ErrorActivity.this, "Can i catch the error?   no, i can't!", Toast.LENGTH_SHORT).show();
                return null;
            }
        }).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                Toast.makeText(ErrorActivity.this, "" + integer, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 3.1
     * retry：错误时重试
     * retry(2)表示出错后重新请求2次
     *
     * Retry操作符不会将原始Observable的onError通知传递给观察者，
     * 它会订阅这个Observable，再给它一次机会无错误地完成它的数据序列。
     * Retry总是传递onNext通知给观察者，由于重新订阅，可能会造成数据项重复
     *
     *
     * 出错时执行顺序：请求网络n+1次-->onError
     * */
    public void retry() {
        globalCount = 0;
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                globalCount++;
                Toast.makeText(ErrorActivity.this, "执行第" + globalCount + "次", Toast.LENGTH_SHORT).show();
                int i = 6/0;
                subscriber.onNext(i);
            }
        }).retry(2).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                Toast.makeText(ErrorActivity.this, "执行第" + globalCount + "次" + integer, Toast.LENGTH_SHORT).show();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Toast.makeText(ErrorActivity.this, throwable.getMessage() + "执行第" + globalCount + "次", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 3.2
     * 使用func方法返回值是否为true/false来设置是否继续retry(即retry的次数是变化的)
     * 方法返回值为true则继续retry，否则onError
     */
    public void retry2() {
        globalCount = 0;
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                globalCount++;
                Toast.makeText(ErrorActivity.this, "执行第" + globalCount + "次", Toast.LENGTH_SHORT).show();
                int i = 6/0;
                subscriber.onNext(i);
            }
        }).retry(new Func2<Integer, Throwable, Boolean>() {
            @Override
            public Boolean call(Integer integer, Throwable throwable) {
                return new Random().nextBoolean();
            }
        }).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                Toast.makeText(ErrorActivity.this, "执行第" + globalCount + "次" + integer, Toast.LENGTH_SHORT).show();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Toast.makeText(ErrorActivity.this, throwable.getMessage() + "执行第" + globalCount + "次", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 4.RetryWhen:
     * 使用场景：当一个请求异常时，启动另一个请求，但新的请求需要有自己的订阅
     * old Request --> throw error --> retryWhen --> new Request --> new subscribe --> old completed
     */
    public void retryWhen() {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                int i= 5 / 0;
                subscriber.onNext(i);
            }
        }).retryWhen(new Func1<Observable<? extends Throwable>, Observable<Integer>>() {
            @Override
            public Observable<Integer> call(Observable<? extends Throwable> observable) {
                int i = 5/1;
                Observable<Integer> ob = observable.just(i).doOnNext(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        Toast.makeText(ErrorActivity.this, "inner doOnNext : " + integer, Toast.LENGTH_SHORT).show();
                    }
                });
                ob.subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        Toast.makeText(ErrorActivity.this, "inner subscribe : " + integer, Toast.LENGTH_SHORT).show();
                    }
                });
                return ob;
            }
        }).doOnCompleted(new Action0() {
            @Override
            public void call() {
                Toast.makeText(ErrorActivity.this, "completed", Toast.LENGTH_SHORT).show();
            }
        }).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                Toast.makeText(ErrorActivity.this, "" + integer, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 5.
     * 过滤--->防止抖动--->连续点击按钮等重复操作
     * throttleFirst:假设设置防抖动2秒，从0开始，每次点击增大1：在2秒内连续点击20次，则最终输出1
     * throttleLast:假设设置防抖动2秒，从0开始，每次点击增大1：在2秒内连续点击20次，则最终输出21
     * debounce:假设时间间隔为2秒，从0开始，每次点击增大1：在2秒内连续点击20次停止点击，则最终输出21
     * throttleWithTimeout:等价于debounce
     * */
    public void preventThrottle() {
        //5.1  throttleFirst：得到一个结果后，在一定时间内得到其他结果，直到时间到了，返回第一个结果
//        RxView.clicks(mBinding.btn1).map(new Func1<Void, Integer>() {
//            @Override
//            public Integer call(Void aVoid) {
//                globalCount++;
//                return globalCount;
//            }
//        }).throttleFirst(2000, TimeUnit.MILLISECONDS).subscribe(new Action1<Integer>() {
//            @Override
//            public void call(Integer integer) {
//                Log.i("rx res","rx res : " + integer);
//            }
//        });
        //5.2   throttleLast：得到一个结果后，在一定时间内得到其他结果，直到时间到了，返回最后一个结果
//        RxView.clicks(mBinding.btn1).map(new Func1<Void, Integer>() {
//            @Override
//            public Integer call(Void aVoid) {
//                globalCount++;
//                return globalCount;
//            }
//        }).throttleLast(2000, TimeUnit.MILLISECONDS).subscribe(new Action1<Integer>() {
//            @Override
//            public void call(Integer integer) {
//                Log.i("rx res","rx res : " + integer);
//            }
//        });
        //5.3 debounce:过滤，如果在一定时间内没有别的结果，则将结果提交给订阅者处理，否则忽略该结果
//        RxView.clicks(mBinding.btn1).map(new Func1<Void, Integer>() {
//            @Override
//            public Integer call(Void aVoid) {
//                globalCount++;
//                return globalCount;
//            }
//        }).debounce(2, TimeUnit.SECONDS).subscribe(new Action1<Integer>() {
//            @Override
//            public void call(Integer integer) {
//                Log.i("rx res","rx res : " + integer);
//            }
//        });
        //5.4 throttleWithTimeout:等价于debounce
        RxView.clicks(mBinding.btn1).map(new Func1<Void, Integer>() {
            @Override
            public Integer call(Void aVoid) {
                globalCount++;
                return globalCount;
            }
        }).throttleWithTimeout(3, TimeUnit.SECONDS).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                Log.i("rx res","globalCount =  " + integer);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        globalCount = 0;
    }
}
