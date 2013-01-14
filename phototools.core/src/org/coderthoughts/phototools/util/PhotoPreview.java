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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class PhotoPreview {
    private static final SimpleDateFormat COMPACT_DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy");

    public static JPanel getPhotoPreview(final String name, Date date, File previewFile) {
        JPanel p = new JPanel();
        getPhotoPreview(name, date, previewFile, p, false);
        return p;
    }

    public static JCheckBox getPhotoPreview(final String name, Date date, File previewFile, JPanel panel) {
        return (JCheckBox) getPhotoPreview(name, date, previewFile, panel, true);
    }

    @SuppressWarnings("serial")
    public static JComponent getPhotoPreview(String name, Date date, File previewFile, JPanel panel, boolean checkBox) {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel image;
        if (previewFile != null) {
            image = new JLabel(new ImageIcon(previewFile.getAbsolutePath())) {
                public void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.drawImage(((ImageIcon)getIcon()).getImage(), 0, 0, getWidth(), getHeight(), null);
                }
            };
        } else {
            image = new JLabel("no preview");
        }

        Dimension d = new Dimension(100, 100);
        image.setMaximumSize(d);
        image.setPreferredSize(d);
        image.setMaximumSize(d);
        image.setBorder(BorderFactory.createRaisedBevelBorder());
        image.setToolTipText(COMPACT_DATE_FORMAT.format(date));
        image.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(image);
        if (checkBox) {
            final JCheckBox cb = new JCheckBox(name);
            cb.setSelected(true);
            cb.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(cb);

            image.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    cb.setSelected(!cb.isSelected());
                }
            });

            return cb;
        } else {
            JLabel label = new JLabel(name);
            panel.add(label);
            return label;
        }
    }
}
