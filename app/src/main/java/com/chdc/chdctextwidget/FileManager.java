package com.chdc.chdctextwidget;

import java.io.File;

/**
 * Created by Wen on 2017/2/22.
 */

public class FileManager {

    /**
     * 临时文件的扩展名
     */
    public static final String TEMP_FILE_EXTENSION = ".ctwtmp";

    /**
     * 获取指定文件的临时文件
     * @param file
     * @return
     */
    public static String getTempFile(String file){
        File f = new File(file);
        return String.format("%s/.%s%s", f.getParent(), f.getName(), TEMP_FILE_EXTENSION);
    }

    /**
     * 获取小部件的标题
     * @param file
     * @return
     */
    public static String getTitle(String file){
        return new File(file).getName();
    }

    /**
     * 获取小部件的内容
     * @param file
     * @return
     */
    public static String getContent(String file){
        String tmpFile = getTempFile(file);
        String fileContent = "";
        if(new File(tmpFile).exists()){
            fileContent = FileIO.readAllText(tmpFile);
        }
        else if(new File(file).exists()){
            fileContent = FileIO.readAllText(file);
        }
        return fileContent;
    }

    /**
     * 获取处理过的文本
     * @param file
     * @param withMarkdownSyntax
     * @return
     */
    public static CharSequence getProcessedContent(String file, boolean withMarkdownSyntax){
        String fileContent = getContent(file);
        return processFileContent(getFileType(file), fileContent, withMarkdownSyntax);
    }

    /**
     * 获取处理过的文本
     * @param file
     * @param withMarkdownSyntax
     * @return
     */
    public static CharSequence getProcessedContent(String file){
        return getProcessedContent(file, false);
    }

    /**
     * 获取文件类型
     * @param file
     * @return
     */
    public static String getFileType(String file){
        String[] rs = new File(file).getName().split("\\.");
        String fileType = "";
        if(rs.length > 1){
            fileType = rs[rs.length - 1];
        }
        return fileType;
    }

    /**
     * 预处理文件内容
     * @param fileContent
     * @return
     */
    public static CharSequence processFileContent(String fileType, String fileContent, boolean withMarkdownSyntax){
        fileType = fileType.toLowerCase();
        fileContent = fileContent.replaceAll("^(\\r?\\n\\s*)+", "");
        if("md".equals(fileType)){
            // Markdown
            return Markdown.toCharSequence(fileContent, withMarkdownSyntax);
        }
        else{
            // 普通文本
            fileContent = fileContent.replaceAll("(\\r?\\n\\s*){2,}", "\n\n");
            return fileContent;
        }
    }
}
