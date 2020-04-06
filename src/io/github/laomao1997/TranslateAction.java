package io.github.laomao1997;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import okhttp3.Call;
import okhttp3.MediaType;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.awt.*;
import java.util.Arrays;

public class TranslateAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        final Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) {
            return;
        }
        // 通过编辑器得到用户选择文本的对象
        SelectionModel model = editor.getSelectionModel();
        // 获取模型中的文本
        String selectedText = model.getSelectedText();

        if (StringUtils.isEmpty(selectedText)) {
            return;
        }
        System.out.println("选中文本：" + selectedText + " 开始查询...");
        // TODO: 2. 查询
        String requestJson;
        try {
            requestJson = buildRequestJson(selectedText).toString();
        } catch (JSONException e1) {
            System.out.println(Arrays.toString(e1.getStackTrace()));
            requestJson = "";
        }
        String url = "";
        // https://github.com/hongyangAndroid/okhttputils
        OkHttpUtils.postString()
                .url(url)
                .content(requestJson)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        showPopup(editor, "请求异常");
                    }

                    @Override
                    public void onResponse(String s, int i) {
                        // TODO: 3. 解析查询返回的结果
                        String parsedResultString;
                        try {
                            parsedResultString = parseResult(s);
                        } catch (Exception exception) {
                            parsedResultString = "";
                        }
                        // TODO: 4. 展示结果
                        showPopup(editor, selectedText + "\n" + parsedResultString);
                    }
                });

    }

    private JSONObject buildRequestJson(String pattern) throws JSONException {
        return new JSONObject().put("pattern", pattern);
    }

    /**
     * {
     *     "error_code": "0",
     *     "error_msg": "",
     *     "pattern": "GG",
     *     "content": [
     *         {
     *             "name_cn": "港股",
     *             "name_en": "GG",
     *             "explanation": "港股，是指在中华人民共和国香港特别行政区香港联合交易所上市的股票。"
     *         },
     *         {
     *             "name_cn": "个股",
     *             "name_en": "GG",
     *             "explanation": "指某一只股票。"
     *         },
     *         {
     *             "name_cn": "港股通",
     *             "name_en": "GGT",
     *             "explanation": "港股通，是指投资者委托上交所会员，通过上交所证券交易服务公司，向联交所进行申报，买卖规定范围内的联交所上市股票。"
     *         }
     *     ]
     * }
     * @param result
     * @return
     * @throws Exception
     */
    private String parseResult(String result) throws Exception {
        return result;
    }

    private void showPopup(Editor editor, String selectedText) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                JBPopupFactory factory = JBPopupFactory.getInstance();
                BalloonBuilder builder = factory.createHtmlTextBalloonBuilder(selectedText, null,
                        new JBColor(new Color(188, 238, 188), new Color(73, 120, 73)), null);

                builder.setFadeoutTime(5000) // 无操作5秒后隐藏
                        .createBalloon() // 创建气泡
                        .show(factory.guessBestPopupLocation(editor), Balloon.Position.below); // 指定位置显示气泡
            }
        });
    }
}
