/**
 * Peer to peer whiteboard
 * Distributed computing.
 * CMPSMC34
 */

package com.twizted.Utility;

import com.twizted.Display.Frame;

import java.awt.*;
import java.util.Random;

/**
 * @author Ian Weeks 6204848
 *         <p>
 *         DrawRobot class.
 *         <p>
 *         Sends random draw instructions to test the program.
 */
public class DrawRobot implements Runnable
{
    private final String chars;
    private boolean robotOn;
    private Frame frame;
    private Random random;

    /**
     * DrawRobot constructor.
     *
     * @param frame A reference to the parent frame.
     */
    public DrawRobot(Frame frame)
    {
        random = new Random();
        this.frame = frame;
        robotOn = true;
        //Char string for random char drawing.
        chars = "abcdefghijklmnopqrstuvwxyz1234567890,.!?ABCDEFGHIJKLMNOPQRSTUVQXYZ";
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run()
    {
        float doText = 0.66f, doCircle = 0.33f;
        int second = 1000, tenthSecond = 100;
        int x, y, size = 1;
        Color color = Color.BLACK;
        x = random.nextInt(frame.getWidth());
        y = random.nextInt(frame.getCanvasHeight());

        //Robot on continuous loop.
        while (robotOn)
        {
            try
            {
                //Check if robot is on.
                if (frame.getSelectPanel().isRobot())
                {
                    //If robot mode is selected.
                    size = changeSize(size);
                    color = changeColour(color);
                    int[] data;

                    //Selection of next robot action.
                    double nextDouble = random.nextDouble();
                    if (nextDouble > doText)
                    {
                        //Text: 33% chance.
                        x += frame.getDraw().getTextOffset();
                        char c = chars.charAt(random.nextInt(chars.length()));
                        data = new int[]{frame.getId(), Draw.TEXT, x, y, (int) c, color.getRGB(), size};
                        x = random.nextInt(frame.getWidth());
                        y = random.nextInt(frame.getCanvasHeight());
                        frame.getDraw().resetTextOffset();

                    } else if (nextDouble > doCircle)
                    {
                        //Circle 33% chance
                        int radius = random.nextInt(15) + 5;
                        x -= radius;
                        y -= radius;
                        data = new int[]{frame.getId(), Draw.CIRCLE, x, y, radius, color.getRGB(), size};
                        x = random.nextInt(frame.getWidth());
                        y = random.nextInt(frame.getCanvasHeight());
                    } else
                    {
                        //Line 33% chance.
                        int endY;
                        int endX;
                        endX = random.nextInt(frame.getWidth());
                        endY = random.nextInt(frame.getCanvasHeight());
                        data = new int[]{frame.getId(), Draw.DRAW, x, y, endX, endY, color.getRGB(), size};
                        x = random.nextInt(frame.getWidth());
                        y = random.nextInt(frame.getCanvasHeight());
                    }

                    frame.getDraw().put(data);
                    frame.getBroadcaster().put(data);
                    Thread.sleep(tenthSecond);
                } else
                {
                    //If robot mode not selected then just wait a second and try again.
                    Thread.sleep(second);
                }
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    private int changeSize(int currentSize)
    {
        //Chance line or text size based on small probability
        float sizeChange = 0.97f;
        return (random.nextDouble() > sizeChange) ? random.nextInt(9) + 1 : currentSize;
    }

    private Color changeColour(Color currentColour)
    {
        //Chance line or text colour based on small probability
        float colourChange = 0.8f;
        if (random.nextDouble() > colourChange)
        {
            float r = random.nextFloat();
            float g = random.nextFloat();
            float b = random.nextFloat();
            return new Color(r, g, b);
        }
        return currentColour;
    }
}
