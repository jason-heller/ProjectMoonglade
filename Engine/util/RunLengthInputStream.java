package util;

public class RunLengthInputStream {

	private int lastByte = Integer.MIN_VALUE;
	private int runLength = 0, runIndex = 0;
	
	private byte[] data;
	
	private int index = 0;
	
	public RunLengthInputStream(byte[] data) {
		this.data = data;
	}
	
	private int read() {
		return data[index++] & 0xff;
		/*if (lastByte == Integer.MIN_VALUE || runIndex == runLength) {
			lastByte = data[index++] & 0xff;
			runLength = data[index++] & 0xff;
			runIndex = 1;
		} else if (runIndex != runLength) {
			runIndex++;
		}
		
		return lastByte;*/
	}
	
	public byte readByte() {
		return (byte)(read() & 0xff);
	}
	
	public float readFloat() {
		return Float.intBitsToFloat(readInt());
	}
	
	public int readInt() {
		int i = ((read() & 0xff) << 24)
		| ((read() & 0xff) << 16)
		| ((read() & 0xff) << 8)
		| ((read() & 0xff));
		return i;
	}
	
	public int readShort() {
		int i = ((read() & 0xff) << 8)
		| ((read() & 0xff));
		return i;
	}
	
	public long readLong() {
		long i = ((read() & 0xff) << 56)
		| ((read() & 0xff) << 48)
		| ((read() & 0xff) << 40)
		| ((read() & 0xff) << 32)
		| ((read() & 0xff) << 24)
		| ((read() & 0xff) << 16)
		| ((read() & 0xff) << 8)
		| ((read() & 0xff));
		return i;
	}
}
