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

import java.awt.Component;
import java.awt.Window;

/**
 * For each tool appearing as a separate tab in the Photo Tool application
 * register an OSGi service contributing the Tool Panel.
 */
public interface ToolPanel {
    /**
     * The title of the tool panel.
     * @return The title.
     */
    String getTitle();

    /**
     * Obtain the panel object.
     * @param parentWindow The parent window of the Photo Tool UI.
     * @return The panel.
     */
    Component getPanel(Window parentWindow);

    /**
     * Called after the main window has been laid out and after the panel was
     * obtained. Can be used to apply some additional layout tweaks that can only
     * be done once the window has been laid out.
     * @param parentWindow The parent window of the Photo Tool UI.
     */
    void postLayout(Window parentWindow);
}
