package util;

import java.io.ByteArrayOutputStream;

public class RunLengthOutputStream extends ByteArrayOutputStream {

	private int lastByte = Integer.MIN_VALUE;
	private int runLength = 0;
	private boolean closed = false;
	
	/*@Override
	public void write(int b) {
		if (b == lastByte) {
			runLength++;
			
			if(runLength == 255) {
				super.write(lastByte & 0xff);
				super.write(runLength & 0xff);
				runLength = 0;
			}
		} else {
			if (runLength != 0) {
				super.write(lastByte & 0xff);
				super.write(runLength & 0xff);
			}
			
			lastByte = b;
			runLength = 1;
		}
	}*/
	
	public void writeLong(long i) {
		write((byte) (0xff & (i >> 56)));
		write((byte) (0xff & (i >> 48)));
		write((byte) (0xff & (i >> 40)));
		write((byte) (0xff & (i >> 32)));
		write((byte) (0xff & (i >> 24)));
		write((byte) (0xff & (i >> 16)));
		write((byte) (0xff & (i >> 8)));
		write((byte) (0xff & (i)));
	}
	
	public void writeInt(int i) {
		write((byte) (0xff & (i >> 24)));
		write((byte) (0xff & (i >> 16)));
		write((byte) (0xff & (i >> 8)));
		write((byte) (0xff & (i)));
	}
	
	public void writeFloat(float f) {
		writeInt(Float.floatToIntBits(f));
	}
	
	public void writeByte(int b) {
		write(b & 0xff);
	}
	
	public void writeShort(int s) {
		write(((s >> 8) & 0xff));
		write((s & 0xff));
	}

	public void close() {
		if (!closed) {
			//super.write(lastByte & 0xff);
			//super.write((runLength) & 0xff);
			closed = true;
		}
	}
	
	@Override
	public byte[] toByteArray() {
		if (!closed) {
			close();
		}
		
		return super.toByteArray();
	}
}
