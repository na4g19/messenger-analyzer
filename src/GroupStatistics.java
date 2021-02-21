import javafx.util.Pair;

import java.util.*;

/**
 * Deals with statistical data of the entire group
 */
public class GroupStatistics {

    // Tracks first occurrence of word being analysed
    private MessageFormat firstOccurrence;
    private String creationDate;
    private String statCreationDate;
    private int period;

    private List<String> userNames;
    private Map<String, UserStatistics> userStats = new HashMap<>();
    private List<String> groupNames = new ArrayList<>();

    private List<Pair<Integer, Integer>> hourlyMessages = new ArrayList<>();
    private List<Pair<String, Integer>> messagesEachDay = new ArrayList<>();
    private List<Pair<String, Integer>> messagesEachMonth = new ArrayList<>();
    private List<Pair<String, Integer>> wordStatistics = new ArrayList<>();

    /**
     * Creates group statistics for specified users in the group
     * @param names the names of the users who's statistics will be analysed
     */
    public GroupStatistics(List<String> names) {

        userNames = names;
        for(int index = 0; index < userNames.size(); index++) {
            userStats.put(userNames.get(index), new UserStatistics());
        }
    }

    /**
     * Calculates the number of messages sent
     * @return the number of messages sent
     */
    public int getMessagesSent() {

        int sum = 0;

        for(String user : userNames) {

            sum += getUserStats().get(user).getMessagesSent();
        }

        return sum;
    }

    /**
     * Adds a name to the list of group names
     * @param name the name to be added
     */
    public void addGroupName(String name) {
        groupNames.add(name);
    }

    /*
     * Getters and Setters
     */
    public Map<String, UserStatistics> getUserStats() {
        return userStats;
    }

    public List<String> getGroupNames() {
        return groupNames;
    }

    public void setGroupNames(List<String> groupNames) {
        this.groupNames = groupNames;
    }

    public List<Pair<String, Integer>> getMessagesEachDay() {
        return messagesEachDay;
    }

    public void setMessagesEachDay(List<Pair<String, Integer>> messagesEachDay) {
        this.messagesEachDay = messagesEachDay;
    }

    public List<Pair<String, Integer>> getMessagesEachMonth() {
        return messagesEachMonth;
    }

    public void setMessagesEachMonth(List<Pair<String, Integer>> messagesEachMonth) {
        this.messagesEachMonth = messagesEachMonth;
    }

    public List<Pair<String, Integer>> getWordStatistics() {
        return wordStatistics;
    }

    public void setWordStatistics(List<Pair<String, Integer>> wordStatistics) {
        this.wordStatistics = wordStatistics;
    }

    public List<Pair<Integer, Integer>> getHourlyMessages() {
        return hourlyMessages;
    }

    public void setHourlyMessages(List<Pair<Integer, Integer>> hourlyMessages) {
        this.hourlyMessages = hourlyMessages;
    }

    public MessageFormat getFirstOccurrence() {
        return firstOccurrence;
    }

    public void setFirstOccurrence(MessageFormat firstOccurrence) {
        this.firstOccurrence = firstOccurrence;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getStatCreationDate() {
        return statCreationDate;
    }

    public void setStatCreationDate(String statCreationDate) {
        this.statCreationDate = statCreationDate;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }
}
