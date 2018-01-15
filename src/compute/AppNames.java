package compute;

import com.intellij.ide.util.PropertiesComponent;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 存放app的
 */
public class AppNames {

    private static final String APPNAMES_KEY = "appnames_key";// 缓存键值

    public static final String APPNAME = "appName";// 缓存的列名

    public static Map<String, Map<String, String>> mapData = new HashMap<>(); // 存放数据

    /**
     * 初始化数据
     */
    public static void initData() {
        try {
            mapData.clear(); // 清空数据

            // 获取数据 2017/4/1 19:56
            String[] data = getData();
            StringBuilder json = new StringBuilder("[");
            for (String aData : data) {
                json.append(aData).append(",");
            }

            json.append("]");

            // 解析json数据 2017/3/20 10:57
            JSONArray jsonArray = JSONArray.fromObject(json.toString());
            JSONObject jsonObject;
            Map<String, String> mapData;
            for (int i = 0; i < jsonArray.size(); i++) {
                jsonObject = jsonArray.optJSONObject(i);
                if (jsonObject != null) {
                    mapData = new HashMap<>();
                    mapData.put(APPNAME, jsonObject.optString(APPNAME).toUpperCase().trim());

                    AppNames.mapData.put(jsonObject.optString(APPNAME).toUpperCase().trim(), mapData);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Suffix mapping Exception is " + e.getMessage());
        }
    }

    /**
     * 获取数据源
     *
     * @return 数据源
     */
    private static String[] getData() {
        if (PropertiesComponent.getInstance().isValueSet(APPNAMES_KEY)) {
            return PropertiesComponent.getInstance().getValues(APPNAMES_KEY);
        } else {
            return new String[]{"{\"appName\": \"App\"},{\"appName\": \"app_company\"},{\"appName\": \"app_shipper\"}, {\"appName\": \"app_boss\"}"};
        }
    }

    /**
     * 保存所有数据
     *
     * @param array 数据源
     */
    private static void saveData(String[] array) {
        PropertiesComponent.getInstance().setValues(APPNAMES_KEY, array);
    }

    /**
     * 添加一行数据
     *
     * @param data 数据
     */
    public static void addData(Map<String, String> data) {
        try {

            // 判断是否存在
            if (data != null && mapData.containsKey(data.get(APPNAME)))
                return;

            // 初始化
            if (mapData.size() == 0)
                initData();

            if (data != null)
                mapData.put(data.get(APPNAME), data);

            JSONObject jsonObject;
            Map<String, String> mapTemp;
            String[] datas = new String[mapData.size()];
            int idx = 0;
            for (String suffix : mapData.keySet()) {
                mapTemp = mapData.get(suffix);
                jsonObject = new JSONObject();
                jsonObject.put(APPNAME, suffix);

                datas[idx++] = jsonObject.toString();
            }

            saveData(datas);
        } catch (Exception e) {
            System.out.println("Types Exception is " + e.getMessage());
        }
    }

    /**
     * 根据key删除
     *
     * @param appName key值
     */
    public static void removeData(String appName) {
        // 初始化
        if (mapData.size() == 0)
            initData();

        if (mapData.containsKey(appName))
            mapData.remove(appName);

        addData(null);
    }

    /**
     * 删除列表
     *
     * @param appNames 列表
     */
    public static void removeData(List<String> appNames) {
        if (appNames == null || appNames.size() == 0)
            return;

        // 初始化
        if (mapData.size() == 0)
            initData();

        for (String appName : appNames)
            if (mapData.containsKey(appName))
                mapData.remove(appName);

        addData(null);
    }

}
