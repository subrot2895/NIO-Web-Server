
/**
 * Various utility methods for working with CharSequences.
 *
 */
public class CharSequences {

	/** 
	 * Prevents instantiation. 
	 */
	private CharSequences () {}

	/**
	 * Returns the index of a character within a CharSequence.
	 *
	 * @param cs The CharSequence
	 * @param c The character to look for.
	 * @return The index of the character, or -1 if the character was
	 * not found
	 */
	public static int indexOf (CharSequence cs, char c) {
		return indexOf(cs, c, 0);
	}

	/**
	 * Returns the index of a character within a CharSequence, starting
	 * from a given index.
	 *
	 * @param cs The CharSequence
	 * @param c The character to look for.
	 * @param fromIndex The index at which to start searching
	 * @return The index of the character, or -1 if the character was
	 * not found
	 */
	public static int indexOf (CharSequence cs, char c, int fromIndex) {
		int fst = (fromIndex < 0) ? 0 : fromIndex;
		int len = cs.length();
		for (int i = fst; i < len; i++) 
			if (cs.charAt(i) == c) 
				return i;
		return -1;
	}

	public static boolean regionMatches (CharSequence cs1, int off1, 
										CharSequence cs2, int off2, int len) {
		if (off1 < 0 || off2 < 0 || off1 + len > cs1.length() 
			|| off2 + len > cs2.length()) {
			return false;
		} else {
			for (int i = 0; i < len; i++) 
				if (cs1.charAt(i+off1) != cs2.charAt(i+off2)) 
					return false;
			return true;
		}
	}

	public static boolean startsWith (CharSequence cs, CharSequence start) {
		return regionMatches(cs, 0, start, 0, start.length());
	}

	public static boolean endsWith (CharSequence cs, CharSequence end) {
		int el = end.length();
		return regionMatches(cs, cs.length()-el, end, 0, el);
	}

	public static boolean equals (CharSequence cs1, CharSequence cs2) {
		int l1 = cs1.length();
		int l2 = cs2.length();
		return l1 == l2 && regionMatches(cs1, 0, cs2, 0, l1);
	}

}

