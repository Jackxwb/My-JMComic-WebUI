package com.example.service;

import com.example.entity.FileItem;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@ApplicationScoped
public class BackDATABASETask {
    private static final List<FileItem> task = new CopyOnWriteArrayList<>();

    @Inject
    EntityManager entityManager;

    @Scheduled(
            cron = "0/3 * * * * ?", // 每 3 秒执行一次
            concurrentExecution = Scheduled.ConcurrentExecution.SKIP // 禁止并发执行（默认 PROCEED 继续执行，需显式设为SKIP）
    )
    @Transactional
    @ActivateRequestContext
    @RunOnVirtualThread
    public void cronTask() {
        while (!task.isEmpty()){
            try {
                FileItem item = task.getFirst();

                if(item.id == null){
                    entityManager.persist(item); // 新实体，持久化
                }else{
                    entityManager.merge(item); // 脱管状态，合并
                }

                task.remove(item);
                log.info("数据保存成功[{}] => {}", item.path, item.aid);
            } catch (Exception e) {
                log.error("保存出错：{}", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void addSaveTask(FileItem item){
        task.add(item);
    }
}
