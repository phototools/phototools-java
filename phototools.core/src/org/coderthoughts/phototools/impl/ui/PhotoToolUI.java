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
package org.coderthoughts.phototools.impl.ui;

import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.coderthoughts.phototools.api.ToolPanel;
import org.coderthoughts.phototools.util.OSGiTools;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;


@SuppressWarnings("serial")
public class PhotoToolUI extends JFrame {
    private static final String PREFERENCE_FILENAME = "photocopy.storage";
    private static final String ICON_RESOURCE = "camera_openclipart_bluePal.png";
    private static final String PREFERENCE_KEY_SELECTED_TAB = "selectedTab";

    private final BundleContext bundleContext;

    public PhotoToolUI(BundleContext ctx) {
        super("Photo Tools");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        bundleContext = ctx;
		URL iconRes = getClass().getResource(ICON_RESOURCE);
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Image image = toolkit.createImage(iconRes);
		setIconImage(image);
		handleOSX(image);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		final JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                setPreferenceValue(PREFERENCE_KEY_SELECTED_TAB, tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()));
            }
        });
		setContentPane(tabbedPane);
		tabbedPane.setOpaque(true);

		String selectedTab = getPreferenceValue(PREFERENCE_KEY_SELECTED_TAB);
		ServiceReference[] refs = OSGiTools.getSortedServiceReferences(bundleContext, ToolPanel.class.getName(), null);
        for (ServiceReference ref : refs) {
            ToolPanel tp = (ToolPanel) bundleContext.getService(ref);
            if (tp != null) {
                Component tab = tp.getPanel(this);
                tabbedPane.addTab(tp.getTitle(), tab);
                if (tp.getTitle().equals(selectedTab))
                    tabbedPane.setSelectedComponent(tab);
            }
        }

        // pack();
        setSize(950, 500);
        setLocationByPlatform(true);

        for (ServiceReference ref : refs) {
	        ToolPanel tp = (ToolPanel) bundleContext.getService(ref);
	        if (tp != null) {
	            tp.postLayout(this);
	        }
	    }
	}

    // This method sets up some non-essential OSX-specific stuff.
    private void handleOSX(final Image dockImage) {
        ClassLoader sysCL = ClassLoader.getSystemClassLoader();
        try {
            // Set up the application dock image
            Class<?> cls = sysCL.loadClass("com.apple.eawt.Application");
            Method getApplicationMethod = cls.getDeclaredMethod("getApplication");
            Object appObj = getApplicationMethod.invoke(null);
            Method setDockIconImageMethod = cls.getDeclaredMethod("setDockIconImage", Image.class);
            setDockIconImageMethod.invoke(appObj, dockImage);

            // Make the about application menu do something sensible.
            Class<?> ahCls = sysCL.loadClass("com.apple.eawt.AboutHandler");
            Method setAboutHandlerMethod = cls.getDeclaredMethod("setAboutHandler", ahCls);

            Object aboutHandler = Proxy.newProxyInstance(sysCL, new Class[] {ahCls}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    JOptionPane.showMessageDialog(PhotoToolUI.this, "Photo Tools " + bundleContext.getBundle().getHeaders().get(Constants.BUNDLE_VERSION) +
                            " (c) contributors at http://github.com/phototools.\nFor more information see the about panel in the application.", "About Photo Tools",
                            JOptionPane.INFORMATION_MESSAGE, new ImageIcon(getClass().getResource("camera_icon_openclipart_bluePal.png")));
                    return null;
                }
            });

            setAboutHandlerMethod.invoke(appObj, new Object[] {aboutHandler});

            // This can be used to set the badge, can be useful later.
            // Method setDockIconBadgeMethod = cls.getDeclaredMethod("setDockIconBadge", String.class);
            // setDockIconBadgeMethod.invoke(appObj, "42");
        } catch (Throwable th) {
            // not OSX, ignore
        }
    }

    private String getPreferenceValue(String key) {
        return getPreferences().getProperty(key);
    }

    private void setPreferenceValue(String key, String value) {
        Properties prefs = getPreferences();
        prefs.setProperty(key, value);
        try {
            prefs.store(new FileOutputStream(bundleContext.getDataFile(PREFERENCE_FILENAME)), getClass().getName() + " preferences");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Properties getPreferences() {
        File stgFile = bundleContext.getDataFile(PREFERENCE_FILENAME);
        Properties prefs = new Properties();
        if (stgFile.exists()) {
            try {
                prefs.load(new FileInputStream(stgFile));
            } catch (IOException e) {
            }
        }
        return prefs;
    }
}

