package com.example.service;

import com.example.entity.ApiResponse;
import com.example.entity.JmcomicConfig;
import com.example.util.YamlUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
//import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@ApplicationScoped
public class ConfigService {

    private static final Path CONFIG_PATH = Path.of("./option.yml");
    private final ObjectMapper yamlMapper;

    public ConfigService() {
        // 配置 YAMLFactory 以保留尽量多的原始特征
        YAMLFactory factory = new YAMLFactory()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER) // 不写入 "---"
                .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);       // 最小化引号，保持简洁

        this.yamlMapper = new ObjectMapper(factory);
    }

    public ApiResponse loadConfig(){
        //Yaml yaml = new Yaml();
        try (InputStream inputStream = Files.newInputStream(CONFIG_PATH)) {
            JmcomicConfig yml = yamlMapper.readValue(inputStream, JmcomicConfig.class);
            //JmcomicConfig yml = yaml.load(inputStream);
            return ApiResponse.returnOK().setDataNow(yml);
        }catch (IOException e){
            log.error("读取文件失败: {}", e.getMessage());
            return ApiResponse.returnFail("读取文件失败").setDataNow(e.getMessage());
        }
    }

    public ApiResponse saveConfig(JmcomicConfig data){
        /*Yaml yaml = new Yaml();
        try (FileWriter writer = new FileWriter("./config.yml")) {
            yaml.dump(data, writer);
            return ApiResponse.returnOK();
        } catch (IOException e) {
            //e.printStackTrace();
            log.error("保存失败");
            return ApiResponse.returnFail("保存失败");
        }*/
        checkConfig();
//        File file = CONFIG_PATH.toFile();
        try{
            //JsonNode node = yamlMapper.readTree(file);
            //data.updateNode(node);
            //yamlMapper.writeValue(file, node);

            JsonNode node = yamlMapper.valueToTree(data);
            YamlUtil.updateYaml(CONFIG_PATH, node);
        }catch (Exception e){
            log.error("写出配置文件失败！{}", e.getMessage());
            return ApiResponse.returnFail("写出配置文件失败").setDataNow(e.getMessage());
        }
        return ApiResponse.returnOK();
    }

    private void checkConfig(){
        if(!Files.exists(CONFIG_PATH)){
            initConfig();
        }
    }
    
    private static final String defConfig= """
# 配置客户端相关
client:
  # impl: 客户端实现类，不配置默认会使用 JmModuleConfig.DEFAULT_CLIENT_IMPL
  # 可配置:
  #  html - 表示网页端
  #  api - 表示APP端
  # APP端不限ip兼容性好，网页端限制ip地区但效率高
  #impl: html
  impl: api
  postman:
    meta_data:
      proxies: null
      cookies:
        AVS: tn0pd4454omfkf0a3qe1s6ni77
  retry_times: 5

dir_rule: # 下载路径规则
  base_dir: .\\Archive
  rule: Bd_Aname_Pindextitle

download:
  # 如果要下载的文件在磁盘上已存在，不用再下一遍
  cache: true
  image:
    decode: true
    suffix: .jpg # 转为jpg格式的图片
  threading:
    # 同时下载的图片数
    # PS: 禁漫网页一次最多请求50张图
    image: 10
    # 同时下载的章节数
    photo: 
""";
    private void initConfig(){
        try {
            Files.writeString(CONFIG_PATH, defConfig);
        } catch (IOException e) {
            //throw new RuntimeException(e);
            log.error("创建默认配置失败! {}", e.getMessage());
        }
    }
}
