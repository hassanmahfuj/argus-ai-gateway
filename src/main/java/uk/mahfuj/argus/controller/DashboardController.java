package uk.mahfuj.argus.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.mahfuj.argus.service.DashboardService;
import uk.mahfuj.argus.service.dto.DashboardResponse;

import java.time.Instant;


@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(final DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/usage")
    public ResponseEntity<DashboardResponse> getUsage(
            @RequestParam(defaultValue = "day") final String granularity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final Instant to
    ) {
        return ResponseEntity.ok(dashboardService.getDashboardData(granularity, from, to));
    }
}
