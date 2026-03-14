package com.example.service.BackendBashTask;

import com.example.entity.FileItem;
import com.example.entity.FixedSizeQueue;
import com.example.service.BackDATABASETask;
import com.example.util.StringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Getter
@RegisterForReflection // 可反射访问类
public class DownloadJMComic extends BaseBackendBashTask {
    private long a_id;
    private String name;
    private String[] tags;
//    private long time;
//    private long endTime;
    private String savePath;

    @Setter
    private String dataSave;
    private Pattern dataSaveP;
//    private Pattern dataSaveP1;
//    private Pattern dataSaveP2;
    private boolean isRelativePath = false;

//    @Inject
//    ExecutorService executorService; // Quarkus 管理的线程池

//    @Inject
//    EntityManager entityManager;

    private FixedSizeQueue<String> logLastNLine;
    public List<String> getLogLastNLine() {
        if(logLastNLine ==null){return null;}
        return logLastNLine.getAll();
    }

    public DownloadJMComic(){
        super();
//        setCharseName("UTF-8");
        //sun.nio.cs.GBK
        logLastNLine = new FixedSizeQueue<>(30);
    }

    public DownloadJMComic(String aID) {
        //super("jmcomic "+ aID + "--option=\""+ System.getProperty("user.dir") + File.separator + "jm.yml\"");
        super("jmcomic "+ aID + " --option=./option.yml");
        setLogMode(BaseBackendBashTask.LOGMODE_LAST_LINE);
        //time = System.currentTimeMillis();
        //name="本子ID："+ aID +" 等待获取标题...";
        a_id = Long.valueOf(aID);
        logLastNLine = new FixedSizeQueue<>(30);
    }


