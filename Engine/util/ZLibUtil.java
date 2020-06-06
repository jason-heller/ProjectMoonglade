package util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import dev.Console;

public class ZLibUtil {

	public static byte[] compress(byte[] data) throws IOException {
		Deflater deflater = new Deflater();
		deflater.setInput(data);
		ByteArrayOutputStream out = new ByteArrayOutputStream(data.length);
		deflater.finish();
		
		byte[] buffer = new byte[1024];
		while (!deflater.finished()) {
			int count = deflater.deflate(buffer);
			out.write(buffer, 0, count);
		}

		out.close();
		return out.toByteArray();
	}
	
	public static byte[] decompress(byte[] data) throws IOException, DataFormatException {
		
		Inflater inflater = new Inflater();
		inflater.setInput(data);
		ByteArrayOutputStream out = new ByteArrayOutputStream(data.length);
		byte[] buffer = new byte[1024];
		while (!inflater.finished()) {
			int count = inflater.inflate(buffer);
			out.write(buffer, 0, count);
			if (count == 0) break;
		}

		out.close();
		return out.toByteArray();
	}
}