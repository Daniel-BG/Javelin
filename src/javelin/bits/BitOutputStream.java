package javelin.bits;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Wraps around an output stream providing bit-wise functionality
 * @author Daniel
 * @see BitInputStream
 */
public class BitOutputStream extends OutputStream {

	@Override
	/**
	 * Flush fully formed bytes to the output. 
	 * @throws IOException
	 */
	public void flush() throws IOException {
		this.stream.flush();
	}
	
	/**
	 * Adds padding to the specified number of bytes
	 * @param byteAmount
	 * @throws IOException 
	 */
	public void addPadding(int byteAmount) throws IOException {
		if (byteAmount > 8) {
			throw new IllegalArgumentException("Can only pad to 8bytes max");
		}
		//first pad the last byte
		if (this.bufferSize > 0) {
			this.writeNBitNumber(0, 8 - this.bufferSize);
		}
		//now pad to byteAmount
		long bytesOutput = this.bitsOutput / 8;
		int missingBytes = (int) (byteAmount - (bytesOutput % byteAmount));
		if (missingBytes < byteAmount) {
			this.writeBits(0l, missingBytes*8, BitStreamConstants.ORDERING_LEFTMOST_FIRST);
		}
	}
	
	/**
	 * Adds padding to the remaining bits (if present) to make a multiple of a byte, 
	 * then flushes the underlying stream
	 * @throws IOException
	 */
	public void paddingFlush() throws IOException {
		//flush remaining bits padding with zeroes
		if (this.bufferSize > 0) {
			this.writeNBitNumber(0, 8 - this.bufferSize);
		}
		this.flush();
	}

	@Override
	public void close() throws IOException {
		this.stream.close();
	}

	@Override
	public void write(int b) throws IOException {
		this.writeByte((byte) b);
	}
	
	
	private OutputStream stream;
	private int buffer;
	private int bufferSize;
	private long bitsOutput;
	
	/**
	 * Create a bit output wrapper around the given outputstream
	 * @param stream
	 */
	public BitOutputStream(OutputStream stream) {
		this.stream = stream;
		this.buffer = 0;
		this.bufferSize = 0;
		this.bitsOutput = 0;
	}
	
	

	/**
	 * Adds a single bit to this stream. A zero is Bit.BIT_ZERO and a one is Bit.BIT_ONE
	 * @param bit
	 * @throws IOException 
	 */
	public void writeBit(int bit) throws IOException {
		if (bit != 0 && bit != 1) {
			throw new IllegalStateException("Whoops @FIFOBitStream.putBit");
		}
		bit = Bit.normalize(bit);
		
		this.buffer <<= 1;
		this.buffer |= bit;
		this.bufferSize ++;
		if (this.bufferSize == 8) {
			this.stream.write(this.buffer);
			this.buffer = 0;
			this.bufferSize = 0;
		}
		this.bitsOutput++;
	}


	/**
	 * Adds a Bit object to the stream
	 * @param bit
	 * @throws IOException 
	 */
	public void writeBit(Bit bit) throws IOException {
		this.writeBit(bit.toInteger());
	}


	/**
	 * @param bits
	 * @param quantity
	 * @param ordering
	 * @throws IOException 
	 */
	public void writeBits(long bits, int quantity, BitStreamConstants ordering) throws IOException {
		switch (ordering) {
		case ORDERING_LEFTMOST_FIRST:
			//adjust the bits so that the first one is in the leftmost position
			bits <<= Long.SIZE - quantity;
			for (int i = 0; i < quantity; i++) {
				writeBit(Bit.fromLong(bits & BitStreamConstants.LONG_LEFT_BIT_MASK));
				bits <<= 1;
			}
			break;
		case ORDERING_RIGHTMOST_FIRST:
			for (int i = 0; i < quantity; i++) {
				writeBit(Bit.fromLong(bits & BitStreamConstants.LONG_RIGHT_BIT_MASK));
				bits >>= 1;
			}
			break;
		}
	}
	
	
	/**
	 * writes the specified number of bits from the given binary int
	 * @param i
	 * @param bits
	 * @throws IOException 
	 */
	public void writeNBitNumber(int i, int bits) throws IOException {
		this.writeBits(i, bits, BitStreamConstants.ORDERING_LEFTMOST_FIRST);
	}
	
	/**
	 * @param i the byte to be written
	 * @throws IOException 
	 */
	public void writeByte(byte i) throws IOException {
		if (this.bufferSize == 0) {
			this.stream.write(i);
			this.bitsOutput += 8;
		} else {
			this.writeNBitNumber(i, Byte.SIZE);
		}
	}
	
	/**
	 * @param f float to be written into the inner BitStream
	 * @throws IOException 
	 */
	public void writeFloat(float f) throws IOException {
		this.writeInt(Float.floatToIntBits(f));
	}
	
	/**
	 * @param d float to be written into the inner BitStream
	 * @throws IOException 
	 */
	public void writeDouble(double d) throws IOException {
		long bits = Double.doubleToLongBits(d);
		int leftBits = (int) (bits >>> 32);
		int rightBits = (int) (bits & 0xffffffffl);
		this.writeInt(leftBits);
		this.writeInt(rightBits);
	}
	
