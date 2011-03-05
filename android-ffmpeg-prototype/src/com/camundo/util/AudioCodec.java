/*
 * Camundo <http://www.camundo.com> Copyright (C) 2011  Wouter Van der Beken.
 *
 * This file is part of Camundo.
 *
 * Camundo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Camundo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Camundo.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.camundo.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


import android.util.Log;

public class AudioCodec { 
	
	public static final String TAG = "AudioCodec";
	
	
	 static final int WAVE_FORMAT_PCM = 0x0001;
	 static final int WAVE_FORMAT_IEEE_FLOAT = 0x0003 ;
	 static final int WAVE_FORMAT_EXTENSIBLE = 0xFFFE;

	 static final String title = "RiffRead32 " ;
	 static final String[] infotype = {"IARL", "IART", "ICMS", "ICMT", "ICOP", "ICRD", "ICRP", "IDIM",
		"IDPI", "IENG", "IGNR", "IKEY", "ILGT", "IMED", "INAM", "IPLT", "IPRD", "ISBJ",
		"ISFT", "ISHP", "ISRC", "ISRF", "ITCH", "ISMP", "IDIT" } ;

	 static final String[] infodesc = {"Archival location", "Artist", "Commissioned", "Comments", "Copyright", 
		"Creation date","Cropped", "Dimensions", "Dots per inch", "Engineer", "Genre", "Keywords", 
		"Lightness settings", "Medium", "Name of subject", "Palette settings", "Product", "Description",
		"Software package", "Sharpness", "Source", "Source form", "Digitizing technician", 
		"SMPTE time code", "Digitization time"};

	
	
	public static final int AUDIO_FILE_FORMAT_WAV = 1;
	

	public static class ADPCM_SWF {
		
		public static final String name = "adpcm_swf";
		
		public static final int RATE_11025 = 11025;
		public static final int RATE_22050 = 22050;
		public static final int RATE_44100 = 44100;
		
	}
	
	
	public static class Nellymoser {
		
		public static final String name = "nellymoser";
		
	}
	
	public static class AAC {
		
		public static final String name = "aac";
		
	}
	
	
	public static class AMRNB {
		
		public static final String name = "amrnb";
		
		public static final int RATE_8000 = 8000;
		
		//[libopencore_amrnb @ 0x1418930] bitrate not supported: use one of 4.75k, 5.15k, 5.9k, 6.7k, 7.4k, 7.95k, 10.2k or 12.2k
		public static final String BITRATE_4_75k = "4.75k";
		public static final String BITRATE_5_15k = "5.15k";
		public static final String BITRATE_5_9k = "5.9k";
		public static final String BITRATE_6_7k = "6.7k";
		public static final String BITRATE_7_4k = "7.4k";
		public static final String BITRATE_7_95k = "7.95k";
		public static final String BITRATE_10_2k = "10.2k";
		public static final String BITRATE_12_2k = "12.2k";
		
	}
	
	
	public static class WaveFile {
		
		public static final int HEADER_SIZE = 44;
		
		public static WavInfo readHeader(InputStream wavStream)	throws IOException {
		 
		    ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
		    buffer.order(ByteOrder.LITTLE_ENDIAN);
		 
		    wavStream.read(buffer.array(), buffer.arrayOffset(), buffer.capacity());
		 
		    buffer.rewind();
		    buffer.position(buffer.position() + 20);
		    int format = buffer.getShort();
		    checkFormat(format == 1, "Unsupported encoding: " + format); // 1 means Linear PCM
		    int channels = buffer.getShort();
		    checkFormat(channels == 1 || channels == 2, "Unsupported channels: " + channels);
		    int rate = buffer.getInt();
		    checkFormat(rate <= 48000 && rate >= 11025, "Unsupported rate: " + rate);
		    buffer.position(buffer.position() + 6);
		    int bits = buffer.getShort();
		    checkFormat(bits == 16, "Unsupported bits: " + bits);
		    int dataSize = 0;
		    
		    /*
		    while (buffer.getInt() != 0x61746164) { // "data" marker
		      Log.d( TAG , "Skipping non-data chunk");
		      int size = buffer.getInt();
		      wavStream.skip(size);
		      buffer.rewind();
		      wavStream.read(buffer.array(), buffer.arrayOffset(), 8);
		      buffer.rewind();
		    }
		    dataSize = buffer.getInt();
		    checkFormat(dataSize > 0, "wrong datasize: " + dataSize);*/
		 
		    return new WavInfo( rate, channels, dataSize);
		}
		
		private static void checkFormat( boolean b , String s) throws IOException {
			if (!b ) {
				throw new IOException(s);
			}
		}
		
		
		
	}
	
	
	public static class PCM_U8 {
		public static final String name = "pcm_u8";
		public static final int RATE_11025 = 11025;
	}

	public static class PCM_S16LE {
		public static final String name = "pcm_s16le";
		public static final int RATE_8000 = 8000;
		public static final int RATE_11025 = 11025;
		public static final int RATE_44100 = 44100;
		
	}
	
}
