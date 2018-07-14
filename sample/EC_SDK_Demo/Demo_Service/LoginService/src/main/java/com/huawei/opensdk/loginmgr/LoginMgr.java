package com.huawei.opensdk.loginmgr;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.huawei.ecterminalsdk.base.TsdkAuthType;
import com.huawei.ecterminalsdk.base.TsdkContactsInfo;
import com.huawei.ecterminalsdk.base.TsdkImLoginParam;
import com.huawei.ecterminalsdk.base.TsdkLocalAddress;
import com.huawei.ecterminalsdk.base.TsdkLoginParam;
import com.huawei.ecterminalsdk.base.TsdkLoginSuccessInfo;
import com.huawei.ecterminalsdk.base.TsdkMediaSrtpMode;
import com.huawei.ecterminalsdk.base.TsdkNetworkInfoParam;
import com.huawei.ecterminalsdk.base.TsdkSecurityTunnelInfo;
import com.huawei.ecterminalsdk.base.TsdkSecurityTunnelMode;
import com.huawei.ecterminalsdk.base.TsdkServiceSecurityParam;
import com.huawei.ecterminalsdk.base.TsdkSipTransportMode;
import com.huawei.ecterminalsdk.base.TsdkVoipAccountInfo;
import com.huawei.ecterminalsdk.models.TsdkCommonResult;
import com.huawei.ecterminalsdk.models.TsdkManager;
import com.huawei.opensdk.commonservice.util.DeviceManager;
import com.huawei.opensdk.commonservice.util.LogUtil;
import com.huawei.opensdk.imservice.ImMgr;
import com.huawei.opensdk.imservice.ImAccountInfo;

import static com.huawei.ecterminalsdk.base.TsdkServerType.TSDK_E_SERVER_TYPE_PORTAL;

/**
 * This class is about login manager
 * 登录管理类
 */
public class LoginMgr {
    private static final String TAG = LoginMgr.class.getSimpleName();

    /**
     * Login manager instance
     * 登录管理实例
     */
    private static LoginMgr instance;

    /**
     * UI callback
     * UI回调
     */
    private ILoginEventNotifyUI loginEventNotifyUI;

    /**
     * Force exit
     * 强制退出
     */
    private boolean isForceLogout = false;

    /**
     * local Ip Address
     * 本地IP
     */
    private String localIpAddress = "";

    private String account = "";

    private String terminal;

    private String sipNumber;

    /**
     * Define a TupEaddrContactorInfo object
     * 通讯录联系人详细信息对象
     */
    private TsdkContactsInfo selfInfo;

    /**
     * [en]This method is used to get login manager instance
     * [cn]获取一个登录管理类的实例对象
     *
     * @return LoginMgr : login manager instance
     * 返回一个登录管理类实例对象
     */
    public static LoginMgr getInstance() {
        if (null == instance) {
            instance = new LoginMgr();
        }
        return instance;
    }

    /**
     * This method is used to registered login callback
     * 注册UI回调函数
     *
     * @param notify : UI callback
     */
    public void regLoginEventNotification(ILoginEventNotifyUI notify) {
        this.loginEventNotifyUI = notify;
    }

