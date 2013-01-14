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
import java.net.URL;
import java.util.Iterator;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

public class DirTreeIteratorTest extends TestCase {
    public void testDirTreeIterator1() throws Exception {
        URL somedirURL = getClass().getResource("somedir");
        File somedir = new File(somedirURL.getFile());
        assertTrue(somedir.isDirectory());

        Iterator<File> it = new DirTreeIterator(somedir);
        assertTrue(it.hasNext());
        File next = it.next();
        assertTrue(next.isFile());
        assertTrue(next.getAbsolutePath().replace('\\', '/').endsWith("somedir/afile.txt"));

        assertTrue(it.hasNext());
        next = it.next();
        assertTrue(next.isDirectory());
        assertTrue(next.getAbsolutePath().replace('\\', '/').endsWith("somedir/subdir1"));

        assertTrue(it.hasNext());
        next = it.next();
        assertTrue(next.isDirectory());
        assertTrue(next.getAbsolutePath().replace('\\', '/').endsWith("somedir/subdir2"));

        assertTrue(it.hasNext());
        next = it.next();
        assertTrue(next.isDirectory());
        assertTrue(next.getAbsolutePath().replace('\\', '/').endsWith("somedir/subdir1/subsub"));

        assertTrue(it.hasNext());
        next = it.next();
        assertTrue(next.isFile());
        assertTrue(next.getAbsolutePath().replace('\\', '/').endsWith("somedir/subdir2/cfile.txt"));

        assertTrue(it.hasNext());
        next = it.next();
        assertTrue(next.isFile());
        assertTrue(next.getAbsolutePath().replace('\\', '/').endsWith("somedir/subdir1/subsub/afile.txt"));

        assertTrue(it.hasNext());
        next = it.next();
        assertTrue(next.isFile());
        assertTrue(next.getAbsolutePath().replace('\\', '/').endsWith("somedir/subdir1/subsub/bfile.txt"));

        assertFalse(it.hasNext());
    }

    public void testDirTreeIterator2() throws Exception {
        URL somedirURL = getClass().getResource("somedir");
        File somedir = new File(somedirURL.toURI().toURL().getFile());
        assertTrue(somedir.isDirectory());

        Iterator<File> it = new DirTreeIterator(somedir);
        File next = it.next();
        assertTrue(next.isFile());
        assertTrue(next.getAbsolutePath().replace('\\', '/').endsWith("somedir/afile.txt"));

        next = it.next();
        assertTrue(next.isDirectory());
        assertTrue(next.getAbsolutePath().replace('\\', '/').endsWith("somedir/subdir1"));

        next = it.next();
        assertTrue(next.isDirectory());
        assertTrue(next.getAbsolutePath().replace('\\', '/').endsWith("somedir/subdir2"));

        next = it.next();
        assertTrue(next.isDirectory());
        assertTrue(next.getAbsolutePath().replace('\\', '/').endsWith("somedir/subdir1/subsub"));

        next = it.next();
        assertTrue(next.isFile());
        assertTrue(next.getAbsolutePath().replace('\\', '/').endsWith("somedir/subdir2/cfile.txt"));

        next = it.next();
        assertTrue(next.isFile());
        assertTrue(next.getAbsolutePath().replace('\\', '/').endsWith("somedir/subdir1/subsub/afile.txt"));

        next = it.next();
        assertTrue(next.isFile());
        assertTrue(next.getAbsolutePath().replace('\\', '/').endsWith("somedir/subdir1/subsub/bfile.txt"));

        try {
            it.next();
            fail("Should have thrown a NoSuchElementException");
        } catch (NoSuchElementException nsee) {
            // good
        }
    }
}
