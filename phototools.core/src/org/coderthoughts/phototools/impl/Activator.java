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

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.jtattoo.plaf.hifi.HiFiLookAndFeel;

import org.coderthoughts.phototools.api.PhotoMetadataProvider;
import org.coderthoughts.phototools.api.PhotoSource;
import org.coderthoughts.phototools.api.ToolPanel;
import org.coderthoughts.phototools.impl.ui.PhotoToolUI;
import org.coderthoughts.phototools.impl.ui.about.AboutToolPanel;
import org.coderthoughts.phototools.impl.ui.photocopy.PhotoCopyToolPanel;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

public class Activator implements BundleActivator {
    @Override
    public void start(final BundleContext context) throws Exception {
        setupLookAndFeel(context);
        setupToolPanels(context);
        setupDirectoryPhotoSource(context);
        setupPhotoProcessors(context);

        // Start the main GUI window
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                PhotoToolUI ui = new PhotoToolUI(context);
                ui.setModal(true);
                ui.setVisible(true);
            }
        });
    }

    private void setupLookAndFeel(BundleContext context) throws UnsupportedLookAndFeelException {
        UIManager.put("ClassLoader", getClass().getClassLoader());

        Properties props = new Properties();
        props.put("logoString", ""); // Otherwise JTattoo puts something in menu bars
        HiFiLookAndFeel.setCurrentTheme(props);
        LookAndFeel lnf = new HiFiLookAndFeel();
        UIManager.setLookAndFeel(lnf);
    }

    private void setupToolPanels(BundleContext context) {
        PhotoCopyToolPanel pctp = new PhotoCopyToolPanel(context);
        Dictionary<String, Object> props1 = new Hashtable<String, Object>();
        props1.put(Constants.SERVICE_RANKING, 100);
        context.registerService(ToolPanel.class.getName(), pctp, props1);

        Dictionary<String, Object> props2 = new Hashtable<String, Object>();
        props2.put(Constants.SERVICE_RANKING, -100);
        context.registerService(ToolPanel.class.getName(), new AboutToolPanel(context), props2);
    }

    private void setupDirectoryPhotoSource(BundleContext context) {
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(Constants.SERVICE_RANKING, 100);
        context.registerService(PhotoSource.class.getName(), new DirectoryPhotoSource(), props);
    }

    private void setupPhotoProcessors(BundleContext context) {
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("format", new String [] {".jpeg", ".jpg"});
        context.registerService(PhotoMetadataProvider.class.getName(), new JPEGMetadataProvider(), props);

        // The ones below aren't really supported by anything, but they need to be copied anyway.
        Dictionary<String, Object> props2 = new Hashtable<String, Object>();
        props2.put("format", new String [] {".gif", ".png"});
        context.registerService(PhotoMetadataProvider.class.getName(), new PNGGIFMetadataProvider(), props2);

        Dictionary<String, Object> props3 = new Hashtable<String, Object>();
        props3.put("format", new String [] {".avi"});
        context.registerService(PhotoMetadataProvider.class.getName(), new AVIMetadataProvider(), props3);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
