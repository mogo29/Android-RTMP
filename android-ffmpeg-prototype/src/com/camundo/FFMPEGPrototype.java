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
package com.camundo;

import java.nio.ByteOrder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import com.camundo.media.AudioPublisher;
import com.camundo.media.AudioSubscriber;
import com.camundo.media.pipe.AudioInputPipe;
import com.camundo.media.pipe.AudioOutputPipe;
import com.camundo.media.pipe.PipeFactory;
import com.camundo.util.AudioCodec;
import com.camundo.util.NetworkUtils;

public class FFMPEGPrototype extends Activity {
	
	
	PowerManager.WakeLock wakeLock;
	
	
	private boolean capturing = false;
	private boolean receiving = false;
	
	
	private Button audioActivityButton;
	private Button videoActivityButton; 
	
	
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
	    		//stopCapture();
	    	}
	    	if ( receiving ) {
	    		//stopSubscribe();
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
     
        audioActivityButton = (Button)findViewById(R.id.audioActivityButton);
        audioActivityButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass( getBaseContext(), AudioActivity.class);
				startActivity(intent);
			}
		});
        
        
        videoActivityButton = (Button)findViewById(R.id.videoActivityButton);
        videoActivityButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
		});
		
    }
    
    
    
    
    
    
}