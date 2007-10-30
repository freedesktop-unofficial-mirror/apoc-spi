/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either
 * the GNU General Public License Version 2 only ("GPL") or
 * the Common Development and Distribution License("CDDL")
 * (collectively, the "License"). You may not use this file
 * except in compliance with the License. You can obtain a copy
 * of the License at www.sun.com/CDDL or at COPYRIGHT. See the
 * License for the specific language governing permissions and
 * limitations under the License. When distributing the software,
 * include this License Header Notice in each file and include
 * the License file at /legal/license.txt. If applicable, add the
 * following below the License Header, with the fields enclosed
 * by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by
 * only the CDDL or only the GPL Version 2, indicate your
 * decision by adding "[Contributor] elects to include this
 * software in this distribution under the [CDDL or GPL
 * Version 2] license." If you don't indicate a single choice
 * of license, a recipient has the option to distribute your
 * version of this file under either the CDDL, the GPL Version
 * 2 or to extend the choice of license to its licensees as
 * provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the
 * option applies only if the new code is made subject to such
 * option by the copyright holder.
 */

package com.sun.apoc.spi.ldap.authentication;

import java.io.InputStream;
import java.io.OutputStream;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

import com.netscape.sasl.SaslClient;
import com.netscape.sasl.SaslException;


public class SaslMechanism implements SaslClient
{
	public static final String sMech					= "GSSAPI";

	private static short				sSequenceMax	= 3;
	private short						mSequence		= 0;
	private CallbackHandler				mHandler;
	private LdapSaslGSSAPICallback[]	mCallbacks		=
		new LdapSaslGSSAPICallback[ 1 ];

	public SaslMechanism( String inHostName, CallbackHandler inHandler )
		throws IllegalArgumentException
	{
		if ( inHandler == null )
		{
			throw new IllegalArgumentException();
		}
		mCallbacks[ 0 ]	= new LdapSaslGSSAPICallback( inHostName );
		mHandler		= inHandler;
	}

	public String getMechanismName() { return sMech; }

	public InputStream getInputStream( InputStream inInputStream )
		throws java.io.IOException 
    { return inInputStream; }

    public OutputStream getOutputStream( OutputStream inOutputStream )
		throws java.io.IOException
    { return inOutputStream; }

    public byte[] createInitialResponse()
		throws SaslException
	{
		try
		{
			mHandler.handle( ( Callback[] )mCallbacks );
			++ mSequence;
			return mCallbacks[ 0 ].getResponse();
		}
		catch( Exception theException )
		{
			throw new SaslException();
		}
	}

    public byte[] evaluateChallenge( byte[] inChallenge )
		throws SaslException
	{
		try
		{
			if ( isComplete() )
			{
				return null;
			}
			else
			{	
				mCallbacks[ 0 ].setChallenge( inChallenge );	
				mHandler.handle( ( Callback[] )mCallbacks );	
				++ mSequence;
				return mCallbacks[ 0 ].getResponse();
			}
		}
		catch( Exception theException )
		{
			throw new SaslException();
		}
	}

    public boolean isComplete() { return mSequence >= sSequenceMax; }
}
