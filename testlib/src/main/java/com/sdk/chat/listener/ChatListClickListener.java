package com.sdk.chat.listener;

import android.view.View;

import com.sdk.chat.ChatActivity;
import com.sdk.chat.adapter.ChatAdapter;
import com.sdk.chat.holder.ViewHolderTag;
import com.sdk.utils.MediaPlayTools;
import com.moor.imkf.db.dao.MessageDao;
import com.moor.imkf.model.entity.FromToMessage;

/**
 * Created by longwei on 2016/3/10.
 */
public class ChatListClickListener implements View.OnClickListener {

    /**
     * 聊天界面
     */
    private ChatActivity mContext;

    public ChatListClickListener(ChatActivity activity, String userName) {
        mContext = activity;
    }

    @Override
    public void onClick(View v) {
        ViewHolderTag holder = (ViewHolderTag) v.getTag();
        FromToMessage iMessage = holder.detail;

        switch (holder.type) {
            case ViewHolderTag.TagType.TAG_RESEND_MSG:
                mContext.resendMsg(iMessage, holder.position);
                break;
            case ViewHolderTag.TagType.TAG_VOICE:
                if (iMessage == null) {
                    return;
                }
                MediaPlayTools instance = MediaPlayTools.getInstance();
                final ChatAdapter adapterForce = mContext.getChatAdapter();
                if (instance.isPlaying()) {
                    instance.stop();
                }
                if (adapterForce.mVoicePosition == holder.position) {
                    adapterForce.mVoicePosition = -1;
                    adapterForce.notifyDataSetChanged();
                    return;
                }
                if (iMessage.unread2 != null && iMessage.unread2.equals("1")) {
                    iMessage.unread2 = "0";
                    holder.holder.voiceUnread.setVisibility(View.GONE);
                }
                MessageDao.getInstance().updateMsgToDao(iMessage);
                adapterForce.notifyDataSetChanged();

                instance.setOnVoicePlayCompletionListener(new MediaPlayTools.OnVoicePlayCompletionListener() {

                    @Override
                    public void OnVoicePlayCompletion() {
                        adapterForce.mVoicePosition = -1;
                        adapterForce.notifyDataSetChanged();
                    }
                });
                String fileLocalPath = holder.detail.filePath;
                instance.playVoice(fileLocalPath, false);
                adapterForce.setVoicePosition(holder.position);
                adapterForce.notifyDataSetChanged();
                break;
            case ViewHolderTag.TagType.TAG_SEND_MSG:
                mContext.sendMsg(iMessage);
                break;
        }
    }
}
