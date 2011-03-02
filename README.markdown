<h2>Description</h2>

<p>
Little prototype for streaming audio.
</p>

<p>
Target is the android platform ( APK 8 ).<br/>
For the moment it streams audio from the microphone to a RTMP server and can receive it back.
</p>



<br/>
<h2>To do</h2>
<ul>
   <li>Latency is approximately 3 seconds depending the network.</li>
   <li>Add video</li>
   <li>Tune FFMPEG build</li>
   <li>Streaming from files can be added</li>
</ul>




<br/>
<h2>FFMPEG</h2>
FFMPEG is wrapped and accessed via java using pipes :)

<pre>
Enabled decoders:
adpcm_swf		flv			h263
amrnb

Enabled encoders:
aac			flv			pcm_s16le
adpcm_swf					h263			pcm_u8
</pre>

http://www.youtube.com/watch?v=pqUUL0QwuMo

