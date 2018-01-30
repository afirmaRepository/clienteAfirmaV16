/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * You may contact the copyright holder at: soporte.afirma@seap.minhap.es
 */

package es.gob.afirma.standalone.ui.preferences;

import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_GENERAL_DEFAULT_FORMAT_BIN;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_GENERAL_DEFAULT_FORMAT_FACTURAE;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_GENERAL_DEFAULT_FORMAT_ODF;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_GENERAL_DEFAULT_FORMAT_OOXML;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_GENERAL_DEFAULT_FORMAT_PDF;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_GENERAL_DEFAULT_FORMAT_XML;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_GENERAL_HIDE_DNIE_START_SCREEN;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_GENERAL_OMIT_ASKONCLOSE;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_GENERAL_SIGNATURE_ALGORITHM;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_GENERAL_UPDATECHECK;
import static es.gob.afirma.standalone.ui.preferences.PreferencesManager.PREFERENCE_GENERAL_USEANALYTICS;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import es.gob.afirma.core.AOCancelledOperationException;
import es.gob.afirma.core.misc.Platform;
import es.gob.afirma.core.signers.AOSignConstants;
import es.gob.afirma.core.ui.AOUIFactory;
import es.gob.afirma.standalone.AutoFirmaUtil;
import es.gob.afirma.standalone.ProxyUtil;
import es.gob.afirma.standalone.SimpleAfirma;
import es.gob.afirma.standalone.SimpleAfirmaMessages;
import es.gob.afirma.standalone.updater.Updater;

final class PreferencesPanelGeneral extends JPanel {

	private static final long serialVersionUID = 5442844766530064610L;

	static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	private final PreferencesPanel preferencesPanel;

	PreferencesPanel getPrefPanel() {
		return this.preferencesPanel;
	}

	private final JComboBox<String> signarureAlgorithms = new JComboBox<>();

	private static final String XADES = AOSignConstants.SIGN_FORMAT_XADES;
	private static final String CADES = AOSignConstants.SIGN_FORMAT_CADES;
	private static final String PADES = AOSignConstants.SIGN_FORMAT_PADES;
	private static final String OOXML = AOSignConstants.SIGN_FORMAT_OOXML;
	private static final String FACTURAE = AOSignConstants.SIGN_FORMAT_FACTURAE;
	private static final String ODF = AOSignConstants.SIGN_FORMAT_ODF;

	private final JComboBox<String> pdfFilesCombo = new JComboBox<>(new String[] { PADES, CADES, XADES });
	private final JComboBox<String> ooxmlFilesCombo = new JComboBox<>(new String[] { OOXML, CADES, XADES });
	private final JComboBox<String> facturaeFilesCombo = new JComboBox<>(new String[] { FACTURAE, XADES, CADES });
	private final JComboBox<String> xmlFilesCombo = new JComboBox<>(new String[] { XADES, CADES });
	private final JComboBox<String> binFilesCombo = new JComboBox<>(new String[] { CADES, XADES });
	private final JComboBox<String> odfFilesCombo = new JComboBox<>(new String[] { ODF, CADES, XADES });

	private final JCheckBox avoidAskForClose = new JCheckBox(SimpleAfirmaMessages.getString("PreferencesPanel.36")); //$NON-NLS-1$

	private final JCheckBox hideDniStartScreen = new JCheckBox(SimpleAfirmaMessages.getString("PreferencesPanel.81")); //$NON-NLS-1$

	private final JCheckBox checkForUpdates = new JCheckBox(SimpleAfirmaMessages.getString("PreferencesPanel.87")); //$NON-NLS-1$

	private final JCheckBox sendAnalytics = new JCheckBox(SimpleAfirmaMessages.getString("PreferencesPanel.89")); //$NON-NLS-1$

	private final DisposableInterface disposableInterface;
	DisposableInterface getDisposableInterface() {
		return this.disposableInterface;
	}

	/** Atributo para gestionar el bloqueo de propiedades. */
	private boolean blocked;

	PreferencesPanelGeneral(final KeyListener keyListener,
			                final ItemListener modificationListener,
			                final DisposableInterface di,
			                final PreferencesPanel prefPanel,
			                final boolean blocked) {
		this.disposableInterface = di;
		this.preferencesPanel = prefPanel;

		this.blocked = blocked;

		createUI(keyListener, modificationListener);
	}

