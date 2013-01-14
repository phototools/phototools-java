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

import java.io.File;
import java.io.IOException;

import org.coderthoughts.phototools.api.PhotoMetadataBuilder;
import org.coderthoughts.phototools.api.PhotoMetadataProvider;
import org.coderthoughts.phototools.util.JPEGTools;

public class JPEGMetadataProvider implements PhotoMetadataProvider {
    @Override
    public Metadata getMetaData(File f) {
        try {
            JPEGTools jpt = JPEGTools.getJPEGTools(f);

            PhotoMetadataBuilder mdBuilder = new PhotoMetadataBuilder();
            return mdBuilder.dateTaken(jpt.getDateTaken()).
                previewFile(f).
                height(jpt.getHeight()).
                width(jpt.getWidth()).
                gpsInfo(jpt.getGPSInfo()).
                details(jpt.getDetails()).getMetadata();
        } catch (IOException e) {
            return null;
        }
    }
}
