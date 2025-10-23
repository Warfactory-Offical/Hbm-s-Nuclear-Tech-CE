package com.llib.technical;
import com.llib.exceptions.LeafiaDevFlaw;
import com.llib.math.range.RangeInt;

import static com.llib.technical.FifthString.CharType.*;
import static com.llib.technical.FifthString.ControlType.*;

public class FifthString {
	public int[] codes = new int[0];
	public String[] utfs = new String[0];
	public FifthString(String str) {
		if (str == null) return;
		char[] chars = str.toCharArray();
		int skipUntil = 0;
		RangeInt range = new RangeInt(0,chars.length-1);
		boolean capital = false;
		for (Integer i : range) {
			if (i < skipUntil) continue;
			char chr = chars[i];
			CharType type = charType(chr);
			if (type.isAlphabet && type.isCapital != capital) {
				if (range.isInRange(i+1) && charType(chars[i+1]).equals(type)) {
					capital = !capital;
					append(CAPITAL_TOGGLE);
				} else
					append(CAPITAL_ONCE);
				append(chr);
			} else if (type.equals(UNICODE)) {
				String utf = "";
				for (skipUntil = i; skipUntil < chars.length; skipUntil++) {
					if (skipUntil-i > 65535) break; // ru crazy??
					if (charType(chars[skipUntil]).equals(UNICODE))
						utf = utf + chars[skipUntil];
					else
						break;
				}
				append(SPECIAL);
				append(utf);
			} else
				append(chr);
		}
		append(END);
		//System.out.println("Data size: "+original+" bits => "+mysize+" bits ("+String.format("%+d bits, %d%% compressed",mysize-original,(int)(100-mysize/(double)original*100))+")");
	}
	@Override
	public String toString() {
		int utfIndex = 0;
		boolean capital = false;
		boolean nextCap = false;
		String str = "";
		for (int code : codes) {
			if (code == ControlType.END.code)
				break;
			else if (code == ControlType.CAPITAL_TOGGLE.code)
				capital = !capital;
			else if (code == ControlType.CAPITAL_ONCE.code)
				nextCap = !nextCap;
			else if (code == ControlType.SPECIAL.code) {
				if (true) { // for future possiblities where you would want to put a branch here
					str = str + utfs[utfIndex];
					utfIndex++;
				}
			} else if (code == 26)
				str = str + " ";
			else if (code == 27)
				str = str + "_";
			else {
				boolean cap = capital;
				if (nextCap) {
					cap = !cap;
					nextCap = false;
				}
				str = str + (char)((cap ? 65 : 97)+code);
			}
		}
		return str;
	}
	public void append(int code) {
		//System.out.println("Appending "+String.format("%2d (%05d)",code,Integer.parseInt(Integer.toBinaryString(code))));
		int[] buffer = new int[codes.length+1];
		System.arraycopy(codes,0,buffer,0,codes.length);
		codes = buffer;
		codes[codes.length-1] = code;
	}
	public void append(String utf) {
		String[] buffer = new String[utfs.length+1];
		System.arraycopy(utfs,0,buffer,0,utfs.length);
		utfs = buffer;
		utfs[utfs.length-1] = utf;
	}
	void append(char chr) {
		CharType type = charType(chr);
		switch(type) {
			case UPPER: append(chr-65); break;
			case LOWER: append(chr-97); break;
			case SPACE: append(26); break;
			case UNDERSCORE: append(27); break;
			default: throw new LeafiaDevFlaw("Unsupported character "+Integer.toHexString(chr)+" - "+chr);
		}
	}
	void append(ControlType control) { append(control.code); }
	enum CharType {
		LOWER(false),UPPER(true),SPACE,UNDERSCORE,UNICODE;
		final boolean isAlphabet;
		final boolean isCapital;
		CharType() { isAlphabet = false; isCapital = false; }
		CharType(boolean capital) { isAlphabet = true; isCapital = capital; }
	}
	CharType charType(char chr) {
		String s = String.valueOf(chr);
		if (chr > 0x7F) return UNICODE;
		else if (s.matches("[a-z]")) return LOWER;
		else if (s.matches("[A-Z]")) return UPPER;
		else if (s.equals(" ")) return SPACE;
		else if (s.equals("_")) return UNDERSCORE;
		return UNICODE;
	}
	public enum ControlType {
		CAPITAL_TOGGLE(28),
		CAPITAL_ONCE(29),
		END(30),
		SPECIAL(31), // currently only UTF
		;
		public final int code;
		ControlType(int code) { this.code = code; }
	}
}
