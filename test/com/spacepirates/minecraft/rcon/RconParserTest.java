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

import com.spacepirates.minecraft.rcon.RconProtocol.RconMessage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
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
public class RconParserTest {

  private static RconProtocol rcon;
  public RconParserTest() {
  }

  @BeforeClass
  public static void setUpClass() {
    rcon = new RconProtocol("192.168.117.182", 32004, "crucible");
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
   * Test of parseWhiteList method, of class RconParser.
   */
  @Test
  public void testParseWhiteList() throws IOException {
    System.out.println("parse the white list to get just the players");
    List expResult = Arrays.asList(new String[] {"patch_n00b", "baseballfriend",
      "cube_wiz", "mineparty60", "rubinator2002", "aidan285", "earthbenderbd",
      "oliester11", "nyavatar", "mice47", "mc_pickaxe",
      "rodgerrafter", "maryally", "roleplaymax"});

    RconMessage response = rcon.send("whitelist list");
    List result = RconParser.parseList(new String(response.payload,"US-ASCII"));

    assertEquals(expResult, result);
  }

  /*@Test
  public void testParsePlayerList() throws IOException {
    System.out.println("parse the player list to get just the players");
    List expResult = Arrays.asList(new String[] {"ShanghaiScott"});

    RconMessage response = rcon.send("list");
    List result = RconParser.parseList(new String(response.payload,"US-ASCII"));

    assertEquals(expResult, result);
  }*/
}