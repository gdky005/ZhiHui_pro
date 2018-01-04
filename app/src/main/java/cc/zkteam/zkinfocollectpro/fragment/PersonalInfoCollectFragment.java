package cc.zkteam.zkinfocollectpro.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cc.zkteam.zkinfocollectpro.R;
import cc.zkteam.zkinfocollectpro.activity.BasicInfoActivity;
import cc.zkteam.zkinfocollectpro.activity.CommonFragmentActivity;
import cc.zkteam.zkinfocollectpro.activity.IDCardScanActivity;
import cc.zkteam.zkinfocollectpro.activity.PersonalInfoCollectActivity;
import cc.zkteam.zkinfocollectpro.adapter.PersonalInfoListAdapter;
import cc.zkteam.zkinfocollectpro.base.BaseFragment;
import cc.zkteam.zkinfocollectpro.base.RvListener;
import cc.zkteam.zkinfocollectpro.bean.BDIdCardBean;
import cc.zkteam.zkinfocollectpro.bean.CollectItemBean;
import cc.zkteam.zkinfocollectpro.bean.PersonalSimpleInfoBean;
import cc.zkteam.zkinfocollectpro.bean.ZHBaseBean;
import cc.zkteam.zkinfocollectpro.utils.L;
import cc.zkteam.zkinfocollectpro.utils.PageCtrl;
import cc.zkteam.zkinfocollectpro.view.ZKImageView;
import cc.zkteam.zkinfocollectpro.view.ZKRecyclerView;
import cc.zkteam.zkinfocollectpro.view.ZKTitleView;
import cn.qqtheme.framework.picker.OptionPicker;
import cn.qqtheme.framework.widget.WheelView;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

/**
 * 个人数据采集页面
 * Created by loong on 2017/12/16.
 */

public class PersonalInfoCollectFragment extends BaseFragment {

    @BindView(R.id.title_personal_info_collect)
    ZKTitleView titlePersonalInfoCollect;
    @BindView(R.id.img_personal_avatar)
    ZKImageView imgPersonalAvatar;
    @BindView(R.id.tv_personal_info_collect_name)
    TextView tvPersonalInfoCollectName;
    @BindView(R.id.tv_personal_info_collect_id)
    TextView tvPersonalInfoCollectId;
    @BindView(R.id.tv_personal_info_collect_project)
    TextView tvPersonalInfoCollectProject;
    @BindView(R.id.tv_personal_info_collect_completed)
    TextView tvPersonalInfoCollectCompleted;
    @BindView(R.id.tv_personal_info_collect_completion)
    TextView tvPersonalInfoCollectCompletion;
    @BindView(R.id.img_change_left)
    ImageView imgChangeLeft;
    @BindView(R.id.btn_modification_info)
    TextView btnModificationInfo;
    @BindView(R.id.img_change_right)
    ImageView imgChangeRight;
    @BindView(R.id.layout_change_collection_state)
    RelativeLayout layoutChangeCollectionState;
    @BindView(R.id.list_personal_info)
    ZKRecyclerView listPersonalInfo;

    private boolean callEdit;
    private String mPersonid = "19";

    @Override
    public int getLayoutId() {
        return R.layout.fragment_personal_info_collect;
    }