	/**
	 * writes the given integer in the output stream,
	 * @param i
	 * @throws IOException 
	 */
	public void writeInt(int i) throws IOException {
		this.writeByte((byte) (i >> 24));
		this.writeByte((byte) (i >> 16));
		this.writeByte((byte) (i >> 8));
		this.writeByte((byte) i);
	}
	
	/**
	 * Writes a variable lenght POSITIVE int. will use from 1-5 bytes, depending
	 * on size. Use if you know you won't find big values, otherwise
	 * don't
	 * @param i
	 * @throws IOException
	 */
	public void writeVLPInt(int i) throws IOException {
		if (i < 0) {
			throw new IllegalArgumentException();
		}
		while (i >= 1 << 7) {
			this.writeByte((byte) (i & 0x7f));
			i >>>= 7;
		}
		this.writeByte((byte) ((i & 0x7f) | 0x80));
	}
	
	/**
	 * @param i an integer containing an unsigned short in the 16 less significant
	 * bits
	 * @throws IOException 
	 */
	public void writeShort(short i) throws IOException {
		this.writeByte((byte) (i >> 8));
		this.writeByte((byte) i);
	}
	
	/**
	 * @param c the character to be written
	 * @throws IOException 
	 */
	private void writeChar(char c) throws IOException {
		this.writeByte((byte) (c >> 8));
		this.writeByte((byte) c);
	}
	
	/**
	 * Writes a boolean to the inner stream, using 1 bit
	 * @param b
	 * @throws IOException 
	 */
	public void writeBoolean(boolean b) throws IOException {
		this.writeNBitNumber(b ? 1 : 0, 1);
	}
	
	/**
	 * Write the given number of elements from the given array
	 * @param array
	 * @param length
	 * @throws IOException 
	 */
	public void writeDoubleArray(double[] array, int length) throws IOException {
		for (int i = 0; i < length; i++) {
			this.writeDouble(array[i]);
		}
	}

	/**
	 * Write the given number of elements from the given array
	 * @param array
	 * @param length
	 * @throws IOException 
	 */
	public void writeFloatArray(float[] array, int length) throws IOException {
		for (int i = 0; i < length; i++) {
			this.writeFloat(array[i]);
		}
	}

	/**
	 * Writes the given array up to the given position
	 * @param array
	 * @param length
	 * @throws IOException 
	 */
	public void writeByteArray(byte[] array, int length) throws IOException {
		for (int i = 0; i < length; i++) {
			this.writeByte(array[i]);
		}
	}
	
	/**
	 * Write the given array up to the given position
	 * @param array
	 * @param length
	 * @throws IOException
	 */
	public void writeIntArray(int[] array, int length) throws IOException {
		for (int i = 0; i < length; i++) {
			this.writeInt(array[i]);
		}
	}
	
	/**
	 * Write the given array up to the given position
	 * @param array
	 * @param bits the bits to write from each integer
	 * @param length
	 * @throws IOException
	 */
	public void writeNBitNumberArray(int[] array, int bits, int length) throws IOException {
		for (int i = 0; i < length; i++) {
			this.writeNBitNumber(array[i], bits);
		}
	}
	
	/**
	 * Writes the given array up to the given position
	 * @param array
	 * @param length
	 * @throws IOException 
	 */
	public void writeCharArray(char[] array, int length) throws IOException {
		for (int i = 0; i < length; i++) {
			this.writeChar(array[i]);
		}
	}

	/**
	 * Writes the given string
	 * @param val
	 * @throws IOException 
	 */
	public void writeString(String val) throws IOException {
		byte[] chars = val.getBytes(StandardCharsets.US_ASCII);
		this.writeByteArray(chars, chars.length);
	}
	
	
	/**
	 * Writes the given enum (which has to be of the given type) 
	 * @param clazz
	 * @param val
	 * @param byteSize if <code>true</code>, the number of bits written will be a multiple
	 * of 8, if <code>false</code>, the least possible amount of bits will be used. <br>
	 * e.g: if the enum has two values, one bit will suffice to save them
	 * @throws IOException
	 */
	public void writeEnum(Class<?> clazz, Enum<?> val, boolean byteSize) throws IOException {
		if (!val.getClass().equals(clazz)) {
			throw new IllegalArgumentException("The given value is not of the given class");
		}
		if (clazz.getEnumConstants() == null) {
			throw new IllegalArgumentException("The given class is not an Enum");
		}
		
		int numberOfEnums = clazz.getEnumConstants().length;
		int bitSize = BitTwiddling.bitsOf(numberOfEnums);
		if (byteSize && bitSize % 8 != 0) {
			bitSize += (8 - (bitSize % 8));
		}
		int index = val.ordinal();
		this.writeNBitNumber(index, bitSize);;	
	}
	

	/**
	 * @return the number of bits output so far
	 */
	public long getBitsOutput() {
		return this.bitsOutput;
	}


	

}
