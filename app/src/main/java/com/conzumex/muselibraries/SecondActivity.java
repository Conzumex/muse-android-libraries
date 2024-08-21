package com.conzumex.muselibraries;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.conzumex.sticky.StickyScrollView;
import com.conzumex.sticky.ui.interfaces.IScrollViewListener;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        StickyScrollView scroll = findViewById(R.id.scrooll);
//        scroll.setHeaderView(R.id.tv_header);

        TextView tvNonScroll = findViewById(R.id.tv_non_scroll);
        tvNonScroll.setOnClickListener(view -> {
            Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show();
        });
//        tvNonScroll.setOnTouchListener(new View.OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN)
//                    scroll.setScrollable(false);
//                else if (event.getAction() == MotionEvent.ACTION_UP)
//                    scroll.setScrollable(true);
//                return false;
//            }
//        });
        scroll.setScrollViewListener(new IScrollViewListener() {
            @Override
            public void onScrollChanged(int l, int t, int oldl, int oldt) {
                Log.d("Scroll","Over x : "+l+" y: "+t+" clamp x : "+oldl+" y: "+oldt);
//                IScrollViewListener.super.onScrollChanged(l, t, oldl, oldt);
            }

            @Override
            public void onScrollStopped(boolean isStopped) {
//                IScrollViewListener.super.onScrollStopped(isStopped);
                Log.d("Scroll","stopped "+isStopped);
            }
        });

//        scroll.setOverScrollListener(new IOverScrollListener() {
//            @Override
//            public void onScrollChanged(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
////                IOverScrollListener.super.onScrollChanged(scrollX, scrollY, clampedX, clampedY);
//                Log.d("Scroll","Over x : "+scrollX+" y: "+scrollY+" clamp x : "+clampedX+" y: "+clampedY);
//            }
//        });

    }
}