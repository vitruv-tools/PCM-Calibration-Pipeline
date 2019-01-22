package tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

public class ReadBinaryTest {

	public static void main(String[] args) throws ClassNotFoundException, IOException {
		File file = new File("test.edp2bin");

		byte[] bytes = FileUtils.readFileToByteArray(file);
		System.out.println(Arrays.toString(deserialise(bytes)));

	}

	public static Double[] deserialise(byte[] bytes) {
		Double[] doubles = new Double[(int) (bytes.length / getElementLength())];
		int blockPos = 0;
		for (int j = 0; j < doubles.length; j++) {
			long l = 0;
			for (int i = 7; i >= 0; i--) {
				l = l << 8;
				l |= bytes[blockPos + i] < 0 ? 256 + bytes[blockPos + i] : bytes[blockPos + i];
			}
			blockPos += 8;
			doubles[j] = Double.longBitsToDouble(l);
		}
		return doubles;
	}

	public static Long[] deserialisel(byte[] bytes) {
		Long[] longs = new Long[(int) (bytes.length / getElementLength())];
		int blockPos = 0;
		for (int j = 0; j < longs.length; j++) {
			long l = 0;
			for (int i = 7; i >= 0; i--) {
				l = l << 8;
				l |= bytes[blockPos + i] < 0 ? 256 + bytes[blockPos + i] : bytes[blockPos + i];
			}
			blockPos += 8;
			longs[j] = l;
		}
		return longs;
	}

	public static long getElementLength() {
		return 8;
	}

}
