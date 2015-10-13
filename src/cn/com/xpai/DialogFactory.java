package cn.com.xpai;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import cn.com.xpai.core.Manager;

public class DialogFactory {

	private static final String TAG = "DialogFactory";
	private static Context mContext = null;

	public static final int CONNECTION_DIALOG = 0x20001;
	public static final int ALERT_DIALOG = 0x20002;
	public static final int ERR_DIALOG = 0x20003;
	public static final int CONNECTION_FAILED_DIALOG = 0x20004;
	public static final int LOGIN_DIALOG = 0x20005;
	public static final int MSG_DIALOG = 0x20006;
	public static final int INFO_DIALOG = 0x20007;
	public static final int SETTING_DIALOG = 0x20008;

	public static void register(Context context) {
		mContext = context;

	}

	public static Dialog getInstance(int dialogID) {
		return getInstance(dialogID, null);
	}

	public static Dialog getInstance(int dialogID, String msgStr) {
		if (mContext == null) {
			Message msg = new Message();
			msg.what = XPHandler.ERR_NOT_REGISTER_DIALOG_FACTORY;
			XPHandler.getInstance().sendMessage(msg);
			return null;
		}
		switch (dialogID) {
		case CONNECTION_DIALOG:
			return ConnectionDialog();
		case ALERT_DIALOG:
			return AlertDialog(msgStr);
		case ERR_DIALOG:
			return ErrorDialog(msgStr);
		case CONNECTION_FAILED_DIALOG:
			return ConnectionFailedDialog(msgStr);
		case LOGIN_DIALOG:
			return LoginDialog();
		case INFO_DIALOG:
			return infoDialog();
		default:
			return null;
		}
	}

	private static Dialog infoDialog() {
		Dialog dialog = new Dialog(mContext);
		dialog.findViewById(R.layout.info);
		return dialog;
	}

