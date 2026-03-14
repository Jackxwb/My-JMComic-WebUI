package com.example.entity;


import com.example.util.StringUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;
import java.util.Map;

@RegisterForReflection
@JsonIgnoreProperties(ignoreUnknown = true) // 忽略类中没有定义的字段
public class JmcomicConfig {
    public Boolean log;
    public Client client;
    @RegisterForReflection
    public static class Client{
        public String impl;
        public Map<String, List<String>> domain;
        public Integer retry_times;
        public Postman postman;
        @RegisterForReflection
        public static class Postman{
            public MetaData meta_data;
            @RegisterForReflection
            public static class MetaData{
                public String proxies;
                public Cookies cookies;
                @RegisterForReflection
                public static class Cookies{
                    public String AVS;
                }
            }
        }
    }

    public DirRule dir_rule;
    @RegisterForReflection
    public static class DirRule{
        public String base_dir;
        public String rule;
    }

    public Download download;
    @RegisterForReflection
    public static class Download{
        public Boolean cache;
        public Image image;
        @RegisterForReflection
        public static class Image{
            public Boolean decode;
            public String suffix;
        }
        public Threading threading;
        @RegisterForReflection
        public static class Threading{
            public Integer image;
            public Integer photo;
        }
    }

    public void updateNode(JsonNode node){
        // 确保 node 是一个 ObjectNode
        if (!(node instanceof ObjectNode rootObject)) {
            return;
        }

        // 1. 更新基础开关 (log)
        ((ObjectNode) node).put("log", true);

        if(this.client!=null){
            // 2. 更新 Client 相关配置 (层级：client -> ...)
            var client = node.withObjectProperty("client");
            client.put("impl", this.client.impl); // 切换为 APP 端以获得更好兼容性
            client.put("retry_times", this.client.retry_times);
            //client.putNull("cache"); // 对应配置中的 cache: null

            // 3. 更新 Domain 列表 (处理 ArrayNode)
            var domain = client.withObjectProperty("domain");//.withArrayProperty("api");
            ArrayNode domainList;
            String currentImpl = (this.client.impl != null) ? this.client.impl : "api";
            if("html".equals(currentImpl)){
                domainList = domain.withArrayProperty("html");
            }else{
                domainList = domain.withArrayProperty("api");
            }
//            if(this.client.domain!=null && this.client.domain.length>0){
//                domainList.removeAll();
//                Arrays.stream(this.client.domain).forEach(domainList::add);
//            }

            if (this.client.domain != null && this.client.domain.containsKey(currentImpl)) {
                // 获取对应 impl 的域名列表
                List<String> specificDomains = this.client.domain.get(currentImpl);
                if (specificDomains != null && !specificDomains.isEmpty()) {
                    domainList.removeAll(); // 清理旧节点
                    // 使用 Java 21 的 List 直接遍历
                    specificDomains.forEach(domainList::add);
                }
            }

            // 4. 更新 Postman & Cookies (深度嵌套)
            if(this.client.postman!=null && this.client.postman.meta_data!=null){
                ObjectNode metaData = client.withObjectProperty("postman")
                        .withObjectProperty("meta_data");
                if(StringUtil.notNull(this.client.postman.meta_data.proxies)){
                    metaData.put("proxies", this.client.postman.meta_data.proxies);
                }
                if(this.client.postman.meta_data.cookies!=null && this.client.postman.meta_data.cookies.AVS!=null){
                    metaData.withObjectProperty("cookies").put("AVS", this.client.postman.meta_data.cookies.AVS);
                }
            }
        }

        if(this.download!=null){
            // 5. 更新下载参数 (层级：download -> threading -> ...)
            var download = node.withObjectProperty("download");
            if(this.download.cache!=null){
                download.put("cache", this.download.cache);
            }

            if(this.download.image!=null){
                var image = download.withObjectProperty("image");
                if(this.download.image.decode!=null){
                    image.put("decode", this.download.image.decode);
                }
                if(StringUtil.notNull(this.download.image.suffix)){
                    image.put("suffix", this.download.image.suffix);
                }

                if(this.download.threading!=null){
                    var threading = download.withObjectProperty("threading");
                    if(StringUtil.notNull(this.download.threading.image)){
                        threading.put("image", this.download.threading.image);
                    }
                    if(StringUtil.notNull(this.download.threading.photo)){
                        threading.put("photo", this.download.threading.photo);
                    }
                }
            }

        }


        // 6. 更新文件夹规则 (dir_rule)
        if(this.dir_rule!=null){
            var dirRule = node.withObjectProperty("dir_rule");
            dirRule.put("base_dir", this.dir_rule.base_dir);
            dirRule.put("rule", this.dir_rule.rule);
        }
    }
}
