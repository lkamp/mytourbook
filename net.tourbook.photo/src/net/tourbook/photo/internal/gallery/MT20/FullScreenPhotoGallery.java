/*******************************************************************************
 * Copyright (C) 2005, 2012  Wolfgang Schramm and Contributors
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
package net.tourbook.photo.internal.gallery.MT20;

import net.tourbook.common.UI;
import net.tourbook.photo.IPhotoGalleryProvider;
import net.tourbook.photo.IPhotoPreferences;
import net.tourbook.photo.IPhotoProvider;
import net.tourbook.photo.PhotoGallery;
import net.tourbook.photo.PhotoSelection;
import net.tourbook.photo.PhotoWrapper;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class FullScreenPhotoGallery implements IPhotoGalleryProvider {

	private static final int		GALLERY_HEIGHT	= 150;

	private ControlAnimation		_photoGalleryAnimation;

	private IDialogSettings			_state;

	private AllControlsListener		_allControlsListener;

	private Shell					_fullScreenShell;
	private Shell					_galleryShell;

	private GalleryMT20				_sourceGallery;
	private PhotoGallery			_photoGallery;
	private FullScreenImageViewer	_fullScreenImageViewer;

	private int						_displayedPhotosHash;
	private int						_displayedItemIndex;

	/**
	 * This listener is added to ALL widgets within the tooltip shell.
	 */
	private class AllControlsListener implements Listener {
		public void handleEvent(final Event event) {
			onAllControlsEvent(event);
		}

	}

	public class ControlAnimation implements Runnable {

		/**
		 * how long each tick is when fading in/out (in ms)
		 */
//		private static final int	FADE_TIME_INTERVAL		= UI.IS_OSX ? 10 : 10;
		private final int			FADE_TIME_INTERVAL	= UI.IS_OSX ? 10 : 10;

		/**
		 * Number of steps when fading in
		 */
		private static final int	FADE_IN_STEPS		= 20;

		/**
		 * Number of steps when fading out
		 */
		private static final int	FADE_OUT_STEPS		= 10;

		private static final int	ALPHA_OPAQUE		= 0xff;

		private Display				_display;
		private Shell				_shell;

		private boolean				_isFadeIn;
		private boolean				_isFadeOut;

		private int					_fadeAlpha;

		public ControlAnimation(final Shell shell, final Control control) {

			_shell = shell;
			_display = shell.getDisplay();
		}

		public void fadeIn() {

			if (_isFadeIn) {
				// fade in is already started
				return;
			}

			if (_isFadeOut) {

				// stop fade out and start with current alpha

				_fadeAlpha = _shell.getAlpha();
			} else {
				_fadeAlpha = 0;
			}

			_isFadeIn = true;
			_isFadeOut = false;

			run();
		}

		public void fadeOut() {

			if (_isFadeOut) {
				// fade out is already started
				return;
			}

			_fadeAlpha = _shell.getAlpha();

			_isFadeIn = false;
			_isFadeOut = true;

			run();
		}

		@Override
		public void run() {

			final boolean isVisible = _shell.isVisible();

			if (_isFadeIn) {

				final int fadeInStep = ALPHA_OPAQUE / FADE_IN_STEPS;

				int newAlpha = _fadeAlpha + fadeInStep;
				if (newAlpha > ALPHA_OPAQUE) {
					newAlpha = ALPHA_OPAQUE;
				}

				// set alpha before shell is displayed
				_shell.setAlpha(newAlpha);

				if (isVisible == false) {

					_shell.setVisible(true);
					_shell.setActive();
				}

				final int currentAlpha = _shell.getAlpha();
				if (currentAlpha != newAlpha) {

					// platform do not support alpha (e.g. Ubuntu 12.04 in my test system)

					_shell.setAlpha(ALPHA_OPAQUE);
					_isFadeIn = false;

				} else {

					_fadeAlpha = currentAlpha;

					if (currentAlpha == ALPHA_OPAQUE) {

						// reached end of fade in
						_isFadeIn = false;

					} else {

						// start timer for a neww fade in
						_display.timerExec(FADE_TIME_INTERVAL, this);
					}
				}

			} else if (_isFadeOut) {

				if (isVisible == false) {
					_isFadeOut = false;
					return;
				}

				final int fadeOutStep = ALPHA_OPAQUE / FADE_OUT_STEPS;

				int newAlpha = _fadeAlpha - fadeOutStep;
				if (newAlpha < 0) {
					newAlpha = 0;
				}

				_shell.setAlpha(newAlpha);

				final int currentAlpha = _shell.getAlpha();
				if (currentAlpha != newAlpha) {

					// platform do not support alpha (e.g. Ubuntu 12.04 in my test system)

					_shell.setAlpha(0);

					_shell.setVisible(false);
					_isFadeOut = false;

				} else {

					_fadeAlpha = currentAlpha;

					if (currentAlpha == 0) {

						// reached end of fade in

						_shell.setVisible(false);
						_isFadeOut = false;

					} else {

						// start timer for a neww fade out
						_display.timerExec(FADE_TIME_INTERVAL, this);
					}
				}
			}
		}
	}

	public FullScreenPhotoGallery(	final Shell fullScreenShell,
									final GalleryMT20 sourceGallery,
									final FullScreenImageViewer fullScreenImageViewer) {

		_fullScreenShell = fullScreenShell;
		_sourceGallery = sourceGallery;
		_fullScreenImageViewer = fullScreenImageViewer;

		createUI();

		_photoGalleryAnimation = new ControlAnimation(_galleryShell, _photoGallery.getGallery());

		_allControlsListener = new AllControlsListener();
		allControlsAddListener(_galleryShell);

		addFullScreenListener();
	}

	private void addFullScreenListener() {

		_fullScreenShell.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(final DisposeEvent e) {
				onDispose();
			}
		});
	}

	/**
	 * ########################### Recursive #########################################<br>
	 * <p>
	 * Add listener to all controls
	 * <p>
	 * ########################### Recursive #########################################<br>
	 * 
	 * @param control
	 */
	private void allControlsAddListener(final Control control) {

		control.addListener(SWT.KeyDown, _allControlsListener);

		if (control instanceof Composite) {
			final Control[] children = ((Composite) control).getChildren();
			for (final Control child : children) {
				allControlsAddListener(child);
			}
		}
	}

	private void createUI() {

		_galleryShell = new Shell(SWT.NO_TRIM | SWT.ON_TOP);

		final Rectangle fsShellSize = _fullScreenShell.getBounds();

		_galleryShell.setBounds(fsShellSize.x, fsShellSize.y, fsShellSize.width, GALLERY_HEIGHT);
		_galleryShell.setLayout(new FillLayout());

		_galleryShell.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(final FocusEvent e) {
				System.out.println(UI.timeStampNano() + " focusGained\t");
				// TODO remove SYSTEM.OUT.PRINTLN

			}

			@Override
			public void focusLost(final FocusEvent e) {
				System.out.println(UI.timeStampNano() + " onFocusLost\t");
				// TODO remove SYSTEM.OUT.PRINTLN

//				hideGallery();
			}
		});

		_galleryShell.addShellListener(new ShellListener() {

			@Override
			public void shellActivated(final ShellEvent e) {}

			@Override
			public void shellClosed(final ShellEvent e) {}

			@Override
			public void shellDeactivated(final ShellEvent e) {
				hideGallery();
			}

			@Override
			public void shellDeiconified(final ShellEvent e) {}

			@Override
			public void shellIconified(final ShellEvent e) {
				hideGallery();
			}
		});

		createUI_10_Gallery(_galleryShell);
	}

	private void createUI_10_Gallery(final Shell parent) {

		_photoGallery = new PhotoGallery();

		_photoGallery.createPhotoGallery(parent, SWT.H_SCROLL, this);
		_photoGallery.createActionBar();

		/*
		 * set fullscreen image viewer in the photo gallery to the fullscreen image viewer in the
		 * source gallery, this is a bit a a hack
		 */
		_photoGallery.setFullScreenImageViewer(_sourceGallery.getFullScreenImageViewer());
	}

	@Override
	public IStatusLineManager getStatusLineManager() {
		return null;
	}

	@Override
	public IToolBarManager getToolBarManager() {
		return null;
	}

	private void hideGallery() {

		if (_galleryShell == null || _galleryShell.isVisible() == false) {
			return;
		}

		_photoGallery.stopLoadingImages();

		_photoGalleryAnimation.fadeOut();
	}

	private void onAllControlsEvent(final Event event) {

		if (event.keyCode == SWT.ESC) {

			// hide full screen gallery
			hideGallery();
		}
	}

	private void onDispose() {

		if (_galleryShell != null) {

			saveState();

			_galleryShell.close();
		}
	}

	@Override
	public void registerContextMenu(final String menuId, final MenuManager menuManager) {}

	void restoreState(final IDialogSettings state) {

		_state = state;

		updateColors(true);

		_photoGallery.restoreState(state);
	}

	void saveState() {

		_photoGallery.saveState(_state);
	}

	@Override
	public void setSelection(final PhotoSelection photoSelection) {
		_fullScreenImageViewer.setSelection(photoSelection);
	}

	boolean showImages(final int mouseY, final int displayedItemIndex) {

//		if (mouseEvent.y == 0) {
		if (mouseY < GALLERY_HEIGHT) {

			// show gallery

			_displayedItemIndex = displayedItemIndex;

			showImages_10_InGallery();

			return true;

		} else {

			// hide gallery

			hideGallery();

			return false;
		}
	}

	private void showImages_10_InGallery() {

		final IPhotoProvider photoProvider = _sourceGallery.getPhotoProvider();
		final PhotoWrapper[] photoWrapper = photoProvider.getSortedAndFilteredPhotoWrapper();

		final int photosHash = photoWrapper.hashCode();
		final String galleryPositionKey = photosHash + "_FullScreenPhotoGallery";//$NON-NLS-1$

		/**
		 * !!! gallery shell must be visible before any gallery methods are called, otherwise the
		 * gallery is hidden and not fully initialized !!!!
		 */

		final boolean isShellVisible = _galleryShell.isVisible();
		if (isShellVisible == false) {
			_photoGalleryAnimation.fadeIn();
		}

		/**
		 * check if new images should be displayed, this check is VERY IMPORTANT otherwise this can
		 * be a performance hog
		 */
		if (_displayedPhotosHash != photosHash) {

			_displayedPhotosHash = photosHash;

			_photoGallery.showImages(photoWrapper, galleryPositionKey);
		}

		// show photo in the gallery which is displayed in the full screen viewer
		_photoGallery.selectItem(_displayedItemIndex, true);
	}

	private void updateColors(final boolean isRestore) {

		final ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
		final Color fgColor = colorRegistry.get(IPhotoPreferences.PHOTO_VIEWER_COLOR_FOREGROUND);
		final Color bgColor = colorRegistry.get(IPhotoPreferences.PHOTO_VIEWER_COLOR_BACKGROUND);
		final Color selectionFgColor = colorRegistry.get(IPhotoPreferences.PHOTO_VIEWER_COLOR_SELECTION_FOREGROUND);

		final Color noFocusSelectionFgColor = Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND);

//		tree.setForeground(fgColor);
//		tree.setBackground(bgColor);

		_photoGallery.updateColors(fgColor, bgColor, selectionFgColor, noFocusSelectionFgColor, isRestore);
	}

}
