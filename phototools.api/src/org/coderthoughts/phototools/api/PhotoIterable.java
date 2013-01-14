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

import java.io.InputStream;
import java.util.Date;

public interface PhotoIterable extends Iterable<PhotoIterable.Entry> {
    /**
     * Get a human readable identification of the location.
     * @return The location string.
     */
    String getLocationString();

    /**
     * Specify the file extensions that should be allowed. This mechanism is
     * case insensitive. This method should be called before the {@link #freeze()} method is called.
     * @param extensions The allowed file name extensions, or <tt>null</tt> if all
     * extensions are allowed.
     * @return returns itself to support fluent-style usage.
     */
    PhotoIterable setExtensions(String ... extensions);

    /**
     * Freeze this iterable. After this none of the set... APIs can be exercised.
     * @return returns itself to support fluent-style usage.
     */
    PhotoIterable freeze();

    /**
     * This class represents a photo object that can be read.
     */
    public class Entry {
        private final String name;
        private final Date date;
        private final InputStream is;

        public Entry(String name, Date date, InputStream is) {
            this.name = name;
            this.date = date;
            this.is = is;
        }

        /**
         * Returns the file name to use for the photo. No file path information is returned.
         * @return The file name without path information, for example IMG_01429.JPG
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the file date to use as a fallback for when the file date cannot be obtained
         * from the photo file metadata itself. Normally this would be the date on the file object
         * backing the photo.
         * @return The file date or <tt>null</tt> if no file date information is available.
         */
        public Date getDate() {
            return date;
        }

        /**
         * Returns a stream to read the photo bytes from. Since the stream may be backed by a temporary
         * file it is important to close the stream to ensure cleanup can happen.
         * @return The stream to read the photo bytes from.
         */
        public InputStream getInputStream() {
            return is;
        }
    }
}
