#RxRetainFragment [![Apache License](https://img.shields.io/badge/license-Apache%20v2-blue.svg)](https://github.com/rovkinmax/RxRetainFragment/blob/master/LICENSE)

install from `jcenter()`
``` gradle
compile 'com.github.rovkinmax:rxretain:1.1.1'
```

##### Binding with compose() operator

``` Java
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        yourAwesomeObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(RetainFactory.bindToRetain(getFragmentManager())) //binding here
                .subscribe(new EmptySubscriber<Object>() {
                    @Override
                    public void onStart() {
                        showProgress();
                    }

                    @Override
                    public void onNext(Object o) {
                        updateUIWithData();
                    }
                });
    }
```

##### Simple example for orientation changes
``` Java
public class MainActivity extends Activity {
    private RetainWrapper<SomeType> rotateExample;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rotateExample = RetainFactory.create(getFragmentManager(), yourAwesomeObservable, new EmptySubscriber<SomeType>() {
            @Override
            public void onStart() {
                //show load progress.
                //It will be called after rotation
            }

            @Override
            public void onNext(SomeType result) {
                //do something
            }
        });
        
        rotateExample.subscibe()//call to start if not started
    }
}
```

##### More then one instance 
``` Java
public class MainActivity extends Activity {
    private RetainWrapper<SomeType> rotateExample;
    private RetainWrapper<SomeType2> rotateExample2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rotateExample = RetainFactory.create(getFragmentManager(), yourAwesomeObservable, new EmptySubscriber<SomeType>() {
            @Override
            public void onStart() {
                //show load progress.
                //It will be called after rotation
            }

            @Override
            public void onNext(SomeType result) {
                //do something
            }
        },"MY_FIRST_TAG");
        
        rotateExample2 = RetainFactory.create(getFragmentManager(), yourAwesomeObservable2, new EmptySubscriber<SomeType2>() {
            @Override
            public void onStart() {
                //show load progress.
                //It will be called after rotation
            }

            @Override
            public void onNext(SomeType2 result) {
                //do something
            }
        }, "MY_SECOND_TAG");
        
        rotateExample.subscibe()//call to start if not started
        rotateExample2.subscibe()//call to start if not started
    }
}
```
##### Start or subscribe for running or finished observable with the same tag
``` Java
public class MainActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RetainFactory.start(getFragmentManager(), yourAwesomeObservable, new EmptySubscriber<SomeType>() {
            @Override
            public void onStart() {
                //show load progress.
                //It will be called after rotation
            }

            @Override
            public void onNext(SomeType result) {
                //do something
            }
        });
    }
}
```

##### Clear result for current RetainWrapper
```Java
curentRetainWrapper.unsubscribe()
```

##### Drop observable for current RetainWrapper
```Java
curentRetainWrapper.unsubscribeAndDropObservable()
```

License
-------

    Copyright 2016 Rovkin Max

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
