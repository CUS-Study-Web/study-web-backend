package studyweb.cus.service.log;

public interface LokiQueryService {
  int countEntries(String action, long startNano, long endNano);
}
