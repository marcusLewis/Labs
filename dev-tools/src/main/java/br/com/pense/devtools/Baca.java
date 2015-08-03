package br.com.pense.devtools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class Baca {

	public Charset detectCharset(File f, String[] charsets) {

		Charset charset = null;

		for (String charsetName : charsets) {
			charset = detectCharset(f, Charset.forName(charsetName));
			if (charset != null) {
				break;
			}
		}

		return charset;
	}

	private Charset detectCharset(File f, Charset charset) {
		try {
			BufferedInputStream input = new BufferedInputStream(
					new FileInputStream(f));

			CharsetDecoder decoder = charset.newDecoder();
			decoder.reset();

			byte[] buffer = new byte[512];
			boolean identified = false;
			while ((input.read(buffer) != -1) && (!identified)) {
				identified = identify(buffer, decoder);
			}

			input.close();

			if (identified) {
				return charset;
			} else {
				return null;
			}

		} catch (Exception e) {
			return null;
		}
	}

	private boolean identify(byte[] bytes, CharsetDecoder decoder) {
		try {
			decoder.decode(ByteBuffer.wrap(bytes));
		} catch (CharacterCodingException e) {
			return false;
		}
		return true;
	}

	public static void main(String[] args) {
		File f = new File("C:\\Desenvolvimento\\Git\\pense-imoveis\\produtos\\imoveis\\pense-imoveis\\src\\main\\webapp\\resources\\v4\\js\\detalhe.js");

		String[] charsetsToBeTested = { "ISO-8859-1", "UTF-8" };

		Baca cd = new Baca();
		Charset charset = cd.detectCharset(f, charsetsToBeTested);

		if (charset != null) {
			/*try {
				InputStreamReader reader = new InputStreamReader(
						new FileInputStream(f), charset);
				int c = 0;
				while ((c = reader.read()) != -1) {
					System.out.print((char) c);
				}
				reader.close();
			} catch (FileNotFoundException fnfe) {
				fnfe.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
*/
			System.out.println(charset);
		} else {
			System.out.println("Unrecognized charset.");
		}
	}

	/*
	 * public static void main(String[] args) throws Exception {
	 * 
	 * //String filename =
	 * "C:\\Desenvolvimento\\Git\\pense-imoveis\\produtos\\imoveis\\pense-imoveis\\src\\main\\webapp\\resources\\v4\\js\\detalhe.js"
	 * ; String filename =
	 * "C:\\Desenvolvimento\\Git\\pense-imoveis\\produtos\\imoveis\\pense-imoveis\\src\\main\\java\\br\\com\\rbs\\pense\\imoveis\\repository\\IGenericRepository.java"
	 * ; File in = new File(filename); InputStreamReader r = new
	 * InputStreamReader(new FileInputStream(in)); System.out.println(
	 * in.exists() + " / " + r.getEncoding());
	 * 
	 * }
	 */

}
