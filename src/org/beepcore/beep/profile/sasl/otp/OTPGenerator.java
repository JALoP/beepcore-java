
/*
 * OTPGenerator.java            $Revision: 1.7 $ $Date: 2003/09/13 21:18:09 $
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
package org.beepcore.beep.profile.sasl.otp;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.Properties;

import org.beepcore.beep.core.BEEPException;
import org.beepcore.beep.profile.ProfileConfiguration;
import org.beepcore.beep.profile.sasl.SASLException;
import org.beepcore.beep.profile.sasl.otp.algorithm.Algorithm;
import org.beepcore.beep.profile.sasl.otp.database.UserDatabasePool;


/**
 * This class serves as a utility that generates OTP information
 * (in the form of a java.util.Properties that can be written to
 * network or disk. 
 *
 * @author Eric Dixon
 * @author Huston Franklin
 * @author Jay Kint
 * @author Scott Pead
 * @version $Revision: 1.7 $, $Date: 2003/09/13 21:18:09 $
 *
 */
public class OTPGenerator {
    // Constants
    // Prompts used during OTP database generation
    private static final String PROMPT_ALGORITHM =
        "Please enter the hash algorithm to use";
    private static final String PROMPT_ALGORITHM_SUGGESTIONS =
        " [ Please use 'otp-md5' or 'otp-sha1' ]";
    private static final String PROMPT_PASSPHRASE =
        "Please enter the passphrase to be used (this will NOT be stored)";
    private static final String PROMPT_RETURN =
        " and hit return when done\n=>";
    private static final String PROMPT_SEED =
        "Please enter some string to serve as your password seed:";
    private static final String PROMPT_SEQUENCE =
        "Please enter an integer corresponding to the OTP sequence you wish to use:";
    private static final String PROMPT_USERNAME =
        "Please enter the username to be used in authentication";

    // Error cases
    private static final String ERR_USER_DB_EXISTS =
        "User database exists for ";
    private static final String ERR_ALGO_UNSUPPORTED =
        "Algorithm not supported=>";
    private static final String ERR_SEED_ALPHANUM =
        "Seed must be composed of alpha-numeric characters only.";
    private static final String ERR_SEED_SIZE =
        "Seed must 1 to 16 characters in length.";
    private static final String ERR_PASSPHRASE_SIZE =
        "Passphrase must be between 10 and 63 characters in length";                                                   
                                                  
    private static String getUserInput()
    {
        try {
            BufferedReader br =
                new BufferedReader(new InputStreamReader(System.in));

            return br.readLine();
        } catch (IOException ioe) {}

        return null;
    }

    private static String promptForAlgorithm()
    {
        boolean done = false;
        String algo = null;

        while (!done) {
            System.out.println(PROMPT_ALGORITHM);
            System.out.println(PROMPT_ALGORITHM_SUGGESTIONS);
            System.out.print(PROMPT_RETURN);

            algo = getUserInput();

            Algorithm a = SASLOTPProfile.getAlgorithm(algo);

            if (a == null) {
                System.out.println(ERR_ALGO_UNSUPPORTED + algo);
            } else {
                done = true;
            }
        }

        return algo;
    }

    private static String promptForPassphrase()
    {
        boolean done = false;
        String pass = null;

        while (!done) {
            System.out.println(PROMPT_PASSPHRASE);
            System.out.print(PROMPT_RETURN);

            pass = getUserInput();
            try
            {
                done = validatePassphrase(pass);
            }
            catch(Exception x)
            {
                System.out.println(x.getMessage());
            }
        }
        return pass;
    }

    private static String promptForSeed()
    {
        boolean done = false;
        String seed = null;

        while (!done) {
            System.out.println(PROMPT_SEED);
            System.out.print(PROMPT_RETURN);

            seed = getUserInput();
            try
            {
                done = validateSeed(seed);
            }
            catch(Exception x)
            {
                System.out.println(x.getMessage());
            }
        }

        return seed.toLowerCase();
    }

