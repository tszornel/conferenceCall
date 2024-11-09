// Copyright (C) 2003-2004 Ericsson AB.


package myApplication;

import java.util.*;
import java.io.*;
import com.ericsson.hosasdk.utility.configuration.NestedProperties;
import com.ericsson.hosasdk.utility.configuration.NotFoundException;
import com.ericsson.hosasdk.utility.configuration.BadSyntaxException;

/**
 * This class is responsible for parsing the configuration file and
 * presenting the configuration data using convenient data types.
 */

public class Configuration extends NestedProperties
{

    /** The singleton instance that can be accessed by all users. */
    public static final Configuration INSTANCE = new Configuration();

    /**
     * Private constructor to prevent multiple instances.
     */
    private Configuration()
    {}
}