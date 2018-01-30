/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * You may contact the copyright holder at: soporte.afirma@seap.minhap.es
 */

package es.gob.afirma.standalone.ui.restoreconfig;

import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

import es.gob.afirma.standalone.SimpleAfirmaMessages;

/**
 * Clase que dibuja la ventana de restauraci&oacute;n de configuraci&oacute;n
 * de navegadores
 *
 */
public final class RestoreConfigDialog extends JDialog {

	/**
	 * Attribute that represents the serial version.
	 */
	private static final long serialVersionUID = -241482490367263150L;

	/** Constructor del panel de preferencias.
	 * @param parent padre del panel de preferencias.
	 * @param modal modal del panel de preferencias.
	 */
	public RestoreConfigDialog(final Frame parent, final boolean modal) {
		super(parent, modal);
		setTitle(SimpleAfirmaMessages.getString("MainMenu.20")); //$NON-NLS-1$
		this.add(new RestoreConfigPanel(this));
		this.setSize(600, 550);
		setResizable(false);
		setLocationRelativeTo(parent);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	/** Muestra el di&aacute;logo de restauraci&oacute;n de configuraci&oacute;n.
	 * @param parent Componente padre del panel de preferencias.
	 * @param modal Modalidad del panel de preferencias.
	 * @param selectedPreferencesTabIndex &Iacute;dice de la pesta&ntilde;a de configuraci&oacute;n seleccionada por defecto. */
	public static void show(final Frame parent, final boolean modal, final int selectedPreferencesTabIndex) {
		new RestoreConfigDialog(parent, modal).setVisible(true);
	}

	/** Muestra el di&aacute;logo de restauraci&oacute;n de configuraci&oacute;n.
	 * @param parent Componente padre del panel de restauracion de configuraci&oacute;n.
	 * @param modal Modalidad del panel de restauracion de configuracion. */
	public static void show(final Frame parent, final boolean modal) {
		new RestoreConfigDialog(parent, modal).setVisible(true);
	}

}
