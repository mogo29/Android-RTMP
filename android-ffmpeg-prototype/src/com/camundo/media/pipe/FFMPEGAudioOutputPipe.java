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
package com.camundo.media.pipe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.camundo.util.AudioCodec;

import android.media.AudioFormat;
import android.util.Log;

public class FFMPEGAudioOutputPipe extends Thread implements AudioOutputPipe {
	
	private static final String TAG = "FFMPEGOutputPipe";
	
	private final static String LINE_SEPARATOR = System.getProperty("line.separator");
	
	
	private Process process;
	private String command;
	private boolean processRunning;
	
	private InputstreamReaderThread errorStreamReaderThread;
	//private InputstreamReaderThread inputStreamReaderThread;
	
		
	private InputStream inputStream;
	
	
	public FFMPEGAudioOutputPipe( String command ) {
		this.command = command;
	}
	
	
	public int read() throws IOException {
		return inputStream.read();
	}
	
	
	public void bootstrap() throws IOException {
		Log.i(TAG , "[ bootstrap() ] avl [" + available() + "]");
		while ( available() < 100 ) {
			Log.i(TAG , "[ bootstrap() ] avl [" + available() + "]");
			try {
				Thread.sleep(1000);
			}
			catch( Exception e ) {
				e.printStackTrace();
			}
		}
		flush();
		Log.i(TAG , "[ bootstrap() ] avl after bootstrap read [" + available() + "]");
	}
	
	
	private void flush() throws IOException{
		int av = inputStream.available();
		inputStream.read(new byte[av] , 0,  av);
	}
	
	
	public int read( byte[] buffer, int offset, int length ) throws IOException {
		return inputStream.read(buffer, offset, length);
	}
	
	
	public int read( byte[] buffer ) throws IOException {
		return inputStream.read(buffer);
	}
	
	
	
	public int available() throws IOException {
		if ( inputStream != null ) {
			return inputStream.available();
		}
		return 0;
		
	}
	
	public InputStream getInputStream() {
		return inputStream;
	}
	
	
	
	public void close() {
		Log.i(TAG , "[ close() ] closing outputstream");
		try {
			if ( inputStream != null ) {
				inputStream.close();
			}
		}
		catch( Exception e ){
			e.printStackTrace();
		}
//		if ( errorStreamReaderThread != null ) {
//			errorStreamReaderThread.finish();
//		}
		if ( process != null ) {
			process.destroy();
			process = null;
		}
	}
	
	
	@Override
	public void run () {
        try {
            Log.d( TAG, "[ run() ] command [" + command + "]");
            process = Runtime.getRuntime().exec(  command , null);
            
            inputStream = process.getInputStream();
            //inputStreamReaderThread = new InputstreamReaderThread(process.getInputStream());
            //inputStream = inputStreamReaderThread.inputStream;
            
            //Log.d( NAME , "[ run() ] inputStream created");
            errorStreamReaderThread = new InputstreamReaderThread(process.getErrorStream());
            Log.d( TAG, "[ run() ] errorStreamReader created");
             
            //inputStreamReaderThread.start();
            errorStreamReaderThread.start();

            if ( inputStream != null) {
            	processRunning = true;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
   }
	
	
	public boolean initialized() {
		return processRunning;
	}
	

	
	class InputstreamReaderThread extends Thread {

		public InputStream inputStream;
		
		public InputstreamReaderThread( InputStream i ){
			this.inputStream = i;
		}
		
		@Override
		public void run() {
            try {
                 InputStreamReader isr = new InputStreamReader(inputStream);
                 BufferedReader br = new BufferedReader(isr, 32);
                 String line;
                 while ((line = br.readLine()) != null) {
                	 Log.d( TAG , line + LINE_SEPARATOR);
                 }
            }
            catch( Exception e ) {
                 e.printStackTrace();
            }
       }
		
		public void finish() {
			if ( inputStream != null ) {
				try {
					inputStream.close();
				}
				catch( Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}


	@Override
	public int getSampleRate() {
		return AudioCodec.PCM_S16LE.RATE_11025;
	}


	@Override
	public int getChannelConfig() {
		return AudioFormat.CHANNEL_CONFIGURATION_MONO;
	}


	@Override
	public int getEncoding() {
		return AudioFormat.ENCODING_PCM_16BIT;
	}


}
