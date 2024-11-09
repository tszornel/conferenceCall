// Copyright (C) 2003-2004 Ericsson AB.


package myApplication;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * This class defines a simple graphical user interface.
 * Users can add (tab) pages and buttons.
 */

public class GUI extends JFrame
{
    private JTabbedPane theTabs = new JTabbedPane();
    private JPanel theButtons = new JPanel();

    /**
     * Creates a frame containing placeholders for (tab) pages and buttons.
     */

    public GUI()
    {
        // construct the contents
        theButtons.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 8));
        JPanel contents = new JPanel();
        contents.setLayout(new BorderLayout());
        contents.add(theTabs, BorderLayout.CENTER);
        contents.add(theButtons, BorderLayout.SOUTH);
        contents.setBorder(new EmptyBorder(8, 8, 0, 8));

        // add the contents to the frame
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(contents, BorderLayout.CENTER);

        // the class that opens the window should close it as well
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    /**
     * Centers and shows the window
     */

    public void showCentered()
    {
        pack();
        Dimension guiSize = getSize();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - guiSize.width) / 2,
            (screenSize.height - guiSize.height) / 2);
        setVisible(true);
    }

    /**
     * Adds a tab page with a given title and contents.
     *
     * @param aTitle the name of the tab page
     * @param aComp the contents of the tab page (will be embedded in a
     *      JScrollPane)
     */

    public void addTab(String aTitle, Component aComp)
    {
        theTabs.add(aTitle, new JScrollPane(aComp));
    }

    /**
     * Adds a tab page with a given title and contents.
     *
     * @param aTitle the name of the tab page
     * @param aText the contents of the tab page
     */

    public void addTab(String aTitle, String aText)
    {
        JTextArea text = new JTextArea(18, 50);

        text.setText(aText);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);
        text.setBorder(new EmptyBorder(4, 8, 4, 8));
        addTab(aTitle, text);
    }

    /**
     * Adds a button.
     *
     * @param anAction the action to perform when the button is clicked
     */

    public void addButton(Action anAction)
    {
        theButtons.add(new JButton(anAction));
    }
}
