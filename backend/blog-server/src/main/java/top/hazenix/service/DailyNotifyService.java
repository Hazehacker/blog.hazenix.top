package top.hazenix.service;

import java.time.LocalDate;

public interface DailyNotifyService {
    void runForDate(LocalDate statDate);
    void sendTestMail();
}
