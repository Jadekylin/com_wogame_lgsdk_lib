package com.madnow.lgsdk.service;

import android.app.Activity;
import android.app.Application;
import android.content.res.Resources;
import android.os.Handler;
import android.util.Log;

import com.bytedance.applog.AppLog;
import com.madnow.lgsdk.activity.FullScreenVideoADActivity;
import com.madnow.lgsdk.activity.RewardVideoADActivity;
import com.ss.union.game.sdk.LGSDK;
import com.ss.union.gamecommon.LGConfig;
import com.wogame.cinterface.AdInterface;
import com.wogame.cinterface.TeaInterface;
import com.wogame.common.AppMacros;

import org.json.JSONObject;

public class LgSdkService extends AdInterface {

    private static final String TAG = "LgSdkService";

    private static LgSdkService mService = null;
    private Activity mActivity;
    private int mVideoCompleteStatus;//0为完成;1发放成功；2发送失败
    private boolean mIsSkipped;
    private String nPlaceId;

    private String mFullScreenId;
    private String mRewardId;
    private String mRewardId2;
    private RewardVideoADActivity mRewardVideoAd;
    private FullScreenVideoADActivity mFullScreenVideoAd;
    private boolean[] isLoaded;

    public static LgSdkService getInstance() {
        if (mService == null) {
            mService = new LgSdkService();
            mService.setDelegate(mService);
        }
        return mService;
    }

    public void initWithApplication(Application app) {
    }

