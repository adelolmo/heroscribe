/*
  HeroScribe
  Copyright (C) 2002-2004 Flavio Chierichetti and Valerio Chierichetti
  
  HeroScribe Enhanced (changes are prefixed with HSE in comments)
  Copyright (C) 2011 Jason Allen  
   
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License version 2 (not
  later versions) as published by the Free Software Foundation.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package org.lightless.heroscribe.list;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class List implements Serializable {

	private static final long serialVersionUID = 1L;

	public LBoard board;

	public TreeMap<String, LObject> list;
	public TreeSet<Kind> kinds;

	public String version;
	public String vectorPrefix, vectorSuffix;
	public String rasterPrefix, rasterSuffix;
	public String samplePrefix, sampleSuffix;

	public List() {
		list = new TreeMap<>();
		kinds = new TreeSet<>();
	}

	public Iterator<LObject> objectsIterator() {
		/* I know it's unefficient, but I need the objects ordered by value, not key
		 * (i.e. by name, not id) */

		return new TreeSet<LObject>(list.values()).iterator();
	}

	public Iterator<Kind> kindsIterator() {
		return kinds.iterator();
	}

	public LObject getObject(String id) {
		return list.get(id);
	}

	// HSE - get an object by its name
	public LObject getObjectByName(String name) {
		var i = list.entrySet().iterator();
		Map.Entry<String, LObject> e;
		LObject myObj = new LObject();
		while (i.hasNext()) {
			e = i.next();
			if (e.getValue().toString().contentEquals(name)) {
				myObj = list.get(e.getKey());
			}
		}
		return myObj;
	}

	public LBoard getBoard() {
		return board;
	}

	public Kind getKind(String id) {
		Iterator<Kind> iterator = kindsIterator();
		Kind found = null;

		while (iterator.hasNext()) {
			Kind kind = iterator.next();
			if (id.equals(kind.id)) {
				found = kind;
				break;
			}
		}

		return found;
	}

	public String getVectorPath(String id, String region) {
		return vectorPrefix + getObject(id).getIcon(region).path + vectorSuffix;
	}

	public String getRasterPath(String id, String region) {
		return rasterPrefix + getObject(id).getIcon(region).path + rasterSuffix;
	}

	public String getSamplePath(String id, String region) {
		return samplePrefix + getObject(id).getIcon(region).path + sampleSuffix;
	}

	public String getVectorPath(String region) {
		return vectorPrefix + getBoard().getIcon(region).path + vectorSuffix;
	}

	public String getRasterPath(String region) {
		return rasterPrefix + getBoard().getIcon(region).path + rasterSuffix;
	}

	public String getSamplePath(String region) {
		return samplePrefix + getBoard().getIcon(region).path + sampleSuffix;
	}

}
