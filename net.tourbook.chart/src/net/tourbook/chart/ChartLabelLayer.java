/*******************************************************************************
 * Copyright (C) 2005, 2009  Wolfgang Schramm and Contributors
 *   
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software 
 * Foundation version 2 of the License.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA    
 *******************************************************************************/
/**
 * @author Wolfgang Schramm Created: 06.07.2005
 */

package net.tourbook.chart;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;

public class ChartLabelLayer implements IChartLayer {

	private static final int			LABEL_HEIGHT	= 4;
	private static final int			LABEL_WIDTH		= 4;
	private static final int			LABEL_OFFSET	= 3;

	private int							fLabelOffset	= LABEL_OFFSET;

	private final ArrayList<ChartLabel>	fChartLabels	= new ArrayList<ChartLabel>();

	private RGB							fLineColor		= new RGB(189, 0, 255);

	/**
	 * Adds a new marker to the internal marker list, the list can be retrieved with getMarkerList()
	 * 
	 * @param label
	 * @param xCoord
	 *            Position of the marker on the x axis
	 * @param label
	 */
	public void addLabel(final ChartLabel label) {
		fChartLabels.add(label);
	}

	/**
	 * Draws the marker(s) for the current graph config
	 * 
	 * @param gc
	 * @param drawingData
	 * @param fChartComponents
	 */
	public void draw(final GC gc, final ChartDrawingData drawingData, final Chart chart) {

		final Display display = Display.getCurrent();

		final int devYTop = drawingData.getDevYTop();
		final int devYBottom = drawingData.getDevYBottom();
		final int devGraphImageOffset = chart.getDevGraphImageXOffset();
		final int devGraphHeight = drawingData.getDevGraphHeight();
		final int devGraphWidth = drawingData.getDevGraphWidth();

		final int graphYBottom = drawingData.getGraphYBottom();
		final int[] yValues = drawingData.getYData().getHighValues()[0];
		final float scaleX = drawingData.getScaleX();
		final float scaleY = drawingData.getScaleY();

		final Color colorLine = new Color(display, getLineColor());

		gc.setClipping(0, devYTop, devGraphWidth, devGraphHeight);

		for (final ChartLabel chartMarker : fChartLabels) {

			final int yValue = yValues[chartMarker.serieIndex];
			final int devYGraph = (int) ((yValue - graphYBottom) * scaleY) - 0;

			int devXMarker = (int) (chartMarker.graphX * scaleX) - devGraphImageOffset;
			int devYMarker = devYBottom - devYGraph;

			final int visualPosition = chartMarker.visualPosition;
			final Point labelExtend = gc.textExtent(chartMarker.markerLabel);
			final int markerType = chartMarker.type;

			if (markerType == ChartLabel.MARKER_TYPE_DEVICE) {
				gc.setBackground(display.getSystemColor(SWT.COLOR_RED));
			} else {
				gc.setBackground(colorLine);
			}
			gc.setForeground(colorLine);

			final int markerWidth2 = LABEL_WIDTH / 2;

			// draw marker
			gc.fillRectangle(devXMarker - markerWidth2, devYBottom - devYGraph, LABEL_WIDTH, LABEL_HEIGHT);

			if (chartMarker.visualType != ChartLabel.VISIBLE_TYPE_DEFAULT) {
				gc.setForeground(display.getSystemColor(SWT.COLOR_BLUE));
			}

			boolean isVertical = true;

			final int labelHeight = labelExtend.y;
			final int labelWidth = labelExtend.x;

//			fMarkerOffset = 0;

			switch (visualPosition) {
			case ChartLabel.VISUAL_VERTICAL_ABOVE_GRAPH:
				devYMarker -= fLabelOffset;
				break;

			case ChartLabel.VISUAL_VERTICAL_BELOW_GRAPH:
				devYMarker += labelWidth + fLabelOffset + markerWidth2;
				break;

			case ChartLabel.VISUAL_VERTICAL_TOP_CHART:
				devYMarker = devYTop + labelWidth;
				break;

			case ChartLabel.VISUAL_VERTICAL_BOTTOM_CHART:
				devYMarker = devYBottom - fLabelOffset;
				break;

			case ChartLabel.VISUAL_HORIZONTAL_ABOVE_GRAPH_LEFT:
				devXMarker -= labelWidth + fLabelOffset;
				devYMarker -= labelHeight + fLabelOffset;
				isVertical = false;
				break;

			case ChartLabel.VISUAL_HORIZONTAL_ABOVE_GRAPH_CENTERED:
				devXMarker -= labelWidth / 2;
				devYMarker -= labelHeight + fLabelOffset;
				isVertical = false;
				break;

			case ChartLabel.VISUAL_HORIZONTAL_ABOVE_GRAPH_RIGHT:
				devXMarker += fLabelOffset + markerWidth2;
				devYMarker -= labelHeight + fLabelOffset;
				isVertical = false;
				break;

			case ChartLabel.VISUAL_HORIZONTAL_BELOW_GRAPH_LEFT:
				devXMarker -= labelWidth + fLabelOffset;
				devYMarker += fLabelOffset + markerWidth2;
				isVertical = false;
				break;

			case ChartLabel.VISUAL_HORIZONTAL_BELOW_GRAPH_CENTERED:
				devXMarker -= labelWidth / 2;
				devYMarker += fLabelOffset + markerWidth2;
				isVertical = false;
				break;

			case ChartLabel.VISUAL_HORIZONTAL_BELOW_GRAPH_RIGHT:
				devXMarker += fLabelOffset + markerWidth2;
				devYMarker += fLabelOffset + markerWidth2;
				isVertical = false;
				break;

			case ChartLabel.VISUAL_HORIZONTAL_GRAPH_LEFT:
				devXMarker -= labelWidth + fLabelOffset;
				devYMarker -= labelHeight / 2;
				isVertical = false;
				break;

			case ChartLabel.VISUAL_HORIZONTAL_GRAPH_RIGHT:
				devXMarker += fLabelOffset + markerWidth2;
				devYMarker -= labelHeight / 2;
				isVertical = false;
				break;

			default:
				break;
			}

			devXMarker += chartMarker.labelXOffset;
			devYMarker -= chartMarker.labelYOffset;

			// draw marker label
			if (chart.getAdvancedGraphics()) {

				if (isVertical) {

					/*
					 * vertical position
					 */

					// draw label to the left side of the marker
					devXMarker -= labelHeight;

					// don't draw the marker before the chart
					final int devXImageOffset = chart.getDevGraphImageXOffset();
					if (devXImageOffset == 0 && devXMarker < 0) {
						devXMarker = 0;
					}

					// force label to be not below the bottom
					if (devYMarker > devYBottom) {
						devYMarker = devYBottom;
					}

					// force label to be not above the top
					if (devYMarker - labelWidth < devYTop) {
						devYMarker = devYTop + labelWidth;
					}

					final Transform tr = new Transform(display);
					tr.translate(devXMarker, devYMarker);
					tr.rotate(-90f);

					gc.setTransform(tr);

					gc.setAntialias(SWT.ON);
					gc.drawText(chartMarker.markerLabel, 0, 0, true);
					gc.setAntialias(SWT.OFF);

					gc.setTransform(null);
					tr.dispose();

				} else {

					/*
					 * horizontal position
					 */

					// don't draw the marker before the chart
					final int devXImageOffset = chart.getDevGraphImageXOffset();
					if (devXImageOffset == 0 && devXMarker < 0) {
						devXMarker = 0;
					}

					if (devXMarker + labelWidth > devGraphWidth) {
						devXMarker = devGraphWidth - labelWidth;
					}

					// force label to be not below the bottom
					if (devYMarker + labelHeight > devYBottom) {
						devYMarker = devYBottom - labelHeight;
					}

					// force label to be not above the top
					if (devYMarker - labelHeight < devYTop) {
						devYMarker = devYTop + labelHeight;
					}

					gc.drawText(chartMarker.markerLabel, devXMarker, devYMarker, true);
				}

			} else {
				// use simple graphics
				devYMarker = devYBottom - devYGraph - labelHeight;
				if (devXMarker + labelWidth > devGraphWidth) {
					devXMarker -= labelWidth;
				}
				gc.drawText(chartMarker.markerLabel, devXMarker, devYMarker, true);
			}
		}

		gc.setClipping((Rectangle) null);

		colorLine.dispose();
	}

	public RGB getLineColor() {
		return fLineColor;
	}

	public void setLineColor(final RGB lineColor) {
		fLineColor = lineColor;
	}
}
