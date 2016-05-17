/**
 * Peer to peer whiteboard
 * Distributed computing.
 * CMPSMC34
 */
package com.twizted.Cache;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Ian Weeks 6204848
 *         ActionCache class.
 *         <p>
 *         Stores the last 5000 actions when networking
 *         is turned on.  This is used to provide a
 *         history to new peers.
 *         This is a singleton class.
 */
public class ActionCache implements Iterable<int[]>
{
    private static ActionCache instance;
    private volatile BlockingQueue<int[]> queue;

    private ActionCache()
    {
        queue = new LinkedBlockingQueue<>();
    }

    /**
     * Get the instance of this ActionCache;
     *
     * @return The instance of this ActionCache.
     */
    public static ActionCache getInstance()
    {
        if (instance == null)
        {
            instance = new ActionCache();
        }
        return instance;
    }

    /**
     * Add an action to the cache.
     *
     * @param data Action to be added.
     * @throws InterruptedException If interrupted while blocking.
     */
    public synchronized void put(int[] data) throws InterruptedException
    {
        //Keep the size of the history to a given limit.
        //I chose 10000 but have tested up to 100000.
        if (queue.size() >= 10000)
        {
            queue.take();
        }
        queue.put(data);
    }

    /**
     * Get the size of this cache.
     *
     * @return The size of this cache.
     */
    public int getSize()
    {
        return queue.size();
    }

    /**
     * Clear this cache.
     */
    public void clear()
    {
        queue.clear();
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<int[]> iterator()
    {
        return queue.iterator();
    }
}
