/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * You may contact the copyright holder at: soporte.afirma@seap.minhap.es
 */

package es.gob.afirma.standalone;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.logging.Logger;

import es.gob.afirma.standalone.crypto.CypherDataManager;
import es.gob.afirma.standalone.ui.preferences.PreferencesManager;

/** Utilidades para el manejo y establecimiento del <i>Proxy</i> de red para las
 * conexiones de la aplicaci&oacute;n.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class ProxyUtil {

	private static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	/** Indica si las variables del Proxy que pudiesen encontrarse en las variables de entorno de Java
	 * provienen de un establecimiento externo (en cuyo caso no se alteran nunca) o si han sido establecidas a
	 * trav&eacute;s del GUI de AutoFirma, en cuyo caso deben limpiarse al desmarcar la casilla "Usar proxy",
	 * para efectivamente dejar de usar un proxy de red. */
	private static boolean clearOnUncheck = false;

	private ProxyUtil() {
		// No instanciable
	}

	private static boolean setDefaultHttpProxy() {
		return setDefaultProxy("http://www.google.com", "http.proxyHost", "http.proxyPort"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	private static boolean setDefaultHttpsProxy() {
		return setDefaultProxy("https://www.google.com", "https.proxyHost", "https.proxyPort"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	private static boolean setDefaultProxy(final String urlToCkech, final String hostProperty, final String portProperty) {
		System.setProperty("java.net.useSystemProxies", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		final List<Proxy> l;
		final URI uri;
		try {
			uri = new URI(urlToCkech);
		    l = ProxySelector.getDefault().select(uri);
		}
		catch (final URISyntaxException e) {
			LOGGER.warning(
				"No se ha podido comprobar el proxy por defecto: " + e //$NON-NLS-1$
			);
		    return false;
		}
		if (l != null) {
		    for (final Proxy proxy : l) {
		    	LOGGER.info(
	    			"Las conexiones para protocolo '" + uri.getScheme() + "' son por defecto de tipo: " + proxy.type() //$NON-NLS-1$ //$NON-NLS-2$
    			);
		        final InetSocketAddress addr = (InetSocketAddress) proxy.address();

		        if (addr == null) {
		            continue;
		        }

				System.setProperty(hostProperty, addr.getHostName());
				System.setProperty(portProperty, Integer.toString(addr.getPort()));

		        LOGGER.info(
					"Se usara el Proxy configurado en sistema para las conexiones de red HTTP: " + addr.getHostName() + ":" + addr.getPort() //$NON-NLS-1$ //$NON-NLS-2$
				);

				return true;
		    }
		}
		return false;
	}

	/** Establece la configuraci&oacute;n para el servidor <i>Proxy</i> seg&uacute;n los valores
     * de configuraci&oacute;n encontrados. */
    public static void setProxySettings() {

    	if (PreferencesManager.getBoolean(PreferencesManager.PREFERENCE_GENERAL_PROXY_SELECTED)) {

    		final String proxyHost = PreferencesManager.get(PreferencesManager.PREFERENCE_GENERAL_PROXY_HOST);
    		final String proxyPort = PreferencesManager.get(PreferencesManager.PREFERENCE_GENERAL_PROXY_PORT);
    		final String proxyUsername = PreferencesManager.get(PreferencesManager.PREFERENCE_GENERAL_PROXY_USERNAME);
    		final String cipheredProxyPassword = PreferencesManager.get(PreferencesManager.PREFERENCE_GENERAL_PROXY_PASSWORD);

    		if (proxyHost != null &&
    				!proxyHost.trim().isEmpty() &&
    					proxyPort != null &&
    						!proxyPort.trim().isEmpty()) {

    			LOGGER.info(
					"Establecido Proxy de red desde el GUI de la aplicacion: " + proxyHost + ":" + proxyPort //$NON-NLS-1$ //$NON-NLS-2$
				);

    			System.setProperty("http.proxyHost", proxyHost); //$NON-NLS-1$
    			System.setProperty("http.proxyPort", proxyPort); //$NON-NLS-1$

    			System.setProperty("https.proxyHost", proxyHost); //$NON-NLS-1$
    			System.setProperty("https.proxyPort", proxyPort); //$NON-NLS-1$

    			System.setProperty("ftp.proxHost", proxyHost); //$NON-NLS-1$
    			System.setProperty("ftp.proxyPort", proxyPort); //$NON-NLS-1$

    			System.setProperty("socksProxyHost", proxyHost); //$NON-NLS-1$
    			System.setProperty("socksProxyPort", proxyPort); //$NON-NLS-1$

        		if (proxyUsername != null &&
        				!proxyUsername.trim().trim().isEmpty() &&
        					cipheredProxyPassword != null &&
        						!cipheredProxyPassword.trim().trim().isEmpty()) {

        			final char[] proxyPassword;
					try {
						proxyPassword = decipherPassword(cipheredProxyPassword);
					}
					catch (final Exception e) {
						LOGGER.warning("No se pudo descifrar la contrasena del proxy. No se configurara el usuario y contrasena: " + e); //$NON-NLS-1$
						Authenticator.setDefault(null);
						return;
					}

        			Authenticator.setDefault(
    					new Authenticator() {
	    			        @Override
	    					public PasswordAuthentication getPasswordAuthentication() {
	    			            return new PasswordAuthentication(
    			            		proxyUsername,
    			            		proxyPassword
			            		);
	    			        }
	    			    }
					);
        		}

        		clearOnUncheck = true;
    		}
    		// Indicar que si se quiere usar proxy pero con los valores en blanco se entiende
    		// como limpiar valores que pudiesen estar ya establecidos en Java
    		else {
    			clearJavaProxy();
    		}
    	}
    	// Si no se indica nada en el GUI de AutoFirma, se dejan los valores por defecto de
    	// la JVM tal y como estaban, nunca se sobreescriben, a menos que estos mismos valores
    	// hubiesen sido establecidos con el mismo AutoFirma.
    	else {
    		// Si es establecio el Proxy con AutoFirma y se desmarca, se borra con AutoFirma
    		if (clearOnUncheck) {
    			clearJavaProxy();
    		}
    		// Si se desmarca con AutoFirma pero habia un Proxy establecido externamente, se respeta
    		else {
	    		final String javaProxyHost = System.getProperty("http.proxyHost"); //$NON-NLS-1$
	    		final String javaProxyPort = System.getProperty("http.proxyPort"); //$NON-NLS-1$
	    		// Si hay un proxy establecido a nivel de JVM...
	    		if (javaProxyHost != null &&
	    				javaProxyPort != null &&
	    					!javaProxyHost.trim().isEmpty() &&
	    						!javaProxyPort.trim().isEmpty()) {
	    			LOGGER.info(
						"Se usara el Proxy por defecto de Java para las conexiones de red: " + javaProxyHost + ":" + javaProxyPort //$NON-NLS-1$ //$NON-NLS-2$
					);
	    		}
	    		// Miramos a ver si hay establecido un proxy a nivel de SO (solo para Windows 7 y
	    		// superiores y Linux con GNOME. Si lo hay, las funciones lo aplican
	    		else if (!setDefaultHttpProxy() && !setDefaultHttpsProxy()) {
	    			// Si las funciones no han encontrado y establecido un proxy a nivel de SO, ya no nos
	    			// quedan mas opciones, no se usa proxy
	    			LOGGER.info("No se usara Proxy para las conexiones de red"); //$NON-NLS-1$
	    		}
    		}
    	}
    }

    private static void clearJavaProxy() {
		LOGGER.info("No se usara Proxy para las conexiones de red"); //$NON-NLS-1$
		System.clearProperty("http.proxyHost"); //$NON-NLS-1$
		System.clearProperty("http.proxyPort"); //$NON-NLS-1$
		System.clearProperty("http.nonProxyHosts"); //$NON-NLS-1$
		System.clearProperty("https.proxyHost"); //$NON-NLS-1$
		System.clearProperty("https.proxyPort"); //$NON-NLS-1$
		System.clearProperty("https.nonProxyHosts"); //$NON-NLS-1$
		System.clearProperty("ftp.proxHost"); //$NON-NLS-1$
		System.clearProperty("ftp.proxyPort"); //$NON-NLS-1$
		System.clearProperty("ftp.nonProxyHosts"); //$NON-NLS-1$
		System.clearProperty("socks.proxyHost"); //$NON-NLS-1$
		System.clearProperty("socks.proxyPort"); //$NON-NLS-1$
		System.clearProperty("socks.nonProxyHosts"); //$NON-NLS-1$
		Authenticator.setDefault(null);
    }

    private static final char[] PWD_CIPHER_KEY = new char[] {'8', 'W', '{', 't', '2', 'r', ',', 'B'};

    /** Cifra una contrase&ntilde;a.
     * @param password Contrase&ntilde;a cifrada o <code>null</code> si la contrase&ntilde;a proporcionada es
     *                 nula o vac&iacute;a.
     * @return Contrase&tilde;a cifrada en base 64.
     * @throws GeneralSecurityException Cuando se produce un error durante el cifrado. */
    public static String cipherPassword(final char[] password) throws GeneralSecurityException {
    	if (password == null || password.length < 1) {
    		return null;
    	}
    	return CypherDataManager.cipherData(
			String.valueOf(password).getBytes(StandardCharsets.UTF_8),
			String.valueOf(
				PWD_CIPHER_KEY
			).getBytes(StandardCharsets.UTF_8)
		);
    }

    /** Descifra una contrase&ntilde;a
     * @param cipheredPassword Contrase&ntilde;a cifrada en base 64.
     * @return Contrase&ntilde;a o <code>null</code> si la entrada cifrada era nula o vac&iacute;a.
     * @throws GeneralSecurityException Cuando se produce un error durante el descifrado.
     * @throws IOException Cuando los datos introducidos no son un base 64 v&aacute;lido. */
    public static char[] decipherPassword(final String cipheredPassword) throws GeneralSecurityException, IOException {
    	if (cipheredPassword == null  || cipheredPassword.isEmpty()) {
    		return null;
    	}
    	final byte[] p = CypherDataManager.decipherData(
			cipheredPassword.getBytes(StandardCharsets.UTF_8),
			String.valueOf(
				PWD_CIPHER_KEY
			).getBytes(StandardCharsets.UTF_8)
		);
    	return new String(p, StandardCharsets.UTF_8).toCharArray();
    }
}
