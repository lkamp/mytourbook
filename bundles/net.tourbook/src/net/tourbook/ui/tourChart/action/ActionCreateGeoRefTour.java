package net.tourbook.ui.tourChart.action;

import net.tourbook.Messages;
import net.tourbook.application.TourbookPlugin;
import net.tourbook.ui.UI;
import net.tourbook.ui.tourChart.TourChart;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Display;

public class ActionCreateGeoRefTour extends Action {
	
	private TourChart	_tourChart;
	
	public ActionCreateGeoRefTour(final TourChart tourChart) {

		setText(Messages.tourCatalog_view_action_create_geo_reference_tour);
		setImageDescriptor(TourbookPlugin.getImageDescriptor(Messages.Image__tour_map_ref_tour_new));

		_tourChart = tourChart;
	}
	
	@Override
	public void run() {
		final InputDialog dialog = new InputDialog(
				Display.getCurrent().getActiveShell(),
				"Title",
				"Message",
				UI.EMPTY_STRING,
				null);
		
		dialog.open();
		
	}
}
