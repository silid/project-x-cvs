package org.apache.commons.net.ftp;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Commons" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

/***
 * FTPCommand stores a set of constants for FTP command codes.  To interpret
 * the meaning of the codes, familiarity with RFC 959 is assumed.
 * The mnemonic constant names are transcriptions from the code descriptions
 * of RFC 959.  For those who think in terms of the actual FTP commands,
 * a set of constants such as <a href="#USER"> USER </a> are provided
 * where the constant name is the same as the FTP command.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 ***/

public final class FTPCommand
{


    public static final int USER = 0;
    public static final int PASS = 1;
    public static final int ACCT = 2;
    public static final int CWD = 3;
    public static final int CDUP = 4;
    public static final int SMNT = 5;
    public static final int REIN = 6;
    public static final int QUIT = 7;
    public static final int PORT = 8;
    public static final int PASV = 9;
    public static final int TYPE = 10;
    public static final int STRU = 11;
    public static final int MODE = 12;
    public static final int RETR = 13;
    public static final int STOR = 14;
    public static final int STOU = 15;
    public static final int APPE = 16;
    public static final int ALLO = 17;
    public static final int REST = 18;
    public static final int RNFR = 19;
    public static final int RNTO = 20;
    public static final int ABOR = 21;
    public static final int DELE = 22;
    public static final int RMD = 23;
    public static final int MKD = 24;
    public static final int PWD = 25;
    public static final int LIST = 26;
    public static final int NLST = 27;
    public static final int SITE = 28;
    public static final int SYST = 29;
    public static final int STAT = 30;
    public static final int HELP = 31;
    public static final int NOOP = 32;

    public static final int USERNAME = USER;
    public static final int PASSWORD = PASS;
    public static final int ACCOUNT = ACCT;
    public static final int CHANGE_WORKING_DIRECTORY = CWD;
    public static final int CHANGE_TO_PARENT_DIRECTORY = CDUP;
    public static final int STRUCTURE_MOUNT = SMNT;
    public static final int REINITIALIZE = REIN;
    public static final int LOGOUT = QUIT;
    public static final int DATA_PORT = PORT;
    public static final int PASSIVE = PASV;
    public static final int REPRESENTATION_TYPE = TYPE;
    public static final int FILE_STRUCTURE = STRU;
    public static final int TRANSFER_MODE = MODE;
    public static final int RETRIEVE = RETR;
    public static final int STORE = STOR;
    public static final int STORE_UNIQUE = STOU;
    public static final int APPEND = APPE;
    public static final int ALLOCATE = ALLO;
    public static final int RESTART = REST;
    public static final int RENAME_FROM = RNFR;
    public static final int RENAME_TO = RNTO;
    public static final int ABORT = ABOR;
    public static final int DELETE = DELE;
    public static final int REMOVE_DIRECTORY = RMD;
    public static final int MAKE_DIRECTORY = MKD;
    public static final int PRINT_WORKING_DIRECTORY = PWD;
    //  public static final int LIST = LIST;
    public static final int NAME_LIST = NLST;
    public static final int SITE_PARAMETERS = SITE;
    public static final int SYSTEM = SYST;
    public static final int STATUS = STAT;
    //public static final int HELP = HELP;
    //public static final int NOOP = NOOP;

    // Cannot be instantiated
    private FTPCommand()
    {}

    static final String[] _commands = {
                                          "USER", "PASS", "ACCT", "CWD", "CDUP", "SMNT", "REIN", "QUIT", "PORT",
                                          "PASV", "TYPE", "STRU", "MODE", "RETR", "STOR", "STOU", "APPE", "ALLO",
                                          "REST", "RNFR", "RNTO", "ABOR", "DELE", "RMD", "MKD", "PWD", "LIST",
                                          "NLST", "SITE", "SYST", "STAT", "HELP", "NOOP"
                                      };


    /***
     * Retrieve the FTP protocol command string corresponding to a specified
     * command code.
     * <p>
     * @param The command code.
     * @return The FTP protcol command string corresponding to a specified
     *         command code.
     ***/
    public static final String getCommand(int command)
    {
        return _commands[command];
    }

}
