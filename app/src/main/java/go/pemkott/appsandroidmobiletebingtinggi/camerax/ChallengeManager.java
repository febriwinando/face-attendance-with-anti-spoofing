package go.pemkott.appsandroidmobiletebingtinggi.camerax;

import java.util.Random;
//
//public class ChallengeManager {
//
//    public enum Challenge {
//        BLINK, SMILE
//    }
//
//    private Challenge current;
//
//    public ChallengeManager() {
//        randomize();
//    }
//
//    public void randomize() {
//        current = new Random().nextBoolean()
//                ? Challenge.BLINK
//                : Challenge.SMILE;
//    }
//
//    public Challenge getCurrent() {
//        return current;
//    }
//}

public class ChallengeManager {

    public enum Challenge {
        BLINK("Silakan KEDIP"),
        SMILE("Silakan SENYUM");

        public final String text;

        Challenge(String text) {
            this.text = text;
        }
    }

    private Challenge current;
    private boolean completed = false;

    public ChallengeManager() {
        reset();
    }

    public void reset() {
        completed = false;
        current = new Random().nextBoolean()
                ? Challenge.BLINK
                : Challenge.SMILE;
    }

    public Challenge getCurrent() {
        return current;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void markCompleted() {
        completed = true;
    }
}
