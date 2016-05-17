/**
 * Peer to peer whiteboard
 * Distributed computing.
 * CMPSMC34
 */

package com.twizted.Display;

import com.twizted.Cache.ActionCache;
import com.twizted.Cache.ImageCache;
import com.twizted.Cache.PeerCache;
import com.twizted.FileDrop;
import com.twizted.Network.UdpBroadcaster;
import com.twizted.Network.UdpReceiver;
import com.twizted.Utility.Draw;
import com.twizted.Utility.DrawRobot;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import static com.twizted.Network.UdpReceiver.*;


/**
 * @author Ian Weeks 6204848
 *         <p>
 *         Frame class.
 *         <p>
 *         Creates the window and canvas as well
 *         as serving as the parent for all other
 *         classes.
 */
public class Frame extends Canvas implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener
{
    public final static Color ERASE = Color.WHITE;
    private static int imageCount = 0;
    private final PeerCache peerCache = new PeerCache();
    private final SelectPanel selectPanel;
    private final Draw draw;
    private final ActionCache actionCache;
    private final int[] myIp;
    private volatile BufferStrategy bs;
    private Point start, end;
    private UdpBroadcaster broadcaster;
    private ColourPanel colourPanel;
    private volatile JLabel peerCount, historySize;
    private boolean exiting;
    private int width = 1000, height = 800, canvasHeight = (int) (height * 0.714);
    private ImageCache imageCache;


    /**
     * * Default Frame constructor.
     *
     * @throws IOException If any IO error occurs.
     */
    public Frame() throws IOException
    {
        String ipString = InetAddress.getLocalHost().toString();
        myIp = new int[4];
        for (int i = 0; i < myIp.length; i++)
        {
            myIp[i] = Integer.valueOf(ipString.split("/")[1].split("\\.")[i]);
        }

        /*
         * Note that the start end
         * system was chosen to
         * prevent gaps in the
         * drawing when the mouse
         * is moving fast.
         *
         * This allowed for a
         * much more fluid
         * drawing experience.
         */
        start = new Point();
        end = new Point();

        // Add the various listeners required.
        this.addKeyListener(this);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);

        JFrame frame = new JFrame("6204848 UDP Whiteboard by Ian Weeks");

        //On exit notify the network that this peer is leaving.
        frame.addWindowListener(new WindowAdapter()
        {
            /**
             * Invoked when a window is in the process of being closed.
             * The close operation can be overridden at this point.
             *
             * @param e The event which indicates that the window is closing.
             */
            @Override
            public void windowClosing(WindowEvent e)
            {
                try
                {
                    exiting = true;
                    int[] leaveData = {getId(), LEAVE_NOTE, myIp[0], myIp[1], myIp[2], myIp[3]};
                    actionCache.clear();
                    broadcaster.put(leaveData);
                    Thread.sleep(200);
                    System.exit(0);
                } catch (InterruptedException e1)
                {
                    e1.printStackTrace();
                }
            }
        });

        frame.setPreferredSize(new Dimension(width, height));
        frame.setResizable(false);

        // Set the background to the erase colour.
        this.setBackground(ERASE);
        this.setIgnoreRepaint(true);
        this.setPreferredSize(new Dimension(width, canvasHeight));

        frame.getContentPane().add(Frame.this, BorderLayout.NORTH);

        JPanel palletPanel = new JPanel();
        palletPanel.setBackground(Color.LIGHT_GRAY);
        palletPanel.setPreferredSize(new Dimension(width, (int) (height * 0.258)));
        palletPanel.setLayout(null);

        /*
         * Start the broadcasting, receiving and
         * drawing components of the system.
         *
         * Each runs in its own thread.
         *
         * Draw and broad cast use a synchronised
         * blocking queue system.
         */
        draw = Draw.getInstance(this, width, canvasHeight);
        broadcaster = new UdpBroadcaster(this);
        UdpReceiver receiver = new UdpReceiver(this);
        DrawRobot drawRobot = new DrawRobot(this);

