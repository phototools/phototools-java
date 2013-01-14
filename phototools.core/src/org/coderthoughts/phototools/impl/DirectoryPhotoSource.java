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

import java.awt.Window;
import java.io.File;

import javax.swing.JFileChooser;

import org.coderthoughts.phototools.api.PhotoIterable;
import org.coderthoughts.phototools.api.PhotoSource;
import org.coderthoughts.phototools.util.DirectoryPhotoIterable;

public class DirectoryPhotoSource implements PhotoSource {
    @Override
    public String getLabel() {
        return "Directory";
    }

    @Override
    public PhotoIterable getPhotoIterable(Window parentWindow, String initialSelection) {
        JFileChooser chooser = new JFileChooser(initialSelection);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(parentWindow) == JFileChooser.APPROVE_OPTION) {
            return new DirectoryPhotoIterable(chooser.getSelectedFile().getAbsolutePath());
        } else {
            return null;
        }
    }

    @Override
    public PhotoIterable getPhotoIterableFromLocation(String location) {
        File f = new File(location);
        if (f.isDirectory())
            return new DirectoryPhotoIterable(location);
        else
            return null;
    }
}
