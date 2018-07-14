package com.huawei.opensdk.ec_sdk_demo.logic.conference.mvp;


import android.content.Context;
import android.view.ViewGroup;

import com.huawei.ecterminalsdk.base.TsdkConfRole;
import com.huawei.opensdk.callmgr.CallMgr;
import com.huawei.opensdk.demoservice.ConfConstant;
import com.huawei.opensdk.demoservice.MeetingMgr;
import com.huawei.opensdk.demoservice.Member;
import com.huawei.opensdk.ec_sdk_demo.R;
import com.huawei.opensdk.ec_sdk_demo.ui.base.MVPBasePresenter;


public class DataConfPresenter extends MVPBasePresenter<IDataConfContract.DataConfView>
        implements IDataConfContract.IDataConfPresenter
{
    private String confID;

    @Override
    public void attachSurfaceView(ViewGroup container, Context context)
    {
        MeetingMgr.getInstance().attachSurfaceView(container, context);
    }

    @Override
    public void sendChatMsg(String content) {
        MeetingMgr.getInstance().sendConfMessage(content);
    }

    @Override
    public void setConfID(String confID) {
        this.confID = confID;
    }

    public String getSubject()
    {
        return MeetingMgr.getInstance().getCurrentConferenceBaseInfo().getSubject();
    }

    @Override
    public void closeConf()
    {
        int result = MeetingMgr.getInstance().leaveConf();
        if (result != 0) {
            getView().showCustomToast(R.string.leave_conf_fail);
            return;
        }
    }

    @Override
    public void finishConf()
    {
        int result = MeetingMgr.getInstance().endConf();
        if (result != 0) {
            getView().showCustomToast(R.string.end_audio_conf);
            return;
        }
    }


    @Override
    public boolean muteSelf()
    {
        Member self = MeetingMgr.getInstance().getCurrentConferenceSelf();

        if (self == null)
        {
            return false;
        }
        int result = MeetingMgr.getInstance().muteAttendee(self, !self.isMute());
        if (result != 0)
        {
            return false;
        }
        return true;
    }

    @Override
    public int switchLoudSpeaker()
    {
        return CallMgr.getInstance().switchAudioRoute();
    }

    @Override
    public boolean isChairMan()
    {
        Member self = MeetingMgr.getInstance().getCurrentConferenceSelf();

        return (self.getRole() == TsdkConfRole.TSDK_E_CONF_ROLE_ATTENDEE ? false:true);
    }
}
