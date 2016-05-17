/**
 * Peer to peer whiteboard
 * Distributed computing.
 * CMPSMC34
 */

package com.twizted.Utility;

import javafx.beans.NamedArg;

/**
 * @author Ian Weeks 6204848
 *         <p>
 *         Triple class.
 *         <p>
 *         Like a Pair but with three...
 * @see javafx.util.Pair
 */
public class Triple<K, S, I>
{
    private K key;
    private S valueOne;
    private I valueTwo;

    /**
     * Triple constructor.
     *
     * @param key      The key.
     * @param valueOne First value.
     * @param valueTwo Second value.
     */
    public Triple(@NamedArg("key") K key, @NamedArg("valueOne") S valueOne, @NamedArg("valueTwo") I valueTwo)
    {
        this.key = key;
        this.valueOne = valueOne;
        this.valueTwo = valueTwo;
    }

    /**
     * Get the key.
     *
     * @return The key.
     */
    public K getKey()
    {
        return key;
    }

    /**
     * Get value one.
     *
     * @return Value one.
     */
    public S getValueOne()
    {
        return valueOne;
    }

    /**
     * Get value two.
     *
     * @return Value two.
     */
    public I getValueTwo()
    {
        return valueTwo;
    }

    /**
     * Test this <code>Triple</code> for equality with another <code>Object</code>.
     * <p>
     * If the <code>Object</code> to be tested is not a <code>Triple</code>, then this method returns <code>false</code>.
     * <p>
     * Two <code>Triple</code>s are considered equal if and only if both the names and first values are equal.
     *
     * @param o the <code>Object</code> to test for
     *          equality with this <code>Pair</code>
     * @return <code>true</code> if the given <code>Object</code> is
     * equal to this <code>Pair</code> else <code>false</code>
     */
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Triple)
        {
            Triple triple = (Triple) o;

            if (key == triple.getKey() && valueOne.equals(triple.getValueOne()))
            {
                return true;
            }
        }
        return false;
    }
}