    /**
     * [en]This method is used to login
     * [cn]鉴权登录
     *
     * @param loginParam        [en]login param
     *                          [cn]登录入参
     * @return int : 0:success
     */
    public int login(LoginParam loginParam) {
        int ret;
        //Get local IP
        //获取本地IP
        localIpAddress = DeviceManager.getLocalIpAddress(loginParam.isVPN());

        if (TsdkManager.getInstance().getAppInfo().getSupportAudioAndVideoCall() == 1)
        {
            //Set security param
            TsdkServiceSecurityParam serviceSecurityParam = new TsdkServiceSecurityParam();

            TsdkMediaSrtpMode srtpMode = TsdkMediaSrtpMode.enumOf(loginParam.getSrtpMode());
            if(null != srtpMode) {
                serviceSecurityParam.setMediaSrtpMode(srtpMode);
            }

            TsdkSipTransportMode sipTransportMode = TsdkSipTransportMode.enumOf(loginParam.getSipTransportMode());
            if(null != sipTransportMode) {
                serviceSecurityParam.setSipTransportMode(sipTransportMode);
            }

            serviceSecurityParam.setIsApplyConfigPriority(loginParam.getEnableConfigApplication());

            TsdkSecurityTunnelMode tunnelMode = TsdkSecurityTunnelMode.enumOf(loginParam.getSecurityTunnelMode());
            if (null != tunnelMode)
            {
                serviceSecurityParam.setSecurityTunnelMode(tunnelMode);
            }
            
            TsdkManager.getInstance().setConfigParam(serviceSecurityParam);

            //Set network param
            TsdkNetworkInfoParam networkInfoParam = new TsdkNetworkInfoParam();
            if (1 == loginParam.getPortPriority())
            {
                networkInfoParam.setSipServerUdpPort(loginParam.getUdpPort());
                networkInfoParam.setSipServerTlsPort(loginParam.getTlsPort());
            }
            else
            {
                networkInfoParam.setSipServerUdpPort(0);
                networkInfoParam.setSipServerTlsPort(0);
            }

            TsdkManager.getInstance().setConfigParam(networkInfoParam);
        }

        TsdkLocalAddress localAddress = new TsdkLocalAddress(localIpAddress);
        ret = TsdkManager.getInstance().setConfigParam(localAddress);
        if (ret != 0) {
            LogUtil.e(TAG, "config local ip is failed, return " + ret);
            return ret;
        }

        //TSDK 鉴权登录入参
        TsdkLoginParam tsdkLoginParam = new TsdkLoginParam();
        tsdkLoginParam.setUserId(1);
        tsdkLoginParam.setAuthType(TsdkAuthType.TSDK_E_AUTH_NORMAL);
        tsdkLoginParam.setUserName(loginParam.getUserName());
        account = loginParam.getUserName();
        tsdkLoginParam.setPassword(loginParam.getPassword());
        tsdkLoginParam.setServerAddr(loginParam.getServerUrl());
        tsdkLoginParam.setServerPort(loginParam.getServerPort());
        tsdkLoginParam.setServerVersion("");
        tsdkLoginParam.setServerType(TSDK_E_SERVER_TYPE_PORTAL);
        tsdkLoginParam.setUserTiket("");

        ret = TsdkManager.getInstance().getLoginManager().login(tsdkLoginParam);
        if (ret != 0) {
            LogUtil.e(TAG, "login is failed, return " + ret);
            return ret;
        }
        LogUtil.i(TAG, "start login.");
        return ret;
    }


    /**
     * This method is used to logout
     * 登出
     */
    public void logout() {
        int ret = TsdkManager.getInstance().getLoginManager().logout();
        if (ret != 0) {
            LogUtil.e(TAG, "login is failed, return " + ret);
        }
        ImMgr.getInstance().imLogout();
    }


    /**
     * [en]This method is used to handle the successful authentication.
     * [cn]处理鉴权成功事件
     *
     * @param userId            [en]Indicates user id
     *                          [cn]用户标识
     * @param imLoginParam      [en]Indicates im login param
     *                          [cn]IM登录参数
     */
    public void handleAuthSuccess(int userId, TsdkImLoginParam imLoginParam) {
        LogUtil.e(TAG, "authorize success.");
        // 启动IM的登录
        if (TsdkManager.getInstance().getAppInfo().getSupportIm() == 1 && null != imLoginParam) {
            ImAccountInfo imAccountInfo = new ImAccountInfo();
            imAccountInfo.setAccount(imLoginParam.getAccount());
            imAccountInfo.setToken(imLoginParam.getToken());
            String[] MaaUri = imLoginParam.getMaaServerUri().split(":");
            imAccountInfo.setMaaServer(MaaUri[0]);
            imAccountInfo.setMaaServerPort(Integer.parseInt(MaaUri[1]));
            imAccountInfo.setPassword(imLoginParam.getPassword());
            imAccountInfo.setPushServer(imLoginParam.getPushServerUri());

            sendHandlerMessage(LoginEvent.LOGIN_E_EVT_AUTH_SUCCESS.getIndex(), imAccountInfo);
        }
    }

