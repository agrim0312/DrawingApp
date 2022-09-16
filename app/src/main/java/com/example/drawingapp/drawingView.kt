package com.example.drawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import java.lang.reflect.Type


class drawingView(context:Context,attrs:AttributeSet) : View(context,attrs){
    private var mDrawPath : CustomPath? = null
    private var mDrawPaint : Paint? = null
    private var mCanvasBitmap:Bitmap? = null
    private var canvasPaint:Paint? = null
    private var canvas:Canvas? = null
    private var mBrushSize : Float = 0.toFloat()
    private var color: Int = Color.BLACK
    private var mPaths = ArrayList<CustomPath>()

    init {
        setUpEnvironment()
    }

    private fun setUpEnvironment(){
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color,mBrushSize)
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        canvasPaint = Paint(Paint.DITHER_FLAG)
        mBrushSize = 10.toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap  = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(mCanvasBitmap!!,0f,0f,canvasPaint)

        for(path in mPaths){
            canvas?.drawPath(path,mDrawPaint!!)
            mDrawPaint?.strokeWidth = path.brushSize
            mDrawPaint?.color = path.color
        }

        if(!mDrawPath!!.isEmpty){
            canvas?.drawPath(mDrawPath!!,mDrawPaint!!)
            mDrawPaint?.strokeWidth = mDrawPath!!.brushSize
            mDrawPaint?.color = mDrawPath!!.color
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y
        when(event?.action){
            MotionEvent.ACTION_DOWN ->{
                mDrawPath?.color = color
                mDrawPath?.brushSize = mBrushSize
                mDrawPath?.reset()
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath?.moveTo(touchX,touchY)
                    }
                }
            }
            MotionEvent.ACTION_MOVE->{
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath?.lineTo(touchX,touchY)
                    }
                }
            }
            MotionEvent.ACTION_UP->{
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(color,mBrushSize)

            }
            else->{
                return false
            }
        }
        invalidate()

        return true
    }

    fun setStrokeBrushSize(newSize:Float){
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,newSize,resources.displayMetrics)
        mDrawPaint!!.strokeWidth = mBrushSize
    }

    fun coloor(newColor:String){
        color = Color.parseColor(newColor)
        mDrawPaint?.color = color
        mDrawPaint?.strokeWidth = mDrawPath!!.brushSize
    }
    fun UndoFunctionality(){

        if(mPaths.isNotEmpty()){
            mPaths.removeAt(mPaths.lastIndex)

        }
        invalidate()
    }

    internal inner class CustomPath(var color:Int , var brushSize:Float): Path()
}