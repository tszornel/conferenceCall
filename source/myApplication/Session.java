// Copyright (C) 2003-2004 Ericsson AB.


package myApplication;

import com.ericsson.hosasdk.api.TpAddress;

import com.ericsson.hosasdk.api.cc.mpccs.IpAppCallLeg;
import com.ericsson.hosasdk.api.cc.mpccs.TpCallLegIdentifier;
import com.ericsson.hosasdk.api.cc.mpccs.TpMultiPartyCallIdentifier;
import java.util.*;

/**
 * Session contains all the information regarding the call set up process
 *
 * @author Ericsson AB
 */

public class Session
{
    private TpMultiPartyCallIdentifier itsCallId = null;
    private TpCallLegIdentifier itsPreviousLeg = null;
    private TpCallLegIdentifier itsCurrentLeg = null;
    private String[] itsParties;
    private int itsNextPartyToRoute;
    TimerTask itsGuard;

    /**
     * The constructor
     */
    public Session(TpMultiPartyCallIdentifier aCallId, String[] aParties)
    {
        itsCallId = aCallId;
        itsParties = aParties;
    }

    /**
     * Returns the call Id
     */
    public TpMultiPartyCallIdentifier getCallId()
    {
        return itsCallId;
    }

     /**
     * Sets the previous leg id
     */
    public void setPreviousLegIdentifier(TpCallLegIdentifier aPreviousLeg)
    {
        itsPreviousLeg = aPreviousLeg;
    }

    /**
     * Returns the previous leg id
     */
    public TpCallLegIdentifier getPreviousLegIdentifier()
    {
        return itsPreviousLeg;
    }

    /**
     * Sets the current leg id
     */
    public void setCurrentLegIdentifier(TpCallLegIdentifier aCurrentLeg)
    {
        itsCurrentLeg = aCurrentLeg;
    }

    /**
     * Returns the current leg id
     */
    public TpCallLegIdentifier getCurrentLegIdentifier()
    {
        return itsCurrentLeg;
    }

    /**
     * Returns a party that has not been routed yet,
     * or null if all parties have been routed
     */
    public String getNextParty()
    {
        return itsNextPartyToRoute < itsParties.length
            ? itsParties[itsNextPartyToRoute++]
            : null;
    }


    public boolean allRouted()
    {
        return itsNextPartyToRoute == itsParties.length;
    }

boolean first = true;
}