/*
 * PureTLSPackageBridge.java  $Rev$
 *
 * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
 *
 * The contents of this file are subject to the Blocks Public License (the
 * "License"); You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.invisible.net/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 */
package COM.claymoresystems.ptls;

import java.security.PrivateKey;
import java.util.Vector;

/**
 * this is a hack that allows access to the data members of certain PureTLS
 * classes but since I'm not allowed to change the PureTLS source code (I know,
 * it's Open Source, but boss' orders are boss' orders.)
 */
public class PureTLSPackageBridge {

	public static void setPrivateKey( SSLContext ctx, PrivateKey key ) {
		ctx.privateKey = key;
	}

	public static void initCertificates( SSLContext ctx ) {
		ctx.certificates = new Vector();
	}

	public static void addCertificate( SSLContext ctx, byte[] cert ) {
		if( ctx.certificates == null )
			ctx.certificates = new Vector();
		ctx.certificates.add( cert );
	}

	public static void initRootCertificates( SSLContext ctx ) {
		ctx.root_list = new Vector();
	}

	public static void addRootCertificate( SSLContext ctx, byte[] cert ) {
		if( ctx.certificates == null )
			ctx.root_list = new Vector();
		ctx.root_list.add( cert );
	}
}
