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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Scanner;

/**
 *
 * @author scott
 */
public class RconCLI {

  /**
   * Set this -Dmacros=filewithmacros.txt
   */
  private static final String P_MACRO_FILE = "macros";
  /**
   * Default macros file in working directory: macros.txt
   */
  private static final String D_MACRO_FILE
    = System.getProperty("user.dir") + System.getProperty("file.separator")
    + "macros.txt";
  private static final Properties MACROS = new Properties();
  private static final String MACRO_SEP = ";";
  private static final String P_CONFIG_FILE = "config";
  private static final String D_CONFIG_FILE = System.getProperty("user.dir")
    + System.getProperty("file.separator") + "config.txt";
  private static final Properties SYSPROPS = System.getProperties();
  private static final String P_COMMAND_FILE = "commands"; // -Dcommands=
  private static final String P_USER = "rcon.macro.user";
  private static final String D_USER = "ShanghaiScott";
  private static final String P_PASS = "rcon.password";
  private static final String P_HOST = "rcon.host";
  private static final String P_PORT = "rcon.port";
  private static final String P_SERVER_NAME = "rcon.server_name";
  private static final String D_SERVER_NAME = "server";

  public RconProtocol rcon;
  public File commands;

  /**
   * Interact with a Minecraft server via the Rcon protocol using the command
   * line.
   *
   * @param args
   */
  public static void main(final String[] args) {

    try {
      SYSPROPS.load(new FileReader(System.getProperty(P_CONFIG_FILE, D_CONFIG_FILE)));
      MACROS.load(new FileReader(System.getProperty(P_MACRO_FILE, D_MACRO_FILE)));
    } catch (IOException ex) {
      System.out.println("WARNING: File not loaded: " + ex.getMessage());
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
        case 3:
          SYSPROPS.setProperty(P_SERVER_NAME, args[i]);
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
    final RconCLI cli = new RconCLI(rcon);

    // If we are told to use a command file, check that it exists
    // if it exists use it, otherwise error out
    if (!(System.getProperty(P_COMMAND_FILE) == null
      || System.getProperty(P_COMMAND_FILE).isEmpty())) {
      cli.commands = new File(System.getProperty(P_COMMAND_FILE));
      if (cli.commands.exists()) {
        // for each line of the command file, send the command
        // Note: Try with resources, yeah!
        try (BufferedReader br = new BufferedReader(new FileReader(cli.commands))) {
          for (String line; (line = br.readLine()) != null;) {
            cli.send(line);
          }
        } catch (IOException ex) {
          System.out.println("ERROR: Command execution failed: "
            + ex.getMessage());
          System.exit(2);
        }
      }
      System.exit(0);
    } 
    // if we are processing a command file, we won't get to this code...
    final Scanner in = new Scanner(System.in, "UTF-8");

    System.out.printf(
      "Server: %1$s:%2$s%n", SYSPROPS.getProperty(P_HOST),
      (SYSPROPS.getProperty(P_PORT)));
    String[] commandWords;
    String user = SYSPROPS.getProperty(P_USER, D_USER);
    String server_name = SYSPROPS.getProperty(P_SERVER_NAME, D_SERVER_NAME);

    while (true) {
      System.out.printf(server_name + "> ");
      try {
        String commandLine = in.nextLine();
        if (commandLine.isEmpty()) {
          continue;
        } else {
          commandWords = commandLine.split("\\s+");
        }
        String command = commandWords[0];
        if ("QUIT".equalsIgnoreCase(command)) {
          System.exit(0);
          // Macros are can be one word or two. If two, the second
          // word is used to replace any occurence of "$user" in the macro.
        } else if (MACROS.containsKey(command)) {
          String cuser;
          if (commandWords.length == 2) {
            cuser = commandWords[1];
          } else {
            cuser = user;
          }
          String[] commands = MACROS.getProperty(command).split(";");
          for (String command1 : commands) {
            cli.macro(command1, cuser);
          }
        } else {
          cli.send(commandLine);
        }
      } catch (IOException ex) {
        System.out.printf("Error: %1s%n", ex.getMessage());
      }
    }
  }

  private static void validateProperty(final String inKey) {
    final String value = SYSPROPS.getProperty(inKey);
    if (value == null || value.isEmpty()) {
      System.out.println("ERROR: Missing value for property: " + inKey);
      System.out.println("Set the property in the config.txt or on the command line:");
      System.out.println("Usage: RconCLI host port password");
      System.exit(1);
    }
  }

  public RconCLI(final RconProtocol inRcon) {
    this.rcon = inRcon;
  }

  public void send(final String inCommand)
    throws IOException {
    RconMessage response = this.rcon.send(inCommand);
    //System.out.println(inCommand);
    //System.out.println(response.toString());
    //String[] lines =
    //  new String(response.payload,StandardCharsets.US_ASCII).split("---");//\\r?\\n
    //System.out.println(lines.length);
    //for (int i = 0; i < lines.length; i++ ) {
    //  System.out.println(lines[i]);
    //}
    System.out.println(new String(response.payload, StandardCharsets.US_ASCII));
  }

  public void macro(final String inMacro, final String inUser)
    throws IOException {
    boolean hasUser = true;
    if (inUser == null || inUser.isEmpty()) {
      hasUser = false;
    }
    String[] commands = inMacro.split(MACRO_SEP);
    for (String acommand : commands) {
      if (hasUser) {
        acommand = acommand.replace("$user", inUser);
      }
      send(acommand);
    }
  }
}
