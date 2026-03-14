package com.example.config;

import io.quarkus.runtime.ShutdownEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库生命周期管理器
 */
@Slf4j
@ApplicationScoped
public class DatabaseLifecycleManager {
    @Inject
    DataSource dataSource;

    void onStop(@Observes ShutdownEvent ev) {
        log.info("正在执行优雅停机，强制同步 H2 数据到磁盘...");
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            // 执行 CHECKPOINT 会强制将所有更改写入磁盘并压缩日志
            stmt.execute("CHECKPOINT");
            log.info("数据已强制保存，正在释放资源");
            // 执行 SHUTDOWN COMPACT 会确保数据库状态一致并释放文件锁
            stmt.execute("SHUTDOWN COMPACT");
            log.info("H2 数据库已安全关闭。");
        }catch (SQLException e){
            if (e.getErrorCode() == 90121) {
                log.warn("H2 数据库已被提前关闭，跳过手动关闭逻辑。");
            }else{
                log.error("关闭数据库时发生异常: {}", e.getMessage());
            }
        }catch (Exception e) {
            log.error("关闭数据库时发生未知报错: {}", e.getMessage());

        }
    }
}
