package tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Triple;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.data.ServiceCallBundle;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.data.usage.AbstractUsageElement;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.data.usage.UsageCallStructure;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.data.usage.UsageLoopStructure;

// TODO better structuring
public class UsageSession {
	private static final String CALLER_NOT_SET = "<not set>";

	private List<ServiceCall> calls;
	private long entryTime;

	public UsageSession(long entry) {
		calls = new ArrayList<>();
		entryTime = entry;
	}

	public void update(ServiceCall call) {
		if (call.getCallerServiceExecutionId().equals(CALLER_NOT_SET)) {
			// => ENTRY CALL
			calls.add(call);
		}
	}

	public long getEntryTime() {
		return entryTime;
	}

	public List<AbstractUsageElement> compress() {
		// sort before
		sortCalls();

		// bundle calls
		List<ServiceCallBundle> bundles = bundleCalls();

		// find structure
		return buildUsage(bundles, findRepeatingBundles(bundles));
	}

	private List<AbstractUsageElement> buildUsage(List<ServiceCallBundle> bundles,
			List<Triple<Integer, Integer, Integer>> patterns) {
		final List<AbstractUsageElement> els = new ArrayList<>();

		// build whole structure
		for (ServiceCallBundle bundle : bundles) {
			if (bundle.isLoop()) {
				UsageLoopStructure loop = new UsageLoopStructure(bundle.getCallCount());
				loop.addChild(new UsageCallStructure(bundle.getServiceCall()));
				els.add(loop);
			} else {
				els.add(new UsageCallStructure(bundle.getServiceCall()));
			}
		}

		// merge structures
		final List<Integer> removes = new ArrayList<>();
		for (Triple<Integer, Integer, Integer> pattern : patterns) {
			// merge elements
			int patternSize = pattern.getMiddle() - pattern.getLeft() + 1;
			// [a, b, a, b, a, b] => pattern start is where the first repetition starts ( at
			// second a ) and the end is at the end of the pattern
			final int refPatternStart = pattern.getLeft();
			final int patternStart = pattern.getLeft() + patternSize;
			final int patternEnd = patternStart + pattern.getRight() * patternSize;

			for (int cursor = patternStart; cursor < patternEnd; cursor += patternSize) {
				final int cursorCopy = cursor;

				IntStream.range(cursor, cursor + patternSize).forEach(k -> {
					int offset = k - cursorCopy;
					els.get(refPatternStart + offset).merge(els.get(k));
					removes.add(k);
				});
			}

			// we need to build a loop out of these elements
			// TODO problem is that the loop moves the indices
			UsageLoopStructure newLoop = new UsageLoopStructure(pattern.getRight());
			for (int z = refPatternStart; z < refPatternStart + patternSize; z++) {
				newLoop.addChild(els.get(z));

				if (z != refPatternStart) {
					removes.add(z);
				}
			}
			els.set(refPatternStart, newLoop); // replace here this doesnt changes the indices and that is imp
		}

		// we need to reverse the list so we dont need to modify the indices
		Collections.sort(removes, Collections.reverseOrder());
		for (int k : removes) {
			els.remove(k);
		}

		return els;
	}

	/**
	 * Finds repeating structures in the service calls. Looks very complex but i
	 * think its O(n^2) -> could be worse :)
	 * 
	 * @param bundles
	 *            bundled service calls (sorted)
	 * @return triple of integers (start, end, repetitions) of the certain structure
	 */
	private List<Triple<Integer, Integer, Integer>> findRepeatingBundles(List<ServiceCallBundle> bundles) {
		List<Triple<Integer, Integer, Integer>> ret = new ArrayList<>();

		for (int k = 0; k < bundles.size(); k++) {
			// bundles should be an arraylist otherwise this is very inefficient
			ServiceCallBundle bundle = bundles.get(k);

			int nextOcc = -1;
			for (int j = k + 1; j < bundles.size(); j++) {
				if (bundles.get(j).canBeBundled(bundle)) {
					// this is the next occurrence
					nextOcc = j;
					break;
				}
			}

			// counts following occurences
			int upcomingOccurences = 0;
			if (nextOcc >= 0) {
				int patternLength = nextOcc - k;
				for (int z = k + patternLength; z < bundles.size() - (patternLength - 1); z += patternLength) {
					final int kCopy = k;
					final int zCopy = z;

					boolean patternMatching = IntStream.range(z, z + patternLength).allMatch(index -> {
						return bundles.get(index).canBeBundled(bundles.get(kCopy + (index - zCopy)));
					});

					if (patternMatching) {
						upcomingOccurences++;
					} else {
						break;
					}
				}

				if (upcomingOccurences > 0) {
					ret.add(Triple.of(k, nextOcc - 1, upcomingOccurences));

					k += (upcomingOccurences + 1) * patternLength;
					k--; // because ++ comes afterwards
				}
			}
		}

		return ret;
	}

	private List<ServiceCallBundle> bundleCalls() {
		List<ServiceCallBundle> bundles = new ArrayList<>();

		ServiceCallBundle currentBundle = null;
		for (ServiceCall call : calls) {
			if (currentBundle == null) {
				currentBundle = new ServiceCallBundle(call);
			} else {
				if (currentBundle.canBeBundled(call)) {
					currentBundle.bundle(call);
				} else {
					bundles.add(currentBundle);
					currentBundle = new ServiceCallBundle(call);
				}
			}
		}

		if (currentBundle != null) {
			bundles.add(currentBundle);
		}

		return bundles;
	}

	private void sortCalls() {
		calls.sort((a, b) -> {
			if (a.getEntryTime() - b.getEntryTime() > 0) {
				return 1;
			} else if (a.getEntryTime() < b.getEntryTime()) {
				return -1;
			} else {
				return 0;
			}
		});
	}

}
