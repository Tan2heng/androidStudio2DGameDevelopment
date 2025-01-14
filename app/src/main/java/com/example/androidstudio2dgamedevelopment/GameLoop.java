package com.example.androidstudio2dgamedevelopment;

import android.graphics.Canvas;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.util.zip.Adler32;

public class GameLoop extends Thread{
    private static final double MAX_UPS = 30.0;
    private static final double UPS_PERIOD = 1E+3 / MAX_UPS;
    private boolean isRunning = false;
    private SurfaceHolder surfaceHolder;
    private Game game;
    private double averageUPS;
    private double averageFPS;

    public GameLoop(Game game, SurfaceHolder surfaceHolder) {
        this.game = game;
        this.surfaceHolder = surfaceHolder;
    }

    public double getAverageUPS() {
        return averageUPS;
    }

    public double getAverageFPS() {
        return averageFPS;
    }

    public void startLoop() {
        isRunning = true;
        start();
    }

    @Override
    public void run() {
        super.run();
        //Declaring time and cycle count variables
        int updateCount = 0;
        int frameCount = 0;
        long startTime;
        long elapsedTime;
        long sleepTime = 0;

        //Game loop
        Canvas canvas = null;
        startTime = System.currentTimeMillis();
        while(isRunning){



            //Try to  update and render game
            try {
                canvas = surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    game.update();
                    updateCount++;

                    game.draw(canvas);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } finally {
                if(canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                        frameCount++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            //Pause Game Loop to not exceed target UPS
            elapsedTime = System.currentTimeMillis() - startTime;
            sleepTime = (long)(updateCount* UPS_PERIOD - elapsedTime);
            if (sleepTime > 0) {
                try {
                    sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //skip frames to keep up with target UPS
            while(sleepTime < 0 && updateCount < MAX_UPS-1) {
                game.update();
                updateCount++;
                elapsedTime = System.currentTimeMillis() - startTime;
                sleepTime = (long)(updateCount* UPS_PERIOD - elapsedTime);
            }
            // calculate average UPS and FPS
            elapsedTime = System.currentTimeMillis() - startTime;
            if(elapsedTime >= 1000) {
                averageUPS = updateCount / (1E-3 * elapsedTime);
                averageFPS = frameCount / (1E-3 * elapsedTime);
                updateCount = 0;
                frameCount = 0;
                startTime = System.currentTimeMillis();
            }
        }
    }
}
