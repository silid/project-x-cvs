package org.apache.commons.net.ftp.parser;

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
 
import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestSuite;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;

/**
 * @author <a href="mailto:scohen@apache.org">Steve Cohen</a>
 * @author <a href="sestegra@free.fr">Stephane ESTE-GRACIAS</a>
 * @version $Id$
 */
public class VMSFTPEntryParserTest extends FTPParseTestFramework
{
    private static final String[] badsamples = 
    {

        "1-JUN.LIS;1              9/9           2-jun-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)", 
        "1-JUN.LIS;2              9/9           JUN-2-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,)", 
        "1-JUN.LIS;2              a/9           2-JUN-98 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,)", 
        "DATA.DIR; 1              1/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (,RWED,RWED,RE)", 
        "120196.TXT;1           118/126        14-APR-1997 12:45:27 PM  [GROUP,OWNER]    (RWED,,RWED,RE)", 
        "30CHARBAR.TXT;1         11/18          2-JUN-1998 08:38:42  [GROUP-1,OWNER]    (RWED,RWED,RWED,RE)", 
        "A.;2                    18/18          1-JUL-1998 08:43:20  [GROUP,OWNER]    (RWED2,RWED,RWED,RE)", 
        "AA.;2                  152/153        13-FED-1997 08:13:43  [GROUP,OWNER]    (RWED,RWED,RWED,RE)",
        "Directory USER1:[TEMP]\r\n\r\n",
        "\r\nTotal 14 files"
    };
    
    private static final String[] goodsamples = 
    {		
        "1-JUN.LIS;1              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)", 
        "1-JUN.LIS;3              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,)", 
        "1-JUN.LIS;2              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,)", 
        "DATA.DIR;1               1/9           2-JUN-1998 07:32:04  [TRANSLATED]     (,RWED,RWED,RE)", 
        "120196.TXT;1           118/126        14-APR-1997 12:45:27  [GROUP,OWNER]    (RWED,,RWED,RE)", 
        "30CHARBAR.TXT;1         11/18          2-JUN-1998 08:38:42  [GROUP,OWNER]    (RWED,RWED,RWED,RE)", 
        "A.;2                    18/18          1-JUL-1998 08:43:20  [GROUP,OWNER]    (RWED,RWED,RWED,RE)", 
        "AA.;2                  152/153        13-FEB-1997 08:13:43  [GROUP,OWNER]    (RWED,RWED,RWED,RE)",
        "UCX$REXECD_STARTUP.LOG;1098\r\n                         4/15         24-FEB-2003 13:17:24  [POSTWARE,LP]    (RWED,RWED,RE,)",
        "UNARCHIVE.COM;1          2/15          7-JUL-1997 16:37:45  [POSTWARE,LP]    (RWE,RWE,RWE,RE)",
        "UNXMERGE.COM;15          1/15         20-AUG-1996 13:59:50  [POSTWARE,LP]    (RWE,RWE,RWE,RE)",
        "UNXTEMP.COM;7            1/15         15-AUG-1996 14:10:38  [POSTWARE,LP]    (RWE,RWE,RWE,RE)",
        "UNZIP_AND_ATTACH_FILES.COM;12\r\n                        14/15         24-JUL-2002 14:35:40  [TRANSLATED]    (RWE,RWE,RWE,RE)",
        "UNZIP_AND_ATTACH_FILES.SAV;1\r\n                        14/15         17-JAN-2002 11:13:53  [POSTWARE,LP]    (RWE,RWED,RWE,RE)"        
    };
    
    private static final String fullListing = "Directory USER1:[TEMP]\r\n\r\n"+
    "1-JUN.LIS;1              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)\r\n"+ 
    "2-JUN.LIS;1              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,)\r\n"+ 
    "3-JUN.LIS;1              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,)\r\n"+
    "\r\nTotal 3 files"; 
    
    /**
     * @see junit.framework.TestCase#TestCase(String)
     */
    public VMSFTPEntryParserTest(String name)
    {
        super(name);
    }  

    /**
     * Test the legacy interface for parsing file lists.
     * @throws IOException
     */
    public void testParseFileList() throws IOException
    {        
        VMSFTPEntryParser parser = new VMSFTPEntryParser();
        FTPFile[] files = parser.parseFileList(new ByteArrayInputStream(fullListing.getBytes()));
        assertEquals(3, files.length);
        assertEquals(files[0].getName(), "2-JUN.LIS", files[0].getName());
        assertEquals(files[1].getName(), "3-JUN.LIS", files[1].getName());
        assertEquals(files[2].getName(), "1-JUN.LIS", files[2].getName());        
    }

