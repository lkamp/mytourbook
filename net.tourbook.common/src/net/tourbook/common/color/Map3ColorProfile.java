/*******************************************************************************
 * Copyright (C) 2005, 2014  Wolfgang Schramm and Contributors
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
package net.tourbook.common.color;

import net.tourbook.common.Messages;
import net.tourbook.common.util.StatusUtil;

import org.eclipse.swt.graphics.Image;

/**
 * Contains all colors for one graph to paint a tour in the 3D map.
 */
public class Map3ColorProfile extends MapColorProfile implements Cloneable {

	/**
	 * Unique id to identify a color profile.
	 */
	private int				_profileId;

	/**
	 * 
	 */
	private MapColorId		_mapColorId;

	/**
	 * Name which is visible in the UI.
	 */
	private String			_profileName	= Messages.Map3_Color_DefaultProfileName;

	private ProfileImage	_profileImage	= new Map3ProfileImage();

	private static int		_idCounter		= 0;

	public Map3ColorProfile(final MapColorId mapColorId) {
		_mapColorId = mapColorId;
	}

	/**
	 * @param rgbVertices
	 * @param minBrightness
	 * @param minBrightnessFactor
	 * @param maxBrightness
	 * @param maxBrightnessFactor
	 */
	public Map3ColorProfile(final RGBVertex[] rgbVertices,
	//
							final int minBrightness,
							final int minBrightnessFactor,
							final int maxBrightness,
							final int maxBrightnessFactor) {

		_profileId = createProfileId();

		_profileImage.setVertices(rgbVertices);

		this.minBrightness = minBrightness;
		this.minBrightnessFactor = minBrightnessFactor;
		this.maxBrightness = maxBrightness;
		this.maxBrightnessFactor = maxBrightnessFactor;
	}

	/**
	 * @param valueColors
	 * @param minBrightness
	 * @param minBrightnessFactor
	 * @param maxBrightness
	 * @param maxBrightnessFactor
	 * @param isMinOverwrite
	 * @param minOverwrite
	 * @param isMaxOverwrite
	 * @param maxOverwrite
	 */
	public Map3ColorProfile(final RGBVertex[] rgbVertices,
	//
							final int minBrightness,
							final int minBrightnessFactor,
							final int maxBrightness,
							final int maxBrightnessFactor,
							//
							final boolean isMinOverwrite,
							final int minOverwrite,
							final boolean isMaxOverwrite,
							final int maxOverwrite
	//
	) {

		this(rgbVertices, minBrightness, minBrightnessFactor, maxBrightness, maxBrightnessFactor);

		this.isMinValueOverwrite = isMinOverwrite;
		this.overwriteMinValue = minOverwrite;
		this.isMaxValueOverwrite = isMaxOverwrite;
		this.overwriteMaxValue = maxOverwrite;
	}

	@Override
	public Map3ColorProfile clone() {

		Map3ColorProfile clonedObject = null;

		try {

			clonedObject = (Map3ColorProfile) super.clone();

			clonedObject._profileId = createProfileId();
			clonedObject._profileName = new String(_profileName);

			clonedObject._profileImage = _profileImage.clone();

		} catch (final CloneNotSupportedException e) {
			StatusUtil.log(e);
		}

		return clonedObject;
	}

	/**
	 * Creates profile image, this image must be disposed who created it.
	 * 
	 * @param width
	 * @param height
	 * @param isHorizontal
	 * @return
	 */
	public Image createImage(final int width, final int height, final boolean isHorizontal) {

		return _profileImage.createImage(width, height, isHorizontal);
	}

	/**
	 * Create a unique id.
	 * 
	 * @return
	 */
	private int createProfileId() {

		return ++_idCounter;
	}

	public MapColorId getMapColorId() {
		return _mapColorId;
	}

	public int getProfileId() {
		return _profileId;
	}

	public ProfileImage getProfileImage() {
		return _profileImage;
	}

	public String getProfileName() {
		return _profileName;
	}

	public void setProfileId(final int profileId) {
		_profileId = profileId;
	}

	public void setProfileName(final String name) {
		_profileName = name;
	}

}
