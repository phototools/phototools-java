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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * An iterator that iterates through all files in a directory tree. Note that directory entries are also
 * returned as part of this process. This iterator is not suitable for deleting directory trees as the
 * directories are returned before their content. To use this iterator for deleting a directory tree its
 * order needs to be reversed.
 */
public class DirTreeIterator implements Iterator<File> {
    Traverser traverser;
    List<File> dirs = new ArrayList<File>();

    public DirTreeIterator(File rootDir) {
        traverser = new Traverser(rootDir);
    }

    @Override
    public boolean hasNext() {
        boolean hasNext = traverser.hasNext();
        if (!hasNext)
            if (traverseToNext())
                return hasNext();

        return hasNext;
    }

    @Override
    public File next() {
        File cur = traverser.getNext();
        if (cur != null && cur.isDirectory()) {
            dirs.add(cur);
        }

        if (cur == null) {
            if (traverseToNext()) {
                return next();
            } else {
                throw new NoSuchElementException();
            }
        }

        return cur;
    }

    private boolean traverseToNext() {
        if (dirs.size() > 0) {
            traverser = new Traverser(dirs.remove(0));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    private static class Traverser {
        private File[] files;
        private int curIdx = -1;

        Traverser(File rootDir) {
            files = rootDir.listFiles();

            if (files == null)
                files = new File [] {};

            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
        }

        File getNext() {
            curIdx++;

            if (files.length > curIdx)
                return files[curIdx];
            else
                return null;
        }

        boolean hasNext() {
            return files.length > (curIdx+1);
        }
    }
}
