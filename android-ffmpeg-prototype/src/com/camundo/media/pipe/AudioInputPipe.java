package com.camundo.media.pipe;

import java.io.IOException;

public interface AudioInputPipe {
	
	public void start();
	
	public boolean initialized();
	
	public void writeBootstrap();
	
	
	public void write( int oneByte ) throws IOException;
	public void write( byte[] buffer, int offset, int length ) throws IOException;
	
	public void close();

}
