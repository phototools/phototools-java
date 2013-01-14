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

import junit.framework.TestCase;

public class DirTreeIterableTest extends TestCase {
    public void testDirTreeIterable() throws Exception {

        URL somedirURL = getClass().getResource("somedir");
        File somedir = new File(somedirURL.toURI().toURL().getFile());
        assertTrue(somedir.isDirectory());

        Iterable<File> iterable = new DirTreeIterable(somedir);
        Iterator<File> it = iterable.iterator();
        File next = it.next();
        assertTrue(next.isFile());
        assertTrue(next.getAbsolutePath().replace('\\', '/').endsWith("somedir/afile.txt"));

        for (File f : iterable) {
            assertTrue(f.isFile());
            assertTrue(f.getAbsolutePath().replace('\\', '/').endsWith("somedir/afile.txt"));
            break;
        }
    }
}
