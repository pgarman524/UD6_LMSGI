/*
 *    Qizx Free_Engine-4.4p1
 *
 *    This code is part of the Qizx application components
 *    Copyright (c) 2004-2010 Axyana Software -- All rights reserved.
 *
 *    For conditions of use, see the accompanying license files.
 */
package com.qizx.server.api;

import com.qizx.api.Library;
import com.qizx.api.LibraryMember;
import com.qizx.api.QizxException;
import com.qizx.server.util.QizxRequestBase;
import com.qizx.server.util.RequestException;
import com.qizx.util.basic.PathUtil;

import java.io.IOException;

/**
 * Move or rename a Document or a Collection.
 */
public class MoveRequest extends QizxRequestBase
{
    public String getName()
    {
        return "move";
    }

    public void handlePost()
        throws RequestException, IOException
    {
        String libName = getLibraryParam();

        String srcPath = getParameter("src");
        String dstPath = getParameter("dst");

        try {
            Library lib = acquireSession(libName);
            response.setContentType(MIME_PLAIN_TEXT);
            
            LibraryMember src = requireMember(lib, srcPath);
            LibraryMember dst = lib.getMember(dstPath);
            LibraryMember result = null;
            
            if(src.isCollection()) {
                if(dst == null)
                    result = lib.renameMember(srcPath, dstPath);
                else if(dst.isCollection()) {
                    dstPath = PathUtil.makePath(dstPath,
                                                PathUtil.getBaseName(srcPath));
                    result = lib.renameMember(srcPath, dstPath);
                }
                else throw new RequestException(XML_DATA, "cannot rename Collection to Document");
            }
            else {
                if(dst != null) {
                    if(dst.isCollection()) {
                        dstPath = PathUtil.makePath(dstPath,
                                                    PathUtil.getBaseName(srcPath));
                        dst = lib.getMember(dstPath);
                    }
                    if(dst != null) {
                        dst.delete(); 
                    }
                }
                result = lib.renameMember(srcPath, dstPath);             
            }
            lib.commit();
            if(result != null)
                println(result.getPath());
        }
        catch (QizxException e) {
            throw new RequestException(e);
        }
    }
}
