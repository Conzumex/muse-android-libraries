package com.conzumex.progressbar.charts;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.conzumex.progressbar.ProgressTextFormatter;
import com.conzumex.progressbar.R;
import com.conzumex.progressbar.RoundedProgressBarVertical;

import java.text.DecimalFormat;
import java.util.List;

public class ProgressBarGraphChart extends LinearLayout {
    String TAG = "ProgressBarGraphChart";
    LinearLayout parentLayout;
    //DEFAULT VALUES
    int selectedTextColor = getResources().getColor(android.R.color.white);
    int unSelectedTextColor = getResources().getColor(android.R.color.darker_gray);
    int progressTextUnselectedColor = getResources().getColor(android.R.color.darker_gray);
    int progressTextSelectedColor = getResources().getColor(android.R.color.black);
    int progressBarBackgroundColor = getResources().getColor(android.R.color.black);
    int progressBarBackgroundUnselectedColor = getResources().getColor(android.R.color.black);
    int progressBarProgressColor = getResources().getColor(android.R.color.white);
    int progressBarProgressUnSelectedColor = getResources().getColor(android.R.color.darker_gray);
    int unSelectedFontFamily = R.font.rb_regular;
    int selectedFontFamily = R.font.rb_bold;
    Typeface unSelectedFontFace = ResourcesCompat.getFont(getContext(),unSelectedFontFamily);
    Typeface selectedFontFace = ResourcesCompat.getFont(getContext(),selectedFontFamily);
    int barHeight = 0;
    float cornerRadius = -1;
    chartSelector selector;
    int selectedItem=-1;
    int initialSelectedPos = -1;
    boolean isUnselectingMode = false;
    public ProgressBarGraphChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        View parentView = inflater.inflate(R.layout.custom_graph_ring_chart_progress_bar_chart, this);
        parentLayout = (LinearLayout) ((LinearLayout) parentView).getChildAt(0);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ProgressBarGraphChart, 0, 0);
        barHeight = a.getDimensionPixelSize(R.styleable.ProgressBarGraphChart_barHeight, 0);
        initialSelectedPos = a.getInteger(R.styleable.ProgressBarGraphChart_selectedPos, -1);

        int fontId = a.getResourceId(R.styleable.ProgressBarGraphChart_selectedFont,0);
        if (fontId != 0)
            selectedFontFace = ResourcesCompat.getFont(context,fontId);
        int unselectedFontId = a.getResourceId(R.styleable.ProgressBarGraphChart_unSelectedFont,0);
        if (unselectedFontId != 0)
            unSelectedFontFace = ResourcesCompat.getFont(context,unselectedFontId);

        //get the colors
        @ColorInt int newProgressDrawableColor = a.getColor(R.styleable.ProgressBarGraphChart_selectedProgressColor, -1);
        if (newProgressDrawableColor != -1) progressBarProgressColor = newProgressDrawableColor;

        newProgressDrawableColor = a.getColor(R.styleable.ProgressBarGraphChart_unSelectedProgressColor, -1);
        if (newProgressDrawableColor != -1) progressBarProgressUnSelectedColor = newProgressDrawableColor;

        @ColorInt int newProgressBackgroundColor = a.getColor(R.styleable.ProgressBarGraphChart_selectedProgressBackgroundColor, -1);
        if (newProgressBackgroundColor != -1) progressBarBackgroundColor = newProgressBackgroundColor;

        newProgressBackgroundColor = a.getColor(R.styleable.ProgressBarGraphChart_unSelectedProgressBackgroundColor, -1);
        if (newProgressBackgroundColor != -1) progressBarBackgroundUnselectedColor = newProgressBackgroundColor;

        @ColorInt int newProgressTextColor = a.getColor(R.styleable.ProgressBarGraphChart_selectedProgressTextColor, -1);
        if (newProgressTextColor != -1) progressTextSelectedColor = newProgressTextColor;

        newProgressTextColor = a.getColor(R.styleable.ProgressBarGraphChart_unSelectedProgressTextColor, -1);
        if (newProgressTextColor != -1) progressTextUnselectedColor = newProgressTextColor;

        @ColorInt int newLabelColor = a.getColor(R.styleable.ProgressBarGraphChart_selectedLabelColor, -1);
        if (newLabelColor != -1) selectedTextColor = newLabelColor;

        newLabelColor = a.getColor(R.styleable.ProgressBarGraphChart_unSelectedLabelColor, -1);
        if (newLabelColor != -1) unSelectedTextColor = newLabelColor;

        cornerRadius = a.getDimension(R.styleable.ProgressBarGraphChart_cornerRadius, -1);

        initView();
    }

    //set labels on top for the 7 values
    public void setLabels(List<String> dayNames){
        if(parentLayout==null){
            Log.e(TAG,"parent is null");
            return;
        }
        if(parentLayout.getChildCount()!=dayNames.size()){
            Log.e(TAG,"labels and graph items are different size");
            return;
        }
        for(int i=0;i<parentLayout.getChildCount();i++) {
            LinearLayout itemLayout = (LinearLayout) parentLayout.getChildAt(i);
            TextView tvTitle = (TextView) itemLayout.getChildAt(1);
            tvTitle.setText(dayNames.get(i));
        }
    }

    /** set progress percentage for the 7 values
     * progress in 100 only
     */
    public void setProgressData(List<Integer> progressValues){
        if(parentLayout==null){
            Log.e(TAG,"parent is null");
            return;
        }
        if(parentLayout.getChildCount()!=progressValues.size()){
            Log.e(TAG,"progress values and graph items are different size");
            return;
        }
        for(int i=0;i<parentLayout.getChildCount();i++) {
            LinearLayout itemLayout = (LinearLayout) parentLayout.getChildAt(i);
            RoundedProgressBarVertical rpProgressBar = (RoundedProgressBarVertical) itemLayout.getChildAt(2);
            rpProgressBar.setProgressPercentage(progressValues.get(i),false);
        }
    }

    public void initView(){
        for(int i=0;i<parentLayout.getChildCount();i++){
            View graphItem = parentLayout.getChildAt(i);
            //to make all items unselected
            makeItemUnSelected(i);
            //set initial selected item
            if(initialSelectedPos != -1 && i==initialSelectedPos){
                selectedItem = initialSelectedPos;
                makeItemSelectedAnimated(initialSelectedPos);
            }
            graphItem.setTag(i);
            //set the listeners
            graphItem.setOnClickListener(view -> {
                int tempViewPos = (int) view.getTag();
                //check for the current selected item is same for the clicked item
                if(selectedItem == tempViewPos){
                    //check for unselect the selected one mode is on
                    if(isUnselectingMode){
                        makeItemUnSelectedAnimated(selectedItem);
                        selectedItem = -1;
                    }
                    //call selector event
                    if(selector!=null)
                        selector.onSelect(selectedItem);
                }else{
                    makeItemUnSelectedAnimated(selectedItem);
                    selectedItem = tempViewPos;
                    makeItemSelectedAnimated(tempViewPos);
                    //call selector event
                    if(selector!=null)
                        selector.onSelect(tempViewPos);
                }

            });
        }
    }

    void makeItemSelected(int pos){
        if(parentLayout==null)
            Log.e(TAG,"Parent item is null");
        else{
            LinearLayout itemLayout = (LinearLayout) parentLayout.getChildAt(pos);
            itemLayout.setScaleX(1.15f);
            itemLayout.setScaleY(1.15f);
            //for label
            TextView tvTitle = (TextView) itemLayout.getChildAt(1);
            tvTitle.setTextColor(selectedTextColor);
            tvTitle.setTypeface(selectedFontFace);
            //for progress
            RoundedProgressBarVertical rpProgressBar = (RoundedProgressBarVertical) itemLayout.getChildAt(2);
            rpProgressBar.setCustomFontFamily(selectedFontFamily);
            rpProgressBar.setProgressDrawableColor(progressBarProgressColor);
            rpProgressBar.setBackgroundDrawableColor(progressBarBackgroundColor);
            rpProgressBar.setProgressTextColor(progressTextSelectedColor);
        }
    }
    void makeItemSelectedAnimated(int pos){
        if(parentLayout==null)
            Log.e(TAG,"Parent item is null");
        else{
            LinearLayout itemLayout = (LinearLayout) parentLayout.getChildAt(pos);
            itemLayout.animate()
                    .scaleY(1.15f)
                    .scaleX(1.15f)
                    .start();
            //for label
            TextView tvTitle = (TextView) itemLayout.getChildAt(1);
            tvTitle.setTextColor(selectedTextColor);
            tvTitle.setTypeface(selectedFontFace);
            //for progress
            RoundedProgressBarVertical rpProgressBar = (RoundedProgressBarVertical) itemLayout.getChildAt(2);
            rpProgressBar.setCustomFontFamily(selectedFontFamily);
            rpProgressBar.setProgressDrawableColor(progressBarProgressColor);
            rpProgressBar.setBackgroundDrawableColor(progressBarBackgroundColor);
            rpProgressBar.setProgressTextColor(progressTextSelectedColor);
        }
    }

    void makeItemUnSelected(int pos){
        if(parentLayout==null)
            Log.e(TAG,"Parent item is null");
        else{
            if(pos==-1){
                Log.e(TAG,"clicked on first item");
                return;
            }
            LinearLayout itemLayout = (LinearLayout) parentLayout.getChildAt(pos);
            itemLayout.setScaleX(1f);
            itemLayout.setScaleY(1f);
            //for label
            TextView tvTitle = (TextView) itemLayout.getChildAt(1);
            tvTitle.setTextColor(unSelectedTextColor);
            tvTitle.setTypeface(unSelectedFontFace);
            //for progress
            RoundedProgressBarVertical rpProgressBar = (RoundedProgressBarVertical) itemLayout.getChildAt(2);
            rpProgressBar.setCustomFontFamily(unSelectedFontFamily);
            rpProgressBar.setProgressDrawableColor(progressBarProgressUnSelectedColor);
            rpProgressBar.setBackgroundDrawableColor(progressBarBackgroundUnselectedColor);
            rpProgressBar.setProgressTextColor(progressTextUnselectedColor);
            if(barHeight!=0){
                rpProgressBar.getLayoutParams().height = barHeight;
            }
            if(cornerRadius!=-1){
                rpProgressBar.setCornerRadius(cornerRadius);
            }
        }
    }
    void makeItemUnSelectedAnimated(int pos){
        if(parentLayout==null)
            Log.e(TAG,"Parent item is null");
        else{
            if(pos==-1){
                Log.e(TAG,"clicked on first item");
                return;
            }
            LinearLayout itemLayout = (LinearLayout) parentLayout.getChildAt(pos);
            itemLayout.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .start();
            //for label
            TextView tvTitle = (TextView) itemLayout.getChildAt(1);
            tvTitle.setTextColor(unSelectedTextColor);
            tvTitle.setTypeface(unSelectedFontFace);
            //for progress
            RoundedProgressBarVertical rpProgressBar = (RoundedProgressBarVertical) itemLayout.getChildAt(2);
            rpProgressBar.setCustomFontFamily(unSelectedFontFamily);
            rpProgressBar.setProgressDrawableColor(progressBarProgressUnSelectedColor);
            rpProgressBar.setBackgroundDrawableColor(progressBarBackgroundUnselectedColor);
            rpProgressBar.setProgressTextColor(progressTextUnselectedColor);
        }
    }

    public void setSelectedItem(int pos){
        if (pos>6) {
            Log.d(TAG, "initial pos is greater than index 6");
            return;
        }

        //check for the current selected item is same for the changed item
        if(selectedItem == pos){
            //check for unselect the selected one mode is on
            if(isUnselectingMode){
                makeItemUnSelectedAnimated(selectedItem);
                selectedItem = -1;
            }
            //call selector event
            if(selector!=null)
                selector.onSelect(selectedItem);
        }else{
            makeItemUnSelectedAnimated(selectedItem);
            selectedItem = pos;
            makeItemSelectedAnimated(pos);
            //call selector event
            if(selector!=null)
                selector.onSelect(pos);
        }
    }

    public void setProgressFormatter(ProgressTextFormatter mFormatter) {
        if(parentLayout==null)
            Log.e(TAG,"Parent item is null");
        else{
            for(int i=0;i<parentLayout.getChildCount();i++){
                LinearLayout itemLayout = (LinearLayout) parentLayout.getChildAt(i);
                //for progress
                RoundedProgressBarVertical rpProgressBar = (RoundedProgressBarVertical) itemLayout.getChildAt(2);
                rpProgressBar.setProgressTextFormatter(mFormatter);
            }
        }
    }

    public void disablePercentage(String minWidthString){
        ProgressTextFormatter mFormatter = new ProgressTextFormatter() {
                @NonNull
                @Override
                public String getMinWidthString() {
                    return minWidthString;
                }

                @NonNull
                @Override
                public String getProgressText(float progressValue) {
                    return new DecimalFormat("#").format(progressValue*100)+"";
                }
            };
        setProgressFormatter(mFormatter);
    }

    public void disablePercentage(){
        ProgressTextFormatter mFormatter = new ProgressTextFormatter() {
            @NonNull
            @Override
            public String getMinWidthString() {
                return "9";
            }

            @NonNull
            @Override
            public String getProgressText(float progressValue) {
                return new DecimalFormat("#").format(progressValue*100)+"";
            }
        };
        setProgressFormatter(mFormatter);
    }

    public interface chartSelector{
        void onSelect(int pos);
    }

    public void setOnChartSelector(chartSelector selector){
        this.selector = selector;
    }
}
