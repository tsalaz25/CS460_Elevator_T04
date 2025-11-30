package Audio;

import javax.sound.sampled.*;
import java.net.URL;

public final class Sfx {

    public static final Clip FIRE_LOOP      = load("city_firebell_loop1.wav");
    public static final Clip ELEVATOR_LOOP  = load("elevator_loop1.wav");
    public static final Clip ELEVATOR_START = load("elevator_start1.wav");
    public static final Clip ELEV_BELL      = load("elevbell1.wav");
    public static final Clip DOOR_CLOSE     = load("hall_elev_door.wav");

    private Sfx() {}

    private static Clip load(String fileName) {
        try {
            URL url = Sfx.class.getResource("/sfx/" + fileName);
            if (url == null) {
                System.err.println("[SFX ] Missing sound file: /sfx/" + fileName);
                return null;
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            return clip;
        } catch (Exception e) {
            System.err.println("[SFX ] Failed to load sound: " + fileName);
            e.printStackTrace();
            return null;
        }
    }

    public static void play(Clip clip) {
        if (clip == null) return;
        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);
        clip.start();
    }

    public static void loop(Clip clip) {
        if (clip == null) return;
        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public static void stop(Clip clip) {
        if (clip == null) return;
        clip.stop();
        clip.setFramePosition(0);
    }
}

