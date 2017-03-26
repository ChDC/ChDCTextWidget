package com.chdc.chdctextwidget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;

import java.io.File;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ren.qinc.edit.PerformEdit;

public class ViewTextActivity extends AppCompatActivity {

    /**
     * 从小部件打开的 Intent Action
     */
    static final String ACTION_OPEN_BY_WIDGET ="com.chdc.chdctextwidget.ViewTextActivity.action.OPEN_BY_WIDGET";

    static final int REQUEST_CODE_STORAGE_ACCESS = 2;

    /**
     * 当前正在处理的文件
     */
    private String filePath = null;

    /**
     * 当前文件类型
     */
    private String fileType = "";

    /**
     * 当前文件内容
     */
    private String fileContent = "";

    /**
     * 当前是否是编辑模式
     */
    private boolean editMode = false;

    /**
     * 文本内容
     */
    EditText txtContent = null;
    PerformEdit mPerformEdit = null;
    ScrollView scroll = null;
    MenuItem mnuDone = null;
    MenuItem mnuDocumentStructure = null;

    SharedPreferences preferences = null;

    private static final String TAG = "ViewTextActivity";

    Uri documentUri = null;
//    private View btnOK = null;
//    private View btnUndo = null;
//    private View btnRedo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_text);

        preferences = getSharedPreferences("config", MODE_PRIVATE);
        txtContent = (EditText) findViewById(R.id.txtContent);
        mPerformEdit = new PerformEdit(txtContent);

        scroll = (ScrollView)findViewById(R.id.scroll);

        handleIntent();

