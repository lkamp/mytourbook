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
package de.byteholder.geoclipse.mapprovider;

import de.byteholder.geoclipse.map.Tile;
import de.byteholder.geoclipse.map.event.TileEventId;

public class LogEntry {

	static final int	COLUMN_WIDTH_THREAD_NAME	= 12;
	static final int	COLUMN_WIDTH_COUNTER		= 4;

	TileEventId			tileEventId;
	Tile				tile;

	/**
	 * Epoch time in milliseconds
	 */
	long				time;
	String				threadName;
	String				counter;

	public LogEntry(final TileEventId tileEventId,
					final Tile tile,
					final long nanoTime,
					final String threadName,
					final int counter) {

		this.tileEventId = tileEventId;
		this.tile = tile;
		this.time = nanoTime / 1000_000;

		// force same width           					 012345
		this.counter = Integer.toString(counter).concat("      ").substring(0, COLUMN_WIDTH_COUNTER); //$NON-NLS-1$

		// force same width                  0123456789012
		this.threadName = threadName.concat("             ").substring(0, COLUMN_WIDTH_THREAD_NAME); //$NON-NLS-1$

	}
}
