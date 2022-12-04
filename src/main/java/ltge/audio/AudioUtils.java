package ltge.audio;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import javazoom.jl.player.Player;

public class AudioUtils {

	private static Random rnd = new Random();
	private static Player mp3Player = null;
	
	public static OggClip load(String audioFile) throws IOException {
		return new OggClip(AudioUtils.class.getClassLoader().getResourceAsStream(audioFile));
	}
	
	public static void playOggClip(OggClip clip) {
		clip.play();
	}
	
	public static void playMP3Clip(InputStream mp3Clip) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					new Player(mp3Clip).play();
				} catch (Exception ex) {
					System.out.println("Error occured during playback process:" + ex.getMessage());
				}
			}
		}).start();
	}
	
	public static void playBackgroundMusic(String... mp3MusicFiles) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String musicFile = mp3MusicFiles[rnd.nextInt(mp3MusicFiles.length)];
					BufferedInputStream buffer = new BufferedInputStream(AudioUtils.class.getClassLoader().getResourceAsStream(musicFile));
					mp3Player = new Player(buffer);
					mp3Player.play();
				} catch (Exception ex) {
					System.out.println("Error occured during playback process:" + ex.getMessage());
				}
			}
		}).start();
	}
	
	public static void stopBackgroundMusic() {
		if (mp3Player != null) {
			mp3Player.close();
		}
	}
}
