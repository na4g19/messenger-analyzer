import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deals with statistical data of the user
 */
public class UserStatistics {

    private int messagesSent;
    private long wordsSent;
    private long charsSent;

    private int namesChanged = 0;
    private int groupNameChanged = 0;
    private int photoChanged = 0;
    private int themeChanged = 0;
    private int spamMessagesSent = 0;
    private int spamWordsSent = 0;
    private int spamCharsSent = 0;
    private int reactionsSent = 0;
    private int reactionsReceived = 0;

    private double averageWords;
    private double averageChars;

    private List<String> nicknames = new ArrayList<>();

    // frequency of each word
    private Map<String, Integer> commonWords = new HashMap<>();

    // frequency of words of specific length
    private List<Pair<String, Integer>> commonWordsFour = new ArrayList<>();
    private List<Pair<String, Integer>> commonWordsFive = new ArrayList<>();
    private List<Pair<String, Integer>> commonWordsSix = new ArrayList<>();
    private List<Pair<String, Integer>> commonWordsSeven = new ArrayList<>();
    private List<Pair<String, Integer>> commonWordsEight = new ArrayList<>();
    private List<Pair<String, Integer>> commonWordsNine = new ArrayList<>();

    /**
     * Adds a word to user's used words
     * @param key the word
     * @param value the frequency of the word
     */
    public void addCommonWord(String key, Integer value) {
        commonWords.put(key, value);
    }

    /**
     * Adds a nickname to the list of user's nicknames
     * @param nickname the nickname to be added
     */
    public void addNickname(String nickname) {
        nicknames.add(nickname);
    }

    /*
     * Incrementers
     */
    public void incrSpamWords(int words) {
        spamWordsSent += words;
    }

    public void incrSpamChars(int chars) {
        spamCharsSent += chars;
    }

    public void incrReactionsReceived(int reactions) {
        reactionsReceived += reactions;
    }

    public void incrPhotoChanged() {
        photoChanged++;
    }

    public void incrNamesChanged() {
        namesChanged++;
    }

    public void incrGroupChanged() {
        groupNameChanged++;
    }

    public void incrThemeChanged() {
        themeChanged++;
    }

    public void incrReactionsSent() {
        reactionsSent++;
    }

    /*
     * Getters and Setters
     */

    public int getMessagesSent() {
        return messagesSent;
    }

    public void setMessagesSent(int messagesSent) {
        this.messagesSent = messagesSent;
    }

    public long getCharsSent() {
        return charsSent;
    }

    public void setCharsSent(long charsSent) {
        this.charsSent = charsSent;
    }

    public long getWordsSent() {
        return wordsSent;
    }

    public void setWordsSent(long wordsSent) {
        this.wordsSent = wordsSent;
    }

    public List<Pair<String, Integer>> getCommonWordsFour() {
        return commonWordsFour;
    }

    public void setCommonWordsFour(List<Pair<String, Integer>> commonWordsFour) {
        this.commonWordsFour = commonWordsFour;
    }

    public List<Pair<String, Integer>> getCommonWordsFive() {
        return commonWordsFive;
    }

    public void setCommonWordsFive(List<Pair<String, Integer>> commonWordsFive) {
        this.commonWordsFive = commonWordsFive;
    }

    public List<Pair<String, Integer>> getCommonWordsSix() {
        return commonWordsSix;
    }

    public void setCommonWordsSix(List<Pair<String, Integer>> commonWordsSix) {
        this.commonWordsSix = commonWordsSix;
    }

    public List<Pair<String, Integer>> getCommonWordsSeven() {
        return commonWordsSeven;
    }

    public void setCommonWordsSeven(List<Pair<String, Integer>> commonWordsSeven) {
        this.commonWordsSeven = commonWordsSeven;
    }

    public List<Pair<String, Integer>> getCommonWordsEight() {
        return commonWordsEight;
    }

    public void setCommonWordsEight(List<Pair<String, Integer>> commonWordsEight) {
        this.commonWordsEight = commonWordsEight;
    }

    public Map<String, Integer> getCommonWords() {
        return commonWords;
    }

    public List<String> getNicknames() {
        return nicknames;
    }

    public int getNamesChanged() {
        return namesChanged;
    }

    public void setNicknames(List<String> nicknames) {
        this.nicknames = nicknames;
    }

    public int getGroupNameChanged() {
        return groupNameChanged;
    }

    public int getPhotoChanged() {
        return photoChanged;
    }

    public int getThemeChanged() {
        return themeChanged;
    }

    public double getAverageWords() {
        return averageWords;
    }

    public void setAverageWords(double averageWords) {
        this.averageWords = averageWords;
    }

    public double getAverageChars() {
        return averageChars;
    }

    public void setAverageChars(double averageChars) {
        this.averageChars = averageChars;
    }

    public void incrSpamMessages() {
        spamMessagesSent++;
    }

    public int getSpamMessagesSent() {
        return spamMessagesSent;
    }

    public int getSpamWordsSent() {
        return spamWordsSent;
    }

    public int getSpamCharsSent() {
        return spamCharsSent;
    }

    public int getReactionsSent() {
        return reactionsSent;
    }

    public int getReactionsReceived() {
        return reactionsReceived;
    }

    public List<Pair<String, Integer>> getCommonWordsNine() {
        return commonWordsNine;
    }

    public void setCommonWordsNine(List<Pair<String, Integer>> commonWordsNine) {
        this.commonWordsNine = commonWordsNine;
    }
}
