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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author scott
 */
public class RconProtocolTest {

  public RconProtocolTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of authenticate method, of class RconProtocol.
   */
  @Test
  public void testAuthenticate() {
    System.out.println("authenticate");
    String inPassword = "crucible";
    RconProtocol instance = new RconProtocol("192.168.117.182", 32004);
    instance.setRequestId(12345);
    Integer expResult = 12345;
    Integer result = instance.authenticate(inPassword);
    assertEquals(expResult, result);
  }

  @Test
  public void testAuthenticationFailed() {
    System.out.println("authentication fails on purpose");
    String inPassword = "crucibleX";
    RconProtocol instance = new RconProtocol("192.168.117.182", 32004);
    instance.setRequestId(12345);
    Integer expResult = -1;
    Integer result = instance.authenticate(inPassword);
    assertEquals(expResult, result);
  }

  /**
   * Test of send method, of class RconProtocol.
   */
  /*@Test
  public void testSend() throws Exception {
    System.out.println("send");
    RconProtocol.Command inType = null;
    String inPayload = "";
    RconProtocol instance = null;
    RconProtocol.RconMessage expResult = null;
    RconProtocol.RconMessage result = instance.send(inType, inPayload);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }*/
}