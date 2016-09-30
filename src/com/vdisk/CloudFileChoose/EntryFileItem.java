package com.vdisk.CloudFileChoose;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;

import com.idwtwt.extrabackup.R;
import com.vdisk.android.fileChoose.*;
import com.vdisk.net.VDiskAPI.Entry;
/**
 * 文件对象，继承自Entry
 * 
 * @author NashLegend
 */
public class EntryFileItem extends Entry {

    //private static final long serialVersionUID = 2675728441786325207L;

    /**
     * 文件在文件列表中显示的icon
     */
    private int entryicon =R.drawable.ic_launcher;

    /**
     * 文件是否在列表中被选中
     */
    private boolean selected = false;

    /**
     * 文件类型，默认为FILE_TYPE_NORMAL，即普通文件。
     */
    private int fileType = FileUtil.FILE_TYPE_NORMAL;

    /**
     * 文件后缀
     */
    private String suffix = "";

    @SuppressWarnings("unchecked")
	public  EntryFileItem(Entry entry) {
    	bytes = entry.bytes;
		hash = entry.hash;
		//entryicon = EntryFileItem.entryicon;
		isDir = entry.isDir;
		path = entry.path;
		//parentPath=entryFileItem.parentPath();
		root = entry.root;
		size = entry.size;
		mimeType = entry.mimeType;
		thumbExists = entry.thumbExists;
		isDeleted = entry.isDeleted;
		thumb = entry.thumb;
		Object json_contents =entry.contents;
		if (json_contents != null && json_contents instanceof JSONArray) {
			contents = new ArrayList<Entry>();
			Object Eentry;
			Iterator<?> it = ((JSONArray) json_contents).iterator();
			while (it.hasNext()) {
				Eentry = it.next();
				if (Eentry instanceof Map) {
					contents.add(new Entry((Map<String, Object>) Eentry));
				}
			}
		} else {
			contents = null;
		}
        setFileTypeBySuffix();
    }
    /**
     * 根据后缀取得文件类型
     */
    private void setFileTypeBySuffix() {

        int type = FileUtil.getFileType(this);
        setFileType(type);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getFileType() {
        return fileType;
    }

    /**
     * 设置fileTyle,同时修改icon
     * 
     * @param fileType
     */
    public void setFileType(int fileType) {
        this.fileType = fileType;
        switch (fileType) {
            case FileUtil.FILE_TYPE_APK:
                setIcon(R.drawable.format_apk);
                break;
            case FileUtil.FILE_TYPE_FOLDER:
                setIcon(R.drawable.format_folder);
                break;
            case FileUtil.FILE_TYPE_IMAGE:
                setIcon(R.drawable.format_picture);
                break;
            case FileUtil.FILE_TYPE_NORMAL:
                setIcon(R.drawable.format_unkown);
                break;
            case FileUtil.FILE_TYPE_AUDIO:
                setIcon(R.drawable.format_music);
                break;
            case FileUtil.FILE_TYPE_TXT:
                setIcon(R.drawable.format_text);
                break;
            case FileUtil.FILE_TYPE_VIDEO:
                setIcon(R.drawable.format_media);
                break;
            case FileUtil.FILE_TYPE_ZIP:
                setIcon(R.drawable.format_zip);
                break;
            case FileUtil.FILE_TYPE_HTML:
                setIcon(R.drawable.format_html);
                break;
            case FileUtil.FILE_TYPE_PDF:
                setIcon(R.drawable.format_pdf);
                break;
            case FileUtil.FILE_TYPE_WORD:
                setIcon(R.drawable.format_word);
                break;
            case FileUtil.FILE_TYPE_EXCEL:
                setIcon(R.drawable.format_excel);
                break;
            case FileUtil.FILE_TYPE_PPT:
                setIcon(R.drawable.format_ppt);
                break;
            case FileUtil.FILE_TYPE_TORRENT:
                setIcon(R.drawable.format_torrent);
                break;
            case FileUtil.FILE_TYPE_EBOOK:
                setIcon(R.drawable.format_ebook);
                break;
            case FileUtil.FILE_TYPE_CHM:
                setIcon(R.drawable.format_chm);
                break;
            default:
                setIcon(R.drawable.format_unkown);
                break;
        }
    }

    public int getIcon() {
        return entryicon;
    }

    public void setIcon(int icon) {
        this.entryicon = icon;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
