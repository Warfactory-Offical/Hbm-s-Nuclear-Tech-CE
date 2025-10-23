package com.llib.technical;

import com.llib.math.range.RangeInt;

public class BitByteBuf {
	public byte[] bytes;
	protected BitByteBuf() {
		this.bytes = new byte[1];
	}
	protected void allocateBits(int bits) {
		int expected = (int)Math.ceil(bits/8d);
		if (bytes.length < expected) {
			byte[] newBytes = new byte[expected];
			System.arraycopy(bytes,0,newBytes,expected-bytes.length,bytes.length);
			bytes = newBytes;
		}
	}
	public int extractBits(int start,int end) {
		//System.out.println("Extracting");
		int outValue = 0;
		RangeInt indexRange = new RangeInt(Math.floorDiv(start,8),Math.floorDiv(end,8));
		int startOffset = Math.floorMod(start,8);
		int endOffset = Math.floorMod(end,8);
		int filter = (int)(((long)1<<(endOffset+1))-1);
		for (Integer bytepos : indexRange) {
			int extract = bytepos < this.bytes.length ? bytes[bytes.length-1-bytepos]&0xFF : 0;
			if (bytepos >= this.bytes.length) {
				System.out.println("SERIOUS WARNING: Attempt to extract byte "+bytepos+", outside range 0 ~ "+(bytes.length-1)+"!");
			}
			if (bytepos == indexRange.max)
				extract = extract&filter;
			//System.out.println("FILTER: "+Integer.toBinaryString(filter));
			outValue = outValue | (int)((long)extract << (bytepos-indexRange.min)*8 >>> startOffset);
		}
		return outValue;
	}
	protected int insertBits(int start,int value,int length) {
		//System.out.println("Inserting");
		allocateBits(start+length);
		int end = start+length-1;
		RangeInt range = new RangeInt(Math.floorDiv(start,8),Math.floorDiv(end,8));
		int startOffset = Math.floorMod(start,8);
		for (Integer bytepos : range)
			bytes[bytes.length-1-bytepos] = (byte)(bytes[bytes.length-1-bytepos] | (value&0xFFFFFFFFL) << startOffset >>> (bytepos-range.min)*8);
		//System.out.println("NEW BYTE: "+Byte.toUnsignedInt(bytes[bytes.length-1-bytepos]));
		return start+length;
	}
}
