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
package org.coderthoughts.phototools.util.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.coderthoughts.phototools.api.PhotoIterable;
import org.coderthoughts.phototools.api.PhotoMetadataProvider;
import org.coderthoughts.phototools.api.PhotoMetadataProvider.Metadata;
import org.coderthoughts.phototools.util.Streams;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class UIUtils {
    public static JPanel getPhotoPreview(BundleContext bundleContext, PhotoIterable.Entry entry, Date fromDate, Date toDate) {
        JPanel p = new JPanel();
        if (getPhotoPreview(bundleContext, entry, p, fromDate, toDate, false) == null)
            return null;
        return p;
    }

    public static JCheckBox getPhotoPreview(BundleContext bundleContext, PhotoIterable.Entry entry, Date fromDate, Date toDate, JPanel panel) {
        return (JCheckBox) getPhotoPreview(bundleContext, entry, panel, fromDate, toDate, true);
    }

    private static JComponent getPhotoPreview(BundleContext bundleContext, PhotoIterable.Entry entry, JPanel panel, Date fromDate, Date toDate, boolean checkbox) {
        File tempFile = null;
        try {
            String n = entry.getName();
            String extension = "";
            int idx = n.lastIndexOf('.');
            if (idx >= 0)
                extension = n.substring(idx);

            extension = extension.toLowerCase();

            tempFile = File.createTempFile("PhotoCopy", extension);
            Streams.pump(entry.getInputStream(), new FileOutputStream(tempFile));

            Date date = null;
            File previewFile = null;
            ServiceReference[] refs = bundleContext.getServiceReferences(PhotoMetadataProvider.class.getName(), "(format=" + extension + ")");
            if (refs != null) {
                for (ServiceReference ref : refs) {
                    PhotoMetadataProvider processor = (PhotoMetadataProvider) bundleContext.getService(ref);
                    if (processor == null)
                        continue;

                    Metadata metadata = processor.getMetaData(tempFile);
                    date = metadata.getDateTaken();
                    previewFile = metadata.getPreviewFile();
                }
            }

            if (date == null)
                date = entry.getDate();

            if (fromDate != null && date != null)
                if (date.getTime() < fromDate.getTime())
                    return null;

            if (toDate != null && date != null)
                if (date.getTime() >= toDate.getTime())
                    return null;

            return PhotoPreview.getPhotoPreview(entry.getName(), date, previewFile, panel, checkbox);
        } catch (Exception e) {
            return null;
        } finally {
            if (tempFile != null)
                tempFile.delete();
        }
    }


}
