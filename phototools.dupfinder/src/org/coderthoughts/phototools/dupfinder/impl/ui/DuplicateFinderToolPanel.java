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
package org.coderthoughts.phototools.dupfinder.impl.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.coderthoughts.phototools.api.PhotoIterable;
import org.coderthoughts.phototools.api.PhotoIterable.Entry;
import org.coderthoughts.phototools.api.PhotoMetadataProvider;
import org.coderthoughts.phototools.api.ToolPanel;
import org.coderthoughts.phototools.util.DirectoryPhotoIterable;
import org.coderthoughts.phototools.util.OSGiTools;
import org.coderthoughts.phototools.util.ui.UIUtils;
import org.coderthoughts.phototools.util.ui.WrappingFlowLayout;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class DuplicateFinderToolPanel implements ToolPanel {
    private static final String PREFERENCE_FILENAME = "dupfinder.storage";
    private static final String PREFERENCE_KEY_DIRECTORY = "directory";
    private static final String PREFERENCE_KEY_EXTENSIONS = "extensions";

    private final BundleContext bundleContext;
    private Collection<JCheckBox> extensionChecks;
    private JPanel thePanel;
    private JSplitPane toolSplitPane;

    public DuplicateFinderToolPanel(BundleContext context) {
        bundleContext = context;
    }

    @Override
    public String getTitle() {
        return "Duplicate Finder";
    }

    @Override
    public Component getPanel(final Window parentWindow) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel left = new JPanel(new BorderLayout());
        JPanel leftContents = new JPanel();
        leftContents.setLayout(new BoxLayout(leftContents, BoxLayout.Y_AXIS));
        left.add(leftContents, BorderLayout.NORTH); // add to BorderLayout.NORTH to keep contents compact
        JScrollPane leftSP = new JScrollPane(left,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        final JPanel right = new JPanel(new BorderLayout());
        right.add(new JLabel("Preview:"), BorderLayout.NORTH);
        JScrollPane rightSP = new JScrollPane(right,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        leftContents.add(new JLabel("Duplicate finder tries to find duplicate images in your collection."));
        leftContents.add(new JLabel("Select a root directory to start the process."));

        JPanel locationPNL = new JPanel(new FlowLayout(FlowLayout.LEADING));
        locationPNL.add(new JLabel("Location: "));

        final JTextField locationTF = new JTextField(35);
        locationPNL.add(locationTF);

        JButton selectBTN = new JButton("Select...");
        selectBTN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser(locationTF.getText());
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (chooser.showOpenDialog(parentWindow) == JFileChooser.APPROVE_OPTION) {
                    String path = chooser.getSelectedFile().getAbsolutePath();
                    locationTF.setText(path);
                    setPreferenceValue(PREFERENCE_KEY_DIRECTORY, path);
                    updateLocationAsync(right, path);
                }
            }
        });
        locationPNL.add(selectBTN);
        locationPNL.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftContents.add(locationPNL);

        JPanel typesPNL = new JPanel();
        typesPNL.setLayout(new BoxLayout(typesPNL, BoxLayout.Y_AXIS));

        typesPNL.setToolTipText("Select the file extensions to consider when checking " +
        		"for duplicates. Analysis on large movie files may be slow.");
        TitledBorder typesBorder = BorderFactory.createTitledBorder("File extensions");
        typesBorder.setTitlePosition(TitledBorder.TOP);
        typesPNL.setBorder(typesBorder);
        typesPNL.setAlignmentX(Component.LEFT_ALIGNMENT);

        final Map<String, JCheckBox> types = new TreeMap<String, JCheckBox>();
        for(ServiceReference ref : OSGiTools.getSortedServiceReferences(bundleContext,
                PhotoMetadataProvider.class.getName(), null)) {
            for (String ext : OSGiTools.getStringPlusProperty(ref.getProperty("format"))) {
                final JCheckBox cb = new JCheckBox(ext);
                cb.addChangeListener(new ChangeListener() {
                    boolean savedSelectionState = false;
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        if (cb.isSelected() != savedSelectionState) {
                            savedSelectionState = cb.isSelected();
                            setPreferenceValue(PREFERENCE_KEY_EXTENSIONS, getExtensionPrefValue());
                        }
                    }
                });
                types.put(ext, cb);
            }
        }
        extensionChecks = types.values();
        Collection<String> selected = parseExtensionPrefValue(getPreferenceValue(PREFERENCE_KEY_EXTENSIONS), types.keySet());
        JPanel curTypesPNL = addTypesCheckboxPanel(typesPNL);
        for (JCheckBox cb : extensionChecks) {
            if (curTypesPNL.getComponentCount() > 6) {
                curTypesPNL = addTypesCheckboxPanel(typesPNL);
            }
            cb.setSelected(selected.contains(cb.getText()));
            curTypesPNL.add(cb);
        }
        curTypesPNL = addTypesPanel(typesPNL, FlowLayout.TRAILING);
        curTypesPNL.add(getAllOrNoneButton(types.values(), true));
        curTypesPNL.add(getAllOrNoneButton(types.values(), false));
        leftContents.add(typesPNL);

        JPanel startPNL = new JPanel(new FlowLayout(FlowLayout.LEADING));
        startPNL.add(new JLabel("To start click ->"));

        JButton startBTN = new JButton("Start!");
        startBTN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!new File(locationTF.getText()).isDirectory()) {
                    JOptionPane.showMessageDialog(parentWindow, "Please specify a directory first", "No directory selected", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    RunDuplicateFinderDialog.run(parentWindow, bundleContext, locationTF.getText(), getExtensions());
                }
            }
        });
        startPNL.add(startBTN);
        startPNL.setAlignmentX(Component.LEFT_ALIGNMENT);
        startPNL.setAlignmentY(Component.TOP_ALIGNMENT);
        leftContents.add(startPNL);

        String storedLocation = getPreferenceValue(PREFERENCE_KEY_DIRECTORY);
        if (storedLocation != null) {
            locationTF.setText(storedLocation);
            updateLocationAsync(right, storedLocation);
        }

        toolSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSP, rightSP);
        panel.add(toolSplitPane);

        thePanel = panel;
        return panel;
    }

    private JPanel addTypesCheckboxPanel(JPanel parent) {
        return addTypesPanel(parent, FlowLayout.LEADING);
    }

    private JPanel addTypesPanel(JPanel parent, int alignment) {
        JPanel curTypesPNL = new JPanel(new FlowLayout(alignment));
        curTypesPNL.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(curTypesPNL);
        return curTypesPNL;
    }

    @Override
    public void postLayout(Window parentWindow) {
        toolSplitPane.setDividerLocation((int) (thePanel.getWidth() * 0.6));
    }

    private Collection<String> parseExtensionPrefValue(String preferenceValue, Collection<String> defaults) {
        if (preferenceValue == null)
            return defaults;
        else
            return Arrays.asList(preferenceValue.split(" "));
    }

    private String getExtensionPrefValue() {
        StringBuilder sb = new StringBuilder();
        for (String ext : getExtensions()) {
            sb.append(ext);
            sb.append(" ");
        }
        return sb.toString();
    }

    private Collection<String> getExtensions() {
        List<String> l = new ArrayList<String>();
        for (JCheckBox cb : extensionChecks) {
            if (cb.isSelected()) {
                l.add(cb.getText());
            }
        }
        return l;
    }

    private JButton getAllOrNoneButton(final Collection<JCheckBox> checkBoxes, final boolean all) {
        JButton btn = new JButton(all ? "All" : "None");
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (JCheckBox cb : checkBoxes) {
                    cb.setSelected(all);
                }
            }
        });
        return btn;
    }

    private void updateLocationAsync(final JPanel previewPNL, final String directory) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                PhotoIterable pi = new DirectoryPhotoIterable(directory).
                        setExtensions(getExtensions().toArray(new String[] {})).
                        freeze();
                updateLocation(previewPNL, pi);
            }
        }).start();
    }

    private void updateLocation(JPanel previewPNL, PhotoIterable pi) {
        if (pi == null)
            return;

        previewPNL.removeAll();
        JLabel label = new JLabel("Searching ...");
        previewPNL.add(label, BorderLayout.NORTH);
        JPanel imagePNL = new JPanel();
        FlowLayout flowLayout = new WrappingFlowLayout();
        flowLayout.setAlignment(FlowLayout.LEADING);
        imagePNL.setLayout(flowLayout);
        previewPNL.add(imagePNL, BorderLayout.CENTER);

        int numImages = 0;
        for (Entry entry : pi) {
            if (addImageIfWithinConstraints(imagePNL, entry))
                numImages++;

            if (numImages >= 6)
                break;
        }

        if (numImages == 0)
            label.setText("No images found at location: " + pi.getLocationString());
        else
            label.setText("Found these images: " + pi.getLocationString());
    }

    private boolean addImageIfWithinConstraints(final JPanel imagePanel, final Entry entry) {
        final JPanel p = UIUtils.getPhotoPreview(bundleContext, entry, null, null);
        if (p != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    imagePanel.add(p);
                }
            });
        }
        return p != null;
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
