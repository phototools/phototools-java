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
package org.coderthoughts.phototools.mp4.impl;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;

import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.TrackMetaData;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import org.coderthoughts.phototools.api.PhotoMetadataBuilder;
import org.coderthoughts.phototools.api.PhotoMetadataProvider;

public class MP4MetadataProvider implements PhotoMetadataProvider {
    @Override
    public Metadata getMetaData(File f) {
        Date date = null;
        File previewFile = null;
        Integer height = null, width = null;

        RandomAccessFile raf = null;
        try {
             raf = new RandomAccessFile(f, "r");
             Movie video = MovieCreator.build(raf.getChannel());
             for (Track track : video.getTracks()) {
                 if (!track.isEnabled())
                     continue;

                 TrackMetaData md = track.getTrackMetaData();
                 date = md.getCreationTime();
                 previewFile = getPreviewFile(track);
                 height = (int) md.getHeight();
                 width = (int) md.getWidth();
             }

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            closeCloseable(raf);
        }

        return new PhotoMetadataBuilder().
                dateTaken(date).
                previewFile(previewFile).
                height(height).
                width(width).getMetadata();
    }

    private File getPreviewFile(Track track) throws IOException {
        for (ByteBuffer sample : track.getSamples()) {
            File tempFile = File.createTempFile("Preview", ".jpg");
            FileChannel fc = null;
            try {
                fc = new FileOutputStream(tempFile).getChannel();
                fc.write(sample);

                if (isJPEGFile(tempFile)) {
                    tempFile.deleteOnExit();
                    return tempFile;
                } else {
                    tempFile.delete();
                }
            } finally {
                closeCloseable(fc);
            }
        }

        return null;
    }

    private boolean isJPEGFile(File tempFile) throws IOException {
        RandomAccessFile rf = new RandomAccessFile(tempFile, "r");
        try {
            // is it a JPEG file? Check for the magic start (FFD8) and end (FFD9) markers.
            if (rf.read() == 255) {
                if (rf.read() == 216) {
                    rf.seek(rf.length() - 2);
                    if (rf.read() == 255) {
                        if (rf.read() == 217) {
                            return true;
                        }
                    }
                }
            }
        } finally {
            rf.close();
        }
        return false;
    }

    private void closeCloseable(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
