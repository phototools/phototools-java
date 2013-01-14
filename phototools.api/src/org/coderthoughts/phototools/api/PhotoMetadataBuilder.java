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

import java.io.File;
import java.util.Date;

public class PhotoMetadataBuilder {
    private Date dateTaken;
    private String details = "";
    private String gpsInfo;
    private Integer height;
    private Integer width;
    private File previewFile;

    public PhotoMetadataBuilder dateTaken(Date d) {
        dateTaken = d;
        return this;
    }

    public PhotoMetadataBuilder details(String d) {
        details = d;
        return this;
    }

    public PhotoMetadataBuilder height(Integer h) {
        height = h;
        return this;
    }

    public PhotoMetadataBuilder width(Integer w) {
        width = w;
        return this;
    }

    public PhotoMetadataBuilder gpsInfo(String g) {
        gpsInfo = g;
        return this;
    }

    public PhotoMetadataBuilder previewFile(File f) {
        previewFile = f;
        return this;
    }

    public PhotoMetadataProvider.Metadata getMetadata() {
        return new MD(dateTaken, previewFile, height, width, gpsInfo, details);
    }

    private static class MD implements PhotoMetadataProvider.Metadata {
        private final Date dateTaken;
        private final String gpsInfo;
        private final Integer height;
        private final Integer width;
        private final File previewFile;
        private final String details;

        public MD(Date dateTaken, File previewImage, Integer height, Integer width, String gpsInfo, String details) {
            this.dateTaken = dateTaken;
            this.previewFile = previewImage;
            this.height = height;
            this.width = width;
            this.gpsInfo = gpsInfo;
            this.details = details;
        }

        public Date getDateTaken() {
            return dateTaken;
        }

        public File getPreviewFile() {
            return previewFile;
        }

        public String getGPSInfo() {
            return gpsInfo;
        }

        public Integer getHeightInPixels() {
            return height;
        }

        public Integer getWidthInPixels() {
            return width;
        }

        public String getDetails() {
            return details;
        }
    }
}
