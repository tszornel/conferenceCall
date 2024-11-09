// Copyright (C) 2003-2004 Ericsson AB.


package myApplication;

import com.ericsson.hosasdk.api.cc.TpCallError;
import com.ericsson.hosasdk.api.cc.TpCallEventInfo;
import com.ericsson.hosasdk.api.cc.TpReleaseCause;

import com.ericsson.hosasdk.api.cc.mpccs.IpAppCallLeg;
import com.ericsson.hosasdk.api.cc.mpccs.IpAppCallLegAdapter;
import com.ericsson.hosasdk.api.cc.mpccs.TpCallLegIdentifier;

import com.ericsson.hosasdk.utility.sync.Synchronizer;

/**
 * This class is responsible for all needed HOSA implementation regarding
 * Multi Party Call Control for this application.
 *
 * @author Ericsson AB
 */
public class LegProcessor extends IpAppCallLegAdapter
{
    private Synchronizer itsSync = new Synchronizer();
    private Feature itsParent;

    /**
     * The constructor
     *
     * @param aParent the Parent to which this class can callback to
     */
    public LegProcessor(Feature aParent)
    {
        itsParent = aParent;
    }

    /**
     * This asynchronous method reports that an event has occurred that was
     * requested to be reported. The routed method from the feature will be
     * used to inform the feature that the leg is routed.
     *
     * @param callLegSessionID specifies the call leg session ID of the call leg
     * @param eventInfo specifies data associated with this event
     */
    public void eventReportRes(int callLegSessionID, TpCallEventInfo eventInfo)
    {
        itsParent.routed(callLegSessionID);
    }

    /**
     * This method can be used to detach the leg from the call.
     * This is needed when you want to play an announcement on the leg.
     *
     * @param aLeg the leg which needs to be detached
     */
    public void detach(TpCallLegIdentifier aLeg)
    {
        aLeg.CallLegReference.detachMediaReq(aLeg.CallLegSessionID);

        // wait at most 60 seconds for the leg to be detached
        itsSync.waitForResult(aLeg.CallLegSessionID, 60000);

    }

    /**
     * This method can be used to attach the leg from the call.
     * This is needed after you played an announcement on the leg.
     *
     * @param aLeg the leg which needs to be attached
     */
    public void attach(TpCallLegIdentifier aLeg)
    {
        aLeg.CallLegReference.attachMediaReq(aLeg.CallLegSessionID);

        // wait at most 60 seconds for the leg to be attached
        itsSync.waitForResult(aLeg.CallLegSessionID, 60000);
    }

    public void continueProcessing(TpCallLegIdentifier aLeg)
    {
        aLeg.CallLegReference.continueProcessing(aLeg.CallLegSessionID);
    }

    /**
     * Result method comming from the SDK
     *
     * @param callLegSessionID Identified the leg
     */
    public void attachMediaRes(int callLegSessionID)
    {
        itsSync.notifyResult(callLegSessionID);
    }

    /**
     * result method comming from the SDK
     *
     * @param callLegSessionID Identified the leg
     */
    public void detachMediaRes(int callLegSessionID)
    {
        itsSync.notifyResult(callLegSessionID);
    }

    // unused methods:
    // public void eventReportErr(int callLegSessionID, TpCallError errorIndication)
    // public void attachMediaErr(int callLegSessionID, TpCallError errorIndication)
    // public void detachMediaErr(int callLegSessionID, TpCallError errorIndication)
    // public void getInfoRes(int callLegSessionID, TpCallLegInfoReport callLegInfoReport)
    // public void getInfoErr(int callLegSessionID, TpCallError errorIndication)
    // public void routeErr(int callLegSessionID, TpCallError errorIndication)
    // public void superviseRes(int callLegSessionID, int report, int usedTime)
    // public void superviseErr(int callLegSessionID, TpCallError errorIndication)
    // public void callLegEnded(int callLegSessionID, TpReleaseCause cause)
}