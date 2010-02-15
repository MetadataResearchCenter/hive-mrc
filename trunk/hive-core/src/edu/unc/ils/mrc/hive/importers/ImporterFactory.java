package edu.unc.ils.mrc.hive.importers;

import edu.unc.ils.mrc.hive.api.SKOSScheme;

public class ImporterFactory {

	public static final int SKOSIMPORTER = 1;

	private static int importer = 1;

	public static void selectImporter(int importer) {
		ImporterFactory.importer = importer;
	}

	public static Importer getImporter(SKOSScheme scheme) {
		if (importer == SKOSIMPORTER)
			return new SKOSImporter(scheme);
		else
			return null;

	}

}
