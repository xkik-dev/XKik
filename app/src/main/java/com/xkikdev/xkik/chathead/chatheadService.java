package com.xkikdev.xkik.chathead;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v7.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.flipkart.chatheads.ChatHead;
import com.flipkart.chatheads.ChatHeadViewAdapter;
import com.flipkart.chatheads.arrangement.MaximizedArrangement;
import com.flipkart.chatheads.arrangement.MinimizedArrangement;
import com.flipkart.chatheads.container.DefaultChatHeadManager;
import com.flipkart.chatheads.container.WindowManagerContainer;
import com.flipkart.circularImageView.CircularDrawable;
import com.github.bassaer.chatmessageview.models.Message;
import com.github.bassaer.chatmessageview.models.User;
import com.github.bassaer.chatmessageview.views.ChatView;
import com.xkikdev.xkik.MainActivity;
import com.xkikdev.xkik.R;
import com.xkikdev.xkik.Util;
import com.xkikdev.xkik.datatype_parsers.msgText;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class chatheadService extends Service {

    final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final IBinder mBinder = new LocalBinder();
    private DefaultChatHeadManager<String> chatHeadManager;
    private WindowManagerContainer windowManagerContainer;
    private Map<String, View> viewCache = new HashMap<>();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        windowManagerContainer = new WindowManagerContainer(this);
        chatHeadManager = new DefaultChatHeadManager<String>(this, windowManagerContainer);

        // The view adapter is invoked when someone clicks a chat head.
        chatHeadManager.setViewAdapter(new ChatHeadViewAdapter<String>() {

            @Override
            public View attachView(String key, ChatHead chatHead, ViewGroup parent) {

                View cachedView = viewCache.get(key);
                if (cachedView == null) {
                    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    View view = inflater.inflate(R.layout.fragment_bubblechat, parent, false);
                    //TextView identifier = (TextView) view.findViewById(R.id.identifier);
                    //identifier.setText(key);
                    ChatView mChatView = (ChatView) view.findViewById(R.id.chat_view);
                    mChatView.setRightBubbleColor(Color.RED);
                    mChatView.setLeftBubbleColor(Color.WHITE);
                    mChatView.setBackgroundColor(Color.rgb(50, 50, 50));
                    mChatView.setSendButtonColor(Color.rgb(70, 70, 90));
                    mChatView.setSendIcon(R.drawable.ic_action_send);
                    mChatView.setRightMessageTextColor(Color.WHITE);
                    mChatView.setLeftMessageTextColor(Color.BLACK);
                    mChatView.setUsernameTextColor(Color.WHITE);
                    mChatView.setSendTimeTextColor(Color.WHITE);
                    mChatView.setDateSeparatorColor(Color.WHITE);
                    mChatView.setInputTextHint("new message...");
                    mChatView.setMessageMarginTop(5);
                    mChatView.setMessageMarginBottom(5);

                    final User me = new User(1, "xk_dev", BitmapFactory.decodeResource(getResources(), R.drawable.face_1));
                    mChatView.send(new Message.Builder()
                            .setUser(me)
                            .setRightMessage(true)
                            .setMessageText("test")
                            .hideIcon(true)
                            .build()
                    );


                    cachedView = view;
                    viewCache.put(key, view);
                }
                parent.addView(cachedView);
                return cachedView;
            }

            @Override
            public void detachView(String key, ChatHead<? extends Serializable> chatHead, ViewGroup parent) {
                View cachedView = viewCache.get(key);
                if (cachedView != null) {
                    parent.removeView(cachedView);
                }
            }

            @Override
            public void removeView(String key, ChatHead<? extends Serializable> chatHead, ViewGroup parent) {
                View cachedView = viewCache.get(key);
                if (cachedView != null) {
                    viewCache.remove(key);
                    parent.removeView(cachedView);
                }
            }

            @Override
            public Drawable getChatHeadDrawable(String key) {
                return chatheadService.this.getChatHeadDrawable();
            }
        });

        chatHeadManager.setArrangement(MinimizedArrangement.class, null);
        moveToForeground();

    }

    private Drawable getChatHeadDrawable(Bitmap icon) {
        CircularDrawable circularDrawable = new CircularDrawable();
        circularDrawable.setBitmapOrTextOrIcon(icon);
        return circularDrawable;
    }

    private Drawable getChatHeadDrawable() {
        return getChatHeadDrawable(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
    }

    private void moveToForeground() {
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("XKik heads")
                .setContentText("Click to configure.")
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))
                .build();

        startForeground(1, notification);
    }

    public void handleRecvMsg(msgText msg) {
        if (chatHeadManager.findChatHeadByKey(msg.getFromUserParsed()) == null) {
            addChatHead(msg.getFromUserParsed());
        }
    }

    public void addChatHead(final String uname) {
        chatHeadManager.addChatHead(uname, false, true);
        chatHeadManager.bringToFront(chatHeadManager.findChatHeadByKey(uname));
        new Thread() {
            @Override
            public void run() {
                final Bitmap pfp = Util.getUserProfilePicture(uname);
                if (pfp == null) {
                    return;
                }
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        CircularDrawable circularDrawable = new CircularDrawable();
                        circularDrawable.setBitmapOrTextOrIcon(pfp);
                        chatHeadManager.findChatHeadByKey(uname).setImageDrawable(circularDrawable);
                    }
                });
            }
        }.start();
    }

    public void removeChatHead(String uname) {
        chatHeadManager.removeChatHead(uname, true);
    }

    public void removeAllChatHeads() {
        chatHeadManager.removeAllChatHeads(true);
    }

    public void toggleArrangement() {
        if (chatHeadManager.getActiveArrangement() instanceof MinimizedArrangement) {
            chatHeadManager.setArrangement(MaximizedArrangement.class, null);
        } else {
            chatHeadManager.setArrangement(MinimizedArrangement.class, null);
        }
    }

    public void updateBadgeCount(String uname) {
        chatHeadManager.reloadDrawable(uname);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        windowManagerContainer.destroy();
    }

    public void minimize() {
        chatHeadManager.setArrangement(MinimizedArrangement.class, null);
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public chatheadService getService() {
            // Return this instance of LocalService so clients can call public methods
            return chatheadService.this;
        }
    }
}