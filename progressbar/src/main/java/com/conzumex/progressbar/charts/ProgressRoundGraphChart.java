package com.conzumex.progressbar.charts;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.conzumex.progressbar.R;

import java.util.List;

public class ProgressRoundGraphChart extends LinearLayout {
    String TAG = "ProgressRoundGraphChart";
    LinearLayout parentLayout;
    //DEFAULT VALUES
    int selectedTextColor = getResources().getColor(android.R.color.white);
    int unSelectedTextColor = getResources().getColor(android.R.color.darker_gray);
    int progressTextSelectedColor = getResources().getColor(android.R.color.white);
    int progressTextUnSelectedColor = getResources().getColor(android.R.color.darker_gray);
    int progressBarTintUnSelectedColor = getResources().getColor(android.R.color.darker_gray);
    int progressBarTintSelectedColor = getResources().getColor(android.R.color.white);
    int progressRoundDrawable = R.drawable.custom_ring_progress_bar_round;
    int unSelectedFontFamily = R.font.rb_regular;
    int selectedFontFamily = R.font.rb_bold;
    Typeface unSelectedFontFace = ResourcesCompat.getFont(getContext(),unSelectedFontFamily);
    Typeface selectedFontFace = ResourcesCompat.getFont(getContext(),selectedFontFamily);
    chartSelector selector;
    int selectedItem=-1;
    int initialSelectedPos = -1;
    boolean isUnselectingMode = false;
    float selectedProgressTextSize = -1;
    float unSelectedProgressTextSize = -1;
    public ProgressRoundGraphChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        View parentView = inflater.inflate(R.layout.custom_graph_ring_chart_progress_round_chart, this);
        parentLayout = (LinearLayout) ((LinearLayout) parentView).getChildAt(0);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ProgressRoundGraphChart, 0, 0);
        initialSelectedPos = a.getInteger(R.styleable.ProgressRoundGraphChart_selectedPos, -1);

        selectedProgressTextSize = a.getDimensionPixelSize(R.styleable.ProgressRoundGraphChart_selectedProgressTextSize, -1);
        unSelectedProgressTextSize = a.getDimensionPixelSize(R.styleable.ProgressRoundGraphChart_unSelectedProgressTextSize, -1);

        int fontId = a.getResourceId(R.styleable.ProgressRoundGraphChart_selectedFont,0);
        if (fontId != 0)
            selectedFontFace = ResourcesCompat.getFont(context,fontId);
        int unselectedFontId = a.getResourceId(R.styleable.ProgressRoundGraphChart_unSelectedFont,0);
        if (unselectedFontId != 0)
            unSelectedFontFace = ResourcesCompat.getFont(context,unselectedFontId);

        //get the colors
        @ColorInt int newProgressDrawableColor = a.getColor(R.styleable.ProgressRoundGraphChart_selectedProgressColor, -1);
        if (newProgressDrawableColor != -1) progressBarTintSelectedColor = newProgressDrawableColor;

        newProgressDrawableColor = a.getColor(R.styleable.ProgressRoundGraphChart_unSelectedProgressColor, -1);
        if (newProgressDrawableColor != -1) progressBarTintUnSelectedColor = newProgressDrawableColor;

        @ColorInt int newProgressTextColor = a.getColor(R.styleable.ProgressRoundGraphChart_selectedProgressTextColor, -1);
        if (newProgressTextColor != -1) progressTextSelectedColor = newProgressTextColor;

        newProgressTextColor = a.getColor(R.styleable.ProgressRoundGraphChart_unSelectedProgressTextColor, -1);
        if (newProgressTextColor != -1) progressTextUnSelectedColor = newProgressTextColor;

        @ColorInt int newLabelColor = a.getColor(R.styleable.ProgressRoundGraphChart_selectedLabelColor, -1);
        if (newLabelColor != -1) selectedTextColor = newLabelColor;

        newLabelColor = a.getColor(R.styleable.ProgressRoundGraphChart_unSelectedLabelColor, -1);
        if (newLabelColor != -1) unSelectedTextColor = newLabelColor;

        int newProgressDrawable = a.getResourceId(R.styleable.ProgressRoundGraphChart_progressDrawable, -1);
        if (newProgressDrawable != -1) progressRoundDrawable = newProgressDrawable;

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
            ConstraintLayout clParentProgress = (ConstraintLayout) itemLayout.getChildAt(2);
            ProgressBar progressBar = (ProgressBar) clParentProgress.getChildAt(0);
            TextView progressText = (TextView) clParentProgress.getChildAt(1);
            progressBar.setProgress(progressValues.get(i));
            progressText.setText(progressValues.get(i)+"");
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

