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
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AudioActivity extends Activity {

	public static final String rtmpUrl = "rtmp://camundo.com:1935/test";
	public static final String client1 = "client1.a";
	public static final String client2 = "client2.a";

	PowerManager.WakeLock wakeLock;

	private boolean capturing = false;
	private boolean receiving = false;

	private EditText rtmpServerUrlText;
	private EditText publishingTopicText;
	private EditText subscribingTopicText;
	private Button switchTopicButton;

	private Button startCaptureButton;
	private Button stopCaptureButton;

	private TextView receivedStatusText;

	private AudioCall audioCall;
	
	
	private int bytesReceived;
	private int bytesPerSecond;
	private long bytesReceivedStart;
	

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
			if (capturing) {
				stopCall();
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
		setContentView(R.layout.audio);
		
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); 

		rtmpServerUrlText = (EditText) findViewById(R.id.rtmpServerUrl);
		rtmpServerUrlText.setText(rtmpUrl);

		publishingTopicText = (EditText) findViewById(R.id.publishingTopic);
		publishingTopicText.setText(client1);

		subscribingTopicText = (EditText) findViewById(R.id.subscribingTopic);
		subscribingTopicText.setText(client2);

		receivedStatusText = (TextView) findViewById(R.id.receivedStatusText);

		switchTopicButton = (Button) findViewById(R.id.switchTopicButton);
		switchTopicButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!capturing && !receiving) {
					Editable text1 = publishingTopicText.getText();
					Editable text2 = subscribingTopicText.getText();
					publishingTopicText.setText(text2);
					subscribingTopicText.setText(text1);
				} else {
					Toast.makeText(
							getBaseContext(),
							"stop subscribing and publishing before switching topics",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		startCaptureButton = (Button) findViewById(R.id.startCaptureButton);
		startCaptureButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!capturing) {
					startCall();
				}
			}
		});

		stopCaptureButton = (Button) findViewById(R.id.stopCaptureButton);
		stopCaptureButton.setEnabled(false);
		stopCaptureButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopCall();
			}
		});

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
				"streamingWakelock");

		Log.d("---------------------->", "" + ByteOrder.nativeOrder());
	}

	public void startCall() {

		String rtmp = rtmpServerUrlText.getText().toString().trim();
		String pTopic = publishingTopicText.getText().toString().trim();
		String sTopic = subscribingTopicText.getText().toString().trim();

		if (audioCall != null) {
			audioCall.finish();
		}

		audioCall = new AudioCall(this, rtmp, pTopic, sTopic);
		wakeLock.acquire();
		audioCall.start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (audioCall != null) {
					if (audioCall.audioSubscriber != null && audioCall.audioSubscriber.overallBytesReceived != 0) {
						bytesReceived = audioCall.audioSubscriber.overallBytesReceived;
						if ( bytesReceivedStart == 0 ) {
							bytesReceivedStart = System.currentTimeMillis();
						}
						else {
							bytesPerSecond = bytesReceived / (((int)(System.currentTimeMillis() - bytesReceivedStart)) / 1000);
						}
						runOnUiThread(done);
					}
					try {
						Thread.sleep(3000);
					} catch (Exception e) {}
				}
			}

			Runnable done = new Runnable() {
				public void run() {
					if (audioCall != null && audioCall.audioSubscriber != null) {
						receivedStatusText
								.setText("received "
										+ (audioCall.audioSubscriber.overallBytesReceived / 1024)
										+ "Kb ( " + (bytesPerSecond / 1025)  + "Kb/s )");
					}
				}
			};

		}).start();

		startCaptureButton.setEnabled(false);
		stopCaptureButton.setEnabled(true);

	}

	public void stopCall() {
		startCaptureButton.setEnabled(true);
		stopCaptureButton.setEnabled(false);
		wakeLock.release();
		if (audioCall != null) {
			audioCall.finish();
			audioCall = null;
		}
	}

}