import java.io.ByteArrayOutputStream;
import java.util.HashMap;

/**
 *
 * @author steven wooding
 */
public class Base64
{
	/**
	 * RFC 4648'ish, URL and Filename Safe version
	 * Just modify the alphabet below to your needs
	 */
	public static final String base64chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
	public static final int[] masks = new int[]	{ 0, 1, 3, 7, 15, 31, 63, 127, 255 };
	public static final HashMap<Byte, Byte> base64reverse = new HashMap<Byte, Byte>();

	static
	{
		int length = base64chars.length();
		for(byte i=0;i<length;i++) base64reverse.put((byte)base64chars.charAt(i), i);
		base64reverse.put((byte)'+', (byte)62); // Enables this parser to also parse Base64 strings from RFC 1421, RFC 2045, RFC 3548, RFC 4880, RFC 1642
		base64reverse.put((byte)'/', (byte)63); // Enables this parser to also parse Base64 strings from RFC 1421, RFC 2045, RFC 3548, RFC 4880, RFC 1642 
		
		base64reverse.put((byte)'.', (byte)62); // Enables this parser to also parse Base64 strings from YUI Library. Y64
	}
	
	/**
	 * Not really the standard way to do it. But I couldn't find an existing lib
	 * that suits my fancy. Throws a lot of bits around and stuff, but it seems 
	 * to produce the correct results compared to other base64 systems. Just
	 * throw in some very raw binary data and catch the ASCII friendly string that
	 * falls out of this method. Short, sweet and simple!!
	 * 
	 * Created : 2015-02-22 - Steven Wooding
	 * 
	 * @param input Raw stuff
	 * @return ASCII friendly base64 string
	 */
	public static String encode(byte[] input)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int bitbuf = 0;
		int shifter = 2;
		for(int i : input)
		{
			if(i<0) i += 256; // Fixes negative values.
			baos.write(base64chars.charAt(bitbuf << (8 - shifter) | (i >> shifter)));
			bitbuf = i & masks[shifter];
			shifter += 2;
			if(shifter==8)
			{
				baos.write(base64chars.charAt(bitbuf));
				shifter = 2;
				bitbuf = 0;
			}
		}
		if(shifter>2) baos.write(base64chars.charAt(bitbuf << (8 - shifter)));
		
		return new String(baos.toByteArray());
	}
	
	/**
	 * Actually shorter than the encoder!! :O Here is the base64
	 * decoder, nice short, sweet and simple! Throw in a base64 encoded string
	 * and it will spit out the raw bits.
	 * 
	 * @param input ASCII friendly base64 String
	 * @return Very very raw ones and zeros!
	 */
	public static byte[] decode(String input)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] bytes = input.getBytes();
		int bitbuf = 0;
		int shifter = 2;
		int decoded = 0;
		for(int i : bytes)
		{
			Byte chr = base64reverse.get((byte)i);
			if(chr!=null)
			{
				decoded = chr;
				if(shifter==2) bitbuf = decoded << shifter;
				else
				{
					baos.write((decoded >> (8 - shifter)) | bitbuf);
					bitbuf = (decoded & masks[8 - shifter]) << shifter;
				}
				shifter = (shifter == 8 ? 2 : shifter + 2);
			}
		}
		return baos.toByteArray();
	}
}
