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
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.coderthoughts.phototools.api.PhotoMetadataBuilder;
import org.coderthoughts.phototools.api.PhotoMetadataProvider;
import org.coderthoughts.phototools.impl.JPEGMetadataProvider;
import org.coderthoughts.phototools.impl.ui.photocopy.PhotoCopyToolPanel;
import org.coderthoughts.phototools.util.DirTreeIterable;
import org.coderthoughts.phototools.util.DirTreeIterator;
import org.coderthoughts.phototools.util.DirectoryPhotoIterable;
import org.coderthoughts.phototools.util.FileTools;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class CopierTest extends TestCase {
    public void testCopier() throws Exception {
        File tempDir = createTempDir();

        try {
            BundleContext ctx = Mockito.mock(BundleContext.class);
            JPEGMetadataProvider jpegSvc = new JPEGMetadataProvider();
            registerMetadataProviderService(ctx, jpegSvc, "(format=.jpg)");

            PhotoMetadataProvider testMp = new PhotoMetadataProvider() {
                @Override
                public Metadata getMetaData(File f) {
                    return new PhotoMetadataBuilder().getMetadata();
                }
            };
            registerMetadataProviderService(ctx, testMp, "(format=.jpeg)");
            registerMetadataProviderService(ctx, new DummyMetadataProvider(), "(format=.png)");

            Copier copier = new Copier(ctx);

            String tds = PhotoCopyToolPanel.DEFAULT_TARGET_DATE_STRUCTURE;
            URL imageDirURL = getClass().getResource("images");
            String imageDir = imageDirURL.getFile();
            copier.copy(new DirectoryPhotoIterable(imageDir).freeze(), tempDir.getAbsolutePath(),
                    tds, null, null, null);

            assertTrue(new File(tempDir, getFileModificationDate(tds, imageDir + "/PhotoTools.png") + "PhotoTools.png").isFile());
            assertTrue(new File(tempDir, "2012/2012-05-30/image1.jpg").isFile());
            assertTrue(new File(tempDir, "2012/2012-05-30/image1_1.jpg").isFile());
            assertTrue(new File(tempDir, getFileModificationDate(tds, imageDir + "/sub/subsub/image1.jpeg") + "image1.jpeg").isFile());

            Collection<String> expectedFiles =
                    Arrays.asList("PhotoTools.png", "image1.jpg", "image1_1.jpg","image1.jpeg");
            for (File f : new DirTreeIterable(tempDir)) {
                if (f.isDirectory())
                    continue;
                if (!expectedFiles.contains(f.getName()))
                    fail(f.getAbsolutePath());
            }

            Iterator<File> it = new DirTreeIterator(tempDir);
            assertTrue(it.hasNext());
        } finally {
            deleteDirectoryTree(tempDir);
            assertEquals("Should contain no files: " + Arrays.toString(tempDir.listFiles()),
                   0, tempDir.listFiles().length);
            assertTrue(tempDir.delete());
        }
    }

    public void testCopierImgSelection() throws Exception {
        File tempDir = createTempDir();

        try {
            BundleContext ctx = Mockito.mock(BundleContext.class);
            JPEGMetadataProvider jpegSvc = new JPEGMetadataProvider();
            registerMetadataProviderService(ctx, jpegSvc, "(format=.jpg)");

            Copier copier = new Copier(ctx);

            URL imageDirURL = getClass().getResource("images");
            String imageDir = imageDirURL.getFile();
            copier.copy(new DirectoryPhotoIterable(imageDir).freeze(), tempDir.getAbsolutePath(),
                    PhotoCopyToolPanel.DEFAULT_TARGET_DATE_STRUCTURE, null, null,
                    Arrays.asList("image1.jpg", "textfile.txt"));

            assertTrue(new File(tempDir, "2012/2012-05-30/image1.jpg").isFile());
            assertTrue(new File(tempDir, "2012/2012-05-30/image1_1.jpg").isFile());

            Collection<String> expectedFiles = Arrays.asList("image1.jpg", "image1_1.jpg");
            for (File f : new DirTreeIterable(tempDir)) {
                if (f.isDirectory())
                    continue;
                if (!expectedFiles.contains(f.getName()))
                    fail(f.getAbsolutePath());
            }

            Iterator<File> it = new DirTreeIterator(tempDir);
            assertTrue(it.hasNext());
        } finally {
            deleteDirectoryTree(tempDir);
            assertEquals("Should contain no files: " + Arrays.toString(tempDir.listFiles()),
                   0, tempDir.listFiles().length);
            assertTrue(tempDir.delete());
        }
    }

    public void testCopierDateSelection() throws Exception {
        File tempDir = createTempDir();

        try {
            BundleContext ctx = Mockito.mock(BundleContext.class);
            JPEGMetadataProvider jpegSvc = new JPEGMetadataProvider();
            registerMetadataProviderService(ctx, jpegSvc, "(format=.jpg)");
            registerMetadataProviderService(ctx, jpegSvc, "(format=.jpeg)");
            registerMetadataProviderService(ctx, new DummyMetadataProvider(), "(format=.png)");

            Copier copier = new Copier(ctx);

            String tds = PhotoCopyToolPanel.DEFAULT_TARGET_DATE_STRUCTURE;
            URL imageDirURL = getClass().getResource("images");
            String imageDir = imageDirURL.getFile();
            copier.copy(new DirectoryPhotoIterable(imageDir).freeze(), tempDir.getAbsolutePath(),
                    tds, getDate(1, 5, 2012), getDate(1, 6, 2012), null);

            Set<File> expectedFiles = new HashSet<File>(Arrays.asList(new File(tempDir, "2012/2012-05-30/image1.jpg"),
                    new File(tempDir, "2012/2012-05-30/image1_1.jpg")));
            Set<File> actualFiles = new HashSet<File>();
            for (File f : new DirTreeIterable(tempDir)) {
                if (f.isFile())
                    actualFiles.add(f);
            }
            assertEquals(expectedFiles, actualFiles);
        } finally {
            deleteDirectoryTree(tempDir);
            assertEquals("Should contain no files: " + Arrays.toString(tempDir.listFiles()),
                   0, tempDir.listFiles().length);
            assertTrue(tempDir.delete());
        }
    }

    public void testCopierDateSelection2() throws Exception {
        File tempDir = createTempDir();

        try {
            BundleContext ctx = Mockito.mock(BundleContext.class);
            JPEGMetadataProvider jpegSvc = new JPEGMetadataProvider();
            registerMetadataProviderService(ctx, jpegSvc, "(format=.jpg)");
            registerMetadataProviderService(ctx, jpegSvc, "(format=.jpeg)");
            registerMetadataProviderService(ctx, new DummyMetadataProvider(), "(format=.png)");

            Copier copier = new Copier(ctx);

            String tds = PhotoCopyToolPanel.DEFAULT_TARGET_DATE_STRUCTURE;
            URL imageDirURL = getClass().getResource("images");
            String imageDir = imageDirURL.getFile();
            copier.copy(new DirectoryPhotoIterable(imageDir).freeze(), tempDir.getAbsolutePath(),
                    tds, getDate(16, 8, 2012), null, null);

            Set<File> expectedFiles = new HashSet<File>(Arrays.asList(new File(tempDir, "2012/2012-08-16/image1.jpeg"),
                    new File(tempDir, getFileModificationDate(tds, imageDir + "/PhotoTools.png") + "PhotoTools.png")));
            Set<File> actualFiles = new HashSet<File>();
            for (File f : new DirTreeIterable(tempDir)) {
                if (f.isFile())
                    actualFiles.add(f);
            }
            assertEquals(expectedFiles, actualFiles);
        } finally {
            deleteDirectoryTree(tempDir);
            assertEquals("Should contain no files: " + Arrays.toString(tempDir.listFiles()),
                   0, tempDir.listFiles().length);
            assertTrue(tempDir.delete());
        }
    }

    private String getFileModificationDate(String dataStructure, String filePath) {
        Date d = FileTools.getFileModificationDate(new File(filePath));
        SimpleDateFormat sdf = new SimpleDateFormat(dataStructure);
        return sdf.format(d);
    }

    private void registerMetadataProviderService(BundleContext ctx, PhotoMetadataProvider svc, String lookupFilter) throws InvalidSyntaxException {
        ServiceReference jpegSvcRef = Mockito.mock(ServiceReference.class);
        Mockito.when(ctx.getServiceReferences(PhotoMetadataProvider.class.getName(), lookupFilter)).
            thenReturn(new ServiceReference[] {jpegSvcRef});
        Mockito.when(ctx.getService(jpegSvcRef)).thenReturn(svc);
    }

    private File createTempDir() throws IOException {
        File tempDir = File.createTempFile(getClass().getSimpleName(), ".dir");
        tempDir.delete();
        tempDir.mkdirs();
        assertTrue(tempDir.isDirectory());
        return tempDir;
    }

    private void deleteDirectoryTree(File root) {
        List<File> l = new ArrayList<File>();
        for (File f : new DirTreeIterable(root)) {
            l.add(f);
        }

        if (l.size() == 0)
            return;
        for (int i=l.size()-1; i >= 0; i--) {
            assertTrue(l.get(i).delete());
        }
    }

    private Date getDate(int day, int month, int year) {
        // Note that month is 1-based (January = 1)
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.YEAR, year);
        return c.getTime();
    }

    private static class DummyMetadataProvider implements PhotoMetadataProvider {
        @Override
        public Metadata getMetaData(File f) {
            return new PhotoMetadataBuilder().getMetadata();
        }
    }
}
