package com.example.entity;

//import javax.persistence.Entity;
//import javax.persistence.Id;
import com.example.config.DoubleStringConverter;
import com.example.util.FileUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.Arc;
import io.quarkus.arc.ManagedContext;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Time;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
@Entity
@RegisterForReflection
public class FileItem extends PanacheEntity {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    public Long id;
    public String name;
    @Lob
    @Column(name="path", unique = true, columnDefinition = "TEXT")
    public String path;
    public boolean isFile;
    //public Time time;
    /**
     * 本对象最后修改日期
     */
    public LocalDateTime time;
    @Lob
    @Column(name = "image", columnDefinition = "TEXT")
    public String image;
    /**
     * tag标签
     */
    public String mark;
    /**
     * 是否已阅
     */
    public Boolean isSeen;
    /**
     * 评分
     */
    public Byte rate;
    /**
     * 本子ID
     */
    public String aid;
    /**
     * 子目录下文件数量
     */
    public Integer subdirectories;
    /**
     * 图片比例，前端虚拟渲染需要知道图片宽高
     *
     */
    @Convert(converter = DoubleStringConverter.class)// 使用转换器
    @Column(name = "ratio", columnDefinition = "VARCHAR(20)")// 保存数据库使用字符串
    @JsonFormat(shape = JsonFormat.Shape.STRING)// 序列化为 JSON 时强制转为 String
    public Double ratio;

    private boolean checkPath(File file){
        if(file==null){
            log.error("文件不存在: [{}]", path);
            return false;
        }
        if (!file.exists()){
            log.error("文件不存在: [{}]", path);
            return false;
        }
        return true;
    }
    private boolean checkPath(Path path){
        if(path==null || !Files.exists(path)){
            log.error("文件不存在: [{}]", path);
            return false;
        }
        return true;
    }
    public void updateTime(){
        updateTime(new File(path));
    }
//    private static final ZoneOffset defZoneOffset = ZoneOffset.of("+8");
    private static final ZoneId defZone = ZoneId.of("Asia/Shanghai");
    public void updateTime(File file){
        if (!checkPath(file)){return;}
//        if(time==null){
//            //time = new Time(file.lastModified());
//        }else{
//            //time.setTime(file.lastModified());
//        }
//        time = LocalDateTime.ofEpochSecond(file.lastModified(), 0, defZoneOffset);
        time = Instant.ofEpochMilli(file.lastModified()).atZone(defZone).toLocalDateTime();
    }
    public void updateImage(){updateImage(new File(path));}
    public void updateImage(File file){
        if (!checkPath(file)){return;}
        if(file.isFile()){
            if(FileUtil.isFileImage(file)){
                image = path;
            }
        }else{
            image = FileUtil.findDirImage(file);
        }
    }
    public void updateImage(Path path){
        if (!checkPath(path)){return;}
        if(!Files.isDirectory(path)){
            if(FileUtil.isFileImage(path)){
                image = path.toAbsolutePath().toString();
            }
        }else{
            image = FileUtil.findDirImage(path);
        }
    }
    public void updateIsFile(){
        //updateIsFile(new File(path));
        updateIsFile(Path.of(path));
    }
    public void updateIsFile(File file){
        isFile = file.isFile();
    }
    public void updateIsFile(Path path){
        isFile = !Files.isDirectory(path);
    }
    public void updateSubdirectories(Path path){
//        String[] tmp = file.list();
//        if(tmp!=null && !isFile){
//            subdirectories = tmp.length;
//        }
//        Path path = file.toPath();
        // 使用 try-with-resources 确保 Stream（句柄）及时关闭
        try (Stream<Path> stream = Files.list(path)) {
            // .count() 在底层比获取全量 String[] 快得多
            this.subdirectories = (int) stream.count();
        } catch (Exception e) {
            // 处理权限不足或非目录情况
            this.subdirectories = 0;
        }
    }
    public void updateSubdirectories(){
        //updateSubdirectories(new File(path));
        updateSubdirectories(Path.of(path));
    }

    /**
     * 更新基础信息
     * @param path 文件路径
     * @return 信息是否已更改
     */
    public boolean updatePathBasicAttributes(Path path){
        //Files.readBasicFileAttributes
        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            LocalDateTime nowLastModifiedTime = Instant.ofEpochMilli(attrs.lastModifiedTime().toMillis()).atZone(defZone).toLocalDateTime();
            if(nowLastModifiedTime.equals(time)){
                return false;
            }
            isFile = !attrs.isDirectory();
            time = nowLastModifiedTime;
        } catch (IOException e) {
            //throw new RuntimeException(e);
            log.error("更新文件失败：{}", e.getMessage());
        }
        return true;
    }

    /**
     * 更新数据
     * @param fileItem
     */
    public FileItem updateData(FileItem fileItem){
        name = fileItem.name;
        path = fileItem.path;
        isFile = fileItem.isFile;
        time = fileItem.time;
        mark = fileItem.mark;
        isSeen = fileItem.isSeen;
        rate = fileItem.rate;
        aid = fileItem.aid;
        subdirectories = fileItem.subdirectories;
        ratio = fileItem.ratio;
        return this;
    }

    /*@Transactional
    public static FileItem create(File file){
        FileItem item = new FileItem();
        item.name = file.getName();
        item.path = file.getAbsolutePath();
        item.isFile = file.isFile();
        item.mark = "";
        item.isSeen = false;
        item.rate = 0;
        item.subdirectories = 0;

        item.updateTime(file);
        item.updateImage(file);
        item.updateIsFile(file);
        item.updateSubdirectories(file);

        // 保存对象
        item.persistAndFlush();
        return item;
    }*/
    @Transactional
    public static FileItem create(Path path){
        FileItem item = new FileItem();
        item.name = path.getFileName().toString();
        item.path = path.toAbsolutePath().toString();
        //item.isFile = !Files.isDirectory(path);
        item.mark = "";
        item.isSeen = false;
        item.rate = 0;
        item.subdirectories = 0;

        item.updateFileItem(path);

        // 保存对象
        item.persistAndFlush();
        return item;
    }
