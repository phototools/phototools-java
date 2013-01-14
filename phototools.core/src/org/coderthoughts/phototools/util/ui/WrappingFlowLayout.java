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
package org.coderthoughts.phototools.util.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * A flow layout that wraps properly when put in a JScrollPane that only scrolls vertically.
 */
@SuppressWarnings("serial")
public class WrappingFlowLayout extends FlowLayout {
    public WrappingFlowLayout() {
        super();
    }

    public WrappingFlowLayout(int align) {
        super(align);
    }

    @Override
    public Dimension minimumLayoutSize(Container target)
    {
        Dimension minimum = computeSize(target, true);
        minimum.width = minimum.width - getHgap() + 1;
        return minimum;
    }

    @Override
    public Dimension preferredLayoutSize(Container target)
    {
        return computeSize(target, false);
    }

    private Dimension computeSize(Container targetContainer, boolean minimum)
    {
        int targetWidth = targetContainer.getSize().width;

        if (targetWidth == 0)
            targetWidth = Integer.MAX_VALUE;

        int hgap = getHgap();
        int vgap = getVgap();
        Insets insets = targetContainer.getInsets();
        int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
        int maxWidth = targetWidth - horizontalInsetsAndGap;

        Dimension dimension = new Dimension(0, 0);
        int rowWidth = 0;
        int rowHeight = 0;

        int componentCount = targetContainer.getComponentCount();
        for (int i = 0; i < componentCount; i++)
        {
            Component component = targetContainer.getComponent(i);

            if (component.isVisible())
            {
                Dimension d;
                if (minimum)
                    d = component.getMinimumSize();
                else
                    d = component.getPreferredSize();

                if (rowWidth + d.width > maxWidth)
                {
                    addRow(dimension, rowWidth, rowHeight);
                    rowWidth = 0;
                    rowHeight = 0;
                }

                if (rowWidth != 0)
                    rowWidth += hgap;

                rowWidth += d.width;
                rowHeight = Math.max(rowHeight, d.height);
            }
        }
        addRow(dimension, rowWidth, rowHeight);

        dimension.width += horizontalInsetsAndGap;
        dimension.height += insets.top + insets.bottom + vgap * 2;

        Container scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane.class, targetContainer);
        if (scrollPane != null)
            dimension.width -= (hgap + 1);

        return dimension;
    }

    private void addRow(Dimension dimension, int width, int height)
    {
        dimension.width = Math.max(dimension.width, width);

        if (dimension.height > 0)
            dimension.height += getVgap();

        dimension.height += height;
    }
}
