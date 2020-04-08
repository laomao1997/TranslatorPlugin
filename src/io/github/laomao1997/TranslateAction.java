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
import org.codehaus.jettison.json.JSONArray;
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
        // 查询
        if (selectedText.isEmpty()) {
            return;
        }
        String requestJson;
        try {
            requestJson = buildRequestJson(selectedText).toString();
        } catch (JSONException e1) {
            System.out.println(Arrays.toString(e1.getStackTrace()));
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("pattern", selectedText);
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
            requestJson = jsonObject.toString();
        }
        String url = "http://47.93.58.173:8080/Dictionary/dictionary/query";
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
                        // 解析查询返回的结果
                        String parsedResultString;
                        try {
                            parsedResultString = parseResult(s);
                        } catch (Exception exception) {
                            parsedResultString = "";
                        }
                        // 展示结果
                        showPopup(editor, parsedResultString);
                    }
                });

    }

    private JSONObject buildRequestJson(String pattern) throws JSONException {
        return new JSONObject().put("pattern", pattern);
    }

    private String parseResult(String response) throws Exception {
        System.out.println("Result: " + response);
        StringBuilder resultBuilder = new StringBuilder();
        try {
            JSONObject resultJson = new JSONObject(response);
            String statusString = getStatusCode(resultJson);
            if (statusString == null || !statusString.equals("0")) {
                String message = getStatusMsg(resultJson);
                if (message == null || message.isEmpty()) {
                    throw new Exception("Message is null.");
                }
                return message;
            }
            JSONObject dataJson = getData(resultJson);
            if (dataJson == null) {
                throw new JSONException("Data is null.");
            }
            String pattern = getPattern(dataJson);
            if (pattern == null || pattern.isEmpty()) {
                throw new JSONException("pattern is null.");
            }
            resultBuilder.append("<h1>").append(pattern).append("</h1>").append("\n");
            JSONArray dataModelsJsonArray = getDataModels(dataJson);
            if (dataModelsJsonArray == null) {
                throw new JSONException("dataModels is null.");
            }
            resultBuilder.append("<ul>");
            for (int i = 0; i < dataModelsJsonArray.length(); i++) {
                JSONObject dataModelJson = dataModelsJsonArray.optJSONObject(i);
                if (dataModelJson == null) {
                    throw new JSONException("dataModelJson is null.");
                }
                DataModel dataModel = DataModel.fromJson(dataModelJson);
                if (dataModel == null) {
                    throw new JSONException("dataModel is null.");
                }
                System.out.println(dataModel.toString());
                resultBuilder.append(dataModel.toBeautifiedString());
            }
            resultBuilder.append("</ul>");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return resultBuilder.toString();
    }

    private String getStatusCode(JSONObject jsonObject) {
        return jsonObject.optString("status");
    }

    private String getStatusMsg(JSONObject jsonObject) {
        return jsonObject.optString("message");
    }

    private JSONObject getData(JSONObject jsonObject) {
        return jsonObject.optJSONObject("data");
    }

    private String getPattern(JSONObject jsonObject) {
        return jsonObject.optString("pattern");
    }

    private JSONArray getDataModels(JSONObject jsonObject) {
        return jsonObject.optJSONArray("dataModels");
    }

    private void showPopup(Editor editor, String selectedText) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                JBPopupFactory factory = JBPopupFactory.getInstance();
                BalloonBuilder builder = factory.createHtmlTextBalloonBuilder(selectedText, null,
                        new JBColor(new Color(188, 238, 188), new Color(73, 120, 73)), null);

                builder.setFadeoutTime(10000) // 无操作10秒后隐藏
                        .createBalloon() // 创建气泡
                        .show(factory.guessBestPopupLocation(editor), Balloon.Position.below); // 指定位置显示气泡
            }
        });
    }
}
