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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.camundo.Camundo;

/**
 * Bootstrap makes ffmpeg go faster ( already connects to rtmp e.g. )
 * @author wouter
 *
 */
public  class FFMPEGBootstrap {
		
		static {
			try {
				InputStream inputStream = Camundo.getContext().getAssets().open("bootstrap.amr");
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte buf[]=new byte[1024];
				int len;
				while((len=inputStream.read(buf))>0) {
					baos.write(buf,0,len);
				}
				AMR_BOOTSTRAP = baos.toByteArray();
				inputStream.close();
			}
			catch( Exception e ) {
				e.printStackTrace();
			}
		}
	
	
		public static byte[] AMR_BOOTSTRAP;

}
