package com.camundo.media;

public class WavInfo {
	public int rate;
	public int channels;
	public int dataSize;

	public WavInfo( int rate, int channels, int dataSize ) {
		this.rate = rate;
		this.channels = channels;
		this.dataSize = dataSize;
	}
}