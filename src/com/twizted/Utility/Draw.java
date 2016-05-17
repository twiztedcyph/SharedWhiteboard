/**
 * Peer to peer whiteboard
 * Distributed computing.
 * CMPSMC34
 */
package com.twizted.Utility;

import com.twizted.Display.Frame;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Ian Weeks 6204848
 *         <p>
 *         Draw class.
 *         <p>
 *         Handles all actual drawing to the main canvas.
 */
public class Draw implements Runnable
{
    public static final int DRAW = 0, CLEAR = 1, TEXT = 2, CIRCLE = 3, IMAGE = 4;
    private static int textOffset;
    private Frame frame;
    private volatile BlockingQueue<int[]> queue;
    private boolean runDraw;
    private int canvasWidth, canvasHeight;
    private static Draw instance;

    public static Draw getInstance(Frame frame, int canvasWidth, int canvasHeight)
    {
        if (instance == null)
        {
            instance = new Draw(frame, canvasWidth, canvasHeight);
        }
        return instance;
    }

    /**
     * Draw constructor.
     *
     * @param frame        A reference to the parent frame.
     * @param canvasWidth  The drawable canvas width.
     * @param canvasHeight The drawable canvas height.
     */
    private Draw(Frame frame, int canvasWidth, int canvasHeight)
    {
        queue = new LinkedBlockingQueue<>();
        this.frame = frame;
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        runDraw = true;
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
        //String to test for punctuation.
        String punctuation = ",.?!'\"";
        while (runDraw)
        {
            try
            {
                /*
                 * Draw data comes from a blocking queue.
                 *
                 * The draw object waits until a draw instruction
                 * is available and then sorts and executes it.
                 *
                 * This queuing system is very efficient.  It
                 * allows the program to carry out hundreds of
                 * instructions per second.
                 */
                int[] data = queue.take();
                if (frame.getSelectPanel().network() && !frame.getSelectPanel().isReplaying())
                {
                    frame.getActionCache().put(data);
                    frame.getHistorySize().setText(String.format("History size: %d", frame.getActionCache().getSize()));
                }
                //System.out.println("after after block");
                Graphics2D g2d = (Graphics2D) frame.getBs().getDrawGraphics();

                switch (data[1])
                {
                    case DRAW:
                        Point from = new Point(data[2], data[3]);
                        Point to = new Point(data[4], data[5]);
                        g2d.setColor(new Color(data[6]));
                        g2d.setStroke(new BasicStroke(data[7]));
                        g2d.drawLine(from.x, from.y, to.x, to.y);
                        break;
                    case CLEAR:
                        g2d.setColor(Frame.ERASE);
                        g2d.fillRect(0, 0, canvasWidth, canvasHeight);
                        break;
                    case TEXT:
                        Point start = new Point(data[2], data[3]);
                        char c = (char) data[4];
                        g2d.setColor(new Color(data[5]));
                        //Font scaling.
                        Font oldFont = g2d.getFont();
                        Font newFont = oldFont.deriveFont(oldFont.getSize() * (float) (15 * data[6] / 10));
                        g2d.setFont(newFont);

                        if (Character.isLetter(c) || Character.isSpaceChar(c) || Character.isDigit(c) || punctuation
                                .contains(String.valueOf(c)))
                        {
                            String s = String.valueOf(c);
                            //Get the offset for the next character.
                            int i = g2d.getFontMetrics().stringWidth(s);
                            textOffset += i;
                            g2d.drawString(s, start.x, start.y);
                        }
                        break;
                    case CIRCLE:
                        Point center = new Point(data[2], data[3]);
                        g2d.setColor(new Color(data[5]));
                        g2d.setStroke(new BasicStroke(data[6]));
                        g2d.drawOval(center.x, center.y, data[4] * 2, data[4] * 2);
                        break;
                    case IMAGE:
                        Point imageStart = new Point(data[2], data[3]);
                        System.out.println("Before request");
                        BufferedImage image = frame.getImageCache().get(data[4], data[8]);
                        System.out.println("After request");
                        g2d.drawImage(image, imageStart.x, imageStart.y, null);
                        System.out.println("After draw");
                        break;
                    default:
                        //Should never get here but error message just in case.
                        System.err.println("Unrecognised draw command.");
                }

                g2d.dispose();
                frame.getBs().show();
            } catch (InterruptedException e)
            {
                System.out.println("Error in draw.");
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * @param data Data array to be drawn.
     * @throws InterruptedException If the queue is interrupted while waiting.
     */
    public synchronized void put(int[] data) throws InterruptedException
    {
        queue.put(data);
    }

    /**
     * Get the offset for the next char.
     *
     * @return The offset for the next char.
     */
    public int getTextOffset()
    {
        return textOffset;
    }

    /**
     * Reset the text offset.
     */
    public void resetTextOffset()
    {
        textOffset = 0;
    }
}
