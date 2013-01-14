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
import java.io.IOException;
import java.util.Date;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.jpeg.JpegDirectory;

public class JPEGTools {
    private final Metadata metadata;

    private JPEGTools(Metadata md) {
        metadata = md;
    }

    public static JPEGTools getJPEGTools(File file) throws IOException {
        try {
            return new JPEGTools(ImageMetadataReader.readMetadata(file));
        } catch (ImageProcessingException e) {
            System.out.println("Cannot read metadata from file: " + file);
            return null;
        }
    }

    public Date getDateTaken() {
        ExifSubIFDDirectory directory = metadata.getDirectory(ExifSubIFDDirectory.class);
        if (directory != null) {
            Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            if (date != null)
                return date;
        }

        ExifIFD0Directory dir2 = metadata.getDirectory(ExifIFD0Directory.class);
        if (dir2 != null)
            return dir2.getDate(ExifIFD0Directory.TAG_DATETIME);

        return null;
    }

    public String getGPSInfo() {
        GpsDirectory dir = metadata.getDirectory(GpsDirectory.class);
        if (dir != null) {
            GeoLocation loc = dir.getGeoLocation();
            if (loc != null)
                return "info present";
        }
        return null;
    }

    public Integer getHeight() {
        JpegDirectory dir = metadata.getDirectory(JpegDirectory.class);
        if (dir != null) {
            return dir.getInteger(JpegDirectory.TAG_JPEG_IMAGE_HEIGHT);
        }
        return null;
    }

    public Integer getWidth() {
        JpegDirectory dir = metadata.getDirectory(JpegDirectory.class);
        if (dir != null) {
            return dir.getInteger(JpegDirectory.TAG_JPEG_IMAGE_WIDTH);
        }
        return null;
    }

    public String getDetails() {
        StringBuilder sb = new StringBuilder();
        for (Directory d : metadata.getDirectories()) {
            sb.append(d.getName() + "\n");
            for (Tag t : d.getTags()) {
                String value = d.getString(t.getTagType());
                if (value.contains("@"))
                    // not a value, just an object reference
                    continue;
                sb.append("  " + t.getTagName() + ":" + value + "\n");
            }
        }
        return sb.toString();
    }
}
