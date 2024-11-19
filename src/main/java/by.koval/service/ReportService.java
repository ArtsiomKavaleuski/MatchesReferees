package by.koval.service;

import by.koval.storage.DataLoader;
import by.koval.storage.WordWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportService {
    @Autowired
    private final DataLoader dataLoader;
    @Autowired
    private final WordWorker wordWorker;

    public static String ZVL = "Женская Высшая лига 2024";
    public static String VL = "Беларусбанк - Высшая лига 2024";
    public static String BK23 = "BETERA - Кубок Беларуси 2023/2024";
    public static String BK24 = "BETERA - Кубок Беларуси 2024/2025";
    public static String PL = "Первая лига 2024";
    public static String ZK24 = "Женский Кубок Беларуси 2024";
    public static String ZSK24 = "Женский Суперкубок 2024";
    public static String BSK24 = "BETERA - Суперкубок Беларуси 2024";
    public static String groupA = "Вторая лига 2024 - Финальный этап - Группа А";
    public static String groupB = "Вторая лига 2024 - Финальный этап - Группа Б";
    public static String groupC = "Вторая лига 2024 - Финальный этап - Группа В";
    public static String groupD = "Вторая лига 2024 - Финальный этап - Группа Г";
    public static String play_off = "Вторая лига 2024 - Финальный этап - Плей-офф";
    public static String fin = "Вторая лига 2024 - Финальный этап - за 5-8 места";

    public byte[] getRefByMatchesByRound(String competitionCode, long round) throws IOException, InterruptedException {
        return wordWorker.writeToDocument(dataLoader.getSortedMatches(competitionCode, (int) round));
    }

    public String getCompetitionByCode(String competitionCode) {
        String competitionName = switch (competitionCode) {
            case "ZVL" -> ZVL;
            case "VL" -> VL;
            case "PL" -> PL;
            case "playoff2L" -> play_off;
            case "fin2L" -> fin;
            default -> "null";
        };
        return competitionName;
    }

    public void refreshData() throws IOException, InterruptedException {
        dataLoader.loadResults();
    }
}
