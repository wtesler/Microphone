package tesler.will.mic.test;

import static java.lang.System.out;
import static org.junit.Assert.*;

import javax.sound.sampled.AudioFormat;

import tesler.will.mic.Microphone;
import tesler.will.mic.MicrophoneListener;

public class Test {

	@org.junit.Test
	public void simpleTest() {

		AudioFormat format = new AudioFormat(8000, 16, 1, true, false);
		MicrophoneListener listener = new MicrophoneListener() {
			@Override
			public void onReceive(byte[] data) {
			}
		};

		Microphone mic = new Microphone(format, listener);
		mic.start();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail();
		}
		mic.interrupt();
	}

	@org.junit.Test
	public void invalidAudioFormat() {

		// This is not a possible format.
		AudioFormat format = new AudioFormat(8000, 24, 3, true, true);

		MicrophoneListener listener = new MicrophoneListener() {
			@Override
			public void onReceive(byte[] data) {
			}
		};

		Microphone mic = new Microphone(format, listener);
		mic.start();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail();
		}

		if (mic.isRunning()) {
			fail();
		}
		mic.interrupt();
	}

}
