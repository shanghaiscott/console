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
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author scott
 */
public class RconUserMonitor extends TimerTask {
  private static final Logger LOG = Logger.getLogger(RconUserMonitor.class.getName());
  /**
   * Set this -Dmacros=filewithmacros.txt
   */
  private static final String P_MACRO_FILE = "macros";
  /**
   * Default macros file in working directory: macros.txt
   */

  private static final String P_CONFIG_FILE = "config";
  private static final String D_CONFIG_FILE = System.getProperty("user.dir")
    + System.getProperty("file.separator") + "config.txt";
  private static final Properties SYSPROPS = System.getProperties();
  private static final String P_PASS = "rcon.password";
  private static final String P_HOST = "rcon.host";
  private static final String P_PORT = "rcon.port";

  private static final String C_LIST_USERS = "list";

  public RconProtocol rcon;
  public String lastOutput;

  /**
   * Interact with a Minecraft server via the Rcon protocol using the command
   * line.
   *
   * @param args
   */
  public static void main(final String[] args) {

    try {
      SYSPROPS.load(new FileReader(System.getProperty(P_CONFIG_FILE, D_CONFIG_FILE)));
    } catch (IOException ex) {
      LOG.log(Level.WARNING, "WARNING: File not loaded: {0}", ex.getMessage());
    }

    // Override config file setting with provided args if any:
    for (int i = 0; i < args.length; i++) {
      switch (i) {
        case 0:
          SYSPROPS.setProperty(P_HOST, args[i]);
          break;
        case 1:
          SYSPROPS.setProperty(P_PORT, args[i]);
          break;
        case 2:
          SYSPROPS.setProperty(P_PASS, args[i]);
          break;
        default:
          break;
      }
    }
    validateProperty(P_HOST);
    validateProperty(P_PORT);
    validateProperty(P_PASS);

    final RconProtocol rcon
      = new RconProtocol(SYSPROPS.getProperty(P_HOST),
        Integer.parseInt(SYSPROPS.getProperty(P_PORT)),
        SYSPROPS.getProperty(P_PASS));
    final RconUserMonitor monitor = new RconUserMonitor(rcon);
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(monitor, 0, 10 * 1000);// 10 seconds
    LOG.log(Level.INFO,"Server: {0}:{1}", 
      new Object[] { SYSPROPS.getProperty(P_HOST), SYSPROPS.getProperty(P_PORT) });

  }

  private static void validateProperty(final String inKey) {
    final String value = SYSPROPS.getProperty(inKey);
    if (value == null || value.isEmpty()) {
      LOG.log(Level.SEVERE, "ERROR: Missing value for property: {0}", inKey);
      LOG.info("Set the property in the config.txt or on the command line:");
      LOG.info("Usage: RconUserMonitor host port password");
      System.exit(1);
    }
  }

  public RconUserMonitor(final RconProtocol inRcon) {
    this.rcon = inRcon;
  }

  public String send(final String inCommand)
    throws IOException {
    RconMessage response = this.rcon.send(inCommand);
    String output = new String(response.payload, StandardCharsets.US_ASCII);
    //System.out.println(inCommand);
    //System.out.println(response.toString());
    //String[] lines =
    //  new String(response.payload,StandardCharsets.US_ASCII).split("---");//\\r?\\n
    //System.out.println(lines.length);
    //for (int i = 0; i < lines.length; i++ ) {
    //  System.out.println(lines[i]);
    //}
    //System.out.println(output);
    return output;
  }

  @Override
  public void run() {
    try {
      String output = send(C_LIST_USERS);
      if (!(output == null || output.isEmpty())) {
        if (!output.equals(lastOutput)) {
          if (lastOutput != null) {
            LOG.info("USER JOIN/LEAVE DETECTED");
            LOG.info(output);
          }
          lastOutput = output;
        }
      }
    } catch (IOException ex) {
      LOG.log(Level.SEVERE, "ERROR: {0}", ex.getMessage());
    }
  }

}