	void savePreferences() {
		// Opciones varias
		PreferencesManager.put(PREFERENCE_GENERAL_SIGNATURE_ALGORITHM, this.signarureAlgorithms.getSelectedItem().toString());
		PreferencesManager.putBoolean(PREFERENCE_GENERAL_OMIT_ASKONCLOSE, this.avoidAskForClose.isSelected());
		PreferencesManager.putBoolean(PREFERENCE_GENERAL_HIDE_DNIE_START_SCREEN, this.hideDniStartScreen.isSelected());
		if (SimpleAfirma.isUpdatesEnabled()) {
			PreferencesManager.putBoolean(PREFERENCE_GENERAL_UPDATECHECK, this.checkForUpdates.isSelected());
		}
		PreferencesManager.putBoolean(PREFERENCE_GENERAL_USEANALYTICS, this.sendAnalytics.isSelected());

		// Formatos por defecto
		PreferencesManager.put(PREFERENCE_GENERAL_DEFAULT_FORMAT_BIN, this.binFilesCombo.getSelectedItem().toString());
		PreferencesManager.put(PREFERENCE_GENERAL_DEFAULT_FORMAT_FACTURAE, this.facturaeFilesCombo.getSelectedItem().toString());
		PreferencesManager.put(PREFERENCE_GENERAL_DEFAULT_FORMAT_OOXML, this.ooxmlFilesCombo.getSelectedItem().toString());
		PreferencesManager.put(PREFERENCE_GENERAL_DEFAULT_FORMAT_PDF, this.pdfFilesCombo.getSelectedItem().toString());
		PreferencesManager.put(PREFERENCE_GENERAL_DEFAULT_FORMAT_XML, this.xmlFilesCombo.getSelectedItem().toString());
		PreferencesManager.put(PREFERENCE_GENERAL_DEFAULT_FORMAT_ODF, this.odfFilesCombo.getSelectedItem().toString());
	}

	void loadPreferences() {

		this.signarureAlgorithms.setSelectedItem(
			PreferencesManager.get(PREFERENCE_GENERAL_SIGNATURE_ALGORITHM)
		);
		this.avoidAskForClose.setSelected(PreferencesManager.getBoolean(PREFERENCE_GENERAL_OMIT_ASKONCLOSE));
		this.hideDniStartScreen.setSelected(PreferencesManager.getBoolean(PREFERENCE_GENERAL_HIDE_DNIE_START_SCREEN));

		if (
			Boolean.getBoolean(Updater.AUTOFIRMA_AVOID_UPDATE_CHECK) ||
			Boolean.parseBoolean(System.getenv(Updater.AUTOFIRMA_AVOID_UPDATE_CHECK)) ||
			!SimpleAfirma.isUpdatesEnabled()
		) {
			this.checkForUpdates.setSelected(false);
			this.checkForUpdates.setEnabled(false);
		}
		else {
			this.checkForUpdates.setSelected(PreferencesManager.getBoolean(PREFERENCE_GENERAL_UPDATECHECK));
		}

		if (Boolean.getBoolean(SimpleAfirma.DO_NOT_SEND_ANALYTICS) ||
				Boolean.parseBoolean(System.getenv(SimpleAfirma.DO_NOT_SEND_ANALYTICS_ENV))) {
			this.sendAnalytics.setSelected(false);
			this.sendAnalytics.setEnabled(false);
		}
		else {
			this.sendAnalytics.setSelected(PreferencesManager.getBoolean(PREFERENCE_GENERAL_USEANALYTICS));
		}

		// Formatos por defecto
		this.pdfFilesCombo.setSelectedItem(PreferencesManager.get(PREFERENCE_GENERAL_DEFAULT_FORMAT_PDF));
		this.ooxmlFilesCombo.setSelectedItem(PreferencesManager.get(PREFERENCE_GENERAL_DEFAULT_FORMAT_OOXML));
		this.facturaeFilesCombo.setSelectedItem(PreferencesManager.get(PREFERENCE_GENERAL_DEFAULT_FORMAT_FACTURAE));
		this.odfFilesCombo.setSelectedItem(PreferencesManager.get(PREFERENCE_GENERAL_DEFAULT_FORMAT_ODF));
		this.xmlFilesCombo.setSelectedItem(PreferencesManager.get(PREFERENCE_GENERAL_DEFAULT_FORMAT_XML));
		this.binFilesCombo.setSelectedItem(PreferencesManager.get(PREFERENCE_GENERAL_DEFAULT_FORMAT_BIN));
	}

