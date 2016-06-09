package com.bretdeasy.sliderocker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.bretdeasy.sliderocker.view.SlideRocker;

public class MainActivity extends AppCompatActivity {
    private int counter = 0;
    private TextView counterTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        counterTextView = (TextView)findViewById(R.id.text_view_counter);
        SlideRocker slideRocker = (SlideRocker)findViewById(R.id.slideRocker);
        if (slideRocker != null) {
            slideRocker.setOnSlideUpdateListener(new SlideRocker.OnSlideUpdateListener() {
                @Override
                public void onSlideUpdate(SlideRocker slideRocker) {
                    if (slideRocker.isPositive()) {
                        counter++;
                    } else {
                        counter--;
                    }

                    counterTextView.setText(String.valueOf(counter));
                }
            });
        }
    }

}
