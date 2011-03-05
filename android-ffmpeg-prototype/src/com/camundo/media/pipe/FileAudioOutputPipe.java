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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.media.AudioFormat;
import android.util.Log;

import com.camundo.util.AudioCodec;
import com.camundo.util.WavInfo;

public class FileAudioOutputPipe extends Thread implements AudioOutputPipe {

	private static final String TAG = "FileOutputPipe";

	private File file;
	private InputStream inputStream;
	private int sampleRate;
	private int channels;

	private boolean closed = false;

	public FileAudioOutputPipe(File file) {
		this.file = file;
	}

	public int read() throws IOException {
		return inputStream.read();
	}

	
	public void bootstrap() throws IOException {
		Log.i(TAG, "[ bootstrap() ] avl [" + available() + "]");
		while (available() < 100) {
			Log.i(TAG, "[ bootstrap() ] avl [" + available() + "]");
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		flush();
		Log.i(TAG, "[ bootstrap() ] avl after bootstrap read [" + available()
				+ "]");
	}

	
	private void flush() throws IOException {
		int av = 100;
		inputStream.read(new byte[av], 0, av);
	}

	public int available() throws IOException {
		if (inputStream != null) {
			return inputStream.available();
		}
		return 0;

	}

	public int read(byte[] buffer, int offset, int length) throws IOException {
		return inputStream.read(buffer, offset, length);
	}

	public int read(byte[] buffer) throws IOException {
		if (!closed) {
			return inputStream.read(buffer);
		}
		return -1;
	}

	public void close() {
		try {
			if (!closed) {
				synchronized (inputStream) {
					inputStream.close();
					closed = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start() {
		try {
			inputStream = new FileInputStream(file);
			WavInfo w = AudioCodec.WaveFile.readHeader(inputStream);
			this.sampleRate = w.rate;
			this.channels = w.channels;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean initialized() {
		return true;
	}

	@Override
	public int getSampleRate() {
		return sampleRate;
	}

	@Override
	public int getChannelConfig() {
		if (channels == 1) {
			return AudioFormat.CHANNEL_OUT_MONO;
		} else if (channels == 2) {
			return AudioFormat.CHANNEL_OUT_STEREO;
		}
		return 0;
	}

	@Override
	public int getEncoding() {
		return AudioFormat.ENCODING_PCM_16BIT;
	}

}
