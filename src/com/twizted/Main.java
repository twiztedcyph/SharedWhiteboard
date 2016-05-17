/**
 * Peer to peer whiteboard
 * Distributed computing.
 * CMPSMC34
 */

package com.twizted;

import com.twizted.Display.Frame;

import javax.swing.*;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Main class.
 */
public class Main
{
    /**
     * Entry point for the program.
     *
     * @param args Program starting arguments.
     * @throws UnknownHostException If the host IP cannot be found.
     * @throws SocketException      If a socket cannot be read or written to.
     * @throws InterruptedException If a thread or queue is interrupted while waiting.
     */
    public static void main(final String[] args) throws UnknownHostException, SocketException, InterruptedException
    {
        //Start swing on its own ui thread.
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    new Frame();
                } catch (IOException e)
                {
                    System.out.println("Socket or host error.\n" + e.getMessage());
                    System.out.println(e.getMessage());
                }
            }
        });
    }
}
