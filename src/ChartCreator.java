import javafx.util.Pair;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.TextAnchor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Creates charts for user statistics
 */
public class ChartCreator {

    private GroupStatistics statistics;

    private final Font labelFont = new Font("SansSerif", Font.BOLD, 36);
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 56);
    private final Font legendFont = new Font("SansSerif", Font.BOLD, 18);
    private final Font domainFont = new Font("SansSerif", Font.BOLD, 22);
    private final Font rangeFont = new Font("SansSerif", Font.BOLD, 12);
    private final Font axisLabelFont = new Font("SansSerif", Font.BOLD, 30);

    public ChartCreator(GroupStatistics statistics) {
        this.statistics = statistics;
    }

    /**
     * Creates a chart for hourly messages sent
     * @return the Image object of the chart
     */
    public Image hourlyMessagesChart() {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int totalMessages = statistics.getMessagesSent();

        for(Pair<Integer, Integer> entry : statistics.getHourlyMessages()) {
            dataset.setValue(round( (double) entry.getValue() / totalMessages * 100, 4),
                    "", entry.getKey() + " - " + (entry.getKey() + 1));
        }

        JFreeChart chart = ChartFactory.createBarChart("Percentage of Daily Messages Each Hour",
                "Hour", "Percentage", dataset,
                PlotOrientation.VERTICAL, false, false, false);

        chart.getTitle().setFont(titleFont);

        CategoryPlot plot = (CategoryPlot)chart.getPlot();
        plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.BOLD, 16));
        plot.getDomainAxis().setLabelFont(axisLabelFont);
        plot.getRangeAxis().setTickLabelFont(rangeFont);
        plot.getRangeAxis().setLabelFont(axisLabelFont);

        return chart.createBufferedImage(1920, 1080);
    }

    /**
     * Creates a chart for daily messages sent
     * @return the Image object of the chart
     */
    public Image dailyMessagesChart() {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for(Pair<String, Integer> entry : statistics.getMessagesEachDay()) {
            dataset.setValue(entry.getValue(), "", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart("Messages Each Day",
                "Day", "Number of Messages",
                dataset, PlotOrientation.VERTICAL, false, false, false);

        chart.getTitle().setFont(titleFont);
        CategoryPlot plot = (CategoryPlot)chart.getPlot();
        plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.BOLD, 0));

        plot.getDomainAxis().setLabelFont(axisLabelFont);
        plot.getRangeAxis().setTickLabelFont(rangeFont);
        plot.getRangeAxis().setLabelFont(axisLabelFont);

        plot.getDomainAxis().setCategoryMargin(0.0);
        plot.getDomainAxis().setLowerMargin(0.0);
        plot.getDomainAxis().setUpperMargin(0.0);

        CategoryItemRenderer renderer = plot.getRenderer();
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setItemLabelsVisible(false);

        return chart.createBufferedImage(1920, 1080);
    }

    /**
     * Creates a chart for monthly messages sent
     * @return the Image object of the chart
     */
    public Image monthlyMessagesChart() {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for(Pair<String, Integer> entry : statistics.getMessagesEachMonth()) {
            dataset.setValue(entry.getValue(), "", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart("Messages Each Month",
                "Month", "Number of Messages",
                dataset, PlotOrientation.VERTICAL, false, false, false);

        chart.getTitle().setFont(titleFont);
        CategoryPlot plot = (CategoryPlot)chart.getPlot();
        plot.getDomainAxis().setTickLabelFont(domainFont);
        plot.getDomainAxis().setLabelFont(axisLabelFont);
        plot.getRangeAxis().setTickLabelFont(rangeFont);
        plot.getRangeAxis().setLabelFont(axisLabelFont);

        CategoryItemRenderer renderer = plot.getRenderer();
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setItemLabelsVisible(true);
        renderer.setItemLabelFont( new Font("SansSerif", Font.BOLD, 20));

        return chart.createBufferedImage(1920, 1080);
    }

    /**
     * Creates a chart for the frequency of the words
     * @return the Image object of the chart
     */
    public Image wordFrequencyChart() {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for(Pair<String, Integer> entry : statistics.getWordStatistics()) {
            dataset.setValue(entry.getValue(), "", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart("\"seni\" Usage Each Day",
                "", "Times Used Per Day",
                dataset, PlotOrientation.VERTICAL, false, false, false);

        chart.getTitle().setFont(titleFont);
        CategoryPlot plot = (CategoryPlot)chart.getPlot();
        plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.BOLD, 0));
        plot.getDomainAxis().setLabelFont(axisLabelFont);
        plot.getRangeAxis().setTickLabelFont(rangeFont);
        plot.getRangeAxis().setLabelFont(axisLabelFont);

        plot.getDomainAxis().setCategoryMargin(0.0);
        plot.getDomainAxis().setLowerMargin(0.0);
        plot.getDomainAxis().setUpperMargin(0.0);

        BarRenderer renderer = (BarRenderer) ((CategoryPlot) chart.getPlot()).getRenderer();
        renderer.setItemLabelGenerator(new ItemGenerator(statistics.getFirstOccurrence()));
        renderer.setItemLabelsVisible(true);
        renderer.setItemLabelFont( new Font("SansSerif", Font.BOLD, 14));

        // Change domain label orientation
        renderer.setPositiveItemLabelPosition(new ItemLabelPosition(
                ItemLabelAnchor.OUTSIDE2, TextAnchor.BOTTOM_LEFT, TextAnchor.BOTTOM_LEFT, -3.14 / 2));

        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        chart.setBackgroundPaint(Color.lightGray);
        plot.getDomainAxis().setTickLabelFont(new Font("Dialog", Font.PLAIN, 8));

        int skipNth = 0;

        // Show only every fourth domain label
        for(Pair<String, Integer> entry : statistics.getWordStatistics()) {

            if(skipNth % 4 != 0) {
                plot.getDomainAxis().setTickLabelPaint(entry.getKey(), Color.lightGray);
            }

            skipNth++;
        }

        return chart.createBufferedImage(1920, 1080);
    }

    /**
     * Creates a chart for total message sent
     * @return the Image object of the chart
     */
    public Image messagesSendChart() {

        DefaultPieDataset dataset = new DefaultPieDataset();

        for(String user : statistics.getUserStats().keySet()) {
            dataset.setValue(user, statistics.getUserStats().get(user).getMessagesSent());
        }

        JFreeChart chart = ChartFactory.createPieChart("Non-Spam Messages Sent", dataset, false, true, false);

        chart.getTitle().setFont(titleFont);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelFont(labelFont);

        return chart.createBufferedImage(1920, 1080);
    }

    /**
     * Creates a chart for total words sent
     * @return the Image object of the chart
     */
    public Image wordsSendChart() {

        DefaultPieDataset dataset = new DefaultPieDataset();

        for(String user : statistics.getUserStats().keySet()) {
            dataset.setValue(user, statistics.getUserStats().get(user).getWordsSent());
        }

        JFreeChart chart = ChartFactory.createPieChart("Non-Spam Words Sent", dataset, false, true, false);

        chart.getTitle().setFont(titleFont);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelFont(labelFont);

        return chart.createBufferedImage(1920, 1080);
    }

    /**
     * Creates a chart for total characters sent
     * @return the Image object of the chart
     */
    public Image charsSendChart() {

        DefaultPieDataset dataset = new DefaultPieDataset();

        for(String user : statistics.getUserStats().keySet()) {
            dataset.setValue(user, statistics.getUserStats().get(user).getCharsSent());
        }

        JFreeChart chart = ChartFactory.createPieChart("Non-Spam Characters Sent", dataset, false, true, false);

        chart.getTitle().setFont(titleFont);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelFont(labelFont);

        return chart.createBufferedImage(1920, 1080);
    }

    /**
     * Creates a chart for spam messages sent
     * @return the Image object of the chart
     */
    public Image spamMessagesSendChart() {

        DefaultPieDataset dataset = new DefaultPieDataset();

        for(String user : statistics.getUserStats().keySet()) {
            dataset.setValue(user, statistics.getUserStats().get(user).getSpamMessagesSent());
        }

        JFreeChart chart = ChartFactory.createPieChart("Spam Messages Sent", dataset, false, true, false);

        chart.getTitle().setFont(titleFont);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelFont(labelFont);

        return chart.createBufferedImage(1920, 1080);
    }

    /**
     * Creates a chart for spam words sent
     * @return the Image object of the chart
     */
    public Image spamWordsSendChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();

        for(String user : statistics.getUserStats().keySet()) {
            dataset.setValue(user, statistics.getUserStats().get(user).getSpamWordsSent());
        }

        JFreeChart chart = ChartFactory.createPieChart("Spam Words Sent", dataset, false, true, false);

        chart.getTitle().setFont(titleFont);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelFont(labelFont);

        return chart.createBufferedImage(1920, 1080);
    }

    /**
     * Creates a chart for spam characters sent
     * @return the Image object of the chart
     */
    public Image spamCharsSendChart() {

        DefaultPieDataset dataset = new DefaultPieDataset();

        for(String user : statistics.getUserStats().keySet()) {
            dataset.setValue(user, statistics.getUserStats().get(user).getSpamCharsSent());
        }

        JFreeChart chart = ChartFactory.createPieChart("Spam Characters Sent", dataset, false, true, false);

        chart.getTitle().setFont(titleFont);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelFont(labelFont);

        return chart.createBufferedImage(1920, 1080);
    }

    /**
     * Creates a chart for average length of message
     * @return the Image object of the chart
     */
    public Image averageMessageChart() {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for(String user : statistics.getUserStats().keySet()) {
            dataset.setValue(round(statistics.getUserStats().get(user).getAverageWords(), 4),
                    user, "Average Words Per Message");
        }

        for(String user : statistics.getUserStats().keySet()) {
            dataset.setValue(round(statistics.getUserStats().get(user).getAverageChars(), 4),
                    user, "Average Characters Per Message");
        }

        JFreeChart chart = ChartFactory.createBarChart("Average Message", "", "",
                dataset, PlotOrientation.VERTICAL, true, false, false);

        chart.getTitle().setFont(titleFont);
        chart.getLegend().setItemFont(legendFont);

        CategoryPlot plot = (CategoryPlot)chart.getPlot();
        plot.getDomainAxis().setTickLabelFont(domainFont);
        plot.getRangeAxis().setTickLabelFont(rangeFont);

        CategoryItemRenderer renderer = plot.getRenderer();
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setItemLabelsVisible(true);
        renderer.setItemLabelFont(labelFont);

        return chart.createBufferedImage(1920, 1080);
    }

    /**
     * Creates a chart for reactions sent
     * @return the Image object of the chart
     */
    public Image reactionsChart() {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for(String user : statistics.getUserStats().keySet()) {
            dataset.setValue(statistics.getUserStats().get(user).getReactionsSent(),
                    user, "Reactions given");
        }

        for(String user : statistics.getUserStats().keySet()) {
            dataset.setValue(statistics.getUserStats().get(user).getReactionsReceived(),
                    user, "Reactions received");
        }

        JFreeChart chart = ChartFactory.createBarChart("Reactions", "", "",
                dataset, PlotOrientation.VERTICAL, true, false, false);

        chart.getTitle().setFont(titleFont);
        chart.getLegend().setItemFont(legendFont);

        CategoryPlot plot = (CategoryPlot)chart.getPlot();
        plot.getDomainAxis().setTickLabelFont(domainFont);
        plot.getRangeAxis().setTickLabelFont(rangeFont);

        CategoryItemRenderer renderer = plot.getRenderer();
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setItemLabelsVisible(true);
        renderer.setItemLabelFont(labelFont);

        return chart.createBufferedImage(1920, 1080);
    }

    /**
     * Creates a chart for actions made
     * @return the Image object of the chart
     */
    public Image actionsMadeChart() {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for(String user : statistics.getUserStats().keySet()) {
            dataset.setValue(statistics.getUserStats().get(user).getNamesChanged(),
                    user, "Names of others changed");
        }

        for(String user : statistics.getUserStats().keySet()) {
            dataset.setValue(statistics.getUserStats().get(user).getGroupNameChanged(),
                    user, "Name of group changed");
        }

        for(String user : statistics.getUserStats().keySet()) {
            dataset.setValue(statistics.getUserStats().get(user).getPhotoChanged(),
                    user, "Group photo changed");
        }

        for(String user : statistics.getUserStats().keySet()) {
            dataset.setValue(statistics.getUserStats().get(user).getThemeChanged(),
                    user, "Group theme changed");
        }


        JFreeChart chart = ChartFactory.createBarChart("Action Made", "", "",
                dataset, PlotOrientation.VERTICAL, true, false, false);

        chart.getTitle().setFont(titleFont);
        chart.getLegend().setItemFont(legendFont);

        CategoryPlot plot = (CategoryPlot)chart.getPlot();
        plot.getDomainAxis().setTickLabelFont(domainFont);
        plot.getRangeAxis().setTickLabelFont(rangeFont);

        CategoryItemRenderer renderer = plot.getRenderer();
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setItemLabelsVisible(true);
        renderer.setItemLabelFont(labelFont);

        return chart.createBufferedImage(1920, 1080);
    }

    /**
     * Converts image to byte array
     * @param image image to be converted
     * @return byte array representing the given image
     */
    public byte[] imageAsByteArray(Image image) {

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        try {
            ImageIO.write((RenderedImage) image, "png", byteStream);
            return byteStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Rounds number
     * @param value number to be rounded
     * @param places precision
     * @return the rounded number
     */
    private double round(double value, int places) {

        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private class ItemGenerator extends StandardCategoryItemLabelGenerator {

        private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        private MessageFormat message;

        private ItemGenerator(MessageFormat message) {
            this.message = message;
        }

        @Override
        public String generateLabel(CategoryDataset dataset, int row, int column) {

            if(dataset.getColumnKey(column).equals(dateFormat.format(new Date(message.getTimestamp())))) {
                return message.getSender() + " " + dataset.getColumnKey(column);
            }
            return "";
        }
    }
}
