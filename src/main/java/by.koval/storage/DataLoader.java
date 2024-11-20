package by.koval.storage;

import by.koval.model.Match;
import by.koval.model.comparators.MatchByDateComparator;
import com.google.gson.*;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalTime;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataLoader {
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
    HashMap<Integer, Match> matches = new HashMap<>();
    HashMap<Integer, String> ref = new HashMap<>();
    HashMap<Integer, String> first = new HashMap<>();
    HashMap<Integer, String> second = new HashMap<>();
    HashMap<Integer, String> fourth = new HashMap<>();
    HashMap<Integer, String> insp = new HashMap<>();
    HashMap<Integer, String> del = new HashMap<>();
    int pageNumber = 0;

    public URI getMatchURI() {
        return URI.create("https://comet.abff.by/data-backend/api/public/areports/run/"
                + pageNumber + "/1000/?API_KEY=bf55c36fddd21f35ec790ea33710c04fc0627559d37aa6e1" +
                "857488ac40f09a78129f63d6ddd792e01fe59a7f8d2418a04dec8d628ed498295ac5360361e07234");
    }

    public URI getRefURI() {
        return URI.create("https://comet.abff.by/data-backend/api/public/areports/run/"
                + pageNumber + "/1000/?API_KEY=f06b6785f05192bd0b92c9bd87271f8c7b57b367519029b8e9b546d0409ddb37f41" +
                "fd918045fd181eb5e2c9e18d0d40c2f7ef852db052335d907eb8a99dffaed");
    }

    public void add(Match match) {
        matches.putIfAbsent(match.getMatchId(), match);
    }

    public void addOfficial(String role, int matchID, String name) {
        String[] splitName = name.split(" ");
        String lastName = splitName[1];
        String firstName = splitName[0];
        String shortName = firstName.charAt(0) + "." + lastName;
        switch (role) {
            case "Арбитр":
                ref.put(matchID, shortName);
                break;
            case "1й ассистент арбитра":
                first.put(matchID, shortName);
                break;
            case "2й ассистент арбитра":
                second.put(matchID, shortName);
                break;
            case "Четвёртый арбитр":
                fourth.put(matchID, shortName);
                break;
            case "Инспектор":
                insp.put(matchID, shortName);
                break;
            case "Делегат":
                del.put(matchID, shortName);
                break;
        }
    }

    public List<Match> getSortedMatches(String champName) {
        return matches.values().stream()
                .filter(m -> m.getChampionshipName().equals(champName))
                .toList();
    }

    public List<Match> getSortedMatches(List<String> champName, int round) {
        List<Match> matchesList = new ArrayList<>();
        for(String champ : champName) {
            matchesList.addAll(matches.values().stream()
                    .filter(m -> m.getChampionshipName().equals(champ))
                    .filter(m -> m.getMatchRound() == round)
                    .sorted(new MatchByDateComparator())
                    .toList());
            System.out.println(champ);
        }
        return matchesList;
    }

    public List<Match> getSortedMatches(String champName, int round) {
        return matches.values().stream()
                .filter(m -> m.getChampionshipName().equals(champName))
                .filter(m -> m.getMatchRound() == round)
                .sorted(new MatchByDateComparator())
                .toList();
    }

    public List<String> getChampionships() {
        return matches.values().stream()
                .map(Match::getChampionshipName)
                .distinct()
                .toList();
    }


    public void loadResults() throws InterruptedException, IOException {
        double time = Instant.now().getNano();
        JsonArray matches = null;
        JsonArray referees = null;
        while (true) {
            double time1 = Instant.now().getNano();
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(getMatchURI())
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
            HttpResponse<String> response = client.send(request, handler);
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonArray jsonArray = jsonElement.getAsJsonObject().get("results").getAsJsonArray();
            if (jsonArray.isEmpty()) {
                pageNumber = 0;
                break;
            }
            if(matches == null){
                matches = jsonArray;
            } else {
                matches.addAll(jsonArray);
            }
            double time2 = Instant.now().getNano() - time1;
            System.out.println("page " + pageNumber + " of Matches was loaded in " + time2/1000000000 + " sec");
            pageNumber++;
        }

        while (true) {
            double time1 = LocalTime.now().getSecond();
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(getRefURI())
                    .version(HttpClient.Version.HTTP_1_1)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
            HttpResponse<String> response = client.send(request, handler);
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonArray jsonArray = jsonElement.getAsJsonObject().get("results").getAsJsonArray();
            if (jsonArray.isEmpty()) {
                pageNumber = 0;
                break;
            }
            if(referees == null){
                referees = jsonArray;
            } else {
                referees.addAll(jsonArray);
            }
            double time2 = LocalTime.now().getSecond() - time1;
            System.out.println("page " + pageNumber + " of Referees was loaded in " + time2 + " sec");
            pageNumber++;
        }
        double timeAvg = Instant.now().getNano() - time;
        System.out.println("All data was loaded in " + timeAvg/1000000000 + " sec");

        assert referees != null;
        for (JsonElement j : referees) {
            if (!j.getAsJsonObject().get("roleStatus").toString().contains("ОТКАЗАНО") && (j.getAsJsonObject().get("name").getAsString().equals("Женская Высшая лига 2024") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Суперкубок") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Беларусбанк") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Вторая лига 2024 - Финальный этап - Группа А") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Вторая лига 2024 - Финальный этап - Группа Б") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Вторая лига 2024 - Финальный этап - Группа В") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Вторая лига 2024 - Финальный этап - Группа Г") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Вторая лига 2024 - Финальный этап - Плей-офф") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Вторая лига 2024 - Финальный этап - за 5-8 места") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Первая лига 2024") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Кубок"))) {
                int matchId = Integer.parseInt(j.getAsJsonObject().get("uid").getAsString());
                String officialName = j.getAsJsonObject().get("personName").getAsString();
                String role = j.getAsJsonObject().get("registrationType").getAsString();
                int matchID = Integer.parseInt(j.getAsJsonObject().get("matchId").getAsString());

                addOfficial(role, matchID, officialName);
            }
        }

        assert matches != null;
        for (JsonElement j : matches) {
            if (j.getAsJsonObject().get("name").getAsString().equals("Женская Высшая лига 2024") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Суперкубок") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Беларусбанк") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Вторая лига 2024 - Финальный этап - Группа А") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Вторая лига 2024 - Финальный этап - Группа Б") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Вторая лига 2024 - Финальный этап - Группа В") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Вторая лига 2024 - Финальный этап - Группа Г") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Вторая лига 2024 - Финальный этап - Плей-офф") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Вторая лига 2024 - Финальный этап - за 5-8 места") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Первая лига 2024") ||
                    j.getAsJsonObject().get("name").getAsString().contains("Кубок")) {
                int matchId = Integer.parseInt(j.getAsJsonObject().get("uid").getAsString());
                String matchDateTime = j.getAsJsonObject().get("matchDate").getAsString();
                String matchDescription = j.getAsJsonObject().get("matchDescription").getAsString();
                int homeTeamId = Integer.parseInt(j.getAsJsonObject().get("homeTeam").getAsString());
                int awayTeamId = Integer.parseInt(j.getAsJsonObject().get("awayTeam").getAsString());
                String championshipName = j.getAsJsonObject().get("name").getAsString();
                String matchStatus = j.getAsJsonObject().get("matchStatus").getAsString();
                int matchRound = Integer.parseInt(j.getAsJsonObject().get("round").getAsString());
                String city = j.getAsJsonObject().get("facilityPlaceName").getAsString();
                String stadium = j.getAsJsonObject().get("facility").getAsString();
                ArrayList<String> officials = new ArrayList<>();
                officials.add(ref.get(matchId));
                officials.add(first.get(matchId));
                officials.add(second.get(matchId));
                officials.add(fourth.get(matchId));
                officials.add(insp.get(matchId));
                officials.add(del.get(matchId));

                Match match = new Match(matchId, matchDateTime, matchDescription, homeTeamId,
                        awayTeamId, championshipName, matchStatus, matchRound, city, stadium, officials);
                add(match);
            }
        }
    }
}
