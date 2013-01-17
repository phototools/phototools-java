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
import java.util.Collections;
import java.util.Comparator;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class OSGiTools {
    private OSGiTools() {}

    public static ServiceReference[] getSortedServiceReferences(BundleContext ctx, String className, String filter) {
        ServiceReference[] refs = null;
        try {
            refs = ctx.getServiceReferences(className, filter);
        } catch (InvalidSyntaxException ise) {
            throw new RuntimeException(ise);
        }

        if (refs == null)
            return new ServiceReference[] {};

        Arrays.sort(refs, new ServiceRankingComparator());
        return refs;
    }

    @SuppressWarnings("unchecked")
    public static Collection<String> getStringPlusProperty(Object property) {
        if (property == null) {
            return Collections.emptySet();
        }

        if (property instanceof Collection) {
            return (Collection<String>) property;
        } else if (property instanceof String[]) {
            return Arrays.asList((String[])property);
        } else {
            return Collections.singleton(property.toString());
        }
    }

    static class ServiceRankingComparator implements Comparator<ServiceReference> {
        @Override
        public int compare(ServiceReference o1, ServiceReference o2) {
            Integer r1 = getServiceRanking(o1);
            Integer r2 = getServiceRanking(o2);
            return -r1.compareTo(r2);
        }

        private Integer getServiceRanking(ServiceReference sref) {
            Object prop = sref.getProperty(Constants.SERVICE_RANKING);
            if (prop instanceof Integer) {
                return (Integer) prop;
            } else {
                return 0;
            }
        }
    }
}