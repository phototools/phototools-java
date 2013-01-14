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

import java.net.URL;

import junit.framework.TestCase;

import org.coderthoughts.phototools.api.PhotoIterable;
import org.coderthoughts.phototools.api.PhotoIterable.Entry;
import org.coderthoughts.phototools.util.DirectoryPhotoIterable;

public class DirectoryPhotoSourceTest extends TestCase {
    public void testDirectoryPhotoSource() {
        DirectoryPhotoSource dps = new DirectoryPhotoSource();
        assertEquals("Directory", dps.getLabel());

        URL imageDirURL = getClass().getResource("photocopy/images");
        String imageDir = imageDirURL.getFile();
        PhotoIterable iterable = dps.getPhotoIterableFromLocation(imageDir);
        assertTrue(iterable instanceof DirectoryPhotoIterable);
        assertEquals(imageDir, iterable.getLocationString());

        iterable.setExtensions(".txt");
        iterable.freeze();

        Entry entry = iterable.iterator().next();
        assertEquals("textfile.txt", entry.getName());

        assertNull(dps.getPhotoIterableFromLocation("huh"));
    }
}
