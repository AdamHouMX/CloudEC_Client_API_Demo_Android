package com.huawei.opensdk.ec_sdk_demo.logic.conference.mvp;

import com.huawei.ecterminalsdk.base.TsdkConfMediaType;
import com.huawei.ecterminalsdk.base.TsdkConfRecordMode;
import com.huawei.opensdk.demoservice.Member;
import com.huawei.opensdk.ec_sdk_demo.logic.BaseView;

import java.util.List;


public interface ConfCreateContract
{
    interface ConfCreateView extends BaseView
    {
        void refreshListView(List<Member> memberList);

        void createFailed();

        void createSuccess();

        void updateAccessNumber(String accessNumber);
    }

    interface IConfCreatePresenter
    {
        void setStartTime(String startTime);

        void setMediaType(TsdkConfMediaType mediaType);

        void setRecordType(TsdkConfRecordMode recordType);

        void setAutoRecord(boolean isAtuo);

        void setBookType(boolean isInstantConference);

        void setDuration(int duration);

        void setSubject(String subject);

        void addMember(Member member);

        void delMember(int index);

        void createConference();

        void receiveBroadcast(String broadcastName, Object obj);

        void updateAccessNumber(String accessNumber);
    }
}
