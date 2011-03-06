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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.camundo.media.pipe.AudioOutputPipe;



public class AudioSubscriber extends Thread{

	private static final String TAG = "AudioSubscriber";
	
	private AudioOutputPipe pipe;
	private AudioTrack audioTrack;
	
	public int overallBytesReceived = 0;
	 

    public AudioSubscriber( AudioOutputPipe pipe ) {
    	this.pipe = pipe;
    }
    
        
    @Override
    public void run() {
        try {
        	//hmmm can always try
        	//pipe.setPriority(MAX_PRIORITY);
            pipe.start();
            
            while( !pipe.initialized() ) {
            	Log.i( TAG, "[ run() ] pipe not yet running, waiting.");
            	try {
            		Thread.sleep(1000);
            	}
            	catch( Exception e) {
            		e.printStackTrace();
            	}
            }
            
            int minBufferSize =  AudioTrack.getMinBufferSize( pipe.getSampleRate(), pipe.getChannelConfig(), pipe.getEncoding());
            
            audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, 
						            		pipe.getSampleRate(), 
						            		pipe.getChannelConfig(), 
						            		pipe.getEncoding(), 
						            		minBufferSize * 4, 
						            		AudioTrack.MODE_STREAM);
           
            
            ByteBuffer buffer = ByteBuffer.allocate(minBufferSize);
		    buffer.order(ByteOrder.LITTLE_ENDIAN);
		    
            Log.d( TAG, "buffer length [" + minBufferSize + "]");
        	int len;
            
            //wait until minimum amount of data ( header is 44 )
            pipe.bootstrap();
            
            
            boolean started = false;
            
           	while( (len = pipe.read(buffer.array())) > 0 ) {
           		//Log.d(NAME, "[ run() ] len [" + len + "] buffer empty [" + pipe.available() + "]" );
           		overallBytesReceived+= audioTrack.write(buffer.array(), 0, len);
           		if (!started && overallBytesReceived > minBufferSize ){
           			audioTrack.play();
                    started = true;
                }
           	}
            
        } 
        catch (IOException e) {
            Log.e( TAG, "[ run() ]", e);
        }
        Log.i( TAG, "[ run() ] done");
    }
    
    
    public void shutdown(){
    	Log.i( TAG , "[ shutdown() ] up is false");
    	if ( pipe != null ) {
    		Log.i( TAG , "[ shutdown() ] closing pipe");
    		pipe.close();
    	}
    	if ( audioTrack != null ) {
    		audioTrack.stop();
    		audioTrack.release();
    	}
        
    }

    
}
