/*
 * Camundo <http://www.camundo..com> Copyright (C) 2011  Wouter Van der Beken.
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
package com.camundo.media;

public class AudioCodec { 
	
	
	public static class ADPCM_SWF {
		
		public static final String name = "adpcm_swf";
		
		public static final int RATE_11025 = 11025;
		public static final int RATE_22050 = 22050;
		public static final int RATE_44100 = 44100;
		
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

}
