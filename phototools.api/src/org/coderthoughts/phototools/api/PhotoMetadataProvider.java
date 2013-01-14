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
package org.coderthoughts.phototools.api;

import java.io.File;
import java.util.Date;

/**
 * Service API for Photo Metadata Providers. An implementation can extract metadata from
 * a photo (or video) file such as the date taken and small preview image.
 *
 * PhotoMetadataProvider instances are registered as OSGi services with the <tt>format</tt>
 * property set to the supported extensions (in lower case), e.g.
 * <pre>
 *   Dictionary props = new Hashtable();
 *   props.put("format", new String [] {".jpeg", ".jpg"});
 *   bundleContext.registerService(PhotoMetadataProvider.class.getName(), new JPEGProcessor(), props);
 * </pre>
 *
 * To obtain a PhotoMetadataProvider for a file, look it up in the OSGi Service Registry, e.g.:
 * <pre>
 *   bundleContext.getServiceReferences(PhotoMetadataProvider.class.getName(), "(format=.jpeg)");
 * </pre>
 * Then, given a PhotoMetadataProvider instance, get it to process a file:
 * <pre>
 *   PhotoMetadataProvider processor = (PhotoMetadataProvider) bundleContext.getService(sref);
 *   Metadata metadata = processor.getMetaData(tempFile);
 *   Date dateTaken = metadata.getDateTaken();
 *</pre>
 */
public interface PhotoMetadataProvider {
    /**
     * Get metadata for a photo or video file
     * @param f The file to process.
     * @return The metadata found.
     */
    Metadata getMetaData(File f);

    public interface Metadata {
        /**
         * Obtain the date the photo was taken (not necessarily the file date).
         * @return The date taken or <tt>null</tt> if this information is unavailable.
         */
        public Date getDateTaken();

        /**
         * Obtain a small preview file for the photo or movie. The preview file will always be a JPEG file.
         * @return The preview file or <tt>null</tt> if this information is unavailable.
         */
        public File getPreviewFile();

        /**
         * Obtain GPS informaton
         * @return GPS information or <tt>null</tt> if this information is unavailable.
         */
        public String getGPSInfo();

        /**
         * Obtain the height in pixels.
         * @return The height in pixels or <tt>null</tt> if this information is unavailable.
         */
        public Integer getHeightInPixels();

        /**
         * Obtain the width in pixels.
         * @return The width in pixels or <tt>null</tt> if this information is unavailable.
         */
        public Integer getWidthInPixels();

        /**
         * Obtain all relevant details in textual format.
         * @return Details in textual format.
         */
        public String getDetails();
    }
}
