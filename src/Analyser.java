import javafx.util.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Analyser {

    // All messages sent
    private List<MessageFormat> messages = new ArrayList<>();

    // Messages classified as spam
    private List<MessageFormat> spamMessages;

    // Messages that aren't written by any user
    private List<MessageFormat> infoMessages;

    // Informative messages about the change of state for some property
    private List<MessageFormat> nameChangeMessages = new ArrayList<>();
    private List<MessageFormat> groupChangeMessages = new ArrayList<>();
    private List<MessageFormat> photoChangeMessages = new ArrayList<>();
    private List<MessageFormat> themeChangeMessages = new ArrayList<>();

    // Users participating in chat
    private List<String> userNames;

    // Dates between the first and last message
    private List<Date> chatDates;

    private GroupStatistics statistics;

    // Keywords that determine whether a message is classified as a nameChangeMessage
    private final String[] NAME_CHANGE_KEYWORDS = new String[] {
            "pakeitė savo pravardę į", "pakeitė tavo vartotojo vardą į", "pakeitė Mykolas Lekavičius vardą į",
            "pakeitė Dominykas Simpukas vardą į", "pakeitėte vartotojo vardą iš Dominykas Simpukas į",
            "pakeitėte vartotojo vardą iš Mykolas Lekavičius į", "set your nickname to"
    };

    // Keywords that determine whether a message is classified as a groupChangeMessage
    private final String[] GROUP_CHANGE_KEYWORDS = new String[] {
            "pavadino grupę", "pavadinote grupę"
    };

    // Keywords that determine whether a message is classified as a photoChangeMessage
    private final String[] PHOTO_CHANGE_KEYWORDS = new String[] {
            "pakeitė grupės nuotrauką.", "pakeitėte grupės nuotrauką"
    };

    // Keywords that determine whether a message is classified as a themeChangeMessage
    private final String[] THEME_CHANGE_KEYWORDS = new String[] {
            "changed the chat theme to"
    };

    /**
     * Analyses the messages
     * @param files Messenger message files in JSON format
     */
    private Analyser(List<File> files) {

        JSONRepair.repairJSON(files);
        readJSON(files);

        // Filters out spam and informative messages
        MessageFilter filter = new MessageFilter();
        filter.filter(messages);

        infoMessages = filter.getInfoMessages();
        spamMessages = filter.getSpamMessages();
        userNames = new ArrayList<>(filter.getNameAliases().keySet());

        statistics = new GroupStatistics(userNames);

        getTypeMessages(GROUP_CHANGE_KEYWORDS, groupChangeMessages);
        getTypeMessages(NAME_CHANGE_KEYWORDS, nameChangeMessages);
        getTypeMessages(PHOTO_CHANGE_KEYWORDS, photoChangeMessages);
        getTypeMessages(THEME_CHANGE_KEYWORDS, themeChangeMessages);

        chatDates = getChatDates();
        analyse();
    }

    /**
     * Analyses the messages
     */
    private void analyse() {

        getMessagesSent();
        getWordsSent();
        getCharsSent();

        findWordFrequency();
        getCommonWords();
        allocateNicknames();

        getGroupNames();
        getPhotosChanged();
        getThemesChanged();

        getAverageChars();
        getAverageWords();
        getSpamMessages();

        getReactions();
        messagesEachDay();
        messagesEachMonth();

        getWordStatistics("seni");
        getHourlyMessages();
        getCreationDate();

        getStatCreationDate();
        getPeriodBetween();
    }

    /**
     * Calculates the number of days between the first and last message
     */
    private void getPeriodBetween() {

        String from = statistics.getCreationDate().substring(0, 10);
        String to = statistics.getStatCreationDate().substring(0, 10);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate fromDate = LocalDate.parse(from, dtf);
        LocalDate toDate = LocalDate.parse(to, dtf);
        long daysBetween = ChronoUnit.DAYS.between(fromDate, toDate);
        statistics.setPeriod((int) daysBetween);
    }

    /**
     * Gets the dates between two given dates
     * @param startDate the date to count from
     * @param endDate the date to count to
     * @return the list of dates
     */
    private List<Date> getDatesBetween(Date startDate, Date endDate) {

        List<Date> datesInRange = new ArrayList<>();
        Calendar calendar = new GregorianCalendar();
        Calendar endCalendar = new GregorianCalendar();

        calendar.setTime(startDate);
        endCalendar.setTime(endDate);

        while(calendar.before(endCalendar)) {
            Date result = calendar.getTime();
            datesInRange.add(result);
            calendar.add(Calendar.DATE, 1);
        }

        return datesInRange;
    }

    /**
     * Calculates the time the first message was sent
     */
    private void getCreationDate() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(messages.get(messages.size() - 1).getTimestamp());
        statistics.setCreationDate(dateFormat.format(date));
    }

    /**
     * Calculates the time the last message was sent
     */
    private void getStatCreationDate() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(messages.get(0).getTimestamp());
        statistics.setStatCreationDate(dateFormat.format(date));
    }

    /**
     * Returns the dates between the first and last message in the chat
     * @return list of dates between the first and last message
     */
    private List<Date> getChatDates() {

        Date endDate = new Date(messages.get(0).getTimestamp());
        Date startDate = new Date(messages.get(messages.size() - 1).getTimestamp());

        return getDatesBetween(startDate, endDate);
    }

    /**
     * Calculates the number of messages sent each hour
     */
    private void getHourlyMessages() {

        // Number of messages sent each hour
        Map<Integer, Integer> hoursToMessages = new HashMap<>();

        // Initialise each hour to zero
        for(int i = 0; i < 24; i++) {
            hoursToMessages.put(i, 0);
        }

        // For each message
        for(MessageFormat message : messages) {

            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(message.getTimestamp());

            LocalTime nowTime = LocalTime.of(calendar.get(
                    Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    calendar.get(Calendar.SECOND));

            LocalTime beforeTime = LocalTime.of(0, 0, 0);
            LocalTime afterTime = LocalTime.of(1, 0, 0);

            // For each hour
            for(int i = 0; i < 24; i++) {

                // if the message was sent between two given hours
                if(afterTime.getHour() == 0 && nowTime.isAfter(beforeTime) ||
                 nowTime.isAfter(beforeTime) && nowTime.isBefore(afterTime)) {

                    // Increment messages sent that hour
                    hoursToMessages.put(beforeTime.getHour(), hoursToMessages.get(beforeTime.getHour()) + 1);
                }

                // Go to next hour
                beforeTime = beforeTime.plusHours(1);
                afterTime = afterTime.plusHours(1);
            }
        }

        List<Pair<Integer, Integer>> hourlyMessages = new ArrayList<>();

        for(Map.Entry entry : hoursToMessages.entrySet()) {
            hourlyMessages.add(new Pair(entry.getKey(), entry.getValue()));
        }

        hourlyMessages.sort(Comparator.comparing(Pair::getKey));
        statistics.setHourlyMessages(hourlyMessages);
    }

    /**
     * Calculates the number of messages each day
     */
    private void messagesEachDay() {

        // Number of messages sent each day
        Map<String, Integer> messagesPerDay = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for(Date date : chatDates) {
            messagesPerDay.put(dateFormat.format(date), 0);
        }

        for(MessageFormat message : messages) {
            String dateString = dateFormat.format(new Date(message.getTimestamp()));
            messagesPerDay.put(dateString, messagesPerDay.get(dateString) + 1);
        }

        List<Pair<String, Integer>> messagesList = new ArrayList<>();

        for(Map.Entry entry : messagesPerDay.entrySet()) {
            messagesList.add(new Pair(entry.getKey(), entry.getValue()));
        }

        messagesList.sort(Comparator.comparing(Pair::getKey));
        statistics.setMessagesEachDay(messagesList);
    }

    /**
     * Calculates the number of messages each month
     */
    private void messagesEachMonth() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
        Map<String, Integer> messagesPerMonth = new HashMap<>();

        for(Date date : chatDates) {
            messagesPerMonth.put(dateFormat.format(date), 0);
        }

        for(MessageFormat message : messages) {

            String dateString = dateFormat.format(new Date(message.getTimestamp()));
            messagesPerMonth.put(dateString, messagesPerMonth.get(dateString) + 1);
        }

        List<Pair<String, Integer>> messagesList = new ArrayList<>();

        for(Map.Entry entry : messagesPerMonth.entrySet()) {
            messagesList.add(new Pair(entry.getKey(), entry.getValue()));
        }

        messagesList.sort(Comparator.comparing(Pair::getKey));
        statistics.setMessagesEachMonth(messagesList);
    }

    /**
     * Finds the number of times the specified word is used each day
     * @param searchWord the word to be analysed
     */
    private void getWordStatistics(String searchWord) {

        String wordRegex = "";
        MessageFormat firstOccurrence = null;

        // Constructs a regex that allows a word to have repeating letters e.g. "word" -> "wwooorrd"
        for(char c : searchWord.toLowerCase().toCharArray()) {
            wordRegex += c + "+";
        }

        // Maps day to the number of times the word was used that day
        Map<String, Integer> wordStats = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for(Date date : chatDates) {
            wordStats.put(dateFormat.format(date), 0);
        }

        // For each message
        for(MessageFormat message : messages) {

            // Get the words of the message
            String[] words = message.getContent().split("\\s+");

            // For each word
            for(String word : words) {

                // Check if it matches the specified word
                if(word.toLowerCase().matches(wordRegex)) {

                    // Increment the number of occurrences on the day the word was used
                    String dateString = dateFormat.format(new Date(message.getTimestamp()));
                    wordStats.put(dateString, wordStats.get(dateString) + 1);
                    // FIXME: 1/20/2021 ????
                    firstOccurrence = message;
                }
            }
        }

        List<Pair<String, Integer>> wordFreq = new ArrayList<>();

        for(Map.Entry entry : wordStats.entrySet()) {
            wordFreq.add(new Pair(entry.getKey(), entry.getValue()));
        }

        wordFreq.sort(Comparator.comparing(Pair::getKey));
        statistics.setWordStatistics(wordFreq);
        statistics.setFirstOccurrence(firstOccurrence);
    }

    /**
     * Calculates reactions sent by each person
     */
    private void getReactions() {

        for(MessageFormat message : messages) {

            // If message has a reaction
            if(!message.getReactions().isEmpty()) {

                // Increment received reactions
                statistics.getUserStats().get(message.getSender()).incrReactionsReceived(message.getReactions().size());

                // For each reaction increment reactions sent by that person
                for(MessageFormat.Reaction reaction : message.getReactions()) {

                    for(String user : userNames) {

                        if(reaction.getSender().equals(user)) {
                            statistics.getUserStats().get(user).incrReactionsSent();
                        }
                    }
                }
            }
        }
    }

    /**
     * Calculates each user's spam statistics
     */
    private void getSpamMessages() {

        for(MessageFormat message : spamMessages) {

            for(String user : userNames) {

                if(message.getSender().equals(user)) {

                    statistics.getUserStats().get(user).incrSpamMessages();
                    statistics.getUserStats().get(user).incrSpamWords(message.getContent().split("\\s+").length);
                    statistics.getUserStats().get(user).incrSpamChars(message.getContent().length());
                }
            }
        }
    }

    /**
     * Calculates each user's average message length in words
     */
    private void getAverageWords() {

        for(String user : userNames) {

            UserStatistics stats = statistics.getUserStats().get(user);
            stats.setAverageWords((double) stats.getWordsSent() / stats.getMessagesSent());
        }
    }

    /**
     * Calculates each user's average message length in characters
     */
    private void getAverageChars() {

        for(String user : userNames) {

            UserStatistics stats = statistics.getUserStats().get(user);
            stats.setAverageChars((double) stats.getCharsSent() / stats.getWordsSent());
        }
    }

    /**
     * Calculates the number of times each user has changed the theme
     */
    private void getThemesChanged() {

        for(MessageFormat message : themeChangeMessages) {

            for(String keyword : THEME_CHANGE_KEYWORDS) {

                for(String user : userNames) {

                    if(message.getContent().startsWith(user + " " + keyword)) {
                        statistics.getUserStats().get(user).incrThemeChanged();
                    }
                }
            }
        }
    }

    /**
     * Calculates the number of times each user has changed the group photo
     */
    private void getPhotosChanged() {

        for(MessageFormat message : photoChangeMessages) {

            for(String keyword : PHOTO_CHANGE_KEYWORDS) {

                for(String user : userNames) {

                    if(message.getContent().startsWith(user + " " + keyword)) {
                        statistics.getUserStats().get(user).incrPhotoChanged();
                    }
                }
            }
        }
    }

    /**
     * Builds up a list of informative messages of specific type.
     * @param keywords the keywords that the messages of that type start with
     * @param typeMessages the list that holds the messages of the type
     */
    private void getTypeMessages(String[] keywords, List<MessageFormat> typeMessages) {

        for(MessageFormat message : infoMessages) {

            nextMessage:
            for(String keyword : keywords) {

                for (String name : userNames) {

                    if (message.getContent().startsWith(name + " " + keyword)) {
                        typeMessages.add(message);

                        // No other keyword can match the same message
                        break nextMessage;
                    }
                }

            }
        }
    }

    /**
     * Gets all previous group names
     */
    private void getGroupNames() {

        for(MessageFormat message : groupChangeMessages) {

            for(String keyword : GROUP_CHANGE_KEYWORDS) {

                for(String user : userNames) {

                    if(message.getContent().startsWith(user + " " + keyword)) {

                        statistics.addGroupName(getNamePart(message.getContent(), user + " " + keyword));

                        statistics.getUserStats().get(user).incrGroupChanged();
                    }
                }
            }
        }

        List<String> sortedList = removeDuplicates(statistics.getGroupNames());
        sortedList.sort(Comparator.comparing(String::length));

        statistics.setGroupNames(sortedList);
    }

    /**
     * Assigns previous nicknames to users
     */
    private void allocateNicknames() {

        for(MessageFormat message : nameChangeMessages) {

            for(String keyword : NAME_CHANGE_KEYWORDS) {

                for(String user : userNames) {

                    if(message.getContent().startsWith(user + " " + keyword)) {

                        statistics.getUserStats().get(determineUser(keyword, user))
                                .addNickname(getNamePart(message.getContent(), user + " " + keyword));

                        statistics.getUserStats().get(user).incrNamesChanged();
                    }
                }
            }
        }

        for(String user : userNames) {

            List<String> sortedList = removeDuplicates(statistics.getUserStats().get(user).getNicknames());
            sortedList.sort(Comparator.comparing(String::length));

            statistics.getUserStats().get(user).setNicknames(sortedList);
        }
    }

    /**
     * Removes duplicates from a list
     * @param list the list from which the duplicates need to be removed
     * @return list without the duplicates
     */
    private List<String> removeDuplicates(List<String> list) {

        Map<String, Integer> duplicateMap = new HashMap<>();

        for(String member : list) {
            duplicateMap.put(member, 1);
        }

        return new ArrayList<>(duplicateMap.keySet());
    }

    /**
     * Returns the name of the user that the automatically generated message addresses
     * @param keyword the automatically generated message
     * @param name the sender
     * @return the name of the user
     */
    private String determineUser(String keyword, String name) {

        switch(keyword) {

            case "pakeitė savo pravardę į" :
                return name;
            case "pakeitė tavo vartotojo vardą į" :
                return "Nedas Aravičius";
            case "set your nickname to" :
                return "Nedas Aravičius";
            default :
                return findName(keyword);
        }
    }

    /**
     * Finds the name of the user in the automatically generated message
     * @param message the full message to be checked
     * @return the name found or null if no name exists in the message
     */
    private String findName(String message) {

        for(String user : userNames) {

            if(message.contains(user)) {
                return user;
            }
        }

        return null;
    }

    /**
     * Extracts the name from the message
     * @param message the message that includes the name
     * @param beginning index in the message where the name starts
     * @return the name extracted
     */
    private String getNamePart(String message, String beginning) {

        String name = message.substring(beginning.length() + 1);
        return name.endsWith(".") ? name.substring(0, name.length() - 1) : name;
    }

    /**
     * Returns the value of JSON object if its not null
     * @param message JSON object representation of a message
     * @param property the property of the JSON object
     * @return if property isn't null - the value of property, empty string otherwise
     */
    private String safeInsert(JSONObject message, String property) {
        return message.get(property) == null ? "" : (String) message.get(property);
    }

    /**
     * Reads the JSON files to create a list of messages
     * @param files JSON files containing messages
     */
    private void readJSON(List<File> files) {

        JSONParser parser = new JSONParser();

        for(File file : files) {

            try {

                JSONObject data = (JSONObject) parser.parse(new FileReader(file));
                JSONArray participants = (JSONArray) data.get("participants");
                JSONArray messages = (JSONArray) data.get("messages");

                for(Object messageObject : messages) {

                    JSONObject message = (JSONObject) messageObject;
                    MessageFormat messageFormat = new MessageFormat();

                    messageFormat.setSender((String) message.get("sender_name"));
                    messageFormat.setTimestamp((long) message.get("timestamp_ms"));
                    messageFormat.setContent(safeInsert(message, "content"));
                    messageFormat.setType((String) message.get("type"));

                    JSONArray users = (JSONArray) message.get("users");

                    // if message refers to some users
                    if(users != null) {

                        for(Object userObject : users) {

                            JSONObject user = (JSONObject) userObject;
                            messageFormat.addUser((String) user.get("name"));
                        }
                    }

                    JSONArray photos = (JSONArray) message.get("photos");

                    // if message contains photos
                    if(photos != null) {

                        for(Object photoObject : photos) {

                            JSONObject photo = (JSONObject) photoObject;
                            messageFormat.addPhoto((String) photo.get("uri"));

                        }
                    }

                    JSONArray reactions = (JSONArray) message.get("reactions");

                    // if message contains reactions
                    if(reactions != null) {

                        for(Object reactionObj : reactions) {

                            JSONObject reaction = (JSONObject) reactionObj;

                            if(reaction.get("reaction") != null && reaction.get("actor") != null) {
                                messageFormat.addReaction((String) reaction.get("reaction"), (String) reaction.get("actor"));
                            }
                        }
                    }

                    this.messages.add(messageFormat);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Counts the number of messages sent by each user
     */
    private void getMessagesSent() {

        Map<String, Integer> messageCount = new HashMap<>();

        for(MessageFormat message : messages) {

            if(messageCount.containsKey(message.getSender())) {
                messageCount.put(message.getSender(), messageCount.get(message.getSender()) + 1);
            } else {
                messageCount.put(message.getSender(), 1);
            }
        }

        for(String user : userNames) {
            statistics.getUserStats().get(user).setMessagesSent(messageCount.get(user));
        }
    }

    /**
     * Counts the number of words sent by each user
     */
    private void getWordsSent() {

        Map<String, Integer> wordCount = new HashMap<>();

        for(MessageFormat message : messages) {

            if(wordCount.containsKey(message.getSender())) {
                wordCount.put(message.getSender(), wordCount.get(message.getSender()) +
                        message.getContent().split("\\s+").length);
            } else {
                wordCount.put(message.getSender(), message.getContent().split("\\s+").length);
            }
        }

        for(String user : userNames) {
            statistics.getUserStats().get(user).setWordsSent(wordCount.get(user));
        }
    }

    /**
     * Counts the number of characters sent by each user
     */
    private void getCharsSent() {

        Map<String, Integer> charCount = new HashMap<>();

        for(MessageFormat message : messages) {

            if(charCount.containsKey(message.getSender())) {
                charCount.put(message.getSender(), charCount.get(message.getSender()) + message.getContent().length());
            } else {
                charCount.put(message.getSender(), message.getContent().length());
            }
        }

        for(String user : userNames) {
            statistics.getUserStats().get(user).setCharsSent(charCount.get(user));
        }
    }

    /**
     * Calculates the frequency of each word
     */
    private void findWordFrequency() {

        Integer count;

        for(String user : userNames) {

            for (MessageFormat message : messages) {

                if(message.getSender().equals(user)) {

                    String[] words = message.getContent().toLowerCase().split("\\s+");

                    for (String word : words) {

                        count = statistics.getUserStats().get(user).getCommonWords().get(word);
                        count = (count == null) ? 1 : ++count;
                        statistics.getUserStats().get(user).addCommonWord(word, count);
                    }
                }
            }
        }
    }

    /**
     * Finds the top N most used words of specified length
     * @param wordFrequency the frequency of each word
     * @param wordLength the length of the words
     * @param topN the number of words to find
     * @return a list of most used words and their frequency
     */
    private List<Pair<String, Integer>> findCommonWords(Map<String, Integer> wordFrequency, int wordLength, int topN) {

        Map<String, Integer> words = new HashMap<>();

        for(Map.Entry<String, Integer> entry : wordFrequency.entrySet()) {

            if(entry.getKey().length() == wordLength) {
                words.put(entry.getKey(), entry.getValue());
            }
        }

        // sorts the words
        List<Map.Entry<String, Integer>> list = new ArrayList<>(words.entrySet());
        list.sort(Map.Entry.comparingByValue());
        Collections.reverse(list);

        words = new LinkedHashMap<>();
        for(Map.Entry<String, Integer> entry : list) {
            words.put(entry.getKey(), entry.getValue());
        }

        Set<String> keys = words.keySet();
        String[] keysArray = keys.toArray(new String[keys.size()]);
        List<Pair<String, Integer>> returnList = new ArrayList<>();

        for(int index = 0; index < topN; index++) {
            returnList.add(new Pair(keysArray[index], wordFrequency.get(keysArray[index])));
        }

        return returnList;
    }

    /**
     * Gets most used words for different lengths
     */
    private void getCommonWords() {

        for(String user : userNames) {

            UserStatistics currStats = statistics.getUserStats().get(user);

            currStats.setCommonWordsFour(findCommonWords(currStats.getCommonWords(),4, 5));
            currStats.setCommonWordsFive(findCommonWords(currStats.getCommonWords(),5, 5));
            currStats.setCommonWordsSix(findCommonWords(currStats.getCommonWords(),6, 5));
            currStats.setCommonWordsSeven(findCommonWords(currStats.getCommonWords(),7, 5));
            currStats.setCommonWordsEight(findCommonWords(currStats.getCommonWords(),8, 5));
            currStats.setCommonWordsNine(findCommonWords(currStats.getCommonWords(),9, 5));
        }
    }

    public static void main(String[] args) {

        List<File> files = new ArrayList<>();
        File dir = new File("C:\\file_location");

        File[] JSONFiles = dir.listFiles();

        if(JSONFiles != null && JSONFiles.length > 0) {

            for(File file : JSONFiles) {

                if(file.getName().endsWith(".json")) {
                    files.add(file);
                }
            }
        }

        // FIXME: 21/02/2021 add error checking

        // Analyses files
        Analyser analyser = new Analyser(files);

        // Writes statistics to pdf file
        PDFWriter pdfWriter = new PDFWriter(analyser.statistics);
        pdfWriter.writeToPDF();
    }
}
