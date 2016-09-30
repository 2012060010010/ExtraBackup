package com.vdisk.CloudFileChoose;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import com.vdisk.net.VDiskAPI;
import com.vdisk.net.VDiskAPI.Entry;
import com.vdisk.net.exception.VDiskException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class EntryFileListAdapter extends BaseAdapter {
	private ArrayList<EntryFileItem> list = new ArrayList<EntryFileItem>();
	private Context mContext;
	private String currentDirectory;
	private EntryFileDialogView dialogView;
    private VDiskAPI<?> mApi;
	public EntryFileListAdapter(Context Context) {
		mContext = Context;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = new EntryFileItemView(mContext);
			holder.fileItemView = (EntryFileItemView) convertView;
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.fileItemView.setFileItem(list.get(position), this,
				dialogView.getFileMode());
		return holder.fileItemView;
	}

	class ViewHolder {
		EntryFileItemView fileItemView;
	}

	public ArrayList<EntryFileItem> getList() {
		return list;
	}

	public void setList(ArrayList<EntryFileItem> list) {
		this.list = list;
	}

	/**
	 * 打开文件夹，更新文件列表
	 * 
	 * @param file
	 */
	public void openFolder(EntryFileItem entryFileItem) {
		final String path=entryFileItem.path;
		if (entryFileItem != null && !entryFileItem.isDeleted && entryFileItem.isDir) {
			if (!entryFileItem.path.equals(currentDirectory)) {
				// 与当前目录不同
				currentDirectory = entryFileItem.path;
				list.clear();
				  new Thread() {
						@Override
						public void run() {
					 try {
						 Entry metadata = mApi.metadata(path,  null,  true,  false);
						 List<Entry> contents = metadata.contents;
					if (contents != null) {
						for (int i = 0; i < contents.size(); i++) {
							Entry tmpFile = contents.get(i);
							if (!tmpFile.isDir
									&& (dialogView.getFileMode() == VdiskFileDialog.FILE_MODE_OPEN_FOLDER_MULTI || dialogView
											.getFileMode() == VdiskFileDialog.FILE_MODE_OPEN_FOLDER_SINGLE)) {
								continue;
							}
							list.add(new EntryFileItem(tmpFile));
						}
					}
					System.out.print("获取的文件个数为：    ");
					System.out.println(list.size());
					contents = null;
					metadata = null;
					 } catch (VDiskException e) {
							e.printStackTrace();
							System.out.println("file reader error!!");
					}
				 }}.start();
				sortList();
				notifyDataSetChanged();
			}
		}
		dialogView.getPathText().setText(entryFileItem.path);
	}
	/**
	 * 打开根文件夹，更新文件列表
	 * 
	 * @param void
	 */
	public void openFolder() {
		final String path="/";
				list.clear();
				  new Thread() {
						@Override
						public void run() {
					 try {
						 Entry metadata = mApi.metadata(path,  null,  true,  false);
						 List<Entry> contents = metadata.contents;
					if (contents != null) {
						for (int i = 0; i < contents.size(); i++) {
							Entry tmpFile = contents.get(i);
							if (!tmpFile.isDir
									&& (dialogView.getFileMode() == VdiskFileDialog.FILE_MODE_OPEN_FOLDER_MULTI || dialogView
											.getFileMode() == VdiskFileDialog.FILE_MODE_OPEN_FOLDER_SINGLE)) {
								continue;
							}
							list.add(new EntryFileItem(tmpFile));
						}
					}
					System.out.print("获取的文件个数为：    ");
					System.out.println(list.size());
					contents = null;
					metadata = null;
					 } catch (VDiskException e) {
							e.printStackTrace();
							System.out.println("file reader error!!");
					}
				 }}.start();
				sortList();
				notifyDataSetChanged();
		dialogView.getPathText().setText("/");
	}
	/**
	 * 打开文件夹，更新文件列表
	 * 
	 * @param path
	 */
	public void openFolder(String vdiskpath) {
		 final String path=vdiskpath;
			if (!path.equals(currentDirectory)) {
				// 与当前目录不同
				currentDirectory = path;
				list.clear();
			  new Thread() {
					@Override
					public void run() {
				 try {
					 Entry metadata = mApi.metadata(path,  null,  true,  false);
					 List<Entry> contents = metadata.contents;
				if (contents != null) {
					for (int i = 0; i < contents.size(); i++) {
						Entry tmpFile = contents.get(i);
						if (!tmpFile.isDir
								&& (dialogView.getFileMode() == VdiskFileDialog.FILE_MODE_OPEN_FOLDER_MULTI || dialogView
										.getFileMode() == VdiskFileDialog.FILE_MODE_OPEN_FOLDER_SINGLE)) {
							continue;
						}
						list.add(new EntryFileItem(tmpFile));
					}
				}
				System.out.print("获取的文件个数为：    ");
				System.out.println(list.size());
				contents = null;
				metadata = null;
				 } catch (VDiskException e) {
						e.printStackTrace();
						System.out.println("file reader error!!");
				}
			 }}.start();
				sortList();
				notifyDataSetChanged();
			}
		
		dialogView.getPathText().setText(path);
	}
	
	/**
	 * 选择当前目录下所有文件
	 */
	public void selectAll() {
		int mode = dialogView.getFileMode();
		if (mode > VdiskFileDialog.FILE_MODE_OPEN_FILE_MULTI) {
			// 单选模式应该看不到全选按钮才对
			return;
		}
		for (Iterator<EntryFileItem> iterator = list.iterator(); iterator.hasNext();) {
			EntryFileItem entryFileItem = (EntryFileItem) iterator.next();

			if (mode == VdiskFileDialog.FILE_MODE_OPEN_FILE_MULTI
					&& entryFileItem.isDir) {
				// entryFileItem是目录，但是只能选择文件，则返回
				continue;
			}
			if (mode == VdiskFileDialog.FILE_MODE_OPEN_FOLDER_MULTI
					&& !entryFileItem.isDir) {
				// entryFileItem是文件，但是只能选择目录，则返回
				continue;
			}

			entryFileItem.setSelected(true);
		}
		notifyDataSetChanged();
	}

	/**
	 * 取消所有文件的选中状态
	 */
	public void unselectAll() {
		for (Iterator<EntryFileItem> iterator = list.iterator(); iterator.hasNext();) {
			EntryFileItem entryFileItem = (EntryFileItem) iterator.next();
		entryFileItem.setSelected(false);
		}
		notifyDataSetChanged();
	}

	/**
	 * 只在选中时调用，取消选中不调用，且只由FileItemView调用
	 * 
	 * @param fileItem
	 */
	public void selectOne(EntryFileItem entryFileItem) {
		int mode = dialogView.getFileMode();
		if (mode > VdiskFileDialog.FILE_MODE_OPEN_FILE_MULTI) {
			// 如果是单选
			if (mode == VdiskFileDialog.FILE_MODE_OPEN_FILE_SINGLE
					&& entryFileItem.isDir) {
				// fileItem是目录，但是只能选择文件，则返回
				return;
			}
			if (mode == VdiskFileDialog.FILE_MODE_OPEN_FOLDER_SINGLE
					&& !entryFileItem.isDir) {
				// fileItem是文件，但是只能选择目录，则返回
				return;
			}
			for (Iterator<EntryFileItem> iterator = list.iterator(); iterator
					.hasNext();) {
				EntryFileItem tmpItem = (EntryFileItem) iterator.next();
				if (tmpItem.equals(entryFileItem)) {
					tmpItem.setSelected(true);
				} else {
					tmpItem.setSelected(false);
				}
			}
		} else {
			// 如果是多选
			if (mode == VdiskFileDialog.FILE_MODE_OPEN_FILE_MULTI
					&& entryFileItem.isDir) {
				// fileItem是目录，但是只能选择文件，则返回
				return;
			}
			if (mode == VdiskFileDialog.FILE_MODE_OPEN_FOLDER_MULTI
					&& !entryFileItem.isDir) {
				// fileItem是文件，但是只能选择目录，则返回
				return;
			}
			entryFileItem.setSelected(true);
		}

		notifyDataSetChanged();
	}

	public void sortList() {
		FileItemComparator comparator = new FileItemComparator();
		Collections.sort(list, comparator);
	}

	/**
	 * 取消一个的选择，其他逻辑都在FileItemView里面
	 */
	public void unselectOne() {
		dialogView.unselectCheckBox();
	}

	/**
	 * @return 选中的文件列表
	 */
	public ArrayList<Entry> getSelectedFiles() {
		ArrayList<Entry> selectedFiles = new ArrayList<Entry>();
		for (Iterator<EntryFileItem> iterator = list.iterator(); iterator.hasNext();) {
			EntryFileItem fileEntry = iterator.next();// 强制转换为FileEntry
			if (fileEntry.isSelected()) {
				selectedFiles.add(fileEntry);
			}
		}
		return selectedFiles;
	}

	public class FileItemComparator implements Comparator<EntryFileItem> {

		@SuppressLint("DefaultLocale") @Override
		public int compare(EntryFileItem lhs, EntryFileItem rhs) {
			if (lhs.isDir != rhs.isDir) {
				// 如果一个是文件，一个是文件夹，优先按照类型排序
				if (lhs.isDir) {
					return -1;
				} else {
					return 1;
				}
			} else {
				// 如果同是文件夹或者文件，则按名称排序
				return lhs.fileName().toLowerCase(Locale.ENGLISH)
						.compareTo(rhs.fileName().toLowerCase(Locale.ENGLISH));
			}
		}
	}

	public Entry getCurrentDirectory() {
		String path=currentDirectory;
		Entry metadata = null;
			 try {
				   metadata = mApi.metadata(path,  null,  true,  false);
				} catch (VDiskException e) {
					
					e.printStackTrace();
				}
		return metadata;
	}

	public EntryFileDialogView getsDialogView() {
		return dialogView;
	}

	public void setDialogView(EntryFileDialogView dialogView) {
		this.dialogView = dialogView;
	}

}
