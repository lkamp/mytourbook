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
package net.tourbook.photo.manager;

import java.io.File;

import net.tourbook.photo.gallery.MT20.IGalleryCustomData;
import net.tourbook.ui.UI;

/**
 * Wrapper for a photo image file, sorting and filtering attributes and the {@link Photo} itself.
 */
public class PhotoWrapper implements IGalleryCustomData {

	/**
	 * Photo image file
	 */
	public File		imageFile;

	public String	imageFileName;
	public String	imageFileExt;
	public String	imageFilePathName;

	/**
	 * Last modified in GMT
	 */
	public long		imageFileLastModified;
	public long		imageSortingTime;

	public long		imageFileSize;

	public Photo	photo;

	/**
	 * GPS has three states:
	 * 
	 * <pre>
	 * -1 state is not yet set, EXIF data are not yet loaded
	 *  0 photo do not contain GPS data
	 *  1 photo contains GPS data
	 * </pre>
	 */
	public int		gpsState	= -1;

	public PhotoWrapper(final File file) {

		imageFile = file;

		imageFileName = imageFile.getName();
		imageFilePathName = imageFile.getPath();
		imageFileLastModified = imageFile.lastModified();

		imageFileSize = imageFile.length();

		final int dotPos = imageFileName.lastIndexOf(".");
		imageFileExt = dotPos > 0 ? imageFileName.substring(dotPos + 1).toLowerCase() : UI.EMPTY_STRING;

		// initially sort by file date until exif data are loaded
		imageSortingTime = imageFileLastModified;
	}

	@Override
	public String getUniqueId() {
		return imageFilePathName;
	}

	@Override
	public String toString() {

		return (imageFileLastModified + "  ") + imageFileName;

//		return photo.toString();
	}

}