    private static final Pattern c = Pattern.compile("标题:(.*), 关键词:(.*)$");
    @Override
    //@Transactional
    @ActivateRequestContext
    public void logHook(String logLine) {
//        super.logHook(logLine);
        logLastNLine.add(logLine);
        boolean addTagInfo = false;
        if(name==null){
            Matcher m = c.matcher(logLine);
            if(m.find()){
                name = m.group(1).trim();
                String tagArr = m.group(2).trim();
                ObjectMapper mapper = new ObjectMapper();
                try {
                    tagArr = tagArr.replaceAll("'","\"");
                    tags = mapper.readValue(tagArr, String[].class);
                    log.info("本子名字: {}", name);
                    log.info("本子标签: {}", Arrays.toString(tags));
                    addTagInfo = true;
                } catch (JsonProcessingException e) {
                    log.error("tag数组解析异常:[{}] {}", tagArr, e.getMessage());
                    //throw new RuntimeException(e);
                }
            }
        }
        if(savePath==null){
            /*
            if(dataSave.endsWith("\\") || dataSave.endsWith("/")){
                dataSave = dataSave + File.separator;
            }
            */
            dataSave = dataSave.replaceAll("\\\\", "/");

            if(dataSave.startsWith(".")){
                dataSave = dataSave.substring(1);
                isRelativePath = true;
            }
            /*
            if(dataSave.startsWith("/")){
                dataSave = dataSave.substring(1);
            }
            */
            if(!dataSave.endsWith("/")){
                dataSave = dataSave + "/";
            }

            if(dataSaveP == null){
                dataSave = "(" + dataSave + ".*?/)|(" + dataSave + ".*?$)" ;
                log.info("dataSave 1 => {}", dataSave);
                dataSaveP = Pattern.compile(dataSave);
            }

            Matcher m = dataSaveP.matcher(logLine);
            if(m.find()){
                String tmp = m.group(1).trim();
                if (tmp.isEmpty()){
                    tmp = m.group(2).trim();
                }
                if(isRelativePath){
                    savePath = "." + tmp;
                }else{
                    savePath = tmp;
                }

                log.info("保存目录：{}", savePath);
                savePath = FileItem.AutomaticProcessingPath(savePath);
                log.info("保存目录(转换后)：{}", savePath);

                /*try {
                    *//*CompletableFuture.runAsync(()->{ // 开启数据库事务
                        FileItem fileItem = FileItem.getFormPath(savePath);
                        fileItem.aid = String.valueOf(a_id);
                        fileItem.persistAndFlush();
                        log.info("{} > [{}]", fileItem.path, fileItem.aid);
                    });*//*
                    FileItem fileItem = FileItem.getFormPath(savePath);
                    if(StringUtil.isNull(fileItem.aid)){
                        fileItem.aid = String.valueOf(a_id);
                    }
                    if(tags!=null){
                        fileItem.addMark(tags);
                    }
                    //fileItem.persistAndFlush();
                    //fileItem.save();
                    // 2. 将数据传递给新线程处理（不涉及JPA操作）
//                    CompletableFuture.runAsync(() -> {
//                        fileItem.persistAndFlush();
//                        log.info("保存数据到数据库");
//                    }, executorService).exceptionally(ex -> {
//                        log.error("线程出错");
//                        return null;
//                    });
                    BackDATABASETask.addSaveTask(fileItem);
//                    if(fileItem.id == null){
//                        entityManager.persist(fileItem); // 新实体，持久化
//                    }else{
//                        entityManager.merge(fileItem); // 脱管状态，合并
//                    }
                    log.info("更新路径数据 {} > [{}] > [{}]", fileItem.path, fileItem.aid, tags);
                } catch (Exception e) {
                    //throw new RuntimeException(e);
                    log.error("关联下载目录失败! {}", e.getMessage());
                    e.printStackTrace();
                }*/
                updateDataBase("获取路径触发更新");
            }else if (addTagInfo || StringUtil.notNull(name)){
                log.warn("已获取本子资料，但未能定位到找到本子根目录，等待本子路径出现");
            }

            /*
            if(dataSaveP1 == null){
//                Path path_ = Paths.get(dataSave);
//                path_ = path_.normalize();
//                try {
//                    path_ = path_.toRealPath(LinkOption.NOFOLLOW_LINKS);
//                } catch (IOException e) {
//                    //throw new RuntimeException(e);
//                    log.warn("路径转换出现异常, {} <> {}", dataSave, path_.toString());
//                }
//                dataSave = path_.toString();
                dataSave = dataSave + ".*?/" ;
                //dataSave = dataSave.replace("\\","\\\\");
                dataSaveP1 = Pattern.compile(dataSave);
                log.info("dataSave 1 => {}", dataSave);
            }
            if(dataSaveP2 == null){
                dataSave = dataSave + ".*?$";
                //dataSave = dataSave.replace("\\","\\\\");
                dataSaveP2 = Pattern.compile(dataSave);
                log.info("dataSave 2 => {}", dataSave);
            }
            Matcher m1 = dataSaveP1.matcher(logLine);
            if(m1.find()){
                savePath = m1.group(0).trim();
                log.info("保存目录01：{}", savePath);

                try {
                    FileItem fileItem = FileItem.getFormPath(savePath);
                    fileItem.aid = String.valueOf(a_id);
                    fileItem.persistAndFlush();
                } catch (Exception e) {
                    //throw new RuntimeException(e);
                    log.error("关联下载目录失败01! {}", e.getMessage());
                    e.printStackTrace();
                }
            }
            if(savePath == null){
                Matcher m2 = dataSaveP2.matcher(logLine);
                if(m2.find()){
                    savePath = m2.group(0).trim();
                    log.info("保存目录02：{}", savePath);

                    try {
                        FileItem fileItem = FileItem.getFormPath(savePath);
                        fileItem.aid = String.valueOf(a_id);
                        fileItem.persistAndFlush();
                    } catch (Exception e) {
                        //throw new RuntimeException(e);
                        log.error("关联下载目录失败02! {}", e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            */
        } else if (addTagInfo) {// 只有在先获取了路径，后得到tag时才触发更新。正常流程不会触发此处
            updateDataBase("获取Tag触发更新");
        }

        //System.out.printf(">%d> %s\r\n", getPid(), logLine);
        try {
            if("UTF-8".equals(getCharseName())){
                System.out.printf(">%d> %s\r\n", getPid(), logLine);
            }else{
                //System.out.printf(">%d> %s\r\n", getPid(), new String(logLine.getBytes(getCharseName()) , StandardCharsets.UTF_8));
                System.out.printf(">%d> %s\r\n", getPid(), new String(logLine.getBytes(StandardCharsets.UTF_8) , StandardCharsets.UTF_8));
            }
        } catch (Exception e) {// UnsupportedEncodingException
            //throw new RuntimeException(e);
            System.out.printf(">%d> %s\r\n", getPid(), logLine);
        }
//        log.info(">{}> {}",getPid(), logLine);
    }

    private void updateDataBase(String mark){
        try {
            FileItem fileItem = FileItem.getFormPath(savePath);
            if(StringUtil.isNull(fileItem.aid)){
                fileItem.aid = String.valueOf(a_id);
            }
            if(tags!=null){
                fileItem.addMark(tags);
            }
            BackDATABASETask.addSaveTask(fileItem);
            log.info("更新路径数据 {} > [{}] > [{}] | [{}]", fileItem.path, fileItem.aid, tags, mark);
        } catch (Exception e) {
            log.error("关联下载目录失败! {}, | [{}]", e.getMessage(), mark);
            e.printStackTrace();
        }
    }

    @Override
    public void startRun() {
        log.info("开始下载本子：{}", a_id);
        super.startRun();
        log.info("等待运行完成...");
    }

    @Override
    public void exitHook(int exitCode, String errMessage) {
        super.exitHook(exitCode, errMessage);
        //endTime = System.currentTimeMillis();
        log.info("【{}】下载本子结束。{}: {}", a_id, exitCode, errMessage);
        //log.info(new String("编码运行结束[JMComic]: {}".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8), getCharseName());
        log.info("编码运行结束[JMComic]: {}", getCharseName());
    }
}
