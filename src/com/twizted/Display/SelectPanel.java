/**
 * Peer to peer whiteboard
 * Distributed computing.
 * CMPSMC34
 */

package com.twizted.Display;

import com.twizted.Cache.PeerCache;
import com.twizted.Network.UdpReceiver;
import com.twizted.Network.UdpServer;
import com.twizted.Utility.Draw;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.SocketException;
import java.util.Hashtable;

import static com.twizted.Network.UdpReceiver.REQ_HISTORY;

/**
 * @author Ian Weeks 6204848
 *
 *         SelectPanel class.
 *
 *         Handles user selected options. E.g Text, Lines, Draw size etc.
 *         This is a singleton class.
 */
public class SelectPanel extends JPanel
{
    private static SelectPanel instance;
    private JSlider thickSlider;
    private JToggleButton connect;
    private JToggleButton disconnect;
    private JToggleButton line;
    private JToggleButton text;
    private volatile boolean networkOn;
    private JLabel thickLabel;
    private boolean robot, replaying;

    /**
     * Creates a new <code>JPanel</code> with a double buffer
     * and a flow layout.
     *
     * @param frame Reference to parent frame.
     */
    @SuppressWarnings("Duplicates")
    private SelectPanel(final Frame frame)
    {
        //Explicit positioning of components.
        this.setLayout(null);
        this.setBackground(Color.LIGHT_GRAY);

        //Buttons.
        connect = new JToggleButton("Connect");
        connect.setBounds(60, 50, 100, 25);
        connect.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                if (e.getStateChange() == ItemEvent.SELECTED)
                {
                    try
                    {
                        //Set network flag true.
                        networkOn = true;
                        disconnect.setSelected(false);
                        //Check for peers.
                        frame.findPeers();
                        frame.getImageCache().clear();
                        //Small pause to ensure peer search is complete.
                        Thread.sleep(500);
                        //Get the peer cache and longest history.
                        PeerCache pc = frame.getPeerCache();
                        if (pc.getLongestHistory() != null)
                        {
                            int[] myIp = frame.getMyIp();
                            int targetID = pc.getLongestHistory().getKey();
                            int[] data = {frame.getId(), REQ_HISTORY, targetID, myIp[0], myIp[1], myIp[2], myIp[3]};

                            //Initialise the history listener.
                            UdpServer historyServer = new UdpServer(frame, pc.getLongestHistory().getValueTwo());
                            Thread historyThread = new Thread(historyServer);
                            historyThread.start();

                            //Send the history request.
                            frame.getBroadcaster().put(data);

                            //Wait for this thread to finish.
                            historyThread.join();
                        }
                    } catch (InterruptedException | SocketException e1)
                    {
                        e1.printStackTrace();
                    }

                } else if (e.getStateChange() == ItemEvent.DESELECTED)
                {
                    disconnect.setSelected(true);
                }
            }
        });
        this.add(connect);

        disconnect = new JToggleButton("Disconnect");
        disconnect.setSelected(true);
        disconnect.setBounds(60, 100, 100, 25);
        disconnect.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                if (e.getStateChange() == ItemEvent.SELECTED)
                {
                    try
                    {
                        //Send disconnecting message and reset peer size label.
                        int[] myIp = frame.getMyIp();
                        int[] leaveData = {frame.getId(), UdpReceiver.LEAVE_NOTE, myIp[0], myIp[1], myIp[2], myIp[3]};
                        frame.getActionCache().clear();
                        frame.getHistorySize().setText(
                                String.format("History size: %d", frame.getActionCache().getSize()));
                        frame.getBroadcaster().put(leaveData);
                        //Small pause to ensure disconnect message is sent.
                        Thread.sleep(200);
                    } catch (InterruptedException ex)
                    {
                        ex.printStackTrace();
                    }
                    //Set network flag false.
                    networkOn = false;
                    connect.setSelected(false);
                    frame.getPeerCache().resetPeers();
                    frame.getPeerCount().setText("Peer count: 0");
                } else if (e.getStateChange() == ItemEvent.DESELECTED)
                {
                    connect.setSelected(true);
                }
            }
        });
        this.add(disconnect);

        line = new JToggleButton("Line");
        line.setSelected(true);
        line.setBounds(200, 50, 100, 25);
        line.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                if (e.getStateChange() == ItemEvent.SELECTED)
                {
                    //Line draw selected.
                    text.setSelected(false);
                    thickLabel.setText("Line Thickness");
                } else
                {
                    text.setSelected(true);
                }
            }
        });
        this.add(line);

        text = new JToggleButton("Text");
        text.setBounds(200, 100, 100, 25);
        frame.getDraw().resetTextOffset();
        text.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                if (e.getStateChange() == ItemEvent.SELECTED)
                {
                    //Text drawing selected.
                    line.setSelected(false);
                    thickLabel.setText("      Text size");
                } else
                {
                    line.setSelected(true);
                }
            }
        });
        this.add(text);

        //Robot toggle button.
        JToggleButton robotButton = new JToggleButton("Robot");
        robotButton.setBounds(60, 150, 100, 25);
        robotButton.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                robot = e.getStateChange() == ItemEvent.SELECTED;
            }
        });
        this.add(robotButton);

        final JButton replay = new JButton("Slow replay");
        replay.setBounds(200, 150, 100, 25);
        replay.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                System.out.println("Clicked");
                replaying = true;
                Thread t = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            int[] clear = {frame.getId(), Draw.CLEAR};
                            frame.getDraw().put(clear);

                            for (int[] data : frame.getActionCache())
                            {
                                frame.getDraw().put(data);

                                Thread.sleep(10);
                            }
                        } catch (InterruptedException e1)
                        {
                            e1.printStackTrace();
                        }
                        replaying = false;
                    }
                });
                t.start();
            }
        });
        this.add(replay);


        thickLabel = new JLabel("Line Thickness");
        thickLabel.setBounds(390, 0, 100, 20);
        this.add(thickLabel);

        //Line and text size slider.
        thickSlider = new JSlider(JSlider.VERTICAL, 1, 10, 1);
        thickSlider.setBackground(Color.LIGHT_GRAY);
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(1, new JLabel("Small"));
        labelTable.put(10, new JLabel("Big"));
        thickSlider.setLabelTable(labelTable);
        thickSlider.setPaintLabels(true);
        thickSlider.setBounds(413, 25, 80, 170);
        this.add(thickSlider);
    }

    /**
     * Get the SelectPanel instance.
     *
     * @param frame Reference to parent frame.
     * @return The SelectPanel instance.
     */
    static SelectPanel getInstance(Frame frame)
    {
        if (instance == null)
        {
            instance = new SelectPanel(frame);
        }
        return instance;
    }

    /**
     * Get the current value of the thickness slider.
     *
     * @return The current value of the thickness slider.
     */
    int getThickValue()
    {
        return thickSlider.getValue();
    }

    /**
     * Set the value of the thickness slider.
     *
     * @param i The new thickness value.
     */
    void setThickSlider(int i)
    {
        //Quick check... shouldn't be needed but meh...
        if (i > 0 && i < 11)
        {
            thickSlider.setValue(i);
        }
    }

    /**
     * Get the line flag.
     * <p/>
     *
     * @return True if line selected. False otherwise.
     */
    boolean isLine()
    {
        return line.isSelected();
    }

    /**
     * Get the network flag.
     *
     * @return True if connected. False otherwise.
     */
    public boolean network()
    {
        return networkOn;
    }

    /**
     * Get the replaying flag.
     *
     * @return The replaying flag.
     */
    public boolean isReplaying()
    {
        return replaying;
    }

    /**
     * Get the robot flag.
     *
     * @return True if robot is selected. False otherwise.
     */
    public boolean isRobot()
    {
        return robot;
    }
}
