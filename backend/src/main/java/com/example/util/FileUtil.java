package com.example.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
import com.example.entity.FileItem;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public class FileUtil {
    public static boolean checkPath(File file){
        if(file==null){
            log.error("文件不存在");
            return false;
        }
        if (!file.exists()){
            log.error("文件不存在: [{}]", file.getPath());
            return false;
        }
        return true;
    }
    public static boolean checkPath(Path path){
        if(path==null){
            log.error("文件不存在");
            return false;
        }
        if (!Files.exists(path)){
            log.error("文件不存在: [{}, {}]", path.toString(), path.toAbsolutePath().toString());
            return false;
        }
        return true;
    }


    //private static final String[] fileSuffix = "bmp,jpg,png,tif,gif,pcx,tga,exif,fpx,svg,psd,cdr,pcd,dxf,ufo,eps,ai,raw,wmf,webp,avif,apng".split(",");
    private static final Set<String> fileSuffix = new HashSet<>();
    static{
        String[] split = "bmp,jpg,png,tif,gif,pcx,tga,exif,fpx,svg,psd,cdr,pcd,dxf,ufo,eps,ai,raw,wmf,webp,avif,apng".split(",");
        fileSuffix.addAll(List.of(split));
    }
    public static String getFileNameSuffix(String name){
        int ii = name.lastIndexOf(".");
        if(ii>0){
            return name.substring(ii+1, name.length());
        }
        return "";
    }
    public static String getFileName(String name){
        int ii = name.indexOf(".");
        if(ii>0){
            return name.substring(0,ii-1);
        }
        return "";
    }
    public static boolean isFileImage(File file){
        if(!checkPath(file)){
            return false;
        }

        String name = file.getName();
        String suffix = getFileNameSuffix(name);

        return fileSuffix.contains(suffix);
    }
    public static boolean isFileImage(Path path){
        if(!checkPath(path)){
            return false;
        }

        String name = path.getFileName().toString();
        String suffix = getFileNameSuffix(name);

        return fileSuffix.contains(suffix);
    }

    public static String findDirImage(File file){
        File[] files = file.listFiles();
        List<String> arr_dir = new ArrayList<>();
        List<String> arr_file = new ArrayList<>();
        if(files==null){
            return null;
        }
        for (int i = 0; i < files.length; i++) {
            File file_ = files[i];
            //String name = getFileName(file_.getName());
            String name = file_.getName();
            if(file_.isFile()){
                if(isFileImage(file_)){
                    arr_file.add(name);
                }
            }else{
                arr_dir.add(name);
            }
        }

        if(!arr_file.isEmpty()){
            arr_file.sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return FileUtil.compare(o1, o2);
                }
            });
            //Collections.sort(arr_file);
            return file.getPath() + File.separator + arr_file.get(0);
        }

        if(!arr_dir.isEmpty()){
            arr_dir.sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return FileUtil.compare(o1, o2);
                }
            });
            //Collections.sort(arr_dir);
            String oneDir = file.getPath() + File.separator + arr_dir.get(0);
            return findDirImage(new File(oneDir));
        }
        return null;
    }
    public static String findDirImage(Path dirPath) {
        List<String> subDirs = new ArrayList<>();
        List<String> imageFiles = new ArrayList<>();

        // 1. 使用 try-with-resources 确保 DirectoryStream 及时关闭句柄
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
            for (Path entry : stream) {
                String name = entry.getFileName().toString();
                // 2. 利用 Files.isRegularFile，避免重复系统调用
                if (Files.isRegularFile(entry)) {
                    if (isFileImage(entry)) {
                        imageFiles.add(name);
                    }
                } else if (Files.isDirectory(entry)) {
                    subDirs.add(name);
                }
            }
        } catch (IOException e) {
            return null;
        }

        // 3. 处理文件逻辑：如果当前目录下有图片，按自定义排序取第一个
        if (!imageFiles.isEmpty()) {
            // 使用 Java 8+ lambda 简化 Comparator
            imageFiles.sort(FileUtil::compare);
            return dirPath.resolve(imageFiles.get(0)).toString();
        }

        // 4. 递归逻辑：如果没图片但有子目录，按排序取第一个子目录继续深度查找
        if (!subDirs.isEmpty()) {
            subDirs.sort(FileUtil::compare);
            Path firstDir = dirPath.resolve(subDirs.get(0));
            return findDirImage(firstDir); // 递归
        }

        return null;
    }

    public static int compare(String n1, String n2){
        /*char[] c1 = n1.toCharArray();
        char[] c2 = n2.toCharArray();

        *//*int min = Math.min(c1.length, c2.length);
        int i_c1=0,i_c2=0;
        for (int i = 0; i < min; i++) {
            char c_1 = c1[i];
            char c_2 = c2[i];

            if(c_1)
        }*//*
        List<Integer> c_1 = new ArrayList<>();

        return 0;*/
        //return compare_v0(n1, n2);
        //return compare_v1(n1, n2);
        return compare_v2(n1, n2);
    }
    /*private static int compare_v0(String n1, String n2){
        char[] c1 = n1.toCharArray();
        char[] c2 = n2.toCharArray();

        int max = Math.max(c1.length, c2.length);
        int i_c1=0,i_c2=0;
        int tmp_1=0,tmp_2=0;
        for (int i = 0; i < max; i++) {
            tmp_1=0;
            tmp_2=0;
            // 溢出检查
            if(c1.length <= i+i_c1){
                //c1已遍历完成
                if(c2.length <= i+i_c2){
                    //如果c2也遍历完成，那就代表之前没分出前后，现在也没有分出先后，即两个文本同样的权重
                    // exp: 第001话 = 第0001话 = 第1话
                    return 0;
                }else{
                    // 如果c2没有遍历完成，那就代表c2比c1长，需要排在c1后面
                    return -(int)c2[i+i_c2];
                }
            }
            if(c2.length <= i+i_c1){
                // c1 没有超出，c2超出了
                return (int)c1[i+i_c1];
            }
            char c_1 = c1[i+i_c1];
            char c_2 = c2[i+i_c2];

            while (c_1>=48 && c_1 <=57 && c1.length > i+i_c1){
                //tmp_1 += Character.getNumericValue(c_1);
                tmp_1 = addStr(tmp_1, c_1);
                i_c1++;
                c_1 = c1[i+i_c1];
            }
            while (c_2>=48 && c_2 <=57 && c2.length > i+i_c2){
                //tmp_2 += Character.getNumericValue(c_2);
                tmp_2 = addStr(tmp_2, c_2);
                i_c2++;
                c_2 = c2[i+i_c2];
            }
            if(tmp_1==0){tmp_1 = (int)c_1;}
            if(tmp_2==0){tmp_2 = (int)c_2;}
            if(tmp_1 == tmp_2){
                continue;
            }
            return tmp_1 - tmp_2;
        }

        return 0;
    }*/

    private static int compare_v1(String n1, String n2) {
        char[] c1 = n1.toCharArray();
        char[] c2 = n2.toCharArray();
        int[] r1 = charArrMerge(c1);
        int[] r2 = charArrMerge(c2);

        int min = Math.min(r1.length, r2.length);
        int i = 0;
        for (; i < min; i++) {
            if(r1[i] == r2[i]){
                continue;
            }
            return r1[i] - r2[i];
        }
        //前面都没有判断出来，长的排后面
        if(r1.length > r2.length){
            return r1[i];
        } else if (r1.length == r2.length) {
            // 一样长的话，那就是两个文本权重相等
            return 0;
        } else{
            return r2[i];
        }
    }

    private static int[] charArrMerge(char[] cArr){
        int[] r1 = new int[cArr.length];
        int[] r_tmp = new int[cArr.length];
        int i_in = 0;
        int i_tmp_val = 0;
        for (int i = 0; i < cArr.length; i++) {
            char c_t = cArr[i];
            if(c_t>=48 && c_t <=57){
                i_tmp_val = addStr(i_tmp_val, c_t);
                continue;
            }else{
                // 缓存出栈
                if(i_tmp_val>0){
                    r_tmp[i_in] = i_tmp_val;
                    i_tmp_val = 0;
                    i_in++;
                }

                i_tmp_val = c_t;
            }

            r_tmp[i_in] = i_tmp_val;
            i_tmp_val = 0;
            i_in++;
        }

        // 缓存出栈
        if(i_tmp_val>0){
            r_tmp[i_in] = i_tmp_val;
            i_tmp_val = 0;
            i_in++;
        }

        if(i_in+1 == r1.length){
            return r_tmp;
        }
        //return Arrays.stream(r_tmp).limit(i_in+1).toArray();
        return Arrays.copyOf(r_tmp, i_in+1);
    }

    private static int addStr(int num, char str){
        String tmp = num + "" + Character.getNumericValue(str);
        return Integer.valueOf(tmp);
    }

    // Ai 优化 --- start ---
    public static int compare_v2(String s1, String s2) {
        if (s1 == null || s2 == null) return s1 == s2 ? 0 : (s1 == null ? -1 : 1);

        int i = 0, j = 0;
        while (i < s1.length() && j < s2.length()) {
            char c1 = s1.charAt(i);
            char c2 = s2.charAt(j);

            // 如果两个都是数字，进入数字比较模式
            if (isDigit(c1) && isDigit(c2)) {
                int result = compareNumbers(s1, i, s2, j);
                if (result != 0) return result;

                // 跳过已比较的数字部分
                i = skipDigits(s1, i);
                j = skipDigits(s2, j);
            } else {
                if (c1 != c2) return c1 - c2;
                i++;
                j++;
            }
        }
        return s1.length() - s2.length();
    }
    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    private static int compareNumbers(String s1, int i, String s2, int j) {
        // 方案：为了防止 BigInteger 带来的性能损耗，先比较有效位长度，再比较数值
        int iStart = i;
        int jStart = j;

        // 跳过前导零（可选，根据业务逻辑决定是否 01 == 1）
        while (i < s1.length() && s1.charAt(i) == '0') i++;
        while (j < s2.length() && s2.charAt(j) == '0') j++;

        int iEnd = skipDigits(s1, i);
        int jEnd = skipDigits(s2, j);

        int len1 = iEnd - i;
        int len2 = jEnd - j;

        // 长度不同，长的数值大
        if (len1 != len2) return len1 - len2;

        // 长度相同，按字符逐位比较（等同于数值比较）
        for (; i < iEnd; i++, j++) {
            if (s1.charAt(i) != s2.charAt(j)) {
                return s1.charAt(i) - s2.charAt(j);
            }
        }

        // 如果数值相等但前导零数量不同
        return (iEnd - iStart) - (jEnd - jStart);
    }
    private static int skipDigits(String s, int index) {
        while (index < s.length() && isDigit(s.charAt(index))) index++;
        return index;
    }
    // Ai 优化 --- end ---

    public static String readFile(File file, Long reLen, String charsetName){
        StringBuffer re = new StringBuffer();
        BufferedReader br = null;
        Long dataLen = 0L;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charsetName));
            String buff = null;
            while ((buff = br.readLine()) != null) {
                re.append(buff);
                re.append("\r\n");
                dataLen++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        reLen = dataLen;
        return re.toString();
    }

    public record ImageDimension(int width, int height) {
        public Double ratio(){
            if(width<=0 || height<=0){
                return null;
            }
            return Double.valueOf(width) / Double.valueOf(height);
        }
    }
    // 定义 metadata-extractor 中通用的长宽 Tag ID
    private static final int Directory_TAG_WIDTH = 1;  // 多数目录中 1 代表宽度
    private static final int Directory_TAG_HEIGHT = 3; // 多数目录中 3 代表高度
    public static ImageDimension getImageDimension(Path imagePath){
        String mimeType;
        try {
            mimeType = Files.probeContentType(imagePath);
            if(mimeType==null || mimeType.isBlank() || !mimeType.startsWith("image")){
                log.warn("路径非图片: {}, {}", imagePath, mimeType);
                return null;
            }
        } catch (IOException e) {
            return null;
        }

        try (InputStream is = new BufferedInputStream(Files.newInputStream(imagePath))) {
            Metadata metadata = ImageMetadataReader.readMetadata(is);

            // 针对不同格式的通用处理逻辑
            int width = 0;
            int height = 0;

            // 这能自动兼容 JPEG, PNG, GIF, BMP, WebP, TIFF 等
            for (Directory directory : metadata.getDirectories()) {
                if (directory.containsTag(Directory_TAG_WIDTH) && directory.containsTag(Directory_TAG_HEIGHT)) {
                    width = directory.getInt(Directory_TAG_WIDTH);
                    height = directory.getInt(Directory_TAG_HEIGHT);

                    // 如果成功获取到非零数值，直接跳出
                    if (width > 0 && height > 0) {
                        return new ImageDimension(width, height);
                    }
                }
            }

            // 备选方案：从 Exif 信息中获取
            var exifDir = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (exifDir != null) {
                width = exifDir.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH);
                height = exifDir.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT);
                if (width > 0 && height > 0) {
                    return new ImageDimension(width, height);
                }
            }

            log.warn("获取图片尺寸失败: {}, {}", imagePath, mimeType);
        } catch (Exception e) {
            log.warn("获取图片尺寸出现异常: {}, {}, {}", imagePath, mimeType, e.getMessage());
        }
        return null;
    }
}