        //Initialise and add the bottom panels.
        try
        {
            colourPanel = ColourPanel.getInstance();
            colourPanel.setBounds(793, 5, 200, 200);
            palletPanel.add(colourPanel);
        } catch (IOException | AWTException e)
        {
            e.printStackTrace();
        }
        selectPanel = SelectPanel.getInstance(this);
        selectPanel.setBounds(0, 5, 500, 200);
        palletPanel.add(selectPanel);

        peerCount = new JLabel("Peer count: 0");
        peerCount.setBounds(580, 5, 100, 30);
        palletPanel.add(peerCount);

        historySize = new JLabel("History Size: 0");
        historySize.setBounds(580, 35, 100, 30);
        palletPanel.add(historySize);

        frame.getContentPane().add(palletPanel, BorderLayout.SOUTH);

        frame.pack();
        // Center the frame on the screen.
        frame.setLocationRelativeTo(null);

        //Initialise the history cache.
        actionCache = ActionCache.getInstance();

        frame.setVisible(true);

        this.requestFocus();
        this.createBufferStrategy(2);
        bs = this.getBufferStrategy();

        //Create and start all required threads.
        final Thread broadcastThread = new Thread(broadcaster);
        Thread receiveThread = new Thread(receiver);
        Thread drawThread = new Thread(draw);
        Thread robotThread = new Thread(drawRobot);
        drawThread.start();
        receiveThread.start();
        broadcastThread.start();
        robotThread.start();

        final String allowedExtensions = "png jpeg jpg bmp";
        imageCache = new ImageCache(this);
        new FileDrop(this, new FileDrop.Listener()
        {

            /**
             * This method is called when files have been successfully dropped.
             *
             * @param files An array of <tt>File</tt>s that were dropped.
             * @since 1.0
             */
            @Override
            public void filesDropped(File[] files)
            {
                for (File file : files)
                {
                    if (allowedExtensions.contains(file.getName().split("\\.")[1]))
                    {
                        try
                        {
                            BufferedImage image = ImageIO.read(file);
                            imageCount++;
                            imageCache.put(imageCount, image);

                            Point p = Frame.this.getMousePosition();
                            int x = p.x - image.getWidth(null) / 2;
                            int y = p.y - image.getHeight(null) / 2;
                            int[] ip = getMyIp();
                            int[] imageData = {getId(), Draw.IMAGE, x, y, imageCount, ip[0], ip[1], ip[2], ip[3]};
                            draw.put(imageData);
                            broadcaster.put(imageData);
                        } catch (IOException | InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    } else
                    {
                        System.out.println("Extension " + file.getName().split("\\.")[1] + " failed");
                    }
                }
            }
        });
    }

    /**
     * Sends out a peer ip request.
     * Any connected peers will reply
     */
    void findPeers()
    {
        try
        {
            int[] requestPeers = {getId(), REQ_IP, myIp[0], myIp[1], myIp[2], myIp[3], actionCache.getSize()};
            broadcaster.put(requestPeers);
        } catch (InterruptedException e)
        {
            System.out.println("Request IP issue.");
            e.printStackTrace();
        }
    }

