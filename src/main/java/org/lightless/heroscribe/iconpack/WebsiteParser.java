/*
  HeroScribe Enhanced Skull
  Copyright (C) 2022 Andoni del Olmo

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

package org.lightless.heroscribe.iconpack;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class WebsiteParser {

	private static final String HEROSCRIBE_ICONS_URL =
			"https://www.heroscribe.org/icons.html";

	public List<IconPackDetails> parse() throws IOException {
		final Document document =
				Jsoup.parse(new URL(HEROSCRIBE_ICONS_URL),
						Math.toIntExact(Duration.ofSeconds(5).toMillis()));

		return downloadIconPacksMetadata(document);
	}

	private List<IconPackDetails> downloadIconPacksMetadata(Document document) throws MalformedURLException {
		final List<IconPackDetails> iconPackMetadata = new ArrayList<>();

		final Elements iconPackElements =
				document.select("ul[class=\"iconpacks\"]")
						.select("li");
		for (Element iconPackElement : iconPackElements) {
			final String link =
					iconPackElement.select("a").attr("href");

			if (!link.contains("click.php")) {
				continue;
			}
			final IconPackDetails ip = extractIconPack(iconPackElement.select("a"));
			iconPackMetadata.add(new IconPackDetails(ip.getName(), ip.getLink(), ip.getId()));
		}
		return iconPackMetadata;
	}

	private IconPackDetails extractIconPack(Elements elements) throws MalformedURLException {
		final String name = elements.get(0).text();
		final String link = elements.get(0).attr("href");
		final String href = elements.get(1).attr("href");
		final String filename = href.substring(href.lastIndexOf('/') + 1);
		final String id = filename.split("\\.")[0];

		return new IconPackDetails(name, new URL(link), id);
	}

	public static class IconPackDetails {
		private final String name;
		private final URL link;
		private final String id;

		public IconPackDetails(String name, URL link, String id) {
			this.name = name;
			this.link = link;
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public URL getLink() {
			return link;
		}

		public String getFilename() {
			return id + ".zip";
		}
	}
}