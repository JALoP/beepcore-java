/*
 * RequestHandler.java  $Revision: 1.2 $ $Date: 2003/09/15 15:23:30 $
 *
 * Copyright (c) 2003 Huston Franklin.  All rights reserved.
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
package org.beepcore.beep.core;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import org.beepcore.beep.util.BufferSegment;

/**
 * This class is used to wrap piggybacked data from the start channel request.
 *
 * @author Huston Franklin
 * @version $Revision: 1.2 $, $Date: 2003/09/15 15:23:30 $
 */
class PiggybackedMSG extends MessageMSGImpl implements MessageMSG {

    PiggybackedMSG(ChannelImpl channel, byte[] data, boolean base64encoding)
    {
        super(channel, -1, new InputDataStream());
        try {
            this.getDataStream().add(new BufferSegment("\r\n".getBytes("UTF-8"))); 
        } catch (UnsupportedEncodingException e) {
            this.channel.getSession().terminate("UTF-8 not supported");
            return;
        }
        this.getDataStream().add(new BufferSegment(data));
        this.getDataStream().setComplete();
    }
    
	public MessageStatus sendANS(OutputDataStream stream)
		throws BEEPException
    {
        throw new BEEPException("ANS reply not valid for piggybacked requests");
	}

	public MessageStatus sendERR(BEEPError error) throws BEEPException {
        throw new BEEPException("ERR reply not valid for piggybacked requests");
	}

	public MessageStatus sendERR(int code, String diagnostic)
		throws BEEPException
    {
        throw new BEEPException("ERR reply not valid for piggybacked requests");
	}

	public MessageStatus sendERR(int code, String diagnostic, String xmlLang)
		throws BEEPException
    {
        throw new BEEPException("ERR reply not valid for piggybacked requests");
	}

	public MessageStatus sendNUL() throws BEEPException {
        throw new BEEPException("ANS reply not valid for piggybacked requests");
	}

	public MessageStatus sendRPY(OutputDataStream stream)
		throws BEEPException
    {
        SessionImpl s = (SessionImpl)this.channel.getSession();
        ByteArrayOutputStream tmp =
            new ByteArrayOutputStream(SessionImpl.MAX_PCDATA_SIZE);
        String data;

        while (stream.availableSegment()) {
            BufferSegment b = stream.getNextSegment(SessionImpl.MAX_PCDATA_SIZE);

            tmp.write(b.getData(), 0, b.getLength());
        }

        try {
            data = tmp.toString("UTF-8");
            int crlf = data.indexOf("\r\n");
            if (crlf != -1) {
                data = data.substring(crlf + "\r\n".length());
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not supported");
        }

        try {
            s.sendProfile(this.channel.getProfile(), data, this.channel);
        } catch (BEEPException e) {
            s.terminate("Error sending profile. " + e.getMessage());
            throw e;
        }

        if (this.channel.getState() != ChannelImpl.STATE_TUNING) {
            this.channel.setState(ChannelImpl.STATE_ACTIVE);
            ((SessionImpl)this.channel.getSession()).enableIO();
        }

        s.fireChannelStarted(this.channel);
        
        return new MessageStatus(this.channel, Message.MESSAGE_TYPE_RPY, -1,
                                 stream);
	}
}
