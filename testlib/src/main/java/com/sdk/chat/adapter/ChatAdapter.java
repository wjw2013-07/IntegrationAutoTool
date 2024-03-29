package com.sdk.chat.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.sdk.chat.ChatActivity;
import com.sdk.chat.chatrow.BaseChatRow;
import com.sdk.chat.chatrow.BreakTipChatRow;
import com.sdk.chat.chatrow.CardRxChatBox;
import com.sdk.chat.chatrow.ChatRowType;
import com.sdk.chat.chatrow.ChatRowUtils;
import com.sdk.chat.chatrow.FileRxChatRow;
import com.sdk.chat.chatrow.FileTxChatRow;
import com.sdk.chat.chatrow.IChatRow;
import com.sdk.chat.chatrow.IframeRxChatRow;
import com.sdk.chat.chatrow.ImageRxChatRow;
import com.sdk.chat.chatrow.ImageTxChatRow;
import com.sdk.chat.chatrow.InvestigateChatRow;
import com.sdk.chat.chatrow.RichRxChatBow;
import com.sdk.chat.chatrow.RichTxChatBox;
import com.sdk.chat.chatrow.TextRxChatRow;
import com.sdk.chat.chatrow.TextTxChatRow;
import com.sdk.chat.chatrow.TripRxChatRow;
import com.sdk.chat.chatrow.VoiceRxChatRow;
import com.sdk.chat.chatrow.VoiceTxChatRow;
import com.sdk.chat.holder.BaseHolder;
import com.sdk.chat.listener.ChatListClickListener;
import com.sdk.utils.DateUtil;
import com.sdk.utils.MediaPlayTools;
import com.moor.imkf.model.entity.FromToMessage;

import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * Package Name:com.imkfsdk.chat.adapter
 * </p>
 * <p>
 * Class Name:ChatAdapter
 * <p>
 * Description:
 * </p>
 *
 * @Author  longwei
 * @Version 1.0 2016/3/9 Release
 * @Reviser: Martin
 * @Modification Time:2018/11/19 3:24 PM
 */
public class ChatAdapter extends BaseAdapter {

    private List<FromToMessage> messageList;

    private Context context;

    private HashMap<Integer, IChatRow> chatRowHashMap;
    public int mVoicePosition = -1;

    protected View.OnClickListener mOnClickListener;

    public ChatAdapter(Context context, List<FromToMessage> messageList) {
        this.context = context;
        this.messageList = messageList;
        chatRowHashMap = new HashMap<Integer, IChatRow>();
        mOnClickListener = new ChatListClickListener((ChatActivity)context, null);
        initRowItems();
    }

    void initRowItems() {
        chatRowHashMap.put(Integer.valueOf(1), new TextRxChatRow(1));
        chatRowHashMap.put(Integer.valueOf(2), new TextTxChatRow(2));
        chatRowHashMap.put(Integer.valueOf(3), new ImageRxChatRow(3));
        chatRowHashMap.put(Integer.valueOf(4), new ImageTxChatRow(4));
        chatRowHashMap.put(Integer.valueOf(5), new VoiceRxChatRow(5));
        chatRowHashMap.put(Integer.valueOf(6), new VoiceTxChatRow(6));
        chatRowHashMap.put(Integer.valueOf(7), new InvestigateChatRow(7));
        chatRowHashMap.put(Integer.valueOf(8), new FileRxChatRow(8));
        chatRowHashMap.put(Integer.valueOf(9), new FileTxChatRow(9));
        chatRowHashMap.put(Integer.valueOf(10), new IframeRxChatRow(10));
        chatRowHashMap.put(Integer.valueOf(11), new BreakTipChatRow(11));
        chatRowHashMap.put(Integer.valueOf(12), new TripRxChatRow(12));
        chatRowHashMap.put(Integer.valueOf(13), new RichRxChatBow(13));
        chatRowHashMap.put(Integer.valueOf(14), new RichTxChatBox(14));
        chatRowHashMap.put(Integer.valueOf(15), new CardRxChatBox(15));

    }

    public void setVoicePosition(int position) {
        mVoicePosition = position;
    }

    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public synchronized FromToMessage getItem(int position) {
        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //根据该条消息获得类型的数字(在枚举类型中的ordinal)
    @Override
    public synchronized int getItemViewType(int position) {
        FromToMessage message = getItem(position);
        int type = getBaseChatRow(ChatRowUtils.getChattingMessageType(message), message.userType.equals("0")).getChatViewType();
        return type;
    }

    //消息类型的数量
    @Override
    public int getViewTypeCount() {
        return ChatRowType.values().length;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FromToMessage message = getItem(position);

        if(message == null) {
            return null;
        }


        //构建消息的view
        Integer messageType = ChatRowUtils.getChattingMessageType(message);
        BaseChatRow chatRow = getBaseChatRow(messageType, message.userType.equals("0"));
        View chatView = chatRow.buildChatView(LayoutInflater.from(context), convertView);
        BaseHolder baseHolder = (BaseHolder) chatView.getTag();

        //显示时间
        boolean showTimer = false;
        if(position == 0) {
            showTimer = true;
        }
        if(position != 0) {
            FromToMessage previousItem = getItem(position - 1);
            if((message.when - previousItem.when >= 180000L)) {
                showTimer = true;
            }
        }

        if(showTimer) {
            baseHolder.getChattingTime().setVisibility(View.VISIBLE);
            baseHolder.getChattingTime().setText(DateUtil.getDateString(message.when, DateUtil.SHOW_TYPE_CALL_LOG).trim());
            baseHolder.getChattingTime().setTextColor(Color.WHITE);
            baseHolder.getChattingTime().setPadding(6, 2, 6, 2);
        } else {
            baseHolder.getChattingTime().setVisibility(View.GONE);
            baseHolder.getChattingTime().setShadowLayer(0.0F, 0.0F, 0.0F, 0);
            baseHolder.getChattingTime().setBackgroundResource(0);
        }
        //填充消息的数据
        chatRow.buildChattingBaseData(context, baseHolder, message, position);

        return chatView;
    }


    /**
     * 根据消息类型返回相对应的消息Item
     * @param rowType
     * @param isSend
     * @return
     */
    public BaseChatRow getBaseChatRow(int rowType , boolean isSend) {
        StringBuilder builder = new StringBuilder("C").append(rowType);
        if(isSend) {
            builder.append("T");
        } else {
            builder.append("R");
        }
        ChatRowType fromValue = ChatRowType.fromValue(builder.toString());
        IChatRow iChatRow = chatRowHashMap.get(fromValue.getId().intValue());
        return (BaseChatRow) iChatRow;
    }

    public View.OnClickListener getOnClickListener() {
        return mOnClickListener;
    }

    public void onPause() {
        mVoicePosition = -1;
        MediaPlayTools.getInstance().stop();
    }
}
