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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamTools {
    private StreamTools() {}

    public static void pump(InputStream is, OutputStream os) throws IOException {
        pump(is, os, 16384);
    }

    public static void pump(InputStream is, OutputStream os, int bufferSize) throws IOException {
        byte[] bytes = new byte[bufferSize];

        int length = 0;
        int offset = 0;

        while ((length = is.read(bytes, offset, bytes.length - offset)) != -1) {
            offset += length;

            if (offset == bytes.length) {
                os.write(bytes, 0, bytes.length);
                offset = 0;
            }
        }
        if (offset != 0) {
            os.write(bytes, 0, offset);
        }

        is.close();
        os.close();
    }
}
