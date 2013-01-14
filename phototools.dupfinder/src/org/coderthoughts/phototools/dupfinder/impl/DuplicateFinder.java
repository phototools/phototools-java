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
package org.coderthoughts.phototools.dupfinder.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.coderthoughts.phototools.api.PhotoMetadataProvider;

public class DuplicateFinder {
    private final Map<Object, List<File>> candidateMap = new HashMap<Object, List<File>>();
    private final Map<String, PhotoMetadataProvider> photoMetadataProviders;

    public DuplicateFinder(Map<String, PhotoMetadataProvider> metadataProviders) {
        photoMetadataProviders = metadataProviders;
    }

    public void addCandidate(File file) {
        if (!file.isFile())
            throw new IllegalArgumentException("Must be a file");

        for (Object key : computeKeys(photoMetadataProviders, file)) {
            synchronized (candidateMap) {
                List<File> l = candidateMap.get(key);
                if (l == null) {
                    l = new ArrayList<File>();
                    candidateMap.put(key, l);
                }
                l.add(file);
            }
        }
    }

    static Object[] computeKeys(Map<String, PhotoMetadataProvider> metadataProviders, File f) {
        String n = f.getName();
        int idx = n.lastIndexOf('.');
        if (idx < 0) {
            return new Object[] {};
        }
        String extension = n.substring(idx).toLowerCase();
        PhotoMetadataProvider pmp = metadataProviders.get(extension);
        if (pmp == null) {
            return new Object[] {};
        }

        Date d = pmp.getMetaData(f).getDateTaken();
        if (d == null) {
            // No date taken found, use file size and name as keys
            return new Object[] {f.getName().toLowerCase(), f.length()};
        }

        Calendar org = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        org.setTime(d);
        Calendar noMillis = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        noMillis.set(Calendar.YEAR, org.get(Calendar.YEAR));
        noMillis.set(Calendar.DAY_OF_MONTH, org.get(Calendar.DAY_OF_MONTH));
        noMillis.set(Calendar.MONTH, org.get(Calendar.MONTH));
        noMillis.set(Calendar.HOUR_OF_DAY, org.get(Calendar.HOUR_OF_DAY));
        noMillis.set(Calendar.MINUTE, org.get(Calendar.MINUTE));
        noMillis.set(Calendar.SECOND, org.get(Calendar.SECOND));
        noMillis.set(Calendar.MILLISECOND, 0);
        noMillis.getTimeInMillis(); // initialize the time
        return new Object[] {noMillis};
    }

    public List<List<File>> getCandidatesList() {
        // keep consolidating until no more changes
        while(consolidate() == true);

        List<List<File>> candidates = new ArrayList<List<File>>();
        for (List<File> similarFiles : candidateMap.values()) {
            if (similarFiles.size() > 1)
                candidates.add(similarFiles);
        }

        return candidates;
    }

    private synchronized boolean consolidate() {
        boolean changes = false;
        for (Iterator<Map.Entry<Object, List<File>>> it = candidateMap.entrySet().iterator(); it.hasNext(); ) {
            Entry<Object, List<File>> candidate = it.next();
            if (candidate.getValue().size() < 2)
                continue;

            boolean candidateMerged = false;
            for (Map.Entry<Object, List<File>> entry : candidateMap.entrySet()) {
                if (candidate.getKey().equals(entry.getKey()))
                    // Same guy, ignore
                    continue;

                List<File> temp = new ArrayList<File>(candidate.getValue());
                temp.retainAll(entry.getValue());
                if (temp.size() > 0) {
                    // there is overlap
                    List<File> target = entry.getValue();
                    for (File f : candidate.getValue()) {
                        if (!target.contains(f)) {
                            target.add(f);
                        }
                    }
                    candidateMerged = true;
                    break;
                }
            }

            if (candidateMerged) {
                changes = true;
                it.remove();
            }
        }
        return changes;
    }
}
