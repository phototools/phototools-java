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
package org.coderthoughts.phototools.impl.photocopy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.coderthoughts.phototools.api.PhotoIterable;
import org.coderthoughts.phototools.api.PhotoIterable.Entry;
import org.coderthoughts.phototools.api.PhotoMetadataProvider;
import org.coderthoughts.phototools.util.StreamTools;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class Copier {
    private final BundleContext bundleContext;

    public Copier(BundleContext ctx) {
        bundleContext = ctx;
    }

    public void copy(PhotoIterable sourceIterable, String targetDirectory, String targetDateStructure, Date fromDate, Date toDate, Collection<String> selectedImageNames) throws Exception {
        System.out.println("Copying from: " + sourceIterable.getLocationString());
        System.out.println("          to: " + targetDirectory);
        System.out.println(" Date format: " + targetDateStructure);
        SimpleDateFormat sdf = new SimpleDateFormat(targetDateStructure);

        for (Entry entry : sourceIterable) {
            String name = entry.getName();
            String extension = name;
            int idx = name.lastIndexOf('.');
            if (idx >= 0)
                extension = name.substring(idx);
            extension = extension.toLowerCase();
            PhotoMetadataProvider mdp = getPhotoMetadataProvider(extension);

            boolean skipFile = name.startsWith(".");
            if (!skipFile)
                skipFile = (mdp == null);

            if (selectedImageNames != null && !selectedImageNames.contains(name))
                skipFile = true;

            if (skipFile) {
                System.out.println("Skipping " + entry.getName());
                entry.getInputStream().close(); // Get rid of the stream by closing it
                continue;
            }

            System.out.print(entry.getName() + ": ");
            InputStream is = null;
            OutputStream os = null;
            File tempFile = File.createTempFile("PhotoCopyTemp", extension);
            try {
                StreamTools.pump(entry.getInputStream(), new FileOutputStream(tempFile), 1024 * 1024);
                System.out.print(".");

                Date timestamp = null;
                timestamp = mdp.getMetaData(tempFile).getDateTaken();
                if (timestamp == null)
                    timestamp = entry.getDate();

                if (timestamp == null) {
                    System.out.println("no date, skipping.");
                    continue;
                }

                if (fromDate != null)
                    if (timestamp.getTime() < fromDate.getTime())
                        continue;

                if (toDate != null)
                    if (timestamp.getTime() >= toDate.getTime())
                        continue;

                String targetPath = sdf.format(timestamp);
                File targetDir = new File(targetDirectory, targetPath).getCanonicalFile();
                targetDir.mkdirs();
                File targetFile = getTargetFile(entry.getName(), tempFile, targetDir);
                if (targetFile == null)
                    continue;

                System.out.print("->" + targetFile.getAbsolutePath());

                is = new FileInputStream(tempFile);
                os = new FileOutputStream(targetFile);
                StreamTools.pump(is, os, 1024 * 1024);
                System.out.println(" Done.");
            } catch (IOException ioe) {
                System.err.println("Problem copying " + entry.getName());
                ioe.printStackTrace();
            } finally {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
                tempFile.delete();
            }
        }
        System.out.println("Done.");
    }

    private PhotoMetadataProvider getPhotoMetadataProvider(String extension) {
        try {
            ServiceReference[] srefs = bundleContext.getServiceReferences(PhotoMetadataProvider.class.getName(), "(format=" + extension + ")");
            if (srefs == null)
                return null;

            if (srefs.length < 1)
                return null;

            return (PhotoMetadataProvider) bundleContext.getService(srefs[0]);
        } catch (InvalidSyntaxException e) {
            return null;
        }
    }

    private static File getTargetFile(String fileName, File contentFile, File targetDir) {
        File targetFileBase = new File(targetDir, fileName);
        File targetFile = targetFileBase;
        int counter = 1;

        while (targetFile.exists()) {
            if (targetFile.length() == contentFile.length()) {
                System.out.println("Already exist - skipping: " + targetFile.getAbsolutePath());
                return null;
            } else {
                String fname = targetFileBase.getName();
                int idx = fname.lastIndexOf('.');

                String prefix, suffix;
                if (idx >= 0) {
                    prefix = fname.substring(0, idx);
                    suffix = fname.substring(idx);
                } else {
                    prefix = fname;
                    suffix = "";
                }

                targetFile = new File(targetFileBase.getParentFile(), prefix + "_" + counter + suffix);
                counter++;
                System.out.println("File with the name " +
                        fileName + " already exist, trying filename: " + targetFile.getName());
            }
        }
        return targetFile;
    }
}
