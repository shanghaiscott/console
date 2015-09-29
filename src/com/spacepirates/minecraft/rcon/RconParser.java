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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author scott
 */
public class RconParser {

  public static List<String> parseList(String inList) {
    final List<String> list = new ArrayList();
    String lastItem = "";
    // tokenize on the "," and remove the last "and"
    String[] cruftFilter = inList.split(":");
    if (cruftFilter.length == 2) {
      inList = cruftFilter[1];
      String[] andFilter = inList.split("\\sand\\s");
      if (andFilter.length == 2) {
        inList = andFilter[0];
        lastItem = andFilter[1];
      }
      String[] commaFilter = inList.replaceAll("\\s", "").split(",");
      list.addAll(Arrays.asList(commaFilter));
      if (!lastItem.isEmpty()) {
        list.add(lastItem.replaceAll("\\s", ""));
      }
    }
    return list;
  }
}
