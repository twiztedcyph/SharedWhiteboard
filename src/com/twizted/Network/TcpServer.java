/**
 * Peer to peer whiteboard
 * Distributed computing.
 * CMPSMC34
 */

package com.twizted.Network;


import com.twizted.Cache.ImageCache;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Ian Weeks 6204848
 * <p>
 * TcpServer class
 * <p>
 * Handles receipt of images using TCP.
 */
public class TcpServer implements Runnable
{
    private ImageCache imageCache;
    private int key;

    /**
     * TcpServer constructor.
     *
     * @param imageCache A reference to the image cache.
     * @param key        The key to be used for this image.
     */
    public TcpServer(ImageCache imageCache, int key)
    {
        this.imageCache = imageCache;
        this.key = key;
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
        System.out.println("Image receiver waiting");
        int port = 55550;
        //Try with resources to auto close socket and streams.
        try (ServerSocket serverSocket = new ServerSocket(port);
             Socket socket = serverSocket.accept();
             InputStream inputStream = socket.getInputStream())
        {
            //Init the array size array.
            byte[] arraySize = new byte[4];


            //Read the array size.
            //noinspection ResultOfMethodCallIgnored
            inputStream.read(arraySize);
            //Get the size of the image.
            int imageSize = ByteBuffer.wrap(arraySize).asIntBuffer().get();

            //Create the exact sized image array.
            byte[] imageArray = new byte[imageSize];

            //Read the image bytes to the image array.
            //noinspection ResultOfMethodCallIgnored
            inputStream.read(imageArray);

            //Create the image from the image bytes.
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageArray));

            //Put the image into the cache.
            imageCache.put(key, image);
            System.out.println("Image receiver complete.");
        } catch (IOException e)
        {
            System.out.println("Error in image receiver.");
            System.out.println(e.getMessage());
        }
    }
}
