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

import java.io.IOException;
import java.io.OutputStream;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.util.Log;



public class FFMPEGRtmpSubscriber extends Thread{

	private static final String NAME = "FFMPEGRtmpSubscriber";
	
	private String socketAddress;
	private FFMPEGOutputPipe pipe;
	
	
	private LocalSocket receiver;
	private boolean up = false;
	
	
    
    public FFMPEGRtmpSubscriber( String socketAddress, FFMPEGOutputPipe pipe ) {
    	this.socketAddress = socketAddress;
    	this.pipe = pipe;
    	
    }
    
    private static final int BUFFER_LENGTH = 1024*2;//for AMR audio this is ok?
    
    
    @Override
    public void run() {
        try {
            LocalServerSocket server = new LocalServerSocket(socketAddress);
            pipe.start();
            
            while( !pipe.processRunning() ) {
            	Log.i( NAME, "[ run() ] pipe not yet running, waiting.");
            	try {
            		Thread.sleep(250);
            	}
            	catch( Exception e) {
            		e.printStackTrace();
            	}
            }
            //wait until minimum amount of data
            pipe.bootstrap();
            
            
            // its up
            up = true;
            while (up) {
                
                receiver = server.accept();
                
                receiver.setReceiveBufferSize(BUFFER_LENGTH);
                receiver.setSendBufferSize(BUFFER_LENGTH);
                
                
                if (receiver != null) {
                	OutputStream output = receiver.getOutputStream();
                    Log.i( NAME, "[ run() ] output [" + output + "]");
                    
                    int read = -1;
                    byte[] buffer = new byte[BUFFER_LENGTH];
                    int len;
                    
                    //while ( (read = pipe.read()) != -1) {
                    	
                    	//output.write(read);
                    	while( (len = pipe.read(buffer)) > 0 ) {
                    		Log.d("CameraCaptureServer", "[ run() ] len [" + len + "] buffer empty [" + pipe.available() + "] receiver [" + receiver + "]" );
                   			output.write(buffer, 0, len);
                   			output.flush();
                    	//}
                    }
                }
                else {
                	Log.i( NAME , "[ run() ] receiver is null!!!");
                }
            }
            if ( pipe != null ) {
            	Log.i( NAME , "[ run() ] closing pipe");
            	pipe.close();
            }
            else {
            	Log.i( NAME , "[ run() ] closing pipe not necessary, is already null");
            }
            
            if ( receiver != null ) {
            	Log.i( NAME, "[ run() ] closing receiver");
            	receiver.close();
            }
            else {
            	Log.i( NAME , "[ run() ] closing receiver not necessary, is already null");
            }
            
            Log.i( NAME, "[ run() ] closing server");
            server.close();
        } 
        catch (IOException e) {
        	e.printStackTrace();
            Log.e(getClass().getName(), e.getMessage());
        }
        Log.i( NAME, "[ run() ] done");
    }
    
    
    public void shutdown(){
    	Log.i( NAME , "[ shutdown() ] up is false");
    	up = false;
    }

    
    public boolean up() {
    	return up;
    }
    

}
