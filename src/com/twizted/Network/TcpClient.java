/**
 * Peer to peer whiteboard
 * Distributed computing.
 * CMPSMC34
 */

package com.twizted.Network;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * @author Ian Weeks 6204848
 *         <p>
 *         TcpClient class.
 *         <p>
 *         Handles sending of images using TCP packets.
 */
class TcpClient implements Runnable
{
    private String recipientIP;
    private BufferedImage image;

    /**
     * TcpClient constructor.
     *
     * @param recipientIP The recipient's ip address.
     * @param image       The image to be sent.
     */
    TcpClient(String recipientIP, BufferedImage image)
    {
        this.recipientIP = recipientIP;
        this.image = image;
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
        int port = 55550;
        //Try with resources to auto close socket and streams.
        try (Socket socket = new Socket(recipientIP, port);
             OutputStream outStream = socket.getOutputStream();
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream())
        {
            //Write the image to a byte array and send the array size and the image bytes.
            //Sending images as objects will not work.
            ImageIO.write(image, "jpg", byteArrayOutputStream);
            byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
            outStream.write(size);
            outStream.write(byteArrayOutputStream.toByteArray());
            outStream.flush();
            System.out.println("Sent image to " + recipientIP + " complete");
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
