package tools.vitruv.applications.pcmjava.modelrefinement.parameters.util;

import java.util.stream.Stream;

public class UtilTest {

	public static void main(String[] args) {
		Stream.of(PcmUtils.class.getMethods()).forEach(e -> System.out.println(e.getName()));

	}

}
