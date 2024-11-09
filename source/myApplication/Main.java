// Copyright (C) 2003-2004 Ericsson AB.


package myApplication;

import java.awt.event.*;
import javax.swing.*;

/**
 * This class is responsible for initializing, starting, stopping and
 * terminating the application.
 */

public class Main
{
    private Feature itsFeature;

    /**
     * Method called by the JVM to launch the application.
     */

    public static void main(String[] args)
        throws Exception
    {
        new Main();
    }

    /**
     * Initializes the application.
     */

    public Main()
        throws Exception
    {
        Configuration.INSTANCE.load("config/config.ini");

        GUI gui = new GUI();

        gui.addButton(new AbstractAction("Start")
        {
            public void actionPerformed(ActionEvent e)
            {
                start();
            }
        });

        itsFeature = new Feature(gui);

        gui.addButton(new AbstractAction("Stop")
        {
            public void actionPerformed(ActionEvent e)
            {
                stop();
            }
        });

        gui.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                terminate();
            }
        });

        gui.showCentered();
    }

    /**
     * Starts interaction with NRG.
     */

    public void start()
    {
        itsFeature.start();
    }

    /**
     * Stops interaction with NRG.
     */

    public void stop()
    {
        itsFeature.stop();
    }

    /**
     * Terminates the application.
     */

    public void terminate()
    {
        System.exit(0);
    }
}