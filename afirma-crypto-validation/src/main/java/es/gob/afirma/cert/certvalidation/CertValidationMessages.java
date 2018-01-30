/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * You may contact the copyright holder at: soporte.afirma@seap.minhap.es
 */

package es.gob.afirma.cert.certvalidation;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

final class CertValidationMessages {

	private static final String BUNDLE_NAME = "certvalidationmessages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private CertValidationMessages() {
		// No instanciable
	}

	static String getString(final String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		}
		catch (final MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
