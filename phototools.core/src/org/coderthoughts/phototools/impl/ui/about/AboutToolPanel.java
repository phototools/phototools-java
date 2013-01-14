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
package org.coderthoughts.phototools.impl.ui.about;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.coderthoughts.phototools.api.AboutInfo;
import org.coderthoughts.phototools.api.ToolPanel;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class AboutToolPanel implements ToolPanel {
    private final BundleContext bundleContext;

    public AboutToolPanel(BundleContext ctx) {
        bundleContext = ctx;
    }

    @Override
    public Component getPanel(Window parentWindow) {
        JPanel panel = new JPanel(new BorderLayout());
        final JTextArea ta = new JTextArea();
        ta.setEditable(false);
        panel.add(ta, BorderLayout.CENTER);
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                initAboutBox(ta);
            }
        });

        return panel;
    }

    @Override
    public void postLayout(Window parentWindow) {
        // Nothing to do
    }

    protected void initAboutBox(JTextArea ta) {
        StringBuilder sb = new StringBuilder();
        sb.append("Photo Tools (c) 2012, 2013 David Bosschaert\n\n");

        kudosToOSGiFramework(sb);
        kudosToEmbeddedLibs(sb);
        listBundles(sb);

        ta.setText(sb.toString());
    }

    private void kudosToOSGiFramework(StringBuilder sb) {
        sb.append("Running OSGi framework: ");
        sb.append(bundleContext.getBundle(0).getSymbolicName());
        sb.append(" <- Kudos!\n\n");
    }

    private void kudosToEmbeddedLibs(StringBuilder sb) {
        sb.append("Thanks to the following projects:\n");
        sb.append(" * www.jtattoo.net for the Look and Feel\n");
        sb.append(" * swingx.java.net for some extra swing widgets\n");
        sb.append(" * http://code.google.com/p/metadata-extractor for the JPEG metadata handling\n");
        sb.append(" * Adobe for xmpcore.jar (included in metadata extractor)\n\n");

        sb.append("The following components are embedded in installed bundles:\n");
        ServiceReference[] refs = null;
        try {
            refs = bundleContext.getServiceReferences(AboutInfo.class.getName(), null);
        } catch (InvalidSyntaxException e) {
            // no filter, will not happen
        }
        if (refs != null) {
            for (ServiceReference ref : refs) {
                AboutInfo info = (AboutInfo) bundleContext.getService(ref);
                if (info != null) {
                    for (String lib : info.embeddedLibraries()) {
                        sb.append(" * " + lib + " [in " + ref.getBundle().getSymbolicName() + "]\n");
                    }
                }
            }
        }

        sb.append("\n");
    }

    private void listBundles(StringBuilder sb) {
        sb.append("Bundles in the system:\n");
        Map<Long, String> bundles = new TreeMap<Long, String>();
        for (Bundle b : bundleContext.getBundles()) {
            bundles.put(b.getBundleId(), b.getSymbolicName() + " " + b.getHeaders().get(Constants.BUNDLE_VERSION)+ " (" + getStatus(b) + ")");
        }

        for (Long id : bundles.keySet()) {
            sb.append("   " + id + " " + bundles.get(id) + "\n");
        }
        sb.append("\n");
    }

    private String getStatus(Bundle b) {
        switch(b.getState()) {
        case Bundle.UNINSTALLED:
            return "uninstalled";
        case Bundle.INSTALLED:
            return "installed";
        case Bundle.RESOLVED:
            return "resolved";
        case Bundle.STARTING:
            return "starting";
        case Bundle.STOPPING:
            return "stopping";
        case Bundle.ACTIVE:
            return "active";
        }
        return "unknown";
    }

    @Override
    public String getTitle() {
        return "About";
    }
}
