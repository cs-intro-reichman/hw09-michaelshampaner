import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
		String window = "";
        char c;
        In in = new In(fileName);
        // Reads just enough characters to form the first window
        for (int i = 0; i < windowLength; i++) {
            window += in.readChar();
        }
        // Processes the entire text, one character at a time
        while (!in.isEmpty()) {
            // Gets the next character
            c = in.readChar();
            // Checks if the window is already in the map
            List probs = CharDataMap.get(window);
            if (probs == null){
                CharDataMap.put(window, new List());
            }
            probs = CharDataMap.get(window);
            // Calculates the counts of the current character.
            probs.update(c);
            // Advances the window: adds c to the window’s end, and deletes the
            // window's first character.
            window = window.substring(1) + c;
        }
        // The entire file has been processed, and all the characters have been counted.
        // Proceeds to compute and set the p and cp fields of all the CharData objects
        // in each linked list in the map.
        for (List probs : CharDataMap.values()){
            calculateProbabilities(probs);
        }

    }


    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	public void calculateProbabilities(List probs) {				
        int totalCount = 0;
        for (int i = 0; i < probs.getSize(); i++) {
            CharData temp = probs.get(i);
            totalCount += temp.count;
        }
        double prevCP = 0;
        for (int i = 0; i < probs.getSize(); i++) {
            CharData temp = probs.get(i);
            temp.p = (double)temp.count / totalCount;
            temp.cp = prevCP + temp.p;
            prevCP = temp.cp;
        }
            
	}
	public char getRandomChar(List probs) {
		double r = randomGenerator.nextDouble();
        for (int i = 0; i < probs.getSize(); i++) {
            CharData temp = probs.get(i);
            if (temp.cp > r){
                return temp.chr;
            }
        }

        return ' ';
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
        String result = initialText;

        // not valid text
        if(initialText.length() < windowLength) {
            return initialText;
        }
        String window = initialText.substring(Math.max(0, initialText.length()-windowLength));
        if (CharDataMap.get(window) == null){
            return initialText;
        }

        // generate each char of the new string
        while (result.length() - initialText.length() < textLength){

            // get the list of the window
            List list = CharDataMap.get(window);
            // get the new char
            char c = getRandomChar(list);
            // add the new char
            result += c;
            // set new window
            window = window.substring(1) + c;
        }
        return result;
	}

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
		// Your code goes here
    }
}
