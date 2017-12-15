package cc.zkteam.zkinfocollectpro.activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.blankj.utilcode.util.ToastUtils;

import java.net.URLEncoder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.zkteam.zkinfocollectpro.Constant;
import cc.zkteam.zkinfocollectpro.R;
import cc.zkteam.zkinfocollectpro.activity.home.HomeActivity;
import cc.zkteam.zkinfocollectpro.base.BaseActivity;
import cc.zkteam.zkinfocollectpro.bean.BDIdCardBean;
import cc.zkteam.zkinfocollectpro.bean.BDIdCardRequestBody;
import cc.zkteam.zkinfocollectpro.bean.BDTokenBean;
import cc.zkteam.zkinfocollectpro.managers.ZKConnectionManager;
import cc.zkteam.zkinfocollectpro.utils.PageCtrl;
import cc.zkteam.zkinfocollectpro.utils.baidu.Base64Util;
import cc.zkteam.zkinfocollectpro.utils.baidu.FileUtil;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};


    String accessToken = "";
    int expires_in = 0; //单位是秒


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.home_btn)
    Button homeBtn;
    @BindView(R.id.bd_access_token)
    Button bdAccessToken;
    @BindView(R.id.map)
    Button map;
    @BindView(R.id.id_card)
    Button idCard;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews() {
        setSupportActionBar(toolbar);
    }

    @Override
    protected void initListener() {
    }

    @Override
    protected void initData() {


        verifyStoragePermissions(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @OnClick({R.id.toolbar, R.id.home_btn, R.id.map, R.id.bd_access_token, R.id.id_card})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.toolbar:
                break;
            case R.id.home_btn:
                PageCtrl.startActivity(HomeActivity.class);
                break;
            case R.id.map:
                PageCtrl.startActivity(MapActivity.class);
                break;
            case R.id.bd_access_token:
                getBDAccessToken();
                break;
            case R.id.id_card:
                getIdCardInfo();
                break;
        }
    }

    private void getIdCardInfo() {
//        准备添加 身份证失败：http://ai.baidu.com/docs#/OCR-API/top
        String localPicPath = Environment.getExternalStorageDirectory().getPath() + "/xiaotieie_id_card.png";

        try {
            byte[] imgData = FileUtil.readFileByBytes(localPicPath);
            String imgStr = Base64Util.encode(imgData);
            String base64EncodeImage = URLEncoder.encode(imgStr, "UTF-8");

            BDIdCardRequestBody requestBody = BDIdCardRequestBody.BDIDBardRequestBodyFactory
                    .getDefaultRequestBody(true, base64EncodeImage);
            RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), requestBody.toString());

            ZKConnectionManager.getInstance().getZKApi().bdIDCard(body, accessToken).enqueue(new Callback<BDIdCardBean>() {
                @Override
                public void onResponse(Call<BDIdCardBean> call, Response<BDIdCardBean> response) {
                    Log.d(TAG, "onResponse() called with: call = [" + call + "], response = [" + response + "]");
                    BDIdCardBean bdIdCardBean = response.body();
                    if (bdIdCardBean != null) {
                        BDIdCardBean.WordsResultBean wordsResultBean = bdIdCardBean.getWords_result();

                        if (wordsResultBean != null) {
                            // TODO: 2017/12/15  在这里处理数据格式，并返回给主界面
                        }
                    }
                }

                @Override
                public void onFailure(Call<BDIdCardBean> call, Throwable t) {

                    Log.d(TAG, "onFailure() called with: call = [" + call + "], t = [" + t + "]");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getBDAccessToken() {
//        参考地址：http://ai.baidu.com/docs#/Auth/top

        //{
        //  "access_token":"24.4aa184e7c8f9026c967660a101a648b5.2592000.1515926944.282335-10539172",
        //  "session_key":"9mzdD0Wia2Q1VKBPqL6oFEeWRAp2yQF8QFQSooQR45SvSUmmePah6WMuNAX9JggJj\/mvo29LDwAHhK31G3u\/eh7HZbKC4w==",
        //  "scope":"public vis-ocr_ocr brain_ocr_scope brain_ocr_general brain_ocr_general_basic brain_ocr_general_enhanced vis-ocr_business_license brain_ocr_webimage brain_all_scope brain_ocr_idcard brain_ocr_driving_license brain_ocr_vehicle_license vis-ocr_plate_number brain_solution brain_ocr_plate_number brain_ocr_accurate brain_ocr_accurate_basic brain_ocr_receipt brain_ocr_business_license wise_adapt lebo_resource_base lightservice_public hetu_basic lightcms_map_poi kaidian_kaidian ApsMisTest_Test\u6743\u9650 vis-classify_flower bnstest_fasf lpq_\u5f00\u653e",
        //  "refresh_token":"25.0a9d6223afecb3f3e54a618823301f36.315360000.1828694944.282335-10539172",
        //  "session_secret":"8724c6186ba896bbe17cb4ad07579c75",
        //  "expires_in":2592000
        // }

        ZKConnectionManager.getInstance().getZKApi().bdAccessToken(Constant.BD_GRANT_TYPE,
                Constant.BD_API_KEY, Constant.BD_SECRET_KEY).enqueue(new Callback<BDTokenBean>() {
            @Override
            public void onResponse(Call<BDTokenBean> call, Response<BDTokenBean> response) {
                Log.d(TAG, "onResponse() called with: call = [" + call + "], response = [" + response + "]");

                BDTokenBean bdTokenBean = response.body();
                if (bdTokenBean != null) {
                    accessToken = bdTokenBean.getAccess_token();
                    expires_in = bdTokenBean.getExpires_in();

                    ToastUtils.showShort("获取 accessToken 成功，请继续下一步操作！");
                }

            }

            @Override
            public void onFailure(Call<BDTokenBean> call, Throwable t) {
                Log.d(TAG, "onFailure() called with: call = [" + call + "], t = [" + t + "]");

                ToastUtils.showShort("获取 Token 失败");

            }
        });
    }


    /**
     * Checks if the app has permission to write to device storage
     * If the app does not has permission then the user will be prompted to
     * grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }
}
