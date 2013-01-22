/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.coderthoughts.phototools.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// This class can be hugely simplified when we're using Java 7
public class FileTools {
    private static final Pattern LS_PATTERN1 = Pattern.compile(".*(\\w\\w\\w)[ ]+(\\d+)[ ]+(\\d\\d)[:](\\d\\d)[:](\\d\\d)[ ]+(\\d\\d\\d\\d).*");
    private static final Pattern LS_PATTERN2 = Pattern.compile(".+(\\d\\d\\d\\d)[-](\\d\\d)[-](\\d\\d)[ ](\\d\\d)[:](\\d\\d)[:](\\d\\d)[.].+");

    // There are 2 unix ls command lines at least one of the mac and another on fedora. We'll just try to figure out which one works best.
    private static volatile boolean preferUnixMethod1 = true;

    /*
     * Return the date the content of the file was modified last. On Windows this is
     * simply File.lastModified() but on unix that API returns when the file was last
     * copied to a new location, so on that platform we have to perform a trick to
     * obtain it via the shell.
     * Note that Java 7 may have a better solution to this...
     */
    public static Date getFileModificationDate(File f) {
        if (File.separatorChar == '\\') {
            return getFileModificationDateWindows(f);
        } else {
            return getFileModificationDateUnix(f);
        }
    }

    private static Date getFileModificationDateWindows(File f) {
        return new Date(f.lastModified());
    }

    private static Date getFileModificationDateUnix(File f) {
        if (preferUnixMethod1)
            return getFileModificationDateUnix12(f);
        else
            return getFileModificationDateUnix21(f);
    }

    private static Date getFileModificationDateUnix12(File f) {
        Date d = null;
        try {
            d = getFileModificationDateUnix1(f);
            if (d != null) {
                preferUnixMethod1 = true;
                return d;
            }
        } catch (Exception e) {
            // try again
        }

        try {
            d = getFileModificationDateUnix2(f);
            if (d != null) {
                preferUnixMethod1 = false;
                return d;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Date getFileModificationDateUnix21(File f) {
        Date d = null;
        try {
            d = getFileModificationDateUnix2(f);
            if (d != null) {
                preferUnixMethod1 = false;
                return d;
            }
        } catch (Exception e) {
            // try again
        }

        if (d == null) {
            try {
                d = getFileModificationDateUnix1(f);
                if (d != null) {
                    preferUnixMethod1 = true;
                    return d;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static Date getFileModificationDateUnix1(File file) throws IOException, InterruptedException {
        String[] cmdarray = new String[] {
                "sh", "-c", "LANG=C TZ=UTC ls -lTU \"" + file.getAbsolutePath() + "\""
        };
        Process process = Runtime.getRuntime().exec(cmdarray);
        process.waitFor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.endsWith(" " + file.getAbsolutePath())) {
                    // this is the file
                    Matcher m = LS_PATTERN1.matcher(line);

                    if (m.matches()) {
                        int year = Integer.parseInt(m.group(6));
                        int month = getUnixMonth(m.group(1));
                        int day = Integer.parseInt(m.group(2));
                        int hour = Integer.parseInt(m.group(3));
                        int minute = Integer.parseInt(m.group(4));
                        int second = Integer.parseInt(m.group(5));
                        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        c.set(year, month, day, hour, minute, second);
                        return c.getTime();
                    }
                }
            }
        } finally {
            reader.close();
        }
        return null;
    }

    private static Date getFileModificationDateUnix2(File file) throws IOException, InterruptedException {
        String[] cmdarray = new String[] {
                "sh", "-c", "LANG=C TZ=UTC ls -l --time=ctime --time-style=full-iso \"" + file.getAbsolutePath() + "\""
        };
        Process process = Runtime.getRuntime().exec(cmdarray);
        process.waitFor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.endsWith(" " + file.getAbsolutePath())) {
                    // this is the file
                    Matcher m = LS_PATTERN2.matcher(line);

                    if (m.matches()) {
                        int year = Integer.parseInt(m.group(1));
                        int month = Integer.parseInt(m.group(2)) - 1;
                        int day = Integer.parseInt(m.group(3));
                        int hour = Integer.parseInt(m.group(4));
                        int minute = Integer.parseInt(m.group(5));
                        int second = Integer.parseInt(m.group(6));
                        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        c.set(year, month, day, hour, minute, second);
                        return c.getTime();
                    }
                }
            }
        } finally {
            reader.close();
        }
        return null;
    }

    private static int getUnixMonth(String month) {
        month = month.toLowerCase();
        if ("jan".equals(month))
            return 0;
        if ("feb".equals(month))
            return 1;
        if ("mar".equals(month))
            return 2;
        if ("apr".equals(month))
            return 3;
        if ("may".equals(month))
            return 4;
        if ("jun".equals(month))
            return 5;
        if ("jul".equals(month))
            return 6;
        if ("aug".equals(month))
            return 7;
        if ("sep".equals(month))
            return 8;
        if ("oct".equals(month))
            return 9;
        if ("nov".equals(month))
            return 10;
        if ("dec".equals(month))
            return 11;
        throw new IllegalArgumentException("Not a valid 3-letter unix month as returned by ls: " + month);
    }
}
