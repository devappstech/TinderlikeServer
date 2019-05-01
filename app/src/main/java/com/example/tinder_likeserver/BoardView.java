package com.example.tinder_likeserver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class BoardView extends View {

    private static final int LINE_THICK = 5;
    private static final int ELT_MARGIN = 20;
    private static final int ELT_STROKE_WIDTH = 15;
    private static int width, height, eltW, eltH;
    private static Paint gridPaint, oPaint, xPaint;
    private static GameEngine gameEngine;
    private static int x;
    private static int y;
    private GameActivity activity;

    public BoardView(Context context) {
        super(context);
    }

    public BoardView(Context context, @Nullable AttributeSet attrs) {

        super(context, attrs);
        gridPaint = new Paint();
        oPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        oPaint.setColor(Color.RED);
        oPaint.setStyle(Paint.Style.STROKE);
        oPaint.setStrokeWidth(ELT_STROKE_WIDTH);
        xPaint = new Paint(oPaint);
        xPaint.setColor(Color.BLUE);
    }

    public void setMainActivity(GameActivity a) {
        activity = a;
    }

    public void setGameEngine(GameEngine g) {
        gameEngine = g;
    }

    public static int getx(){
        return x;
    }

    public static int gety(){
        return y;
    }

    public static void setX(int posX){
        x = posX;
    }

    public static void setY(int posY){
        y = posY;
    }

    //calculate width and height of the elements in the grid
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        height = View.MeasureSpec.getSize(heightMeasureSpec);
        width = View.MeasureSpec.getSize(widthMeasureSpec);
        eltW = (width - LINE_THICK) / 3;
        eltH = (height - LINE_THICK) / 3;

        setMeasuredDimension(width, height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        invalidate();
        if (!gameEngine.isEnded() && event.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) (event.getX() / eltW);
            int y = (int) (event.getY() / eltH);
            int win = gameEngine.play(x, y);
            GameActivity.setPoint(x, y);
            if(win != 0){
                activity.gameEnded(win);
            } else{
                //oponent plays
                 invalidate();

                if(win != 0){
                    activity.gameEnded(win);
                }
            }
            invalidate();

        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        invalidate();
        drawGrid(canvas);
        drawBoard(canvas);
    }

    private void drawBoard(Canvas canvas) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                drawElt(canvas, gameEngine.getElt(i, j), i, j);
            }
        }
    }

    public static void makeAMove(int x, int y){
        Log.d("move", x+ " " + y);
        gameEngine.play(x, y);
    }

    private void drawGrid(Canvas canvas) {
        for (int i = 0; i < 2; i++) {
            // vertical lines
            float left = eltW * (i + 1);
            float right = left + LINE_THICK;
            float top = 0;
            float bottom = height;

            canvas.drawRect(left, top, right, bottom, gridPaint);

            // horizontal lines
            float left2 = 0;
            float right2 = width;
            float top2 = eltH * (i + 1);
            float bottom2 = top2 + LINE_THICK;

            canvas.drawRect(left2, top2, right2, bottom2, gridPaint);
        }
    }

    private static void drawElt(Canvas canvas, int c, int x, int y) {
        if (c == gameEngine.PLAYER_0) {
            float cx = (eltW * x) + eltW / 2;
            float cy = (eltH * y) + eltH / 2;

            canvas.drawCircle(cx, cy, Math.min(eltW, eltH) / 2 - ELT_MARGIN * 2, oPaint);

        } else if (c == gameEngine.PLAYER_X) {
            float startX = (eltW * x) + ELT_MARGIN;
            float startY = (eltH * y) + ELT_MARGIN;
            float endX = startX + eltW - ELT_MARGIN * 2;
            float endY = startY + eltH - ELT_MARGIN;

            canvas.drawLine(startX, startY, endX, endY, xPaint);

            float startX2 = (eltW * (x + 1)) - ELT_MARGIN;
            float startY2 = (eltH * y) + ELT_MARGIN;
            float endX2 = startX2 - eltW + ELT_MARGIN * 2;
            float endY2 = startY2 + eltH - ELT_MARGIN;

            canvas.drawLine(startX2, startY2, endX2, endY2, xPaint);
        }
    }

}
