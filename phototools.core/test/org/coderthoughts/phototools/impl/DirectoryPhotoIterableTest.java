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
package org.coderthoughts.phototools.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import junit.framework.TestCase;

import org.coderthoughts.phototools.api.PhotoIterable;
import org.coderthoughts.phototools.api.PhotoIterable.Entry;
import org.coderthoughts.phototools.util.DirectoryPhotoIterable;
import org.coderthoughts.phototools.util.FileTools;
import org.coderthoughts.phototools.util.StreamTools;

public class DirectoryPhotoIterableTest extends TestCase {
    public void testFilePhotoIterable() throws Exception {
        URL imageDirURL = getClass().getResource("photocopy/images");
        String imageDir = imageDirURL.getFile();
        DirectoryPhotoIterable fpi = new DirectoryPhotoIterable(imageDir);
        assertEquals(imageDir, fpi.getLocationString());

        try {
            fpi.iterator();
            fail("Must call freeze() first");
        } catch (IllegalStateException ise) {
            // good
        }

        fpi.setExtensions(".jpg", ".png");
        fpi.freeze();

        try {
            fpi.setExtensions("*.txt");
            fail("Cannot call setExtensions() after freeze()");
        } catch (IllegalStateException ise) {
            // good
        }

        Set<String> expectedFiles = new HashSet<String>();
        File f = new File(imageDir + "/PhotoTools.png");
        File f1 = new File(imageDir + "/image1.jpg");
        File f2 = new File(imageDir + "/sub/image1.jpg");
        // note that there is also a resource /sub/anothersub/image1.jpg but this one is identical
        // in content to f1
        File f4 = new File(imageDir + "/.hidden_image.jpg");
        expectedFiles.add(f.getAbsolutePath());
        expectedFiles.add(f1.getAbsolutePath());
        expectedFiles.add(f2.getAbsolutePath());
        // expectedFiles.add(f3.getAbsolutePath()); // this one is identical to f1 in content
        expectedFiles.add(f4.getAbsolutePath());

        Set<String> actualFiles = new HashSet<String>();

        for (PhotoIterable.Entry entry : fpi) {
            String n = entry.getName();
            if (n.equals(f.getName())) {
                assertDatesSimilar(FileTools.getFileModificationDate(f), entry.getDate());
                byte[] expectedBytes = suckStream(new FileInputStream(f));
                byte[] actualBytes = suckStream(entry.getInputStream());
                assertTrue(Arrays.equals(expectedBytes, actualBytes));
                actualFiles.add(f.getAbsolutePath());
            } else if (n.equals(f4.getName())) {
                assertDatesSimilar(FileTools.getFileModificationDate(f4), entry.getDate());
                byte[] expectedBytes = suckStream(new FileInputStream(f4));
                byte[] actualBytes = suckStream(entry.getInputStream());
                assertTrue(Arrays.equals(expectedBytes, actualBytes));
                actualFiles.add(f4.getAbsolutePath());
            } else if (n.equals(f1.getName())) {
                byte[] f1Bytes = suckStream(new FileInputStream(f1));
                byte[] f2Bytes = suckStream(new FileInputStream(f2));

                byte[] actualBytes = suckStream(entry.getInputStream());
                if (Arrays.equals(f1Bytes, actualBytes))
                    actualFiles.add(f1.getAbsolutePath());
                else if (Arrays.equals(f2Bytes, actualBytes))
                    actualFiles.add(f2.getAbsolutePath());
                else
                    fail("Unrecognized entry content: " + n);
            } else {
                fail(n);
            }
        }
        assertEquals(expectedFiles, actualFiles);

        assertEquals(imageDir, fpi.getLocationString());
    }

    public void testFilePhotoIterable2() throws Exception {
        URL imageDirURL = getClass().getResource("photocopy/images");
        String imageDir = imageDirURL.getFile();
        DirectoryPhotoIterable fpi = new DirectoryPhotoIterable(imageDir);
        assertEquals(imageDir, "" + fpi);

        fpi.freeze();
        Iterator<Entry> it = fpi.iterator();
        assertTrue(it.hasNext());
        assertTrue(it.hasNext());
        assertEquals(".hidden_image.jpg", it.next().getName());
        assertEquals("PhotoTools.png", it.next().getName());
        assertEquals("image1.jpg", it.next().getName());
        assertEquals("textfile.txt", it.next().getName());

        while (it.hasNext())
            it.next();

        try {
            it.next();
            fail();
        } catch (NoSuchElementException nsee) {
            // good;
        }
    }

    private static byte [] suckStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            StreamTools.pump(is, baos);
            return baos.toByteArray();
        } finally {
            is.close();
        }
    }

    public void testFilePhotoIterable3() throws Exception {
        URL imageDirURL = getClass().getResource("photocopy/images");
        String imageDir = imageDirURL.getFile();
        DirectoryPhotoIterable fpi = new DirectoryPhotoIterable(imageDir);
        fpi.setExtensions((String[])null);
        fpi.freeze();
        Iterator<Entry> it = fpi.iterator();
        assertEquals(".hidden_image.jpg", it.next().getName());
        assertEquals("PhotoTools.png", it.next().getName());
        assertEquals("image1.jpg", it.next().getName());
        assertEquals("textfile.txt", it.next().getName());
    }

    private void assertDatesSimilar(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(d1);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(d2);

        assertEquals(c1.get(Calendar.YEAR), c2.get(Calendar.YEAR));
        assertEquals(c1.get(Calendar.MONTH), c2.get(Calendar.MONTH));
        assertEquals(c1.get(Calendar.DAY_OF_MONTH), c2.get(Calendar.DAY_OF_MONTH));
        assertEquals(c1.get(Calendar.HOUR_OF_DAY), c2.get(Calendar.HOUR_OF_DAY));
        assertEquals(c1.get(Calendar.MINUTE), c2.get(Calendar.MINUTE));
    }
}
