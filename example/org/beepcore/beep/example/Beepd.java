/*
 * Beepd.java  $Revision: 1.8 $ $Date: 2002/10/05 15:33:22 $
 *
 * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
 *
 * The contents of this file are subject to the Blocks Public License (the
 * "License"); You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.beepcore.org/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 */
package org.beepcore.beep.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

import javax.xml.parsers.*;

import org.w3c.dom.*;

import org.xml.sax.SAXException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.beepcore.beep.core.ProfileRegistry;
import org.beepcore.beep.core.SessionTuningProperties;
import org.beepcore.beep.profile.Profile;
import org.beepcore.beep.profile.ProfileConfiguration;
import org.beepcore.beep.transport.tcp.TCPSessionCreator;

/**
 * Sample BEEP server analogous to inetd. Based on the configuration file
 * it loads the specified profiles and listens for new sessions on the
 * specified ports.
 *
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.8 $, $Date: 2002/10/05 15:33:22 $
 */
public class Beepd extends Thread {
    private int port;
    ProfileRegistry reg;

    private Log log = LogFactory.getLog(this.getClass());


    /**
     * Parses the beepd element in the configuration file and loads the
     * classes for the specified profiles.
     *
     * @param serverConfig &ltbeepd&gt configuration element.
     */
    private Beepd(Element serverConfig) throws Exception {
        reg = new ProfileRegistry();

        if (serverConfig.hasAttribute("port") == false) {
            throw new Exception("Invalid configuration, no port specified");
        }

        port = Integer.parseInt(serverConfig.getAttribute("port"));

        // Parse the list of profile elements.
        NodeList profiles = serverConfig.getElementsByTagName("profile");
        for (int i=0; i<profiles.getLength(); ++i) {
            if (profiles.item(i).getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element profile = (Element) profiles.item(i);

            if (profile.getNodeName().equalsIgnoreCase("profile") == false) {
                continue;
            }

            String uri;
            String className;
            String requiredProperites;
            String tuningProperties;

            if (profile.hasAttribute("uri") == false) {
                throw new Exception("Invalid configuration, no uri specified");
            }

            uri = profile.getAttribute("uri");

            if (profile.hasAttribute("class") == false) {
                throw new Exception("Invalid configuration, no class " +
                                    "specified for profile " + uri);
            }

            className = profile.getAttribute("class");

            // parse the parameter elements into a ProfileConfiguration
            ProfileConfiguration profileConfig =
                parseProfileConfig(profile.getElementsByTagName("parameter"));

            // load the profile class
            Profile p;
            try {
                p = (Profile) Class.forName(className).newInstance();
            } catch (ClassNotFoundException e) {
                throw new Exception("Class " + className + " not found");
            } catch (ClassCastException e) {
                throw new Exception("class " + className + " does not " +
                                    "implement the " +
                                    "org.beepcore.beep.profile.Profile " +
                                    "interface");
            }

            SessionTuningProperties tuning = null;

            if (profile.hasAttribute("tuning")) {
                String tuningString = profile.getAttribute("tuning");
                Hashtable hash = new Hashtable();
                StringTokenizer tokens = new StringTokenizer(tuningString,
                                                             ":=");

                try {
                    while (tokens.hasMoreTokens()) {
                        String parameter = tokens.nextToken();
                        String value = tokens.nextToken();

                        hash.put(parameter, value);
                    }
                } catch (NoSuchElementException e) {
                    e.printStackTrace();

                    throw new Exception("Error parsing tuning property on " +
                                        "profile " + uri);
                }

                tuning = new SessionTuningProperties(hash);
            }

            // Initialize the profile and add it to the advertised profiles
            reg.addStartChannelListener(uri,
                                        p.init(uri, profileConfig),
                                        tuning);
        }
    }

    public static void main(String[] argv) {
        File config = new File("config.xml");

        for (int i=0; i < argv.length; ++i) {
            if (argv[i].equalsIgnoreCase("-config")) {
                config = new File(argv[++i]);
                if (config.exists() == false) {
                    System.err.println("Beepd: Error file " +
                                       config.getAbsolutePath() +
                                       " does not exist");
                    return;
                }
            } else {
                System.err.println(usage);
                return;
            }
        }

        Document doc;
        try {
            DocumentBuilder builder =
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = builder.parse(new FileInputStream(config));
        } catch (ParserConfigurationException e) {
            System.err.println("Beepd: Error parsing config\n" +
                e.getMessage());
            return;
        } catch (SAXException e) {
            System.err.println("Beepd: Error parsing config\n" +
                e.getMessage());
            return;
        } catch (IOException e) {
            System.err.println("Beepd: Error parsing config\n" +
                e.getMessage());
            return;
        }

        // Parse the configuration file
        Collection servers;
        try {
            servers = parseConfig(doc.getDocumentElement());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return;
        }

        // Start the servers listening
        Iterator i = servers.iterator();
        while (i.hasNext()) {
            ((Beepd) i.next()).start();
        }

        System.out.println("Beepd: started");
    }

    public void run() {
        try {
            // Loop listening for new Sessions
            while (true) {
                TCPSessionCreator.listen(port, reg);
            }
        } catch (Exception e) {
            log.error("Listener exiting", e);
        }
    }

    private static Collection parseConfig(Element doc) throws Exception {
        LinkedList servers = new LinkedList();
        NodeList serverNodes = doc.getChildNodes();

        for (int i=0; i<serverNodes.getLength(); ++i) {
            Node s = serverNodes.item(i);
            if (s.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            if (s.getNodeName().equalsIgnoreCase("beepd") == false) {
                continue;
            }

            servers.add(new Beepd((Element) s));
        }

        return servers;
    }

    /**
     * Parses the child elements of a profile element into a
     * <code>ProfileConfiguration</code> object.
     *
     * @param profileConfig list of parameter child elements of a profile
     *                      element.
     */
    private static ProfileConfiguration
        parseProfileConfig(NodeList profileConfig) throws Exception
    {
        ProfileConfiguration config = new ProfileConfiguration();

        for (int i=0; i<profileConfig.getLength(); ++i) {
            Element parameter = (Element) profileConfig.item(i);

            if (parameter.hasAttribute("name") == false ||
                parameter.hasAttribute("value") == false)
            {
                throw new Exception("Invalid configuration parameter " +
                                    "missing name or value attibute");
            }

            config.setProperty(parameter.getAttribute("name"),
                               parameter.getAttribute("value"));
        }

        return config;
    }

    private static final String usage =
        "usage: beepd [-config <file>]\n\n" +
        "options:\n" +
        "    -config file  File to read the configuration from.\n";
}
