/**
 * Peer to peer whiteboard
 * Distributed computing.
 * CMPSMC34
 */

package com.twizted.Network;

import com.twizted.Display.Frame;
import com.twizted.Utility.Draw;
import com.twizted.Utility.Triple;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

/**
 * @author Ian Weeks 6204848
 *         <p>
 *         UdpReceiver class
 *         <p>
 *         Handles reception of multicast packets.
 */
public class UdpReceiver implements Runnable
{
    @SuppressWarnings("WeakerAccess")
    public static final int REQ_IP = 5, ANS_IP = 6, LEAVE_NOTE = 7, REQ_HISTORY = 8, CLEAR_REQ = 9, IMAGE_REQ = 10;
    private byte[] receiveData;
    private boolean receiverOn;
    private Frame frame;

    /**
     * UdpReceiver constructor.
     *
     * @param frame A reference to the parent frame.
     */
    public UdpReceiver(Frame frame)
    {
        receiveData = new byte[1024];
        receiverOn = true;
        this.frame = frame;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>receiverOn</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>receiverOn</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run()
    {
        try
        {
            MulticastSocket multicastSocket = new MulticastSocket(55559);
            //My chosen multicast ip.
            String multicastIP = "224.0.159.82";
            multicastSocket.joinGroup(InetAddress.getByName(multicastIP));
            //Receiver on continuous loop.
            while (receiverOn)
            {
                //Check network flag.
                if (frame.getSelectPanel().network())
                {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    multicastSocket.receive(receivePacket);
                    byte[] b = receivePacket.getData();
                    System.out.println("Receive: " + b.length);
                    ByteArrayInputStream bis = new ByteArrayInputStream(b);
                    ObjectInput in = new ObjectInputStream(bis);

                    try
                    {
                        int[] data = (int[]) in.readObject();
                        System.out.println("This: " + frame.getId() + "\tFrom: " + data[0]);
                        //Exclude messages that originated at this peer.
                        if (data[0] != frame.getId())
                        {
                            //Action based on int at data[1]
                            switch (data[1])
                            {
                                case Draw.CLEAR:
                                case Draw.DRAW:
                                case Draw.TEXT:
                                case Draw.CIRCLE:
                                case Draw.IMAGE:
                                    frame.getDraw().put(data);
                                    break;
                                case REQ_IP:
                                    int key = data[0];
                                    String ip = String.format("%d.%d.%d.%d", data[2], data[3], data[4], data[5]);
                                    frame.getPeerCache().addPeer(new Triple<>(key, ip, data[6]));
                                    int size = frame.getPeerCache().getSize();
                                    frame.getPeerCount().setText(String.format("Peer count: %d", size));
                                    int[] myIp = frame.getMyIp();
                                    size = frame.getActionCache().getSize();
                                    int[] ipAns = {frame.getId(), ANS_IP, myIp[0], myIp[1], myIp[2], myIp[3], size};
                                    frame.getBroadcaster().put(ipAns);
                                    break;
                                case ANS_IP:
                                    /*
                                     * 0 = id
                                     * 1 = data type
                                     * 2,3,4,6 = IP
                                     */
                                    key = data[0];
                                    ip = String.format("%d.%d.%d.%d", data[2], data[3], data[4], data[5]);
                                    frame.getPeerCache().addPeer(new Triple<>(key, ip, data[6]));
                                    frame.getPeerCount().setText(
                                            String.format("Peer count: %d", frame.getPeerCache().getSize()));
                                    break;
                                case LEAVE_NOTE:
                                    ip = String.format("%d.%d.%d.%d", data[2], data[3], data[4], data[5]);
                                    frame.getPeerCache().removePeer(ip);
                                    frame.getPeerCount().setText(
                                            String.format("Peer count: %d", frame.getPeerCache().getSize()));
                                    break;
                                case REQ_HISTORY:
                                    /*
                                     * 0 = id
                                     * 1 = data type
                                     * 2 = tarID
                                     * 3,4,5,6 = IP
                                     */
                                    if (data[2] == frame.getId())
                                    {
                                        ip = String.format("%d.%d.%d.%d", data[3], data[4], data[5], data[6]);
                                        System.out.println("History request from " + ip);
                                        UdpClient historyClient = new UdpClient(ip, frame.getActionCache());
                                        Thread historyThread = new Thread(historyClient);
                                        historyThread.start();
                                    }
                                    break;
                                case CLEAR_REQ:
                                    String reqTitle = String.format("Peer %d has requested a clear.", data[1]);
                                    int dialogResult = JOptionPane.showConfirmDialog(frame, "Would you like to clear?",
                                                                                     reqTitle,
                                                                                     JOptionPane.YES_NO_OPTION);
                                    if (dialogResult == JOptionPane.YES_OPTION)
                                    {
                                        int[] clear = {frame.getId(), Draw.CLEAR};
                                        frame.getDraw().put(clear);
                                        frame.clearAll();
                                    }
                                    break;
                                case IMAGE_REQ:
                                    System.out.print("IMAGE REQUEST:   ");
                                    System.out.println(data[2] + " vs " +  frame.getId());
                                    if (data[2] == frame.getId())
                                    {
                                        ip = String.format("%d.%d.%d.%d", data[4], data[5], data[6], data[7]);
                                        System.out.println("Image request from " + ip);
                                        BufferedImage image = frame.getImageCache().get(data[3], data[7]);

                                        TcpClient tcpClient = new TcpClient(ip, image);
                                        Thread tcpThread = new Thread(tcpClient);
                                        tcpThread.start();
                                    }
                                    break;
                                default:
                                    //Rogue packet protection.
                                    System.out.println("Received invalid packet.");
                                    System.out.println(Arrays.toString(data));
                            }
                        }
                    } catch (ClassNotFoundException | IndexOutOfBoundsException e)
                    {
                        //Rogue packet protection.
                        //Message given, loop continues.
                        System.out.println("Exception: Received invalid packet.");
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException | InterruptedException e)
        {
            System.err.println("Error in Receiver class.");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