//    /**
//     * 获取文件信息，弃用
//     * @param file
//     * @return
//     */
//    @Transactional
//    public static FileItem getFormPath(File file){
//        String path_ = file.getAbsolutePath();
//        FileItem result = FileItem.find("path", path_).firstResult();
//        if(result==null){
//            return create(file);
//        }
////        result.updateTime(file);
////        result.updateImage(file);
////        result.updateIsFile(file);
////        result.updateSubdirectories(file);
//        result.updateFileItem(file);
//        return result;
//    }
//    public void updateFileItem(File file){
//        this.updateTime(file);
//        this.updateImage(file);
//        this.updateIsFile(file);
//        this.updateSubdirectories(file);
//    }
    public static FileItem getFormPath(String str){
        return getFormPath(Path.of(str));
    }
    private static FileItem getFormDataBase(String path){
        ManagedContext requestContext = Arc.container().requestContext();
        if (requestContext.isActive()) {
            FileItem item = FileItem.find("path", path).firstResult();
            if(item==null){
                return null;
            }
            // 解除JPA托管
            FileItem.getEntityManager().detach(item);
            return item;
        }else{
            try {
                requestContext.activate();
                FileItem item = FileItem.find("path", path).firstResult();
                if(item==null){
                    return null;
                }
                // 解除JPA托管
                FileItem.getEntityManager().detach(item);
                return item;
            } finally {
                requestContext.terminate();
            }
        }
    }
//    @Transactional
    public static FileItem getFormPath(Path path){
        if(!Files.exists(path)){
            log.error("路径不存在！{}", path.toString());
            return null;
        }
        String AbsolutePath = path.toAbsolutePath().toString();
//        FileItem result = FileItem.find("path", AbsolutePath).firstResult();
        // 解除JPA托管
//        FileItem.getEntityManager().detach(result);
        FileItem result = getFormDataBase(AbsolutePath);
        if (result==null){
            return create(path);
        }
        result.updateFileItem(path);
        return result;
    }
    public void updateFileItem(){
        updateFileItem(Path.of(this.path));
    }
    public void updateFileItem(Path path){
        boolean isUpdate = this.updatePathBasicAttributes(path);
        if(isFile){
            if(isUpdate){
                this.updateImage(path);
            }
            if(isUpdate  || this.ratio==null || this.ratio == 0){
                // 更新图片比例
                FileUtil.ImageDimension imageDimension = FileUtil.getImageDimension(path);
                if(imageDimension!=null){
                    this.ratio = imageDimension.ratio();
                }
            }
        }else{
            this.updateImage(path);
            this.updateSubdirectories(path);
        }

    }
//    @Transactional
//    @ActivateRequestContext
//    public void save(){
//        this.persistAndFlush();
//    }
//    public static FileItem getFormPath(String path){
//        return getFormPath(new File(path));
//    }

    /**
     * 自动转换路径
     * <p> 将相对路径转换成绝对路径 </p>
     * @param path
     * @return
     */
    public static String AutomaticProcessingPath(String path){
        Path path_ = Paths.get(path);
        path_ = path_.normalize();
        try {
            path_ = path_.toRealPath(LinkOption.NOFOLLOW_LINKS);
        } catch (IOException e) {
            //throw new RuntimeException(e);
            log.warn("路径转换出现异常, {} <> {}", path, path_.toString());
        }
        return path_.toString();
    }

    public void addMark(String tag){
        Set<String> markSet = initMarkSet();
        markSet.add(tag);
        try {
            saveMarkSet(markSet);
        } catch (JsonProcessingException e) {
            //throw new RuntimeException(e);
            log.error("保存Mark失败！{}", e.getMessage());
        }
    }
    public void addMark(Set<String> tags){
        Set<String> markSet = initMarkSet();
        markSet.addAll(tags);
        try {
            saveMarkSet(markSet);
        } catch (JsonProcessingException e) {
            //throw new RuntimeException(e);
            log.error("保存Mark失败！{}", e.getMessage());
        }
    }
    public void addMark(String[] tags){
        Set<String> markSet = initMarkSet();
        markSet.addAll(List.of(tags));
        try {
            saveMarkSet(markSet);
        } catch (JsonProcessingException e) {
            //throw new RuntimeException(e);
            log.error("保存Mark失败！{}", e.getMessage());
        }
    }
    private Set<String> initMarkSet(){
        ObjectMapper mapper = new ObjectMapper();
        Set<String> markSet = new LinkedHashSet<>();
        try {
            markSet = mapper.readValue(mark, markSet.getClass());
        } catch (JsonProcessingException e) {
            String tmp = mark;
            if (tmp!=null && !tmp.isEmpty()){
                if(tmp.startsWith("[")){
                    tmp = tmp.substring(1);
                }
                if(tmp.startsWith("]")){
                    tmp = tmp.substring(0, tmp.length()-2);
                }
                String[] arr = tmp.split(",");
                markSet.addAll(List.of(arr));
            }
            //throw new RuntimeException(e);
        }
        mapper.clearProblemHandlers();
        mapper = null;
        return markSet;
    }
    private void saveMarkSet(Set<String> data) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mark = mapper.writeValueAsString(data);
    }
}