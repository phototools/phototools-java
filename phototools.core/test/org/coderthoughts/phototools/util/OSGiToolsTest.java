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

import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

public class OSGiToolsTest extends TestCase {
    public void testGetReferences() {
        BundleContext ctx = Mockito.mock(BundleContext.class);

        ServiceReference[] refs = OSGiTools.getSortedServiceReferences(ctx, "org.foo.Blah", "(x=y)");
        assertEquals(0, refs.length);
    }

    public void testGetReferences2() throws Exception {
        ServiceReference sr1 = mockServiceReference(-71);
        ServiceReference sr2 = mockServiceReference(42);
        ServiceReference sr3 = Mockito.mock(ServiceReference.class);;

        BundleContext ctx = Mockito.mock(BundleContext.class);
        Mockito.when(ctx.getServiceReferences("org.foo.Blah", "(x=y)")).thenReturn(
                new ServiceReference[] {sr1, sr2, sr3});

        ServiceReference[] expected = new ServiceReference[] {sr2, sr3, sr1};
        ServiceReference[] actual = OSGiTools.getSortedServiceReferences(ctx, "org.foo.Blah", "(x=y)");
        assertTrue(Arrays.equals(expected, actual));
    }

    private ServiceReference mockServiceReference(Integer ranking) {
        ServiceReference sr1 = Mockito.mock(ServiceReference.class);
        Mockito.when(sr1.getProperty(Constants.SERVICE_RANKING)).thenReturn(ranking);
        return sr1;
    }

    public void testStringPlusProperty() {
        assertEquals(0, OSGiTools.getStringPlusProperty(null).size());

        List<String> l = Arrays.asList("a", "b", "c");
        assertEquals(l, OSGiTools.getStringPlusProperty(l));

        String[] a = new String[] {"a", "b", "c"};
        assertEquals(l, OSGiTools.getStringPlusProperty(a));

        Collection<String> c = OSGiTools.getStringPlusProperty("a b c");
        assertEquals(1, c.size());
        assertEquals("a b c", c.iterator().next());
    }
}
