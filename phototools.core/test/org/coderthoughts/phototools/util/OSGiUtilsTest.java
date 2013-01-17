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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

public class OSGiUtilsTest extends TestCase {
    public void testStringPlusProperty() {
        assertEquals(0, OSGiUtils.getStringPlusProperty(null).size());

        List<String> l = Arrays.asList("a", "b", "c");
        assertEquals(l, OSGiUtils.getStringPlusProperty(l));

        String[] a = new String[] {"a", "b", "c"};
        assertEquals(l, OSGiUtils.getStringPlusProperty(a));

        Collection<String> c = OSGiUtils.getStringPlusProperty("a b c");
        assertEquals(1, c.size());
        assertEquals("a b c", c.iterator().next());
    }
}
