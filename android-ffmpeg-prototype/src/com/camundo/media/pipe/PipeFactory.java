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

import com.camundo.util.AudioCodec;
import com.camundo.util.FFMPEGBootstrap;
import com.camundo.util.FFMPEGWrapper;

public class PipeFactory {

	private FFMPEGWrapper ffWrapper;
	private String ffmpegCommand;

	public PipeFactory() {
		ffWrapper = FFMPEGWrapper.getInstance();
		ffmpegCommand = ffWrapper.data_location + ffWrapper.ffmpeg;
	}

	
	public AudioInputPipe getADPCMAudioInputPipe(String publisherString) {
		String command = ffmpegCommand
				+ " -analyzeduration 0 -i pipe:0 -re -vn -acodec "
				+ AudioCodec.ADPCM_SWF.name + " -ar "
				+ AudioCodec.ADPCM_SWF.RATE_11025 + " -ac 1 -f flv "
				+ publisherString;
		return newAudioInputPipe(command);
	}

	
	public AudioInputPipe getNellymoserAudioInputPipe(String publisherString) {
		String command = ffmpegCommand
				+ " -analyzeduration 0 -muxdelay 0 -muxpreload 0 -i pipe:0 -re -vn -acodec "
				+ AudioCodec.Nellymoser.name
				+ " -ar 8000 -ac 1 -ab 16k -f flv " + publisherString;
		return newAudioInputPipe(command);
	}

	
	public AudioInputPipe getAACAudioInputPipe(String publisherString)
			throws Exception {
		// in case of aac, string should start with mp4: to be recognized by
		// red5
		// if ( publisherString.indexOf("mp4:") == -1) {
		// throw new
		// Exception("Publisher string should contain 'mp4:' for aac");
		// }
		String command = ffmpegCommand
				+ " -strict experimental -analyzeduration 0 -muxdelay 0 -muxpreload 0 -i pipe:0 -re -vn -acodec "
				+ AudioCodec.AAC.name + " -ac 1 -ar 8000 -ab 16k -f flv "
				+ publisherString;
		return newAudioInputPipe(command);
	}

	
	public AudioOutputPipe getAudioOutputPipe(String publisherString,
			int audioFileFormat, String codecName, String sourceCodec) {
		String command;
		// add -strict experimental and acodec for AAC
		if (sourceCodec.equals(AudioCodec.AAC.name)) {
			command = ffmpegCommand
					+ " -strict experimental -acodec aac -analyzeduration 0 -muxdelay 0 -muxpreload 0 -vn -itsoffset -2 -i "
					+ publisherString + " -re -vn -acodec ";
		} else if (sourceCodec.equals(AudioCodec.Nellymoser.name)) {
			command = ffmpegCommand
					+ " -analyzeduration 0 -vn -itsoffset -5 -acodec nellymoser -ar 8000 -ac 1 -i "
					+ publisherString + " -re -vn -acodec ";
		} else if (sourceCodec.equals(AudioCodec.ADPCM_SWF.name)) {
			command = ffmpegCommand
					+ "  -acodec adpcm_swf -analyzeduration 0 -muxdelay 0 -muxpreload 0 -vn -itsoffset -2 -i "
					+ publisherString + " -re -vn -acodec ";
		} else {
			throw new UnsupportedOperationException(
					"no support dor source codec [" + sourceCodec + "]");
		}
		if (audioFileFormat == AudioCodec.AUDIO_FILE_FORMAT_WAV) {
			if (codecName.equals(AudioCodec.PCM_S16LE.name)) {
				command += AudioCodec.PCM_S16LE.name + " -ar "
						+ AudioCodec.PCM_S16LE.RATE_11025 + " -ac 1";
			}
			command += " -f wav pipe:1";
		}
		FFMPEGAudioOutputPipe pipe = new FFMPEGAudioOutputPipe(command);
		return pipe;
	}

	
	private FFMPEGAudioInputPipe newAudioInputPipe(String command) {
		FFMPEGAudioInputPipe pipe = new FFMPEGAudioInputPipe(command);
		pipe.setBootstrap(FFMPEGBootstrap.AMR_BOOTSTRAP);
		return pipe;
	}

	
	public AudioInputPipe getVideoInputPipe(String publisherString) {
		String command = ffmpegCommand
				+ " -analyzeduration 0 -i pipe:0 -re -an -r 25 -f flv -b 100k -s 320x240 "
				+ publisherString;
		FFMPEGAudioInputPipe pipe = new FFMPEGAudioInputPipe(command);
		return pipe;
	}

}