	/** Carga las opciones de configuraci&oacute;n por defecto del panel general
	 * desde un fichero externo de preferencias. */
	void loadDefaultPreferences() {

		try {
			PreferencesManager.clearAll();
		}
		catch (final Exception e) {
			LOGGER.warning("No se pudo restaurar la configuracion de la aplicacion: e"); //$NON-NLS-1$
		}
		loadPreferences();
		getDisposableInterface().disposeInterface();
	}

	void createUI(final KeyListener keyListener,
				  final ItemListener modificationListener) {


		setLayout(new GridBagLayout());

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridx = 0;
		gbc.gridy = 0;

		final FlowLayout fLayout = new FlowLayout(FlowLayout.LEADING);

		final JPanel signConfigPanel = new JPanel(new GridBagLayout());
		signConfigPanel.setBorder(
			BorderFactory.createTitledBorder(
				BorderFactory.createTitledBorder(SimpleAfirmaMessages.getString("PreferencesPanel.108")) //$NON-NLS-1$
			)
		);

		final GridBagConstraints signConstraint = new GridBagConstraints();
		signConstraint.fill = GridBagConstraints.HORIZONTAL;
		signConstraint.weightx = 1.0;
		signConstraint.gridy = 0;
		signConstraint.insets = new Insets(0, 0, 0, 0);

		final JButton importConfigFromFileButton = new JButton(
			SimpleAfirmaMessages.getString("PreferencesPanel.107") //$NON-NLS-1$
		);

		importConfigFromFileButton.setMnemonic('I');
		importConfigFromFileButton.addActionListener(
			ae -> {
				if ((ae.getModifiers() & ActionEvent.ALT_MASK) != 0) {
					final String url = (String) AOUIFactory.showInputDialog(
						getParent(),
						SimpleAfirmaMessages.getString("PreferencesPanel.109"), //$NON-NLS-1$
						SimpleAfirmaMessages.getString("PreferencesPanel.110"), //$NON-NLS-1$
						JOptionPane.QUESTION_MESSAGE,
						null,
						null,
						null
					);
					if (url == null || url.trim().isEmpty()) {
						return;
					}
					try {
						PreferencesPlistHandler.importPreferencesFromUrl(url, isBlocked());
					}
					catch(final Exception e) {
						LOGGER.log(
							Level.SEVERE,
							"Error importando la configuracion desde red (" + url + "): " + e, //$NON-NLS-1$ //$NON-NLS-2$
							e
						);
						AOUIFactory.showErrorMessage(
							getParent(),
							SimpleAfirmaMessages.getString("PreferencesPanel.116"), //$NON-NLS-1$
							SimpleAfirmaMessages.getString("PreferencesPanel.117"), //$NON-NLS-1$
							JOptionPane.ERROR_MESSAGE
						);
						return;
					}
				}
				else {
					final String configFilePath;
					try {
						configFilePath = AOUIFactory.getLoadFiles(
							SimpleAfirmaMessages.getString("PreferencesPanel.86"), //$NON-NLS-1$
							null,
							null,
							new String[] { "afconfig" }, //$NON-NLS-1$
							SimpleAfirmaMessages.getString("PreferencesPanel.111"), //$NON-NLS-1$
							false,
							false,
							AutoFirmaUtil.getDefaultDialogsIcon(),
							PreferencesPanelGeneral.this
						)[0].getAbsolutePath();
					}
					catch(final AOCancelledOperationException ex) {
						// Operacion cancelada por el usuario
						return;
					}
					PreferencesPlistHandler.importPreferences(configFilePath, getParent(), isBlocked());
				}
				AOUIFactory.showMessageDialog(
						getParent(),
						SimpleAfirmaMessages.getString("PreferencesPanel.142"), //$NON-NLS-1$
						SimpleAfirmaMessages.getString("PreferencesPanel.143"), //$NON-NLS-1$
						JOptionPane.INFORMATION_MESSAGE
					);
				getDisposableInterface().disposeInterface();
			}
		);
		importConfigFromFileButton.getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("PreferencesPanel.112") //$NON-NLS-1$
		);

