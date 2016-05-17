/**
 * Peer to peer whiteboard
 * Distributed computing.
 * CMPSMC34
 */

package com.twizted.Display;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author Ian Weeks 6204848
 *         <p>
 *         ColourPanel class.
 *         <p>
 *         Handles the user selection of draw and text colours
 *         This is a singleton class.
 */
public class ColourPanel extends JPanel implements MouseListener
{
    private static ColourPanel instance;
    private BufferedImage colourPallet;
    private Robot robot;
    private Color drawColour;

    /**
     * Creates a new <code>JPanel</code> with a double buffer
     * and a flow layout.
     */
    private ColourPanel() throws IOException, AWTException
    {
        //Load the colour wheel.
        colourPallet = ImageIO.read(new File("colourWheel.png"));

        this.setPreferredSize(new Dimension(100, 200));
        this.setBackground(Color.GREEN);

        this.addMouseListener(this);

        //Set default draw colour.
        drawColour = Color.black;
        this.setBackground(drawColour);

        //Constructs a Robot object in the coordinate system of the primary screen.
        robot = new Robot();

        JLabel label = new JLabel("Draw Colour");
        this.add(label);
    }

    /**
     * Get the ColourPanel instance.
     *
     * @return The ColourPanel instance.
     * @throws IOException  If the colour wheel graphic cannot be read.
     * @throws AWTException If the platform configuration does not allow low-level input control.
     */
    public static ColourPanel getInstance() throws IOException, AWTException
    {
        if (instance == null)
        {
            instance = new ColourPanel();
        }
        return instance;
    }

    public Color getDrawColour()
    {
        return drawColour;
    }

    /**
     * Calls the UI delegate's paint method, if the UI delegate
     * is non-<code>null</code>.  We pass the delegate a copy of the
     * <code>Graphics</code> object to protect the rest of the
     * paint code from irrevocable changes
     * (for example, <code>Graphics.translate</code>).
     * <p>
     * If you override this in a subclass you should not make permanent
     * changes to the passed in <code>Graphics</code>. For example, you
     * should not alter the clip <code>Rectangle</code> or modify the
     * transform. If you need to do these operations you may find it
     * easier to create a new <code>Graphics</code> from the passed in
     * <code>Graphics</code> and manipulate it. Further, if you do not
     * invoker super's implementation you must honor the opaque property,
     * that is
     * if this component is opaque, you must completely fill in the background
     * in a non-opaque color. If you do not honor the opaque property you
     * will likely see visual artifacts.
     * <p>
     * The passed in <code>Graphics</code> object might
     * have a transform other than the identify transform
     * installed on it.  In this case, you might get
     * unexpected results if you cumulatively apply
     * another transform.
     *
     * @param g the <code>Graphics</code> object to protect
     * @see #paint
     * @see ComponentUI
     */
    @Override
    protected void paintComponent(Graphics g)
    {
        //Paint the colour wheel.
        super.paintComponent(g);
        g.drawImage(colourPallet.getScaledInstance(200, 200, Image.SCALE_SMOOTH), 0, 0, 200, 200, null);
    }

    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     *
     * @param e The mouse event
     */
    @Override
    public void mouseClicked(MouseEvent e)
    {
        //Get the pixel colour at the mouse cursor on click.
        Point p = MouseInfo.getPointerInfo().getLocation();
        drawColour = robot.getPixelColor((int) p.getX(), (int) p.getY());
        //Set the clicked colour to the current colour.
        this.setBackground(drawColour);
    }

    //<editor-fold desc="Unused but mandatory mouse event stuff">

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param e The mouse event
     */
    @Override
    public void mousePressed(MouseEvent e)
    {

    }

    /**
     * Invoked when a mouse button has been released on a component.
     *
     * @param e The mouse event
     */
    @Override
    public void mouseReleased(MouseEvent e)
    {
        //Unused.
    }

    /**
     * Invoked when the mouse enters a component.
     *
     * @param e The mouse event
     */
    @Override
    public void mouseEntered(MouseEvent e)
    {
        //Unused.
    }

    /**
     * Invoked when the mouse exits a component.
     *
     * @param e The mouse event
     */
    @Override
    public void mouseExited(MouseEvent e)
    {
        //Unused.
    }
    //</editor-fold>
}
