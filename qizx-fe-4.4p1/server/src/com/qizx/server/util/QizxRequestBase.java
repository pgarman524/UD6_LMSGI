/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.server.util;

import com.qizx.api.*;
import com.qizx.api.util.logging.Statistics;
import com.qizx.util.TableGenerator;

import com.xmlmind.multipartreq.Part;

import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

/**
 * Superclass of requests using a Qizx Library connection.
 */
public abstract class QizxRequestBase extends Request
{    
    public static final String MIME_QIZX_ERROR = "text/x-qizx-error; charset=UTF-8";
    public static final String JSON = "json";

    // Qizx specific error codes
    protected static final String ACCESS = "AccessControl";
    protected static final String XML_DATA = "XMLData";
    protected static final String COMPILATION = "Compilation";
    protected static final String EVALUATION = "Evaluation";
    protected static final String TIMEOUT = "TimeOut";
    
    public    static final String MIME_PROPERTY = "content-type";

    protected QizxDriver driver;
    protected Library libSession;
    protected Part currentPart;

    private long startTime;

    
    protected QizxDriver getDriver()
    {
        if(driver == null) {
            driver = QizxDriver.initialize(servlet);
        }
        return driver;
    }
    
    public void setup()
    {
        driver = getDriver();
        libSession = null;
        startTime = System.nanoTime();
    }
    
    public void cleanup()
    {
        if(driver == null)
            return;
        Statistics.Activity ac = driver.getActivityStats(getName());
        long endTime = System.nanoTime();
        ac.addTime(endTime - startTime);
        
        if(libSession != null) {
            driver.releaseSession(libSession);
            libSession = null;
        }
    }

    protected void sendError(RequestException he)
        throws IOException
    {
        if(!response.isCommitted())
            response.reset();

        // define the content-type and error code: can be redefined
        startErrorContent();
        
        // handle Qizx exceptions:
        Throwable cause = he.getCause();
        if(cause instanceof CompilationException)
        {
            CompilationException ce = (CompilationException) cause;
            println(COMPILATION + ": " + ce.getMessage());
            Message[] errors = ce.getMessages();
            if (errors != null)
                for (int i = 0; i < errors.length; i++) {
                    Message m = errors[i];
                    if (m.getType() != Message.DETAIL)
                        println("line " + m.getLineNumber()
                                + " column " + m.getColumnNumber() 
                                + ":\n " + m.getText());
                    else
                        println("  " + m.getText());
                }
        }
        else if(cause instanceof EvaluationException) {
            EvaluationException ee = (EvaluationException) cause;
            print(EVALUATION + ": ");
            QName code = ee.getErrorCode();
            print(code != null? code.getLocalPart() : "<unknown code>");
            println(" " + ee.getMessage());

            EvaluationStackTrace[] stack = ee.getStack();
            for (int i = 0; i < stack.length; ++i) {
                EvaluationStackTrace frame = stack[i];
                String sig = frame.getSignature();
                sig = (sig == null)? "" : ("in " + sig);
                print(sig + " at line " + frame.getLineNumber()
                          + " column " + frame.getColumnNumber());
                if(frame.getModuleURI() != null)
                    print(" in " + frame.getModuleURI());
                println("");
            }
        }
        else if(cause instanceof AccessControlException) {
            AccessControlException dme = (AccessControlException) cause;
            print((he.code == null? ACCESS : he.code) + ": " + dme.getMessage());
        }
        else if(cause instanceof DataModelException) {
            DataModelException dme = (DataModelException) cause;
            print((he.code == null? XML_DATA : he.code) + ": " + dme.getMessage());
        }
        else {  // standard error
            println(he.code + ": " + he.getMessage());
            if(cause != null) {
                println("cause: " + cause);
                for (StackTraceElement f : cause.getStackTrace()) {
                    println(" " + f);
                }
            }
        }
        
        endErrorContent();
    }

    protected void startErrorContent() throws IOException
    {
        // for best compatibility with broken clients (eg Flex), no HTTP error
        // but a specific content-type
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MIME_QIZX_ERROR);
    }

    protected void endErrorContent() throws IOException
    {
    }


    protected QizxDriver requireQizxDriver()
        throws RequestException
    {
        QizxDriver drv = getDriver();
        if (drv == null)
            throw new RequestException(XML_DATA, "XML server is not active");
        return drv;
    }

    /**
     * Access to Qizx engine.
     * @param libraryName if null, there must be only one Library in the server
     * @param userName can be null if no authentication
     * @return a Library session ready to use for this User.
     */
    protected synchronized LibraryManager requireEngine ()
        throws RequestException, DataModelException
    {
        return requireQizxDriver().requireEngine();
    }

    /**
     * Get a ready Library session.
     * <p>Session is released automatically by cleanup()
     * @param libraryName if null, there must be only one Library in the server
     * @return a Library session ready to use for this User.
     */
    protected Library acquireSession (String libraryName)
        throws RequestException, DataModelException
    {
        Library session =
            requireQizxDriver().acquireSession(libraryName, getUserName(), this);
        libSession = session;
        return session;
    }

    protected void checkAdminRole(QizxDriver driver)
        throws RequestException
    {
        if (!driver.hasAdminRole(getUserName(), request))
            throw new RequestException(ACCESS,
                         "administrator privilege required for this operation");
    }

    public boolean isInRole(String roleName, User user)
    {
        //  user param is supposed to correspond to request user
        return request.isUserInRole(roleName);
    }

    /**
     * Returns the 'path' parameter, getting URL path if absent.
     */
    protected String getPathParam()
    {
        String path = getParameter("path");
        if (path == null) {
            path = request.getPathInfo();
        }
        return path;
    }

    /**
     * Returns the 'library' parameter, detecting void value.
     */
    protected String getLibraryParam()
    {
        String lib = getParameter("library");
        if(lib == null)
            return null;
        lib = lib.trim();
        return (lib.length() == 0)? null : lib;
    }
    
    protected LibraryMember requireMember(Library lib, String path)
        throws DataModelException, RequestException
    {
        if(path == null)
            throw new RequestException(BAD_REQUEST, "parameter 'path' should be defined");
        LibraryMember member = lib.getMember(path);
        if(member == null) {
            throw new RequestException(NOT_FOUND, "Library member '" + path +
                                       "' not found in library " + lib.getName());
        }
        return member;
    }
    
    protected HashSet<String> parseNameList(String names)
    {
        if(names == null)
            return null;
        HashSet<String> set = new HashSet<String>();
        String[] nm = names.split("[ \t\n,;:]+");
        for (int i = 0; i < nm.length; i++)
            set.add(nm[i]);
        return set;
    }

    protected TableGenerator prepareTableFormat(String format, String[] headers)
    {
        TableGenerator gen = new TableGenerator(headers);
        if(JSON.equalsIgnoreCase(format)) {
            gen.setJsonFormat("records");
            response.setContentType(MIME_JSON);
        }
        else {
            response.setContentType(MIME_PLAIN_TEXT);
        }
        return gen;
    }
    
    // Get XML data either for file part or from String parameter
    protected InputSource openXMLSource(String paramName)
        throws RequestException
    {
        try {
            currentPart = getPart(paramName);
            
            if(currentPart == null) {
                String data = getParameter(paramName);
                if(data == null)
                    return null;
                return new InputSource(new StringReader(data));
            }
            
            InputStream stream = currentPart.getInputStream();
            InputSource source = new InputSource(stream);
            return source;
        }
        catch (ServletException e) {
            throw new RequestException(SERVER, e);
        }
        catch (IOException e) {
            throw new RequestException(SERVER, e);
        }
    }
}
