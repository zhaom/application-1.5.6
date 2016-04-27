/*
 * Copyright 2010, Mahmood Ali.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following disclaimer
 *     in the documentation and/or other materials provided with the
 *     distribution.
 *   * Neither the name of Mahmood Ali. nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.babeeta.butterfly.application.gateway.ios;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import com.google.protobuf.ByteString;

/**
 * Represents an APNS notification to be sent to Apple service.
 */
public class ApnsNotification {

	private final String uid;
	private final static byte COMMAND = 0;
	private final ByteString deviceToken;
	private final ByteString payload;

	/**
	 * The infinite future for the purposes of Apple expiry date
	 */
	public final static int MAXIMUM_EXPIRY = Integer.MAX_VALUE;

	/**
	 * The infinite future for the purposes of Apple expiry date
	 */
	public final static Date MAXIMUM_DATE = new Date(Integer.MAX_VALUE * 1000L);

	/**
	 * Constructs an instance of {@code ApnsNotification}.
	 * 
	 * The message encodes the payload with a {@code UTF-8} encoding.
	 * 
	 * @param dtoken
	 *            The Hex of the device token of the destination phone
	 * @param payload
	 *            The payload message to be sent
	 * @throws DecoderException
	 * @throws UnsupportedEncodingException
	 */
	public ApnsNotification(String uid,
			String dtoken, String payload) throws DecoderException {
		this.uid = uid;
		this.deviceToken = ByteString.copyFrom(Hex.decodeHex(dtoken
				.toCharArray()));
		this.payload = ByteString.copyFromUtf8(payload);
	}

	/**
	 * Returns the binary representation of the device token.
	 * 
	 */
	public ByteString getDeviceToken() {
		return deviceToken;
	}

	/**
	 * Returns the binary representation of the payload.
	 * 
	 */
	public ByteString getPayload() {
		return payload;
	}

	public String getUid() {
		return uid;
	}

	/**
	 * Returns the length of the message in bytes as it is encoded on the wire.
	 * 
	 * Apple require the message to be of length 255 bytes or less.
	 * 
	 * @return length of encoded message in bytes
	 */
	public int length() {
		int length = 1 + 4 + 4 + 2 + deviceToken.size() + 2 + payload.size();
		return length;
	}

	/**
	 * Returns the binary representation of the message as expected by the APNS
	 * server.
	 * 
	 * The returned array can be used to sent directly to the APNS server (on
	 * the wire/socket) without any modification.
	 */
	public byte[] marshall() {
		ByteArrayOutputStream boas = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(boas);

		try {
			dos.writeByte(COMMAND);
			dos.writeShort(deviceToken.size());
			dos.write(deviceToken.toByteArray());
			dos.writeShort(payload.size());
			dos.write(payload.toByteArray());
			return boas.toByteArray();
		} catch (IOException e) {
			throw new AssertionError();
		}
	}

}