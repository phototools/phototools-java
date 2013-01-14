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
package org.coderthoughts.phototools.dupfinder.impl;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.coderthoughts.phototools.api.PhotoMetadataBuilder;
import org.coderthoughts.phototools.api.PhotoMetadataProvider;

public class DuplicateFinderTest extends TestCase {
    public void testKeyGeneration() {
        URL imageDirURL = getClass().getResource("images");
        File imageDir = new File(imageDirURL.getFile());

        Map<String, PhotoMetadataProvider> providers = new HashMap<String, PhotoMetadataProvider>();
        PhotoMetadataProvider jpmp = getMockJPEGMetadataProvider();
        providers.put(".jpg", jpmp);
        providers.put(".jpeg", jpmp);

        PhotoMetadataProvider gpmp = getMockGifMetadataProvider();
        providers.put(".gif", gpmp);

        Object[] k1 = DuplicateFinder.computeKeys(providers, new File(imageDir, "image1_1.jpg"));
        Object[] k2 = DuplicateFinder.computeKeys(providers, new File(imageDir, "image1.jpeg"));
        Object[] k3 = DuplicateFinder.computeKeys(providers, new File(imageDir, "sub/image1.jpg"));
        assertEquals(1, k1.length);
        assertTrue(Arrays.deepEquals(k1, k2));
        assertTrue(Arrays.deepEquals(k2, k3));

        Object[] k4 = DuplicateFinder.computeKeys(providers, new File(imageDir, "services.gif"));
        Object[] k5 = DuplicateFinder.computeKeys(providers, new File(imageDir, "svcs.gif"));
        Object[] k6 = DuplicateFinder.computeKeys(providers, new File(imageDir, "sub/services.gif"));
        assertEquals(2, k4.length);
        assertEquals(2, k5.length);
        assertEquals(2, k6.length);
        assertFalse(Arrays.deepEquals(k4, k5));
        assertFalse(Arrays.deepEquals(k4, k6));
        assertFalse(Arrays.deepEquals(k5, k6));
    }

    public void testDuplicateFinder() {
        URL imageDirURL = getClass().getResource("images");
        File imageDir = new File(imageDirURL.getFile());

        Map<String, PhotoMetadataProvider> providers = new HashMap<String, PhotoMetadataProvider>();
        PhotoMetadataProvider jpmp = getMockJPEGMetadataProvider();
        providers.put(".jpg", jpmp);
        providers.put(".jpeg", jpmp);

        PhotoMetadataProvider gpmp = getMockGifMetadataProvider();
        providers.put(".gif", gpmp);

        DuplicateFinder df = new DuplicateFinder(providers);
        File fa1 = new File(imageDir, "image1_1.jpg");
        File fa2 = new File(imageDir, "image1.jpeg");
        File fa3 = new File(imageDir, "sub/image1.jpg");
        File fb1 = new File(imageDir, "services.gif");
        File fb2 = new File(imageDir, "svcs.gif");
        File fb3 = new File(imageDir, "sub/services.gif");
        File fc = new File(imageDir, "PhotoTools.gif");
        df.addCandidate(fa1);
        df.addCandidate(fa2);
        df.addCandidate(fa3);
        df.addCandidate(fb1);
        df.addCandidate(fb2);
        df.addCandidate(fb3);
        df.addCandidate(fc);

        // The following should be ignored as there is no metadata provider for textfiles provided
        df.addCandidate(new File(imageDir, "textfile.txt"));
        df.addCandidate(new File(imageDir, "tf2.txt"));

        try {
            df.addCandidate(new File(imageDir, "nonexistent.gif"));
            fail("Should not accept a non-existent file");
        } catch (IllegalArgumentException iae) {
            // good
        }

        List<List<File>> candidates = df.getCandidatesList();
        assertEquals("Should be 2 groups of candidates", 2, candidates.size());

        Set<File> expectedSet1 = new HashSet<File>(Arrays.asList(fa1, fa2, fa3));
        Set<File> expectedSet2 = new HashSet<File>(Arrays.asList(fb1, fb2, fb3));
        Set<File> candidateSet1 = new HashSet<File>(candidates.get(0));
        Set<File> candidateSet2 = new HashSet<File>(candidates.get(1));
        boolean found1 = candidateSet1.equals(expectedSet1) && candidateSet2.equals(expectedSet2);
        boolean found2 = candidateSet2.equals(expectedSet1) && candidateSet1.equals(expectedSet2);
        assertTrue(found1 || found2);
        assertFalse(found1 == found2);
    }

    private PhotoMetadataProvider getMockGifMetadataProvider() {
        return new PhotoMetadataProvider() {
            @Override
            public Metadata getMetaData(File f) {
                return new PhotoMetadataBuilder().getMetadata();
            }
        };
    }

    private PhotoMetadataProvider getMockJPEGMetadataProvider() {
        return new PhotoMetadataProvider() {
            Collection<String> similarDates = Arrays.asList("image1_1.jpg", "image1.jpeg", "image1.jpg");
            int counter = 0;

            @Override
            public Metadata getMetaData(File f) {
                Date d;
                if (similarDates.contains(f.getName())) {
                    // Deliberately make the seconds different, the comparison should ignore those
                    d = getDate(13, 2, 1971, 12, 30, 45, counter++ * 10);
                } else {
                    d = new Date(); // now
                }

                return new PhotoMetadataBuilder().dateTaken(d).getMetadata();
            }
        };
    }

    private Date getDate(int day, int month, int year, int hour, int min, int sec, int millis) {
        // Note that month is 1-based (January = 1)
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.YEAR, year);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, min);
        c.set(Calendar.SECOND, sec);
        c.set(Calendar.MILLISECOND, millis);
        return c.getTime();
    }
}
