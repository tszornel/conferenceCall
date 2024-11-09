// Copyright (C) 2003-2004 Ericsson AB.


package myApplication;

import java.util.HashMap;

import com.ericsson.hosasdk.api.TpAddress;
import com.ericsson.hosasdk.api.cc.TpCallEventInfo;

import com.ericsson.hosasdk.api.cc.mpccs.IpAppCallLeg;
import com.ericsson.hosasdk.api.cc.mpccs.IpMultiPartyCallControlManager;
import com.ericsson.hosasdk.api.cc.mpccs.TpCallLegIdentifier;
import com.ericsson.hosasdk.api.cc.mpccs.TpMultiPartyCallIdentifier;

import com.ericsson.hosasdk.api.ui.IpUIManager;
import com.ericsson.hosasdk.api.ui.TpUICallIdentifier;

import com.ericsson.hosasdk.utility.framework.FWproxy;

import com.ericsson.hosasdk.utility.log.ObjectWriter;
import com.ericsson.hosasdk.utility.log.SimpleTracer;

import com.ericsson.hosasdk.api.HOSAMonitor;


import java.util.*;
import java.util.Timer;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * This class implements the logic of the application.
 */

public class Feature
{

    static String APPLICATION_NAME = "Conference Call";
    static String USER_TAB_TEXT = "Parties";
    static String USER_BUTTON_TEXT = "Create Conference Call";

    // the FWProxy used to obtain the service managers
    private FWproxy itsFramework;

    // the service managers
    private IpMultiPartyCallControlManager itsMPCCManager;
    private IpUIManager itsUIManager;

    // the HOSA callbacks
    private MPCCProcessor itsMPCCProcessor;
    private CallProcessor itsCallProcessor;
    private LegProcessor itsLegProcessor;
    private UIProcessor itsUIProcessor;

    Map itsSessions = new HashMap();
    Timer itsGuardManager = new Timer();

    /**
     * Initializes a new instance, without starting interaction with NRG
     * (see start)
     *
     * @param aGUI the GUI of the application
     */
    public Feature(GUI aGUI)
    {
        aGUI.setTitle(APPLICATION_NAME);
        aGUI.addTab("Description", getDescription());

        final JTextArea numbersField = new JTextArea();
        numbersField.setBorder(new EmptyBorder(4, 8, 4, 8));
        aGUI.addTab(USER_TAB_TEXT, numbersField);
        aGUI.addButton(new AbstractAction(USER_BUTTON_TEXT)
        {
            public void actionPerformed(ActionEvent e)
            {
                StringTokenizer s = new StringTokenizer(numbersField.getText(),
                    " \t\n\r\f,+");
                String[] numbers = new String[s.countTokens()];
                for (int i = 0; i != numbers.length; i++)
                {
                    numbers[i] = s.nextToken();
                }

                try
                {
                    establishCall(numbers);
                }
                catch (Exception ex)
                {
                    System.err.println(ObjectWriter.print(ex));
                }
            }
        });
    }

    /**
     * Returns a descriptive text that explains the application and its configuration.
     */

    private String getDescription()
    {
        String s = "Press START to connect to the Framework"
            + " and request the OSA Multi Party Call Control (MPCC) service and the"
            + " OSA Generic User Interaction (UI) service from the Framework.\n";
        s += "\n";
        s += "Press CREATE CONFERENCE CALL to setup a call between all specified parties.\n";
        s += "\n";
        s += "Press STOP to release resources in the NRG and the application.\n";
        return s;
    }


    /**
     * Connect to the Framework and obtain the Service Capability Features(SCF)
     * needed.
     */
    protected void start()
    {
        System.out.println("Starting HOSA tracing");
        HOSAMonitor.addListener(SimpleTracer.SINGLETON);

        itsFramework = new FWproxy(Configuration.INSTANCE);
        itsMPCCManager = (IpMultiPartyCallControlManager) itsFramework.obtainSCF("P_MULTI_PARTY_CALL_CONTROL");
        itsUIManager = (IpUIManager) itsFramework.obtainSCF("P_USER_INTERACTION");

        itsLegProcessor = new LegProcessor(this);
        itsCallProcessor = new CallProcessor(this, itsLegProcessor);
        itsMPCCProcessor = new MPCCProcessor(this, itsMPCCManager,
            itsCallProcessor);
        itsUIProcessor = new UIProcessor(itsUIManager);
    }

