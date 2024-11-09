// Copyright (C) 2003-2004 Ericsson AB.


package myApplication;

import com.ericsson.hosasdk.utility.sync.Synchronizer;
import com.ericsson.hosasdk.api.ui.IpUIManager;
import com.ericsson.hosasdk.api.ui.IpAppUIManager;
import com.ericsson.hosasdk.api.ui.IpAppUIManagerAdapter;
import com.ericsson.hosasdk.api.ui.IpAppUICall;
import com.ericsson.hosasdk.api.ui.IpAppUICallAdapter;
import com.ericsson.hosasdk.api.ui.IpAppUIManager;
import com.ericsson.hosasdk.api.ui.IpUIManager;
import com.ericsson.hosasdk.api.ui.TpUICallIdentifier;
import com.ericsson.hosasdk.api.ui.TpUICollectCriteria;
import com.ericsson.hosasdk.api.ui.TpUIInfo;
import com.ericsson.hosasdk.api.ui.TpUITargetObject;
import com.ericsson.hosasdk.api.ui.TpUIVariableInfo;
import com.ericsson.hosasdk.api.ui.P_UI_RESPONSE_REQUIRED;
import com.ericsson.hosasdk.api.ui.P_UI_FINAL_REQUEST;
import com.ericsson.hosasdk.api.cc.mpccs.TpCallLegIdentifier;
import com.ericsson.hosasdk.api.cc.mpccs.TpMultiPartyCallIdentifier;
import com.ericsson.hosasdk.utility.sync.Synchronizer;
import com.ericsson.hosasdk.api.ui.TpUIError;
import com.ericsson.hosasdk.api.ui.TpUIReport;

/**
 * todo
 */

public class UIProcessor extends IpAppUIManagerAdapter
{
    private IpUIManager itsUIManager;
    public Synchronizer itsSync = new Synchronizer();
    private UICallCallback itsUICallCallback;

    /**
     * Creates a new instance.
     *
     * @param aUIManager a User Interaction manager
     */

    public UIProcessor(IpUIManager aUIManager)
    {
        itsUIManager = aUIManager;
        itsUICallCallback = new UICallCallback(itsSync);
    }

    /**
     * Disposes of this instance.
     */

    public void dispose()
    {
        super.dispose();
        itsUICallCallback.dispose();
    }

    /**
     * Starts a new UI session.
     *
     * @param aCall the call to which the UI session will be associated to
     */

    public TpUICallIdentifier start(TpMultiPartyCallIdentifier aCall)
    {
        TpUITargetObject uiTarget = new TpUITargetObject();
        uiTarget.MultiPartyCall(aCall);
        return itsUIManager.createUICall(itsUICallCallback, uiTarget);
    }

    public TpUICallIdentifier start(TpCallLegIdentifier aLeg)
    {
        TpUITargetObject uiTarget = new TpUITargetObject();
        uiTarget.CallLeg(aLeg);
        return itsUIManager.createUICall(itsUICallCallback, uiTarget);
    }

    /**
     * Stops a new UI session.
     *
     * @param aUISession the session Id
     */

    public void stop(TpUICallIdentifier aUISession)
    {
        aUISession.UICallRef.release(aUISession.UserInteractionSessionID);
    }

    /**
     * Plays an announcement, collects a single digit from the user and returns this response.
     * If no response is received within 10 seconds, null is returned.
     *
     * @param aUISession the session Id
     * @param aQuestion the announcement Id.
     * @param aLastAnnouncement specifies if this is the last announcement
     */

    public Object askDigit(TpUICallIdentifier aUISession, int aQuestion, boolean aLastAnnouncement)
    {
        TpUICollectCriteria criteria = new TpUICollectCriteria(1, // minLength
            1, // maxLength
            "", // endSequence
            10000, // startTimeout
            10000); // interCharTimeout

        int assignmentID = aUISession.UICallRef.sendInfoAndCollectReq(aUISession.UserInteractionSessionID,
            createInfo(aQuestion), "", // default language
            new TpUIVariableInfo[0], criteria,
            P_UI_RESPONSE_REQUIRED.value
            | (aLastAnnouncement ? P_UI_FINAL_REQUEST.value : 0));

        // wait at most 60 seconds for a response from NRG
        // (if a timeout occurs, null is returned)
        return itsSync.waitForResult(assignmentID, 60000);
    }

    /**
     * Plays an announcement and returns after the announcement has completed..
     *
     * @param aUISession the session Id
     * @param aMessage the announcement Id
     * @param aLastAnnouncement specifies if this is the last announcement
     */

    public void say(TpUICallIdentifier aUISession, int aMessage, boolean aLastAnnouncement)
    {
        int assignmentID = aUISession.UICallRef.sendInfoReq(aUISession.UserInteractionSessionID,
            createInfo(aMessage), "", // default language
            new TpUIVariableInfo[0], 1, // repeatIndicator
            P_UI_RESPONSE_REQUIRED.value
            | (aLastAnnouncement ? P_UI_FINAL_REQUEST.value : 0));

        // wait at most 60 seconds for the announcement to finish
        itsSync.waitForResult(assignmentID, 60000);
    }

    private static TpUIInfo createInfo(int anInfoId)
    {
        TpUIInfo info = new TpUIInfo();
        info.InfoID(anInfoId);
        return info;
    }

    private static class UICallCallback extends IpAppUICallAdapter
        implements IpAppUICall
    {
        private Synchronizer theSync;

        /**
         * @param aSync
         */
        public UICallCallback(Synchronizer aSync)
        {
            theSync = aSync;
        }

        public void sendInfoAndCollectRes(int userInteractionSessionID, int assignmentID, TpUIReport response, java.lang.String collectedInfo)
        {
            theSync.notifyResult(assignmentID, collectedInfo);
        }

        public void sendInfoAndCollectErr(int userInteractionSessionID, int assignmentID, TpUIError error)
        {
            theSync.notifyResult(assignmentID);
        }

        public void sendInfoRes(int userInteractionSessionID, int assignmentID, TpUIReport response)
        {
            System.out.println("res");
            theSync.notifyResult(assignmentID);
        }

        public void sendInfoErr(int userInteractionSessionID, int assignmentID, TpUIError error)
        {
            System.out.println("err");
            theSync.notifyResult(assignmentID);
        }

        // unused methods:
        // public void recordMessageRes(int userInteractionSessionID, int assignmentID, TpUIReport response, int messageID)
        // public void recordMessageErr(int userInteractionSessionID, int assignmentID, TpUIError error)
        // public void deleteMessageRes(int usrInteractionSessionID, TpUIReport response, int assignmentID)
        // public void deleteMessageErr(int usrInteractionSessionID, TpUIError error, int assignmentID)
        // public void abortActionRes(int userInteractionSessionID, int assignmentID)
        // public void abortActionErr(int userInteractionSessionID, int assignmentID, TpUIError error)
        // public void userInteractionFaultDetected(int userInteractionSessionID, TpUIFault fault)
    }
}