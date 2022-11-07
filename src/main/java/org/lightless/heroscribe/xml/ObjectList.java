package org.lightless.heroscribe.xml;

import javax.xml.bind.annotation.*;

/**
 * @author Andoni del Olmo
 * @since 07/11/2022
 */
@XmlRootElement
public class ObjectList {

	private String version;
	private String vectorPrefix;
	private String vectorSuffix;
	private String rasterPrefix;
	private String rasterSuffix;
	private String samplePrefix;
	private String sampleSuffix;


}