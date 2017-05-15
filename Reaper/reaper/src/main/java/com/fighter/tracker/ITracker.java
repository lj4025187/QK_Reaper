package com.fighter.tracker;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.HashMap;

/**
 * Created by lichen on 17-5-8.
 */

public interface ITracker {
    /**
     *  init tracker
     *  @param context the sdk context
     * */
    void init(Context context);

    /**
     * tracker display event.
     *
     * @param context the context
     * @param param the display event param
     */
    void trackDisplayEvent(@NonNull Context context, @NonNull EventDisPlayParam param);

    /**
     * track click event.
     *
     * @param context the context
     * @param param the click event param
     */
    void trackClickEvent(@NonNull Context context,@NonNull EventClickParam param);

    /**
     * tracker action event
     *
     * @param context the context
     * @param param the action event param
     */
    void trackActionEvent(@NonNull Context context, @NonNull EventActionParam param);

    /**
     * tracker download fail event
     *
     * @param context the context
     * @param param the download event param
     */
    void trackDownloadEvent(@NonNull Context context, @NonNull EventDownLoadFail param);
}
