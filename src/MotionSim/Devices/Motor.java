package Devices;

import java.util.Locale;

/** Team 5 Motor device for Motion Simulations API */
public final class Motor {
    /** Motor states */
    public enum State {STOPPED, UP, DOWN, EMERGENCY}

    /** Output sink for emitting response lines */
    public interface Output{void send(String line);}

    private State state;
    private Output output;

    /** Constructor: Creates a Team5_Temp.Motor */
    public Motor(Output output) {
        if (output == null) {
            throw new IllegalArgumentException("output required");
        }
        this.output = output;
        this.state = State.STOPPED;
    }

    /** Replaces the Output used to emit response lines */
    public void setOutput(Output output) {
        if (output == null) {
            throw new IllegalArgumentException("output required");
        }
        this.output = output;
    }

    /** Sets the next state and emits OK and STATE lines */
    private void setState(State next, String reason) {
        this.state = next;
        emit("OK " + reason);
        emit("STATE " + state.name());
    }

    /** Returns the current state name */
    public String getState() {
        return state.name();
    }


    /** Sends a single response line to the configured output */
    private void emit(String s) {
        output.send(s);
    }

    /** Parses a single command line and emits response */
    public void receive(String line) {
        if (line == null) {
            return;
        }
        String msg = line.trim();
        if (msg.isEmpty()) {
            return;
        }

        //Uppercase with stable locale and collapse multiple spaces
        String upper = msg.toUpperCase(Locale.ROOT).replaceAll("\\s+"," ").trim();

        //Accept START UP and START DOWN as synonyms
        if (upper.equals("START UP")) {
            setState(State.UP, "UP");
            return;
        }
        if (upper.equals("START DOWN")) {
            setState(State.DOWN, "DOWN");
            return;
        }

        String verb = upper.split("\\s+")[0];
        switch (verb) {
            case "START":
                emit("WARN need direction UP or DOWN");
                break;
            case "UP":
                setState(State.UP, "UP");
                break;
            case "DOWN":
                setState(State.DOWN, "DOWN");
                break;
            case "STOP":
                //Stop all motion
                setState(State.STOPPED, "STOP");
                break;
            case "EMERGENCY":
                //Enter emergency state immediately
                setState(State.EMERGENCY, "EMERGENCY");
                break;
            case "CLEAR":
                //Leave emergency state back to stopped
                if (state == State.EMERGENCY) {
                    setState(State.STOPPED, "CLEAR");
                }
                else {
                    emit("WARN not in EMERGENCY");
                }
                break;
            case "STATUS":
                //Report current state only
                emit("STATE " + state.name());
                break;
            case "SPEED":
                //Accept SPEED for API compatibility
                //it does not change behavior in this motor
                emit("OK SPEED");
                break;
            default:
                emit("WARN unknown " + msg);
        }
    }
}
