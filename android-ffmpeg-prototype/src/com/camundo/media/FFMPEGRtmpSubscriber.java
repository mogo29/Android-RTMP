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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;



public class FFMPEGRtmpSubscriber extends Thread{

	private static final String NAME = "FFMPEGRtmpSubscriber";
	
	private FFMPEGOutputPipe pipe;
	private AudioTrack audioTrack;
	
	private static int BUFFER_LENGTH = 1024 * 4;//for WAV audio this is ok?
	
    
    public FFMPEGRtmpSubscriber( FFMPEGOutputPipe pipe ) {
    	this.pipe = pipe;
    }
    
    
    
    @Override
    public void run() {
        try {
        	
        	
            
        	pipe.setPriority(MAX_PRIORITY);
            pipe.start();
            
            while( !pipe.processRunning() ) {
            	Log.i( NAME, "[ run() ] pipe not yet running, waiting.");
            	try {
            		Thread.sleep(1000);
            	}
            	catch( Exception e) {
            		e.printStackTrace();
            	}
            }
            
            int minBufferSize = AudioTrack.getMinBufferSize(AudioCodec.PCM_S16LE.RATE_11025, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT) *2 ;
            audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, AudioCodec.PCM_S16LE.RATE_11025, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize * 4, AudioTrack.MODE_STREAM);
            
            BUFFER_LENGTH = minBufferSize;
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_LENGTH);
		    buffer.order(ByteOrder.LITTLE_ENDIAN);
		    
            //byte[] buffer = new byte[BUFFER_LENGTH];
            Log.d( NAME, "buffer length [" + BUFFER_LENGTH + "]");
        	int len;
            
            //wait until minimum amount of data
            pipe.bootstrap( 100 );
            
            
            int overallBytes = 0;
            boolean started = false;
            
            //Log.d( NAME, "info rate[" + info.rate + "] channels [" + info.channels + "] dataSize [" + info.dataSize + "]");
            //BUFFER_LENGTH = info.dataSize;
            
           	while( (len = pipe.read(buffer.array(), buffer.arrayOffset(), buffer.capacity())) > 0 ) {
           		//Log.d(NAME, "[ run() ] len [" + len + "] buffer empty [" + pipe.available() + "]" );
           		overallBytes+= audioTrack.write(buffer.array(), 0, len);
           		//audioTrack.flush();
           		if (!started && overallBytes > minBufferSize ){
           			audioTrack.setPlaybackHeadPosition(2);
           			audioTrack.play();
                    started = true;
                }
           		//audioTrack.flush();
           	}
           	
            
        } 
        catch (IOException e) {
        	e.printStackTrace();
            Log.e(getClass().getName(), e.getMessage());
        }
        Log.i( NAME, "[ run() ] done");
    }
    
    
    public void shutdown(){
    	Log.i( NAME , "[ shutdown() ] up is false");
    	pipe.close();
    	audioTrack.flush();
        audioTrack.stop();
        audioTrack.release();
        
    }

    
}
