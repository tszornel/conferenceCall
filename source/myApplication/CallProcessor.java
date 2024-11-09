// Copyright (C) 2003-2004 Ericsson AB.


package myApplication;

import com.ericsson.hosasdk.api.TpAddress;
import com.ericsson.hosasdk.api.TpAddressPresentation;
import com.ericsson.hosasdk.api.TpAddressPlan;
import com.ericsson.hosasdk.api.TpAddressScreening;

import com.ericsson.hosasdk.api.cc.TpAdditionalCallEventCriteria;
import com.ericsson.hosasdk.api.cc.TpCallAppInfo;
import com.ericsson.hosasdk.api.cc.TpCallEndedReport;
import com.ericsson.hosasdk.api.cc.TpCallError;
import com.ericsson.hosasdk.api.cc.TpCallEventRequest;
import com.ericsson.hosasdk.api.cc.TpCallEventType;
import com.ericsson.hosasdk.api.cc.TpCallMonitorMode;
import com.ericsson.hosasdk.api.cc.TpReleaseCause;

import com.ericsson.hosasdk.api.cc.mpccs.IpAppMultiPartyCall;
import com.ericsson.hosasdk.api.cc.mpccs.IpAppMultiPartyCallAdapter;
import com.ericsson.hosasdk.api.cc.mpccs.TpCallLegIdentifier;
import com.ericsson.hosasdk.api.cc.mpccs.TpMultiPartyCallIdentifier;

/**
 * This class is responsible for all needed HOSA implementation regarding
 * Multi Party Call Control for this application
 *
 * @author Ericsson AB
 */
public class CallProcessor extends IpAppMultiPartyCallAdapter
{
    private Feature itsParent;
    private LegProcessor itsLegProcessor;

    /**
     * The constructor of this class
     *
     * @param aParent the Parent to which this class can callback to
     * @param aLegProcessor the legProcessor which the CallProcessor can use
     */
    public CallProcessor(Feature aParent, LegProcessor aLegProcessor)
    {
        itsParent = aParent;
        itsLegProcessor = aLegProcessor;
    }

    /**
     * This method can be used to create a new MPCC leg. Because this leg will be used in an
     * application initiated call the originating address will be specified as undefined.
     *
     * @param aCallId The MPCC call identifier
     * @param aPhoneNumber the phone number to which the leg needs to be routed
     *
     * @return the MPCC leg identifier
     */
    public TpCallLegIdentifier createAndRouteLeg(TpMultiPartyCallIdentifier aCallId, String aPhoneNumber)
    {
        TpAdditionalCallEventCriteria answerCriteria = new TpAdditionalCallEventCriteria();
        answerCriteria.Dummy((short) 0);
        TpCallEventRequest answerRequest = new TpCallEventRequest(TpCallEventType.P_CALL_EVENT_ANSWER,
            answerCriteria,
            TpCallMonitorMode.P_CALL_MONITOR_MODE_INTERRUPT);

        TpReleaseCause[] RELEASE_CAUSES = {TpReleaseCause.P_BUSY,
            TpReleaseCause.P_CALL_RESTRICTED,
            TpReleaseCause.P_DISCONNECTED,
            TpReleaseCause.P_GENERAL_FAILURE,
            TpReleaseCause.P_NO_ANSWER, TpReleaseCause.P_NOT_REACHABLE,
            TpReleaseCause.P_PREMATURE_DISCONNECT,
            TpReleaseCause.P_ROUTING_FAILURE,
            TpReleaseCause.P_TIMER_EXPIRY,
            TpReleaseCause.P_UNAVAILABLE_RESOURCE,
            TpReleaseCause.P_USER_NOT_AVAILABLE};
        TpAdditionalCallEventCriteria releaseCriteria = new TpAdditionalCallEventCriteria();
        releaseCriteria.TerminatingReleaseCauseSet(RELEASE_CAUSES);
        TpCallEventRequest releaseRequest = new TpCallEventRequest(TpCallEventType.P_CALL_EVENT_TERMINATING_RELEASE,
            releaseCriteria,
            TpCallMonitorMode.P_CALL_MONITOR_MODE_INTERRUPT);

        TpCallEventRequest[] requestedEvents = {answerRequest,
            releaseRequest};

        TpAddress originatingAddress = new TpAddress(TpAddressPlan.P_ADDRESS_PLAN_NOT_PRESENT,
            "", "",
            TpAddressPresentation.P_ADDRESS_PRESENTATION_UNDEFINED,
            TpAddressScreening.P_ADDRESS_SCREENING_UNDEFINED, "");

        return aCallId.CallReference.createAndRouteCallLegReq(aCallId.CallSessionID,
            requestedEvents, createE164Address(aPhoneNumber),
            originatingAddress, new TpCallAppInfo[0], itsLegProcessor);
    }

    /**
     * Deassigns the call.
     *
     * @param aCallId The MPCC call identifier
     */
    public void deassign(TpMultiPartyCallIdentifier aCallId)
    {
        aCallId.CallReference.deassignCall(aCallId.CallSessionID);
    }

    // unused methods:
    // public void getInfoRes(int callSessionID, TpCallInfoReport callInfoReport)
    // public void getInfoErr(int callSessionID, TpCallError errorIndication)
    // public void superviseRes(int callSessionID, int report, int usedTime)
    // public void superviseErr(int callSessionID, TpCallError errorIndication)
    // public void callEnded(int callSessionID, TpCallEndedReport report)
    // public void createAndRouteCallLegErr(int callSessionID, TpCallLegIdentifier callLegReference, TpCallError errorIndication)

    private static TpAddress createE164Address(String aNumber)
    {
        return new TpAddress(TpAddressPlan.P_ADDRESS_PLAN_E164,
            aNumber, "",
            TpAddressPresentation.P_ADDRESS_PRESENTATION_UNDEFINED,
            TpAddressScreening.P_ADDRESS_SCREENING_UNDEFINED, "");
    }

}