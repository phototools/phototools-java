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
package org.coderthoughts.phototools.impl.ui.photocopy;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.coderthoughts.phototools.api.PhotoIterable;
import org.coderthoughts.phototools.util.ui.UIUtils;
import org.coderthoughts.phototools.util.ui.WrappingFlowLayout;
import org.jdesktop.swingx.JXDatePicker;
import org.osgi.framework.BundleContext;

@SuppressWarnings("serial")
public class SelectImageDialog extends JDialog {
    private final BundleContext bundleContext;
    private boolean cancelled = false;
    private Collection<String> selection;
    private final PhotoIterable photoIterable;
    private final JPanel imagePanel;
    private final List<JCheckBox> checkBoxes = Collections.synchronizedList(new ArrayList<JCheckBox>());
    public JCheckBox lastSelected;
    private final JXDatePicker fromDP;
    private final JXDatePicker toDP;
    private volatile int curIteration = 0;
    private boolean imagesLoaded;

    private SelectImageDialog(Window parentWindow, BundleContext ctx, PhotoIterable sourceIterable, Date orgFromDate, Date orgToDate, Collection<String> initial) {
        super(parentWindow);
        bundleContext = ctx;
        photoIterable = sourceIterable;
        selection = initial;
        setTitle("Available photos and videos");

        JPanel dialogPanel = new JPanel(new BorderLayout());
        setContentPane(dialogPanel);

        imagePanel = new JPanel(new WrappingFlowLayout(FlowLayout.LEADING));
        JScrollPane scrollPane = new JScrollPane(imagePanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        dialogPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        dialogPanel.add(buttonPanel, BorderLayout.SOUTH);

        JPanel selectionButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        buttonPanel.add(selectionButtonPanel, BorderLayout.WEST);

        selectionButtonPanel.add(new JLabel("Select: "));
        JButton allButton = new JButton("All");
        allButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectAll(true);
            }
        });
        selectionButtonPanel.add(allButton);

        JButton noneButton = new JButton("None");
        noneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectAll(false);
            }
        });
        selectionButtonPanel.add(noneButton);

        selectionButtonPanel.add(new JLabel("From:"));
        fromDP = createDatePicker(orgFromDate);
        selectionButtonPanel.add(fromDP);
        selectionButtonPanel.add(new JLabel("To:"));
        toDP = createDatePicker(orgToDate);
        selectionButtonPanel.add(toDP);

        JPanel closeButtonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        buttonPanel.add(closeButtonPanel, BorderLayout.EAST);

        JButton okBtn = new JButton("OK");
        okBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                curIteration++;
                setVisible(false);
            }
        });
        closeButtonPanel.add(okBtn);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelled = true;
                curIteration++;
                setVisible(false);
            }
        });
        closeButtonPanel.add(cancelBtn);

        Dimension parentSize = parentWindow.getSize();
        setSize((int) (parentSize.width * 0.9), (int) (parentSize.height * 0.9));
    }

    private JXDatePicker createDatePicker(Date initialDate) {
        final JXDatePicker dp = new JXDatePicker(initialDate);
        dp.setFormats(PhotoCopyToolPanel.DATE_PICKER_FORMAT);
        dp.setEditable(true);
        dp.addActionListener(new ActionListener() {
            long lastTime = dp.getDate() == null ? -1 : dp.getDate().getTime();

            @Override
            public void actionPerformed(ActionEvent e) {
                Date d = dp.getDate();
                long t = -1;
                if (d != null)
                    t = d.getTime();

                if (t != lastTime) {
                    lastTime = t;
                    if (imagesLoaded)
                        selection = getSelectedImages();
                    loadImagesAsync();
                }
            }
        });
        dp.getEditor().addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                try {
                    dp.commitEdit();
                } catch (ParseException pe) {
                }
            }
        });
        return dp;
    }

    private Collection<String> getSelectedImages() {
        Collection<String> l = new HashSet<String>(checkBoxes.size());
        synchronized(checkBoxes) {
            for (JCheckBox cb : checkBoxes) {
                if (cb.isSelected()) {
                    String name = cb.getText();
                    if (l.contains(name)) {
                        JOptionPane.showMessageDialog(this,
                                "Warning: the selected set of images contains an image with the name '" + name + "' more than once. " +
                                "Only one resouce with this name will be processed.", "Multiple images with the same name", JOptionPane.WARNING_MESSAGE);
                    } else {
                        l.add(name);
                    }
                }
            }
        }
        return l;
    }

    void selectAll(boolean select) {
        synchronized (checkBoxes) {
            for (JCheckBox cb : checkBoxes) {
                cb.setSelected(select);
            }
        }
    }

    void selectAllInBetween(JCheckBox valueDefiningCB, JCheckBox otherCB) {
        synchronized (checkBoxes) {
            boolean in = false;
            for (JCheckBox cb : checkBoxes) {
                boolean prevIn = in;
                if (cb == valueDefiningCB || cb == otherCB) {
                    in = !in;
                }
                if (in || prevIn)
                    cb.setSelected(valueDefiningCB.isSelected());
            }
        }
    }

    private void loadImagesAsync() {
        curIteration++;
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadImages();
            }
        }).start();
    }

    private void loadImages() {
        final int iteration = curIteration; // record the current iteration, if a new iteration is started we need to stop this action
        imagesLoaded = false;
        checkBoxes.clear();
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    imagePanel.removeAll();
                    imagePanel.repaint();
                }
            });
        } catch (Exception ex) {
        }

        for (PhotoIterable.Entry entry : photoIterable) {
            if (iteration != curIteration)
                return; // the window was closed or needs to be rebuilt

            if (Runtime.getRuntime().freeMemory() < 5 * 1024 * 1024) {
                // If less than 5MB left, stop adding images
                final JTextArea ta = new JTextArea("No memory for more images. Increase Java Heap space to select from a larger list.");
                ta.setEditable(false);
                ta.setLineWrap(true);
                ta.setWrapStyleWord(true);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        imagePanel.add(ta);
                        imagePanel.revalidate();
                    }
                });
                System.gc();
                return;
            }

            final JPanel previewPanel = new JPanel();
            JCheckBox cb = UIUtils.getPhotoPreview(bundleContext, entry, fromDP.getDate(), toDP.getDate(), previewPanel);
            if (cb != null) {
                if (selection == null || selection.contains(cb.getText()))
                    cb.setSelected(true);
                else
                    cb.setSelected(false);

                StickySelectionListener ssl = new StickySelectionListener(cb);
                for (Component c : previewPanel.getComponents()) {
                    c.addMouseListener(ssl);
                }
                checkBoxes.add(cb);
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            imagePanel.add(previewPanel);
                            imagePanel.revalidate();
                        }
                    });
                } catch (Exception e) {
                }
            }
        }
        imagesLoaded = true;
    }

    private class StickySelectionListener extends MouseAdapter {
        private final JCheckBox checkBox;

        public StickySelectionListener(JCheckBox associatedCheckBox) {
            checkBox = associatedCheckBox;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if ((e.getModifiers() & InputEvent.SHIFT_MASK) > 0) {
                if (lastSelected != null) {
                    selectAllInBetween(checkBox, lastSelected);
                }
            }
            lastSelected = checkBox;
        }
    }

    static Collection<String> selectImages(BundleContext ctx, Window parentWindow, final PhotoIterable sourceIterable, JXDatePicker parentFromDP, JXDatePicker parentToDP, Collection<String> initialSelection) {
        SelectImageDialog dialog = new SelectImageDialog(parentWindow, ctx, sourceIterable, parentFromDP.getDate(), parentToDP.getDate(), initialSelection);
        dialog.setLocationByPlatform(true);
        dialog.setModal(true);
        dialog.loadImagesAsync();
        dialog.setVisible(true);
        if (!dialog.cancelled) {
            parentFromDP.setDate(dialog.fromDP.getDate());
            parentToDP.setDate(dialog.toDP.getDate());
            return dialog.getSelectedImages();
        } else {
            return null;
        }
    }
}
