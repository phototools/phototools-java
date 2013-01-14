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
package org.coderthoughts.phototools.mp4.impl;

import java.util.Dictionary;
import java.util.Hashtable;

import org.coderthoughts.phototools.api.AboutInfo;
import org.coderthoughts.phototools.api.PhotoMetadataProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
    @Override
    public void start(BundleContext context) throws Exception {
        setupPhotoProcessors(context);
    }

    private void setupPhotoProcessors(BundleContext context) {
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("format", new String [] {".mp4", ".mov", ".m4v", ".3gp"});
        context.registerService(PhotoMetadataProvider.class.getName(), new MP4MetadataProvider(), props);

        context.registerService(AboutInfo.class.getName(), new MyAboutInfo(), null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