    /**
     * Invoked when a key has been released.
     * See the class description for {@link KeyEvent} for a definition of
     * a key released event.
     *
     * @param e The event which indicates that a keystroke occurred.
     */
    @Override
    public void keyReleased(KeyEvent e)
    {
        if (selectPanel.isLine())
        {
            switch (e.getKeyCode())
            {
                case KeyEvent.VK_C:
                    try
                    {
                        // No need for the rest of the fields when doing a clear...
                        int[] data = {getId(), Draw.CLEAR};
                        draw.put(data);

                        //Send a clear request to peers.
                        int[] req = {getId(), CLEAR_REQ};
                        broadcaster.put(req);
                        clearAll();
                    } catch (InterruptedException e1)
                    {
                        e1.printStackTrace();
                    }
                    break;
                case 38:
                    selectPanel.setThickSlider(selectPanel.getThickValue() + 1);
                    break;
                case 40:
                    selectPanel.setThickSlider(selectPanel.getThickValue() - 1);
                    break;
                default:
                    System.out.printf("KeyCode %d is not bound.\n", e.getKeyCode());
            }
        } else
        {
            //text options.
            if (start != null)
            {
                try
                {
                    //These assignments are only to shorten the data line.
                    char c = e.getKeyChar();
                    int x = start.x + draw.getTextOffset();
                    int colour = colourPanel.getDrawColour().getRGB();
                    int[] data = {getId(), Draw.TEXT, x, start.y, (int) c, colour, selectPanel.getThickValue()};
                    draw.put(data);
                    broadcaster.put(data);
                } catch (InterruptedException e1)
                {
                    e1.printStackTrace();
                }
            }
        }
    }

    public void clearAll()
    {
        actionCache.clear();
        imageCount = 0;
        imageCache.clear();
        historySize.setText("History size: 0");
    }

    /**
     * Get this peer's id.
     *
     * @return This peers id.
     */
    public int getId()
    {
        //return 21;
        return myIp[3];
    }