    private static String promptForSequence()
    {
        boolean done = false;
        String seq = null;

        while (!done) {
            System.out.println(PROMPT_SEQUENCE);
            System.out.print(PROMPT_RETURN);

            seq = getUserInput();
            done = validateSequence(seq);
        }

        return seq;
    }

    private static String promptForUsername()
    {
        System.out.println(PROMPT_USERNAME);
        System.out.print(PROMPT_RETURN);

        return getUserInput();
    }

    private static boolean validateUserName(String user)
    {
        boolean result = false;

        if (user == null) {
            return false;
        }

        try {
            Properties test = new Properties();

            test.load(new FileInputStream(user
                                          + UserDatabasePool.OTP_SUFFIX));
        } catch (Exception x) {
            return true;
        }

        return false;
    }

    static boolean validatePassphrase(String pp)
        throws SASLException
    {
        int length = pp.length();
        if(length < 10 || length > 63)
        {
            System.out.println("The length of "+length+" is invalid.");
            throw new SASLException(ERR_PASSPHRASE_SIZE);
        }
        return true;
    }

    static boolean validateSeed(String seed)
        throws SASLException
    {
        int length = seed.length();
        if(length <= 0 || length > 10)
        {
            System.out.println("The length of "+length+" is invalid.");
            throw new SASLException(ERR_SEED_SIZE);
        }
        for(int i = 0; i < length; i++)
        {
            char c = seed.charAt(i);

            // Unfortunately, they allow '$' and '_'
            if(!Character.isDigit(c) && !Character.isLetter(c))
            {
                System.out.println("The character=>"+seed.charAt(i)+"<= is invalid.");
                throw new SASLException(ERR_SEED_ALPHANUM);
            }
        }                
        return true;
    }

    static boolean validateSequence(String seq)
    {
        int i = 0;

        try {
            i = Integer.parseInt(seq);

            if (i < 0) {
                return false;
            }

            return true;
        } catch (Exception x) {
            return false;
        }
    }
        
    static void printHex(byte buff[])
    {
        System.out.println(SASLOTPProfile.convertBytesToHex(buff));
    }

    /**
     * Method main is the method used to run the OTP generator.
     * IT prompts the users for information necessary to create
     * the OTP database and validates as it goes.  Once it has
     * valid input, it produces the file.
     */
    public static void main(String argv[]) throws SASLException
    {
        SASLOTPProfile sop = new SASLOTPProfile();

        try {
            sop.init(SASLOTPProfile.URI, new ProfileConfiguration());
        } catch (BEEPException x) {}

        Properties p = new Properties();

        // Get username and check to make sure this won't clobber
        // existing OTP database information.
        String username = null;

        while (!validateUserName((username = promptForUsername())));

        // Get the passphrase, to only be used transiently
        String passphrase = null;

        passphrase = promptForPassphrase();

        // Get the seed, algorith, and sequence
        String seed = promptForSeed();
        String algo = promptForAlgorithm();
        String sequence = promptForSequence();

        // Generate the hash
        Algorithm a = SASLOTPProfile.getAlgorithm(algo);
        int limit = Integer.parseInt(sequence);
        byte hash[] = a.generateHash(seed + passphrase), temp[];

        for (int i = 0; i < limit; i++) {
            temp = a.generateHash(hash);
            hash = temp;
        }

        printHex(hash);

        passphrase = SASLOTPProfile.convertBytesToHex(hash);

        // Store the OTP db information
        try {
            p.put(UserDatabasePool.OTP_AUTHENTICATOR, username);
            p.put(UserDatabasePool.OTP_ALGO, algo);
            p.put(UserDatabasePool.OTP_LAST_HASH, passphrase);
            p.put(UserDatabasePool.OTP_SEQUENCE, sequence);
            p.put(UserDatabasePool.OTP_SEED, seed);
            p.store(new FileOutputStream(username + UserDatabasePool.OTP_SUFFIX),
                    UserDatabasePool.OTP_HEADER);
        } 
        catch (Exception x) 
        {
            throw new SASLException(x.getMessage());
        }
    }
}
