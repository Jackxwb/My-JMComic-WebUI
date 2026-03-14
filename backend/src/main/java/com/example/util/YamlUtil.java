package com.example.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YamlUtil {
    /**
     * 更新 YAML 文件中的简单键值对，同时保持所有注释和空格不变
     * @param filePath 文件路径
     * @param updates 要更新的键值对映射，Key 支持多级（如 "client.impl"）
     */
    public static void updateYaml(Path filePath, Map<String, String> updates) throws IOException {
        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        List<String> updatedLines = new ArrayList<>(lines);

        updates.forEach((keyPath, newValue) -> {
            String[] keys = keyPath.split("\\.");
            int currentDepth = 0;

            // 遍历每一行进行匹配替换
            for (int i = 0; i < updatedLines.size(); i++) {
                String line = updatedLines.get(i);
                String trimmedLine = line.trim();

                // 忽略纯注释行
                if (trimmedLine.startsWith("#") || trimmedLine.isEmpty()) continue;

                // 匹配当前层级的 Key
                String currentKey = keys[currentDepth];
                // 正则说明：匹配行首空格 + Key + 冒号 + 后面跟随的内容
                Pattern pattern = Pattern.compile("^(\\s*)" + Pattern.quote(currentKey) + ":(\\s*)(.*)$");
                Matcher matcher = pattern.matcher(line);

                if (matcher.find()) {
                    // 如果已经到达目标最深层级（例如 client.impl 中的 impl）
                    if (currentDepth == keys.length - 1) {
                        String indent = matcher.group(1);
                        String spaceAfterColon = matcher.group(2);
                        String existingValueAndComment = matcher.group(3);

                        // 保留行尾注释（如果存在）
                        String comment = "";
                        if (existingValueAndComment.contains("#")) {
                            comment = " " + existingValueAndComment.substring(existingValueAndComment.indexOf("#"));
                        }

                        // 组装新行：缩进 + key: + 空格 + 新值 + 原有行尾注释
                        String newLine = indent + currentKey + ":" + spaceAfterColon + newValue + comment;
                        updatedLines.set(i, newLine);
                        return; // 完成该 keyPath 的更新
                    } else {
                        // 还没到最后一级，继续向下找
                        currentDepth++;
                    }
                }
            }
        });

        Files.write(filePath, updatedLines, StandardCharsets.UTF_8);
    }

    public static void updateYaml(Path filePath, JsonNode node) throws IOException{
        Map<String, String> data = flatten(node);
        updateYaml(filePath, data);
    }


    /**
     * 将 JsonNode 扁平化为路径 Map
     * 输入：{ "client": { "impl": "api" } }
     * 输出：{ "client.impl": "api" }
     */
    public static Map<String, String> flatten(JsonNode node) {
        Map<String, String> flatMap = new HashMap<>();
        traverseAndFlatten("", node, flatMap);
        return flatMap;
    }
    private static void traverseAndFlatten(String prefix, JsonNode node, Map<String, String> map) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            // 使用 Java 21 的语法糖遍历字段
            objectNode.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                String newPrefix = prefix.isEmpty() ? key : prefix + "." + key;
                traverseAndFlatten(newPrefix, value, map);
            });
        } else if (node.isArray()) {
            // 注意：对于数组（如 domain），扁平化通常取其索引或整体字符串
            // 在 option.yml 场景下，建议将数组转为 YAML 风格的字符串
            map.put(prefix, node.toString());
        } else if (!node.isMissingNode() && !node.isNull()) {
            // 基础类型：String, Number, Boolean
            // asText() 会去掉引号，适合直接写入 YAML
            map.put(prefix, node.asText());
        }
    }
}
