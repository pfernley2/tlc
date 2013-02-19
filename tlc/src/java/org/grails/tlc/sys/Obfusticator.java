/*
 *  Copyright 2010-2013 Paul Fernley.
 *
 *  This file is part of the Three Ledger Core (TLC) software
 *  from Paul Fernley.
 *
 *  TLC is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TLC is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with TLC.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.grails.tlc.sys;

import java.io.UnsupportedEncodingException;

public class Obfusticator {

    private static final String hexChars = "5a72b16d4c03f9e8";

    // Create a simple obfustication of a string using a key, returning null
    // if there is any problem. The returned string is in hex format and would
    // not need HTML encoding.
    public static String encrypt(String key, String text) {
        if (key == null || key.length() == 0 || text == null || text.length() == 0) {
            return null;
        }

        byte[] bytes;
        try {
            bytes = getBytes(key.getBytes("UTF-8"), text.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            return null;
        }

        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            if (i % 2 == 0) {
                sb.append(hexChars.charAt(bytes[i] & 0x0f));
                sb.append(hexChars.charAt((bytes[i] >> 4) & 0x0f));
            } else {
                sb.append(hexChars.charAt((bytes[i] >> 4) & 0x0f));
                sb.append(hexChars.charAt(bytes[i] & 0x0f));
            }
        }

        return sb.toString();
    }

    // Reverse a simple obfustication of a string using a key, returning null
    // if there is any problem. The returned string is in hex format and would
    // not need HTML encoding.
    public static String decrypt(String key, String text) {
        if (key == null || key.length() == 0 || text == null || text.length() == 0 || text.length() % 2 != 0) {
            return null;
        }

        byte[] bytes = new byte[text.length() / 2];
        int nibble1, nibble2;
        for (int i = 0, j = 0; i < text.length(); i += 2, j++) {
            nibble1 = hexChars.indexOf(text.charAt(i));
            nibble2 = hexChars.indexOf(text.charAt(i + 1));
            if (nibble1 < 0 || nibble2 < 0) {
                return null;
            }

            if (j % 2 == 0) {
                bytes[j] = (byte) ((nibble2 << 4) | nibble1);
            } else {
                bytes[j] = (byte) ((nibble1 << 4) | nibble2);
            }
        }

        try {
            return new String(getBytes(key.getBytes("UTF-8"), bytes), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
    }

    private static byte[] getBytes(byte[] key, byte[] text) {

        int keyLength = key.length;
        int textLength = text.length;

        for (int i = 0, j = 3; i < textLength; i++, j++) {
            if (j >= keyLength) {
                j = 0;
            }

            text[i] = (byte) (text[i] ^ key[j]);
        }

        return text;
    }
}
