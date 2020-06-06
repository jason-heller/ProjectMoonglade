package util;

import java.io.ByteArrayOutputStream;

public class RunLengthOutputStream extends ByteArrayOutputStream {

	private int lastByte = Integer.MIN_VALUE;
	private int runLength = 0;
	
	@Override
	public void write(int b) {
		if (b == lastByte) {
			runLength++;
			
			if(runLength == 256) {
				super.write(lastByte);
				super.write(runLength);
				runLength = 1;
			}
		} else {
			if (runLength != 0) {
				super.write(lastByte);
				super.write(runLength);
				runLength = 1;
			}
			
			lastByte = b;
		}
	}
}
