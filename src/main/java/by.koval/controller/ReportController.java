package by.koval.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import by.koval.service.ReportService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/matches")
@Slf4j
public class ReportController {
    @Autowired
    private final ReportService reportService;

    @GetMapping(value = "/{competitionCode}/round/{id}", produces = "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    public @ResponseBody ResponseEntity<Resource> words(@PathVariable("competitionCode") String competitionCode, @PathVariable("id") long id) throws IOException, InterruptedException {
        String competitionName = reportService.getCompetitionByCode(competitionCode);
        byte[] bytes = reportService.getRefByMatchesByRound(competitionName, id);
        Resource resource = new InputStreamResource(new ByteArrayInputStream(bytes));
        //return new HttpEntity<Resource>(resource);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + competitionCode + "_round_" + id + ".docx" + "\"")
                .body(resource);
    }

    @GetMapping("/refresh")
    public void refreshData() throws IOException, InterruptedException{
        reportService.refreshData();
    }

    @GetMapping("/competitions/all")
    public List<String> getChampionshipNames() {
        return reportService.getChampionshipNames();
    }

}