    /**
     * [en]This method is used to handle the failed authentication.
     * [cn]处理鉴权失败事件
     *
     * @param userId            [en]Indicates user id
     *                          [cn]用户标识
     * @param result            [en]Indicates response results
     *                          [cn]响应结果
     */
    public void handleAuthFailed(int userId, TsdkCommonResult result) {
        LogUtil.e(TAG, "authorize failed: " + result.getReasonDescription());
        this.loginEventNotifyUI.onLoginEventNotify(LoginConstant.LoginUIEvent.LOGIN_FAILED, result.getResult(), result.getReasonDescription());
    }

    /**
     * [en]This method is used to handle the refresh failed authentication.
     * [cn]处理鉴权刷新失败事件
     *
     * @param userId            [en]Indicates user id
     *                          [cn]用户标识
     * @param result            [en]Indicates response results
     *                          [cn]响应结果
     */
    public void handleAuthRefreshFailed(int userId, TsdkCommonResult result) {
        LogUtil.e(TAG, "refresh token failed:" + result.getReasonDescription());

        this.logout();
        this.loginEventNotifyUI.onLoginEventNotify(LoginConstant.LoginUIEvent.LOGOUT, 0, result.getReasonDescription());

    }

    /**
     * [en]This method is used to handle the success login
     * [cn]处理登录成功事件
     *
     * @param userId            [en]Indicates user id
     *                          [cn]用户标识
     */
    public void handleLoginSuccess(int userId, TsdkLoginSuccessInfo successInfo) {
        LogUtil.i(TAG, "voip login success");
        this.loginEventNotifyUI.onLoginEventNotify(LoginConstant.LoginUIEvent.LOGIN_SUCCESS, userId, "voip login success");
    }

    /**
     * [en]This method is used to handle the failed login
     * [cn]处理登录失败事件
     *
     * @param userId            [en]Indicates user id
     *                          [cn]用户标识
     * @param result            [en]Indicates response results
     *                          [cn]响应结果
     */
    public void handleLoginFailed(int userId, TsdkCommonResult result) {
        LogUtil.e(TAG, "voip login failed: " + result.getReasonDescription());
        this.loginEventNotifyUI.onLoginEventNotify(LoginConstant.LoginUIEvent.LOGIN_FAILED, result.getResult(), result.getReasonDescription());
    }

    /**
     * [en]This method is used to handle the success logout
     * [cn]处理登出成功事件
     *
     * @param userId            [en]Indicates user id
     *                          [cn]用户标识
     */
    public void handleLogoutSuccess(int userId ) {
        LogUtil.e(TAG, "logout success " );
        this.loginEventNotifyUI.onLoginEventNotify(LoginConstant.LoginUIEvent.LOGOUT, 0, "logout success ");
    }

    /**
     * [en]This method is used to handle the failed logout
     * [cn]处理登出失败事件
     *
     * @param userId            [en]Indicates user id
     *                          [cn]用户标识
     * @param result            [en]Indicates response results
     *                          [cn]响应结果
     */
    public void handleLogoutFailed(int userId, TsdkCommonResult result) {
        LogUtil.e(TAG, "logout failed: " + result.getReasonDescription());
//        this.loginEventNotifyUI.onLoginEventNotify(LoginConstant.LoginUIEvent.LOGOUT, 0, result.getReasonDescription());
    }

    /**
     * [en]This method is used to handle the force logout
     * [cn]处理强制登出事件
     *
     * @param userId            [en]Indicates user id
     *                          [cn]用户标识
     */
    public void handleForceLogout(int userId ) {
        LogUtil.i(TAG, "voip force logout");
        isForceLogout = true;
        this.logout();

        this.loginEventNotifyUI.onLoginEventNotify(LoginConstant.LoginUIEvent.LOGOUT, 0, "voip force logout");
    }

