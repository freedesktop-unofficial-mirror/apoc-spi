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
package com.sun.apoc.spi.ldap.util;

import java.util.Calendar;
import java.util.TimeZone;

public class Timestamp
{
	private static final TimeZone sUTC = TimeZone.getTimeZone( "UTC" );

	public static final String	sEpoch			= "19700101000000Z";
	public static final long	sEpochMillis	= getMillis( sEpoch );

	public static String getTimestamp()
	{
		StringBuffer theBuffer = new StringBuffer( 15 );
		Calendar theCalendar = Calendar.getInstance( sUTC );
		theBuffer.append( theCalendar.get( Calendar.YEAR ) );
		padAppend( theBuffer, theCalendar.get( Calendar.MONTH ) + 1 );
		padAppend( theBuffer, theCalendar.get( Calendar.DAY_OF_MONTH ) );
		padAppend( theBuffer, theCalendar.get( Calendar.HOUR_OF_DAY ) );
		padAppend( theBuffer, theCalendar.get( Calendar.MINUTE ) );
		padAppend( theBuffer, theCalendar.get( Calendar.SECOND ) );
		theBuffer.append( "Z" );
		return theBuffer.toString();
	}

	public static long getMillis( String inTimestamp )
	{
		if ( inTimestamp == null )
		{
			return sEpochMillis;
		}
		Calendar theCalendar = Calendar.getInstance( sUTC );
		theCalendar.clear();
		theCalendar.set(
			Integer.parseInt( inTimestamp.substring( 0, 4 ) ),
			Integer.parseInt( inTimestamp.substring( 4, 6 ) ) - 1,
			Integer.parseInt( inTimestamp.substring( 6, 8 ) ),
			Integer.parseInt( inTimestamp.substring( 8, 10 ) ),
			Integer.parseInt( inTimestamp.substring( 10, 12 ) ),
			Integer.parseInt( inTimestamp.substring( 12, 14 ) ) );
		return theCalendar.getTimeInMillis();
	}

	public static boolean isNewer( String inTimestamp1, String inTimestamp2 )
	{
		String theTimestamp1 = inTimestamp1.substring( 0, 13 );
		String theTimestamp2 = inTimestamp2.substring( 0, 13 );
		return Long.parseLong( theTimestamp1 ) >
			   Long.parseLong( theTimestamp2 );
	}


	private static void padAppend( StringBuffer inBuffer, int inValue )
	{
		if ( inValue < 10 )
		{
			inBuffer.append( 0 );
		}
		inBuffer.append( inValue );
	}
}
