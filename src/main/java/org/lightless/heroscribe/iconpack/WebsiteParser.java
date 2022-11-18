package org.lightless.heroscribe.iconpack;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.io.*;
import java.net.*;
import java.time.*;
import java.util.*;

public class WebsiteParser {

	private static final String HEROSCRIBE_ICONS_URL =
			"http://www.heroscribe.org/icons.html";

	public List<IconPackDetails> parse() throws IOException {
		final Document document =
				Jsoup.parse(new URL(HEROSCRIBE_ICONS_URL),
						Math.toIntExact(Duration.ofSeconds(5).toMillis()));

		return downloadIconPacksMetadata(document);
	}

	public List<IconPackDetails> downloadIconPacksMetadata(Document document) {
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

	private IconPackDetails extractIconPack(Elements elements) {
		final String name = elements.get(0).text();
		final String link = elements.get(0).attr("href");
		final String href = elements.get(1).attr("href");
		final String filename = href.substring(href.lastIndexOf('/') + 1);
		final String id = filename.split("\\.")[0];

		return new IconPackDetails(name, link, id);
	}

	public static class IconPackDetails {
		private final String name;
		private final String link;
		private final String id;

		public IconPackDetails(String name, String link, String id) {
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

		public String getLink() {
			return link;
		}

		public String getFilename() {
			return id + ".zip";
		}
	}
}