package com.example.service;

import com.example.entity.FileItem;
import com.example.util.FileUtil;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@ApplicationScoped // 或者 @Singleton
public class LibraryService {

    public List<FileItem> listFile(String path){
        List<FileItem> fileItems = getFiles(path);

        Collections.sort(fileItems, new Comparator<FileItem>() {
            @Override
            public int compare(FileItem o1, FileItem o2) {
//                System.out.println(o1.name + " -- " + o2.name + o1.name.compareTo(o2.name));
//                return o1.name.compareTo(o2.name);
                return FileUtil.compare(o1.name, o2.name);
            }
        });

        return fileItems;
    }

    private static final int batchSize = 500; // 分批次查询, 建议每批 500-1000，平衡网络开销与内存占用
    private List<FileItem> getFiles(String pathStr){
//        if (files == null || files.length == 0) {
//            return List.of();
//        }
        Path dir = Paths.get(pathStr);
        List<String> paths = new ArrayList<>();

        // 1. 提取所有绝对路径 (Java 21 Stream 优化)
        // DirectoryStream 会在 OS 层面流式读取，不会像 listFiles() 那样一次性申请大数组
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            //paths = new ArrayList<>();
            for (Path entry : stream) {
                // 直接将 Path 转为绝对路径字符串并加入列表
                paths.add(entry.toAbsolutePath().toString());
            }
        } catch (IOException e) {
            // 处理目录不存在或权限问题
            return List.of();
        }

        // 2. 批量查询数据库中已存在的记录
        // 使用 Panache 的 list("path in ?1", List) 语法
        //List<FileItem> existingItems = FileItem.list("path in ?1", paths);

        //如果 paths 列表超过 1000 个，JPA 查询会因为 SQL 参数上限而报错
        //分批次查询
        final List<String> finalPaths = paths;
        List<FileItem> existingItems = IntStream.range(0, (paths.size() + batchSize - 1) / batchSize)
                .mapToObj(i -> {
                    int start = i * batchSize;
                    int end = Math.min(start + batchSize, finalPaths.size());
                    return finalPaths.subList(start, end);
                })
                .flatMap(batch -> FileItem.<FileItem>find("path in ?1", batch).<FileItem>stream())
                .toList();

        // 3. 将查询结果转为 Map，方便 O(1) 效率查找 (Key: path, Value: FileItem)
        Map<String, FileItem> itemMap = existingItems.stream().peek(FileItem::updateFileItem)
                .collect(Collectors.toMap(item -> item.path, item -> item));

        existingItems = null;

        // 4. 循环 files，匹配或创建新对象
        return paths.stream().map(path -> {
                    // 如果 Map 中存在则返回，不存在则 new 一个（不存入 DB）
                    return itemMap.computeIfAbsent(path, k->{
                        // Lambda 表达式是懒加载的。只有 itemMap 中找不到路径时，才会调用 create
                        return FileItem.create(Path.of(path));
                    });
                })
                .collect(Collectors.toList());
    }
}
