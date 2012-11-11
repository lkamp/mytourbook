/*******************************************************************************
 * Copyright (C) 2005, 2010  Wolfgang Schramm and Contributors
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
package net.tourbook.data;

import java.io.File;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import net.tourbook.database.TourDatabase;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import de.byteholder.geoclipse.map.UI;

@Entity
public class TourPhoto {

	public static final int	DB_LENGTH_FILE_PATH	= 255;

	/**
	 * Unique id for the {@link TourPhoto} entity
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long			photoId				= TourDatabase.ENTITY_IS_NOT_SAVED;

	/**
	 * Image filename with extension.
	 */
	private String			imageFileName;

	/**
	 * Image file extension.
	 */
	private String			imageFileExt;

	/**
	 * Image file path without filename.
	 */
	private String			imageFilePath;

	@Transient
	private String			imageFilePathName;

	/**
	 * Exif time in milliseconds, when not available, the last modified time of the image file is
	 * used.
	 */
	private long			imageExifTime;

	/**
	 * Last modified in GMT
	 */
	private long			imageFileLastModified;

	/**
	 * <code>0</code> geo position is from a tour<br>
	 * <code>1</code> geo position is from a photo<br>
	 */
	private int				isGeoFromPhoto;

	private double			latitude			= Double.MIN_VALUE;
	private double			longitude			= Double.MIN_VALUE;

	@ManyToOne(optional = false)
	private TourData		tourData;

	/**
	 * unique id for manually created markers because the {@link #photoId} is 0 when the marker is
	 * not persisted
	 */
	@Transient
	private long			_createId			= 0;

	/**
	 * manually created marker or imported marker create a unique id to identify them, saved marker
	 * are compared with the marker id
	 */
	private static int		_createCounter		= 0;

	public TourPhoto() {}

	public TourPhoto(final TourData tourData, final File imageFile, final long photoExifTime) {

		_createId = ++_createCounter;

		this.tourData = tourData;

		final IPath filePath = new Path(imageFile.getAbsolutePath());

		final String fileExtension = filePath.getFileExtension();

		imageFileName = filePath.lastSegment();
		imageFileExt = fileExtension == null ? UI.EMPTY_STRING : fileExtension;
		imageFilePath = filePath.removeLastSegments(1).toOSString();

		imageFileLastModified = imageFile.lastModified();
		imageExifTime = photoExifTime;
	}

	@Override
	public boolean equals(final Object obj) {

		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TourPhoto)) {
			return false;
		}

		final TourPhoto other = (TourPhoto) obj;

		if (_createId == 0) {

			// photo is from the database
			if (photoId != other.photoId) {
				return false;
			}
		} else {

			// photo was create
			if (_createId != other._createId) {
				return false;
			}
		}

		return true;
	}

	public long getImageExifTime() {
		return imageExifTime;
	}

	public String getImageFileExt() {
		return imageFileExt;
	}

	public String getImageFileName() {
		return imageFileName;
	}

	public String getImageFilePath() {
		return imageFilePath;
	}

	/**
	 * @return Returns the full filepathname
	 */
	public String getImageFilePathName() {

		if (imageFilePathName == null) {
			imageFilePathName = new Path(imageFilePath).append(imageFileName).toOSString();
		}

		return imageFilePathName;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (_createId ^ (_createId >>> 32));
		result = prime * result + (int) (photoId ^ (photoId >>> 32));
		return result;
	}

	public boolean isGeoFromPhoto() {
		return isGeoFromPhoto == 1;
	}

	public boolean isGeoFromTour() {
		return isGeoFromPhoto == 0;
	}

	public void setGeoLocation(final double latitude, final double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public void setIsGeoFromPhoto() {
		isGeoFromPhoto = 0;
	}

	public void setIsGeoFromTour() {
		isGeoFromPhoto = 1;
	}

	@Override
	public String toString() {
		return new StringBuilder()//
				.append(TourPhoto.class.getSimpleName())
				.append(" id:") //$NON-NLS-1$
				.append(photoId)
				.append(" createId:") //$NON-NLS-1$
				.append(_createId)
				.toString();
	}

}