<h2>Description</h2>

<p>
Little prototype for streaming audio.
</p>

<p>
Target is the android platform ( APK 8 ).<br/>
For the moment it streams audio from the microphone to a RTMP server and can receive it back.<br/>
<a href="http://www.youtube.com/watch?v=pqUUL0QwuMo">http://www.youtube.com/watch?v=pqUUL0QwuMo
</a>
</p>



<br/>
<h2>To do</h2>
<ul>
   <li>Latency is approximately 2-3 seconds depending the network.</li>
   <li>Add video</li>
   <li>Tune FFMPEG build</li>
   <li>Streaming from files can be added</li>
</ul>




<br/>
<h2>FFMPEG</h2>
FFMPEG is wrapped and accessed via java using pipes :)

<pre>
Enabled decoders:
aac			amrnb			h263
adpcm_swf					flv			nellymoser

Enabled encoders:
aac			h263			pcm_s16le
adpcm_swf					nellymoser		pcm_u8
flv

</pre>


