package com.me.openstreetmapdemo

import android.graphics.Paint
import org.osmdroid.views.overlay.PaintList

class MonochromaticPaintList(private val mPaint:Paint) : PaintList {

    override fun getPaint(): Paint = mPaint

    override fun getPaint(pIndex: Int, pX0: Float, pY0: Float, pX1: Float, pY1: Float): Paint? = null
}