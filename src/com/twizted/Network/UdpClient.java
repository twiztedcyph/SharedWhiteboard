/**
 * Peer to peer whiteboard
 * Distributed computing.
 * CMPSMC34
 */

package com.twizted.Network;

import com.twizted.Cache.ActionCache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * @author Ian Weeks 6204848
 *         <p>
 *         UdpClient Class.
 *         <p>
 *         Handles sending of history. Direct (non multicast) udp packets.
 */
class UdpClient implements Runnable
{
    private String address;
    private ActionCache actionCache;

    /**
     * UdpClient constructor.
     *
     * @param address     The recipient's ip address.
     * @param actionCache A reference to the ActionCache.
     */
    UdpClient(String address, ActionCache actionCache)
    {
        this.address = address;
        this.actionCache = actionCache;
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
        try
        {
            InetAddress inetAddress = InetAddress.getByName(address);
            //For each entry in the history.
            for (int[] data : actionCache)
            {
                DatagramPacket packet;
                //Try with resources to auto close closeable objects.
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     ObjectOutputStream oos = new ObjectOutputStream(baos);
                     DatagramSocket udpSocket = new DatagramSocket())
                {
                    oos.writeObject(data);
                    byte[] sendData = baos.toByteArray();

                    // Create the packet to be sent.
                    packet = new DatagramPacket(sendData, sendData.length);
                    packet.setAddress(inetAddress);
                    packet.setPort(55558);

                    // Send the packet.
                    udpSocket.send(packet);
                    System.out.println("History packet sent.");
                }
            }
            System.out.println("History send complete.");
        } catch (IOException e)
        {
            System.out.println("Error in history send");
            System.out.println(e.getMessage());
        }
    }
}
