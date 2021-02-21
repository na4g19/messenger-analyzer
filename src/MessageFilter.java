import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.*;

/**
 * Filters out spam and automatically generated messages
 */
public class MessageFilter {

    private List<MessageFormat> spamMessages = new ArrayList<>();

    // Automatically generated messages
    private List<MessageFormat> infoMessages = new ArrayList<>();

    // The phrases that appear in automatically generated messages
    private List<String> filterKeywords = new ArrayList<>();

    // Other names for a user
    private Map<String, List<String>> nameAliases = new HashMap<>();

    /**
     * Filters out spam and automatically generated messages
     * @param messages the messages to be filtered out
     */
    public void filter(List<MessageFormat> messages) {

        filterInfoMsgs(messages);
        filterSpam(messages);
        filterOthers(messages);
    }

    /**
     * Filters out automatically generated messages
     * @param messages the messages to be filtered out
     */
    private void filterInfoMsgs(List<MessageFormat> messages) {

        addKeywords();
        addAliases();

        correctInfoMessages(messages);

        ListIterator<MessageFormat> messagesIt = messages.listIterator();

        while(messagesIt.hasNext()) {

            MessageFormat message = messagesIt.next();

            nextMessage:
            for(String keyword : filterKeywords) {

                for(String name : nameAliases.keySet()) {

                    if(message.getContent().startsWith(name + " " + keyword)) {

                        infoMessages.add(message);
                        messagesIt.remove();
                        break nextMessage;
                    }
                }
            }
        }
    }

    /**
     * Changes the name aliases to actual name in automatically generated messages
     * @param messages all messages
     */
    private void correctInfoMessages(List<MessageFormat> messages) {

        for(MessageFormat message : messages) {

            nextMessage:
            for(String keyword : filterKeywords) {

                for(String user : nameAliases.keySet()) {

                    for (String name : nameAliases.get(user)) {

                        if (message.getContent().startsWith(name + " " + keyword) &&
                            !message.getContent().startsWith(user + " " + keyword)) {

                            message.setContent(user + " " + message.getContent().substring(name.length() + 1));
                            break nextMessage;
                        }
                    }
                }
            }
        }
    }

