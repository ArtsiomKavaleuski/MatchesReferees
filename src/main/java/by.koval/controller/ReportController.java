package by.koval.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;
import by.koval.service.ReportService;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/matches")
@Slf4j
public class ReportController {
    @Autowired
    private final ReportService reportService;

    @GetMapping(value = "/{competition}/round/{id}", produces = "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    public @ResponseBody HttpEntity<Resource> words(@PathVariable("competition") String competition, @PathVariable("id") long id) throws IOException, InterruptedException {
        byte[] bytes = reportService.getRefByMatchesByRound(reportService.getCompetitionByCode(competition), id);
        Resource resource = new InputStreamResource(new ByteArrayInputStream(bytes));
        return new HttpEntity<Resource>(resource);
    }
    //todo
    //need to personalise the filename

    @GetMapping("/refresh")
    public void refreshData() throws IOException, InterruptedException{
        reportService.refreshData();
    }

}
