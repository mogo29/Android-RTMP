package com.camundo;

import android.content.Context;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.camundo.media.AudioPublisher;
import com.camundo.media.AudioSubscriber;
import com.camundo.media.pipe.AudioInputPipe;
import com.camundo.media.pipe.AudioOutputPipe;
import com.camundo.media.pipe.PipeFactory;
import com.camundo.util.AudioCodec;

public class AudioCall extends Thread {
	
	private static final String TAG = "AudioCall";
	
	private Context context;
	private String rtmpServerUrl;
	private String publishingTopic;
	private String subscribingTopic;
	
	private final String sourceCodec = AudioCodec.Nellymoser.name;
	
	private LocalSocket localSocket;
	private static final String LOCAL_SOCKET_ADDRESS_MIC = "microphoneCapture";
	
	private MediaRecorder recorder;
	
	private PipeFactory pipeFactory = new PipeFactory();
	
	public AudioPublisher audioPublisher;
	public AudioSubscriber audioSubscriber;
	
	
	
	public AudioCall( Context context, String rtmpUrl, String pTopic, String sTopic ) {
		this.context = context;
		this.rtmpServerUrl = rtmpUrl;
		this.publishingTopic = pTopic;
		this.subscribingTopic = sTopic;
	}
	
	
	
	@Override
	public void run() {
		startPublish();
		startSubscribe();
	}
	
	
	public void finish() {
		Log.i(TAG, "[finish()]");
		try {
			stopCapture();
		}
		catch( Exception e ) {
			Log.e(TAG, "can not stop publishing", e);
		}
		
		try {
			stopSubscribe();
		}
		catch( Exception e ) {
			Log.e(TAG, "can not stop subscribing", e);
		}
	}
	
	public void startPublish() {
    	try {

    		String url = rtmpServerUrl + "/" + publishingTopic;
    		
    		//define the pipe
    		AudioInputPipe pipe = null;
    		if ( sourceCodec.equals(AudioCodec.Nellymoser.name)) {
    			pipe = pipeFactory.getNellymoserAudioInputPipe( url );
    		}
    		else if ( sourceCodec.equals(AudioCodec.ADPCM_SWF.name) ) {
    			pipe = pipeFactory.getADPCMAudioInputPipe( url );
    		}
    		else if ( sourceCodec.equals(AudioCodec.AAC.name ) ) {
    			pipe = pipeFactory.getAACAudioInputPipe( url );
    		}
    		
    		//start the publisher
    		audioPublisher = new AudioPublisher( LOCAL_SOCKET_ADDRESS_MIC, pipe);
    		audioPublisher.start();
    		//and wait until it is up
    		while ( !audioPublisher.up()) {
    			try {
    				Log.i( TAG , "waiting for capture server to be up");
    				Thread.sleep(50);
    			}
    			catch( Exception e ){
    				e.printStackTrace();
    			}
    		}
    		
    		//connect to the local socket
    		localSocket = new LocalSocket();
    		localSocket.connect(new LocalSocketAddress(LOCAL_SOCKET_ADDRESS_MIC));
    		Log.i( TAG , "connected to socket");
    		//localSocket.setReceiveBufferSize(64);
    		//localSocket.setSendBufferSize(64);
    		
    		//prepare the mediarecorder
    		recorder = new MediaRecorder();
    		recorder.setAudioSource(AudioSource.MIC);
    		recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
    		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    		recorder.setOutputFile(localSocket.getFileDescriptor());
    		recorder.prepare();
    		
    		try {
				Log.i( TAG , "waiting for capture server to be up");
				Thread.sleep(1500);
			}
			catch( Exception e ){
				e.printStackTrace();
			}
    		
    		recorder.start();
    		
    	}
    	catch( Exception e ) {
    		e.printStackTrace();
    		if ( e.getMessage().equalsIgnoreCase("broken pipe")) {
    			Toast.makeText(context, "can not connect", Toast.LENGTH_SHORT).show();
    		}
    	}
    	
    }
    
    
   
    
    public void stopCapture() {
    	try {
    		if ( audioPublisher != null ) {
        		Log.i( TAG , "shutting down publisher");
    			audioPublisher.shutdown();
    			localSocket.close();
    			audioPublisher = null;
    		}
    		recorder.stop();
    		recorder.release();
    	}
    	catch( Exception e ) {
    		Log.e(TAG, "[stopCapture()]", e);
    	}
    	
    }

    
    
    
    
    public void startSubscribe() {
    	try {
    		String url = rtmpServerUrl.trim() + "/" + subscribingTopic.trim();
    		
    		//start the publisher
    		AudioOutputPipe pipe = pipeFactory.getAudioOutputPipe(url, AudioCodec.AUDIO_FILE_FORMAT_WAV, AudioCodec.PCM_S16LE.name, sourceCodec);
    		//FileAudioOutputPipe pipe = new FileAudioOutputPipe(new File("/data/data/com.camundo/sample.wav"));
    		audioSubscriber = new AudioSubscriber(pipe);
    		audioSubscriber.start();
    		
    	}
    	catch( Exception e ) {
    		Log.e(TAG, "[startSubscribe()]", e);
    	}
    }
    
    
    
    public void stopSubscribe() {
    	try {
    		if ( audioSubscriber != null ) {
        		Log.i(TAG , "shutting down subscriber");
        		audioSubscriber.shutdown();
        		audioSubscriber = null;
    		}
    	}
    	catch( Exception e ) {
    		e.printStackTrace();
    	}
    }
	

}
