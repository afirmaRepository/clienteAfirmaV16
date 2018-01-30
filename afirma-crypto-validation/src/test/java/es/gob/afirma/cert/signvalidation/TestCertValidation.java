package es.gob.afirma.cert.signvalidation;

import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.junit.Ignore;
import org.junit.Test;

import es.gob.afirma.cert.certvalidation.CertificateVerifierFactory;
import es.gob.afirma.cert.certvalidation.ValidationResult;

/** Pruebas de validaci&oacute;n de certificados.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class TestCertValidation {

	/** Prueba de certificados FNMT Componentes.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	@Ignore
	public void testFnmt() throws Exception {
		final X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
			TestCertValidation.class.getResourceAsStream("/cert_test_fnmt.cer") //$NON-NLS-1$
		);
		final ValidationResult vr = CertificateVerifierFactory.getCertificateVerifier(
			cert
		).validateCertificate();
		vr.check();
	}

	/** Prueba de certificados MDEF.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	@Ignore
	public void testMdef() throws Exception {
		final X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
			TestCertValidation.class.getResourceAsStream("/Defensa.cer") //$NON-NLS-1$
		);
		final ValidationResult vr = CertificateVerifierFactory.getCertificateVerifier(
			cert
		).validateCertificate();
		vr.check();
	}

	/** Prueba de certificados gen&eacute;ricos.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	@Ignore
	public void testGen() throws Exception {
		final X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
			TestCertValidation.class.getResourceAsStream("/CERT_ATOS_TEST.cer") //$NON-NLS-1$
		);
		final ValidationResult vr = CertificateVerifierFactory.getCertificateVerifier(
			cert
		).validateCertificate();
		vr.check();
	}

}
