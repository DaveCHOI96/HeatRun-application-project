package com.main.heatrun.global.fcm;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class FcmService {

    // --- 단일 디바이스 푸시 알림 ---
    public void sendToDevice(String fcmToken, String title, String body) {
        if (fcmToken == null || fcmToken.isBlank()) {
            log.warn("FCM 토큰 없음 - 푸시 알림 스킵");
            return;
        }

        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder()
                                    .setSound("default")
                                    .build())
                            .build())
                    .build();
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 발송 성공: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 발송 실패: token={}, error={}", fcmToken, e.getMessage());
        }
    }

    // 여러 디바이스 푸시 알림 (크루 전체)
    public void sendToDevices(List<String> fcmTokens, String title, String body) {
        // null / 빈 토큰 필터링
        List<String> validTokens = fcmTokens.stream()
                .filter(token -> token != null && !token.isBlank())
                .toList();

        if (validTokens.isEmpty()) {
            log.warn("유효한 FCM 토큰 없음 - 푸시 알림 스킵");
            return;
        }

        try {
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(validTokens)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder()
                                    .setSound("default")
                                    .build())
                            .build())
                    .build();

            BatchResponse response = FirebaseMessaging
                    .getInstance()
                    .sendEachForMulticast(message);

            log.info("FCM 멀티캐스트 발송: 성공={}, 실패={}",
                    response.getSuccessCount(), response.getFailureCount());
        } catch (FirebaseMessagingException e) {
            log.error("FCM 멀티캐스트 발송 실패: {}", e.getMessage());
        }
    }

    // 응원 수신 알림
    public void sendCheerNotification(String fcmToken, String senderNickname) {
        sendToDevice(fcmToken, "응원이 도착했어요!", senderNickname + "님이 응원을 보냈습니다. 화이팅!");
    }

    // 스트릭 유지 알림
    public void sendStreakReminderNotification(String fcmToken, int currentStreak) {
        sendToDevice(fcmToken, "오늘도 달려볼까요?", "현재 " + currentStreak + "일 연속 달리기 중! 스트릭을 유지하세요." );
    }

    // 크루 점령률 달성 알림
    public void sendCrewConquestNotification(List<String> fcmTokens, String crewName, String areaName, int percentage) {
        sendToDevices(fcmTokens, "\uD83C\uDFC6" + crewName + " 영역 점령!",
                areaName + " " + percentage + "% 점령 달성!");
    }

    // 레벨업 알림
    public void sendLevelUpNotification(String fcmToken, int newLevel) {
        sendToDevice(fcmToken, "레벨업!", "축하해요! Lv." + newLevel + " 달성!");
    }

    // 고스트 추월 알림
    public void sendGhostOvertakeNotification(String fcmToken) {
        sendToDevice(fcmToken, "고스트 추월!", "과거의 나를 추월했어요! 이 페이스 유지하세요!");
    }



}
