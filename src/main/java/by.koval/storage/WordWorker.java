package by.koval.storage;

import by.koval.model.Match;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.stereotype.Component;

import java.io.*;
import java.math.BigInteger;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

@Component
public class WordWorker {
    String fileName;

    public void mergeRow(XWPFTableRow secondRow, String text) {
        XWPFTableCell cell2 = secondRow.getTableCells().get(0);
        cell2.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        XWPFParagraph par = cell2.getParagraphs().get(0);
        par.setSpacingBetween(1);
        par.setAlignment(ParagraphAlignment.CENTER);
        par.setSpacingBefore(0);
        par.setSpacingAfter(0);
        XWPFRun cell1Run = par.createRun();
        cell1Run.setBold(true);
        cell1Run.setFontFamily("Cambria");
        cell1Run.setText(text);

        CTTcPr tcpr = secondRow.getTableCells().get(0).getCTTc().addNewTcPr();
        CTHMerge cthMerge = tcpr.addNewHMerge();
        cthMerge.setVal(STMerge.RESTART);
        for (int i = 1; i < secondRow.getTableCells().size(); i++) {
            tcpr = secondRow.getTableCells().get(i).getCTTc().addNewTcPr();
            cthMerge = tcpr.addNewHMerge();
            cthMerge.setVal(STMerge.CONTINUE);
        }
    }

    public String dateConverter(LocalDate date) {
        String month = switch (date.getMonth()) {
            case APRIL -> "апреля";
            case MAY -> "мая";
            case JUNE -> "июня";
            case JULY -> "июля";
            case AUGUST -> "августа";
            case SEPTEMBER -> "сентября";
            case OCTOBER -> "октября";
            case NOVEMBER -> "ноября";
            default -> "month";
        };

        return date.getDayOfMonth() + " " + month;
    }

    public String dayOfWeekConverter(LocalDate date) {
        String day = switch (date.getDayOfWeek()) {
            case MONDAY -> "понедельник";
            case TUESDAY -> "вторник";
            case WEDNESDAY -> "среда";
            case THURSDAY -> "четверг";
            case FRIDAY -> "пятница";
            case SATURDAY -> "суббота";
            case SUNDAY -> "воскресенье";
        };

        return day;
    }

