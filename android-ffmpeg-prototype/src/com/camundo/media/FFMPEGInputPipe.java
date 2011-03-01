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
package com.camundo.media;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.util.Log;

public class FFMPEGInputPipe extends Thread {
	
	private final static String LINE_SEPARATOR = System.getProperty("line.separator");
	
	
	private Process process;
	private String command;
	private boolean processRunning;
	
	private InputstreamReaderThread errorStreamReaderThread;
	private InputstreamReaderThread inputStreamReaderThread;
	
	private OutputStream outputStream;
	
	private byte[] bootstrap;
	
	
	public FFMPEGInputPipe( String command ) {
		this.command = command;
	}
	
	
	public void write( int oneByte ) throws IOException {
			outputStream.write(oneByte);
	}
	
	
	
	public void write( byte[] buffer, int offset, int length ) throws IOException {
		outputStream.write(buffer, offset, length);
		outputStream.flush();
	}
	
	
	protected void setBootstrap( byte[] bootstrap ) {
		this.bootstrap = bootstrap;
	}
	
	protected void writeBootstrap() {
		if ( bootstrap != null ){
			try {
				write( bootstrap, 0, bootstrap.length);
			}
			catch( Exception e ) {
				e.printStackTrace();
			}
		}
	}
	
	
	public void close() {
		Log.i("FFMPEGInputPipe", "[ close() ] closing outputstream");
		try {
			outputStream.close();
		}
		catch( Exception e ){
			e.printStackTrace();
		}
		inputStreamReaderThread.finish();
		errorStreamReaderThread.finish();
		process.destroy();
		process = null;
	}
	
	
	@Override
	public void run () {
        try {
        	Log.d("FFMPEGInputPipe", " " + outputStream);
            System.out.println("[ streamToRtmp() ] [" + command + "]");
            process = Runtime.getRuntime().exec(  command , null);
            
            inputStreamReaderThread = new InputstreamReaderThread(process.getInputStream());
            Log.d("FFMPEGInputPipe", "[ run() ] inputStreamReader created");
            errorStreamReaderThread = new InputstreamReaderThread(process.getErrorStream());
            Log.d("FFMPEGInputPipe", "[ run() ] errorStreamReader created");
             
            inputStreamReaderThread.start();
            errorStreamReaderThread.start();
             
            outputStream = process.getOutputStream();
            Log.d("FFMPEGInputPipe", "[ run() ] os : " + outputStream);
            if ( outputStream != null) {
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

		private InputStream inputStream;
		
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
                	 Log.d("FFMPEGInputPipe", line + LINE_SEPARATOR);
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
