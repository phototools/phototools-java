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

import java.awt.Window;

/**
 * Interface to select the location from where photos should be downloaded. In most cases
 * this will involve opening a window that allows the user to select this location.
 */
public interface PhotoSource {
    /**
     * The label for this source, for example 'File System' or 'iPod'.
     * @return The label to use.
     */
    String getLabel();

    /**
     * Calling this method should open a selection window where the user can select where the
     * photos are to be copied or downloaded from.
     *
     * @param parentWindow The parent window. If the implementation opens a GUI window, this can
     * be used to specify its parent.
     * @param initialSelection A previous selection, if any. This value was previously
     * returned from {@link PhotoIterable#getLocationString()}.
     * @return A PhotoIterable to obtain photos from the selected location.
     */
    PhotoIterable getPhotoIterable(Window parentWindow, String initialSelection);

    /**
     * Obtain a PhotoIterable given a location which is represented as a string.
     * The string used must have been previously obtained from a PhotoIterable.
     * @param location The location string.
     * @return The PhotoIterable or <tt>null</tt> if one cannot be obtained.
     */
    PhotoIterable getPhotoIterableFromLocation(String location);
}
