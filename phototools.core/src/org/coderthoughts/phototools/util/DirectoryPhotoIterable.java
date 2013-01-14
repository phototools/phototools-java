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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.coderthoughts.phototools.api.PhotoIterable;

public class DirectoryPhotoIterable implements PhotoIterable {
    private final String rootLocation;
    private String[] extensions = null;
    private volatile boolean freezeCalled = false;

    public DirectoryPhotoIterable(String rootLocation) {
        this.rootLocation = rootLocation;
    }

    @Override
    public String getLocationString() {
        return rootLocation;
    }

    @Override
    public DirectoryPhotoIterable freeze() {
        freezeCalled = true;
        return this;
    }

    @Override
    public DirectoryPhotoIterable setExtensions(String... extensions) {
        if (freezeCalled)
            throw new IllegalStateException("Cannot set extensions after calling freeze");
        this.extensions = extensions;
        return this;
    }

    @Override
    public Iterator<Entry> iterator() {
        if (!freezeCalled)
            throw new IllegalStateException("Must call freeze before obtaining an iterator");

        return new Iterator<Entry>(){
            private DirTreeIterator dirTreeIterator = new DirTreeIterator(new File(rootLocation));
            private File nextFile;

            @Override
            public boolean hasNext() {
                if (nextFile == null) {
                    getNextFile();
                }
                return nextFile != null;
            }

            private void getNextFile() {
                while (dirTreeIterator.hasNext()) {
                    File f = dirTreeIterator.next();
                    if (f.isFile()) {
                        boolean allowedExtension = false;
                        if (extensions != null) {
                            for (String ext : extensions) {
                                if (f.getName().toLowerCase().endsWith(ext.toLowerCase())) {
                                    allowedExtension = true;
                                    break;
                                }
                            }
                        }

                        if (extensions == null || allowedExtension) {
                            nextFile = f;
                            return;
                        }
                    }
                }
            }

            @Override
            public Entry next() {
                if (nextFile == null)
                    getNextFile();

                if (nextFile == null)
                    throw new NoSuchElementException();

                final String filePath = nextFile.getAbsolutePath();

                try {
                    return new Entry(nextFile.getName(), new Date(nextFile.lastModified()), new FileInputStream(nextFile)) {
                        @Override
                        public Date getDate() {
                            // This operation is expensive, so only invoke if needed
                            Date creationDate = FileTools.getFileModificationDate(new File(filePath));
                            if (creationDate != null)
                                return creationDate;
                            else
                                return super.getDate(); // will return the last modified
                        }
                    };
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } finally {
                    nextFile = null;
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public String toString() {
        return rootLocation;
    }
}
