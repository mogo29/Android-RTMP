package com.camundo.media.pipe;

import java.io.IOException;

public interface AudioOutputPipe {
	
	public void start();
	
	public boolean initialized();
	
	public void bootstrap() throws IOException;
	
	public int read( byte[] buffer, int offset, int length ) throws IOException;
	public int read( byte[] buffer ) throws IOException;
	
	public int available() throws IOException;
	
	public int getSampleRate();
	public int getChannelConfig();
	public int getEncoding();

	
	public void close();
	
}
