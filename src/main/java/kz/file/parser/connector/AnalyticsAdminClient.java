package kz.file.parser.connector;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "analyticsAdminClient", url = "${app.analytics.base-url}")
public interface AnalyticsAdminClient {

  @DeleteMapping("/api/admin/product-analytics/period")
  void deletePeriod(
      @RequestHeader("Authorization") String authorization,
      @RequestParam("year") Integer year,
      @RequestParam("month") Integer month);

  @DeleteMapping("/api/admin/product-analytics/tariffs")
  Integer deleteTariffs(@RequestHeader("Authorization") String authorization);
}
