package eu.imouto.hupl;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import eu.imouto.hupl.HttpUploader.FileToUpload;


public class AsyncUpload extends AsyncTask<Void, Integer, HttpUploader.HttpResult>
{
    private Context m_context;
    private Host m_host;
    private FileToUpload m_file;

    NotificationManager m_notMgr;
    NotificationCompat.Builder m_notBldr;

    public AsyncUpload(Context context, Host host, FileToUpload file)
    {
        m_context = context;
        m_host = host;
        m_file = file;
    }

    @Override
    protected HttpUploader.HttpResult doInBackground(Void... voids)
    {
        String strTitle = m_context.getResources().getString(R.string.app_name);
        String strUpl = m_context.getResources().getString(R.string.notification_status_uploading);

        m_notMgr = (NotificationManager) m_context.getSystemService(Context.NOTIFICATION_SERVICE);
        m_notBldr = new NotificationCompat.Builder(m_context);
        m_notBldr.setContentTitle(strTitle);
        m_notBldr.setContentText(strUpl);
        m_notBldr.setColor(0xFF0095FF);
        m_notBldr.setSmallIcon(R.drawable.stat_sys_upload_anim0);
        m_notBldr.setLargeIcon(BitmapFactory.decodeResource(m_context.getResources(),R.mipmap.ic_launcher));
        m_notBldr.setProgress(0,0,true);
        m_notMgr.notify(1, m_notBldr.build());

        //upload file
        return HttpUploader.uploadFile(m_host, m_file);
    }

    @Override
    protected void onPostExecute(HttpUploader.HttpResult result)
    {
        String strCompl = m_context.getResources().getString(R.string.notification_status_complete);
        String strBadResp = m_context.getResources().getString(R.string.notification_status_bad_response);
        String strEx = m_context.getResources().getString(R.string.notification_status_exception);
        String strShare = m_context.getResources().getString(R.string.notification_button_share);
        String strOpen = m_context.getResources().getString(R.string.notification_button_open);
        String strCopy = m_context.getResources().getString(R.string.notification_button_copy);

        if (!result.exception.isEmpty())
        {
            m_notBldr.setSmallIcon(R.drawable.ic_error_white_24dp);
            m_notBldr.setContentText(strEx+" "+result.exception);
        }
        else if (result.status != 200)
        {
            m_notBldr.setSmallIcon(R.drawable.ic_error_white_24dp);
            m_notBldr.setContentText(strBadResp+" "+result.status);
        }
        else
        {
            //upload successful, show options to share/open the resulting link
            m_notBldr.addAction(R.drawable.ic_menu_share, strShare, createSharePendingIntent(result.response));
            m_notBldr.addAction(R.drawable.ic_menu_copy, strCopy, createCopyPendingIntent(result.response));
            m_notBldr.addAction(R.drawable.ic_menu_view, strOpen, createOpenPendingIntent(result.response));

            m_notBldr.setSmallIcon(R.drawable.ic_done_white_24dp);
            m_notBldr.setContentText(strCompl);
        }

        m_notBldr.setProgress(0,0,false);

        m_notMgr.notify(1, m_notBldr.build());
    }

    private PendingIntent createSharePendingIntent(String url)
    {
        String strChooserTitle = m_context.getResources().getString(R.string.share_chooser_title);

        Intent in = new Intent(Intent.ACTION_SEND);
        in.setType("text/plain");
        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        in.putExtra(Intent.EXTRA_TEXT, url);
        in = Intent.createChooser(in,strChooserTitle);

        return PendingIntent.getActivity(m_context,(int)System.currentTimeMillis(),in,0);
    }

    private PendingIntent createOpenPendingIntent(String url)
    {
        Intent in = new Intent(Intent.ACTION_VIEW);
        in.setData(Uri.parse(url));

        return PendingIntent.getActivity(m_context,(int)System.currentTimeMillis(),in,0);
    }

    private PendingIntent createCopyPendingIntent(String url)
    {
        CopyBroadcastReceiver receiver = new CopyBroadcastReceiver(url);
        IntentFilter filter = new IntentFilter("com.example.ACTION_COPY");
        m_context.registerReceiver(receiver, filter);

        Intent in = new Intent("com.example.ACTION_COPY");

        return PendingIntent.getBroadcast(m_context, (int)System.currentTimeMillis(), in, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private class CopyBroadcastReceiver extends BroadcastReceiver
    {
        private String m_url;

        public CopyBroadcastReceiver(String url)
        {
            m_url = url;
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
            String strCopied = m_context.getResources().getString(R.string.toast_url_copied);
            ClipboardManager clipMgr = (ClipboardManager)m_context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData data = ClipData.newPlainText("url", m_url);
            clipMgr.setPrimaryClip(data);
            Toast.makeText(m_context, strCopied, Toast.LENGTH_SHORT).show();
        }
    }

}