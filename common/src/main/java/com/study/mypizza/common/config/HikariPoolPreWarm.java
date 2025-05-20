package com.study.mypizza.common.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class HikariPoolPreWarm {

    private final DataSource dataSource;

    // pre-warm할 커넥션 수 (maximumPoolSize에 맞게 설정하세요)
    // 이 클래스 없어도 지정된 수 만큼 만들어줌
    private static final int WARMUP_CONNECTIONS = 10;

    @PostConstruct
    public void warmUp() {
        System.out.print("🔧 HikariCP 커넥션 풀 Pre-warming 시작... ");

        for (int i = 0; i < WARMUP_CONNECTIONS; i++) {
            try (Connection conn = dataSource.getConnection()) {
                // 바로 반환하면 풀에 커넥션이 채워짐
            } catch (SQLException e) {
                System.err.println("⚠️ 실패: " + e.getMessage());
            }
        }

        System.out.println("✅ 완료 (" + WARMUP_CONNECTIONS + "개)");
    }
}