    /**
     * Loads filter keywords from file
     */
    private void addKeywords() {

        String line;

        try(BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(
                "C:\\keywords.txt")))) {

            while((line = input.readLine()) != null) {
                filterKeywords.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads name aliases from file
     */
    private void addAliases() {

        JSONParser parser = new JSONParser();

        try {

            JSONObject data = (JSONObject) parser.parse(new FileReader( new File(
                    "C:\\nameAliases.json")));

            JSONArray users = (JSONArray) data.get("users");

            for(Object userObj : users) {

                JSONObject user = (JSONObject) userObj;
                nameAliases.put((String) user.get("name"), new ArrayList<>());
                JSONArray aliases = (JSONArray) user.get("aliases");

                for(Object aliasObj : aliases) {

                    JSONObject alias = (JSONObject) aliasObj;
                    nameAliases.get((String) user.get("name")).add((String) alias.get("alias"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Filters out spam messages
     * @param messages the messages to be filtered out
     */
    private void filterSpam(List<MessageFormat> messages) {

        // Number of messages following and previous to the current message
        final int SAMPLE_SIZE = 5;
        List<String> sample;

        // Test small sample of messages for similarities
        for(int index = 0; index < messages.size(); index++) {

            sample = new ArrayList<>();

            if(index > SAMPLE_SIZE && index + SAMPLE_SIZE < messages.size()) {

                for(int samplePos = index - SAMPLE_SIZE; samplePos <= index + SAMPLE_SIZE; samplePos++) {

                    if(samplePos != index) {
                        sample.add(messages.get(samplePos).getContent());
                    }
                }
            }

            String[] sampleArray = new String[sample.size()];
            sampleArray = sample.toArray(sampleArray);

            if(isSpam(messages.get(index).getContent(), sampleArray)) {
                spamMessages.add(messages.get(index));
            }
        }

        messages.removeIf(message -> spamMessages.contains(message));
    }

    /**
     * Removes messages that are sent by users who's statistics are not being tracked
     * @param messages all the messages sent
     */
    private void filterOthers(List<MessageFormat> messages) {

        ListIterator<MessageFormat> messagesIterator = messages.listIterator();
        boolean toRemove;

        while(messagesIterator.hasNext()) {

            MessageFormat message = messagesIterator.next();
            toRemove = true;

            for(String user : nameAliases.keySet()) {

                if(message.getSender().equals(user)) {
                    toRemove = false;
                }
            }

            if(toRemove) {
                messagesIterator.remove();
            }
        }
    }

    /**
     * Tests if a message is considered as spam
     * @param message the message to be tested
     * @param messages past and future messages
     * @return true if message is spam, false otherwise
     */
    private boolean isSpam(String message, String[] messages) {

        return isOneLetterWord(message) || doWordsRepeat(message) || doesMessageRepeat(message, messages) || message.isEmpty();
    }

    /**
     * Determines whether a message consisting of one word is considered as spam
     * @param message the message to be evaluated
     * @return true if the message is spam, false otherwise
     */
    private boolean isOneLetterWord(String message) {

        Map<Character, Integer> charMap = new HashMap<>();

        if(message.split("\\s+").length == 1) {

            for(char ch : message.toCharArray()) {

                if(charMap.containsKey(ch)) {
                    charMap.put(ch, charMap.get(ch) + 1);
                } else {
                    charMap.put(ch, 1);
                }
            }

            for(Character ch : charMap.keySet()) {

                if(message.length() > 80 && (double) charMap.get(ch) / message.length() * 100 > 80) {
                    return true;
                }
            }

            return false;

        } else {
            return false;
        }
    }

    /**
     * Determines whether a message is spam based on frequency and diversity of words in message
     * @param message the string to be evaluated
     * @return true if message is spam, false otherwise
     */
    private boolean doWordsRepeat(String message) {

        String[] words = message.split("\\s+");
        Map<String, Integer> wordMap = getFrequencies(message);

        return words.length > 2 && wordMap.size() * 2 <= words.length;
    }

    /**
     * Determines whether a message is spam based on whether messages following it are similar
     * @param message the message to be evaluated for spam
     * @param messages the messages following and the messages before the message that's being evaluated
     * @return true if the message is spam, false otherwise
     */
    private boolean doesMessageRepeat(String message, String[] messages) {

        Map<String, Integer> wordMap = getFrequencies(message);
        Map<String, Integer> nextMsgMap;

        for(String msg : messages) {

            int commonWords = 0;

            nextMsgMap = getFrequencies(msg);

            for(String key : wordMap.keySet()) {

                if(nextMsgMap.containsKey(key)) {
                    commonWords++;
                }
            }

            if(message.length() > 10 && wordMap.size() > 1 &&
                    (double) commonWords / Math.max(wordMap.size(), nextMsgMap.size()) * 100 > 95) {
                return true;
            }
        }

        return false;
    }

    /**
     * Calculates the frequencies of each word in a string
     * @param message the string to be evaluated
     * @return the mapping of each word to the times it occurs in the string
     */
    private Map<String, Integer> getFrequencies(String message) {

        Map<String, Integer> wordMap = new HashMap<>();
        String[] words = message.split("\\s+");

        for(String word : words) {

            if(wordMap.containsKey(word)) {
                wordMap.put(word, wordMap.get(word) + 1);
            } else {
                wordMap.put(word, 1);
            }
        }

        return wordMap;
    }

    /*
     * Getters
     */
    public List<MessageFormat> getInfoMessages() {
        return infoMessages;
    }

    public Map<String, List<String>> getNameAliases() {
        return nameAliases;
    }

    public List<MessageFormat> getSpamMessages() {
        return spamMessages;
    }
}
