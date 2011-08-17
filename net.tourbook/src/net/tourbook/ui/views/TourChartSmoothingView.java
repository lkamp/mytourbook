/*******************************************************************************
 * Copyright (C) 2005, 2011  Wolfgang Schramm and Contributors
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
package net.tourbook.ui.views;

import net.tourbook.Messages;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

public class TourChartSmoothingView extends ViewPart {

	public static final String	ID	= "net.tourbook.ui.views.TourChartSmoothingView";	//$NON-NLS-1$

	private SmoothingUI			_smoothingUI;

	private FormToolkit			_tk;

	private ScrolledComposite	_scrolledContainer;
	private Composite			_scrolledContent;

	public TourChartSmoothingView() {}

	@Override
	public void createPartControl(final Composite parent) {

		_smoothingUI = new SmoothingUI();

		createUI(parent);
	}

	private void createUI(final Composite parent) {

		_tk = new FormToolkit(parent.getDisplay());

		_scrolledContainer = new ScrolledComposite(parent, SWT.V_SCROLL);
		{
			_scrolledContent = _tk.createComposite(_scrolledContainer);
			GridDataFactory.fillDefaults().applyTo(_scrolledContent);
			GridLayoutFactory.swtDefaults().numColumns(1).applyTo(_scrolledContent);
			{
				_smoothingUI.createUI(_scrolledContent, false);

				/*
				 * Link: update all tours
				 */
				final Link link = new Link(_scrolledContent, SWT.NONE);
				GridDataFactory.fillDefaults().indent(0, 5).grab(true, false).applyTo(link);
				link.setText(Messages.TourChart_Smoothing_Link_UpdateAllTours);
				link.setToolTipText(Messages.Compute_Smoothing_Button_ForAllTours_Tooltip);
				link.setEnabled(true);
				link.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(final SelectionEvent e) {
						_smoothingUI.computeSmoothingForAllTours();
					}
				});
				_tk.adapt(link, true, true);
			}

			// setup scrolled container
			_scrolledContainer.setExpandVertical(true);
			_scrolledContainer.setExpandHorizontal(true);
			_scrolledContainer.addControlListener(new ControlAdapter() {
				@Override
				public void controlResized(final ControlEvent e) {
					onResizeScrolledContainer(_scrolledContent);
				}
			});

			_scrolledContainer.setContent(_scrolledContent);
		}
	}

	@Override
	public void dispose() {

		_smoothingUI.dispose();
		_tk.dispose();

		super.dispose();
	}

	private void onResizeScrolledContainer(final Composite container) {
		_scrolledContainer.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	@Override
	public void setFocus() {}

}