    /**
     * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#testParseFieldsOnDirectory()
     */
    public void testParseFieldsOnDirectory() throws Exception
    {

        FTPFile dir = getParser().parseFTPEntry("DATA.DIR;1               1/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)");
        assertTrue("Should be a directory.", 
                   dir.isDirectory());
        assertEquals("DATA.DIR", 
                     dir.getName());
        assertEquals(512, 
                     dir.getSize());
        assertEquals("Tue Jun 02 07:32:04 1998", 
                     df.format(dir.getTimestamp().getTime()));
        assertEquals("GROUP", 
                     dir.getGroup());
        assertEquals("OWNER", 
                     dir.getUser());
        checkPermisions(dir);
        
        
        dir = getParser().parseFTPEntry("DATA.DIR;1               1/9           2-JUN-1998 07:32:04  [TRANSLATED]    (RWED,RWED,RWED,RE)");
        assertTrue("Should be a directory.", 
                           dir.isDirectory());
        assertEquals("DATA.DIR", 
                             dir.getName());
        assertEquals(512, 
                             dir.getSize());
        assertEquals("Tue Jun 02 07:32:04 1998", 
                             df.format(dir.getTimestamp().getTime()));
        assertEquals(null, 
                     dir.getGroup());
        assertEquals("TRANSLATED", 
                     dir.getUser());
        checkPermisions(dir);
    }

    /**
     * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#testParseFieldsOnFile()
     */
    public void testParseFieldsOnFile() throws Exception
    {
        FTPFile file = getParser().parseFTPEntry("1-JUN.LIS;1              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)");
        assertTrue("Should be a file.", 
                   file.isFile());
        assertEquals("1-JUN.LIS", 
                     file.getName());
        assertEquals(9 * 512, 
                     file.getSize());
        assertEquals("Tue Jun 02 07:32:04 1998", 
                     df.format(file.getTimestamp().getTime()));
        assertEquals("GROUP", 
                     file.getGroup());
        assertEquals("OWNER", 
                     file.getUser());
        checkPermisions(file);
        
        
        file = getParser().parseFTPEntry("1-JUN.LIS;1              9/9           2-JUN-1998 07:32:04  [TRANSLATED]    (RWED,RWED,RWED,RE)");
        assertTrue("Should be a file.", 
                   file.isFile());
        assertEquals("1-JUN.LIS", 
                     file.getName());
        assertEquals(9 * 512, 
                     file.getSize());
        assertEquals("Tue Jun 02 07:32:04 1998", 
                     df.format(file.getTimestamp().getTime()));
        assertEquals(null, 
                     file.getGroup());
        assertEquals("TRANSLATED", 
                     file.getUser());
        checkPermisions(file);
    }

    /**
     * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#getBadListing()
     */
    protected String[] getBadListing()
    {

        return (badsamples);
    }

    /**
     * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#getGoodListing()
     */
    protected String[] getGoodListing()
    {

        return (goodsamples);
    }

    /**
     * @see org.apache.commons.net.ftp.parser.FTPParseTestFramework#getParser()
     */
    protected FTPFileEntryParser getParser()
    {

        return (new VMSFTPEntryParser());
    }

    /**
     * Method checkPermisions.
     * Verify that the VMS parser does NOT  set the permissions.
     * @param dir
     */
    private void checkPermisions(FTPFile dir)
    {
        assertTrue("Owner should not have read permission.", 
                   !dir.hasPermission(FTPFile.USER_ACCESS, 
                                      FTPFile.READ_PERMISSION));
        assertTrue("Owner should not have write permission.", 
                   !dir.hasPermission(FTPFile.USER_ACCESS, 
                                      FTPFile.WRITE_PERMISSION));
        assertTrue("Owner should not have execute permission.", 
                   !dir.hasPermission(FTPFile.USER_ACCESS, 
                                      FTPFile.EXECUTE_PERMISSION));
        assertTrue("Group should not have read permission.", 
                   !dir.hasPermission(FTPFile.GROUP_ACCESS, 
                                      FTPFile.READ_PERMISSION));
        assertTrue("Group should not have write permission.", 
                   !dir.hasPermission(FTPFile.GROUP_ACCESS, 
                                      FTPFile.WRITE_PERMISSION));
        assertTrue("Group should not have execute permission.", 
                   !dir.hasPermission(FTPFile.GROUP_ACCESS, 
                                      FTPFile.EXECUTE_PERMISSION));
        assertTrue("World should not have read permission.", 
                   !dir.hasPermission(FTPFile.WORLD_ACCESS, 
                                      FTPFile.READ_PERMISSION));
        assertTrue("World should not have write permission.", 
                   !dir.hasPermission(FTPFile.WORLD_ACCESS, 
                                      FTPFile.WRITE_PERMISSION));
        assertTrue("World should not have execute permission.", 
                   !dir.hasPermission(FTPFile.WORLD_ACCESS, 
                                      FTPFile.EXECUTE_PERMISSION));
    }
    
    /**
     * Method suite.
     * @return TestSuite
     */
    public static TestSuite suite()
    {
        return(new TestSuite(VMSFTPEntryParserTest.class));
    }
}
