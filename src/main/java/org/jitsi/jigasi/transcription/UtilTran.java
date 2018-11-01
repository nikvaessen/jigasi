/*
 * Jigasi, the JItsi GAteway to SIP.
 *
 * Copyright @ 2017 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jitsi.jigasi.transcription;

import org.jitsi.util.*;
import org.json.*;

import java.io.*;
import java.net.*;

/**
 * Utility functions used in the transcription package.
 *
 * @author Damian Minkov
 * @author Nik Vaessen
 */
public class UtilTran
{
    /**
     * The logger for this class
     */
    private final static org.jitsi.util.Logger logger
        = Logger.getLogger(UtilTran.class);

    /**
     * Posts json object to an address of a service to handle it and further
     * process it.
     * @param address the address where to send the post request.
     * @param json the json object to send.
     */
    public static void postJSON(String address, JSONObject json)
    {
        try
        {
            URL url = new URL(address);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes());
            os.flush();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                logger.error("Error for action post received: "
                    + conn.getResponseCode()
                    + "(" + conn.getResponseMessage() + ")");
            }

            conn.disconnect();
        }
        catch (IOException e)
        {
            logger.error("Error posting transcription", e);
        }
    }

    /**
     * Convert an array of bytes into an array of 16-bit integers.
     * It's assumed the input array contains an even amount of bytes,
     * and for each pair of bytes, the first byte is the least-significant.
     *
     * @param audio the array to convert
     * @return the converted array
     */
    public static int[] convertByteArrayTo16bitIntArrays(byte[] audio)
    {
        int[] converted = new int[audio.length / 2];

        for(int i = 0; i < converted.length; i++)
        {
            /*
             * We convert 2 bytes to a 16 bit integer into the following manner:
             *
             * We assume big-endian, so the first bit is the highest value
             * (256) and the last bit is the lowest value (1).
             *
             * We also assume that the byte-pair the byte pair comes in least-
             * significant order. That is, byte 1 contains the bits for 2^0
             * to 2^7 and byte 2 contains the bits for 2^8 to 2^15.
             *
             * For this example, let's assume:
             *
             * b1: 0000 0001
             * b2: 0010 0000
             *
             * In order to create the integer, we first shift the second byte
             * 8 bits to the left (with b2 << 8). An example:
             *
             *           0010 0000 << 8 =
             * 0010 0000 0000 0000
             *
             * We then simply do inclusive OR on the resulting byte + 16 bit int
             *
             * b1: 0000 0000 0000 0001 (implicitly converted)
             * b2: 0010 0000 0000 0000
             *
             *  r: 0010 0000 0000 0001
             *
             * where r is now our converted 16 bit integer!
             */
            byte b1 = audio[i*2];
            byte b2 = audio[i*2+1];
            converted[i] = b1 | (b2 << 8);
        }

        return converted;
    }
}
