package db;

import java.util.List;

import entities.ThreadInfo;

/**数据访问接口
 * Created by Stefan on 2016/9/12.
 */
public interface ThreadDao {
    public void  insertThread(ThreadInfo threadInfo);

    public void  deleteThread(String url, int thread_id);

    public void updateThread(String url,int thread_id,int finished);

    public List<ThreadInfo> getThread(String url);

    public boolean isExits(String url,int thread_id);
}