        // 获取读取外置存储卡文件的权限
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            String uriString = preferences.getString("document_uri", null);
            if(uriString != null){
                documentUri = Uri.parse(uriString);
            }
            else{
                Intent intent =  new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent, REQUEST_CODE_STORAGE_ACCESS);
            }
        }
    }

    private void initValues(){
        filePath = "";
        fileContent = "";
        fileType = "";
        editMode = false;
    }

    /**
     * 初始化界面
     */
    private void initView(){
        if(mnuDone != null)
            mnuDone.setIcon( editMode ? R.drawable.toolbar_edit_done : R.drawable.toolbar_edit );
        if(mnuDocumentStructure != null){
            if("md".equals(fileType)){
                mnuDocumentStructure.setVisible(true);
            }
            else{
                mnuDocumentStructure.setVisible(false);
            }
        }
    }

    /**
     * 加载文件内容
     */
    private void loadContent(){
        if(filePath == null)
            return;
        File file = new File(filePath);
        String tmpFile = FileManager.getTempFile(filePath);

        // 设置标题
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(file.getName());

        fileContent = FileManager.getContent(file.toString());
        if(new File(tmpFile).exists()){
            // 临时文件存在，改为编辑模式
            setEditMode(true);
        }
        else if(file.exists()){
            // 临时文件不存在，预览模式
            setEditMode(false);
        }
        else{
            finish();
            return;
        }
        initView();
    }

    /**
     * 设置是否为编辑模式
     * @param editMode
     */
    private void setEditMode(boolean editMode){
//      if(this.editMode != editMode)
        if(this.editMode){
            fileContent = txtContent.getText().toString();
        }
        this.editMode = editMode;
        Utils.setEditTextUneditable(txtContent, !editMode);
        if(mnuDone != null)
            mnuDone.setIcon( editMode ? R.drawable.toolbar_edit_done : R.drawable.toolbar_edit );

        // 获取 TextView 的滚动位置
        int oldScrollY = scroll.getScrollY();
        Log.d(TAG, "setEditMode: " + String.valueOf(scroll.getScrollY()));
        // 加载文件数据
        CharSequence content = FileManager.processFileContent(fileType, fileContent, editMode);
        txtContent.setText(content);
        mPerformEdit.setDefaultText(content);
        scroll.setScrollY(oldScrollY);
    }

    /**
     * 处理 Intent
     */
    private void handleIntent()
    {
        Intent i = getIntent();
        if(i != null) {
            String action = i.getAction();
            String file = null;
            if(ACTION_OPEN_BY_WIDGET.equals(action))
            {
                file = i.getStringExtra("file");
            }
            else if(Intent.ACTION_VIEW.equals(action)){
                file =  Utils.getPath(this, i.getData());
            }

            if(file != null)
            {
                initValues();
                if(!Objects.equals(filePath, file)) {
                    filePath = file;
                    fileType = FileManager.getFileType(file);
                    loadContent();
                }
            }
        }
    }

    /**
     * 保存笔记内容
     */
    void saveFileContent(){
        String currentFileContent = getFileContent();
        if(filePath != null && (!Objects.equals(fileContent, currentFileContent)
                || new File(FileManager.getTempFile(filePath)).exists())) {
            fileContent = currentFileContent;
            boolean result = FileIO.writeAllText(this, documentUri, filePath, fileContent);
            if(result){
                // 删除临时文件
                FileIO.deleteFile(this, documentUri, FileManager.getTempFile(filePath));
                TextWidgetManager.update(this, filePath);
                Utils.showMessage(this, String.format("Success to save file %s!", filePath));
            }
            else{
                Utils.showMessage(this, String.format("Fail to save file %s!", filePath));
            }
        }
    }

    /**
     * 保存笔记内容到临时文件
     */
    void saveFileContentToTmpFile(){
        String currentFileContent = getFileContent();
        if(filePath != null && !Objects.equals(fileContent, currentFileContent)) {
            fileContent = currentFileContent;
            String tmpFile = FileManager.getTempFile(filePath);
            boolean result = FileIO.writeAllText(this, documentUri, tmpFile, fileContent);
            if(result){
                TextWidgetManager.update(this, filePath);
                Utils.showMessage(this, String.format("Success to save temp file %s!", filePath));
            }
            else{
                Utils.showMessage(this, String.format("Fail to save temp file %s!", filePath));
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public final void onActivityResult(final int requestCode, final int resultCode, final Intent resultData) {
        if (requestCode == REQUEST_CODE_STORAGE_ACCESS) {
            if (resultCode == Activity.RESULT_OK) {
                Uri treeUri = resultData.getData();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("document_uri", treeUri.toString());
                editor.apply();
                // Persist access permissions.
//                int takeFlags = resultData.getFlags()
//                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        }
    }

//    void showConfirmDialog(){
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("Exit without saving?");
//        builder.setTitle("Save");
//
//        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                saveFileContent();
//                setEditMode(false);
//                finish();
//            }
//        });
//        builder.setNeutralButton("Don't save", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                setEditMode(false);
//                finish();
//            }
//        });
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//
//            }
//        });
//
//        builder.create().show();
//    }


    private void showPopupWindow() {
        /**
         * 定位PopupWindow，让它恰好显示在Action Bar的下方。 通过设置Gravity，确定PopupWindow的大致位置。
         * 首先获得状态栏的高度，再获取Action bar的高度，这两者相加设置y方向的offset样PopupWindow就显示在action
         * bar的下方了。 通过dp计算出px，就可以在不同密度屏幕统一X方向的offset.但是要注意不要让背景阴影大于所设置的offset，
         * 否则阴影的宽度为offset.
         */
        // 获取状态栏高度
        Rect frame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);

//        状态栏高度：frame.top
        int xOffset = frame.top + getSupportActionBar().getHeight() - 25;// 减去阴影宽度，适配UI.
        int yOffset = Utils.Dp2Px(this, 5f); // 设置x方向offset为5dp

        View parentView = getLayoutInflater().inflate(R.layout.activity_view_text, null);
        View popView = getLayoutInflater().inflate(R.layout.document_structure, null);
        PopupWindow popWind = new PopupWindow(popView,
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);// popView即popupWindow的布局，ture设置focusAble.

        // 必须设置BackgroundDrawable后setOutsideTouchable(true)才会有效。这里在XML中定义背景，所以这里设置为null;
        popWind.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.spinner_dropdown_background));
        popWind.setOutsideTouchable(true); //点击外部关闭
        popWind.setAnimationStyle(android.R.style.Animation_Dialog);// 设置一个动画。

        loadPopupWindowViews(popView, popWind);

        // 设置Gravity，让它显示在右上角。
        popWind.showAtLocation(parentView, Gravity.LEFT | Gravity.TOP, yOffset, xOffset);
    }

    public void loadPopupWindowViews(final View popView, final PopupWindow popWind){
        final SparseArray<String> ds = Markdown.getDocumentStructure(getFileContent());
        int dsSize = ds.size();
        String[] data = new String[dsSize];
        Pattern regex = Pattern.compile("^(#+) ");
        for(int i = 0; i < dsSize; i++){
            String str = ds.valueAt(i);
            Matcher matcher = regex.matcher(str);
            if(matcher.find()){
                int len = matcher.group(1).length();
                data[i] = str.replaceFirst("^#+ ", Utils.stringTimes("   ", len - 1));
            }
        }

        ListView list = (ListView)popView.findViewById(R.id.list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                int lineNo = ds.keyAt((int)id);
                Rect rect = new Rect();
                txtContent.getLineBounds(lineNo, rect);
                scroll.setScrollY(rect.top);
                popWind.dismiss();
            }
        });
    }

    /**
     * 获取文本内容
     * @return
     */
    public String getFileContent(){
        if(editMode){
            return txtContent.getText().toString();
        }
        else{
            return fileContent;
        }
    }

    @Override
    protected void onNewIntent (Intent intent)
    {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_viewtext, menu);

        mnuDone = menu.findItem(R.id.action_done);
        mnuDocumentStructure = menu.findItem(R.id.action_document_structure);

        if(mnuDone != null)
            mnuDone.setIcon( editMode ? R.drawable.toolbar_edit_done : R.drawable.toolbar_edit );
        if(mnuDocumentStructure != null){
            if("md".equals(fileType)){
                mnuDocumentStructure.setVisible(true);
            }
            else{
                mnuDocumentStructure.setVisible(false);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem mi)
    {
        switch (mi.getItemId())
        {
            case R.id.action_done:
                if(this.editMode) {
                    // 编辑模式
                    saveFileContent();
//                    setEditMode(false);
                    editMode = false;
                    finish();
                }
                else{
                    // 非编辑模式
                    setEditMode(true);
                }

                break;

            case R.id.action_document_structure:
                showPopupWindow();
                break;

            case R.id.action_undo:
                mPerformEdit.undo();
                break;

            case R.id.action_redo:
                mPerformEdit.redo();
                break;

        }
        return true;
    }

    @Override
    public void onBackPressed(){
        if(this.editMode){
            // 编辑模式
            saveFileContentToTmpFile();
        }
        finish();
//        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        if(editMode)
            saveFileContentToTmpFile();
        super.onPause();
    }
}
