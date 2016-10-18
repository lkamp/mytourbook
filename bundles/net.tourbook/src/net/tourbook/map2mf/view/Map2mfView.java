/*******************************************************************************
 * Copyright (C) 2005, 2016 Wolfgang Schramm and Contributors
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
package net.tourbook.map2mf.view;

import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;

import net.tourbook.application.TourbookPlugin;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.LatLongUtils;
import org.mapsforge.map.awt.graphics.AwtGraphicFactory;
import org.mapsforge.map.awt.view.MapView;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.cache.FileSystemTileCache;
import org.mapsforge.map.layer.cache.InMemoryTileCache;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.cache.TwoLevelTileCache;
import org.mapsforge.map.layer.debug.TileCoordinatesLayer;
import org.mapsforge.map.layer.debug.TileGridLayer;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OpenStreetMapMapnik;
import org.mapsforge.map.layer.download.tilesource.TileSource;
import org.mapsforge.map.layer.renderer.MapWorkerPool;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.reader.ReadBuffer;

/**
 * This map is based on mapsforge (2Dmf) maps http://mapsforge.org/
 */
public class Map2mfView extends ViewPart {

	public static final String			ID					= "net.tourbook.map2mf.view.Map2mfViewId";	//$NON-NLS-1$

	private static final GraphicFactory	GRAPHIC_FACTORY		= AwtGraphicFactory.INSTANCE;
	private static final boolean		SHOW_DEBUG_LAYERS	= true;

	private final IPreferenceStore		_prefStore			= TourbookPlugin.getPrefStore();
	private final IDialogSettings		_state				= TourbookPlugin.getState(ID);

	private Composite					_mapContainer;

	private Frame						_awtFrame;

	/*
	 * UI controls
	 */

	public Map2mfView() {}

	private BoundingBox addLayers(final MapView mapView) {

		final Layers layers = mapView.getLayerManager().getLayers();

		// Raster
		mapView.getModel().displayModel.setFixedTileSize(256);

		final TileDownloadLayer tileDownloadLayer = createTileDownloadLayer(
				createTileCache(0, 256),
				mapView.getModel().mapViewPosition);
		layers.add(tileDownloadLayer);
		tileDownloadLayer.start();

		final BoundingBox result = new BoundingBox(
				LatLongUtils.LATITUDE_MIN,
				LatLongUtils.LONGITUDE_MIN,
				LatLongUtils.LATITUDE_MAX,
				LatLongUtils.LONGITUDE_MAX);

		mapView.setZoomLevelMin(OpenStreetMapMapnik.INSTANCE.getZoomLevelMin());
		mapView.setZoomLevelMax(OpenStreetMapMapnik.INSTANCE.getZoomLevelMax());

//		// Vector
//		mapView.getModel().displayModel.setFixedTileSize(512);
//		BoundingBox result = null;
//		for (int i = 0; i < mapFiles.size(); i++) {
//			final File mapFile = mapFiles.get(i);
//			final TileRendererLayer tileRendererLayer = createTileRendererLayer(
//					createTileCache(i, 64),
//					mapView.getModel().mapViewPosition,
//					true,
//					true,
//					false,
//					mapFile);
//			final BoundingBox boundingBox = tileRendererLayer.getMapDataStore().boundingBox();
//			result = result == null ? boundingBox : result.extendBoundingBox(boundingBox);
//			layers.add(tileRendererLayer);
//		}

		// Debug
		if (SHOW_DEBUG_LAYERS) {
			layers.add(new TileGridLayer(GRAPHIC_FACTORY, mapView.getModel().displayModel));
			layers.add(new TileCoordinatesLayer(GRAPHIC_FACTORY, mapView.getModel().displayModel));
		}

		return result;
	}

	private MapView createMapView() {

		final MapView mapView = new MapView();

		mapView.getMapScaleBar().setVisible(true);

		if (SHOW_DEBUG_LAYERS) {
			mapView.getFpsCounter().setVisible(true);
		}

		return mapView;
	}

	@Override
	public void createPartControl(final Composite parent) {

		createUI(parent);
	}

	private TileCache createTileCache(final int index, final int capacity) {

		final TileCache firstLevelTileCache = new InMemoryTileCache(capacity);
		final File cacheDirectory = new File(System.getProperty("java.io.tmpdir"), "mapsforge" + index);
		final TileCache secondLevelTileCache = new FileSystemTileCache(1024, cacheDirectory, GRAPHIC_FACTORY);

		return new TwoLevelTileCache(firstLevelTileCache, secondLevelTileCache);
	}

	private TileDownloadLayer createTileDownloadLayer(	final TileCache tileCache,
														final MapViewPosition mapViewPosition) {

		final TileSource tileSource = OpenStreetMapMapnik.INSTANCE;

		final TileDownloadLayer tileDownloadLayer = new TileDownloadLayer(
				tileCache,
				mapViewPosition,
				tileSource,
				GRAPHIC_FACTORY) {

			@Override
			public boolean onTap(final LatLong tapLatLong, final Point layerXY, final Point tapXY) {

				System.out.println("Tap on: " + tapLatLong);
				return true;
			}
		};

		return tileDownloadLayer;
	}

	private void createUI(final Composite parent) {

		/*
		 * Set a Windows specific AWT property that prevents heavyweight components from erasing
		 * their background. Note that this is a global property and cannot be scoped. It might not
		 * be suitable for your application.
		 */
		try {
			System.setProperty("sun.awt.noerasebackground", "true");
		} catch (final NoSuchMethodError error) {}

		// Increase read buffer limit
		ReadBuffer.setMaximumBufferSize(6500000);

		// Multithreading rendering
		MapWorkerPool.NUMBER_OF_THREADS = 2;

		final MapView mapView = createMapView();
		final BoundingBox boundingBox = addLayers(mapView);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);

		// build GUI: container(SWT) -> Frame(AWT) -> Panel(AWT) -> WorldWindowGLCanvas(AWT)
		_mapContainer = new Composite(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND);
//		GridDataFactory.fillDefaults().applyTo(_mapContainer);
		{
			_awtFrame = SWT_AWT.new_Frame(_mapContainer);
			_awtFrame.add(mapView);
		}

		_awtFrame.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(final ComponentEvent e) {
//				Map3Manager.getLayer_TourLegend().resizeLegendImage();
			}
		});

		parent.layout();
	}

	@Override
	public void dispose() {

		super.dispose();
	}

	@Override
	public void setFocus() {

	}
}
