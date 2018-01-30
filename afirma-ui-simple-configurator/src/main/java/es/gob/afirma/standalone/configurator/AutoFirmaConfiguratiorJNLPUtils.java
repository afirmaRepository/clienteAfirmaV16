/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * You may contact the copyright holder at: soporte.afirma@seap.minhap.es
 */

package es.gob.afirma.standalone.configurator;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Funciones de utilidad para dar soporte al despliegue JNLP.
 */
public class AutoFirmaConfiguratiorJNLPUtils {

	private static boolean jnlpDeploymentIdentified = false;
	private static Object extendedService = null;


	/**
	 * Indica si nos encontramos en un despliegue JNLP.
	 * @return {@code true} si nos encontramos en un despliegue JNLP, {@code false} en
	 * caso contrario.
	 */
	public static boolean isJNLPDeployment() {
		return getJnlpExtendedService() != null;
	}

	/**
	 * Recupera un objeto {@code javax.jnlp.ExtendedService} con el que controlar
	 * funciones del servicio JNLP.
	 * @return Objeto {@code javax.jnlp.ExtendedService} o {@code null} en caso de
	 * no encontrarnos en un despliegue JNLP o no poder recuperar el servicio.
	 */
	private static Object getJnlpExtendedService() {

		if (!jnlpDeploymentIdentified) {
			try {
				extendedService = javax.jnlp.ServiceManager.lookup("javax.jnlp.ExtendedService"); //$NON-NLS-1$
			} catch (final Throwable e) {
				extendedService = null;
			}
		}
		return extendedService;
	}

	/**
	 * Selecciona un fichero para empezar a escribir en &eacute;l.
	 * @param outFile Fichero de salida.
	 * @return Flujo de datos en el que escribir.
	 * @throws IOException Si no se puede crear o escribir en el fichero.
	 */
	public static OutputStream selectFileToWrite(final File outFile) throws IOException {
		return selectFileToWrite(outFile, false);
	}

	/**
	 * Selecciona un fichero para empezar a escribir en &eacute;l. Debe comprobarse antes si nos
	 * encontramos en un entorno JNLP.
	 * @param outFile Fichero de salida.
	 * @param append Indica si el nuevo contenido se debe agregar al que ya hay en el fichero.
	 * @return Flujo de datos en el que escribir.
	 * @throws IOException Si no se puede crear o escribir en el fichero.
	 * @throws java.lang.NoClassDefFoundError Si no nos encontramos en un entorno JNLP.
	 */
	public static OutputStream selectFileToWrite(final File outFile, final boolean append) throws IOException {
		return ((javax.jnlp.ExtendedService) getJnlpExtendedService())
				.openFile(outFile).getOutputStream(append);
	}

}
