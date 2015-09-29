/*
 * Copyright 2013, Scott Douglass <scott@swdouglass.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * on the World Wide Web for more details:
 * http://www.fsf.org/licensing/licenses/gpl.txt
 */
package com.spacepirates.minecraft.rcon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author scott
 */
public class RconProtocol {

  private static final Logger LOG = Logger.getLogger(RconProtocol.class.getName());
  private String host = "localhost";
  private Integer port = 25575;
  private final Random random = new Random();
  private Integer requestId = random.nextInt();
  private Socket socket = null;
  private InputStream inputStream = null;
  private OutputStream outputStream = null;
  //  http://wiki.vg/Rcon#Implementation_details
  private static final int REQUESTID_LENGTH = 4; // int = 32 bits = 4 bytes
  private static final int PAD_LENGTH = 2;       // 2 bytes
  private static final int TYPE_LENGTH = 4;      // int = 32 bits = 4 bytes
  private static final int LENGTH_LENGTH = 4;    // int = 32 bits = 4 bytes
  private static final byte[] PAD_BYTES = new byte[]{(byte) 0, (byte) 0};
  private static final int MAX_REQUEST_LENGTH = 1460;
  private static final int AUTH_FAILED = -1;

  /**
   * @return the requestId
   */
  public Integer getRequestId() {
    return requestId;
  }

  /**
   * @param requestId the requestId to set
   */
  public void setRequestId(Integer requestId) {
    this.requestId = requestId;
  }

  public enum Command {

    LOGIN(3),
    RUN(2);
    private int type;

    private Command(final int inType) {
      this.type = inType;
    }
  }

  /**
   * Class to encapsulate the attributes of an Rcon protocol message.
   */
  public static class RconMessage {

    int length;
    int requestId;
    int type;
    byte[] payload;
    ByteBuffer buffer;
    byte[] message;

    /**
     * Read from an InputStream and decode the bytes to populate this
     * RconMessage.
     *
     * @param inStream
     * @throws IOException
     */
    RconMessage(final InputStream inStream)
      throws IOException {
      final byte[] receivedBytes = new byte[MAX_REQUEST_LENGTH];
      final int receivedBytesLength = inStream.read(receivedBytes);
      buffer = ByteBuffer.wrap(receivedBytes, 0, receivedBytesLength);
      buffer.order(ByteOrder.LITTLE_ENDIAN);
      length = buffer.getInt();   // Length
      requestId = buffer.getInt();// Request ID
      type = buffer.getInt();     // Type
      payload = new byte[length - REQUESTID_LENGTH - TYPE_LENGTH - PAD_LENGTH];
      buffer.get(payload);
      buffer.get(new byte[2]); // the 2-byte pad for end of message
    }

    /**
     * Create a command type RconMessage with a given requestId and payload.
     *
     * @param inRequestID
     * @param inPayload
     */
    RconMessage(final Integer inRequestID, final String inPayload) {
      encode(Command.RUN, inRequestID, inPayload);
    }

    /**
     * Create an RconMessage for any type of command, with given command type,
     * requestId and payload.
     *
     * @param inType
     * @param inRequestID
     * @param inPayload
     */
    RconMessage(final Command inType, final Integer inRequestID, final String inPayload) {
      encode(inType, inRequestID, inPayload);
    }

    final void encode(final Command inType, final Integer inRequestID, final String inPayload) {
      payload = inPayload.getBytes(StandardCharsets.US_ASCII);
      length = REQUESTID_LENGTH + TYPE_LENGTH + payload.length + PAD_LENGTH;
      type = inType.type;
      message = new byte[LENGTH_LENGTH + length];
      buffer = ByteBuffer.wrap(message);
      buffer.order(ByteOrder.LITTLE_ENDIAN);
      buffer.putInt(length);
      buffer.putInt(inRequestID);
      buffer.putInt(type);
      buffer.put(payload);
      buffer.put(PAD_BYTES);
    }

    /**
     * Generate a nice JSON version of the RconMessage.
     *
     * @return JSON string of message attributes
     */
    @Override
    public String toString() {
      StringBuilder json = new StringBuilder();
      json.append("{ \"message\": \n  {\n    \"requestId\": \"");
      json.append(requestId);
      json.append("\",\n    \"type\": \"");
      json.append(type);
      json.append("\",\n    \"payload\": \"");
      json.append(new String(payload, StandardCharsets.US_ASCII));
      json.append("\"\n  }\n}");
      return json.toString();
    }
  }

  /**
   * Default no argument constructor to use RconProtocol as a bean.
   */
  public RconProtocol() {
    // default no arg constructor
  }

  /**
   * Construct and RconProtocol which will connect to the provided host and
   * port.
   *
   * @param inHost
   * @param inPort
   */
  public RconProtocol(final String inHost, final Integer inPort) {
    this.host = inHost;
    this.port = inPort;
  }

  /**
   * Construct a RconProtocol and login to the server too.
   *
   * @param inHost
   * @param inPort
   * @param inPassword
   */
  public RconProtocol(final String inHost, final Integer inPort, final String inPassword) {
    this.host = inHost;
    this.port = inPort;
    authenticate(inPassword);
  }

  /**
   * Send the authentication message to the Mineraft server. This will return a
   * message with requestId = -1 on failure.
   *
   * @param inPassword
   * @return the request ID from the response message
   */
  public final Integer authenticate(final String inPassword) {
    int result = 0;
    try {
      LOG.log(Level.INFO, "Logging in...");
      final RconMessage response = send(Command.LOGIN, inPassword);
      result = response.requestId;
      if (AUTH_FAILED == response.requestId) {
        LOG.log(Level.SEVERE, "Login failed");
        //throw new RuntimeException("Login failed");
      }
    } catch (IOException ex) {
      LOG.log(Level.SEVERE, "Connection to server failed.", ex);
    }
    return result;
  }

  /**
   * Send a command message.
   *
   * @param inPayload
   * @return the response RconMessage
   * @throws IOException
   */
  public RconMessage send(final String inPayload) throws IOException {
    return send(Command.RUN, inPayload);
  }

  /**
   * Send any supported message type.
   *
   * @param inType
   * @param inPayload
   * @return the response RconMessage
   * @throws IOException
   */
  public RconMessage send(final Command inType, final String inPayload)
    throws IOException {
    return send(new RconMessage(inType, this.getRequestId(), inPayload));
  }

  public synchronized RconMessage send(final RconMessage inMessage)
  throws IOException {
    if (this.socket == null) {
      this.socket = new Socket(this.host, this.port);
      this.outputStream = this.socket.getOutputStream();
      this.inputStream = this.socket.getInputStream();
    }

    this.outputStream.write(inMessage.message);
    this.outputStream.flush();

    final RconMessage response = new RconMessage(this.inputStream);
    if (response.requestId != this.requestId) {
      LOG.log(Level.SEVERE, "request and response IDs did not match");
      //throw new RuntimeException("Communication is broken");
    }

    return response;
  }
}