    public byte[] writeToDocument(List<Match> matches) throws IOException, InterruptedException {
        byte[] bytes;
        if (matches.isEmpty()) {
            return null;
        }
        WordWorker ww = new WordWorker();
        String teamType = switch (matches.get(0).getChampionshipName()) {
            case "Первая лига 2024" -> "среди команд первой лиги.";
            case "Женская Высшая лига 2024" -> "среди команд женской высшей лиги.";
            case "Беларусбанк - Высшая лига 2024" -> "среди команд высшей лиги.";
            case "Вторая лига 2024 - Финальный этап - Группа А", "Вторая лига 2024 - Финальный этап - Группа Б",
                 "Вторая лига 2024 - Финальный этап - Группа Г", "Вторая лига 2024 - Финальный этап - Группа В",
                 "Вторая лига 2024 - Финальный этап - за 5-8 места",
                 "Вторая лига 2024 - Финальный этап - Плей-офф" -> "среди команд второй лиги.";
            default -> throw new IllegalStateException("Unexpected value: " + matches.get(0).getChampionshipName());
        };

        List<LocalDate> dates = matches.stream()
                .map(Match::getDate)
                .distinct()
                .sorted()
                .toList();

        int numberOfDates = dates.size();
        HashMap<LocalDate, List<Match>> matchesPerDate = new HashMap<>();
        for (LocalDate d : dates) {
            matchesPerDate.put(d, matches.stream().filter(match -> match.getDate().equals(d)).toList());
        }

        HashMap<Integer, List<String>> mainCells = new HashMap<>();
        mainCells.put(0, List.of("№", "п/п"));
        mainCells.put(1, List.of("Играющие команды,", "место проведения."));
        mainCells.put(2, List.of("Время"));
        mainCells.put(3, List.of("ТВ"));
        mainCells.put(4, List.of("Судья"));
        mainCells.put(5, List.of("Помощник"));
        mainCells.put(6, List.of("Помощник"));
        mainCells.put(7, List.of("Резервный судья"));
        mainCells.put(8, List.of("Инспектор", "Делегат"));

        try {
            // создаем модель docx документа,
            // к которой будем прикручивать наполнение (колонтитулы, текст)
            XWPFDocument document = new XWPFDocument();
            //document.setMirrorMargins(true);
            CTSectPr sectPr = document.getDocument().getBody().addNewSectPr();
            CTPageMar pageMar = sectPr.addNewPgMar();
            pageMar.setLeft(BigInteger.valueOf(520L));
            pageMar.setTop(BigInteger.valueOf(520L));
            pageMar.setRight(BigInteger.valueOf(520L));
            pageMar.setBottom(BigInteger.valueOf(520L));

            CTBody body = document.getDocument().getBody();
            if (!body.isSetSectPr()) {
                body.addNewSectPr();
            }

            CTSectPr section = body.getSectPr();
            if (!section.isSetPgSz()) {
                section.addNewPgSz();
            }

            CTPageSz pageSize = section.getPgSz();
            pageSize.setOrient(STPageOrientation.LANDSCAPE);
            pageSize.setW(BigInteger.valueOf(15840));
            pageSize.setH(BigInteger.valueOf(12240));

            // создаем обычный параграф, который будет расположен слева,
            // будет синим курсивом со шрифтом 25 размера
            XWPFParagraph paragraph = document.createParagraph();
            paragraph.setAlignment(ParagraphAlignment.CENTER);
            paragraph.setSpacingBetween(1);
            XWPFRun run = paragraph.createRun();
            run.setFontFamily("Cambria");
            run.setFontSize(11);
            run.setBold(true);
            // HEX цвет без решетки #
            run.setText("Чемпионат Республики Беларусь по футболу сезона 2024");
            run.addBreak();
            run.setText(teamType);
            run.addBreak();
            run.addBreak();
            run.setText(matches.get(0).getMatchRound() + " тур        "
                    + ww.dateConverter(dates.get(0))
                    + " - " + ww.dateConverter(dates.get(dates.size() - 1)) + " "
                    + dates.get(0).getYear() + " года (" + ww.dayOfWeekConverter(dates.get(0)) + " - " + ww.dayOfWeekConverter(dates.get(dates.size() - 1)) + ")");
            run.addBreak();
            XWPFTable table = document.createTable(matches.size() + dates.size() + 1, 9);

            XWPFTableRow firstRow = table.getRows().get(0);

            for (int i = 0; i < firstRow.getTableCells().size(); i++) {
                XWPFTableCell cell = firstRow.getTableCells().get(i);
                cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                XWPFParagraph par = cell.getParagraphs().get(0);
                par.setSpacingBetween(1);
                par.setAlignment(ParagraphAlignment.CENTER);
                par.setSpacingBefore(0);
                par.setSpacingAfter(0);
                XWPFRun cell1Run = par.createRun();
                cell1Run.setBold(true);
                cell1Run.setFontFamily("Cambria");
                if (mainCells.get(i).size() < 2) {
                    cell1Run.setText(mainCells.get(i).get(0));
                } else {
                    cell1Run.setText(mainCells.get(i).get(0));
                    cell1Run.addBreak();
                    cell1Run.setText(mainCells.get(i).get(mainCells.get(i).size()-1));
                }
            }

            int counter = 1;
            int n = 1;


            for (LocalDate d : dates) {
                ww.mergeRow(table.getRows().get(counter), ww.dateConverter(d) + " (" + ww.dayOfWeekConverter(d) + ")");
                counter++;
                for (Match m : matchesPerDate.get(d)) {
                    XWPFTableRow row = table.getRows().get(counter);
                    counter++;
                    XWPFTableCell cell = row.getTableCells().get(0);
                    cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                    cell.setWidth(String.valueOf(500));
                    XWPFParagraph par = cell.getParagraphs().get(0);
                    par.setSpacingBetween(1);
                    par.setAlignment(ParagraphAlignment.CENTER);
                    par.setSpacingBefore(0);
                    par.setSpacingAfter(0);
                    XWPFRun cellRun = par.createRun();
                    cellRun.setBold(true);
                    cellRun.setFontFamily("Cambria");
                    cellRun.setText(String.valueOf(n));
                    n++;

                    XWPFTableCell cell1 = row.getTableCells().get(1);
                    cell1.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                    cell1.setWidth(String.valueOf(4700));
                    XWPFParagraph par1 = cell1.getParagraphs().get(0);
                    par1.setSpacingBetween(1);
                    par1.setAlignment(ParagraphAlignment.LEFT);
                    par1.setSpacingBefore(1);
                    par1.setSpacingAfter(0);
                    XWPFRun cell1Run = par1.createRun();
                    cell1Run.setBold(true);
                    cell1Run.setFontFamily("Cambria");
                    cell1Run.setText(m.getMatchDescription().toUpperCase());
                    XWPFParagraph par2 = cell1.addParagraph();
                    par2.setSpacingAfter(10);
                    XWPFRun cell1Run2 = par2.createRun();
                    cell1Run2.setFontFamily("Cambria");
                    cell1Run2.setText("г. " + m.getCity() + ", стадион «" + m.getStadium() + "»");

                    XWPFTableCell cell2 = row.getTableCells().get(2);
                    cell2.setWidth(String.valueOf(800));
                    cell2.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                    XWPFParagraph par3 = cell2.getParagraphs().get(0);
                    par3.setSpacingBetween(1);
                    par3.setAlignment(ParagraphAlignment.CENTER);
                    par3.setSpacingBefore(0);
                    par3.setSpacingAfter(0);
                    XWPFRun cell2Run = par3.createRun();
                    cell2Run.setBold(true);
                    cell2Run.setFontFamily("Cambria");
                    cell2Run.setText(m.getDateTime().format(DateTimeFormatter.ofPattern("HH:mm")));

                    XWPFTableCell cell3 = row.getTableCells().get(3);
                    cell3.setWidth(String.valueOf(400));

                    XWPFTableCell cell4 = row.getTableCells().get(4);
                    cell4.setWidth(String.valueOf(1700));
                    cell4.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                    XWPFParagraph par4 = cell4.getParagraphs().get(0);
                    par4.setSpacingBetween(1);
                    par4.setAlignment(ParagraphAlignment.LEFT);
                    par4.setSpacingBefore(0);
                    par4.setSpacingAfter(0);
                    XWPFRun cell4Run = par4.createRun();
                    cell4Run.setFontFamily("Cambria");
                    cell4Run.setText(m.getOfficials().get(0));

                    XWPFTableCell cell5 = row.getTableCells().get(5);
                    cell5.setWidth(String.valueOf(1700));
                    cell5.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                    XWPFParagraph par5 = cell5.getParagraphs().get(0);
                    par5.setSpacingBetween(1);
                    par5.setAlignment(ParagraphAlignment.LEFT);
                    par5.setSpacingBefore(0);
                    par5.setSpacingAfter(0);
                    XWPFRun cell5Run = par5.createRun();
                    cell5Run.setFontFamily("Cambria");
                    cell5Run.setText(m.getOfficials().get(1));

                    XWPFTableCell cell6 = row.getTableCells().get(6);
                    cell6.setWidth(String.valueOf(1700));
                    cell6.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                    XWPFParagraph par6 = cell6.getParagraphs().get(0);
                    par6.setSpacingBetween(1);
                    par6.setAlignment(ParagraphAlignment.LEFT);
                    par6.setSpacingBefore(0);
                    par6.setSpacingAfter(0);
                    XWPFRun cell6Run = par6.createRun();
                    cell6Run.setFontFamily("Cambria");
                    cell6Run.setText(m.getOfficials().get(2));

                    XWPFTableCell cell7 = row.getTableCells().get(7);
                    cell7.setWidth(String.valueOf(1700));
                    cell7.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                    XWPFParagraph par7 = cell7.getParagraphs().get(0);
                    par7.setSpacingBetween(1);
                    par7.setAlignment(ParagraphAlignment.LEFT);
                    par7.setSpacingBefore(0);
                    par7.setSpacingAfter(0);
                    XWPFRun cell7Run = par7.createRun();
                    cell7Run.setFontFamily("Cambria");
                    cell7Run.setText(m.getOfficials().get(3));

                    XWPFTableCell cell8 = row.getTableCells().get(8);
                    cell8.setWidth(String.valueOf(1700));
                    cell8.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                    XWPFParagraph par8 = cell8.getParagraphs().get(0);
                    par8.setSpacingBetween(1);
                    par8.setAlignment(ParagraphAlignment.LEFT);
                    par8.setSpacingBefore(0);
                    par8.setSpacingAfter(0);
                    XWPFRun cell8Run = par8.createRun();
                    cell8Run.setFontFamily("Cambria");
                    cell8Run.setText(m.getOfficials().get(4));
                    if (m.getOfficials().get(5) != null) {
                        XWPFParagraph par9 = cell8.addParagraph();
                        par9.setSpacingAfter(10);
                        XWPFRun cell8Run2 = par9.createRun();
                        cell8Run2.setFontFamily("Cambria");
                        cell8Run2.setText(m.getOfficials().get(5));
                    }
                }
            }
            // сохраняем модель docx документа в файл
            fileName = "src/main/resources/Назначения " + matches.get(0).getMatchRound() + " тур " + matches.get(0).getChampionshipName() + ".docx";
            FileOutputStream outputStream = new FileOutputStream(fileName);
            ByteArrayOutputStream outputStream1 = new ByteArrayOutputStream();

            document.write(outputStream);
            outputStream.close();
            document.write(outputStream1);
            bytes = outputStream1.toByteArray();
            return bytes;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

