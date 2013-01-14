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
 */package org.coderthoughts.phototools.dupfinder.impl.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class DetailsDialog extends JDialog {
    private DetailsDialog(Window parentWindow, String title, String details) {
        super(parentWindow, title);

        JPanel panel = new JPanel(new BorderLayout());
        setContentPane(panel);

        JTextArea ta = new JTextArea(details);
        Font smallerFont = new Font(ta.getFont().getName(), 0, ta.getFont().getSize() - 1);
        ta.setFont(smallerFont);
        ta.setEditable(false);
        ta.setAutoscrolls(true);
        JScrollPane scroller = new JScrollPane(ta);
        panel.add(scroller, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        buttonPanel.add(closeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        Dimension parentSize = parentWindow.getSize();
        setSize((int) (parentSize.width * 0.9), (int) (parentSize.height * 0.9));
    }

    static void showDetailsDialog(Window parentWindow, String title, String details) {
        DetailsDialog dialog = new DetailsDialog(parentWindow, title, details);
        dialog.setLocationByPlatform(true);
        dialog.setModal(true);
        dialog.setVisible(true);
    }
}