//    void makeItemSelected(int pos){
//        if(parentLayout==null)
//            Log.e(TAG,"Parent item is null");
//        else{
//            LinearLayout itemLayout = (LinearLayout) parentLayout.getChildAt(pos);
//            itemLayout.setScaleX(1.15f);
//            itemLayout.setScaleY(1.15f);
//            //for label
//            TextView tvTitle = (TextView) itemLayout.getChildAt(1);
//            tvTitle.setTextColor(selectedTextColor);
//            tvTitle.setTypeface(selectedFontFace);
//            //for progress
//            ConstraintLayout clParentProgress = (ConstraintLayout) itemLayout.getChildAt(2);
//            ProgressBar progressBar = (ProgressBar) clParentProgress.getChildAt(0);
//            progressBar.setProgressDrawable(getResources().getDrawable(progressRoundDrawable));
//            progressBar.setProgressTintList(ColorStateList.valueOf(progressBarTintSelectedColor));
//            TextView progressText = (TextView) clParentProgress.getChildAt(1);
//            progressText.setTypeface(selectedFontFace);
//            progressText.setTextColor(progressTextSelectedColor);
//            float currentSize = progressText.getTextSize();
//            progressText.setTextSize(currentSize+5);
//        }
//    }
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
            ConstraintLayout clParentProgress = (ConstraintLayout) itemLayout.getChildAt(2);
            ProgressBar progressBar = (ProgressBar) clParentProgress.getChildAt(0);
            progressBar.setProgressDrawable(getResources().getDrawable(progressRoundDrawable));
            progressBar.setProgressTintList(ColorStateList.valueOf(progressBarTintSelectedColor));
            TextView progressText = (TextView) clParentProgress.getChildAt(1);
            progressText.setTypeface(selectedFontFace);
            progressText.setTextColor(progressTextSelectedColor);
            if(selectedProgressTextSize!=-1)
                progressText.setTextSize(selectedProgressTextSize);
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
            ConstraintLayout clParentProgress = (ConstraintLayout) itemLayout.getChildAt(2);
            ProgressBar progressBar = (ProgressBar) clParentProgress.getChildAt(0);
            progressBar.setProgressDrawable(getResources().getDrawable(progressRoundDrawable));
            progressBar.setProgressTintList(ColorStateList.valueOf(progressBarTintUnSelectedColor));
            TextView progressText = (TextView) clParentProgress.getChildAt(1);
            progressText.setTypeface(unSelectedFontFace);
            progressText.setTextColor(progressTextUnSelectedColor);
            if(unSelectedProgressTextSize!=-1)
                progressText.setTextSize(unSelectedProgressTextSize);
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
            ConstraintLayout clParentProgress = (ConstraintLayout) itemLayout.getChildAt(2);
            ProgressBar progressBar = (ProgressBar) clParentProgress.getChildAt(0);
            progressBar.setProgressDrawable(getResources().getDrawable(progressRoundDrawable));
            progressBar.setProgressTintList(ColorStateList.valueOf(progressBarTintUnSelectedColor));
            TextView progressText = (TextView) clParentProgress.getChildAt(1);
            progressText.setTypeface(unSelectedFontFace);
            progressText.setTextColor(progressTextUnSelectedColor);
            if(unSelectedProgressTextSize!=-1)
                progressText.setTextSize(unSelectedProgressTextSize);
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

    public interface chartSelector{
        void onSelect(int pos);
    }

    public void setOnChartSelector(chartSelector selector){
        this.selector = selector;
    }
}
