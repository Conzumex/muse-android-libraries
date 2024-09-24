package com.conzumex.circleseekbar.marker;

import android.content.Context;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.conzumex.cicleseekbar.R;

public class Marker extends RelativeLayout {
    TextView tvValue;
    public Marker(Context context, int layoutId) {
        super(context);
        setupLayoutResource(layoutId);
    }
    public Marker(Context context) {
        super(context);
        setupLayoutResource(com.conzumex.cicleseekbar.R.layout.marker_default);
    }
    private void setupLayoutResource(int layoutResource) {

        View inflated = LayoutInflater.from(getContext()).inflate(layoutResource, this);
        tvValue = inflated.findViewById(R.id.textView);
        inflated.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        inflated.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

         measure(getWidth(), getHeight());
        inflated.layout(0, 0, inflated.getMeasuredWidth(), inflated.getMeasuredHeight());
    }

    public void setContent(CharSequence content){
        tvValue.setText(content);
        measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
        invalidate();
    }

    public void refreshContent(float progress,float progressSecondary){
        if(tvValue!=null)
            tvValue.setText(progress+" : "+progressSecondary);
        measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
        invalidate();
    }

    public void draw(Canvas canvas, float posX, float posY,float minX,float maxX) {
//        super.draw(canvas);
        int saveId = canvas.save();
        float halfWidth = getWidth()/2;
        float midPoint = posX-(halfWidth);
        float midPointY = posY-(getHeight()/2);
        // translate to the correct position and draw
        midPoint = midPoint<minX?midPoint+(minX-midPoint):(midPoint+halfWidth*2)>maxX?midPoint-((midPoint+halfWidth*2)-maxX):midPoint;
        canvas.translate(midPoint , midPointY);
        draw(canvas);
        canvas.restoreToCount(saveId);
    }
}
