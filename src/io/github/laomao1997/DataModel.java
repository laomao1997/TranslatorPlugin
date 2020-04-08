package io.github.laomao1997;

import org.codehaus.jettison.json.JSONObject;
import org.jetbrains.annotations.Nullable;

public class DataModel {
    private String id;
    private String gmtCreate;
    private String gmtModified;
    private boolean isActive;
    private String nameCn;
    private String nameEn;
    private String explanation;

    public DataModel(String id, String gmtCreate, String gmtModified, boolean isActive, String nameCn, String nameEn, String explanation) {
        this.id = id;
        this.gmtCreate = gmtCreate;
        this.gmtModified = gmtModified;
        this.isActive = isActive;
        this.nameCn = nameCn;
        this.nameEn = nameEn;
        this.explanation = explanation;
    }

    @Nullable
    public static DataModel fromJson(JSONObject jsonObject) {
        String id = jsonObject.optString("id");
        String gmtCreate = jsonObject.optString("gmt_create");
        String gmtModified = jsonObject.optString("gmt_modified");
        int activeNum = jsonObject.optInt("is_active");
        boolean isActive = activeNum == 0;
        String nameCn = jsonObject.optString("name_cn");
        String nameEn = jsonObject.optString("name_en");
        String explanation = jsonObject.optString("explanation");
        if (id == null || gmtCreate == null || gmtModified == null || nameCn == null || nameEn == null || explanation == null) {
            return null;
        }
        return new DataModel(id, gmtCreate, gmtModified, isActive, nameCn, nameEn, explanation);
    }

    public String toBeautifiedString() {
        return "<ul>" +
                "<li>" + "中: " + nameCn + "</li>" +
                "<li>" + "英: " + nameEn + "</li>" +
                "<li>" + explanation + "</li>" +
                "</ul>";
    }


    @Override
    public String toString() {
        return "DataModel{" +
                "id='" + id + '\'' +
                ", gmtCreate='" + gmtCreate + '\'' +
                ", gmtModified='" + gmtModified + '\'' +
                ", isActive=" + isActive +
                ", nameCn='" + nameCn + '\'' +
                ", nameEn='" + nameEn + '\'' +
                ", explanation='" + explanation + '\'' +
                '}';
    }
}
