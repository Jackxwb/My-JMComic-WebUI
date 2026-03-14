package com.example.service.BackendBashTask;

import com.example.entity.BackendBashTask;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@RegisterForReflection // 可反射访问类
public class BaseBackendBashTask {

    @Getter
    private Long id;
    @Getter
    private Long pid;
    private ProcessBuilder pb;
    private Process process;
    @Getter
    private boolean isRunning;
    @Getter
    private boolean isManualStop;
    @Getter
    private int exitCode;
    @Getter
    private String errMessage;

    //@Getter
    protected String[] command;
    // 记录日志模式，0=不记录，1=记录最后一行，2=记录全量
    @Setter
    private byte logMode=0;
    public static final byte LOGMODE_NOT_USE = 0;
    public static final byte LOGMODE_LAST_LINE = 1;
    public static final byte LOGMODE_FULL = 2;
    @Getter
    private String log_lastLine;
    @Getter
    private StringBuffer log_full;
    // 日志中的编码格式
    @Getter
    @Setter
    private String charseName;

    @Getter
    private long time;
    @Getter
    private long endTime;

//    @Setter
//    protected EntityManager entityManager;// 数据库状态管理类

    BaseBackendBashTask(){
        id = System.nanoTime();
        isRunning = false;
        isManualStop = false;
        //charseName = "UTF-8";
    }
    public BaseBackendBashTask(String commandStr){
        this();
        List<String> commandList = new ArrayList<>();
        //commandList = new String[3];
//        String osName = System.getProperty("os.name").toLowerCase();
//        if(osName.contains("win")){
//            //commandList[0] = "cmd";
//            commandList.add("cmd");
//            charseName = "GBK";
//        }
//        else{
//            //commandList[0] = "/bin/sh";
//            commandList.add("/bin/bash");
//            charseName = "UTF-8";
//        }
//        commandList.add("/c");
        //commandList[1] = "/c";
        //commandList[2] = commandStr;

        String[] arrs = commandStr.split(" ");
        commandList.addAll(List.of(arrs));
        command = commandList.toArray(new String[0]);
    }
    public BaseBackendBashTask(String[] commandArr){
        this();
        this.command = commandArr;
    }
    public BaseBackendBashTask(List<String> commandArr){
        this();
        this.command = commandArr.toArray(new String[0]);
    }

    //@Transactional
    //@ActivateRequestContext
    //@RunOnVirtualThread
    public void startRun() {
        isRunning = true;
        isManualStop = false;
        time = System.currentTimeMillis();

        ProcessBuilder pb = new ProcessBuilder(command);
        // 将标准错误流重定向到标准输出流
        pb.redirectErrorStream(true);

        try {
            startHook();

            process = pb.start();
            pid = process.pid();

            // 重要的：启动单独的线程来消耗进程的输出流，防止进程阻塞
            // 使用 ExecutorService 来管理线程
//            Executors.newSingleThreadExecutor().submit(() -> {
//            });
            //Executors.newSingleThreadExecutor().submit(this::logPrint);
            //this.logPrint(); // 改成 @RunOnVirtualThread 使用虚拟线程托管
            Thread.startVirtualThread(this::logPrint);
        } catch (Exception e) {
            errMessage = e.getMessage();
            exitHook(2, "异常: "+errMessage);
        }
    }

    public void manualStop(){
        if (process!=null){
            isManualStop = true;
            exitHook(2, "手动停止");
            process.destroy();
            try {
                // 等待进程终止，给它一些时间
                if (!process.waitFor(20, TimeUnit.SECONDS)) { // 最多等待20秒
                    process.destroyForcibly();
                    isRunning = false;
                }else{
                    isRunning = false;
                }
            } catch (InterruptedException e) {
                errMessage =e.getMessage();
            }
        }
    }
    public void updateRunStatus(){
        if (process!=null){
            isRunning = process.isAlive();
        }
    }

    public void startHook(){}
    //@Transactional
    //@ActivateRequestContext
    public void logHook(String logLine){}
    public void exitHook(int exitCode, String errMessage){
        endTime = System.currentTimeMillis();
        isRunning = false;
        this.exitCode = exitCode;
        if(this.errMessage==null || this.errMessage.isEmpty()){
            this.errMessage = errMessage;
        }else{
            this.errMessage += "\r\n" + errMessage;
        }
    }
    //@Transactional
    //@ActivateRequestContext
    private void logPrint(){
        try {
            switch (logMode){
                case LOGMODE_NOT_USE -> {
                    try {
                        process.waitFor();
                        exitHook(1, "");
                    } catch (InterruptedException e) {
                        exitHook(2, "未知异常");
                        throw new RuntimeException(e);
                    }
                }
                case LOGMODE_FULL -> {
                    log_full = new StringBuffer();
                    BufferedReader out = null;
                    if(charseName!=null && !charseName.trim().isEmpty()){
                        out = new BufferedReader(new InputStreamReader(process.getInputStream(), charseName));
                    }else{
                        out = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    }
                    String line;
                    while ((line = out.readLine()) != null) {
                        log_full.append(line);
                        log_full.append("\r\n");
                        logHook(line);
                    }
                    exitHook(1, "");
                }
                case LOGMODE_LAST_LINE -> {
                    BufferedReader out = null;
                    if(charseName!=null && !charseName.trim().isEmpty()){
                        out = new BufferedReader(new InputStreamReader(process.getInputStream(), charseName));
                    }else{
                        out = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    }
                    String line;
                    while ((line = out.readLine()) != null) {
                        line = line.trim();
                        if(line.isEmpty()){
                            continue;
                        }
                        log_lastLine = line;
                        logHook(line);
                    }
                    exitHook(1, "");
                }
            }
        } catch (Exception e) {
            errMessage = e.getMessage();
            exitHook(2, "日志记录异常: "+errMessage);
            e.printStackTrace();
        }finally {
            isRunning = false;
        }
    }

    public void waitFor() throws InterruptedException {
        if (process!=null){
            process.waitFor();
        }
    }
    public void waitFor(int seconds) throws InterruptedException {
        if (process!=null){
            process.waitFor(seconds, TimeUnit.SECONDS);
        }
    }
}
