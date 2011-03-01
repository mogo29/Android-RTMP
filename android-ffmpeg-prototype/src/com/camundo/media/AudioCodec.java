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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Hashtable;

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
		    
		    while (buffer.getInt() != 0x61746164) { // "data" marker
		      Log.d( TAG , "Skipping non-data chunk");
		      int size = buffer.getInt();
		      wavStream.skip(size);
		      buffer.rewind();
		      wavStream.read(buffer.array(), buffer.arrayOffset(), 8);
		      buffer.rewind();
		    }
		    dataSize = buffer.getInt();
		    checkFormat(dataSize > 0, "wrong datasize: " + dataSize);
		 
		    return new WavInfo( rate, channels, dataSize);
		}
		
		private static void checkFormat( boolean b , String s) throws IOException {
			if (!b ) {
				throw new IOException(s);
			}
		}
		
		
		/*
		 * 
		 if (*(DWORD*)&WaveBuf[nextchunk] == 0x74636166)// "fact"
						nextchunk += 12;
					if (*(DWORD*)&WaveBuf[nextchunk] != 0x61746164) // "data"
						nextchunk += (*(DWORD*)&WaveBuf[nextchunk+4]) + 8;
					if (*(DWORD*)&WaveBuf[nextchunk] == 0x61746164) {// "data"
						Buf = &WaveBuf[nextchunk+8]; // beginning of data
						BufSize = *(int*)&WaveBuf[nextchunk+4];
						WaveFormat.wFormatTag = WAVE_FORMAT_PCM;
						WaveFormat.nChannels = *(WORD*)&WaveBuf[22];
//asdf
//*(DWORD*)&WaveBuf[24] *= 2;
						WaveFormat.nSamplesPerSec = *(DWORD*)&WaveBuf[24];
						WaveFormat.nBlockAlign = *(WORD*)&WaveBuf[32];
//asdf
//*(DWORD*)&WaveBuf[28] *= 2;
						WaveFormat.nAvgBytesPerSec = *(DWORD*)&WaveBuf[28];
						WaveFormat.wBitsPerSample = *(WORD*)&WaveBuf[34];
						WaveFormat.cbSize = 0;
						nChannels = WaveFormat.nChannels;
						nSamplesPerSec = WaveFormat.nSamplesPerSec;
						pointsPerMillisecond = (double)nSamplesPerSec / 1000.0;
						millisecondsPerScreen = pointsPerScreen / pointsPerMillisecond;
						wBitsPerSample = WaveFormat.wBitsPerSample;
/*

		 */
		
		
		
		public static void readHeader2( InputStream is ) throws Exception {
			
			Hashtable listinfo = new Hashtable() ;
			  for(int i=0; i<infotype.length; i++)  //build the hashtable of values for easy searching
				listinfo.put(infotype[i], infodesc[i]);

			
			DataInputStream dis = new DataInputStream(is);
			StringBuffer txtbuf = new StringBuffer("");
			long datasize = 0;
			int bytespersec = 0;
			  int byteread = 0;
			  boolean isPCM = false;

			try {  

			  int riffdata=0;  // size of RIFF data chunk.
			  int chunkSize=0, infochunksize=0, bytecount=0, listbytecount=0;
			  String sfield="", infofield="", infodescription="", infodata="";
			  String sp = "   " ;  // spacer string.
			  String indent = sp + "   " ;
			//long filesize = (new File(selectFile)).length() ;  // get file length.

			//txtbuf.append(selectFile   + "    LENGTH:  " + filesize + " bytes\n\n") ; 

			/*  --------  Get RIFF chunk header --------- */
			  for (int i=1; i<=4; i++) 
			     sfield+=(char)dis.readByte() ;
			   if (!sfield.equals("RIFF")) {
				txtbuf.append(" ****  Not a valid RIFF file  ****\n") ;
				System.out.println(txtbuf.toString());
				System.exit(1) ;
			    }      

			  for (int i=0; i<4; i++) 
				chunkSize += dis.readUnsignedByte() *(int)Math.pow(256,i) ;
				txtbuf.append("\n" + sfield + " ----- data size: "+chunkSize+ " bytes\n") ;
			   
			 sfield="" ;
			  for (int i=1; i<=4; i++) 
				sfield+=(char)dis.readByte() ;
				txtbuf.append("Form-type: "+ sfield + "\n\n") ;

			  riffdata=chunkSize ;
			/* --------------------------------------------- */
			//System.out.println(txtbuf.toString());

			   bytecount = 4 ;  // initialize bytecount to include RIFF form-type bytes.

			  while (bytecount < riffdata )  {    // check for chunks inside RIFF data area. 
			      sfield="" ;
				int firstbyte = dis.readByte() ;
				if(firstbyte == 0) {  //if previous data had odd bytecount, was padded by null so skip
				  bytecount++;
				  continue;
				}
				  sfield+=(char)firstbyte;  //if we have a new chunk
			          for (int i=1; i<=3; i++) 
			            sfield+=(char)dis.readByte() ;

			     chunkSize=0 ;
			         for (int i=0; i<4; i++) 
			           chunkSize += dis.readUnsignedByte() *(int)Math.pow(256,i) ;
				bytecount += (8+chunkSize) ;
				txtbuf.append("\n" + sfield + " ----- data size: "+chunkSize+ " bytes\n") ;

			    if (sfield.equals("data"))   //get data size to compute duration later.
				datasize = chunkSize;

			    if (sfield.equals("fmt ")) {               // extract info from "format" chunk.
			         if (chunkSize<16) {
			             txtbuf.append(" ****  Not a valid fmt chunk  ****\n") ;
			             System.exit(1);
			           }      
			       int wFormatTag = dis.readUnsignedByte() + dis.readUnsignedByte()*256;
			       if (wFormatTag ==WAVE_FORMAT_PCM || wFormatTag ==WAVE_FORMAT_EXTENSIBLE || wFormatTag == WAVE_FORMAT_IEEE_FLOAT)
				isPCM = true;
			            if (wFormatTag == WAVE_FORMAT_PCM) 
			              txtbuf.append(indent + "wFormatTag:  WAVE_FORMAT_PCM\n") ;
			            else if (wFormatTag == WAVE_FORMAT_EXTENSIBLE)
				txtbuf.append(indent + "wFormatTag:  WAVE_FORMAT_EXTENSIBLE\n") ;
				else if (wFormatTag == WAVE_FORMAT_IEEE_FLOAT)
				  txtbuf.append(indent + "wFormatTag:  WAVE_FORMAT_IEEE_FLOAT\n") ;
				else
			              txtbuf.append(indent + "wFormatTag:  non-PCM format  " + wFormatTag + "\n") ;


			       int nChannels = dis.readUnsignedByte() ;
			           dis.skipBytes(1) ;
			             txtbuf.append(indent + "nChannels:  "+nChannels+"\n") ;
			       int nSamplesPerSec=0 ;
			         for (int i=0; i<4; i++) 
			           nSamplesPerSec += dis.readUnsignedByte() *(int)Math.pow(256,i) ;
			             txtbuf.append(indent + "nSamplesPerSec:  "+nSamplesPerSec+"\n") ;
			       int nAvgBytesPerSec=0 ;
			         for (int i=0; i<4; i++) 
			           nAvgBytesPerSec += dis.readUnsignedByte() *(int)Math.pow(256,i) ;
				   bytespersec = nAvgBytesPerSec;
			             txtbuf.append(indent + "nAvgBytesPerSec:  "+nAvgBytesPerSec+"\n") ;
			       int nBlockAlign=0;
			         for (int i=0; i<2; i++)
			           nBlockAlign += dis.readUnsignedByte() *(int)Math.pow(256,i) ;
			             txtbuf.append(indent + "nBlockAlign:  "+nBlockAlign+"\n") ;
			          if (isPCM) {     // if PCM or EXTENSIBLE format
			             int nBitsPerSample = dis.readUnsignedByte() ;
			             dis.skipBytes(1) ;
			             txtbuf.append(indent + "nBitsPerSample:  "+nBitsPerSample+"\n") ;
			            }
			           else  dis.skipBytes(2) ;
			          dis.skipBytes(chunkSize-16) ; //skip over any extra bytes in format specific field.                                  
			      }
			    else
			    if(sfield.equals("LIST")) {
				String listtype="" ;
				for (int i=1; i<=4; i++) 
				 listtype+=(char)dis.readByte() ;
				if(!listtype.equals("INFO")) { //skip over LIST chunks which don't contain INFO subchunks
				 dis.skipBytes(chunkSize-4);
				 continue;
				}

				listbytecount = 4;
				txtbuf.append("\n------- INFO chunks -------\n");
				while(listbytecount < chunkSize) {  //iterate over all entries in LIST chunk
				 infofield="";
				 infodescription = "";
				 infodata="";

				firstbyte = dis.readByte() ;
				if(firstbyte == 0) {  //if previous data had odd bytecount, was padded by null so skip
				  listbytecount++;
				  continue;
				}
				  infofield+=(char)firstbyte;  //if we have a new chunk
			          for (int i=1; i<=3; i++)  //get the remaining part of info chunk name ID
					infofield+=(char)dis.readByte() ;
				  infochunksize=0 ;
			          for (int i=0; i<4; i++)  //get the info chunk data byte size
					infochunksize += dis.readUnsignedByte() *(int)Math.pow(256,i) ;
				  listbytecount += (8+infochunksize) ;

				  for(int i=0; i<infochunksize; i++) {   //get the info chunk data
					byteread=dis.readByte() ;
					if(byteread == 0)    //if null byte in string, ignore it
					 continue;
					infodata+=(char)byteread;
				 }

				infodescription = (String)listinfo.get(infofield);
				if(infodescription !=null)
				  txtbuf.append(infodescription + ": " + infodata + "\n") ;
				else
				  txtbuf.append("unknown : " + infodata + "\n") ;
				}
			//------- end iteration over LIST chunk ------------
				txtbuf.append("------- end INFO chunks -------\n");
			   }

			    else    // if NOT the fmt or LIST chunks just skip over the data.
				dis.skipBytes(chunkSize) ;
			  }  // end while.
			//-----------  End of chunk iteration -------------

			    if(isPCM && datasize>0){   //compute duration of PCM wave file
				long waveduration = 1000L*datasize/bytespersec; //in msec units
				long mins = waveduration/ 60000;	// integer minutes
				double secs = 0.001*(waveduration % 60000);	//double secs.
				txtbuf.append("\nwav duration:  " + mins + " mins  " + secs +  " sec\n") ;
				}

			    txtbuf.append("\nFinal RIFF data bytecount: " + bytecount+"\n") ;
			   System.out.println(txtbuf.toString());
			 }
			catch( Exception e ) {
				e.printStackTrace();// end try.
			}
		}
		
		
		
	}
	
	
	public static class PCM_U8 {
		public static final String name = "pcm_u8";
		public static final int RATE_11025 = 11025;
	}

	public static class PCM_S16LE {
		public static final String name = "pcm_s16le";
		public static final int RATE_11025 = 11025;
	}
	
}
