/**
 * Peer to peer whiteboard
 * Distributed computing.
 * CMPSMC34
 */

package com.twizted.Cache;

import com.twizted.Display.Frame;
import com.twizted.Network.TcpServer;
import com.twizted.Network.UdpReceiver;

import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * @author Ian Weeks 6204848
 *         <p>
 *         ImageCache class.
 *         <p>
 *         Handles storage of images
 */
public class ImageCache
{
    private Frame frame;
    //HashMap for constant time access.
    private HashMap<Integer, BufferedImage> hashMap;

    /**
     * ImageCache constructor.
     *
     * @param frame A reference to the parent frame.
     */
    public ImageCache(Frame frame)
    {
        this.frame = frame;
        hashMap = new HashMap<>();
    }

    /**
     * Add an image record to the cache.
     *
     * @param key   The key to be added.
     * @param image The image to be added.
     */
    public void put(Integer key, BufferedImage image)
    {
        hashMap.put(key, image);
    }

    /**
     * Get an image from the cache.  If the image requested is not found,
     * it will be requested from the originating peer.
     *
     * @param key      The required image key.
     * @param targetID The originator's id.
     * @return The requested image.
     * @throws InterruptedException If the thread is interrupted before finishing.
     */
    public BufferedImage get(Integer key, int targetID) throws InterruptedException
    {
        //First try to fetch the image from the local cache.
        BufferedImage result = hashMap.get(key);
        if (result != null)
        {
            //If found then return it.
            return result;
        }

        //If not found then initialise the listener
        TcpServer tcpServer = new TcpServer(this, key);
        Thread tcpThread = new Thread(tcpServer);
        tcpThread.start();

        //Send image request to original peer.
        int[] myIp = frame.getMyIp();
        int[] req = {frame.getId(), UdpReceiver.IMAGE_REQ, targetID, key, myIp[0], myIp[1], myIp[2], myIp[3]};
        frame.getBroadcaster().put(req);
        //Wait for the image to be received.
        tcpThread.join();
        System.out.println("image cache thread complete");

        //Return the image.
        return hashMap.get(key);
    }

    /**
     * Clear the image cache.
     */
    public void clear()
    {
        hashMap.clear();
    }
}
