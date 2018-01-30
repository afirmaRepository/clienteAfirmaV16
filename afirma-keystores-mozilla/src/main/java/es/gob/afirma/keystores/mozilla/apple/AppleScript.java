package es.gob.afirma.keystores.mozilla.apple;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase para la ejecucion de c&oacute;digo AppleScript, indicado directamente o escrito
 * previamente en un fichero.
 */
public class AppleScript {

	private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	private final String script;

	private final File scriptFile;

	private boolean deleteFile = false;

	/**
	 * Construye un AppleScript para la ejecuci&oacute;n de c&oacute;digo.
	 * @param script C&oacute;digo AppleScript que se desea ejecutar.
	 */
	public AppleScript(String script) {

		if (script == null) {
			throw new IllegalArgumentException("El script de entrada no puede ser nulo"); //$NON-NLS-1$
		}

		this.script = script;
		this.scriptFile = null;
	}

	/**
	 * Construye un AppleScript para la ejecuci&oacute;n de un fichero.
	 * @param scriptFile Fichero con el c&oacute;digo AppleScript que se desea ejecutar.
	 * @param deleteFile Indica si se debe eliminar el fichero de script tras su ejecuci&oacute;n.
	 */
	public AppleScript(File scriptFile, boolean deleteFile) {

		if (scriptFile == null) {
			throw new IllegalArgumentException("El fichero script de entrada no puede ser nulo"); //$NON-NLS-1$
		}

		this.script = null;
		this.scriptFile = scriptFile;
		//this.deleteFile = deleteFile;
		this.deleteFile = false;
	}

	/**
	 * Ejecuta el script creado.
	 * @return Salida del script.
	 * @throws IOException Cuando se produce un error durante la ejecuci&oacute;n.
	 * @throws InterruptedException Cuando el proceso se ve interrumpido.
	 */
	public String run() throws IOException, InterruptedException {
		return run(false);
	}

	/**
	 * Ejecuta con permisos de administrador el script creado. Si es necesario, pide la
	 * contrase&ntilde;a al usuario.
	 * @return Salida del script.
	 * @throws IOException Cuando se produce un error durante la ejecuci&oacute;n.
	 * @throws InterruptedException Cuando el proceso se ve interrumpido.
	 */
	public String runAsAdministrator() throws IOException, InterruptedException {
		return run(true);
	}

	private String run(boolean asAdmin) throws IOException, InterruptedException {

		final List<String> scriptCommands = new ArrayList<>();
		scriptCommands.add("/usr/bin/osascript"); //$NON-NLS-1$
		scriptCommands.add("-e"); //$NON-NLS-1$

		// Componemos el codigo para su ejecucion
		String scriptString;
		if (this.scriptFile != null) {
			//String sentence = "do shell script \"" + this.scriptFile.getAbsolutePath() + "\" args 2>&1 etc";
			scriptString = this.scriptFile.getAbsolutePath();
		}
		else {
			scriptString = this.script;
		}

		// Preparamos el script para la ejecucion
		String sentence = "do shell script \"" + scriptString + "\""; //$NON-NLS-1$ //$NON-NLS-2$
		if (asAdmin) {
			sentence += " with administrator privileges"; //$NON-NLS-1$
		}
		scriptCommands.add(sentence);

		final ProcessBuilder pbuilder = new ProcessBuilder(scriptCommands);
		final Process process = pbuilder.start();
		final int exitValue = process.waitFor();

		if (exitValue != 0) {
			throw new IOException("La ejecucion del script devolvio el codigo de finalizacion: " + exitValue); //$NON-NLS-1$
		}

		// Se lee la salida
		byte[] result;
		try (final InputStream is = process.getInputStream();) {
			result = readInputStream(is);
		}

		// Se elimina el fichero de script si asi se indica. Solo puede ser true cuando codigo en fichero
		if (this.deleteFile) {
			Files.delete(this.scriptFile.toPath());
		}

		return new String(result, DEFAULT_CHARSET);
	}

	/**
	 * Lee todo el contenido de un InputStream.
	 * @param is Flujo de datos de entrada del que leer.
	 * @return Contenido le&iacute;do del flujo.
	 * @throws IOException Cuando ocurre un error durante la lectura.
	 */
	private static byte[] readInputStream(InputStream is) throws IOException {

		int n = 0;
		final byte[] buffer = new byte[1024];
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while ((n = is.read(buffer)) > 0) {
			baos.write(buffer, 0, n);
		}
		return baos.toByteArray();
	}
}
