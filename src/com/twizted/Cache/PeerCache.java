/**
 * Peer to peer whiteboard
 * Distributed computing.
 * CMPSMC34
 */

package com.twizted.Cache;

import com.twizted.Utility.Triple;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Ian Weeks 6204848
 *         PeerCache class.
 *         <p>
 *         Keeps a record of the currently connected peers.
 */
public class PeerCache implements Iterable<Triple<Integer, String, Integer>>
{
    //This class auto iterates through the following ArrayList for simplicity.
    private ArrayList<Triple<Integer, String, Integer>> peerIPs;
    private Triple<Integer, String, Integer> longestHistory;
    private int longest = 0;

    /**
     * Default constructor.
     */
    public PeerCache()
    {
        peerIPs = new ArrayList<>();
    }

    /**
     * Add a peer to this cache.
     *
     * @param peerIP Peer to be added.
     */
    public void addPeer(Triple<Integer, String, Integer> peerIP)
    {
        //Check if a record of the peer exists and if not then add it.
        if (!peerIPs.contains(peerIP))
        {
            peerIPs.add(peerIP);
            System.out.println("Added: " + peerIP.getKey() + " " + peerIP.getValueOne() + " " + peerIP.getValueTwo());
            System.out.println(this.getSize());
            //Check if the added peer has a longer history and if so save a record of it.
            if (peerIP.getValueTwo() > longest)
            {
                longest = peerIP.getValueTwo();
                longestHistory = peerIP;
            }
        }
    }

    /**
     * Remove a peer from this cache.
     *
     * @param peerIP The ip of the peer to be removed.
     */
    public void removePeer(String peerIP)
    {
        //If the peer exists then remove it.
        Triple temp = null;
        //Long winded but working approach to avoid trying to remove a record that's being accessed.
        for (Triple t : peerIPs)
        {
            if (t.getValueOne().equals(peerIP))
            {
                temp = t;
            }
        }
        if (temp != null)
        {
            peerIPs.remove(temp);
        }
    }

    /**
     * Reset the peer list.
     * <p>
     * Resetting empties the list and clears the longest history.
     */
    public void resetPeers()
    {
        peerIPs.clear();
        longestHistory = null;
        System.out.println("PeerCache cleared");
    }

    /**
     * Get the size of this cache.
     *
     * @return The size of this cache.
     */
    public int getSize()
    {
        return peerIPs.size();
    }

    /**
     * Get the peer with the longest (best) history.
     *
     * @return The peer with the longest history.
     * @see Triple
     */
    public Triple<Integer, String, Integer> getLongestHistory()
    {
        return longestHistory;
    }

    /**
     * Returns an iterator over elements of type Triple.
     *
     * @return an Iterator.
     * @see Triple
     */
    @Override
    public Iterator<Triple<Integer, String, Integer>> iterator()
    {
        return peerIPs.iterator();
    }
}
