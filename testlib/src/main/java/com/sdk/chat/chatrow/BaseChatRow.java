package com.sdk.chat.chatrow;

import android.content.Context;
import android.view.ContextMenu;
import android.view.View;

import com.sdk.R;
import com.sdk.chat.holder.BaseHolder;
import com.sdk.chat.holder.ViewHolderTag;
import com.moor.imkf.model.entity.FromToMessage;

/**
 * Created by longwei on 2016/3/9.
 * 处理基本的姓名和头像显示，消息发送状态，这些都是相同的
 */
public abstract class BaseChatRow implements IChatRow {

    int mRowType;

    public BaseChatRow(int type) {
        mRowType = type;
    }

    /**
     * 处理消息的发送状态设置
     *
     * @param position 消息的列表所在位置
     * @param holder   消息ViewHolder
     * @param l
     */
    protected static void getMsgStateResId(int position, BaseHolder holder, FromToMessage msg, View.OnClickListener l) {
        if (msg != null && msg.userType.equals("0")) {
            String msgStatus = msg.sendState;
            if (msgStatus.equals("false")) {
                holder.getUploadState().setImageResource(R.drawable.kf_chat_failure_msgs);
                holder.getUploadState().setVisibility(View.VISIBLE);
                if (holder.getUploadProgressBar() != null) {
                    holder.getUploadProgressBar().setVisibility(View.GONE);
                }
            } else if (msgStatus.equals("true")) {
                holder.getUploadState().setImageResource(0);
                holder.getUploadState().setVisibility(View.GONE);
                if (holder.getUploadProgressBar() != null) {
                    holder.getUploadProgressBar().setVisibility(View.GONE);
                }

            } else if (msgStatus.equals("sending")) {
                holder.getUploadState().setImageResource(0);
                holder.getUploadState().setVisibility(View.GONE);
                if (holder.getUploadProgressBar() != null) {
                    holder.getUploadProgressBar().setVisibility(View.VISIBLE);
                }

            } else {
                if (holder.getUploadProgressBar() != null) {
                    holder.getUploadProgressBar().setVisibility(View.GONE);
                }
            }

            ViewHolderTag holderTag = ViewHolderTag.createTag(msg, ViewHolderTag.TagType.TAG_RESEND_MSG, position);
            holder.getUploadState().setTag(holderTag);
            holder.getUploadState().setOnClickListener(l);
        }
    }

    /**
     * @param contextMenu
     * @param targetView
     * @param detail
     * @return
     */
    public abstract boolean onCreateRowContextMenu(ContextMenu contextMenu, View targetView, FromToMessage detail);

    /**
     * 填充数据
     *
     * @param context
     * @param baseHolder
     * @param detail
     * @param position
     */
    protected abstract void buildChattingData(Context context, BaseHolder baseHolder, FromToMessage detail, int position);

    @Override
    public void buildChattingBaseData(Context context, BaseHolder baseHolder, FromToMessage detail, int position) {

        // 处理其他逻辑
        buildChattingData(context, baseHolder, detail, position);

        //设置用户头像
        String imgPath = detail.im_icon;
        if (baseHolder.getChattingAvatar() != null) {
            /**
             * detail.userType:1
             * 会话左边
             */
            if ("1".equals(detail.userType)) {
                /**
                 * detail.showHtml
                 * true  ：是机器人头像
                 * null 或者 false ：为客服头像
                 */
                if (detail.showHtml != null && detail.showHtml) {
                    baseHolder.getChattingAvatar().setImageResource(R.drawable.kf_head_default_robot);
                    /**
                     * imgPath != null && !"".equals(imgPath)  ： 后台客服设置头像
                     */
                } else if (imgPath != null && !"".equals(imgPath)) {
                    /**
                     * else : 显示默认头像
                     */
                } else {
                    baseHolder.getChattingAvatar().setImageResource(R.drawable.kf_head_default_local);
                }
                /**
                 * detail.userType:0    会话右边
                 */
            } else {
                baseHolder.getChattingAvatar().setImageResource(R.drawable.kf_head_default_local);
            }
        }
    }


}
