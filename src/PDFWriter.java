import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.vandeseer.easytable.TableDrawer;
import org.vandeseer.easytable.structure.Row;
import org.vandeseer.easytable.structure.Table;
import org.vandeseer.easytable.structure.cell.TextCell;

import java.io.IOException;
import java.text.Normalizer;
import java.util.regex.Pattern;

import static org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD;
import static org.vandeseer.easytable.settings.HorizontalAlignment.CENTER;

/**
 * Writes the statistics to a PDF file
 */
public class PDFWriter {

    final int TOP_MARGIN = 20;
    final int LEFT_MARGIN = 10;
    final int NUMBER_OF_PAGES = 7;

    private GroupStatistics statistics;
    private ChartCreator chartCreator;

    // Users who's statistics should be printed
    String[] users;

    /**
     * Creates a pdf writer using the statistics to be written to file
     * @param statistics the stats to be printed
     */
    public PDFWriter(GroupStatistics statistics) {
        this.statistics = statistics;
        chartCreator = new ChartCreator(statistics);
        getUsers();
    }

    /**
     * Creates a pdf file with all of the given statistics
     */
    public void writeToPDF() {

        PDDocument document = new PDDocument();

        for(int pageNo = 0; pageNo < NUMBER_OF_PAGES; pageNo++) {
            PDPage page = new PDPage();
            document.addPage(page);
        }

        PDPage currentPage = document.getPage(0);
        PDRectangle pageBox = currentPage.getMediaBox();
        String title = "Group Chat Analysis";

        try {

            writePageOne(document);
            writePageTwo(document);
            writePageThree(document);
            writePageFour(document);
            writePageFive(document);
            writePageSix(document);
            writePageSeven(document);

            document.save("C:\\chart.pdf");
            document.close();

        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    /**
     * Creates the first page of the pdf
     * @param document the pdf document to be written to
     * @throws IOException if content stream cannot be initialized
     */
    private void writePageOne(PDDocument document) throws IOException {

        PDPage currentPage = document.getPage(0);
        PDRectangle pageBox = currentPage.getMediaBox();
        String title = "Group Chat Analysis";

        PDPageContentStream contentStream = new PDPageContentStream(document, currentPage);

        PDFont font = HELVETICA_BOLD;
        int fontSize = 22;
        int linePadding = 1;
        float stringHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;

        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(LEFT_MARGIN, pageBox.getHeight() - stringHeight - TOP_MARGIN);
        contentStream.showText(title);

        contentStream.newLineAtOffset(0, -fontSize - linePadding);
        contentStream.showText("Creation date: " + statistics.getCreationDate());

        contentStream.newLineAtOffset(0, -fontSize - linePadding);
        contentStream.showText("Current date: " + statistics.getStatCreationDate());

        contentStream.newLineAtOffset(0, -fontSize - linePadding);
        contentStream.showText("Stats for the period of: " + statistics.getPeriod() + " days");
        contentStream.endText();

        contentStream.close();
    }

    /**
     * Creates the second page of the pdf
     * @param document the pdf document to be written to
     * @throws IOException if content stream cannot be initialized
     */
    private void writePageTwo(PDDocument document) throws IOException {

        PDPage currentPage = document.getPage(1);
        PDRectangle pageBox = currentPage.getMediaBox();
        PDPageContentStream contentStream = new PDPageContentStream(document, currentPage);

        final int SIDE_MARGIN_THREE_CHARTS = 6;
        final int TOP_MARGIN_THREE_CHARTS = 81;
        final int WIDTH_TWO_CHARTS = 300;
        final int HEIGHT_THREE_CHARTS = 210;

        PDImageXObject image;

        image = PDImageXObject.createFromByteArray( document,
                chartCreator.imageAsByteArray(chartCreator.messagesSendChart()), "");
        contentStream.drawImage(image, SIDE_MARGIN_THREE_CHARTS,
                pageBox.getHeight() - HEIGHT_THREE_CHARTS - TOP_MARGIN_THREE_CHARTS, WIDTH_TWO_CHARTS, HEIGHT_THREE_CHARTS);

        image = PDImageXObject.createFromByteArray( document,
                chartCreator.imageAsByteArray(chartCreator.spamMessagesSendChart()), "");
        contentStream.drawImage(image, SIDE_MARGIN_THREE_CHARTS + WIDTH_TWO_CHARTS,
                pageBox.getHeight() - HEIGHT_THREE_CHARTS - TOP_MARGIN_THREE_CHARTS, WIDTH_TWO_CHARTS, HEIGHT_THREE_CHARTS);

        image = PDImageXObject.createFromByteArray( document,
                chartCreator.imageAsByteArray(chartCreator.wordsSendChart()), "");
        contentStream.drawImage(image, SIDE_MARGIN_THREE_CHARTS,
                pageBox.getHeight() - TOP_MARGIN_THREE_CHARTS - 2 *HEIGHT_THREE_CHARTS, WIDTH_TWO_CHARTS, HEIGHT_THREE_CHARTS);

        image = PDImageXObject.createFromByteArray( document,
                chartCreator.imageAsByteArray(chartCreator.spamWordsSendChart()), "");
        contentStream.drawImage(image, SIDE_MARGIN_THREE_CHARTS + WIDTH_TWO_CHARTS,
                pageBox.getHeight() - TOP_MARGIN_THREE_CHARTS - 2 * HEIGHT_THREE_CHARTS, WIDTH_TWO_CHARTS, HEIGHT_THREE_CHARTS);

        image = PDImageXObject.createFromByteArray( document,
                chartCreator.imageAsByteArray(chartCreator.charsSendChart()), "");
        contentStream.drawImage(image, SIDE_MARGIN_THREE_CHARTS,
                pageBox.getHeight() - TOP_MARGIN_THREE_CHARTS - 3 * HEIGHT_THREE_CHARTS, WIDTH_TWO_CHARTS, HEIGHT_THREE_CHARTS);

        image = PDImageXObject.createFromByteArray( document,
                chartCreator.imageAsByteArray(chartCreator.spamCharsSendChart()), "");
        contentStream.drawImage(image, SIDE_MARGIN_THREE_CHARTS + WIDTH_TWO_CHARTS,
                pageBox.getHeight() - TOP_MARGIN_THREE_CHARTS - 3 * HEIGHT_THREE_CHARTS, WIDTH_TWO_CHARTS, HEIGHT_THREE_CHARTS);

        contentStream.close();
    }

    /**
     * Creates the third page of the pdf
     * @param document the pdf document to be written to
     * @throws IOException if content stream cannot be initialized
     */
    private void writePageThree(PDDocument document) throws IOException {

        PDPage currentPage = document.getPage(2);
        PDRectangle pageBox = currentPage.getMediaBox();
        PDPageContentStream contentStream = new PDPageContentStream(document, currentPage);

        final int SIDE_MARGIN_THREE_CHARTS = 6;
        final int TOP_MARGIN_THREE_CHARTS = 81;
        final int BETWEEN_CHART_MARGIN = 12;
        final int WIDTH_ONE_CHART = 600;
        final int HEIGHT_TWO_CHARTS = 300;

        PDImageXObject image;

        image = PDImageXObject.createFromByteArray( document,
                chartCreator.imageAsByteArray(chartCreator.averageMessageChart()), "");
        contentStream.drawImage(image, SIDE_MARGIN_THREE_CHARTS,
                pageBox.getHeight() - HEIGHT_TWO_CHARTS - TOP_MARGIN_THREE_CHARTS, WIDTH_ONE_CHART, HEIGHT_TWO_CHARTS);
        image = PDImageXObject.createFromByteArray( document,
                chartCreator.imageAsByteArray(chartCreator.reactionsChart()), "");
        contentStream.drawImage(image, SIDE_MARGIN_THREE_CHARTS,
                pageBox.getHeight() - 2 * HEIGHT_TWO_CHARTS - TOP_MARGIN_THREE_CHARTS - BETWEEN_CHART_MARGIN,
                WIDTH_ONE_CHART, HEIGHT_TWO_CHARTS);

        contentStream.close();
    }

    /**
     * Creates the fourth page of the pdf
     * @param document the pdf document to be written to
     * @throws IOException if content stream cannot be initialized
     */
    private void writePageFour(PDDocument document) throws IOException {

        PDPage currentPage = document.getPage(2);
        PDRectangle pageBox = currentPage.getMediaBox();
        PDPageContentStream contentStream = new PDPageContentStream(document, currentPage);

        final int SIDE_MARGIN_THREE_CHARTS = 6;
        final int TOP_MARGIN_THREE_CHARTS = 81;
        final int BETWEEN_CHART_MARGIN = 12;
        final int WIDTH_ONE_CHART = 600;
        final int HEIGHT_TWO_CHARTS = 300;

        PDImageXObject image;

        image = PDImageXObject.createFromByteArray( document,
                chartCreator.imageAsByteArray(chartCreator.dailyMessagesChart()), "");
        contentStream.drawImage(image, SIDE_MARGIN_THREE_CHARTS,
                pageBox.getHeight() - HEIGHT_TWO_CHARTS - TOP_MARGIN_THREE_CHARTS, WIDTH_ONE_CHART, HEIGHT_TWO_CHARTS);
        image = PDImageXObject.createFromByteArray( document,
                chartCreator.imageAsByteArray(chartCreator.monthlyMessagesChart()), "");
        contentStream.drawImage(image, SIDE_MARGIN_THREE_CHARTS,
                pageBox.getHeight() - 2 * HEIGHT_TWO_CHARTS - TOP_MARGIN_THREE_CHARTS - BETWEEN_CHART_MARGIN,
                WIDTH_ONE_CHART, HEIGHT_TWO_CHARTS);

        contentStream.close();
    }

    /**
     * Creates the fifth page of the pdf
     * @param document the pdf document to be written to
     * @throws IOException if content stream cannot be initialized
     */
    private void writePageFive(PDDocument document) throws IOException {

        PDPage currentPage = document.getPage(2);
        PDRectangle pageBox = currentPage.getMediaBox();
        PDPageContentStream contentStream = new PDPageContentStream(document, currentPage);

        final int SIDE_MARGIN_THREE_CHARTS = 6;
        final int TOP_MARGIN_THREE_CHARTS = 81;
        final int BETWEEN_CHART_MARGIN = 12;
        final int WIDTH_ONE_CHART = 600;
        final int HEIGHT_TWO_CHARTS = 300;

        PDImageXObject image;

        image = PDImageXObject.createFromByteArray( document,
                chartCreator.imageAsByteArray(chartCreator.hourlyMessagesChart()), "");
        contentStream.drawImage(image, SIDE_MARGIN_THREE_CHARTS,
                pageBox.getHeight() - HEIGHT_TWO_CHARTS - TOP_MARGIN_THREE_CHARTS, WIDTH_ONE_CHART, HEIGHT_TWO_CHARTS);
        image = PDImageXObject.createFromByteArray( document,
                chartCreator.imageAsByteArray(chartCreator.actionsMadeChart()), "");
        contentStream.drawImage(image, SIDE_MARGIN_THREE_CHARTS,
                pageBox.getHeight() - 2 * HEIGHT_TWO_CHARTS - TOP_MARGIN_THREE_CHARTS - BETWEEN_CHART_MARGIN,
                WIDTH_ONE_CHART, HEIGHT_TWO_CHARTS);

        contentStream.close();
    }

    /**
     * Creates the sixth page of the pdf
     * @param document the pdf document to be written to
     * @throws IOException if content stream cannot be initialized
     */
    private void writePageSix(PDDocument document) throws IOException {

        PDPage currentPage = document.getPage(2);
        PDRectangle pageBox = currentPage.getMediaBox();
        PDPageContentStream contentStream = new PDPageContentStream(document, currentPage);

        final int SIDE_MARGIN_THREE_CHARTS = 6;
        final int TOP_MARGIN_THREE_CHARTS = 81;
        final int WIDTH_ONE_CHART = 600;
        final int HEIGHT_TWO_CHARTS = 300;

        PDImageXObject image;

        image = PDImageXObject.createFromByteArray( document,
                chartCreator.imageAsByteArray(chartCreator.wordFrequencyChart()), "");
        contentStream.drawImage(image, SIDE_MARGIN_THREE_CHARTS,
                pageBox.getHeight() - HEIGHT_TWO_CHARTS - TOP_MARGIN_THREE_CHARTS, WIDTH_ONE_CHART, HEIGHT_TWO_CHARTS);

        contentStream.close();
    }

    /**
     * Creates the seventh page of the pdf
     * @param document the pdf document to be written to
     * @throws IOException if content stream cannot be initialized
     */
    private void writePageSeven(PDDocument document) throws IOException {

        PDPage currentPage = document.getPage(2);
        PDRectangle pageBox = currentPage.getMediaBox();
        PDPageContentStream contentStream = new PDPageContentStream(document, currentPage);

        PDFont font = HELVETICA_BOLD;
        int fontSize = 22;
        float stringHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
        float stringWidth = font.getStringWidth("Most Common N Letter Words") / 1000 * fontSize;

        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset((pageBox.getWidth() - stringWidth) / 2, pageBox.getHeight() - stringHeight - TOP_MARGIN);
        contentStream.showText("Most Common N Letter Words");
        contentStream.endText();

        int tableTopMargin = 80;
        int tableLeftMargin = 10;
        int tableBetweenPad = 16;
        int tableWidth = 96 * 3;

        TableDrawer tableDrawer = TableDrawer.builder()
                .contentStream(contentStream)
                .startX(tableLeftMargin)
                .startY(pageBox.getHeight() - tableTopMargin)
                .table(getTableOne())
                .build();

        tableDrawer.draw();

        tableDrawer = TableDrawer.builder()
                .contentStream(contentStream)
                .startX(tableLeftMargin + tableWidth + tableBetweenPad)
                .startY(pageBox.getHeight() - tableTopMargin)
                .table(getTableTwo())
                .build();

        tableDrawer.draw();

        tableDrawer = TableDrawer.builder()
                .contentStream(contentStream)
                .startX(tableLeftMargin)
                .startY(pageBox.getHeight() - tableTopMargin - 100)
                .table(getTableThree())
                .build();

        tableDrawer.draw();

        tableDrawer = TableDrawer.builder()
                .contentStream(contentStream)
                .startX(tableLeftMargin + tableWidth + tableBetweenPad)
                .startY(pageBox.getHeight() - tableTopMargin - 100)
                .table(getTableFour())
                .build();

        tableDrawer.draw();

        tableDrawer = TableDrawer.builder()
                .contentStream(contentStream)
                .startX(tableLeftMargin)
                .startY(pageBox.getHeight() - tableTopMargin - 200)
                .table(getTableFive())
                .build();

        tableDrawer.draw();

        tableDrawer = TableDrawer.builder()
                .contentStream(contentStream)
                .startX(tableLeftMargin + tableWidth + tableBetweenPad)
                .startY(pageBox.getHeight() - tableTopMargin - 200)
                .table(getTableSix())
                .build();

        tableDrawer.draw();

        contentStream.close();
    }

    /**
     * Changes all letters with accents to regular ones
     * @param str the string from which to remove the accents
     * @return same string without the accents
     */
    private String deAccent(String str) {

        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

    /**
     * Creates a table with user names filled in
     * @return initail table
     */
    private Table.TableBuilder buildTable() {

        Table.TableBuilder tableBuilder = Table.builder()
                .addColumnsOfWidth(96, 96, 96)
                .fontSize(8)
                .font(HELVETICA_BOLD);

        tableBuilder.addRow(Row.builder()
                .add(TextCell.builder().text(users[0]).borderWidth(1).horizontalAlignment(CENTER).build())
                .add(TextCell.builder().text(users[1]).borderWidth(1).horizontalAlignment(CENTER).build())
                .add(TextCell.builder().text(users[2]).borderWidth(1).horizontalAlignment(CENTER).build())
                .build());

        return tableBuilder;
    }

    /**
     * Get the names of users who's data should be printed
     */
    private void getUsers() {

        users = new String[statistics.getUserStats().size()];
        for(int index = 0; index < statistics.getUserStats().size(); index++) {
            users[index] = statistics.getUserStats().keySet().toArray()[index].toString().split(" ")[0];
        }
    }

    /**
     * Adds a single data row to the table
     * @param tableBuilder the table to be filled
     * @param name1 first user's data
     * @param name2 second user's data
     * @param name3 third user's data
     */
    private void addDataRow(Table.TableBuilder tableBuilder,
                                          String name1, String name2, String name3) {

        tableBuilder.addRow(Row.builder()
                .add(TextCell.builder().text(name1).horizontalAlignment(CENTER).borderWidth(1).build())
                .add(TextCell.builder().text(name2).horizontalAlignment(CENTER).borderWidth(1).build())
                .add(TextCell.builder().text(name3).horizontalAlignment(CENTER).borderWidth(1).build())
                .build());
    }

    /**
     * Constructs the table for most used four letter words
     * @return complete table
     */
    private Table getTableOne() {

        Table.TableBuilder tableBuilder = buildTable();

        for(int row = 1; row <= 5; row++) {

            String firstName = statistics.getUserStats().get("Name1 Surname1").getCommonWordsFour().get(row - 1).getKey() +
                    " - " + statistics.getUserStats().get("Name1 Surname1").getCommonWordsFour().get(row - 1).getValue();
            String secondName = statistics.getUserStats().get("Name2 Surname2").getCommonWordsFour().get(row - 1).getKey() +
                    " - " + statistics.getUserStats().get("Name2 Surname2").getCommonWordsFour().get(row - 1).getValue();
            String thirdName = statistics.getUserStats().get("Name3 Surname3").getCommonWordsFour().get(row - 1).getKey() +
                    " - " + statistics.getUserStats().get("Name3 Surname3").getCommonWordsFour().get(row - 1).getValue();

            addDataRow(tableBuilder, firstName, secondName, thirdName);
        }

        return tableBuilder.build();
    }

    /**
     * Constructs the table for most used five letter words
     * @return complete table
     */
    private Table getTableTwo() {

        Table.TableBuilder tableBuilder = buildTable();

        for(int row = 1; row <= 5; row++) {

            String firstName = statistics.getUserStats().get("Name1 Surname1").getCommonWordsFive().get(row - 1).getKey() +
                    " - " + statistics.getUserStats().get("Name1 Surname1").getCommonWordsFive().get(row - 1).getValue();
            String secondName = statistics.getUserStats().get("Name2 Surname2").getCommonWordsFive().get(row - 1).getKey() +
                    " - " + statistics.getUserStats().get("Name2 Surname2").getCommonWordsFive().get(row - 1).getValue();
            String thirdName = statistics.getUserStats().get("Name3 Surname3").getCommonWordsFive().get(row - 1).getKey() +
                    " - " + statistics.getUserStats().get("Name3 Surname3").getCommonWordsFive().get(row - 1).getValue();

            addDataRow(tableBuilder, firstName, secondName, thirdName);
        }

        return tableBuilder.build();
    }

    /**
     * Constructs the table for most used six letter words
     * @return complete table
     */
    private Table getTableThree() {

        Table.TableBuilder tableBuilder = buildTable();

        for(int row = 1; row <= 5; row++) {

            String firstName = statistics.getUserStats().get("Name1 Surname1").getCommonWordsSix().get(row - 1).getKey() +
                    " - " + statistics.getUserStats().get("Name1 Surname1").getCommonWordsSix().get(row - 1).getValue();
            String secondName = statistics.getUserStats().get("Name2 Surname2").getCommonWordsSix().get(row - 1).getKey() +
                    " - " + statistics.getUserStats().get("Name2 Surname2").getCommonWordsSix().get(row - 1).getValue();
            String thirdName = statistics.getUserStats().get("Name3 Surname3").getCommonWordsSix().get(row - 1).getKey() +
                    " - " + statistics.getUserStats().get("Name3 Surname3").getCommonWordsSix().get(row - 1).getValue();

            addDataRow(tableBuilder, firstName, secondName, thirdName);
        }

        return tableBuilder.build();
    }

    /**
     * Constructs the table for most used seven letter words
     * @return complete table
     */
    private Table getTableFour() {

        Table.TableBuilder tableBuilder = buildTable();

        for(int row = 1; row <= 5; row++) {

            String firstName = statistics.getUserStats().get("Name1 Surname1").getCommonWordsSeven().get(row - 1).getKey() +
                    " - " + statistics.getUserStats().get("Name1 Surname1").getCommonWordsSeven().get(row - 1).getValue();
            String secondName = statistics.getUserStats().get("Name2 Surname2").getCommonWordsSeven().get(row - 1).getKey() +
                    " - " + statistics.getUserStats().get("Name2 Surname2").getCommonWordsSeven().get(row - 1).getValue();
            String thirdName = statistics.getUserStats().get("Name3 Surname3").getCommonWordsSeven().get(row - 1).getKey() +
                    " - " + statistics.getUserStats().get("Name3 Surname3").getCommonWordsSeven().get(row - 1).getValue();

            addDataRow(tableBuilder, firstName, secondName, thirdName);
        }

        return tableBuilder.build();
    }

    /**
     * Constructs the table for most used eight letter words
     * @return complete table
     */
    private Table getTableFive() {

        Table.TableBuilder tableBuilder = buildTable();

        for(int row = 1; row <= 5; row++) {

            String firstName = statistics.getUserStats().get("Name1 Surname1").getCommonWordsEight().get(row - 1).getKey() +
                    " - " + statistics.getUserStats().get("Name1 Surname1").getCommonWordsEight().get(row - 1).getValue();
            String secondName = statistics.getUserStats().get("Name2 Surname2").getCommonWordsEight().get(row - 1).getKey() +
                    " - " + statistics.getUserStats().get("Name2 Surname2").getCommonWordsEight().get(row - 1).getValue();
            String thirdName = statistics.getUserStats().get("Name3 Surname3").getCommonWordsEight().get(row - 1).getKey() +
                    " - " + statistics.getUserStats().get("Name3 Surname3").getCommonWordsEight().get(row - 1).getValue();

            addDataRow(tableBuilder, firstName, secondName, thirdName);
        }

        return tableBuilder.build();
    }

    /**
     * Constructs the table for most used nine letter words
     * @return complete table
     */
    private Table getTableSix() {

        Table.TableBuilder tableBuilder = buildTable();

        for(int row = 1; row <= 5; row++) {

            String firstName = statistics.getUserStats().get("Name1 Surname1").getCommonWordsNine().get(row - 1).getKey() +
                    " - " + statistics.getUserStats().get("Name1 Surname1").getCommonWordsNine().get(row - 1).getValue();
            String secondName = statistics.getUserStats().get("Name2 Surname2").getCommonWordsNine().get(row - 1).getKey() +
                    " - " + statistics.getUserStats().get("Name2 Surname2").getCommonWordsNine().get(row - 1).getValue();
            String thirdName = statistics.getUserStats().get("Name3 Surname3").getCommonWordsNine().get(row - 1).getKey() +
                    " - " + statistics.getUserStats().get("Name3 Surname3").getCommonWordsNine().get(row - 1).getValue();

            addDataRow(tableBuilder, firstName, secondName, thirdName);
        }

        return tableBuilder.build();
    }
}
