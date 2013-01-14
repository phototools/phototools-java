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

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.TimeZone;

import junit.framework.TestCase;

public class FileToolsTest extends TestCase {
    public void testFileModificationDate() throws Exception {
        File tempFile = File.createTempFile(getClass().getSimpleName(), ".tmp");
        File tempFile2 = new File(tempFile.getAbsolutePath() + "2");

        try {
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write("test".getBytes());
            fos.close();
            long orgLastModified = tempFile.lastModified();
            tempFile.setLastModified(orgLastModified - 10000000);
            long actualLastModified = tempFile.lastModified();
            assertTrue("Precondition", actualLastModified < orgLastModified);

            copyFile(tempFile, tempFile2);
            assertTrue("Precondition", tempFile2.exists());

            // Cannot simply complare date objects because the millis can be different.
            Calendar expectedCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            expectedCal.setTimeInMillis(actualLastModified);
            Calendar actualCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            actualCal.setTime(FileTools.getFileModificationDate(tempFile2));

            assertEquals(expectedCal.get(Calendar.YEAR), actualCal.get(Calendar.YEAR));
            assertEquals(expectedCal.get(Calendar.MONTH), actualCal.get(Calendar.MONTH));
            assertEquals(expectedCal.get(Calendar.DAY_OF_MONTH), actualCal.get(Calendar.DAY_OF_MONTH));
            assertEquals(expectedCal.get(Calendar.HOUR_OF_DAY), actualCal.get(Calendar.HOUR_OF_DAY));
            assertEquals(expectedCal.get(Calendar.MINUTE), actualCal.get(Calendar.MINUTE));
        } finally {
            tempFile.delete();
            tempFile2.delete();
        }
    }

    private void copyFile(File tempFile, File tempFile2) throws Exception {
        String [] cmdarray;
        if (File.separatorChar == '\\') {
            cmdarray = new String [] {"cmd", "/c", "copy",
                    tempFile.getAbsolutePath(),
                    tempFile2.getAbsolutePath()};
        } else {
            cmdarray = new String [] {"sh", "-c", "cp -p \"" +
                    tempFile.getAbsolutePath() + "\" \"" +
                    tempFile2.getAbsolutePath() + "\""};
        }
        Process process = Runtime.getRuntime().exec(cmdarray);
        process.waitFor();
    }
}
