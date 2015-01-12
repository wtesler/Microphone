package tesler.will.mic;
import static java.lang.System.out;

import javax.sound.sampled.AudioFormat;

public class Main {


	public static void main(String[] args) {

		AudioFormat format = new AudioFormat(8000, 16, 1, true, false);
		MicrophoneListener listener = new MicrophoneListener() {
			@Override
			public void onReceive(byte[] data) {
				out.println(data);
			}
		};

		Microphone mic = new Microphone(format, listener);
		mic.start();

		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		mic.stopRecording();
	}

}
