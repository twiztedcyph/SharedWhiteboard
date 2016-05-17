/**
 * Peer to peer whiteboard
 * Distributed computing.
 * CMPSMC34
 */

package com.twizted.Network;

import com.twizted.Display.Frame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Ian Weeks 6204848
 *         <p>
 *         UdpBroadcaster
 *         <p>
 *         Handles all multicast broadcasting.
 */
public class UdpBroadcaster implements Runnable
{
    private volatile BlockingQueue<int[]> queue;
    private boolean broadcastOn;
    private InetAddress serverAddress;
    private Frame frame;

    /**
     * UdpBroadcaster constructor.
     *
     * @param frame Reference to parent frame.
     * @throws UnknownHostException If no IP address for the
     *                              {@code generalIP} could be found, or if a scope_id was specified
     *                              for a global IPv6 address.
     */
    public UdpBroadcaster(Frame frame) throws UnknownHostException
    {
        this.frame = frame;
        queue = new LinkedBlockingQueue<>();
        broadcastOn = true;
        String generalIP = "224.0.159.82";
        serverAddress = InetAddress.getByName(generalIP);
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
        //Broadcaster on constant loop.
        while (broadcastOn)
        {
            try
            {
                DatagramPacket packet;
                /*
                 * Because this is a blocking queue,
                 * the program will wait here while
                 * the queue is empty therefore not
                 * wasting CPU cycles.
                 *
                 * If the queue is not empty AND the
                 * network flag is on then send the
                 * packet.
                 *
                 * If the network flag is off then
                 * do nothing and loop again.
                 */
                int[] data = queue.take();

                if (frame.getSelectPanel().network() || frame.isExiting())
                {
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                         ObjectOutputStream oos = new ObjectOutputStream(baos);
                         DatagramSocket udpSocket = new DatagramSocket())
                    {
                        // Create byte array of data.
                        oos.writeObject(data);
                        byte[] sendData = baos.toByteArray();
                        System.out.println("Send: " + sendData.length);

                        // Create the packet to be sent.
                        packet = new DatagramPacket(sendData, sendData.length);
                        packet.setAddress(serverAddress);
                        packet.setPort(55559);

                        // Send the packet.
                        udpSocket.send(packet);
                        System.out.println("Packet sent");
                    }
                }
            } catch (IOException | InterruptedException e)
            {
                e.printStackTrace();
            }
        }

    }

    /**
     * Add a data line to the broadcast queue
     *
     * @param data Data line to be added.
     * @throws InterruptedException If interrupted while waiting
     */
    public synchronized void put(int[] data) throws InterruptedException
    {
        queue.put(data);
    }
}
