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
package com.camundo;

import java.nio.ByteOrder;

import android.app.Activity;
import android.content.Context;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.camundo.media.AudioCodec;
import com.camundo.media.FFMPEGInputPipe;
import com.camundo.media.FFMPEGOutputPipe;
import com.camundo.media.FFMPEGRtmpPublisher;
import com.camundo.media.FFMPEGRtmpSubscriber;
import com.camundo.media.FFMPEGWrapper;
import com.camundo.util.NetworkUtils;

public class FFMPEGPrototype extends Activity {
	
	private FFMPEGRtmpPublisher publisher;
	private FFMPEGRtmpSubscriber subscriber;
	
	private MediaRecorder recorder;
	
	
	private LocalSocket localSocket;
	
	PowerManager.WakeLock wakeLock;
	
	private static final String LOCAL_SOCKET_ADDRESS_MIC = "microphoneCapture";
	
	private boolean capturing = false;
	private boolean receiving = false;
	
	private EditText rtmpServerUrlText;
	private EditText publishingTopicText;
	private EditText subscribingTopicText;
	private Button switchTopicButton;
	
	private Button startCaptureButton;
	private Button stopCaptureButton; 
	private Button startSubscribeButton;
	private Button stopSubscribeButton;
	
	

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_quit:
	    	if ( capturing ) {
	    		stopCapture();
	    	}
	    	if ( receiving ) {
	    		stopSubscribe();
	    	}
	    	finish();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
		
    @Override 
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        rtmpServerUrlText = (EditText)findViewById(R.id.rtmpServerUrl);
        rtmpServerUrlText.setText("rtmp://192.168.1.2:1935/camundo-test-server");
        
        publishingTopicText = (EditText)findViewById(R.id.publishingTopic);
        publishingTopicText.setText("kaka");
        
        subscribingTopicText = (EditText)findViewById(R.id.subscribingTopic);
        subscribingTopicText.setText("pipi");
        
        switchTopicButton = (Button)findViewById(R.id.switchTopicButton);
        switchTopicButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( !capturing && !receiving ) {
					Editable text1 = publishingTopicText.getText();
					Editable text2 = subscribingTopicText.getText();
					publishingTopicText.setText(text2);
					subscribingTopicText.setText(text1);
				}
				else {
					Toast.makeText(getBaseContext(), "stop subscribing and publishing before switching topics", Toast.LENGTH_SHORT).show();
				}
			}
		});
        
        startCaptureButton = (Button)findViewById(R.id.startCaptureButton);
        startCaptureButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( !capturing ) {
					startCapture();
				}
			}
		});
		
		stopCaptureButton = (Button)findViewById(R.id.stopCaptureButton);
		stopCaptureButton.setEnabled(false);
		stopCaptureButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopCapture();
			}
		});
		
		
		startSubscribeButton = (Button)findViewById(R.id.startSubscribeButton);
		startSubscribeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( !receiving ) {
					startSubscribe();
				}
			}
		});
		
		stopSubscribeButton = (Button)findViewById(R.id.stopSubscribeButton);
		stopSubscribeButton.setEnabled(false);
		stopSubscribeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopSubscribe();
			}
		});
		
		
		 
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "streamingWakelock");
		
		
		Log.d("---------------------->", "" + ByteOrder.nativeOrder());
    }
    
    
    
    
    public void startCapture() {
    	if ( !NetworkUtils.isOnline(this)){
    		Toast.makeText(getBaseContext(), "connect to internet first!", Toast.LENGTH_SHORT).show();
    	}
    	try {
    		capturing = true;
    		wakeLock.acquire();
    		
    		String url = rtmpServerUrlText.getText().toString().trim() + "/" + publishingTopicText.getText().toString().trim();
    		
    		//start the publisher
    		FFMPEGInputPipe pipe = FFMPEGWrapper.getInstance().getAudioInputPipe( url );
    		publisher = new FFMPEGRtmpPublisher(LOCAL_SOCKET_ADDRESS_MIC, pipe);
    		publisher.start();
    		//and wait until it is up
    		while ( !publisher.up()) {
    			try {
    				Log.i("[FFMPEGPrototype", "waiting for capture server to be up");
    				Thread.sleep(200);
    			}
    			catch( Exception e ){
    				e.printStackTrace();
    			}
    		}
    		
    		//connect to the local socket
    		localSocket = new LocalSocket();
    		localSocket.connect(new LocalSocketAddress(LOCAL_SOCKET_ADDRESS_MIC));
    		Log.i("[FFMPEGPrototype", "connected to socket");
    		localSocket.setReceiveBufferSize(64);
    		localSocket.setSendBufferSize(64);
    		
    		//prepare the mediarecorder
    		recorder = new MediaRecorder();
    		recorder.setAudioSource(AudioSource.MIC);
    		recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
    		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    		recorder.setOutputFile(localSocket.getFileDescriptor());
    		recorder.prepare();
    		
    		try {
				Log.i("[FFMPEGPrototype", "waiting for capture server to be up");
				Thread.sleep(1000);
			}
			catch( Exception e ){
				e.printStackTrace();
			}
    		
    		recorder.start();
    		
    		stopCaptureButton.setEnabled(true);
    		startCaptureButton.setEnabled(false);
    		
    	}
    	catch( Exception e ) {
    		e.printStackTrace();
    		capturing = false;
    	}
    }
    
    
   
    
    public void stopCapture() {
    	try {
    		if ( publisher != null ) {
        		Log.i("[FFMPEGPrototype", "shutting down publisher");
    			publisher.shutdown();
    			localSocket.close();
    		}
    		recorder.stop();
    		recorder.release();
    	}
    	catch( Exception e ) {
    		e.printStackTrace();
    	}
    	
		wakeLock.release();
		capturing = false;
		
		stopCaptureButton.setEnabled(false);
		startCaptureButton.setEnabled(true);
    }

    
    
    
    
    public void startSubscribe() {
    	if ( !NetworkUtils.isOnline(this)){
    		Toast.makeText(getBaseContext(), "connect to internet first!", Toast.LENGTH_SHORT).show();
    	}
    	try {
    		receiving = true;
    		wakeLock.acquire();
    		
    		String url = rtmpServerUrlText.getText().toString().trim() + "/" + subscribingTopicText.getText().toString().trim();;
    		
    		//start the publisher
    		FFMPEGOutputPipe pipe = FFMPEGWrapper.getInstance().getAudioOutputPipe(url, AudioCodec.AUDIO_FILE_FORMAT_WAV, AudioCodec.PCM_S16LE.name);
    		subscriber = new FFMPEGRtmpSubscriber(pipe);
    		subscriber.start();
    		
    		startSubscribeButton.setEnabled(false);
    		stopSubscribeButton.setEnabled(true);
    		
    		
    	}
    	catch( Exception e ) {
    		e.printStackTrace();
    	}
    }
    
    
    
    public void stopSubscribe() {
    	try {
    		if ( subscriber != null ) {
        		Log.i("[FFMPEGPrototype", "shutting down subscriber");
        		subscriber.shutdown();
    		}
    	}
    	catch( Exception e ) {
    		e.printStackTrace();
    	}
		wakeLock.release();
		receiving = false;
		startSubscribeButton.setEnabled(true);
		stopSubscribeButton.setEnabled(false);
    }
    
    
    
    
    
    
    
    
}