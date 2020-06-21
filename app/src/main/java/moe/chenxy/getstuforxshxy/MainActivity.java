package moe.chenxy.getstuforxshxy;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button login = findViewById(R.id.bt_login);
        login.setOnClickListener((newValue) -> new Thread() {
            public void run() {
                try {
                    // 获取账号和密码
                    TextInputEditText et_name = findViewById(R.id.et_name);
                    TextInputEditText et_pass = findViewById(R.id.et_pass);
                    String name = et_name.getText().toString().trim();
                    String pass = et_pass.getText().toString().trim();
                    // 组建post数据
                    String data = "sid="
                            + URLEncoder.encode(name, "utf-8") + "&passWord="
                            + URLEncoder.encode(pass, "utf-8") + "";
                    //Log.i("Art_Chen", "name : " + name + "pass: " + pass + "data: " + data);
                    String path = "http://yx.xshxy.cn:802/login";
                    URL url = new URL(path);
                    HttpURLConnection conn = (HttpURLConnection) url
                            .openConnection();
                    conn.setConnectTimeout(5000);// 5秒
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                    conn.setRequestProperty("Content-Length", data.length()
                            + "");

                    conn.setDoOutput(true);
                    conn.getOutputStream().write(data.getBytes());
                    int code = conn.getResponseCode();
                    String cookieVal = null;
                    if (code == 200) {
                        InputStream in = conn.getInputStream();
                        cookieVal = conn.getHeaderField("Set-Cookie");
                        String content = StreamTools.readString(in);
                        showToast(content);
                    }
                    String getStu = "http://yx.xshxy.cn:802/data/getStuForShow";
                    URL getStuUrl = new URL(getStu);

                    HttpURLConnection conn1 = (HttpURLConnection) getStuUrl
                            .openConnection();
                    if (cookieVal != null) {
                        conn1.setRequestProperty("Cookie", cookieVal); //继承Cookie
                    }
                    conn1.setConnectTimeout(5000);
                    conn1.setRequestMethod("POST");
                    conn1.setRequestProperty("Referer", "http://yx.xshxy.cn:802/rinfo");
                    conn1.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn1.setDoOutput(true);
                    conn1.getOutputStream().write(data.getBytes());
                    String resultData = "";
                    if (conn.getResponseCode() == 200) {
                        InputStream is = conn1.getInputStream();
                        InputStreamReader isr = new InputStreamReader(is);
                        BufferedReader bufferReader = new BufferedReader(isr);
                        String inputLine = "";
                        while ((inputLine = bufferReader.readLine()) != null) {
                            resultData += inputLine + "\n";
                        }
                        Analysis(resultData);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast("Art_Chen: 连接服务器时发生错误");
                }
            };
        }.start());
    }

    /**
     * 解析
     * @param jsonStr json字符串
     * @throws JSONException
     */
    private void Analysis(String jsonStr) throws JSONException {
        String[] show = new String[7];
        // 初始化list数组对象
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(jsonStr);
        JSONObject dataObj = (JSONObject) jsonObject.get("data");
        // 初始化map数组对象
        HashMap<String, Object> map = new HashMap<>();
        map.put("姓名", dataObj.getString("studentName"));
        map.put("性别", dataObj.getString("sex"));
        map.put("宿舍楼", dataObj.getString("dorPath"));
        map.put("学号", dataObj.getString("sid"));
        map.put("专业", dataObj.getString("majorName"));
        map.put("班级", dataObj.getString("className"));
        map.put("欠费金额(学费)", dataObj.getString("arrearsMoney"));
        list.add(map);
        runOnUiThread(() -> {
            AlertDialog.Builder listDialog =
                    new AlertDialog.Builder(MainActivity.this);
            listDialog.setTitle("结果");
            for (HashMap<String, Object> m : list) {
                int i = 0;
                for (String k : m.keySet()) {
                    show[i]=(k + " : " + m.get(k));
                    i++;
                }
            }
            listDialog.setItems(show, (DialogInterface.OnClickListener) (dialog, which) -> {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("Art_Chen", show[which]);
                cm.setPrimaryClip(mClipData);
                Toast.makeText(MainActivity.this,
                        "已复制到剪切板",
                        Toast.LENGTH_SHORT).show();
            });
            listDialog.show();
        });

    }


    public void showToast(final String content) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), content, Toast.LENGTH_LONG).show());
    }
}
