package com.chdc.chdctextwidget;

import android.text.Html;
import android.util.SparseArray;

/**
 * Created by Wen on 2017/2/22.
 */

public class Markdown {

    /**
     * 把 Markown 文本转换为 CharSequence
     * @param text
     * @return
     */
    public static CharSequence toCharSequence(String text, boolean withSyntax){
        String html = toHtml(text, withSyntax);
        return Html.fromHtml(html);
    }

    /**
     * Markdown 转 HTML
     * @param markdown
     * @return
     */
    public static String toHtml(String markdown, boolean withSyntax){
        String html = markdown;

        String[][] regexs = {

                // Bold
                {"\\*\\*(.+?)\\*\\*", "<b>$1</b>"},
                // Strike through
                {"~~(.+?)~~", !withSyntax ? "<s>$1</s>" : "<s>~~$1~~</s>"},
                // italic
                {"\\*(.+?)\\*", "<i>$1</i>"},
                // underline
                {"__(.+?)__", !withSyntax ? "<u>$1</u>" : "<u>__$1__</u>"},
                // title
                {"(?m)^# (.*)", !withSyntax ? "<h1>$1</h1>" : "<h1># $1</h1>" },
                {"(?m)^## (.*)", !withSyntax ? "<h2>$1</h2>" : "<h2>## $1</h2>" },
                {"(?m)^### (.*)", !withSyntax ? "<h3>$1</h3>" : "<h3>### $1</h3>" },
                {"(?m)^#### (.*)", !withSyntax ? "<h4>$1</h4>" : "<h4>#### $1</h4>" },
                {"(?m)^##### (.*)", !withSyntax ? "<h5>$1</h5>" : "<h5>##### $1</h5>" },
                {"(?m)^###### (.*)", !withSyntax ? "<h6>$1</h6>" : "<h6>###### $1</h6>"},

                // list
                {"(?m)^\\* ", !withSyntax ? "● " : "* "},
                {"(?m)^  \\* ", !withSyntax ? "  ○ " : "  * "},

                // paragraph
                // {"(?m)^(?!<h\\d>)(.+)$\n*","<p>$1</p>"},
                {"(?<=</h\\d>)\\n+",""},
                {"(?<!</h\\d>)\n","<br/>"},
        };

        for(String[] regex: regexs){
            html = html.replaceAll(regex[0], regex[1]);
        }

        if(withSyntax){
            String[][] regexsWithSyntax = {

                    // Bold
                    {"<b>", "<b>**"},
                    {"</b>", "**</b>"},

                    // italic
                    {"<i>", "<i>*"},
                    {"</i>", "*</i>"},
            };
            for(String[] regex: regexsWithSyntax){
                html = html.replaceAll(regex[0], regex[1]);
            }
        }

        return html;
    }

    /**
     * 获取文档的目录结构
     * @param markdown
     * @return
     */
    public static SparseArray<String> getDocumentStructure(String markdown){
        SparseArray<String> result = new SparseArray<>();
        String[] lines = markdown.split("\n");
        for(int i = 0; i < lines.length; i++){
            String line = lines[i];
            if(line.matches("^#+ .*$")){
                result.put(i, line);
            }
        }
        return result;
    }
}