	@SuppressLint("NewApi")
	private static Dialog ConnectionDialog() {
		LayoutInflater factory = LayoutInflater.from(mContext);
		final View connectionEntryView = factory.inflate(
				R.layout.connection_dialog, null);
		final CheckBox connectTcpCheckBox = (CheckBox) connectionEntryView
				.findViewById(R.id.mvconnect_tcp_checkbox);
		final CheckBox mCheckBox = (CheckBox) connectionEntryView
				.findViewById(R.id.mvconnect_checkbox);
		if(Config.isConnectToTcpPort) {
			connectTcpCheckBox.setChecked(true);
		} else {
			connectTcpCheckBox.setChecked(false);
		}
		
		Dialog dialog = new AlertDialog.Builder(mContext).setTitle(R.string.connection_dialog_title)
				.setView(connectionEntryView).setPositiveButton(
						R.string.connection_dialog_connect,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String mCode = ((EditText) connectionEntryView
										.findViewById(R.id.mvcode_edit))
										.getText().toString();
								String mHost = ((EditText) connectionEntryView
										.findViewById(R.id.mvhost_edit))
										.getText().toString();
								String mPort = ((EditText) connectionEntryView
										.findViewById(R.id.mvport_edit))
										.getText().toString();
								
								Config.serviceCode = mCode;
								Config.mvHost = mHost;
								Config.mvPort = mPort;
								if(Config.isConnectToTcpPort &&
										(mPort == null || "".equals(mPort))) {
									Toast.makeText(mContext, "端口号不能为空!", Toast.LENGTH_LONG).show();
									return;
								}
								if(connectTcpCheckBox.isChecked()) {
									Config.isConnectToTcpPort = true;
									try {
										Manager.connectVS(mHost, Integer.parseInt(mPort), 
													Config.netTimeout * 1000, Config.serviceCode, 0);
									} catch (NumberFormatException e) {
										// TODO Auto-generated catch block
										Toast.makeText(mContext, "非法的端口号!", Toast.LENGTH_LONG).show();
										e.printStackTrace();
									}
								} else if(mCheckBox.isChecked()) {
									Config.isConnectToTcpPort = false;
									Manager.connectCloud(Config.getVSUrl,
											Config.netTimeout * 1000, Config.serviceCode, 0);
								} else {
									Config.isConnectToZhiBoYun = false;
									Config.isConnectToTcpPort = false;
									try {
										Manager.initNet(mHost, Integer.parseInt(mPort), 
													Config.netTimeout * 1000, Config.serviceCode, 0);
									} catch (NumberFormatException e) {
										// TODO Auto-generated catch block
										Toast.makeText(mContext, "非法的端口号!", Toast.LENGTH_LONG).show();
										e.printStackTrace();
									}
								}
								Config.save();
							}
						}).setNegativeButton(R.string.connection_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								if(connectTcpCheckBox.isChecked()){
									Config.isConnectToTcpPort = true;
								} else {
									Config.isConnectToTcpPort = false;
								}
								Config.save();
							}
						}).create();
		((EditText) connectionEntryView.findViewById(R.id.mvcode_edit))
		.setText(Config.serviceCode);
		final EditText mvHostET = (EditText) connectionEntryView
				.findViewById(R.id.mvhost_edit);
		mvHostET.setText(Config.mvHost);
		final EditText mvPortET = (EditText) connectionEntryView
				.findViewById(R.id.mvport_edit);
		mvPortET.setText(Config.mvPort);
		if(Config.isConnectToZhiBoYun) {
			mCheckBox.setChecked(true);
			updataView(true, mvHostET, mvPortET, mCheckBox, connectTcpCheckBox);
		} else {
			mCheckBox.setChecked(false);
			updataView(false, mvHostET, mvPortET, mCheckBox, connectTcpCheckBox);
		}
		mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				// TODO Auto-generated method stub
				updataView(isChecked, mvHostET, mvPortET, mCheckBox, connectTcpCheckBox);
			}
		});
		
		connectTcpCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					updataView(!isChecked, mvHostET, mvPortET, mCheckBox, connectTcpCheckBox);
				}
			}
		});
		return dialog;
	}

	@SuppressLint("NewApi")
	private static void updataView(boolean isChecked, EditText mvHostET,
			EditText mvPortET, CheckBox connectZBYCheckBox, CheckBox connectTcpCheckBox){
		if(isChecked) {
			mvHostET.setInputType(InputType.TYPE_NULL);
			mvHostET.getBackground().setAlpha(100);
			mvPortET.setInputType(InputType.TYPE_NULL);
			mvPortET.getBackground().setAlpha(100);
			connectTcpCheckBox.setChecked(false);
			connectTcpCheckBox.setAlpha(100);
			Config.isConnectToZhiBoYun = true;
			Config.isConnectToTcpPort = false;
		} else {
			mvHostET.setText(Config.mvHost);
			mvHostET.setInputType(InputType.TYPE_CLASS_TEXT);
			mvHostET.getBackground().setAlpha(255);
			mvPortET.setText(Config.mvPort);
			mvPortET.setInputType(InputType.TYPE_CLASS_TEXT);
			mvPortET.getBackground().setAlpha(255);
			connectZBYCheckBox.setChecked(false);
			connectTcpCheckBox.setAlpha(255);
			Config.isConnectToZhiBoYun = false;
			Config.isConnectToTcpPort = true;
		}
		Config.save();
	}
	
	private static Dialog AlertDialog(String msg) {
		return new AlertDialog.Builder(mContext).setIcon(R.drawable.alert)
				.setTitle(R.string.msg_dialog_title).setMessage(msg)
				.setPositiveButton(R.string.msg_dialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								/* User clicked OK so do some stuff */
							}
						}).create();
	}

	private static Dialog ErrorDialog(String errMsg) {
		return new AlertDialog.Builder(mContext).setIcon(R.drawable.stop)
				.setTitle(R.string.error_dialog_title).setMessage(errMsg)
				.setPositiveButton(R.string.error_dialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								/* User clicked OK so do some stuff */
							}
						}).create();
	}



	private static Dialog ConnectionFailedDialog(String errMsg) {
		return new AlertDialog.Builder(mContext).setIcon(R.drawable.stop)
				.setTitle(R.string.error_dialog_title).setMessage(errMsg)
				.setPositiveButton(R.string.connection_failed_dialog_connect,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								ConnectionDialog().show();
							}
						}).setNegativeButton(
						R.string.connection_failed_dialog_exit,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								Message msg = new Message();
								msg.what = XPHandler.EXIT_APP;
								XPHandler.getInstance().sendMessage(msg);
							}
						}).create();
	}

	private static Dialog LoginDialog() {
		LayoutInflater factory = LayoutInflater.from(mContext);
		final View loginView = factory.inflate(R.layout.login_dialog, null);
		Dialog loginDialog = new AlertDialog.Builder(mContext).setTitle(R.string.login_dialog_title).setView(
				loginView).setPositiveButton(R.string.login_dialog_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String mUserName = ((EditText) loginView
								.findViewById(R.id.mv_username_edit)).getText()
								.toString();
						String mPassword = ((EditText) loginView
								.findViewById(R.id.mv_password_edit)).getText()
								.toString();
						Config.userName = mUserName;
						Config.userPass = mPassword;
						Config.save();
						Manager.tryLogin(Config.userName, Config.userPass,
								Config.serviceCode);
					}
				}).setNegativeButton(R.string.login_dialog_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				}).create();
		((EditText) loginView.findViewById(R.id.mv_username_edit))
				.setText(Config.userName);
		((EditText) loginView.findViewById(R.id.mv_password_edit))
				.setText(Config.userPass);
		return loginDialog;
	}
	
	static void confirmDialog(String title, DialogInterface.OnClickListener yesListenser) {
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setMessage(title);
		builder.setTitle("提示");
		builder.setPositiveButton(R.string.dialog_confirm, yesListenser);

		builder.setNegativeButton(R.string.dialog_cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		builder.create().show();
	}

}