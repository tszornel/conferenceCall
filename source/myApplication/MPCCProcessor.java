// Copyright (C) 2003-2004 Ericsson AB.


package myApplication;

import com.ericsson.hosasdk.api.cc.mpccs.IpAppMultiPartyCall;
import com.ericsson.hosasdk.api.cc.mpccs.IpAppMultiPartyCallControlManagerAdapter;
import com.ericsson.hosasdk.api.cc.mpccs.IpMultiPartyCallControlManager;
import com.ericsson.hosasdk.api.cc.mpccs.TpAppMultiPartyCallBack;
import com.ericsson.hosasdk.api.cc.mpccs.TpMultiPartyCallIdentifier;

/**
 * This class is responsible for all needed HOSA implementation regarding
 * Multi Party Call Control for this application
 *
 * @author Ericsson AB
 */
public class MPCCProcessor extends IpAppMultiPartyCallControlManagerAdapter
{
    private IpMultiPartyCallControlManager itsMPCCManager;
    private TpAppMultiPartyCallBack itsMPCCCallback;
    private Feature itsParent;

    /**
     * The constructor of this class
     *
     * @param aParent the Parent to which this class can callback to
     * @param aMPCCManager manager used to talk to the NRG
     * @param aMPCCCallCallback Specifies the callback for MPCC
     */
    public MPCCProcessor(Feature aParent,
        IpMultiPartyCallControlManager aMPCCManager,
        IpAppMultiPartyCall aMPCCCallCallback)
    {
        itsParent = aParent;

        itsMPCCCallback = new TpAppMultiPartyCallBack();
        itsMPCCCallback.AppMultiPartyCall(aMPCCCallCallback);
        itsMPCCManager = aMPCCManager;
    }

    /**
     * Creates a new Multi Party Call Control call.
     *
     * @return the call
     */
    public TpMultiPartyCallIdentifier createCall()
    {
        return itsMPCCManager.createCall(itsMPCCCallback.AppMultiPartyCall());
    }


    /**
     * Disposes of this instance.
     */
    public void dispose()
    {
        super.dispose();
    }
}