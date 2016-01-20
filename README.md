# RxRetainFragment

##### Simple example for orientation changes
``` Java
public class MainActivity extends Activity {
    private RxRetainFragment<SomeType> rotateExample;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rotateExample = RxRetainFactory.create(getFragmentManager(), yourAwesomeObservable, new EmptySubscriber<SomeType>() {
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
    private RxRetainFragment<SomeType> rotateExample;
    private RxRetainFragment<SomeType2> rotateExample2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rotateExample = RxRetainFactory.create(getFragmentManager(), yourAwesomeObservable, new EmptySubscriber<SomeType>() {
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
        
        rotateExample2 = RxRetainFactory.create(getFragmentManager(), yourAwesomeObservable2, new EmptySubscriber<SomeType2>() {
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
        RxRetainFactory.start(getFragmentManager(), yourAwesomeObservable, new EmptySubscriber<SomeType>() {
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
