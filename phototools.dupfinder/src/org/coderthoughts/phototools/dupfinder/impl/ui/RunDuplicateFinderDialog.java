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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.coderthoughts.phototools.api.PhotoMetadataProvider;
import org.coderthoughts.phototools.api.PhotoMetadataProvider.Metadata;
import org.coderthoughts.phototools.dupfinder.impl.DuplicateFinder;
import org.coderthoughts.phototools.util.DirTreeIterable;
import org.coderthoughts.phototools.util.FileTools;
import org.coderthoughts.phototools.util.OSGiTools;
import org.coderthoughts.phototools.util.ui.WrappingFlowLayout;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@SuppressWarnings("serial")
public class RunDuplicateFinderDialog extends JDialog {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
    private static final DecimalFormat DOUBLE_FORMATTER = new DecimalFormat("#.##");

    private volatile boolean closed = false;
    private final Map<String, PhotoMetadataProvider> metadataProviders;
    private final String rootLocation;
    private final JLabel statusLabel;
    private final JProgressBar progressBar;
    private ListIterator<List<File>> candidateIterator;
    private JPanel imagePanel;
    private boolean lastActionPrev = false;
    private Font smallerFont;

    public RunDuplicateFinderDialog(Window parentWindow, String root, Map<String, PhotoMetadataProvider> pmps) {
        super(parentWindow);
        rootLocation = root;
        metadataProviders = pmps;
        setTitle("Duplicate Finder");

        JPanel dialogPanel = new JPanel(new BorderLayout());
        setContentPane(dialogPanel);

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel progressLabel = new JLabel("Progress: ");
        topPanel.add(progressLabel, BorderLayout.WEST);
        progressBar = new JProgressBar();
        topPanel.add(progressBar, BorderLayout.CENTER);
        statusLabel = new JLabel();
        topPanel.add(statusLabel, BorderLayout.SOUTH);
        dialogPanel.add(topPanel, BorderLayout.NORTH);
        smallerFont = new Font(progressLabel.getFont().getName(), 0, progressLabel.getFont().getSize() - 1);

        imagePanel = new JPanel(new WrappingFlowLayout(FlowLayout.LEADING));
        JScrollPane scrollPane = new JScrollPane(imagePanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        dialogPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        dialogPanel.add(buttonPanel, BorderLayout.SOUTH);

        JPanel browseButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        buttonPanel.add(browseButtonPanel, BorderLayout.WEST);

        JButton previousBTN = new JButton("<");
        previousBTN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPreviousCandidate();
            }
        });
        browseButtonPanel.add(previousBTN);

        JButton nextBTN = new JButton(">");
        nextBTN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showNextCandidate();
            }
        });
        browseButtonPanel.add(nextBTN);

        JPanel closeButtonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        buttonPanel.add(closeButtonPanel, BorderLayout.EAST);

        JButton closeBTN = new JButton("Close");
        closeBTN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closed = true;
                setVisible(false);
            }
        });
        closeButtonPanel.add(closeBTN);

        Dimension parentSize = parentWindow.getSize();
        setSize((int) (parentSize.width * 0.9), (int) (parentSize.height * 0.9));
    }

    private void initializeDuplicateReview(List<List<File>> candidates) {
        progressBar.setMaximum(candidates.size());
        progressBar.setValue(0);
        statusLabel.setText("Click on an image for more details...");
        candidateIterator = candidates.listIterator();
        showNextCandidate();
    }

    private void showNextCandidate() {
        if (lastActionPrev) {
            lastActionPrev = false;
            // call next again to avoid showing the same candidate twice
            candidateIterator.next();
        }

        if (!candidateIterator.hasNext()) {
            JOptionPane.showMessageDialog(this, "No more duplicates found.", "Duplicate Finder", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int idx = candidateIterator.nextIndex();
        List<File> files = candidateIterator.next();
        showCandidate(files, idx);
    }

    private void showPreviousCandidate() {
        if (!lastActionPrev) {
            lastActionPrev = true;
            // call previous again to avoid showing the same candidate twice
            candidateIterator.previous();
        }

        if (!candidateIterator.hasPrevious()) {
            JOptionPane.showMessageDialog(this, "At the beginning of the list.", "Duplicate Finder", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int idx = candidateIterator.previousIndex();
        List<File> files = candidateIterator.previous();
        showCandidate(files, idx);
    }

    private void showCandidate(List<File> candidates, final int idx) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                imagePanel.removeAll();
                imagePanel.repaint();
                progressBar.setValue(idx + 1);
            }
        });

        for (final File f : candidates) {
            final JPanel dupPanel = getDuplicatePanel(f);
            if (dupPanel != null) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        imagePanel.add(dupPanel);
                        imagePanel.revalidate();
                    }
                });
            }
        }
    }

    private JPanel getDuplicatePanel(final File f) {
        Metadata md = getMetadataProvider(f).getMetaData(f);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel image = new JLabel(new ImageIcon(f.getAbsolutePath())) {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(((ImageIcon)getIcon()).getImage(), 0, 0, getWidth(), getHeight(), null);
            }
        };
        int actualWidth = getWidth() / 4;
        Dimension d = new Dimension(actualWidth, getScaledHeight(md.getWidthInPixels(), md.getHeightInPixels(), actualWidth));
        image.setMaximumSize(d);
        image.setPreferredSize(d);
        image.setMaximumSize(d);
        image.setBorder(BorderFactory.createRaisedBevelBorder());
        image.setAlignmentX(Component.LEFT_ALIGNMENT);
        image.setToolTipText(f.getAbsolutePath());
        panel.add(image);
        panel.add(new JLabel(f.getName()));
        panel.add(getFineLabel("in " + f.getParentFile().getName()));
        double sz = f.length();
        sz /= 1024.0;
        if (sz > 1000) {
            panel.add(getFineLabel(DOUBLE_FORMATTER.format(sz / 1024.0) + " Mb"));
        } else {
            panel.add(getFineLabel(DOUBLE_FORMATTER.format(sz) + " Kb"));
        }

        Date date = md.getDateTaken();
        if (date == null) {
            date = FileTools.getFileModificationDate(f);
        }
        panel.add(getFineLabel(DATE_FORMAT.format(date)));
        addFineLabel(panel, "Size: ", md.getWidthInPixels(), " x ", md.getHeightInPixels());
        addFineLabel(panel, "GPS: ", md.getGPSInfo());
        final Metadata mdFinal = md;
        image.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                DetailsDialog.showDetailsDialog(RunDuplicateFinderDialog.this, f.getAbsolutePath(), mdFinal.getDetails());
            }
        });

        final JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(RunDuplicateFinderDialog.this, "Delete file: " + f.getAbsolutePath(),
                        "Confirm Delete", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
                    if (f.delete()) {
                        deleteButton.setEnabled(false);
                        JOptionPane.showMessageDialog(RunDuplicateFinderDialog.this, "File deleted.", "Delete", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(RunDuplicateFinderDialog.this, "Unable to delete: " + f.getAbsolutePath(), "Delete", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        });
        panel.add(deleteButton);
        return panel;
    }

    private int getScaledHeight(Integer widthInPixels, Integer heightInPixels, int actualWidth) {
        if (widthInPixels == null || heightInPixels == null)
            // If we don't know the dimensions of the image make it square by returning the width of the container
            return actualWidth;

        double factor = (double) actualWidth / (double) widthInPixels;
        return (int) (factor * heightInPixels);
    }

    private void addFineLabel(JPanel panel, Object ... args) {
        StringBuilder sb = new StringBuilder();
        for (Object o : args) {
            if (o == null)
                return;

            sb.append(o.toString());
        }
        panel.add(getFineLabel(sb.toString()));
    }

    private JLabel getFineLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(smallerFont);
        return label;
    }

    private PhotoMetadataProvider getMetadataProvider(File f) {
        String ext = f.getName().toLowerCase();
        int idx = ext.lastIndexOf('.');
        if (idx >= 0)
            ext = ext.substring(idx);

        return metadataProviders.get(ext);
    }

    private void findDuplicates() {
        DirTreeIterable it = new DirTreeIterable(new File(rootLocation));
        int numFiles = 0;
        for (File f : it) {
            if (!f.isFile())
                continue;

            numFiles++;
        }

        progressBar.setMaximum(numFiles);

        DuplicateFinder df = new DuplicateFinder(metadataProviders);
        progressBar.setValue(0);
        for (File f : it) {
            if (closed)
                return;

            if (!f.isFile())
                continue;

            progressBar.setValue(progressBar.getValue() + 1);
            statusLabel.setText("Searching: " + f.getAbsolutePath());
            df.addCandidate(f);
        }

        statusLabel.setText("Analyzing duplicate candidates...");
        initializeDuplicateReview(df.getCandidatesList());
    }

    public static void run(Window parentWindow, BundleContext ctx, String root, Collection<String> extensions) {
        ServiceReference[] refs = OSGiTools.getSortedServiceReferences(ctx, PhotoMetadataProvider.class.getName(), null);
        if (refs.length == 0)
            throw new IllegalStateException("No PhotoMetadataProvider instances found in Service Registry.");

        Map<String, PhotoMetadataProvider> pmps = new HashMap<String, PhotoMetadataProvider>();
        for (ServiceReference ref : refs) {
            PhotoMetadataProvider pmp = (PhotoMetadataProvider) ctx.getService(ref);
            for (String ext : OSGiTools.getStringPlusProperty(ref.getProperty("format"))) {
                if (extensions.contains(ext))
                    pmps.put(ext, pmp);
            }
        }

        final RunDuplicateFinderDialog dialog = new RunDuplicateFinderDialog(parentWindow, root, pmps);
        dialog.setLocationByPlatform(true);
        dialog.setModal(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                dialog.findDuplicates();
            }
        }).start();
        dialog.setVisible(true);
    }
}