    /**
     * [en]This method is used to handle the voip account status
     * [cn]处理voip状态事件
     *
     * @param userId            [en]Indicates user id
     *                          [cn]用户标识
     * @param voipAccountInfo   [en]voip account info
     *                          [cn]voip账号信息
     */
    public void handleVoipAccountStatus(int userId, TsdkVoipAccountInfo voipAccountInfo ) {
        LogUtil.e(TAG, "voip account status: " );

        this.sipNumber = voipAccountInfo.getNumber();
        if (!voipAccountInfo.getTerminal().equals("")) {
            this.terminal = voipAccountInfo.getTerminal();
        }
    }

    public void handleImAccountStatus(int userId, TsdkCommonResult result) {
        LogUtil.e(TAG, "im account status: " );
        //Reserved, temporarily not supported
    }

    /**
     * [en]This method is used to handle the failed firewall detect.
     * [cn]处理防火墙探测失败事件
     *
     * @param userId            [en]Indicates user id
     *                          [cn]用户标识
     * @param result            [en]Indicates response results
     *                          [cn]响应结果
     */
    public void handleFirewallDetectFailed(int userId, TsdkCommonResult result) {
        LogUtil.e(TAG, "firewall detect failed: " + result.getReasonDescription());
        this.loginEventNotifyUI.onLoginEventNotify(LoginConstant.LoginUIEvent.FIREWALL_DETECT_FAILED, result.getResult(), result.getReasonDescription());
    }

    /**
     * [en]This method is used to handle the failed build stg.
     * [cn]处理创建stg通道失败事件
     *
     * @param userId            [en]Indicates user id
     *                          [cn]用户标识
     * @param result            [en]Indicates response results
     *                          [cn]响应结果
     */
    public void handleBuildStgTunnelFailed(int userId, TsdkCommonResult result) {
        LogUtil.e(TAG, "build stg failed: " + result.getReasonDescription());
        this.loginEventNotifyUI.onLoginEventNotify(LoginConstant.LoginUIEvent.BUILD_STG_FAILED, result.getResult(), result.getReasonDescription());
    }

    /**
     * [en]This method is used to handle the security tunnel info
     * [cn]处理安全隧道信息事件
     *
     * @param userId             [en]Indicates user id
     *                           [cn]用户标识
     * @param firewallMode       [en]Indicates firewall mode
     *                           [cn]防火墙模式
     * @param securityTunnelInfo [en]Indicates security tunnel info
     *                           [cn]安全隧道信息
     */
    public void handleSecurityTunnelInfoInd(int userId, int firewallMode, TsdkSecurityTunnelInfo securityTunnelInfo) {
        LogUtil.i(TAG, "firewall mode: " + firewallMode + ", user id: " + userId);
        if (null == securityTunnelInfo)
        {
            LogUtil.i(TAG, "security tunnel info is null.");
        }
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }

    /**
     * This method is used to get sip number
     * 获取sip号码或者终端号
     *
     * @return String : sip number or terminal
     * 返回sip号码或者终端号
     */
    public String getTerminal() {
        if ((null == terminal) || (terminal.equals("")))
        {
            return sipNumber;
        }
        return terminal;
    }

    public String getSipNumber() {
        return sipNumber;
    }

    public TsdkContactsInfo getSelfInfo() {
        return selfInfo;
    }

    public void setSelfInfo(TsdkContactsInfo selfInfo) {
        this.selfInfo = selfInfo;
    }

    /**
     * This method is used to get account
     * 获取用户账号
     *
     * @return String : account number
     * 返回用户账号
     */
    public String getAccount() {
        return account;
    }

    private Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            LogUtil.i(TAG, "what:" + msg.what);
            parallelHandleMessage(msg);
        }
    };

    private void sendHandlerMessage(int what, Object object) {
        if (mMainHandler == null) {
            return;
        }
        Message msg = mMainHandler.obtainMessage(what, object);
        mMainHandler.sendMessage(msg);
    }

    private void parallelHandleMessage(Message msg) {
        if (msg.what == LoginEvent.LOGIN_E_EVT_AUTH_SUCCESS.getIndex()) {
            //IM login
            //登录IM
            ImMgr.getInstance().login((ImAccountInfo) msg.obj);
        }
    }

}
