/**
 * Peer to peer whiteboard
 * Distributed computing.
 * CMPSMC34
 */

package com.twizted.Network;

import com.twizted.Display.Frame;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * @author Ian Weeks 6204848
 *         <p>
 *         UdpServer class.
 *         <p>
 *         Handles receipt of all direct udp packets.
 */
public class UdpServer implements Runnable
{
    private DatagramSocket datagramSocket;
    private byte[] receiveData;
    private int size;
    private Frame frame;

    /**
     * UdpServer constructor.
     *
     * @param frame A reference to the parent frame.
     * @param size  How many packets are expected.
     * @throws SocketException If the socket could not be opened,
     *                         or the socket could not bind to the specified local port.
     */
    public UdpServer(Frame frame, int size) throws SocketException
    {
        //Overkill on the array size but just in case...
        int port = 55558, defaultByteSize = 1024;
        this.frame = frame;
        datagramSocket = new DatagramSocket(port);
        receiveData = new byte[defaultByteSize];
        this.size = size;
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
            //For each expected packet...
            for (int i = 0; i < size; i++)
            {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                //Five second timeout.
                datagramSocket.setSoTimeout(5000);
                try
                {
                    datagramSocket.receive(receivePacket);
                    byte[] b = receivePacket.getData();

                    ByteArrayInputStream bis = new ByteArrayInputStream(b);
                    ObjectInput in = new ObjectInputStream(bis);

                    int[] data = (int[]) in.readObject();

                    frame.getDraw().put(data);
                } catch (SocketTimeoutException ste)
                {
                    //No replies. Break and continue.
                    System.out.println("No history received.");
                    break;
                }
            }
            datagramSocket.close();
            System.out.println("History receive complete");
        } catch (IOException | ClassNotFoundException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
