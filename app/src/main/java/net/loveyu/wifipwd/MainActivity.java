package net.loveyu.wifipwd;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.content.ClipboardManager;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends Activity {

    private ArrayList<String[]> list;
    private Handler handler;
    final static int VersionUpdate = 1;
    final static int OpenUri = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Action ac = new Action(this);
        if (ac.check_root()) {
            setContentView(R.layout.activity_main);
            list = ac.get_list();
            TextView tv = (TextView) findViewById(R.id.textViewNotice);
            if (list == null) {
                tv.setText(getString(R.string.read_wifi_list_error));
            } else {
                if (list.size() == 0) {
                    tv.setText(getString(R.string.wifi_list_is_empty));
                } else {
                    ListView lv = (ListView) findViewById(R.id.listView);
                    lv.setAdapter(new WifiAdapter(this, list));
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            view.showContextMenu();
                        }
                    });
                    registerForContextMenu(lv);
                    tv.setText(getString(R.string.wifi_list_number) + list.size());
                }
            }
        } else {
            setContentView(R.layout.activity_no_root);
        }
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case VersionUpdate:
                        ((Report) msg.obj).open_dialog(MainActivity.this);
                        break;
                    case OpenUri:
                        open_url((String) msg.obj);
                        break;
                }
            }
        };
        new Thread(new Report(this, "http://www.loveyu.net/Update/WifiPwd.php")).start();
    }

    public void NotifyVersionUpdate(Report report) {
        Message msg = handler.obtainMessage(VersionUpdate);
        msg.obj = report;
        handler.sendMessage(msg);
    }

    public void NotifyOpenUri(String uri) {
        Message msg = handler.obtainMessage(OpenUri);
        msg.obj = uri;
        handler.sendMessage(msg);
    }

    private void open_url(String url) {
        Intent intent = new Intent();
        intent.setData(Uri.parse(url));
        intent.setAction(Intent.ACTION_VIEW);
        MainActivity.this.startActivity(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(Menu.NONE, R.string.copy_password, 0, getString(R.string.copy_password));
        menu.add(Menu.NONE, R.string.copy_ssid, 0, getString(R.string.copy_ssid));
        menu.add(Menu.NONE, R.string.copy_ssid_and_password, 0, getString(R.string.copy_ssid_and_password));
    }

    //选中菜单Item后触发
    public boolean onContextItemSelected(MenuItem item) {
        //关键代码在这里
        AdapterView.AdapterContextMenuInfo menuInfo;
        menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int indexListView = menuInfo.position;
        if (list == null) return false;
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        switch (item.getItemId()) {
            case R.string.copy_password:
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, list.get(indexListView)[1]));
                break;
            case R.string.copy_ssid:
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, list.get(indexListView)[0]));
                break;
            case R.string.copy_ssid_and_password:
                String[] s = list.get(indexListView);
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null,
                        getString(R.string.wifi_ssid) + s[0] + "\n" + getString(R.string.password) + s[1]));
                break;
            default:
                return false;
        }
        Toast.makeText(this, getString(R.string.already_copy), Toast.LENGTH_SHORT).show();
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_exit:
                finish();
                return true;
            case R.id.action_help:
                open_url("http://www.loveyu.org/3356.html?from=android_wifipwd");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}