    public int[] getMyIp()
    {
        return myIp;
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param e The event which indicates that a keystroke occurred.
     */
    @Override
    public void mousePressed(MouseEvent e)
    {
        if (SwingUtilities.isLeftMouseButton(e) || SwingUtilities.isRightMouseButton(e))
        {
            start = e.getPoint();
            if (!selectPanel.isLine())
            {
                draw.resetTextOffset();
            }
        }
    }

    /**
     * Invoked when a mouse button is pressed on a component and then
     * dragged.  <code>MOUSE_DRAGGED</code> events will continue to be
     * delivered to the component where the drag originated until the
     * mouse button is released (regardless of whether the mouse position
     * is within the bounds of the component).
     * <p>
     * Due to platform-dependent Drag&amp;Drop implementations,
     * <code>MOUSE_DRAGGED</code> events may not be delivered during a native
     * Drag&amp;Drop operation.
     *
     * @param e The event which indicates that a keystroke occurred.
     */
    @Override
    public void mouseDragged(MouseEvent e)
    {
        boolean left = SwingUtilities.isLeftMouseButton(e);
        boolean right = SwingUtilities.isRightMouseButton(e);

        //Check if either button is pressed.
        if ((left || right) && selectPanel.isLine())
        {
            //Default (both button) stuff.
            end = e.getPoint();
            Color myColour;

            //Button specific stuff.
            if (left)
            {
                // Drawing.
                myColour = colourPanel.getDrawColour();
            } else
            {
                // Erasing.
                myColour = ERASE;
            }

            //More default (both button) stuff.
            /*
             * Layout for the data packet.  Sent as an int array.
             *
             * 0:   Machine id          Must be different for each pc. **Not happy with this system atm**
             * 1:   Instruction type    0 for draw, 1 for clear, could be more for text and shapes.
             * 2:   Start x             x component start of the line.
             * 3:   Start y             y component start of the line.
             * 4:   End x               x component end of the line.
             * 5:   End y               y component end of the line.
             * 6:   Colour              The colour to be used.
             * 7:   Line Thickness       The line thickness to be used.
             */
            int[] data = {getId(), Draw.DRAW, start.x, start.y, end.x, end.y, myColour.getRGB(), selectPanel.getThickValue()};

            try
            {
                broadcaster.put(data);
                draw.put(data);
            } catch (InterruptedException intEx)
            {
                System.out.println("Exception in broadcast/draw put.\n" + intEx.getMessage());
            }

            //The end point becomes the next start point.
            start = end;
        }
    }

    //<editor-fold desc="Accessor methods.">

    /**
     * Get the SelectPanel object.
     *
     * @return This Frame's SelectPanel.
     * @see SelectPanel
     */
    public SelectPanel getSelectPanel()
    {
        return selectPanel;
    }

    /**
     * Get the PeerCache object.
     *
     * @return This Frame's PeerCache.
     * @see PeerCache
     */
    public PeerCache getPeerCache()
    {
        return peerCache;
    }

    /**
     * Get the Draw object.
     *
     * @return This frame's Draw.
     * @see Draw
     */
    public Draw getDraw()
    {
        return draw;
    }

    /**
     * Get the ImageCache object.
     *
     * @return This frame's ImageCache.
     */
    public ImageCache getImageCache()
    {
        return imageCache;
    }

    /**
     * Get the width of the drawing canvas.
     *
     * @return The width of the drawing canvas.
     */
    @Override
    public int getWidth()
    {
        return width;
    }

    /**
     * Get the height of the drawing canvas.
     *
     * @return The height of the drawing canvas.
     */
    public int getCanvasHeight()
    {
        return canvasHeight;
    }

    /**
     * Get the UdpBroadcaster object.
     *
     * @return This frame's UdpBroadcaster.
     * @see UdpBroadcaster
     */
    public UdpBroadcaster getBroadcaster()
    {
        return broadcaster;
    }

    /**
     * Get the ActionCache object.
     *
     * @return This frame's ActionCache.
     * @see ActionCache
     */
    public ActionCache getActionCache()
    {
        return actionCache;
    }

    /**
     * Get the peer count JLabel.
     *
     * @return This frame's peer count JLabel.
     */
    public synchronized JLabel getPeerCount()
    {
        return peerCount;
    }

    /**
     * Get the history size JLabel.
     *
     * @return This frame's history size JLabel.
     */
    public synchronized JLabel getHistorySize()
    {
        return historySize;
    }

    /**
     * Get this exiting status of the program.
     *
     * @return True if currently exiting. False otherwise.
     */
    public boolean isExiting()
    {
        return exiting;
    }

    /**
     * Get the BufferStrategy object for this Frame.
     *
     * @return This Frames BufferStrategy.
     */
    public synchronized BufferStrategy getBs()
    {
        return bs;
    }
    //</editor-fold>

    //<editor-fold desc="Unused but mandatory MouseEvent stuff">

    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     *
     * @param e The mouse event.
     */
    @Override
    public void mouseClicked(MouseEvent e)
    {

    }

    /**
     * Invoked when a mouse button has been released on a component.
     *
     * @param e The mouse event.
     */
    @Override
    public void mouseReleased(MouseEvent e)
    {
    }

    /**
     * Invoked when the mouse enters a component.
     *
     * @param e The mouse event.
     */
    @Override
    public void mouseEntered(MouseEvent e)
    {
    }

    /**
     * Invoked when the mouse exits a component.
     *
     * @param e The mouse event.
     */
    @Override
    public void mouseExited(MouseEvent e)
    {
    }

    /**
     * Invoked when the mouse cursor has been moved onto a component
     * but no buttons have been pushed.
     *
     * @param e The mouse event.
     */
    @Override
    public void mouseMoved(MouseEvent e)
    {
        //TODO -Me Set a different cursor based on what the user is doing.
    }

    /**
     * Invoked when the mouse wheel is rotated.
     *
     * @param e The mouse event.
     * @see MouseWheelEvent
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        //System.out.println("Mouse wheel: " + e);
    }
    //</editor-fold>

    //<editor-fold desc="Unused but mandatory KeyEvent stuff">

    /**
     * Invoked when a key has been typed.
     * See the class description for {@link KeyEvent} for a definition of
     * a key typed event.
     *
     * @param e The event which indicates that a keystroke occurred.
     */
    @Override
    public void keyTyped(KeyEvent e)
    {

    }

    /**
     * Invoked when a key has been pressed.
     * See the class description for {@link KeyEvent} for a definition of
     * a key pressed event.
     *
     * @param e The event which indicates that a keystroke occurred.
     */
    @Override
    public void keyPressed(KeyEvent e)
    {

    }
    //</editor-fold>
}