    @Override
    public void initView(View rootView) {
        titlePersonalInfoCollect.setCenterTVText("数据采集");
        titlePersonalInfoCollect.leftIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    getActivity().finish();
            }
        });
        titlePersonalInfoCollect.rightIV.setVisibility(View.INVISIBLE);
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        requestCollectionStatus();
        zhApiInstance.getPersonalSimpleInfo(mPersonid).enqueue(new Callback<PersonalSimpleInfoBean>() {
            @Override
            public void onResponse(Call<PersonalSimpleInfoBean> call, Response<PersonalSimpleInfoBean> response) {
                if (null == PersonalInfoCollectFragment.this || null == response.body() || 1 != (response.body().getStatus()) || null == response.body().getData()) {
                    Toast.makeText(mContext, "信息请求失败，请稍后重试", Toast.LENGTH_SHORT).show();
                    return;
                }

                PersonalSimpleInfoBean.DataBean infoBean = response.body().getData();
                if (TextUtils.isEmpty(infoBean.getImg())) {
                    imgPersonalAvatar.setImageResource(R.mipmap.ic_launcher);
                } else {
                    imgPersonalAvatar.setImageURI(infoBean.getImg());
                }
                setText(tvPersonalInfoCollectName, "姓名：" + infoBean.getName());
                setText(tvPersonalInfoCollectId, "身份证号：" + infoBean.getId_card());
                setText(tvPersonalInfoCollectProject, "性别：" + infoBean.getSex() + "    民族：" + infoBean.getNation());
                setText(tvPersonalInfoCollectCompleted, "采集项：" + infoBean.getCaijixiang_lei() + "  " + infoBean.getCaijixiang_lei());
            }

            @Override
            public void onFailure(Call<PersonalSimpleInfoBean> call, Throwable t) {
                if (null == PersonalInfoCollectFragment.this) return;
                Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        zhApiInstance.getPersonalInfoList().enqueue(new Callback<CollectItemBean>() {
            @Override
            public void onResponse(Call<CollectItemBean> call, Response<CollectItemBean> response) {
                if (null == PersonalInfoCollectFragment.this || null == response.body()) return;

                if (null == response.body() || response.body().getStatus() != 1) {
                    Toast.makeText(mContext, "信息请求失败，请稍后重试", Toast.LENGTH_SHORT).show();
                    return;
                }

                PersonalInfoListAdapter adapter = new PersonalInfoListAdapter(getActivity(), response.body().getData(), new RvListener() {
                    @Override
                    public void onItemClick(int id, int position) {
                        if (!callEdit) {
                            Toast.makeText(mContext, "当前状态不允许修改信息", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (null == response.body().getData() || response.body().getData().size() < position)
                            return;
                        ((PersonalInfoCollectActivity) getActivity()).showFragment(response.body().getData().get(position).getName(), response.body().getData().get(position).getType());
                    }
                });

                listPersonalInfo.setLayoutManager(new LinearLayoutManager(getActivity()));
                listPersonalInfo.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<CollectItemBean> call, Throwable t) {
                if (null == PersonalInfoCollectFragment.this) return;
                Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestCollectionStatus() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("personid", mPersonid);
            jsonObject.put("act", "getstatus");
            jsonObject.put("memo", "memo");
        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

        zhApiInstance.changeCollectionStatus(body).enqueue(new Callback<ZHBaseBean>() {
            @Override
            public void onResponse(Call<ZHBaseBean> call, Response<ZHBaseBean> response) {
                if (null == PersonalInfoCollectFragment.this || null == response.body()) return;
                if (1 == (response.body().getStatus())) {
                    setText(tvPersonalInfoCollectCompletion, "采集状态：" + response.body().getMsg());
                    if (response.body().getType() == 3 || response.body().getType() == 4 || response.body().getType() == 5) {
                        callEdit = false;
                    } else {
                        callEdit = true;
                    }
                } else {
                    Toast.makeText(mContext, response.body().getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ZHBaseBean> call, Throwable t) {
                if (null == PersonalInfoCollectFragment.this) return;
                Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void initListener() {

    }

    @OnClick({R.id.layout_change_collection_state})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.layout_change_collection_state:
                OptionPicker picker3 = new OptionPicker(getActivity(), new String[]{"采集完成", "重新激活"});
                picker3.setCanceledOnTouchOutside(false);
                picker3.setDividerRatio(WheelView.DividerConfig.FILL);
                picker3.setShadowColor(Color.BLUE, 40);
                picker3.setSelectedIndex(0);
                picker3.setCycleDisable(true);
                picker3.setTextSize(20);
                picker3.setOnOptionPickListener(new OptionPicker.OnOptionPickListener() {
                    @Override
                    public void onOptionPicked(int index1, String item) {
                        String act = index1 == 0 ? "finish" : "edit";

                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("personid", mPersonid);
                            jsonObject.put("act", act);
                            jsonObject.put("memo", "memo");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                        zhApiInstance.changeCollectionStatus(body).enqueue(new Callback<ZHBaseBean>() {
                            @Override
                            public void onResponse(Call<ZHBaseBean> call, Response<ZHBaseBean> response) {
                                if (null == PersonalInfoCollectFragment.this || null == response.body()) {
                                    Toast.makeText(mContext, "请求失败，请稍后重试", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (1 == (response.body().getStatus())) {
                                    Toast.makeText(mContext, response.body().getMsg(), Toast.LENGTH_SHORT).show();
                                    requestCollectionStatus();
                                } else {
                                    Toast.makeText(mContext, response.body().getMsg(), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<ZHBaseBean> call, Throwable t) {
                                if (null == PersonalInfoCollectFragment.this) return;
                                Toast.makeText(mContext, t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                picker3.show();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (resultCode) {
            case RESULT_OK:
                if (data != null && requestCode == 100) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        BDIdCardBean.WordsResultBean wordsResultBean = (BDIdCardBean.WordsResultBean) bundle.getSerializable(IDCardScanActivity.KEY_ID_CARD_INFO_BEAN);

                        if (wordsResultBean != null) {
                            String name = wordsResultBean.getName().getWords();
                            L.i("扫描的姓名是；" + name);
                            ToastUtils.showShort("扫描的姓名是：" + name);
                        }
                    }
                }
                break;
        }
    }
}
