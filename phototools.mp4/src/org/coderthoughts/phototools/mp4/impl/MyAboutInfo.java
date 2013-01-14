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

import org.coderthoughts.phototools.api.AboutInfo;

public class MyAboutInfo implements AboutInfo {
    @Override
    public String[] embeddedLibraries() {
        return new String [] {"mp4parser (http://mp4parser.googlecode.com)"};
    }
}
