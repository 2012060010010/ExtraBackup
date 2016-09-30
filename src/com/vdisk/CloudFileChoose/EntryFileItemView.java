package com.vdisk.CloudFileChoose;

import com.idwtwt.extrabackup.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View.OnClickListener;

/**
 * 文件列表单个item的view
 */
public class EntryFileItemView extends FrameLayout implements OnClickListener,
		OnCheckedChangeListener {

	private ImageView icon;
	private TextView title;
	private CheckBox checkBox;
	private ViewGroup rootFileItemView;
	private EntryFileListAdapter adapter;
	private int fileMode = VdiskFileDialog.FILE_MODE_OPEN_MULTI;
	private boolean selectable = true;

	private EntryFileItem entryFileItem;

	public EntryFileItemView(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.entry_item_view, this);
		icon = (ImageView) findViewById(R.id.image_file_icon);
		title = (TextView) findViewById(R.id.text_file_title);
		rootFileItemView = (ViewGroup) findViewById(R.id.rootFileItemView);
		checkBox = (CheckBox) findViewById(R.id.checkbox_file_item_select);
		setOnClickListener(this);
	}

	public EntryFileItem getFileItem() {
		return entryFileItem;
	}

	public void setFileItem(EntryFileItem fileItem, EntryFileListAdapter adapter,
			int fileMode) {
		this.entryFileItem = fileItem;
		this.adapter = adapter;
		this.fileMode = fileMode;
		icon.setImageResource(fileItem.getIcon());
		title.setText(fileItem.fileName());
		toggleSelectState();

		if (!fileItem.isDir
				&& (fileMode == VdiskFileDialog.FILE_MODE_OPEN_FOLDER_MULTI || fileMode == VdiskFileDialog.FILE_MODE_OPEN_FOLDER_SINGLE)) {
			checkBox.setEnabled(false);
			selectable = false;
			checkBox.setOnCheckedChangeListener(null);
			return;
		}

		if (fileItem.isDir
				&& (fileMode ==VdiskFileDialog.FILE_MODE_OPEN_FILE_MULTI || fileMode == VdiskFileDialog.FILE_MODE_OPEN_FILE_SINGLE)) {
			checkBox.setEnabled(false);
			selectable = false;
			checkBox.setOnCheckedChangeListener(null);
			return;
		}
		selectable = true;
		checkBox.setEnabled(true);
		checkBox.setOnCheckedChangeListener(this);
	}

	/**
	 * 切换选中、未选中状态,fileItem.setSelected(boolean)先发生;
	 */
	public void toggleSelectState() {
		if (entryFileItem.isSelected()) {
			rootFileItemView
					.setBackgroundResource(R.drawable.bg_file_item_select);
		} else {
			rootFileItemView
					.setBackgroundResource(R.drawable.bg_file_item_normal);
		}
		checkBox.setOnCheckedChangeListener(null);
		checkBox.setChecked(entryFileItem.isSelected());
		checkBox.setOnCheckedChangeListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() != R.id.checkbox_file_item_select) {
			if (entryFileItem.isDir) {
				openFolder();
			} else {
				// 选中一个
				selectOne();
			}
		}
	}

	public void selectOne() {
		if (selectable) {
			if (entryFileItem.isSelected()) {
				// 取消选中状态，只在FileItemView就可以
				entryFileItem.setSelected(!entryFileItem.isSelected());
				toggleSelectState();
				adapter.unselectOne();
			} else {
				// 如果要选中某个FileItem，则必须要在adapter里面进行，因为如果是单选的话，还要取消其他的选中状态
				adapter.selectOne(entryFileItem);
			}
		}
	}

	/**
	 * 打开文件夹
	 */
	public void openFolder() {
		adapter.openFolder(entryFileItem);
	}

	public EntryFileListAdapter getAdapter() {
		return adapter;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			adapter.selectOne(entryFileItem);
		} else {
			entryFileItem.setSelected(false);
			rootFileItemView
					.setBackgroundResource(R.drawable.bg_file_item_normal);
			adapter.unselectOne();
		}
	}

	public int getFileMode() {
		return fileMode;
	}
}