    public void initActivity(Activity activity,
                             final String aid,
                             final String channel,
                             final String appName,
                             final String rewardId,
                             final String rewardId2,
                             final String fullScreenId,
                             final String bannerId) {
        mActivity = activity;
        initConfig(aid,channel,appName);
        isLoaded = new boolean[7];

        mRewardId = rewardId;
        mRewardId2 = rewardId2;
        mFullScreenId = fullScreenId;

        mRewardVideoAd = new RewardVideoADActivity();
        mRewardVideoAd.initActivity(mActivity);
        mFullScreenVideoAd = new FullScreenVideoADActivity();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadAdData();
            }
        }, 5000);
    }

    private void loadAdData(){
        mRewardVideoAd.init(mRewardId,mRewardId2);
        mFullScreenVideoAd.init(mActivity,mFullScreenId);
    }

    private void initConfig(final String aid,final String channel,final String appName){
        LGConfig lgConfig = new LGConfig.Builder()
                .appID(aid)
                .loginMode(LGConfig.LoginMode.LOGIN_SILENT)//初始化前确认游戏登录模式 静默登录或者弹框登录
                .mChannel(channel)
                .showFailToast(false)//当静默登录方式登录失败时候 是否由SDK弹出toast提示
                .appName(appName)
//                .abTestVersion("123,111")//该配置为 optional 如果申请了AB测试 那么可以配置，否则可以忽略
//                .debug(true)//只为调试使用 release的时候 请删除该配置
                .build();

        LGSDK.init(mActivity.getApplicationContext(), lgConfig);
        // 申请部分权限,建议在sdk初始化前申请,如：READ_PHONE_STATE、ACCESS_COARSE_LOCATION及ACCESS_FINE_LOCATION权限，
        // 以获取更好的广告推荐效果，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        LGSDK.requestPermissionIfNecessary(mActivity);

        //防成迷
//        LGSDK.getRealNameManager().setAntiAddictionGlobalCallback(new LGAntiAddictionGlobalCallback() {
//            @Override
//            public void onTriggerAntiAddictionRequestPermissionIfNecessary(LGAntiAddictionGlobalResult authGlobalResult) {
//                Log.d(TAG, "onTriggerAntiAddiction()：exit:" + authGlobalResult.exitApp
//                        + ":errno：" + authGlobalResult.getErrNo() + ",errmsg:" + authGlobalResult.getErrMsg());
//                // 若exitApp为true, 说明应用启动的时候触发了防沉迷策略，需要接入方关闭APP
//                // 若为false：说明在游戏的过程中触发了防沉迷策略，若用户正在游戏中，可等待该局游戏结束后进行关闭应用
//                if (authGlobalResult.exitApp) {
////                    exit();
//                }
//            }
//
//        });
    }

    //region 广告回调
    /*********************************************************************************************************
     *
     * @param codeId
     * @param adsType
     */
    public void adsLoaded(String codeId, int adsType) {
        Log.d(TAG, "MainActivity adsType:" + adsType + " 加载成功");
        isLoaded[adsType] = true;
        if (mCallBack != null) {
            mCallBack.onAdStatusListen(adsType, AppMacros.CALL_AD_LOADED, nPlaceId,codeId,"","");
        }
    }

    public void adsShown(String codeId, int adsType) {
        Log.d(TAG, "MainActivity adsType:" + adsType + " 展示成功");
        if (mCallBack != null) {
            mCallBack.onAdStatusListen(adsType, AppMacros.CALL_AD_SHOW, nPlaceId,codeId,"","");
        }
    }

    public void adsClicked(String codeId, int adsType) {
        Log.d(TAG, "MainActivity adsType:" + adsType + " 点击成功");
        if (mCallBack != null) {
            mCallBack.onAdStatusListen(adsType, AppMacros.CALL_AD_CLICK, nPlaceId,codeId,"","");
        }
    }

    public void adsClosed(String codeId, int adsType, String extraJson) {
        Log.d(TAG, "MainActivity adsType:" + adsType + " 关闭成功");
        isLoaded[adsType] = false;
        if (adsType == AppMacros.AT_RewardVideo) {
            if (mVideoCompleteStatus == 1) {
                if (mCallBack != null) {
                    mCallBack.onCallBack(AppMacros.AT_RewardVideo, AppMacros.CALL_SUCCESS,nPlaceId,codeId,"","");
                    mCallBack.onAdStatusListen(AppMacros.AT_RewardVideo, AppMacros.CALL_SUCCESS,nPlaceId,codeId,"","");
                }
            } else {
                if (mCallBack != null) {
                    if(mIsSkipped){
                        mCallBack.onCallBack(AppMacros.AT_RewardVideo, AppMacros.CALL_FALIED, nPlaceId,codeId,"","");
                        mCallBack.onAdStatusListen(AppMacros.AT_RewardVideo, AppMacros.CALL_FALIED,nPlaceId,codeId,"","");
                    }
                    else {
                        mCallBack.onCallBack(AppMacros.AT_RewardVideo, AppMacros.CALL_CANCEL, nPlaceId,codeId,"","");
                        mCallBack.onAdStatusListen(AppMacros.AT_RewardVideo, AppMacros.CALL_FALIED,nPlaceId,codeId,"","");
                    }
                }
            }
            mIsSkipped = false;
            mVideoCompleteStatus = 0;
        }
        else {
            if (mCallBack != null) {
                mCallBack.onCallBack(adsType,AppMacros.CALL_CANCEL, nPlaceId,codeId,"","");
                mCallBack.onAdStatusListen(adsType, AppMacros.CALL_FALIED,nPlaceId,codeId,"","");
            }
        }
    }

    public void adsVideoComplete(String codeId, int adsType, String extraJson) {
        Log.d(TAG, "MainActivity adsType:" + adsType + " 奖励");
        if (adsType == AppMacros.AT_RewardVideo) {
            mVideoCompleteStatus = 1;
        }
    }

    public void adsSkippedVideo(String codeId, int adsType, String extraJson) {
        Log.d(TAG, "MainActivity adsType:" + adsType + " 奖励");
        if (adsType == AppMacros.AT_RewardVideo) {
            mIsSkipped = true;
        }
        if(mCallBack!= null) mCallBack.onAdStatusListen(adsType, AppMacros.CALL_SKIPPED,nPlaceId,codeId,"","");
    }

    public void adsError(String codeId, int adsType, int code, String message) {
        Log.d("TAG", "MainActivity adsType:" + adsType + " 加载失败 errCode:" + code + ", message:" + message);
        if(mCallBack!= null) mCallBack.onAdStatusListen(adsType, AppMacros.CALL_ERROR,nPlaceId,codeId,""+code,message);
    }
    //endregion

    //region 外部广告调用接口
    public void showAd(final int type, final String placeId, final int x, final int y) {
        Log.d(TAG, "showVideo:" + type + " placeId:" + placeId);
        nPlaceId = placeId;

        if ( type == AppMacros.AT_RewardVideo) {
            if(mRewardVideoAd.isLoaded()) {
                mRewardVideoAd.showAd();
            }
            else {
                if (mCallBack != null) {
                    mCallBack.onCallBack(type, AppMacros.CALL_FALIED,nPlaceId,"","","");
                }
            }
        }
        else if(type == AppMacros.AT_FullScreenVideo){
            if(isLoaded[type]) {
                mFullScreenVideoAd.showAd();
            }
            else {
                if (mCallBack != null) {
                    mCallBack.onCallBack(type, AppMacros.CALL_FALIED,nPlaceId,"","","");
                }
            }
        }

    }

    public void closeAd(final int type) {
        Log.d(TAG, "closeAd:" + type);
        if (type == AppMacros.AT_Banner_Bottom) {
//            HippoAdSdk.hideBanner();
        } else if (type == AppMacros.AT_Native) {
            //隐藏信息流广告
//            HippoAdSdk.hideNativeExpressAd();
        }
    }

    public boolean checkAD(final int type, final String placeId) {
        return isLoaded[type];
    }
    //endregion




}