		importConfigFromFileButton.setEnabled(!this.blocked);

		final JButton restoreConfigFromFileButton = new JButton(
			SimpleAfirmaMessages.getString("PreferencesPanel.135") //$NON-NLS-1$
		);

		restoreConfigFromFileButton.setMnemonic('R');
		restoreConfigFromFileButton.addActionListener(ae -> {
			if (AOUIFactory.showConfirmDialog(getParent(), SimpleAfirmaMessages.getString("PreferencesPanel.140"), //$NON-NLS-1$
					SimpleAfirmaMessages.getString("PreferencesPanel.139"), //$NON-NLS-1$
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {

				loadDefaultPreferences();
			}
		});
		restoreConfigFromFileButton.getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("PreferencesPanel.136") //$NON-NLS-1$
		);

		restoreConfigFromFileButton.setEnabled(!this.blocked);

		final JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));

		importConfigFromFileButton.setPreferredSize(new Dimension(280, 28));
		panel.add(importConfigFromFileButton);
		restoreConfigFromFileButton.setPreferredSize(new Dimension(280, 28));
		panel.add(restoreConfigFromFileButton);

		signConfigPanel.add(panel, signConstraint);

		signConstraint.insets = new Insets(5, 7, 3, 7);
		signConstraint.anchor = GridBagConstraints.LINE_START;

		signConstraint.gridy++;

		this.avoidAskForClose.getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("PreferencesPanel.44") //$NON-NLS-1$
		);
		this.avoidAskForClose.setMnemonic('N');
		this.avoidAskForClose.addItemListener(modificationListener);
		this.avoidAskForClose.addKeyListener(keyListener);
		signConfigPanel.add(this.avoidAskForClose, signConstraint);

		signConstraint.gridy++;

		this.hideDniStartScreen.getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("PreferencesPanel.82") //$NON-NLS-1$
		);
		this.hideDniStartScreen.setMnemonic('D');
		this.hideDniStartScreen.addItemListener(modificationListener);
		this.hideDniStartScreen.addKeyListener(keyListener);
		signConfigPanel.add(this.hideDniStartScreen, signConstraint);

		signConstraint.gridy++;

		// Solo se buscaran actualizaciones automaticamente en Windows
		if (SimpleAfirma.isUpdatesEnabled() && Platform.OS.WINDOWS.equals(Platform.getOS())) {
			this.checkForUpdates.getAccessibleContext().setAccessibleDescription(
				SimpleAfirmaMessages.getString("PreferencesPanel.88") //$NON-NLS-1$
			);
			this.checkForUpdates.setMnemonic('B');
			this.checkForUpdates.addItemListener(modificationListener);
			this.checkForUpdates.addKeyListener(keyListener);
			signConfigPanel.add(this.checkForUpdates, signConstraint);
			signConstraint.gridy++;
		}

		this.sendAnalytics.getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("PreferencesPanel.90") //$NON-NLS-1$
		);
		this.sendAnalytics.setMnemonic('t');
		this.sendAnalytics.addItemListener(modificationListener);
		this.sendAnalytics.addKeyListener(keyListener);
		signConfigPanel.add(this.sendAnalytics, signConstraint);

		add(signConfigPanel, gbc);

		final JPanel signGeneralPanel = new JPanel(new GridBagLayout());
		signGeneralPanel.setBorder(
			BorderFactory.createTitledBorder(
				BorderFactory.createTitledBorder(
					SimpleAfirmaMessages.getString("PreferencesPanel.17") //$NON-NLS-1$
				)
			)
		);

		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.gridy = 0;
		c.insets = new Insets(0, 7, 0, 7);

		final JPanel signatureAgorithmPanel = new JPanel(fLayout);
		signatureAgorithmPanel.setBorder(
			BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(), SimpleAfirmaMessages.getString("PreferencesPanel.18") //$NON-NLS-1$
			)
		);
		this.signarureAlgorithms.getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("PreferencesPanel.46") //$NON-NLS-1$
		);
		this.signarureAlgorithms.addItemListener(modificationListener);
		this.signarureAlgorithms.addKeyListener(keyListener);
		this.signarureAlgorithms.setModel(
			new DefaultComboBoxModel<>(
				new String[] {
					"SHA1withRSA", //$NON-NLS-1$
					"SHA512withRSA", //$NON-NLS-1$
					"SHA384withRSA", //$NON-NLS-1$
					"SHA256withRSA" //$NON-NLS-1$
				}
			)
		);
		this.signarureAlgorithms.setEnabled(!isBlocked());
		signatureAgorithmPanel.add(this.signarureAlgorithms);

		signGeneralPanel.add(signatureAgorithmPanel, c);

		final JPanel signatureDefaultsFormats = createSignatureFormatPanel(
			modificationListener,
			keyListener
		);

		final JPanel netConfigPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		netConfigPanel.setBorder(
			BorderFactory.createTitledBorder(
				BorderFactory.createTitledBorder(SimpleAfirmaMessages.getString("PreferencesPanel.125")) //$NON-NLS-1$
			)
		);

		final JButton proxyConfigButton = new JButton(
			SimpleAfirmaMessages.getString("PreferencesPanel.126") //$NON-NLS-1$
		);

		proxyConfigButton.setMnemonic('P');
		proxyConfigButton.addActionListener(
			ae -> changeProxyDlg(getParent())
		);
		proxyConfigButton.getAccessibleContext().setAccessibleDescription(
			SimpleAfirmaMessages.getString("PreferencesPanel.127") //$NON-NLS-1$
		);

		final JLabel proxyLabel = new JLabel(SimpleAfirmaMessages.getString("PreferencesPanel.128")); //$NON-NLS-1$
		proxyLabel.setLabelFor(proxyConfigButton);

		netConfigPanel.add(proxyLabel);
		netConfigPanel.add(proxyConfigButton);

		c.gridy++;
		signGeneralPanel.add(signatureDefaultsFormats, c);
		gbc.gridy++;
		add(signGeneralPanel, gbc);
		gbc.gridy++;
		add(netConfigPanel, gbc);
		gbc.weighty = 1.0;
		gbc.gridy++;
		add(new JPanel(), gbc);

		loadPreferences();
	}

	/** Crea el panel con la configuraci&oacute;n de los formatos de firma a utilizar con cada tipo de fichero.
	 * @param modificationListener Listener para la detecci&oacute;n de cambio de configuraci&oacute;n.
	 * @param keyListener Listener para la detecci&oacute;n del uso de teclas para el cierre de la pantalla.
	 * @return Panel con los componentes de configuraci&oacute;n. */
	private JPanel createSignatureFormatPanel(final ItemListener modificationListener,
		 	                                  final KeyListener keyListener) {

		final JPanel signatureDefaultsFormats = new JPanel(new GridBagLayout());
		signatureDefaultsFormats.setBorder(
			BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(),
				SimpleAfirmaMessages.getString("PreferencesPanel.39") //$NON-NLS-1$
			)
		);

		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(5, 7, 0, 7);

		// PDF
		final JLabel pdfFilesLabel = new JLabel(SimpleAfirmaMessages.getString("PreferencesPanel.74")); //$NON-NLS-1$
		pdfFilesLabel.setLabelFor(this.pdfFilesCombo);
		this.pdfFilesCombo.addItemListener(modificationListener);
		this.pdfFilesCombo.addKeyListener(keyListener);
		this.pdfFilesCombo.setEnabled(!isBlocked());
		c.gridx = 0;
		c.weightx = 0;
		signatureDefaultsFormats.add(pdfFilesLabel, c);
		c.gridx = 1;
		c.weightx = 1.0;
		signatureDefaultsFormats.add(this.pdfFilesCombo, c);
		c.gridy++;

		// OOXML
		final JLabel ooxmlFilesLabel = new JLabel(SimpleAfirmaMessages.getString("PreferencesPanel.75")); //$NON-NLS-1$
		ooxmlFilesLabel.setLabelFor(this.ooxmlFilesCombo);
		this.ooxmlFilesCombo.addItemListener(modificationListener);
		this.ooxmlFilesCombo.addKeyListener(keyListener);
		this.ooxmlFilesCombo.setEnabled(!isBlocked());

		c.gridx = 0;
		c.weightx = 0;
		signatureDefaultsFormats.add(ooxmlFilesLabel, c);
		c.gridx = 1;
		c.weightx = 1.0;
		signatureDefaultsFormats.add(this.ooxmlFilesCombo, c);
		c.gridy++;

		// FACTURAE
		final JLabel facturaeFilesLabel = new JLabel(SimpleAfirmaMessages.getString("PreferencesPanel.76")); //$NON-NLS-1$
		facturaeFilesLabel.setLabelFor(this.facturaeFilesCombo);
		this.facturaeFilesCombo.addItemListener(modificationListener);
		this.facturaeFilesCombo.addKeyListener(keyListener);
		this.facturaeFilesCombo.setEnabled(!isBlocked());

		c.gridx = 0;
		c.weightx = 0;
		signatureDefaultsFormats.add(facturaeFilesLabel, c);
		c.gridx = 1;
		c.weightx = 1.0;
		signatureDefaultsFormats.add(this.facturaeFilesCombo, c);
		c.gridy++;

		// XML
		final JLabel xmlFilesLabel = new JLabel(SimpleAfirmaMessages.getString("PreferencesPanel.77")); //$NON-NLS-1$
		xmlFilesLabel.setLabelFor(this.xmlFilesCombo);
		this.xmlFilesCombo.addItemListener(modificationListener);
		this.xmlFilesCombo.addKeyListener(keyListener);
		this.xmlFilesCombo.setEnabled(!isBlocked());

		c.gridx = 0;
		c.weightx = 0;
		signatureDefaultsFormats.add(xmlFilesLabel, c);
		c.gridx = 1;
		c.weightx = 1.0;
		signatureDefaultsFormats.add(this.xmlFilesCombo, c);
		c.gridy++;

		// ODF
		final JLabel odfFilesLabel = new JLabel(SimpleAfirmaMessages.getString("PreferencesPanel.83")); //$NON-NLS-1$
		odfFilesLabel.setLabelFor(this.odfFilesCombo);
		this.odfFilesCombo.addItemListener(modificationListener);
		this.odfFilesCombo.addKeyListener(keyListener);
		this.odfFilesCombo.setEnabled(!isBlocked());

		c.gridx = 0;
		c.weightx = 0;
		signatureDefaultsFormats.add(odfFilesLabel, c);
		c.gridx = 1;
		c.weightx = 1.0;
		signatureDefaultsFormats.add(this.odfFilesCombo, c);
		c.gridy++;

		// BIN
		final JLabel binFilesLabel = new JLabel(SimpleAfirmaMessages.getString("PreferencesPanel.78")); //$NON-NLS-1$
		binFilesLabel.setLabelFor(this.binFilesCombo);
		this.binFilesCombo.addItemListener(modificationListener);
		this.binFilesCombo.addKeyListener(keyListener);
		this.binFilesCombo.setEnabled(!isBlocked());

		c.gridx = 0;
		c.weightx = 0;
		signatureDefaultsFormats.add(binFilesLabel, c);
		c.gridx = 1;
		signatureDefaultsFormats.add(this.binFilesCombo, c);
		c.gridy++;
		c.weightx = 1.0;

		return signatureDefaultsFormats;
	}

	/** Di&aacute;logo para cambiar la configuraci&oacute;n del <i>proxy</i>.
	 * @param container Contenedor en el que se define el di&aacute;logo. */
    public static void changeProxyDlg(final Container container) {

    	// Cursor en espera
    	container.setCursor(new Cursor(Cursor.WAIT_CURSOR));

    	final ProxyPanel proxyDlg = new ProxyPanel();

    	// Cursor por defecto
    	container.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

    	if(AOUIFactory.showConfirmDialog(
				container,
				proxyDlg,
				SimpleAfirmaMessages.getString("ProxyDialog.0"), //$NON-NLS-1$
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.DEFAULT_OPTION
		) == JOptionPane.OK_OPTION) {

			if (proxyDlg.isProxySelected()) {
				final String host = proxyDlg.getHost();
				final String port = proxyDlg.getPort();

				if(host == null || host == "") { //$NON-NLS-1$
					AOUIFactory.showErrorMessage(
						null,
						SimpleAfirmaMessages.getString("ProxyDialog.1"), //$NON-NLS-1$
						SimpleAfirmaMessages.getString("ProxyDialog.2"), //$NON-NLS-1$
						JOptionPane.ERROR_MESSAGE
					);
					changeProxyDlg(container);
					LOGGER.warning("El host no puede ser nulo o vacio"); //$NON-NLS-1$
				}
				else if(port == null || port == "") { //$NON-NLS-1$
					AOUIFactory.showErrorMessage(
						null,
						SimpleAfirmaMessages.getString("ProxyDialog.3"), //$NON-NLS-1$
						SimpleAfirmaMessages.getString("ProxyDialog.2"), //$NON-NLS-1$
						JOptionPane.ERROR_MESSAGE
					);
					changeProxyDlg(container);
					LOGGER.warning("El puerto no puede ser nulo, vacio o tener mas de 4 digitos"); //$NON-NLS-1$
				}
				else {
					PreferencesManager.put(PreferencesManager.PREFERENCE_GENERAL_PROXY_HOST, host);
					PreferencesManager.put(PreferencesManager.PREFERENCE_GENERAL_PROXY_PORT, port);

					// Si no se establece usuario, nos aseguramos de eliminar el actual. Si se establece, lo guardamos.
					if (proxyDlg.getUsername() == null || proxyDlg.getUsername().isEmpty()) {
						PreferencesManager.remove(PreferencesManager.PREFERENCE_GENERAL_PROXY_USERNAME);
					}
					else {
						PreferencesManager.put(PreferencesManager.PREFERENCE_GENERAL_PROXY_USERNAME, proxyDlg.getUsername());
					}

					// Si no se establece contrasena, nos aseguramos de eliminar la actual. Si se establece,
					// la guardamos cifrada.
					if (proxyDlg.getPassword() == null || proxyDlg.getPassword().length == 0) {
						PreferencesManager.remove(PreferencesManager.PREFERENCE_GENERAL_PROXY_PASSWORD);
					}
					else {
						try {
							final String cipheredPwd = ProxyUtil.cipherPassword(proxyDlg.getPassword());
							if (cipheredPwd != null) {
								PreferencesManager.put(PreferencesManager.PREFERENCE_GENERAL_PROXY_PASSWORD, cipheredPwd);
							}
						}
						catch (final GeneralSecurityException e) {
							LOGGER.severe("Error cifrando la contrasena del Proxy: " + e); //$NON-NLS-1$
							JOptionPane.showMessageDialog(container, SimpleAfirmaMessages.getString("ProxyDialog.19")); //$NON-NLS-1$);
							PreferencesManager.put(PreferencesManager.PREFERENCE_GENERAL_PROXY_PASSWORD, ""); //$NON-NLS-1$
						}
					}
				}
			}
			else {
				PreferencesManager.remove(PreferencesManager.PREFERENCE_GENERAL_PROXY_HOST);
				PreferencesManager.remove(PreferencesManager.PREFERENCE_GENERAL_PROXY_PORT);
				PreferencesManager.remove(PreferencesManager.PREFERENCE_GENERAL_PROXY_USERNAME);
				PreferencesManager.remove(PreferencesManager.PREFERENCE_GENERAL_PROXY_PASSWORD);
			}

			PreferencesManager.putBoolean(
				PreferencesManager.PREFERENCE_GENERAL_PROXY_SELECTED,
				proxyDlg.isProxySelected()
			);

			// Aplicamos los valores tanto si el checkbox esta marcado o no, en un caso lo establecera y en en otro lo
			// eliminara
			ProxyUtil.setProxySettings();
    	}
    }

    /**
	 * Indica si el panel permite o no la edici&oacute;n de sus valores.
	 * @return {@code true} si est&aacute; bloqueado y no permite la edici&oacute;n,
	 * {@code false} en caso contrario.
	 */
	public boolean isBlocked() {
		return this.blocked;
	}

	/**
	 * Establece si deben bloquearse las opciones de configuraci&oacute;n del panel.
	 * @param blocked {@code true} si las opciones de configuraci&oacute;n deben bloquearse,
	 * {@code false} en caso contrario.
	 */
	public void setBlocked(final boolean blocked) {
		this.blocked = blocked;
	}
}
