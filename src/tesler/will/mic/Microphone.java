package tesler.will.mic;

import static java.lang.System.err;
import static java.lang.System.out;

import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

public class Microphone extends Thread {

	private static final boolean INTERACTIVE = false;

	private TargetDataLine mLine;

	private MicrophoneListener mListener = null;

	private AudioFormat mFormat = null;

	private volatile boolean isRunning = false;

	@SuppressWarnings("unused")
	private Microphone() {
	}

	public Microphone(AudioFormat format, MicrophoneListener listener) {

		super("Microphone Thread");

		this.mFormat = format;
		this.mListener = listener;
	}

	void initialize(final boolean interactive) throws LineUnavailableException {

		Scanner in = new Scanner(System.in);

		Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
		// Iterate through mixers, looking for Lines.
		Mixer mixer = null;
		int mixerIndex = 0;

		while (mLine == null) {

			int choice = -1;

			if (interactive) {
				out.println("Select a microphone for input.");

				int i = 0;
				for (Mixer.Info info : mixerInfo) {
					out.format("%d.\n", i++);
					out.format("Name: %s\n", info.getName());
					out.format("Vendor: %s\n", info.getVendor());
					out.format("Version: %s\n", info.getVersion());
					out.format("Description: %s\n\n", info.getDescription());
				}

				try {

					choice = in.nextInt();

					while (choice < 0 || choice >= i) {
						err.format("Choice %d not an option. Choose again.\n",
								choice);
						choice = in.nextInt();
					}
				} catch (InputMismatchException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (NoSuchElementException e) {
					e.printStackTrace();
				}

				out.format("Choice: %d has been selected.\n\n", choice);

				mixer = AudioSystem.getMixer(mixerInfo[choice]);

			} else { // END INTERACTIVE
				mixer = AudioSystem.getMixer(mixerInfo[mixerIndex++]);
			}

			Line.Info[] lineInfo = mixer.getTargetLineInfo();

			for (Line.Info info : lineInfo) {

				// Print class name of each line.
				if (interactive)
					out.format("\tName: %s\n", info.getLineClass().getName());

				if (TargetDataLine.class == info.getLineClass()) {
					try {
						mLine = (TargetDataLine) AudioSystem.getLine(info);
					} catch (LineUnavailableException e) {
						if (interactive)
							err.format("\tLine was unavailable!\n");
					}
				}
			}

			if (mLine == null) {
				if (interactive)
					err.println("\nMixer did not contain a TargetDataLine! Choose a different Mixer.\n");
			}
		}

		// Wont need the Scanner anymore.
		try {
			in.close();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}

		if (mLine == null) {
			throw new LineUnavailableException(
					"NO AVAILABLE TARGET DATA LINE DETECTED");
		}
	}

	@Override
	public void interrupt() {
		super.interrupt();
		isRunning = false;
	}

	@Override
	public boolean isInterrupted() {
		return !isRunning;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void startRecording() {
		start();
	}

	public void stopRecording() {
		interrupt();
	}

	void startRecording(AudioFormat format, MicrophoneListener listener)
			throws LineUnavailableException {

		isRunning = true;

		// RECORDING STARTED
		mLine.open(format, (int) (5 * format.getFrameRate()));
		mLine.start();
		byte[] data = new byte[(int) format.getFrameRate()];
		while (isRunning) {
			mLine.read(data, 0, data.length);
			listener.onReceive(data);
		}

		// RECORDING STOPPED
		mLine.drain();
		mLine.close();
	}

	void listMixers() {
		Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();

		for (Mixer.Info info : mixerInfo) {
			out.format("Name: %s\n", info.getName());
			out.format("Vendor: %s\n", info.getVendor());
			out.format("Version: %s\n", info.getVersion());
			out.format("Description: %s\n", info.getDescription());
		}
	}

	@Override
	public void run() {
		try {
			initialize(INTERACTIVE);
			startRecording(mFormat, mListener);
		} catch (LineUnavailableException e) {
			interrupt();
		}
	}

}
