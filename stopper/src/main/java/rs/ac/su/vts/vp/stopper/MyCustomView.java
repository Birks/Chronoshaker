package rs.ac.su.vts.vp.stopper;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;


public class MyCustomView extends LinearLayout implements OnGlobalLayoutListener {

    public MyCustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout();

    }

    public MyCustomView(Context context) {
        super(context);
        initLayout();
    }

    private void initLayout() {
        setOrientation(LinearLayout.VERTICAL);
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        getViewTreeObserver().removeGlobalOnLayoutListener(this);
        View obj = getChildAt(0);
        int[] pos = new int[2];
        if (obj != null) {
            obj.getLocationOnScreen(pos);
            obj.startAnimation(AnimationUtils.loadAnimation(getContext(),
                    R.anim.customview_anim_in));
        }


    }

    public void onLayoutClose(){
        Log.v("stopper_verbose", "onlayoutclose");
        final View obj = getChildAt(0);
        int[] pos = new int[2];
        if (obj != null) {
            obj.getLocationOnScreen(pos);
            AnimationSet s = new AnimationSet(false);
            s.addAnimation(AnimationUtils.loadAnimation(getContext(),
                    R.anim.customview_anim_out));

                 s.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        Log.v("stopper_verbose", "onAnimationStart");
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        Log.v("stopper_verbose", "onAnimationEnd");


                        obj.startAnimation(AnimationUtils.loadAnimation(getContext(),
                                R.anim.customview_anim_in));

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

            obj.startAnimation(s);

            /*
            obj.startAnimation(AnimationUtils.loadAnimation(getContext(),
                    R.anim.customview_anim_out));
            */


        }


    }


}


