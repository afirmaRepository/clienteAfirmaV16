package es.gob.afirma.cert.signvalidation;

import java.io.InputStream;

import org.junit.Test;

import es.gob.afirma.core.misc.AOUtil;

/** Pruebas de validaci&oacute;n de firmas.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public class TestSignatureValidation {

	private static final String CADES_IMPLICIT_FILE = "cades_implicit.csig"; //$NON-NLS-1$
	private static final String CADES_EXPLICIT_FILE = "cades_explicit.csig"; //$NON-NLS-1$
	private static final String DATA_TXT_FILE = "txt"; //$NON-NLS-1$
	private static final String PADES_FILE = "pades.pdf"; //$NON-NLS-1$
	private static final String PADES_EPES_FILE = "pades_epes.pdf"; //$NON-NLS-1$

	/** Prueba de validaci&oacute;n de firma CAdES.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void testCadesImplicitValidation() throws Exception {
		try (
			final InputStream is = ClassLoader.getSystemResourceAsStream(CADES_IMPLICIT_FILE);
		) {
			final byte[] cades = AOUtil.getDataFromInputStream(is);
			System.out.println(ValidateBinarySignature.validate(cades, null));
		}
	}

	/** Prueba de validaci&oacute;n de firma CAdES explicita sin datos.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void testCadesExplicitValidation() throws Exception {
		try (
			final InputStream is = ClassLoader.getSystemResourceAsStream(CADES_EXPLICIT_FILE);
		) {
			final byte[] cades = AOUtil.getDataFromInputStream(is);
			System.out.println(ValidateBinarySignature.validate(cades, null));
		}
	}

	/** Prueba de validaci&oacute;n de firma CAdES expl&iacute;cita con datos erroneos.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void testCadesExplicitValidationWrongData() throws Exception {
		try (
			final InputStream is = ClassLoader.getSystemResourceAsStream(CADES_EXPLICIT_FILE);
			final InputStream dataIs = ClassLoader.getSystemResourceAsStream(DATA_TXT_FILE);
		) {
			final byte[] cades = AOUtil.getDataFromInputStream(is);
			final byte[] data = AOUtil.getDataFromInputStream(dataIs);
			System.out.println(ValidateBinarySignature.validate(cades, data));
		}
	}

	/** Prueba de validaci&oacute;n de firma PAdES.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void testPadesValidation() throws Exception {
		try (
			final InputStream is = ClassLoader.getSystemResourceAsStream(PADES_FILE);
		) {
			final byte[] pades = AOUtil.getDataFromInputStream(is);
			System.out.println(SignValiderFactory.getSignValider(pades).validate(pades));
		}
	}

	/** Prueba de validaci&oacute;n de firma PAdES-EPES.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void testPadesEpesValidation() throws Exception {
		try (
			final InputStream is = ClassLoader.getSystemResourceAsStream(PADES_EPES_FILE);
		) {
			final byte[] pades = AOUtil.getDataFromInputStream(is);
			System.out.println(SignValiderFactory.getSignValider(pades).validate(pades));
		}
	}
}