    /**
     * Releases the Service Capability Features(SCF) which was obtained by
     * start() and the disconnect from the Framework.
     */
    protected void stop()
    {
        if (itsFramework != null)
        {
            itsUIProcessor.dispose();
            itsMPCCProcessor.dispose();

            itsFramework.releaseSCF(itsUIManager);
            itsFramework.releaseSCF(itsMPCCManager);
            itsFramework.endAccess(null); // Close framework connection
            itsFramework.dispose();
            itsFramework = null;

            System.out.println("Stopping HOSA tracing");
            HOSAMonitor.removeListener(SimpleTracer.SINGLETON);
        }
    }

    /**
     * Creates an application initiated call.
     * @param aPhoneNumbers Specifies the phone numbers for the call
     */

    protected void establishCall(String[] aPhoneNumbers)
    {
        TpMultiPartyCallIdentifier call = itsMPCCProcessor.createCall();
        Session session = new Session(call, aPhoneNumbers);
        // route the first leg
        createAndRouteNextLeg(session);
    }

    /**
     * Invoked when a leg has been routed.
     * @param aLegSessionID identifies the routed leg
     */

    public void routed(final int aLegSessionID)
    {
        final Session session = (Session)itsSessions.get(new Integer(aLegSessionID));
        final TpCallLegIdentifier leg = session.getCurrentLegIdentifier();

        new Thread()
        {
            public void run()
            {
                if (!session.allRouted())
                {
                    if (session.first)
    		        {
	    		        session.first = false;
                        TpUICallIdentifier uiSession = itsUIProcessor.start(session.getCallId());
	                    itsUIProcessor.say(uiSession, 17, true);
    		        }
    		        else
	    	        {
	                    itsLegProcessor.detach(leg);
      	                TpUICallIdentifier uiSession = itsUIProcessor.start(leg);
	                    itsUIProcessor.say(uiSession, 17, true);
   	    	            itsLegProcessor.attach(leg);
    		        }
                }
                else
                {
                    TpUICallIdentifier uiSession = itsUIProcessor.start(session.getCallId());
                    itsUIProcessor.say(uiSession, 16, true);
                }

                Session session2 = removeSession(aLegSessionID);
                session2.setPreviousLegIdentifier(leg);
                createAndRouteNextLeg(session2);
	        }
        }.start();
    }

    /**
     * Creates and routes the next leg in the call,
     * or deassigns the call if all legs have been routed.
     * @param aSession the session that represents the call state
     */

    private synchronized void createAndRouteNextLeg(Session aSession)
    {
        String nextParty = aSession.getNextParty();

        if (nextParty != null)
        {
            TpCallLegIdentifier leg = itsCallProcessor.createAndRouteLeg(aSession.getCallId(),
                nextParty);

            TpCallLegIdentifier previousLeg = aSession.getPreviousLegIdentifier();
            if (previousLeg != null)
            {
                itsLegProcessor.continueProcessing(previousLeg);
            }

            aSession.setCurrentLegIdentifier(leg);
            addSession(leg.CallLegSessionID, aSession);
        }
        else
        {
            itsCallProcessor.deassign(aSession.getCallId());
        }
    }

    /**
     * Adds a new leg session to the session database.
     * @param aLegID Specifies the leg
     * @param aSession Contains the leg session
     */

    synchronized private void addSession(final int aLegID, Session aSession)
    {
        itsSessions.put(new Integer(aLegID), aSession);
        aSession.itsGuard = new TimerTask()
        {
            public void run()
            {
                removeSession(aLegID);
            }
        };
        itsGuardManager.schedule(aSession.itsGuard, 2 * 60 * 1000);
    }

    /**
     * Removes a leg session from the session database.
     * @param aLegID Specifies the leg
     * @return Returns the new hashmap
     */

    synchronized private Session removeSession(int aLegID)
    {
        Session session = (Session) itsSessions.remove(new Integer(aLegID));
        if (session != null)
        {
            session.itsGuard.cancel();
        }
        return session;
    }
}