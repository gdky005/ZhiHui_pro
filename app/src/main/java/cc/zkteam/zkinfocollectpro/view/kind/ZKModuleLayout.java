package cc.zkteam.zkinfocollectpro.view.kind;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ZKModuleLayout
 * 将 ZKKindTitle 和 ZKFormView 统一封装起来，更方便添加数据。
 * Created by WangQing on 2017/12/29.
 */

public class ZKModuleLayout extends ZKBaseView implements IZKResult {

    private List<ZKFiled> zkFiledList = new ArrayList<>();

    public ZKModuleLayout(Context context) {
        super(context);
    }

    public ZKModuleLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ZKModuleLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayoutId() {
        return 0;
    }

    @Override
    protected void initViews(View rootView) {

    }

    /**
     * 设置数据
     *
     * @param jsonArray 二级子数据，每一个 item。
     *                  ----jsonObject 的 key 对应 ZKFiled 中的第一个参数 number，
     *                  ----value 表示后面紧跟的文字。
     * @param map       二级子数据，每一个 item。map 表示 右边需要的数据：
     *                  ----1. 直接是数字，表示 右边的控件类型的 type.
     *                  ----2. ArrayList 表示右边的控件类型需要默认数据：
     *                  --------第0项表示 当前右边的类型 的 type;
     *                  --------第1项表示 当前右边的类型 需要的默认数据，是一组数据列表。
     * @param tileList  一级大标题后面 出现的   数据的单选list （PS: 一个 module 只能有一个这个）
     *                  --------第0项表示 当前标题 的 名字;
     *                  --------第1项表示 当前标题 的 type；
     *                  --------第2项表示 当前标题 需要的默认数据，是一组 String[] 数据列表。
     *
     */
    public void setJsonArray(JSONArray jsonArray, Map<Integer, Object> map, List tileList) {
        setOrientation(VERTICAL);

        try {
            ZKKindTitle zkKindTitle = new ZKKindTitle(context);
            String name = String.valueOf(tileList.get(0));
            int type = Integer.valueOf(String.valueOf(tileList.get(1)));
            String[] strings = new String[0];
            try {
                strings = (String[]) tileList.get(2);
            } catch (Exception e) {
                e.printStackTrace();
            }

            zkKindTitle.setData(name, type, strings);
            addView(zkKindTitle);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.optJSONObject(i);
                if (object != null) {
                    String key = object.names().optString(0);
                    Object value = object.optString(key);

                    ZKFiled zkFiled = new ZKFiled(getContext());
                    int type = ZKFiled.TYPE_FILED_FORM_EDIT_TEXT;

                    Object defaultValue = null;

                    if (map != null && map.size() > 0) {
                        Object obj = map.get(i);

                        if (obj instanceof ArrayList) {
                            ArrayList list = (ArrayList) obj;
                            type = (int) list.get(0);
                            try {
                                value = list.get(1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                defaultValue = list.get(2);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (obj instanceof Integer) {
                            type = (Integer) obj;
                        }
                    }

                    zkFiled.setData(key, (String) value, defaultValue, i, type);

                    zkFiledList.add(zkFiled);
                    addView(zkFiled);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getResult() {
        List<String> resultList = new ArrayList<>();

        for (ZKFiled zkFiled :
                zkFiledList) {
            resultList.add(zkFiled.getResult());
        }

        return resultList;
    }
}
