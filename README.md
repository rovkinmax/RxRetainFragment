# RxRetainFragment

### Simple example for orientation changes
```
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
