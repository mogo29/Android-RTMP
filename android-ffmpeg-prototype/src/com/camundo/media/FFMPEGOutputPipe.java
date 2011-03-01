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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.util.Log;

public class FFMPEGOutputPipe extends Thread {
	
	private static final String NAME = "FFMPEGOutputPipe";
	
	private final static String LINE_SEPARATOR = System.getProperty("line.separator");
	
	
	private Process process;
	private String command;
	private boolean processRunning;
	
	private InputstreamReaderThread errorStreamReaderThread;
	private InputstreamReaderThread inputStreamReaderThread;
	
	private boolean firstRead = false;
	
	
	private InputStream inputStream;
	
	
	public FFMPEGOutputPipe( String command ) {
		this.command = command;
	}
	
	
	public int read() throws IOException {
		return inputStream.read();
	}
	
	
	public void bootstrap( int size ) throws IOException {
		Log.i(NAME , "[ bootstrap() ] avl [" + available() + "]");
		while ( available() < size ) {
			Log.i(NAME , "[ bootstrap() ] avl [" + available() + "]");
			try {
				Thread.sleep(1000);
			}
			catch( Exception e ) {
				e.printStackTrace();
			}
		}
		int av = inputStream.available();
		inputStream.read(new byte[av] , 0,  av);
		Log.i(NAME , "[ bootstrap() ] avl after bootstrap read [" + available() + "]");
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
		Log.i(NAME , "[ close() ] closing outputstream");
		try {
			if ( inputStream != null ) {
				inputStream.close();
			}
		}
		catch( Exception e ){
			e.printStackTrace();
		}
		if ( errorStreamReaderThread != null ) {
			errorStreamReaderThread.finish();
		}
		if ( process != null ) {
			process.destroy();
			process = null;
		}
	}
	
	
	@Override
	public void run () {
        try {
            	Log.d( NAME, "[ run() ] command [" + command + "]");
            process = Runtime.getRuntime().exec(  command , null);
            
            inputStream = process.getInputStream();
            //inputStreamReaderThread = new InputstreamReaderThread(process.getInputStream());
            //inputStream = inputStreamReaderThread.inputStream;
            
            Log.d( NAME , "[ run() ] inputStream created");
            //errorStreamReaderThread = new InputstreamReaderThread(process.getErrorStream());
            Log.d( NAME, "[ run() ] errorStreamReader created");
             
            //inputStreamReaderThread.start();
            //errorStreamReaderThread.start();

            if ( inputStream != null) {
            	processRunning = true;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
   }
	
	
	public boolean processRunning() {
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
                	 Log.d( NAME , line + LINE_SEPARATOR);
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


}
