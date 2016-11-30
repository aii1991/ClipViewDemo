package com.jason.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * @author zjh
 * @date 2016/11/22
 */
public class ClipView extends ImageView{
    private int currentStauts; //是否为拖到状态
    private static final int STATUS_INSIDE_DRAG = 1; //拖拽状态
    private static final int STATUS_OUTSIDE_DRAG = 2;
    private static final int STATUS_ZOOM = 3; //缩放状态
    private final int minWidth = 100; //最小宽度
    private final int minHeight = 50; //最小高度
    private final int maxWidth = 400; //最大宽度
    private final int maxHeight = 450; //最大高度
    private int width = minWidth;
    private int height = minHeight;
    private Paint mRectPaint = new Paint(); //矩形画笔
    private Paint mCirclePaint = new Paint();
    private Point startPoint = new Point(10,10); //起始点
    private boolean isInitDrawRect = false; //是否进行绘制矩形
    private final int radius = 30; //半径
    private final int STROKE_width = 5;
    private Point circlePoint = new Point(); //圆心
    private int lastX;
    private int lastY;

    public ClipView(Context context) {
        super(context);
        init();
    }

    public ClipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClipView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mRectPaint.setColor(Color.RED);
        mRectPaint.setAntiAlias(true);
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setStrokeWidth(STROKE_width);

        mCirclePaint.setColor(Color.BLACK);
        mCirclePaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isInitDrawRect) return;
//        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        int left = startPoint.x;
        int top = startPoint.y;
        if (width < minWidth){
            width = minWidth;
        }
        if (width > maxWidth){
            width = maxWidth;
        }
        if (height < minHeight){
            height = minHeight;
        }
        if (height > maxHeight){
            height = maxHeight;
        }
        int right = startPoint.x + width;
        int bottom = startPoint.y + height;

        canvas.drawRect(left, top, right, bottom, mRectPaint);

        circlePoint.set(right, bottom);
        canvas.drawCircle(right, bottom, radius, mCirclePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if (!isInitDrawRect){
                    isInitDrawRect = true;
                    startPoint.set(checkBorderX(x), checkBorderY(y));
                    postInvalidate();
                }else {
                    if (isTouchCircle(x,y)){
                        currentStauts = STATUS_ZOOM;
                    }else {
                        if (!insideRect(x,y)){
                            currentStauts = STATUS_OUTSIDE_DRAG;
                            startPoint.set(checkBorderX(x), checkBorderY(y));
                            postInvalidate();
                        }else {
                            currentStauts = STATUS_INSIDE_DRAG;
                        }
                    }
                }
                lastX  = x;
                lastY  = y;
                break;
            case MotionEvent.ACTION_MOVE:
                if (currentStauts == STATUS_OUTSIDE_DRAG){
                    startPoint.set(checkBorderX(x),checkBorderY(y));
                }else if(currentStauts == STATUS_INSIDE_DRAG){
                    int distanceX = x - lastX;
                    int distanceY = y - lastY;
                    if (checkBorderMoveX(distanceX) && checkBorderMoveY(distanceY)){
                        startPoint.offset(distanceX,distanceY);
                    }
                } else {
                    int moveDistance = get2PointDistance(lastX,lastY,x,y);
                    if (y - lastY > 0){
                        if (checkBorderMoveX(moveDistance) && checkBorderMoveY(moveDistance)){
                            width += moveDistance;
                            height += moveDistance;
                        }
                    }else {
                        width -= moveDistance;
                        height -= moveDistance;
                    }
                }
                lastX = x;
                lastY = y;
                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }


    /**
     * X-拖拽状态下的边界检查
     * @param distanceX
     * @return
     */
    private boolean checkBorderMoveX(int distanceX){
        if (startPoint.x > 0 && startPoint.x + width + distanceX < getMeasuredWidth()){
            return true;
        }
        return false;
    }

    /**
     * Y-拖拽状态下的边界检查
     * @param distanceY
     * @return
     */
    private boolean checkBorderMoveY(int distanceY){
        if (startPoint.y > 0 && startPoint.y + height + distanceY < getMeasuredHeight()){
            return true;
        }
        return false;
    }

    /**
     * X-边界检查
     * @param x
     * @return
     */
    private int checkBorderX(int x){
        int resultX = 0;
        if (x > 0 && (x + width < getMeasuredWidth())){
            resultX = x;
        }else {
            if (x + width > getMeasuredWidth()){
                resultX = x - ((x + width) - getMeasuredWidth());
            }
            if (x < 0){
                resultX = 0;
            }
        }
        return resultX;
    }

    /**
     * Y-边界检查
     * @param y
     * @return
     */
    private int checkBorderY(int y){
        int resultY = 0;
        if (y > 0 && (y + height) < getMeasuredHeight()){
            resultY = y;
        }else {
            if (y + height > getMeasuredHeight()){
                resultY = y - ((y + height) - getMeasuredHeight());
            }
            if (y < 0){
                resultY = 0;
            }
        }
        return resultY;
    }

    /**
     * 是否在矩形内
     * @return
     */
    private boolean insideRect(int x,int y) {
        if ((x > startPoint.x && x < startPoint.x + width) && (y > startPoint.y && y < startPoint.y + height)){
            return true;
        }
        return false;
    }

    /**
     * 是否在圆上
     * @return
     */
    private boolean isTouchCircle(int x,int y){
        int distance = get2PointDistance(x,y,circlePoint.x,circlePoint.y);
        if (distance <= radius){
            return true;
        }
        return false;
    }


    /**
     * 获取2点之间直线距离
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     * @return
     */
   private int get2PointDistance(int startX,int startY,int endX,int endY){
       return (int) Math.sqrt(Math.pow(startX - endX, 2) + Math.pow(startY - endY, 2));
   }

    /**
     * 进行裁剪
     * @return
     */
    public Bitmap clip(){
        Drawable drawable = getDrawable();
        if (drawable == null || !(drawable instanceof BitmapDrawable)){
            return null;
        }
        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();

        final float[] matrixValues = new float[9];
        getImageMatrix().getValues(matrixValues);

        final float scaleX = matrixValues[Matrix.MSCALE_X];
        final float scaleY = matrixValues[Matrix.MSCALE_Y];
        final float transX = matrixValues[Matrix.MTRANS_X];
        final float transY = matrixValues[Matrix.MTRANS_Y];

        float bitmapLeft = (transX < 0) ? Math.abs(transX) : 0;
        float bitmapTop = (transY < 0) ? Math.abs(transY) : 0;
        float clipX = (bitmapLeft + startPoint.x - transX) / scaleX;
        float clipY = (bitmapTop + startPoint.y - transY) / scaleY;
        float clipWidth = width / scaleX ;
        float clipHeight = height / scaleY ;

        if (clipX + clipWidth > bitmap.getWidth()){
            clipWidth = bitmap.getWidth() - clipX;
        }

        if (clipY + clipHeight > bitmap.getHeight()){
            clipHeight = bitmap.getHeight() - clipY;
        }

        return Bitmap.createBitmap(bitmap,(int)clipX,(int)clipY,(int)clipWidth,(int)clipHeight);
    }
}
