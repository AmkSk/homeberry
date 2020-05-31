package sk.amk.homeberry.view

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import sk.amk.homeberry.R

/**
 * @author Andrej Martinák <andrej.martinak@gmail.com>
 */
class WhiteCircularBackground : RelativeLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setBackgroundResource(R.drawable.white_circle)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setBackgroundResource(R.drawable.white_circle)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (widthMeasureSpec > heightMeasureSpec) {
            super.onMeasure(heightMeasureSpec, heightMeasureSpec)
        } else {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec)
        }
    }
}
